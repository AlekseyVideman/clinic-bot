package nelbiasoftware.clinic_bot.core;

import org.jspecify.annotations.Nullable;

/**
 * Action history in the Appointment creation process.
 * This is minimalistic implementation of FSM for single user.
 */
public enum Step {
    INITIAL,
    PHONE_NUMBER, DATE, TIME, GOAL, MESSAGE_CHOOSE,
    FILLED, WAITING_APPROVEMENT;

    public static @Nullable Step next(Step current) {
        int next = current.ordinal() + 1;
        if (next >= values().length) {
            return null;
        }
        return values()[next];
    }
}
