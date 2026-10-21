package nelbiasoftware.clinic_bot.bot.scenario;

import ezvcard.Ezvcard;
import nelbiasoftware.clinic_bot.bot.Messages;
import nelbiasoftware.clinic_bot.bot.notification.Notifier;
import nelbiasoftware.clinic_bot.core.UserManagement;
import nelbiasoftware.clinic_bot.core.session.SessionStore;
import ru.max.botapi.model.ContactAttachment;
import ru.max.botapi.model.Message;
import ru.max.botapi.model.NewMessageBody;
import ru.max.botapi.model.TextFormat;
import ru.max.botapi.model.User;

public final class RegistrationFlow {

    private RegistrationFlow() {
    }

    public static NewMessageBody processRegister(Message message, User user) {
        String phoneNumber = extractPhoneNumber(message);
        long userId = message.sender()
                             .userId();

        if (UserManagement.isPatientExists(Long.parseLong(phoneNumber))) {
            return new NewMessageBody(Messages.errorPatientAlreadyRegistered(), null, null, null, TextFormat.MARKDOWN);
        }

        registerPatient(userId, phoneNumber, user.firstName());

        return new NewMessageBody(Messages.patientRegistrationCompleted(), null, null, null, TextFormat.MARKDOWN);
    }

    private static String extractPhoneNumber(Message message) {
        var contactAttachment = ((ContactAttachment) message.body()
                                                            .attachments()
                                                            .getFirst());
        return Ezvcard.parse(contactAttachment.payload()
                                                .vcfInfo())
                      .first()
                      .getTelephoneNumbers()
                      .getFirst()
                      .getText();
    }

    private static void registerPatient(long userId, String phoneNumber, String firstName) {
        SessionStore.registerPatient(userId, phoneNumber, firstName);
        Notifier.notifyAdminAboutRegisteredPatient(Long.parseLong(phoneNumber));
    }
}
