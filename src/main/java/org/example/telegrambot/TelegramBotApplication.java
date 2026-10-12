package org.example.telegrambot;

import org.example.telegrambot.bot.TelegramBot;
import org.example.telegrambot.config.JpaConfig;
import org.example.telegrambot.repository.NotificationTaskRepository;
import org.example.telegrambot.repository.NotificationTaskRepositoryImpl;
import org.example.telegrambot.repository.UserRepository;
import org.example.telegrambot.scheduler.NotificationScheduler;
import org.example.telegrambot.util.PropertiesLoader;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

import jakarta.persistence.EntityManagerFactory;

public class TelegramBotApplication {

    private static EntityManagerFactory entityManagerFactory;
    private static NotificationScheduler scheduler;

    public static void main(String[] args) {
        // Initialize database (Hibernate)
        entityManagerFactory = JpaConfig.getEntityManagerFactory();

        try {
            // Load configuration
            String botUsername = PropertiesLoader.getProperty("telegram.bot.username");
            String botToken = PropertiesLoader.getProperty("telegram.bot.token");

            // Create repositories
            UserRepository userRepository = new UserRepository(entityManagerFactory);
            NotificationTaskRepository notificationTaskRepository = new NotificationTaskRepositoryImpl(entityManagerFactory);

            // Create bot
            TelegramBot bot = new TelegramBot(botToken, userRepository, notificationTaskRepository);

            // Create scheduler
            scheduler = new NotificationScheduler(notificationTaskRepository);

            // Start the bot session
            TelegramBotsApi botsApi = new TelegramBotsApi(DefaultBotSession.class);
            botsApi.registerBot(bot);

            System.out.println("Bot '" + botUsername + "' is running with scheduler enabled...");
            System.out.println("Scheduler checks for notifications every minute.");

            // Add shutdown hook
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                System.out.println("Shutting down...");
                if (scheduler != null) {
                    scheduler.shutdown();
                }
                JpaConfig.close();
            }));

        } catch (Exception e) {
            System.err.println("Failed to start bot: " + e.getMessage());
            e.printStackTrace();
            JpaConfig.close();
            System.exit(1);
        }
    }
}
