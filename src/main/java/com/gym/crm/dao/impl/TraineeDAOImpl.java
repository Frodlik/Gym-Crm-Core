package com.gym.crm.dao.impl;

import com.gym.crm.dao.TraineeDAO;
import com.gym.crm.dao.hibernate.TransactionHandler;
import com.gym.crm.exception.DaoException;
import com.gym.crm.model.Trainee;
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
public class TraineeDAOImpl implements TraineeDAO {
    private static final Logger log = LoggerFactory.getLogger(TraineeDAOImpl.class);

    private final TransactionHandler transactionHandler;

    @Override
    public Trainee create(Trainee trainee) {
        return transactionHandler.performReturningWithinSession(entityManager -> {
            entityManager.persist(trainee);

            log.info("Created Trainee with ID: {}", trainee.getId());

            return trainee;
        });
    }

    @Override
    public Optional<Trainee> findById(Long id) {
        return transactionHandler.performReturningWithinSession(entityManager -> {
            Trainee trainee = entityManager.find(Trainee.class, id);

            log.debug("Found trainee with ID: {}", id);

            return Optional.ofNullable(trainee);
        });
    }

    @Override
    public Optional<Trainee> findByUsername(String username) {
        return transactionHandler.performReturningWithinSession(entityManager -> {
            try {
                Trainee trainee = entityManager.createQuery(
                                "SELECT t FROM Trainee t WHERE t.user.username = :username", Trainee.class)
                        .setParameter("username", username)
                        .getSingleResult();

                log.debug("Found trainee with username: {}", username);

                return Optional.of(trainee);
            } catch (NoResultException e) {
                log.debug("No trainee found with username: {}", username);

                return Optional.empty();
            }
        });
    }

    @Override
    public List<Trainee> findAll() {
        return transactionHandler.performReturningWithinSession(entityManager -> {
            List<Trainee> trainees = entityManager.createQuery("FROM Trainee", Trainee.class)
                    .getResultList();

            log.debug("Retrieved all trainees. Count: {}", trainees.size());

            return trainees;
        });
    }

    @Override
    public Trainee update(Trainee trainee) {
        return transactionHandler.performReturningWithinSession(entityManager -> {
            Trainee existingTrainee = entityManager.find(Trainee.class, trainee.getId());
            if (existingTrainee == null) {
                throw new DaoException("Trainee not found with ID: " + trainee.getId());
            }

            Trainee updatedTrainee = entityManager.merge(trainee);

            log.info("Trainee updated with ID: {}", trainee.getId());

            return updatedTrainee;
        });
    }

    @Override
    public Trainee updateTraineeTrainersList(String traineeUsername, List<String> trainerUsernames) {
        return transactionHandler.performReturningWithinSession(entityManager -> {
            Trainee trainee;
            try {
                trainee = entityManager.createQuery(
                                "SELECT t FROM Trainee t WHERE t.user.username = :username", Trainee.class)
                        .setParameter("username", traineeUsername)
                        .getSingleResult();
            } catch (NoResultException e) {
                throw new DaoException("Trainee not found with username: " + traineeUsername);
            }

            List<Trainer> newTrainers = trainerUsernames.stream()
                    .map(username -> {
                        try {
                            return entityManager.createQuery(
                                            "SELECT t FROM Trainer t WHERE t.user.username = :username", Trainer.class)
                                    .setParameter("username", username)
                                    .getSingleResult();
                        } catch (NoResultException e) {
                            throw new DaoException("Trainer not found with username: " + username);
                        }
                    })
                    .toList();

            trainee.getTrainers().clear();
            trainee.getTrainers().addAll(newTrainers);

            Trainee updatedTrainee = entityManager.merge(trainee);

            log.info("Updated trainers list for trainee with username: {}. New trainers count: {}",
                    traineeUsername, newTrainers.size());

            return updatedTrainee;
        });
    }

    @Override
    public void deleteByUsername(String username) {
        transactionHandler.performWithinSession(entityManager -> {
            try {
                Trainee trainee = entityManager.createQuery(
                                "SELECT t FROM Trainee t WHERE t.user.username = :username", Trainee.class)
                        .setParameter("username", username)
                        .getSingleResult();

                entityManager.remove(trainee);

                log.info("Trainee deleted with username: {}", username);
            } catch (NoResultException e) {
                log.warn("Trainee not found for deletion with username: {}", username);
            }
        });
    }
}
