package ch.uzh.ifi.hase.soprafs24.rest.webFilter;

import ch.uzh.ifi.hase.soprafs24.entity.User;

public class UserContextHolder {
    private static final ThreadLocal<User> userHolder = new ThreadLocal<>();

    public static void setCurrentUser(User user) {
        userHolder.set(user);
    }

    public static User getCurrentUser() {
        return userHolder.get();
    }

    public static void clear() {
        userHolder.remove();
    }
}
