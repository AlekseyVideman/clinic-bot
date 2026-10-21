package nelbiasoftware.clinic_bot.bot.routing;

import ru.max.botapi.model.ContactAttachment;
import ru.max.botapi.model.Message;

import static nelbiasoftware.clinic_bot.bot.constant.Command.COMMAND_PREFIX;

public final class MessageClassifier {

    private MessageClassifier() {
    }

    public static boolean isCommand(String message) {
        return message.startsWith(COMMAND_PREFIX);
    }

    public static boolean isRegisterMessage(Message message) {
        var attachments = message
                .body()
                .attachments();
        if (attachments == null) {
            return false;
        }

        return attachments.getFirst() instanceof ContactAttachment;
    }

    public static boolean isAuthorMessage(Message message) {
        var text = message
                .body()
                .text();
        return "b1g".equals(text);
    }
}
