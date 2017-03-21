package lemon.jug.web.connector;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;

public class JugRequestDispatcher implements RequestDispatcher {

    private final Logger logger = LoggerFactory.getLogger(JugRequestDispatcher.class);

    private final String resource;

    public JugRequestDispatcher(String resource) {
        Preconditions.checkNotNull(resource, "resource must not be null");
        this.resource = resource;
    }

    @Override
    public void forward(ServletRequest request, ServletResponse response) {
        Preconditions.checkNotNull(request, "Request must not be null");
        Preconditions.checkNotNull(response, "Response must not be null");
        if (response.isCommitted()) {
            throw new IllegalStateException("Cannot perform forward - response is already committed");
        }
        getHttpServletResponse(response).setForwardedUrl(this.resource);
        if (logger.isDebugEnabled()) {
            logger.debug("MockRequestDispatcher: forwarding to [" + this.resource + "]");
        }
    }
    
    @Override
    public void include(ServletRequest request, ServletResponse response) {
        Preconditions.checkNotNull(request, "Request must not be null");
        Preconditions.checkNotNull(response, "Response must not be null");
        getHttpServletResponse(response).addIncludedUrl(this.resource);
        if (logger.isDebugEnabled()) {
            logger.debug("XcafeRequestDispatcher: including [" + this.resource + "]");
        }
    }

    //    protected FullHttpResponseWrapper getHttpServletResponse(ServletResponse response) {
    //        if (response instanceof FullHttpResponseWrapper) {
    //            return (FullHttpResponseWrapper) response;
    //        }
    //        throw new IllegalArgumentException("XcafeRequestDispatcher requires FullHttpResponseWrapper");
    //    }
    protected HttpResponseFacade getHttpServletResponse(ServletResponse response) {
        if (response instanceof HttpResponseFacade) {
            return (HttpResponseFacade) response;
        }
        throw new IllegalArgumentException("XcafeRequestDispatcher requires FullHttpResponseWrapper");
    }

}
