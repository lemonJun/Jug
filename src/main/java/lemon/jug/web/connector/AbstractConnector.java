package lemon.jug.web.connector;

import java.io.IOException;

public abstract class AbstractConnector implements NetworkConnector {
    private volatile String host;
    private volatile int port = 0;
    
    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    @Override
    public int getLocalPort() {
        return -1;
    }

    @Override
    public void open() throws IOException {
    }

    @Override
    public void close() {
    }

    @Override
    public String toString() {
        return String.format("%s{%s:%d}", super.toString(), getHost() == null ? "0.0.0.0" : getHost(), getLocalPort() <= 0 ? getPort() : getLocalPort());
    }
}
