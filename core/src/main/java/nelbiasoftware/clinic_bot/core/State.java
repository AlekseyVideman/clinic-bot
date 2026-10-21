package nelbiasoftware.clinic_bot.core;

import nelbiasoftware.clinic_bot.core.util.Collection;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

/**
 * Mutable
 */
public class State {

    private State() {}

    public static Map<String, Object> current;

    public static void updateUserManagement(Supplier<Map<String, Object>> nextUserInteraction) {
        current.put(Entity.State.USER_MANAGEMENT, nextUserInteraction.get());
    }

    public static Map<String, Object> getUserManagement() {
        return Collection.get(current, Entity.State.USER_MANAGEMENT);
    }

    public static class Initialization {

        public static void init() {
            current = buildState();
        }

        private static Map<String, Object> buildState() {
            Map<String, Object> state = new HashMap<>();
            state.put(Entity.State.USER_MANAGEMENT, buildUserManagement());

            return state;
        }

        private static Map<String, Object> buildUserManagement() {
            Map<String, Object> userManagementData = new HashMap<>();
            userManagementData.put(Entity.UserManagement.SESSIONS_BY_ID, new HashMap<>());

            return userManagementData;
        }
    }

}
