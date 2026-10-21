package nelbiasoftware.clinic_bot.core.config;

import java.util.Set;

/**
 * System config. It's typed as of is not used in business logic, so flexibility not needed, furthermore testing.
 */
public record AppConfig(
        int workHoursStart,
        int workHoursEnd,
        Set<Long> adminsById,
        String maxToken,
        String jdbcUrl,
        String certificatePath,
        String certificatePassword,
        String webhookUrl
) {
}
