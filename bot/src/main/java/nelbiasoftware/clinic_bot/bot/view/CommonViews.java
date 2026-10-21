package nelbiasoftware.clinic_bot.bot.view;

import nelbiasoftware.clinic_bot.bot.Messages;
import nelbiasoftware.clinic_bot.bot.constant.Callback;
import ru.max.botapi.model.*;

import java.util.List;

import static nelbiasoftware.clinic_bot.bot.constant.Command.*;

public final class CommonViews {

    private CommonViews() {
    }

    public static NewMessageBody unknownInput() {
        List<List<Button>> helpButtons = List.of(
                List.of(new CallbackButton(Messages.buttonPatientHelp(), Callback.PATIENT_HELP, null),
                        new CallbackButton(Messages.buttonAdminHelp(), Callback.ADMIN_HELP, null)
                ),
                List.of(new MessageButton(COMMAND_PREFIX + CREATE_APPOINTMENT)),
                List.of(new MessageButton(COMMAND_PREFIX + LIST_NEXT_APPOINTMENTS)),
                List.of(new MessageButton(COMMAND_PREFIX + CANCEL_APPOINTMENT_CREATION)),
                List.of(new MessageButton(COMMAND_PREFIX + CANCEL_APPOINTMENT))
        );

        return new NewMessageBody(Messages.errorUnknownInput(), buildInlineKeyboard(helpButtons), null, null, TextFormat.MARKDOWN);
    }

    public static NewMessageBody authorInfo() {
        return new NewMessageBody(Messages.authorInfo(), null, null, null, TextFormat.MARKDOWN);
    }

    public static String technicalError(String rayId) {
        return Messages.errorTechnical(rayId);
    }

    public static List<AttachmentRequest> buildInlineKeyboard(List<List<Button>> buttons) {
        InlineKeyboardAttachment.KeyboardPayload payload =
                new InlineKeyboardAttachment.KeyboardPayload(buttons);
        return List.of(new InlineKeyboardAttachmentRequest(payload));
    }
}
