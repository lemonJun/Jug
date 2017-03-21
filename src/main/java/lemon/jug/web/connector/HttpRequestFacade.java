package lemon.jug.web.connector;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.security.Principal;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.servlet.AsyncContext;
import javax.servlet.DispatcherType;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletInputStream;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpUpgradeHandler;
import javax.servlet.http.Part;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpHeaders;
import lemon.jug.web.servlet.ServletByteArrayInputStream;
import lemon.jug.web.servlet.XcafeServletInputStream;
import lemon.jug.web.servlet.session.ISessionManager;
import lemon.jug.web.servlet.session.SessionManager;
import lemon.jug.web.util.RequestUtil;
import lemon.jug.web.util.StringUtils;

public class HttpRequestFacade implements HttpServletRequest {

    private FullHttpRequest fullHttpRequest = null;

    //reuqest scope 
    private Map<String, Object> requestScopeMap = null;

    //servlet param
    private Cookie[] cookies = null;
    private List<String> headerNames = null;
    private Map<String, List<String>> headersMap = null;
    private String queryString = null;
    private StringBuffer requestUrl = null;
    private Map<String, String[]> parameterMap = Maps.newHashMap();

    //Session
    private ISessionManager sessionManager = null;
    private String sessionId = null;
    private boolean sessionIdFromCookie = true;

    private boolean active = true;
    private final Map<String, Object> attributes = new LinkedHashMap<String, Object>();

    private static final String CONTENT_TYPE_HEADER = "Content-Type";
    private final List<Locale> locales = Lists.newLinkedList();

    public HttpRequestFacade(FullHttpRequest fullHttpRequest) {
        this.fullHttpRequest = fullHttpRequest;
        this.headersMap = new HashMap<String, List<String>>();
        this.requestScopeMap = new HashMap<String, Object>();
        try {
            this.sessionManager = new SessionManager(); //SessionManager
        } catch (Exception e) {
            e.printStackTrace();
        }
        this.locales.add(Locale.CHINA);
        //        this.genarateSession();
        //        this.serviceConfig.listenerManager.fireEvent(EventUtils.REQUEST_INITIALIZED, this.serviceConfig.listenerManager.getEventUtils().buildServletRequestEvent(this));
    }

    @Override
    public Object getAttribute(String name) {
        return attributes.get(name);
    }

    protected void checkActive() throws IllegalStateException {
        if (!this.active) {
            throw new IllegalStateException("Request is not active anymore");
        }
    }

    @Override
    public Enumeration<String> getAttributeNames() {
        checkActive();
        return Collections.enumeration(new LinkedHashSet<String>(this.attributes.keySet()));
    }

    @Override
    public String getCharacterEncoding() {
        return this.characterEncoding;
    }

    @Override
    public void setCharacterEncoding(String characterEncoding) throws UnsupportedEncodingException {
        this.characterEncoding = characterEncoding;
        updateContentTypeHeader();
    }
    
    private String characterEncoding = "UTF-8";
    private String contentType;
    private static final String CHARSET_PREFIX = "charset=";

    private void updateContentTypeHeader() {
        if (StringUtils.hasLength(this.contentType)) {
            StringBuilder sb = new StringBuilder(this.contentType);
            if (!this.contentType.toLowerCase().contains(CHARSET_PREFIX) && StringUtils.hasLength(this.characterEncoding)) {
                sb.append(";").append(CHARSET_PREFIX).append(this.characterEncoding);
            }
            doAddHeaderValue(CONTENT_TYPE_HEADER, sb.toString(), true);
        }
    }

    @SuppressWarnings("rawtypes")
    private void doAddHeaderValue(String name, Object value, boolean replace) {
        HttpHeaders headers = fullHttpRequest.headers();
        String header = headers.get(name).toString();
        Preconditions.checkNotNull(value, "Header value must not be null");
        if (header == null || replace) {
            headers.add(name, "");
        }
        if (value instanceof Collection) {
            headers.add(headers);
            headers.add(name, (Collection) value);
        } else if (value.getClass().isArray()) {
            String[] array = (String[]) value;
            if (null != array && array.length > 0) {
                headers.add(name, Arrays.asList(array));
            }
        } else {
            headers.add(name, (String) value);
        }
    }

    @Override
    public int getContentLength() {
        return (fullHttpRequest.content() != null ? fullHttpRequest.content().readableBytes() : -1);
    }

    @Override
    public long getContentLengthLong() {
        return getContentLength();
    }

    @Override
    public String getContentType() {
        return contentType;
    }

    private static final ServletInputStream EMPTY_SERVLET_INPUT_STREAM = new XcafeServletInputStream(new ByteArrayInputStream(new byte[0]));

    @Override
    public ServletInputStream getInputStream() throws IOException {
        if (fullHttpRequest.content() != null) {
            ByteArrayInputStream bins = new ByteArrayInputStream(this.fullHttpRequest.content().array());
            return new ServletByteArrayInputStream(bins);
        } else {
            return EMPTY_SERVLET_INPUT_STREAM;
        }
    }

    //
    @Override
    public String getParameter(String name) {
        String[] arr = (name != null ? getParameterMap().get(name) : null);
        return (arr != null && arr.length > 0 ? arr[0] : null);
    }

    @Override
    public Enumeration<String> getParameterNames() {
        return Collections.enumeration(parameterMap.keySet());
    }

