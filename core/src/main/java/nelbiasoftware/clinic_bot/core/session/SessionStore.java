package nelbiasoftware.clinic_bot.core.session;

import nelbiasoftware.clinic_bot.core.State;
import nelbiasoftware.clinic_bot.core.UserManagement;
import org.jspecify.annotations.Nullable;

import java.util.Map;

/**
 * Facade over {@link State}/{@link UserManagement} so callers don't touch the mutable state directly.
 */
public final class SessionStore {

    private SessionStore() {
    }

    public static @Nullable Map<String, Object> getSession(long userId) {
        return UserManagement.getSession(State.getUserManagement(), userId + "");
    }

    public static Map<String, Object> startSession(long userId) {
        State.updateUserManagement(() -> UserManagement.startSession(State.getUserManagement(), userId + ""));

        return getSession(userId);
    }

    public static void endSession(long userId) {
        State.updateUserManagement(() -> UserManagement.endSession(State.getUserManagement(), userId + ""));
    }

    public static void updateSession(Map<String, Object> session, long userId) {
        State.updateUserManagement(() -> UserManagement.updateSession(State.getUserManagement(), session, userId + ""));
    }

    public static void registerPatient(long userId, String phoneNumber, String firstName) {
        State.updateUserManagement(() -> UserManagement.registerPatient(State.getUserManagement(), userId, phoneNumber, firstName));
    }
}
