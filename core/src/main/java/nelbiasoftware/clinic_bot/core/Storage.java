package nelbiasoftware.clinic_bot.core;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import nelbiasoftware.clinic_bot.core.config.AppConfig;
import nelbiasoftware.clinic_bot.core.db.AppointmentMapper;
import nelbiasoftware.clinic_bot.core.db.OffsetDateTimeTypeHandler;
import nelbiasoftware.clinic_bot.core.db.PatientMapper;
import org.apache.ibatis.mapping.Environment;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.apache.ibatis.transaction.TransactionFactory;
import org.apache.ibatis.transaction.jdbc.JdbcTransactionFactory;
import org.flywaydb.core.Flyway;

import javax.sql.DataSource;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;

public class Storage {

    public static DataSource dataSource;

    private static SqlSessionFactory sqlSessionFactory = null;

    private Storage() {
    }

    public static Map<String, Object> patientByPhoneNumber(long phoneNumber) {
        try (var session = sqlSessionFactory.openSession(false)) {
            return session.getMapper(PatientMapper.class).selectByPhoneNumber(phoneNumber);
        }
    }

    public static Map<String, Object> patientByUserId(long userId) {
        try (var session = sqlSessionFactory.openSession(false)) {
            return session.getMapper(PatientMapper.class).selectByUserId(userId);
        }
    }

    public static boolean isPatientExists(Long phoneNumber) {
        try (var session = sqlSessionFactory.openSession(true)) {
            return session.getMapper(PatientMapper.class).existsByPhoneNumber(phoneNumber);
        }
    }

    public static void insertPatient(Map<String, Object> patient) {
        try (var session = sqlSessionFactory.openSession(true)) {
            session.getMapper(PatientMapper.class).insertPatient(patient);
        }
    }

    public static Map<String, Object> appointmentByPhoneNumber(Long phoneNumber) {
        try (var session = sqlSessionFactory.openSession(false)) {
            return session.getMapper(AppointmentMapper.class).selectByPhoneNumber(phoneNumber);
        }
    }

    public static boolean isAppointmentExists(Long phoneNumber) {
        try (var session = sqlSessionFactory.openSession(true)) {
            return session.getMapper(AppointmentMapper.class).existsByPhoneNumber(phoneNumber);
        }
    }

    public static List<Map<String, Object>> selectNextAppointments(int days) {
        try (var session = sqlSessionFactory.openSession(false)) {
            return session.getMapper(AppointmentMapper.class).selectNextAppointments(days);
        }
    }

    public static List<Map<String, Object>> selectAppointmentsByDate(OffsetDateTime date) {
        try (var session = sqlSessionFactory.openSession(false)) {
            return session.getMapper(AppointmentMapper.class).selectAppointmentsByDate(date);
        }
    }

    public static List<Map<String, Object>> selectOverlappedAppointments(Map<String, Object> appointment) {
        try (var session = sqlSessionFactory.openSession()) {
            return session.getMapper(AppointmentMapper.class).selectOverlappedAppointments(appointment);
        }
    }

    public static void insertAppointment(Map<String, Object> appointment) {
        try (var session = sqlSessionFactory.openSession(true)) {
            session.getMapper(AppointmentMapper.class).insertAppointment(appointment);
        }
    }

    public static void deleteAppointment(Long phoneNumber) {
        try (var session = sqlSessionFactory.openSession(true)) {
            session.getMapper(AppointmentMapper.class).deleteAppointment(phoneNumber);
        }
    }

    public static class Initialization {

        public static void init(AppConfig config) {
            initDataSource(config.jdbcUrl());
            initDbSchema(dataSource);
            Storage.sqlSessionFactory = sqlSessionFactory(dataSource);
        }

        private static DataSource initDataSource(String jdbcUrl) {
            HikariConfig config = new HikariConfig();
            config.setJdbcUrl(jdbcUrl);
            config.setMaximumPoolSize(2);

            return (dataSource = new HikariDataSource(config));
        }

        private static void initDbSchema(DataSource dataSource) {
            Flyway.configure(Storage.class.getClassLoader())
                  .locations("db/migration")
                  .dataSource(dataSource)
                  .cleanDisabled(false)
                  .load()
                  .migrate();
        }

        private static SqlSessionFactory sqlSessionFactory(DataSource dataSource) {
            TransactionFactory transactionFactory = new JdbcTransactionFactory();
            Environment environment = new Environment("1", transactionFactory, dataSource);


            Configuration configuration = new Configuration(environment);

            configuration.addMapper(AppointmentMapper.class);
            configuration.addMapper(PatientMapper.class);

            configuration.getTypeHandlerRegistry().register(OffsetDateTime.class, new OffsetDateTimeTypeHandler());
            configuration.setMapUnderscoreToCamelCase(true);

            return new SqlSessionFactoryBuilder().build(configuration);
        }
    }
}

