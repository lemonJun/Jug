package lemon.jug.web.connector;

import java.util.concurrent.Future;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NettyServerConnector extends AbstractConnector {

    private static final Logger logger = LoggerFactory.getLogger(NettyServerConnector.class);
    private final NettyIO netty = new NettyIO();

    @Override
    public boolean isOpen() {
        return false;
    }

    @Override
    public void init() throws Exception {
        netty.initialize();
    }

    @Override
    public void start() throws Exception {
        netty.start(getHost(), getPort());
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

    @Override
    public Future<Void> shutdown() {
        return null;
    }

}
