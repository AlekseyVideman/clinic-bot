package nelbiasoftware.clinic_bot.core;

public class Entity {

    private Entity() {
    }

    public static class State {

        public static final String USER_MANAGEMENT = "userManagement";
    }

    public static class UserManagement {

        public static final String SESSIONS_BY_ID = "sessionsById";
    }

    public static class Appointment {

        public static final String PATIENT_PHONE_NUMBER = "patientPhoneNumber";
        public static final String START_DATE_TIME = "startDateTime";
        public static final String END_DATE_TIME = "endDateTime";
        public static final String GOAL = "goal";
        public static final String MESSAGE = "message";
    }

    public static class AppointmentDataBuffer {

        public static final String PATIENT_PHONE_NUMBER = "patientPhoneNumber";
        public static final String DATE = "date";
        public static final String START_END_TIME = "startEndTime";
        public static final String START_TIME = "startTime";
        public static final String END_TIME = "endTime";
        public static final String GOAL = "goal";
        public static final String MESSAGE = "message";
    }
    public static class Patient {

        public static final String USER_ID = "userId";
        public static final String PHONE_NUMBER = "phoneNumber";

        public static final String FIRST_NAME = "firstName";

    }

    public static class Admin {

        public static final String ID = "id";

    }

    public static class Session {

        public static final String STEP = "step";
        public static final String APPOINTMENT_DATA_BUFFER = "appointmentDataBuffer";
        public static final String PRECREATED_APPOINTMENT = "precreatedAppointment";

    }

    public static class ConfigurationConst {

        public static final String MAX_TOKEN = "maxToken";
        public static final String JDBC_URL = "jdbcUrl";
        public static final String ADMINS_BY_ID = "adminsById";
    }

}
