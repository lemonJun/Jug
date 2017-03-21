package lemon.jug.web.servlet.session;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpSession;

/**
 * Created by lemon on 16/1/20.
 */
public interface ISession extends HttpSession {

    public void setId(String sessionId);

    public void setNew(boolean _new);

    public void setValid(boolean valid);

    public void setCreationTime(long time);

    public long getCreationTime();

    public void setMaxInactiveInterval(int interval);

    public void setLastAccessedTime(long time);

    public void setServletContext(ServletContext servletContext);
}