    @Override
    public String[] getParameterValues(String name) {
        return (name != null ? parameterMap.get(name) : null);
    }

    //此方法可以借用tomcat的那个工具类
    @Override
    public Map<String, String[]> getParameterMap() {
        try {
            RequestUtil.parseParameters(parameterMap, this.getQueryString(), "utf-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return this.parameterMap;
    }

    @Override
    public String getProtocol() {
        return fullHttpRequest.protocolVersion().protocolName().toString();
    }

    @Override
    public String getScheme() {
        //        return this.serviceConfig.useSSL ? "https" : "http";
        return "http";
    }

    private static final String HOST_HEADER = "Host";
    public static final String DEFAULT_SERVER_NAME = "localhost";
    private String serverName = DEFAULT_SERVER_NAME;

    public void setServerName(String serverName) {
        this.serverName = serverName;
    }

    @Override
    public String getServerName() {
        return "";//WebAppContext.getservername
    }

    //TODO 此值应该从配置中获取
    @Override
    public int getServerPort() {
        return 80;
    }

    @Override
    public BufferedReader getReader() throws IOException {
        if (fullHttpRequest.content() != null) {
            InputStream sourceStream = new ByteArrayInputStream(fullHttpRequest.content().array());
            Reader sourceReader = (this.characterEncoding != null) ? new InputStreamReader(sourceStream, this.characterEncoding) : new InputStreamReader(sourceStream);
            return new BufferedReader(sourceReader);
        } else {
            return null;
        }
    }

    //TODO 未实现 
    @Override
    public String getRemoteAddr() {
        return null;
    }

    @Override
    public String getRemoteHost() {
        return null;
    }

    @Override
    public void setAttribute(String name, Object value) {
        checkActive();
        Preconditions.checkNotNull(name, "Attribute name must not be null");
        if (value != null) {
            this.attributes.put(name, value);
        } else {
            this.attributes.remove(name);
        }
    }

    @Override
    public void removeAttribute(String name) {
        checkActive();
        Preconditions.checkNotNull(name, "Attribute name must not be null");
        this.attributes.remove(name);
    }

    @Override
    public Locale getLocale() {
        return locales.get(0);
    }

    @Override
    public Enumeration<Locale> getLocales() {
        return Collections.enumeration(this.locales);
    }

    private boolean secure = false;

    public void setSecure(boolean secure) {
        this.secure = secure;
    }

    @Override
    public boolean isSecure() {
        return secure;
    }

    @Override
    public RequestDispatcher getRequestDispatcher(String path) {
        return new JugRequestDispatcher(path);
    }

    private final ServletContext servletContext = null;

    @Override
    public String getRealPath(String path) {
        return servletContext.getRealPath(path);
    }

    //TODO
    @Override
    public int getRemotePort() {
        return 0;
    }

    @Override
    public String getLocalName() {
        //        return this.serviceConfig.serverName;
        return "";//WebAppContext.getservername
    }

    //TODO
    @Override
    public String getLocalAddr() {
        return "ip";
    }

    //TODO
    @Override
    public int getLocalPort() {
        return 0;
    }

    @Override
    public ServletContext getServletContext() {
        return servletContext;
    }

    private boolean asyncStarted = false;

    private boolean asyncSupported = false;

    @Override
    public AsyncContext startAsync() throws IllegalStateException {
        return startAsync(this, null);
    }

    @Override
    public AsyncContext startAsync(ServletRequest servletRequest, ServletResponse servletResponse) throws IllegalStateException {
        if (!this.asyncSupported) {
            throw new IllegalStateException("Async not supported");
        }
        this.asyncStarted = true;
        return new JugAsyncContext(servletRequest, servletResponse);
    }

    public void setAsyncStarted(boolean asyncStarted) {
        this.asyncStarted = asyncStarted;
    }

    @Override
    public boolean isAsyncStarted() {
        return false;
    }

    @Override
    public boolean isAsyncSupported() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public AsyncContext getAsyncContext() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public DispatcherType getDispatcherType() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getAuthType() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Cookie[] getCookies() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public long getDateHeader(String name) {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public String getHeader(String name) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Enumeration<String> getHeaders(String name) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Enumeration<String> getHeaderNames() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public int getIntHeader(String name) {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public String getMethod() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getPathInfo() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getPathTranslated() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getContextPath() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getQueryString() {
        return null;
    }

    @Override
    public String getRemoteUser() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public boolean isUserInRole(String role) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public Principal getUserPrincipal() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getRequestedSessionId() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getRequestURI() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public StringBuffer getRequestURL() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getServletPath() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public HttpSession getSession(boolean create) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public HttpSession getSession() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String changeSessionId() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public boolean isRequestedSessionIdValid() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean isRequestedSessionIdFromCookie() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean isRequestedSessionIdFromURL() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean isRequestedSessionIdFromUrl() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean authenticate(HttpServletResponse response) throws IOException, ServletException {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public void login(String username, String password) throws ServletException {
        // TODO Auto-generated method stub

    }

    @Override
    public void logout() throws ServletException {
        // TODO Auto-generated method stub

    }

    @Override
    public Collection<Part> getParts() throws IOException, ServletException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Part getPart(String name) throws IOException, ServletException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public <T extends HttpUpgradeHandler> T upgrade(Class<T> handlerClass) throws IOException, ServletException {
        // TODO Auto-generated method stub
        return null;
    }

}
