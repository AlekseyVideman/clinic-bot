package nelbiasoftware.clinic_bot.core;

import lombok.SneakyThrows;
import nelbiasoftware.clinic_bot.core.config.AppConfig;
import nelbiasoftware.clinic_bot.core.util.Jackson;
import org.tinylog.Logger;
import tools.jackson.core.type.TypeReference;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Collectors;

public class Configuration {

    private static final Path WORKING_DIR = Path.of(System.getProperty("user.dir"));

    private static final String APP_PROPERTIES = "app.properties";
    private static final String VERSION_KEY = "app-version";

    @SneakyThrows
    public static AppConfig loadConfig() {
        Path configPath = extractIfAbsent(APP_PROPERTIES);
        Properties prop = new Properties();
        try (InputStream config = Files.newInputStream(configPath)) {
            prop.load(config);
        }

        checkVersion(prop);

        Set<Long> adminsById = Arrays.stream(prop.getProperty("admins-by-id")
                                           .split(","))
                               .map(String::trim)
                               .map(Long::parseLong)
                               .collect(Collectors.toUnmodifiableSet());
        int workHoursStart = Integer.parseInt(prop.getProperty("work-hours-start"));
        int workHoursEnd = Integer.parseInt(prop.getProperty("work-hours-end"));
        var token = prop.getProperty("max-token");
        var jdbcUrl = prop.getProperty("jdbc-url");
        var certificatePath = prop.getProperty("certificate-path");
        var certificatePassword = prop.getProperty("certificate-password");
        var webhookUrl = prop.getProperty("webhook-url");

        return new AppConfig(workHoursStart, workHoursEnd, adminsById, token, jdbcUrl, certificatePath, certificatePassword, webhookUrl);
    }

    @SneakyThrows
    public static Map<String, String> loadMessages() {
        try (InputStream messages = Files.newInputStream(extractIfAbsent("messages.yaml"))) {
            return Jackson.yaml.readValue(messages, new TypeReference<>() {
            });
        }
    }

    @SneakyThrows
    private static Path extractIfAbsent(String resourceName) {
        Path target = WORKING_DIR.resolve(resourceName);
        if (Files.exists(target)) {
            return target;
        }
        try (InputStream resource = openResource(resourceName)) {
            Files.createDirectories(target.getParent());
            Files.copy(resource, target);
        }
        return target;
    }

    @SneakyThrows
    private static void checkVersion(Properties diskProp) {
        Properties bundledProp = new Properties();
        try (InputStream resource = openResource(APP_PROPERTIES)) {
            bundledProp.load(resource);
        }
        String diskVersion = diskProp.getProperty(VERSION_KEY);
        String bundledVersion = bundledProp.getProperty(VERSION_KEY);
        if (!Objects.equals(diskVersion, bundledVersion)) {
            Logger.warn("{} version mismatch: disk={}, bundled={}. Compare files and update if needed", APP_PROPERTIES, diskVersion, bundledVersion);
        }
    }

    private static InputStream openResource(String resourceName) {
        InputStream resource = Configuration.class.getClassLoader()
                                                  .getResourceAsStream(resourceName);
        if (resource == null) {
            throw new IllegalStateException("Resource not found on classpath: " + resourceName);
        }
        return resource;
    }

}
