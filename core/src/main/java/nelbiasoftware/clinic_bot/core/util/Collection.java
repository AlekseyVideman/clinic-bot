package nelbiasoftware.clinic_bot.core.util;

import lombok.experimental.UtilityClass;
import org.jspecify.annotations.NullMarked;

import java.util.Arrays;
import java.util.Map;
import java.util.Objects;

import static nelbiasoftware.clinic_bot.core.util.Jackson.*;

@UtilityClass
@SuppressWarnings("all")
public class Collection {

    /**
     * Retrieves a value from nested maps by the given path. <br>
     * This method is for the case when there is 100% certainty that a value exists along the specified path.
     *
     * @throws IllegalArgumentException if a developer makes an mistake in the path or passes wrong map
     */
    @NullMarked
    public static <T> T get(Map map, String... path) {
        Object result = map;
        String prevKey = "";

        for (String key : path) {
            if (!(result instanceof Map)) {
                throw new IllegalArgumentException("Key '%s' is not pointing to a Map in map %s at path %s".formatted(prevKey,
                                                                                                                      json.writeValueAsString(map),
                                                                                                                      Arrays.toString(path)));
            }
            Map<String, Object> currentMap = (Map<String, Object>) result;
            result = currentMap.get(key);
            prevKey = key;
        }

        if (Objects.isNull(result)) {
            throw new IllegalArgumentException("Null value for key '%s' in map %s at path %s".formatted(prevKey,
                                                                                                        json.writeValueAsString(map),
                                                                                                        Arrays.toString(path)));
        }

        return (T) result;
    }

    /**
     * Checks if there is a value in nested maps by the given path. <br>
     * This method is for the case the when a map is allowed to have null values for polymorphism.
     */
    public static boolean contains(Map map, String... path) {
        Object result = map;
        for (String key : path) {
            if (!(result instanceof Map)) {
                return false;
            }
            Map<String, Object> currentMap = (Map<String, Object>) result;
            result = currentMap.get(key);
        }

        return result != null;
    }

    /**
     * For cases when null values are permitted
     */
    public static <T> T getOrNull(Map map, String... path) {
        if (contains(map, path)) {
            return Collection.get(map, path);
        }
        return null;
    }

}
