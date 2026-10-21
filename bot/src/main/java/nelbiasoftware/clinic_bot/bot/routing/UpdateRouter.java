package nelbiasoftware.clinic_bot.bot.routing;

import nelbiasoftware.clinic_bot.bot.constant.Callback;
import nelbiasoftware.clinic_bot.bot.controller.CallbackController;
import nelbiasoftware.clinic_bot.bot.controller.CommandController;
import nelbiasoftware.clinic_bot.bot.controller.MessageController;
import nelbiasoftware.clinic_bot.bot.controller.StartController;
import nelbiasoftware.clinic_bot.bot.scenario.AppointmentCreationFlow;
import nelbiasoftware.clinic_bot.bot.scenario.CancellationFlow;
import nelbiasoftware.clinic_bot.bot.scenario.RegistrationFlow;
import nelbiasoftware.clinic_bot.bot.view.AdminViews;
import nelbiasoftware.clinic_bot.bot.view.CommonViews;
import nelbiasoftware.clinic_bot.bot.view.PatientViews;
import org.jspecify.annotations.Nullable;
import ru.max.botapi.client.MaxBotAPI;
import ru.max.botapi.model.BotStartedUpdate;
import ru.max.botapi.model.Message;
import ru.max.botapi.model.MessageCallbackUpdate;
import ru.max.botapi.model.MessageCreatedUpdate;
import ru.max.botapi.model.NewMessageBody;
import ru.max.botapi.model.Update;

import java.util.Map;

public final class UpdateRouter {

    private UpdateRouter() {
    }

    public static void route(Update update, MaxBotAPI api) {
        switch (update) {
            case BotStartedUpdate su -> StartController.handle(su, api);
            case MessageCreatedUpdate cu -> MessageController.handle(cu, api, extractUserId(update));
            case MessageCallbackUpdate cb -> CallbackController.handle(cb, api, extractUserId(update));
            default -> {
                // ignore
            }
        }
    }

    public static long extractUserId(Update update) {
        return switch (update) {
            case BotStartedUpdate su -> su.user()
                                          .userId();
            case MessageCreatedUpdate cu -> cu.message()
                                              .sender()
                                              .userId();
            case MessageCallbackUpdate cb -> cb.callback()
                                               .user()
                                               .userId();
            default -> -1;
        };
    }

    public static NewMessageBody routeResponse(Message message, String input, @Nullable Map<String, Object> session, long userId) {
        if (MessageClassifier.isCommand(input)) {
            return CommandController.processCommand(input, userId, session);
        }
        if (session != null) {
            return AppointmentCreationFlow.appendSessionParameter(session, input, userId);
        }
        return processMessage(message);
    }

    private static NewMessageBody processMessage(Message message) {
        if (MessageClassifier.isRegisterMessage(message)) return RegistrationFlow.processRegister(message, message.sender());
        if (MessageClassifier.isAuthorMessage(message)) return CommonViews.authorInfo();

        return CommonViews.unknownInput();
    }

    public static NewMessageBody routeCallback(String payload, @Nullable Map<String, Object> session, long userId) {
        return switch (payload) {
            case Callback.ADMIN_HELP -> AdminViews.adminHelp();
            case Callback.PATIENT_HELP -> PatientViews.patientHelp();
            case Callback.CANCEL -> PatientViews.cancelAppointmentConfirmation();
            case Callback.CANCEL_APPROVED -> CancellationFlow.cancelApprovedAppointment(userId);

            case String s when s.equals(Callback.SKIP_GOAL)
                    && session != null -> AppointmentCreationFlow.skipGoal(session, userId);
            case String s when s.startsWith(Callback.DATE_PREFIX)
                    && session != null -> AppointmentCreationFlow.date(session, userId, payload);
            case String s when s.equals(Callback.SAVE_APPOINTMENT) && session != null -> AppointmentCreationFlow.appendSessionParameter(session, "", userId);
            case String s when s.equals(Callback.DO_NOT_SAVE_APPOINTMENT) && session != null -> AppointmentCreationFlow.doNotSaveAppointment(userId);
            case String s when s.equals(Callback.MESSAGE_TEMPLATE) && session != null -> AppointmentCreationFlow.messageTemplate(session, userId);

            default -> CommonViews.unknownInput();
        };
    }
}
