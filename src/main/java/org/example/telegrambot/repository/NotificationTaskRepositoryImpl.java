package org.example.telegrambot.repository;

import org.example.telegrambot.entity.NotificationTask;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public class NotificationTaskRepositoryImpl implements NotificationTaskRepository {

    private final EntityManagerFactory entityManagerFactory;

    public NotificationTaskRepositoryImpl(EntityManagerFactory entityManagerFactory) {
        this.entityManagerFactory = entityManagerFactory;
    }

    @Override
    public <S extends NotificationTask> S save(S entity) {
        if (entity == null) {
            return null;
        }
        EntityManager em = entityManagerFactory.createEntityManager();
        try {
            em.getTransaction().begin();
            if (entity.getId() == null) {
                em.persist(entity);
            } else {
                em.merge(entity);
            }
            em.getTransaction().commit();
            return entity;
        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            throw e;
        } finally {
            em.close();
        }
    }

    @Override
    public Optional<NotificationTask> findById(Long id) {
        EntityManager em = entityManagerFactory.createEntityManager();
        try {
            NotificationTask task = em.find(NotificationTask.class, id);
            return Optional.ofNullable(task);
        } finally {
            em.close();
        }
    }

    @Override
    public List<NotificationTask> findAll() {
        EntityManager em = entityManagerFactory.createEntityManager();
        try {
            return em.createQuery("SELECT t FROM NotificationTask t", NotificationTask.class)
                    .getResultList();
        } finally {
            em.close();
        }
    }

    @Override
    public List<NotificationTask> findByChatId(Long chatId) {
        EntityManager em = entityManagerFactory.createEntityManager();
        try {
            return em.createQuery(
                            "SELECT t FROM NotificationTask t WHERE t.chatId = :chatId",
                            NotificationTask.class)
                    .setParameter("chatId", chatId)
                    .getResultList();
        } finally {
            em.close();
        }
    }

    @Override
    public List<NotificationTask> findByStatus(String status) {
        EntityManager em = entityManagerFactory.createEntityManager();
        try {
            return em.createQuery(
                            "SELECT t FROM NotificationTask t WHERE t.status = :status",
                            NotificationTask.class)
                    .setParameter("status", status)
                    .getResultList();
        } finally {
            em.close();
        }
    }

    @Override
    public List<NotificationTask> findByScheduledAtBefore(LocalDateTime dateTime) {
        EntityManager em = entityManagerFactory.createEntityManager();
        try {
            return em.createQuery(
                            "SELECT t FROM NotificationTask t WHERE t.scheduledAt < :dateTime",
                            NotificationTask.class)
                    .setParameter("dateTime", dateTime)
                    .getResultList();
        } finally {
            em.close();
        }
    }

    @Override
    public List<NotificationTask> findByScheduledAtBeforeAndStatus(LocalDateTime dateTime, String status) {
        EntityManager em = entityManagerFactory.createEntityManager();
        try {
            return em.createQuery(
                            "SELECT t FROM NotificationTask t WHERE t.scheduledAt < :dateTime AND t.status = :status",
                            NotificationTask.class)
                    .setParameter("dateTime", dateTime)
                    .setParameter("status", status)
                    .getResultList();
        } finally {
            em.close();
        }
    }

    @Override
    public List<NotificationTask> findByScheduledAt(LocalDateTime dateTime) {
        EntityManager em = entityManagerFactory.createEntityManager();
        try {
            return em.createQuery(
                            "SELECT t FROM NotificationTask t WHERE t.scheduledAt = :dateTime",
                            NotificationTask.class)
                    .setParameter("dateTime", dateTime)
                    .getResultList();
        } finally {
            em.close();
        }
    }

    @Override
    public long count() {
        EntityManager em = entityManagerFactory.createEntityManager();
        try {
            return em.createQuery("SELECT COUNT(t) FROM NotificationTask t", Long.class)
                    .getSingleResult();
        } finally {
            em.close();
        }
    }

    @Override
    public void deleteById(Long id) {
        EntityManager em = entityManagerFactory.createEntityManager();
        try {
            em.getTransaction().begin();
            NotificationTask task = em.find(NotificationTask.class, id);
            if (task != null) {
                em.remove(task);
            }
            em.getTransaction().commit();
        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            throw e;
        } finally {
            em.close();
        }
    }
}
