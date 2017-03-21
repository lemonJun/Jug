package lemon.jug.web.container;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import lemon.jug.web.servlet.SimpleMapper;
import lemon.jug.web.util.EventUtils;

public class DefaultContainer implements Container {

    private static final Logger logger = LoggerFactory.getLogger(DefaultContainer.class);

    private SimpleMapper mapper = null;
    private Container next = null;

    public void invoke(HttpServletRequest request, HttpServletResponse response) {
        try {
            HttpServlet servlet = mapper.getHttpServlet(request);
            if (servlet != null) {
                response.setContentType("text/html");
                servlet.service(request, response);
                servlet.destroy();
            } else {
                response.sendError(HttpServletResponse.SC_NOT_FOUND);
            }
        } catch (Exception e) {
            logger.info("", e);
        } finally {
            if (next != null) {
                next.invoke(request, response);
            }
        }
        //        this.serviceConfig.listenerManager.fireEvent(EventUtils.REQUEST_DESTROYED, this.serviceConfig.listenerManager.getEventUtils().buildServletRequestEvent(request));
    }

    public void initialize() {
        this.mapper = new SimpleMapper();
    }

    public void setNext(Container container) {
        next = container;
    }

    public Container getNext() {
        return next;
    }

    public void start() {

    }

    public void stop() {

    }

    @Override
    public void init() throws Exception {
        // TODO Auto-generated method stub
        
    }

    @Override
    public boolean isRunning() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean isStarted() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean isStarting() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean isStopping() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean isStopped() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean isFailed() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public void addLifeCycleListener(Listener listener) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void removeLifeCycleListener(Listener listener) {
        // TODO Auto-generated method stub
        
    }
}
