package org.example.telegrambot.repository;

import org.example.telegrambot.entity.NotificationTask;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface NotificationTaskRepository {

    <S extends NotificationTask> S save(S entity);

    Optional<NotificationTask> findById(Long id);

    List<NotificationTask> findAll();

    List<NotificationTask> findByChatId(Long chatId);

    List<NotificationTask> findByStatus(String status);

    List<NotificationTask> findByScheduledAtBefore(LocalDateTime dateTime);

    List<NotificationTask> findByScheduledAtBeforeAndStatus(LocalDateTime dateTime, String status);

    List<NotificationTask> findByScheduledAt(LocalDateTime dateTime);

    long count();

    void deleteById(Long id);
}
