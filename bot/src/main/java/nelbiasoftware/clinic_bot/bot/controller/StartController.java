package nelbiasoftware.clinic_bot.bot.controller;

import nelbiasoftware.clinic_bot.bot.view.PatientViews;
import ru.max.botapi.client.MaxBotAPI;
import ru.max.botapi.model.BotStartedUpdate;

public class StartController {

    public static void handle(BotStartedUpdate update, MaxBotAPI api) {
        api.sendMessage(PatientViews.greetingMessage())
           .chatId(update.chatId())
           .execute();
    }
}
