package org.example.telegrambot.scheduler;

import org.example.telegrambot.entity.NotificationTask;
import org.example.telegrambot.repository.NotificationTaskRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class NotificationScheduler {

    private static final Logger log = LoggerFactory.getLogger(NotificationScheduler.class);

    private final NotificationTaskRepository notificationTaskRepository;
    private final Timer timer;

    public NotificationScheduler(NotificationTaskRepository notificationTaskRepository) {
        this.notificationTaskRepository = notificationTaskRepository;
        this.timer = new Timer("NotificationScheduler", true);
        schedule();
    }

    private void schedule() {
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                checkAndSendNotifications();
            }
        }, 0, 60000); // Запуск сразу и каждые 60 секунд
    }

    public void checkAndSendNotifications() {
        java.time.LocalDateTime now = java.time.LocalDateTime.now();
        log.info("Checking notifications for: {}", now);

        List<NotificationTask> pendingTasks =
                notificationTaskRepository.findByScheduledAt(now);

        log.info("Found {} pending notifications", pendingTasks.size());

        for (NotificationTask task : pendingTasks) {
            if (!"PENDING".equals(task.getStatus())) {
                continue;
            }
            try {
                // TODO: send notification via Telegram Bot
                log.info("Sending notification: chatId={}, text={}", task.getChatId(), task.getMessageText());

                task.setStatus("SENT");
                notificationTaskRepository.save(task);
                log.info("Notification sent successfully for task id={}", task.getId());
            } catch (Exception e) {
                log.error("Failed to send notification for task id={}", task.getId(), e);
                task.setStatus("FAILED");
                notificationTaskRepository.save(task);
            }
        }
    }

    public void shutdown() {
        if (timer != null) {
            timer.cancel();
        }
    }
}
