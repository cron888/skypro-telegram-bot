package org.example.telegrambot.bot;

import org.example.telegrambot.entity.NotificationTask;
import org.example.telegrambot.entity.User;
import org.example.telegrambot.repository.NotificationTaskRepository;
import org.example.telegrambot.repository.UserRepository;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

public class TelegramBot extends TelegramLongPollingBot {

    private final UserRepository userRepository;
    private final NotificationTaskRepository notificationTaskRepository;
    private final String botUsername;
    private final String botToken;

    public TelegramBot(String botToken,
                       UserRepository userRepository,
                       NotificationTaskRepository notificationTaskRepository) {
        super(botToken);
        this.botToken = botToken;
        this.botUsername = "skypro_hello_bot";
        this.userRepository = userRepository;
        this.notificationTaskRepository = notificationTaskRepository;
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            String text = update.getMessage().getText();
            Long chatId = update.getMessage().getChat().getId();
            String username = update.getMessage().getFrom().getUserName();

            if ("/start".equals(text)) {
                handleStart(chatId, username);
            } else if (text.startsWith("/remind")) {
                handleRemind(chatId, text);
            } else {
                sendMessage(chatId, "Unknown command. Use /start to begin.");
            }
        }
    }

    private void handleStart(Long chatId, String username) {
        userRepository.findByChatId(chatId).ifPresentOrElse(
                user -> {
                    if (user.getUsername() == null) {
                        user.setUsername(username);
                        userRepository.save(user);
                    }
                },
                () -> userRepository.save(new User(chatId, username))
        );

        String greeting = "Hello, " + (username != null ? username : "user")
                + "! Welcome to the Skypro bot. I am alive and working!";
        sendMessage(chatId, greeting);
    }

    private void handleRemind(Long chatId, String text) {
        String reminderText = text.substring("/remind".length()).trim();
        java.util.regex.Matcher matcher = java.util.regex.Pattern.compile("(\\d{2}\\.\\d{2}\\.\\d{4} \\d{2}:\\d{2}) (.+)").matcher(reminderText);

        if (!matcher.matches()) {
            sendMessage(chatId, "Формат: /remind ДД.ММ.ГГГГ ЧЧ:СС Текст напоминания\n"
                    + "Пример: /remind 01.01.2022 20:00 Сделать домашнюю работу");
            return;
        }

        String dateTimeStr = matcher.group(1);
        String messageText = matcher.group(2);

        java.time.LocalDateTime scheduledAt;
        try {
            scheduledAt = java.time.LocalDateTime.parse(dateTimeStr,
                    java.time.format.DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm"));
        } catch (Exception e) {
            sendMessage(chatId, "Неверный формат даты. Используйте ДД.ММ.ГГГГ ЧЧ:СС");
            return;
        }

        NotificationTask task = new NotificationTask();
        task.setChatId(chatId);
        task.setMessageText(messageText);
        task.setScheduledAt(scheduledAt);
        task.setStatus("PENDING");
        notificationTaskRepository.save(task);

        sendMessage(chatId, "Напоминание сохранено:\n"
                + "Дата: " + dateTimeStr + "\n"
                + "Текст: " + messageText);
    }

    private void sendMessage(Long chatId, String text) {
        SendMessage message = new SendMessage(chatId.toString(), text);
        try {
            execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    @Override
    public String getBotUsername() {
        return botUsername;
    }

    @Override
    public String getBotToken() {
        return botToken;
    }
}
