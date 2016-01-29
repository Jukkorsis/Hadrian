package com.northernwall.hadrian.access.simple;

import com.northernwall.hadrian.Const;
import com.northernwall.hadrian.access.AccessHelper;
import com.northernwall.hadrian.domain.User;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SimpleSessionStore {
    private final static Logger logger = LoggerFactory.getLogger(SimpleSessionStore.class);

    private final AccessHelper accessHelper;
    private final Map<String, SimpleSession> sessions;

    public SimpleSessionStore(AccessHelper accessHelper) {
        this.accessHelper = accessHelper;
        sessions = new HashMap<>();
    }

    public String createUserSession(String username) {
        SimpleSession session = new SimpleSession(username, System.currentTimeMillis() + Const.COOKIE_EXPRIY);
        sessions.put(session.getSessionId(), session);

        User user = accessHelper.getUser(username);

        logger.info("login passed for {}, session {} started", user.getUsername(), session.getSessionId());
        return session.getSessionId();
    }

    public User getUserForSession(String sessionId) {
        SimpleSession session = sessions.get(sessionId);
        if (session == null) {
            logger.warn("Could not find user sesion with ID {}", sessionId);
            return null;
        }
        if (session.getEndDateTime() < System.currentTimeMillis()) {
            logger.warn("Trying to use session {} which has expried", sessionId);
            sessions.remove(sessionId);
            return null;
        }
        String username = session.getUsername();
        User user = accessHelper.getUser(username);
        return user;
    }

}
