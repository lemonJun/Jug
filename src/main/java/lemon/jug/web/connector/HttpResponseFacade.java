package lemon.jug.web.connector;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;

import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpResponseStatus;
import lemon.jug.web.connector.NettyHandler.NettyHelper;
import lemon.jug.web.servlet.OutputStreamFacade;
import lemon.jug.web.servlet.ServletByteBufOutputStream;
import lemon.jug.web.util.ResponseHelper;

public class HttpResponseFacade implements HttpServletResponse {

    private FullHttpResponse fullHttpResponse = null;
    private String characterEncoding = null;
    private HttpRequestFacade httpRequestFacade = null;

    private String forwardedUrl;

    private final List<String> includedUrls = Lists.newArrayList();

    public HttpResponseFacade(FullHttpResponse fullHttpResponse) {
        this.fullHttpResponse = fullHttpResponse;
    }

    public void setHttpRequestFacade(HttpRequestFacade httpRequestFacade) {
        this.httpRequestFacade = httpRequestFacade;
    }

    public void setForwardedUrl(String forwardedUrl) {
        this.forwardedUrl = forwardedUrl;
    }

    public String getForwardedUrl() {
        return this.forwardedUrl;
    }

    public String getIncludedUrl() {
        int count = this.includedUrls.size();
        if (count > 1) {
            throw new IllegalStateException("More than 1 URL included - check getIncludedUrls instead: " + this.includedUrls);
        }
        return (count == 1 ? this.includedUrls.get(0) : null);
    }

    public void addIncludedUrl(String includedUrl) {
        Preconditions.checkNotNull(includedUrl, "Included URL must not be null");
        this.includedUrls.add(includedUrl);
    }

    @Override
    public String getCharacterEncoding() {
        return this.characterEncoding;
    }

    @Override
    public String getContentType() {
        return (String) this.fullHttpResponse.headers().get(HttpHeaderNames.CONTENT_TYPE);
    }

    @Override
    public ServletOutputStream getOutputStream() throws IOException {
        ServletByteBufOutputStream sbytebufous = new ServletByteBufOutputStream(fullHttpResponse.content());
        return sbytebufous;
    }

    @Override
    public PrintWriter getWriter() throws IOException {
        PrintWriter printWriter = new PrintWriter(new OutputStreamFacade(fullHttpResponse.content()), true);
        return printWriter;
    }

    @Override
    public void setCharacterEncoding(String charset) {
        this.characterEncoding = charset;
        this.fullHttpResponse.headers().set(HttpHeaderNames.CONTENT_TYPE, "text/html; charset=" + charset);
    }

    @Override
    public void setContentLength(int len) {
        this.fullHttpResponse.headers().set(HttpHeaderNames.CONTENT_LENGTH, String.valueOf(len));
    }

    @Override
    public void setContentLengthLong(long len) {

    }

    @Override
    public void setContentType(String type) {
        this.fullHttpResponse.headers().set(HttpHeaderNames.CONTENT_TYPE, type);
    }

    @Override
    public void setBufferSize(int size) {
        this.fullHttpResponse.content().capacity(size);
    }

    @Override
    public int getBufferSize() {
        return this.fullHttpResponse.content().capacity();
    }

    @Override
    public void flushBuffer() throws IOException {
        //        this.fullHttpResponse.content().clear();

    }

    @Override
    public void resetBuffer() {
        this.fullHttpResponse.content().clear();
    }

    @Override
    public boolean isCommitted() {
        //        return this..isCommitted();
        return true;

    }

    @Override
    public void reset() {
        this.fullHttpResponse.content().clear();
    }

    @Override
    public void setLocale(Locale loc) {

    }

    @Override
    public Locale getLocale() {
        return null;
    }

    @Override
    public void addCookie(Cookie cookie) {
        String cookieString = ResponseHelper.getCookieString(cookie);
        String cookiesString = (String) this.fullHttpResponse.headers().get(HttpHeaderNames.COOKIE);
        if (cookiesString != null) {
            cookiesString += cookieString;
        } else {
            cookiesString = cookieString;
        }
        this.fullHttpResponse.headers().set(HttpHeaderNames.SET_COOKIE, cookiesString);
    }

    @Override
    public boolean containsHeader(String name) {
        return this.fullHttpResponse.headers().contains(name);
    }

    @Override
    public String encodeURL(String url) {
        return encodeURL(url);
    }

    @Override
    public String encodeRedirectURL(String url) {
        return this.encodeURL(url);
    }

    @Override
    public String encodeUrl(String url) {
        return this.encodeURL(url);
    }

    @Override
    public String encodeRedirectUrl(String url) {
        return this.encodeURL(url);
    }

    @Override
    public void sendError(int sc, String msg) throws IOException {
        HttpResponseStatus status = HttpResponseStatus.valueOf(sc);
        this.fullHttpResponse.setStatus(status);
        NettyHelper.setResponseContent(this.fullHttpResponse, msg);
    }

    @Override
    public void sendError(int sc) throws IOException {
        HttpResponseStatus status = HttpResponseStatus.valueOf(sc);
        this.fullHttpResponse.setStatus(status);
    }

    @Override
    public void sendRedirect(String location) throws IOException {

    }

    @Override
    public void setDateHeader(String name, long date) {
        this.fullHttpResponse.headers().set(name, String.valueOf(date));
    }

    @Override
    public void addDateHeader(String name, long date) {
        this.fullHttpResponse.headers().add(name, String.valueOf(date));
    }

    @Override
    public void setHeader(String name, String value) {
        this.fullHttpResponse.headers().set(name, value);
    }

    @Override
    public void addHeader(String name, String value) {
        this.fullHttpResponse.headers().set(name, value);
    }

    @Override
    public void setIntHeader(String name, int value) {
        this.fullHttpResponse.headers().setInt(name, value);
    }

    @Override
    public void addIntHeader(String name, int value) {
        this.fullHttpResponse.headers().addInt(name, value);
    }

    @Override
    public void setStatus(int sc) {
        HttpResponseStatus status = HttpResponseStatus.valueOf(sc);
        this.fullHttpResponse.setStatus(status);
    }

    @Override
    public void setStatus(int sc, String sm) {
        HttpResponseStatus status = HttpResponseStatus.valueOf(sc);
        this.fullHttpResponse.setStatus(status);
        NettyHelper.setResponseContent(this.fullHttpResponse, sm);
    }

    @Override
    public int getStatus() {
        return 0;
    }

    @Override
    public String getHeader(String name) {
        return (String) this.fullHttpResponse.headers().get(name);
    }

    @Override
    public Collection<String> getHeaders(String name) {
        return Arrays.asList((String) this.fullHttpResponse.headers().get(name));
    }

    @Override
    public Collection<String> getHeaderNames() {
        List<String> nameList = Lists.newArrayList();
        Set<CharSequence> nameListCh = this.fullHttpResponse.headers().names();
        for (Iterator<CharSequence> ite = nameListCh.iterator(); ite.hasNext();) {
            nameList.add(ite.next().toString());
        }
        return nameList;
    }

}
