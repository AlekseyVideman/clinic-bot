package nelbiasoftware.clinic_bot.core

import spock.lang.Specification

import static nelbiasoftware.clinic_bot.core.util.Jackson.json

class AppointmentDataBufferSpec extends Specification {

    private static final String SCHEMA_KEY = "appointmentDataBuffer"
    private static final String SCHEMA_LOCATION = "classpath:schema/appointment-data-buffer.schema.json"

    /**
     * AppointmentDataBuffer defined {@link Entity.AppointmentDataBuffer}
     */
    def "valid appointment with #phone phone number"() {
        given:
        Map<String, Object> data = [
                patientPhoneNumber: phone,
                date              : "15.03",
                startEndTime      : "10:00 - 11:00",
                goal              : "Чистка зубов"
        ]

        when:
        String errors = Validation.validate(
                Map.of(SCHEMA_KEY, json.writeValueAsString(data)),
                Map.of(SCHEMA_KEY, SCHEMA_LOCATION)
        )

        then:
        errors.isEmpty()

        where:
        phone << ["+79161234567", "89161234567", "+7 (916) 123-45-6", "a", "1.5"]
    }
}
