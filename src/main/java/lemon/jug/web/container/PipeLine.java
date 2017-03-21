package lemon.jug.web.container;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class PipeLine implements Container {

    private Container head = null;
    private Container tail = null;

    public void addContainer(Container container) {
        container.initialize();
        if (head == null) {
            head = tail = container;
        } else {
            tail.setNext(container);
            tail = container;
        }
    }
    
    public Container getContainer() {
        return head;
    }

    public void invoke(HttpServletRequest request, HttpServletResponse response) {
        if (head == null) {
            return;
        }
        head.invoke(request, response);
    }

    public void initialize() {

    }

    @Override
    public void start() {
        Container tHead = head;
        while (tHead != null) {
            try {
                tHead.start();
            } catch (Exception e) {
                e.printStackTrace();
            }
            tHead = tHead.getNext();
        }
    }

    @Override
    public void stop() {
        Container tHead = head;
        while (tHead != null) {
            try {
                tHead.stop();
            } catch (Exception e) {
                e.printStackTrace();
            }
            tHead = tHead.getNext();
        }
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
    public void setNext(Container container) {

    }

    @Override
    public Container getNext() {
        return null;
    }

}
