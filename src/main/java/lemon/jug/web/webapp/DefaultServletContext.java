package lemon.jug.web.webapp;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Enumeration;
import java.util.EventListener;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import javax.servlet.Filter;
import javax.servlet.FilterRegistration;
import javax.servlet.RequestDispatcher;
import javax.servlet.Servlet;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRegistration;
import javax.servlet.SessionCookieConfig;
import javax.servlet.SessionTrackingMode;
import javax.servlet.descriptor.JspConfigDescriptor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import lemon.jug.web.servlet.session.ISessionManager;
import lemon.jug.web.util.EnumerationImplIterator;

public class DefaultServletContext implements ServletContext {

    private static final Logger logger = LoggerFactory.getLogger(DefaultServletContext.class);

    private String contextPath = null;
    private Map<String, String> initParams = null;
    private ConcurrentHashMap<String, Object> attribute = new ConcurrentHashMap<String, Object>();
    private ISessionManager sessionManager = null;

    //instance for each request
    public ServletContext getInstance() {
        return this;
    }

    public void setContextPath(String contextPath) {
        this.contextPath = contextPath;
    }
    
    public void setInitParams(Map<String, String> initParams) {
        this.initParams = initParams;
    }

    public void setSessionManager(ISessionManager sessionManager) {
        this.sessionManager = sessionManager;
    }

    public ISessionManager getSessionManager() {
        return this.sessionManager;
    }
    @Override
    public String getContextPath() {
        return this.contextPath;
    }
    @Override
    public ServletContext getContext(String uripath) {
        // TODO Auto-generated method stub
        return null;
    }
    @Override
    public int getMajorVersion() {
        // TODO Auto-generated method stub
        return 0;
    }
    @Override
    public int getMinorVersion() {
        // TODO Auto-generated method stub
        return 0;
    }
    @Override
    public int getEffectiveMajorVersion() {
        return 0;
    }
    @Override
    public int getEffectiveMinorVersion() {
        return 0;
    }
    @Override
    public String getMimeType(String file) {
        // TODO Auto-generated method stub
        return null;
    }
    @Override
    @SuppressWarnings("rawtypes")
    public Set getResourcePaths(String path) {
        // TODO Auto-generated method stub
        return null;
    }
    
    @Override
    public URL getResource(String path) throws MalformedURLException {
        return new URL("file", null, path);
    }
    @Override
    public InputStream getResourceAsStream(String path) {
        File file = new File(path);
        InputStream ins = null;
        try {
            ins = new FileInputStream(file);
        } catch (FileNotFoundException e) {
            logger.info("", e);
        }
        return ins;
    }
    @Override
    public RequestDispatcher getRequestDispatcher(String path) {
        return null;
    }
    @Override
    public RequestDispatcher getNamedDispatcher(String name) {
        return null;
    }

    public Servlet getServlet(String name) throws ServletException {
        Servlet servlet = null;
        return servlet;
    }

    @SuppressWarnings("rawtypes")
    public Enumeration getServlets() {
        // TODO Auto-generated method stub
        return null;
    }

    @SuppressWarnings("rawtypes")
    public Enumeration getServletNames() {
        // TODO Auto-generated method stub
        return null;
    }

    public void log(String msg) {
        logger.info(msg);
    }

    public void log(Exception exception, String msg) {
        logger.info(msg, exception);
    }

    public void log(String message, Throwable throwable) {
        logger.info(message, throwable);
    }

    /**
     * 
     */
    public String getRealPath(String path) {
        String realPath = null;
        try {
            realPath = new File(path).getCanonicalPath();
        } catch (Exception e) {
        } finally {
            return realPath;
        }
    }

    public String getServerInfo() {
        // TODO Auto-generated method stub
        return null;
    }

    public String getInitParameter(String name) {
        return this.initParams.get(name);
    }

    @SuppressWarnings("rawtypes")
    public Enumeration getInitParameterNames() {
        return new EnumerationImplIterator<String>(this.initParams.keySet().iterator());
    }

    public boolean setInitParameter(String s, String s1) {
        return false;
    }

    public Object getAttribute(String name) {
        return this.attribute.get(name);
    }

    @SuppressWarnings("rawtypes")
    public Enumeration getAttributeNames() {
        return this.attribute.keys();
    }

    public void setAttribute(String name, Object object) {
        Object oldValue = this.attribute.put(name, object);

        //        if (oldValue != null) {
        //            serviceConfig.listenerManager.fireEvent(EventUtils.CONTEXT_ATTRIBUTE_REPLACED, serviceConfig.listenerManager.getEventUtils().buildServletContextAttributeEvent(name, oldValue));
        //        } else {
        //            serviceConfig.listenerManager.fireEvent(EventUtils.CONTEXT_ATTRIBUTE_ADDED, serviceConfig.listenerManager.getEventUtils().buildServletContextAttributeEvent(name, object));
        //        }
    }

    public void removeAttribute(String name) {
        Object value = this.attribute.remove(name);

        //        serviceConfig.listenerManager.fireEvent(EventUtils.CONTEXT_ATTRIBUTE_REMOVED, serviceConfig.listenerManager.getEventUtils().buildServletContextAttributeEvent(name, value));
    }


    public ServletRegistration.Dynamic addServlet(String s, String s1) {
        return null;
    }

    public ServletRegistration.Dynamic addServlet(String s, Servlet servlet) {
        return null;
    }

    public ServletRegistration.Dynamic addServlet(String s, Class<? extends Servlet> aClass) {
        return null;
    }

    public <T extends Servlet> T createServlet(Class<T> aClass) throws ServletException {
        return null;
    }

    public ServletRegistration getServletRegistration(String s) {
        return null;
    }

    public Map<String, ? extends ServletRegistration> getServletRegistrations() {
        return null;
    }

    public FilterRegistration.Dynamic addFilter(String s, String s1) {
        return null;
    }

    public FilterRegistration.Dynamic addFilter(String s, Filter filter) {
        return null;
    }

    public FilterRegistration.Dynamic addFilter(String s, Class<? extends Filter> aClass) {
        return null;
    }

    public <T extends Filter> T createFilter(Class<T> aClass) throws ServletException {
        return null;
    }

    public FilterRegistration getFilterRegistration(String s) {
        return null;
    }

    public Map<String, ? extends FilterRegistration> getFilterRegistrations() {
        return null;
    }

    public SessionCookieConfig getSessionCookieConfig() {
        return null;
    }

    public void setSessionTrackingModes(Set<SessionTrackingMode> set) {

    }

    public Set<SessionTrackingMode> getDefaultSessionTrackingModes() {
        return null;
    }

    public Set<SessionTrackingMode> getEffectiveSessionTrackingModes() {
        return null;
    }

    public void addListener(String s) {

    }

    public <T extends EventListener> void addListener(T t) {

    }

    public void addListener(Class<? extends EventListener> aClass) {

    }

    public <T extends EventListener> T createListener(Class<T> aClass) throws ServletException {
        return null;
    }

    public JspConfigDescriptor getJspConfigDescriptor() {
        return null;
    }

    public ClassLoader getClassLoader() {
        return this.getClassLoader();
    }

    public void declareRoles(String... strings) {

    }

    public String getVirtualServerName() {
        return null;
    }
    
    
    public String getServletContextName() {
        return null;
    }

}
