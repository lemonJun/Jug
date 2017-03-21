package lemon.jug.web.connector;

public class NioConfig {

    private int so_back_log = 128;
    private boolean so_keep_alive = true;

    public int getSo_back_log() {
        return so_back_log;
    }

    public void setSo_back_log(int so_back_log) {
        this.so_back_log = so_back_log;
    }

    public boolean isSo_keep_alive() {
        return so_keep_alive;
    }

    public void setSo_keep_alive(boolean so_keep_alive) {
        this.so_keep_alive = so_keep_alive;
    }

}
