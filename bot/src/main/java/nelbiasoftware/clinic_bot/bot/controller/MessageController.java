package nelbiasoftware.clinic_bot.bot.controller;

import nelbiasoftware.clinic_bot.bot.routing.UpdateRouter;
import nelbiasoftware.clinic_bot.core.session.SessionStore;
import org.tinylog.Logger;
import ru.max.botapi.client.MaxBotAPI;
import ru.max.botapi.model.MessageCreatedUpdate;
import ru.max.botapi.model.NewMessageBody;

import java.util.Map;

public class MessageController {

    public static void handle(MessageCreatedUpdate update, MaxBotAPI api, long userId) {
        String input = update.message()
                             .body()
                             .text();
        long chatId = update.message()
                            .recipient()
                            .chatId();

        Logger.debug("User {} sent message: {}", userId, input);

        Map<String, Object> session = SessionStore.getSession(userId);
        NewMessageBody response = UpdateRouter.routeResponse(update.message(), input, session, userId);

        api.sendMessage(response)
           .chatId(chatId)
           .execute();
    }
}
