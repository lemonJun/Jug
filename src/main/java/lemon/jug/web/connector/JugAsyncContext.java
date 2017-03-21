package lemon.jug.web.connector;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.AsyncContext;
import javax.servlet.AsyncEvent;
import javax.servlet.AsyncListener;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.common.base.Preconditions;

/**
 * 值得好好研究下spring的bean工具层   
 *
 * @author lemon
 * @date  2016年11月4日 下午3:12:07
 * @see
 */
public class JugAsyncContext implements AsyncContext {

    private final HttpServletRequest request;

    private final HttpServletResponse response;

    private final List<AsyncListener> listeners = new ArrayList<AsyncListener>();

    private String dispatchedPath;

    private long timeout = 10 * 1000L; // 10 seconds is Tomcat's default

    private final List<Runnable> dispatchHandlers = new ArrayList<Runnable>();

    public JugAsyncContext(ServletRequest request, ServletResponse response) {
        this.request = (HttpServletRequest) request;
        this.response = (HttpServletResponse) response;
    }

    public void addDispatchHandler(Runnable handler) {
        Preconditions.checkNotNull(handler);
        this.dispatchHandlers.add(handler);
    }

    @Override
    public ServletRequest getRequest() {
        return this.request;
    }

    @Override
    public ServletResponse getResponse() {
        return this.response;
    }

    @Override
    public boolean hasOriginalRequestAndResponse() {
        //        return (this.request instanceof FullHttpRequestWrapper) && (this.response instanceof FullHttpResponseWrapper);
        return (this.request instanceof HttpRequestFacade) && (this.response instanceof HttpResponseFacade);
    }

    @Override
    public void dispatch() {
        dispatch(this.request.getRequestURI());
    }

    @Override
    public void dispatch(String path) {
        dispatch(null, path);
    }

    @Override
    public void dispatch(ServletContext context, String path) {
        this.dispatchedPath = path;
        for (Runnable r : this.dispatchHandlers) {
            r.run();
        }
    }

    public String getDispatchedPath() {
        return this.dispatchedPath;
    }

    @Override
    public void complete() {
        HttpRequestFacade fullRequest = WebUtils.getNativeRequest(request, HttpRequestFacade.class);
        if (fullRequest != null) {
            fullRequest.setAsyncStarted(false);
        }
        for (AsyncListener listener : this.listeners) {
            try {
                listener.onComplete(new AsyncEvent(this, this.request, this.response));
            } catch (IOException ex) {
                throw new IllegalStateException("AsyncListener failure", ex);
            }
        }
    }

    @Override
    public void start(Runnable runnable) {
        runnable.run();
    }

    @Override
    public void addListener(AsyncListener listener) {
        this.listeners.add(listener);
    }

    @Override
    public void addListener(AsyncListener listener, ServletRequest request, ServletResponse response) {
        this.listeners.add(listener);
    }

    public List<AsyncListener> getListeners() {
        return this.listeners;
    }

    //
    @Override
    public <T extends AsyncListener> T createListener(Class<T> clazz) throws ServletException {
        try {
            return clazz.newInstance();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public void setTimeout(long timeout) {
        this.timeout = timeout;
    }

    @Override
    public long getTimeout() {
        return this.timeout;
    }

}
