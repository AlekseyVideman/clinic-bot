package nelbiasoftware.clinic_bot.core.util;

import org.jspecify.annotations.Nullable;

import java.util.function.Function;

import static java.util.Objects.*;

/**
 * Either-like; ROP
 */
public record Result<T>(@Nullable T result, @Nullable String failureMessage) {

    public Result {
        if (!(isNull(result) || isNull(failureMessage))) {
            throw new IllegalArgumentException("Result and failure message should be mutually exclusive");
        } else if (isNull(result) && isNull(failureMessage)) {
            throw new IllegalArgumentException("Result and failure message should be mutually exclusive");
        }
    }

    public boolean isOk() {
        return failureMessage == null;
    }

    public Result<T> bind(Function<T, Result<T>> next) {
        if (isOk()) {
            return next.apply(result);
        } else {
            return this;
        }
    }

}
