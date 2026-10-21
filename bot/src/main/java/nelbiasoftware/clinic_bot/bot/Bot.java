package nelbiasoftware.clinic_bot.bot;

import nelbiasoftware.clinic_bot.bot.controller.CommandController;
import nelbiasoftware.clinic_bot.bot.notification.Notifier;
import nelbiasoftware.clinic_bot.bot.routing.UpdateRouter;
import nelbiasoftware.clinic_bot.bot.task.Scheduler;
import nelbiasoftware.clinic_bot.bot.view.CommonViews;
import nelbiasoftware.clinic_bot.core.Configuration;
import nelbiasoftware.clinic_bot.core.State;
import nelbiasoftware.clinic_bot.core.Storage;
import nelbiasoftware.clinic_bot.core.appointment.AvailabilityService;
import nelbiasoftware.clinic_bot.core.config.AppConfig;
import org.tinylog.Logger;
import ru.max.botapi.client.MaxBotAPI;
import ru.max.botapi.model.*;
import ru.max.botapi.webhook.MaxWebhookServer;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static nelbiasoftware.clinic_bot.bot.constant.Command.*;

public class Bot {

    static MaxBotAPI api;

    static void main() throws IOException {
        AppConfig config = Configuration.loadConfig();
        Storage.Initialization.init(config);
        Scheduler.init(Storage.dataSource);
        AvailabilityService.Initialization.init(config);
        CommandController.Initialization.init(config);
        Notifier.Initialization.init(config);
        State.Initialization.init();

        var api = MaxBotAPI.create(config.maxToken());
        Bot.api = api;
        subscribe(api, config);
        registerCommands(api);
    }

    private static void registerCommands(MaxBotAPI api) {
        final var commands = List.of(
                new BotCommand(CREATE_APPOINTMENT, Messages.commandCreateAppointmentDesc()),
                new BotCommand(CANCEL_APPOINTMENT_CREATION, Messages.commandCancelAppointmentCreationDesc()),
                new BotCommand(LIST_NEXT_APPOINTMENTS, Messages.commandListNextAppointmentsDesc()),
                new BotCommand(CANCEL_APPOINTMENT, Messages.commandCancelAppointmentDesc())
        );
        api.editMyCommands(new BotCommandsPatch(commands))
           .execute();
    }

    private static void subscribe(MaxBotAPI api, AppConfig config) throws IOException {
        ExecutorService dispatch = Executors.newVirtualThreadPerTaskExecutor();
        var server = MaxWebhookServer.builder()
                                     .path("/")
                                     .port(443)
                                     .dispatchExecutor(dispatch)
                                     .serializer(api.serializer())
                                     .handler(update -> {
                                         try {
                                             UpdateRouter.route(update, api);
                                         } catch (Exception e) {
                                             String rayId = UUID.randomUUID()
                                                                .toString();
                                             Logger.error(e,
                                                          "[rayId={}]: {}",
                                                          rayId,
                                                          Objects.requireNonNullElse(e.getMessage(), "no message"));

                                             long userId = UpdateRouter.extractUserId(update);
                                             if (userId > 0) {
                                                 sendErrorMessage(userId, rayId);
                                             }
                                         }
                                     })
                                     .build();
        server.start(loadSslContext(config));

        var webhookUrl = config.webhookUrl();
        server.register(api, webhookUrl, Set.of(UpdateType.BOT_STARTED, UpdateType.MESSAGE_CREATED, UpdateType.MESSAGE_CALLBACK));

        Runtime.getRuntime()
               .addShutdownHook(new Thread(() -> {
                   try {
                       server.unregister(api, webhookUrl);
                   } catch (RuntimeException e) {
                       Logger.error(e, "Failed to unregister the webhook: " + webhookUrl);
                   }
                   server.close();
                   dispatch.close();
                   api.close();
               }));
    }

    private static SSLContext loadSslContext(AppConfig config) {
        char[] password = config.certificatePassword()
                                .toCharArray();
        try {
            KeyStore keyStore = KeyStore.getInstance("PKCS12");
            try (InputStream certificate = Files.newInputStream(Path.of(config.certificatePath()))) {
                keyStore.load(certificate, password);
            }
            KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
            keyManagerFactory.init(keyStore, password);
            SSLContext context = SSLContext.getInstance("TLS");
            context.init(keyManagerFactory.getKeyManagers(), null, null);
            return context;
        } catch (GeneralSecurityException | IOException e) {
            Logger.error(e, "Failed to load SSL context from certificate {}", config.certificatePath());
            throw new IllegalStateException(e);
        }
    }

    private static void sendErrorMessage(long userId, String rayId) {
        api.sendMessage(new NewMessageBody(CommonViews.technicalError(rayId), null, null, null, TextFormat.MARKDOWN))
           .userId(userId)
           .execute();
    }

    public static void sendMessage(long userId, NewMessageBody body) {
        api.sendMessage(body)
           .userId(userId)
           .execute();
    }

}
