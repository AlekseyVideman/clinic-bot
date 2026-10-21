package nelbiasoftware.clinic_bot.bot.controller;

import nelbiasoftware.clinic_bot.bot.routing.UpdateRouter;
import nelbiasoftware.clinic_bot.core.session.SessionStore;
import org.tinylog.Logger;
import ru.max.botapi.client.MaxBotAPI;
import ru.max.botapi.model.CallbackAnswer;
import ru.max.botapi.model.MessageCallbackUpdate;
import ru.max.botapi.model.NewMessageBody;

import java.util.Map;

public class CallbackController {

    public static void handle(MessageCallbackUpdate update, MaxBotAPI api, long userId) {
        String callbackId = update.callback()
                                  .callbackId();
        String payload = update.callback()
                               .payload();

        Logger.debug("Called callback [id: {}, payload: {}]", callbackId,  payload);

        Map<String, Object> session = SessionStore.getSession(userId);
        NewMessageBody response = UpdateRouter.routeCallback(payload, session, userId);

        api.answerOnCallback(new CallbackAnswer(response, null), callbackId)
           .execute();
    }

}
