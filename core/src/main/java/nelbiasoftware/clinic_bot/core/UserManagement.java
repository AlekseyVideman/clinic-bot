package nelbiasoftware.clinic_bot.core;

import nelbiasoftware.clinic_bot.core.util.Collection;
import org.jspecify.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

public class UserManagement {

    public static @Nullable Map<String, Object> patientByPhoneNumber(Long phoneNumber) {
        return Storage.patientByPhoneNumber(phoneNumber);
    }

    public static @Nullable Map<String, Object> patientByUserId(long userId) {
        return Storage.patientByUserId(userId);
    }

    public static Map<String, Object> startSession(Map<String, Object> userManagementData, String userId) {
        sessionsById(userManagementData).put(userId, createSession());

        return userManagementData;
    }

    private static Map<String, Object> createSession() {
        var session = new HashMap<String, Object>();
        session.put(Entity.Session.APPOINTMENT_DATA_BUFFER, new HashMap<String, Object>());
        session.put(Entity.Session.PRECREATED_APPOINTMENT, new HashMap<String, Object>());
        session.put(Entity.Session.STEP, Step.INITIAL);

        return session;
    }

    public static Map<String, Object> endSession(Map<String, Object> userManagementData, String userId) {
        var session = getSession(userManagementData, userId);
        if (session == null) {
            return userManagementData; // no modifications
        }

        Map<String, Object> sessionsById = Collection.get(userManagementData, Entity.UserManagement.SESSIONS_BY_ID);
        sessionsById.remove(userId);

        return userManagementData;
    }

    public static Map<String, Object> updateSession(Map<String, Object> userManagementData, Map<String, Object> session, String userId) {
        Map<String, Object> sessionsById = Collection.get(userManagementData, Entity.UserManagement.SESSIONS_BY_ID);
        sessionsById.put(userId, session);

        return userManagementData;
    }

    public static @Nullable Map<String, Object> getSession(Map<String, Object> userManagementData, String userId) {
        return Collection.getOrNull(userManagementData, Entity.UserManagement.SESSIONS_BY_ID, userId);
    }

    public static Map<String, Object> sessionsById(Map<String, Object> userManagementData) {
        return Collection.get(userManagementData, Entity.UserManagement.SESSIONS_BY_ID);
    }

    public static Map<String, Object> registerPatient(Map<String, Object> userManagementData, long userId, String phoneNumber, String firstName) {
        Map<String, Object> patient = Map.of(Entity.Patient.USER_ID, userId,
                                             Entity.Patient.FIRST_NAME, firstName,
                                             Entity.Patient.PHONE_NUMBER, Long.parseLong(phoneNumber));
        Storage.insertPatient(patient);

        return userManagementData;
    }

    public static boolean isPatientExists(Long phoneNumber) {
        return Storage.isPatientExists(phoneNumber);
    }
}
