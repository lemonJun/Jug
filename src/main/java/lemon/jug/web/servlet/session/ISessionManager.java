package lemon.jug.web.servlet.session;

import javax.servlet.http.HttpSession;

/**
 * Created by lemon on 16/1/20.
 */
public interface ISessionManager extends Runnable {

    public HttpSession getSession(String sessionid, boolean create);

    public boolean isRequestedSessionIdValid(String sessionId);

    public void loadSession();

    public void saveSession();
}
