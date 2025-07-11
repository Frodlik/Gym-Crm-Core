package com.gym.crm.dao.impl;

import com.gym.crm.dao.TrainerDAO;
import com.gym.crm.dao.hibernate.TransactionHandler;
import com.gym.crm.exception.DaoException;
import com.gym.crm.model.Trainer;
import jakarta.persistence.NoResultException;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class TrainerDAOImpl implements TrainerDAO {
    private static final Logger log = LoggerFactory.getLogger(TrainerDAOImpl.class);

    private final TransactionHandler transactionHandler;

    @Override
    public Trainer create(Trainer trainer) {
        return transactionHandler.performReturningWithinSession(entityManager -> {
            entityManager.persist(trainer);

            log.info("Created Trainer with ID: {}", trainer.getId());

            return trainer;
        });
    }

    @Override
    public Optional<Trainer> findById(Long id) {
        return transactionHandler.performReturningWithinSession(entityManager -> {
            Trainer trainer = entityManager.find(Trainer.class, id);

            log.debug("Trainer found with ID: {}", id);

            return Optional.ofNullable(trainer);
        });
    }

    @Override
    public Optional<Trainer> findByUsername(String username) {
        return transactionHandler.performReturningWithinSession(entityManager -> {
            try {
                Trainer trainer = entityManager.createQuery(
                                "SELECT t FROM Trainer t WHERE t.user.username = :username", Trainer.class)
                        .setParameter("username", username)
                        .getSingleResult();

                log.debug("Found trainer with username: {}", username);

                return Optional.of(trainer);
            } catch (NoResultException e) {
                log.debug("No trainer found with username: {}", username);

                return Optional.empty();
            }
        });
    }

    @Override
    public List<Trainer> findAll() {
        return transactionHandler.performReturningWithinSession(entityManager -> {
            List<Trainer> trainers = entityManager.createQuery("FROM Trainer", Trainer.class)
                    .getResultList();

            log.debug("Retrieved all trainers. Count: {}", trainers.size());

            return trainers;
        });
    }

    @Override
    public Trainer update(Trainer trainer) {
        return transactionHandler.performReturningWithinSession(entityManager -> {
            Trainer existingTrainer = entityManager.find(Trainer.class, trainer.getId());
            if (existingTrainer == null) {
                throw new DaoException("Trainer not found with ID: " + trainer.getId());
            }

            Trainer updatedTrainer = entityManager.merge(trainer);

            log.info("Trainer updated with ID: {}", trainer.getId());

            return updatedTrainer;
        });
    }
}
