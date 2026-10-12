package org.example.telegrambot.bot;

import org.example.telegrambot.entity.NotificationTask;
import org.example.telegrambot.entity.User;
import org.example.telegrambot.repository.NotificationTaskRepository;
import org.example.telegrambot.repository.UserRepository;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TelegramBotUpdatesListener {

    private static final Pattern REMIND_PATTERN =
            Pattern.compile("(\\d{2}\\.\\d{2}\\.\\d{4} \\d{2}:\\d{2}) (.+)");
    private static final DateTimeFormatter DATE_FORMATTER =
            DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");

    private final UserRepository userRepository;
    private final NotificationTaskRepository notificationTaskRepository;

    public TelegramBotUpdatesListener(UserRepository userRepository,
                                      NotificationTaskRepository notificationTaskRepository) {
        this.userRepository = userRepository;
        this.notificationTaskRepository = notificationTaskRepository;
    }

    public int process(List<Update> updates) {
        for (Update update : updates) {
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
        return updates.size();
    }

    public void handleStart(Long chatId, String username) {
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

    public void handleRemind(Long chatId, String text) {
        String reminderText = text.substring("/remind".length()).trim();

        Matcher matcher = REMIND_PATTERN.matcher(reminderText);
        if (!matcher.matches()) {
            sendMessage(chatId, "Формат: /remind ДД.ММ.ГГГГ ЧЧ:СС Текст напоминания\n"
                    + "Пример: /remind 01.01.2022 20:00 Сделать домашнюю работу");
            return;
        }

        String dateTimeStr = matcher.group(1);
        String messageText = matcher.group(2);

        LocalDateTime scheduledAt;
        try {
            scheduledAt = LocalDateTime.parse(dateTimeStr, DATE_FORMATTER);
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

    public void sendMessage(Long chatId, String text) {
        SendMessage message = new SendMessage();
        message.setChatId(chatId.toString());
        message.setText(text);
    }
}
