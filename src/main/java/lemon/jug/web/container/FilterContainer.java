package lemon.jug.web.container;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import lemon.jug.web.servlet.filter.ApplicationFilterChain;

/**
 * 过滤器链的处理流程
 *
 * FilterContainer
 *                -invoke-> IApplicationFilterChain
 *                                                 -doFilter-> Filter
 *                                                 -doFilter-> Filter
 *                                                 -invokeLast-> invoke next container
 */
public class FilterContainer implements Container, IFilterContainer {

    private static final Logger logger = LoggerFactory.getLogger(FilterContainer.class);

    private Container next = null;
    private HttpServletRequest httpServletRequest = null;
    private HttpServletResponse httpServletResponse = null;

    public void invoke(HttpServletRequest request, HttpServletResponse response) {
        this.httpServletRequest = request;
        this.httpServletResponse = response;
        ApplicationFilterChain.getInstance().reset();
        try {
            logger.info(request.getRequestURI());
            ApplicationFilterChain.getInstance().doFilter(request, response);
        } catch (Exception e) {
            logger.error("", e);
        }
    }

    public void setNext(Container container) {
        next = container;
    }

    public Container getNext() {
        return next;
    }

    public void invokeLast() {
        next.invoke(httpServletRequest, httpServletResponse);
    }

    public void start() {

    }

    public void stop() {
        ApplicationFilterChain.getInstance().destroy();
    }

    @Override
    public void init() throws Exception {

    }

    @Override
    public boolean isRunning() {
        return false;
    }

    @Override
    public boolean isStarted() {
        return false;
    }

    @Override
    public boolean isStarting() {
        return false;
    }

    @Override
    public boolean isStopping() {
        return false;
    }

    @Override
    public boolean isStopped() {
        return false;
    }

    @Override
    public boolean isFailed() {
        return false;
    }

    @Override
    public void addLifeCycleListener(Listener listener) {

    }

    @Override
    public void removeLifeCycleListener(Listener listener) {

    }

    @Override
    public void initialize() {

    }
}
