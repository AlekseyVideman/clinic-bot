package nelbiasoftware.clinic_bot.core;

import com.networknt.schema.*;
import com.networknt.schema.Error;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static nelbiasoftware.clinic_bot.core.util.Jackson.*;

public class Validation {

    private static final SchemaRegistry schemaRegistry = SchemaRegistry.withDefaultDialect(SpecificationVersion.DRAFT_2020_12,
                                                                                           builder -> builder.schemaRegistryConfig(SchemaRegistryConfig.builder()
                                                                                                                                                       .formatAssertionsEnabled(
                                                                                                                                                               true)
                                                                                                                                                       .build()));

    private Validation() {
    }

    public static String validateAppointmentDataBuffer(Map<String, Object> appointmentDataBuffer) {
        var singleKey = "_";
        var schemaLocations = Map.of(singleKey, "classpath:schema/appointment-data-buffer.schema.json");
        Map<String, String> appointmentDataBufferJson = Map.of(singleKey, json.writeValueAsString(appointmentDataBuffer));
        return Validation.validate(appointmentDataBufferJson, schemaLocations);
    }

    /**
     * Validate Map against schemas entry by entry. Keys of validation subject and schemas must be equal! <br>
     *
     * @param subject         subject to validate, in JSON
     * @param schemaLocations schema locations <br>
     *                        <code>classpath:</code> prefix is used to load a schema from the jar.
     * @return errors
     */
    public static String validate(Map<String, String> subject, Map<String, String> schemaLocations) {
        return schemaLocations.entrySet().stream()
                              .map(schema -> {
                                  var schemaLocation = SchemaLocation.of(schema.getValue());
                                  var validator = schemaRegistry.getSchema(schemaLocation);
                                  List<Error> errors = validator.validate(subject.get(schema.getKey()), InputFormat.JSON);
                                  if (errors.isEmpty()) {
                                      return "";
                                  }
                                  return formatErrorMessages(errors);
                              })
                              .filter(s -> !s.isEmpty())
                              .collect(Collectors.joining(System.lineSeparator()));
    }

    private static String formatErrorMessages(List<Error> errorMessages) {
        return errorMessages.stream()
                            .map(Error::toString)
                            .collect(Collectors.joining(System.lineSeparator()));
    }

}
