package lemon.jug.web.servlet.session;

import java.security.SecureRandom;
import java.util.Enumeration;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpSession;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by lemon on 16/1/20.
 */
public class SessionManager implements ISessionManager {

    private static final Logger logger = LoggerFactory.getLogger(SessionManager.class);

    private int expireCheckInterval = 60;//seconds
    private int sessionIdLength = 32;
    private ConcurrentHashMap<String, ISession> sessions = null;
    private SecureRandom secureRandom = null;
    private ServletContext servletContext = null;

    private Thread managerThread = null;
    private boolean running = false;

    public SessionManager() throws Exception {
        this.sessionIdLength = 18;
        this.sessions = new ConcurrentHashMap<String, ISession>();
        secureRandom = SecureRandom.getInstance("SHA1PRNG");
        secureRandom.setSeed(System.currentTimeMillis());
    }

    public void run() {
        while (running) {
            try {
                TimeUnit.SECONDS.sleep(expireCheckInterval);
            } catch (InterruptedException e) {
                logger.info("SessionManager: " + e);
            }
            checkExpire();
        }
    }

    public void start() {
        if (!running) {
            managerThread = new Thread(this);
            running = true;
            managerThread.start();
            ;
        }
    }

    public void stop() {
        if (running) {
            running = false;
        }
    }

    /**
     *
     * @param sessionid
     * @param create if true, when there is no exist session, then create one
     * @return HttpSession
     */
    public HttpSession getSession(String sessionid, boolean create) {
        ISession session = this.sessions.get(sessionid);
        if (session == null && !create) {
            return null;
        } else if (session == null) {
            session = this.createSession();
        } else {
            session.setNew(false);
        }
        SessionFacade sessionFacade = new SessionFacade(session);
        return sessionFacade;
    }

    public boolean isRequestedSessionIdValid(String sessionId) {
        return sessions.containsKey(sessionId);
    }

    public void loadSession() {

    }

    public void saveSession() {

    }

    private ISession createSession() {
        ISession session = new SessionBase();
        String sessionId = null;
        do {
            sessionId = generateSessionId();
        } while (sessions.containsKey(sessionId));
        session.setNew(true);
        session.setValid(true);
        session.setCreationTime(System.currentTimeMillis());
        session.setMaxInactiveInterval(1000);
        session.setId(sessionId);
        session.setServletContext(servletContext);
        sessions.put(sessionId, session);

        //        this.serviceConfig.listenerManager.fireEvent(EventUtils.SESSION_CREATED, this.serviceConfig.listenerManager.getEventUtils().buildHttpSessionEvent(session));

        return session;
    }

    private String generateSessionId() {
        byte random[] = new byte[16];

        // Render the result as a String of hexadecimal digits
        StringBuilder buffer = new StringBuilder();

        int resultLenBytes = 0;

        while (resultLenBytes < sessionIdLength) {
            getRandomBytes(random);
            for (int j = 0; j < random.length && resultLenBytes < sessionIdLength; j++) {
                byte b1 = (byte) ((random[j] & 0xf0) >> 4);
                byte b2 = (byte) (random[j] & 0x0f);
                if (b1 < 10)
                    buffer.append((char) ('0' + b1));
                else
                    buffer.append((char) ('A' + (b1 - 10)));
                if (b2 < 10)
                    buffer.append((char) ('0' + b2));
                else
                    buffer.append((char) ('A' + (b2 - 10)));
                resultLenBytes++;
            }
        }

        return buffer.toString();
    }

    private byte[] getRandomBytes(byte[] random) {
        secureRandom.nextBytes(random);
        return random;
    }

    /**
     * check and clear expired session
     */
    private void checkExpire() {
        long currentTime = System.currentTimeMillis();
        Enumeration<String> keys = sessions.keys();
        while (keys.hasMoreElements()) {
            String key = keys.nextElement();
            ISession session = sessions.get(key);
            if ((currentTime - session.getCreationTime()) >= session.getMaxInactiveInterval()) {
                //Session expired
                session.setValid(false);
                sessions.remove(key);

                //                this.serviceConfig.listenerManager.fireEvent(EventUtils.SESSION_DESTROYED, this.serviceConfig.listenerManager.getEventUtils().buildHttpSessionEvent(session));
            }
        }
    }
}
