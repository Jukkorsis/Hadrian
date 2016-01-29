package com.northernwall.hadrian.access.simple;

import com.northernwall.hadrian.Const;
import com.northernwall.hadrian.db.DataAccess;
import com.northernwall.hadrian.domain.User;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SimpleSessionStore {
    private final static Logger logger = LoggerFactory.getLogger(SimpleSessionStore.class);

    private final DataAccess dataAccess;
    private final Map<String, SimpleSession> sessions;

    public SimpleSessionStore(DataAccess dataAccess) {
        this.dataAccess = dataAccess;
        sessions = new HashMap<>();
    }

    public String createUserSession(String username) {
        SimpleSession session = new SimpleSession(username, System.currentTimeMillis() + Const.COOKIE_EXPRIY);
        sessions.put(session.getSessionId(), session);

        User user = getUser(username);

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
        User user = getUser(username);
        return user;
    }

    private User getUser(String username) {
        User user = dataAccess.getUser(username);
        if (user == null) {
            List<User> users = dataAccess.getUsers();
            if (users == null || users.isEmpty()) {
                logger.info("No users found. So creating {} as the first user", username);
                user = new User(username, username, true, true);
            } else {
                logger.info("User {} not found, creating", username);
                user = new User(username, username, false, false);
            }
            dataAccess.saveUser(user);
        }
        return user;
    }
}
