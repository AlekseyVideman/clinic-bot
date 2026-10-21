package nelbiasoftware.clinic_bot.core.util;

import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.SerializationFeature;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.dataformat.yaml.YAMLMapper;

public class Jackson {

    private Jackson() {
    }

    public static final ObjectMapper json = JsonMapper.builder()
                                                      .enable(SerializationFeature.INDENT_OUTPUT)
                                                      .build();

    public static final YAMLMapper yaml = YAMLMapper.builder()
                                                    .build();
}
