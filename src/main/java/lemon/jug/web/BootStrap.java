package lemon.jug.web;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import lemon.jug.web.component.LifeCycle;
import lemon.jug.web.connector.NetworkConnector;
import lemon.jug.web.webapp.WebApp;

public class BootStrap implements LifeCycle {

    private static final Logger logger = LoggerFactory.getLogger(BootStrap.class);

    private NetworkConnector connector;
    private WebApp appcontext;

    public NetworkConnector getConnector() {
        return connector;
    }

    public void setConnector(NetworkConnector connector) {
        this.connector = connector;
    }
    
    public WebApp getAppcontext() {
        return appcontext;
    }

    public void setAppcontext(WebApp appcontext) {
        this.appcontext = appcontext;
    }

    @Override
    public void init() throws Exception {
        try {
            appcontext.init();
            logger.info("init app..");
            connector.init();
            logger.info("init connector..");
        } catch (Exception e) {
            logger.error("init error", e);

        }
    }

    @Override
    public void start() throws Exception {
        try {
            connector.start();
            logger.info(String.format("jug server started host:%s port:%s", connector.getHost(), connector.getPort()));
        } catch (Exception e) {
            logger.error("start error", e);
        }
    }

    @Override
    public void stop() throws Exception {

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

}
