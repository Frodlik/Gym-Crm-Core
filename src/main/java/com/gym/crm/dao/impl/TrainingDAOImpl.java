package com.gym.crm.dao.impl;

import com.gym.crm.dao.TrainingDAO;
import com.gym.crm.dao.criteria.TrainingCriteriaBuilder;
import com.gym.crm.dao.hibernate.TransactionHandler;
import com.gym.crm.model.Training;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class TrainingDAOImpl implements TrainingDAO {
    private static final Logger log = LoggerFactory.getLogger(TrainingDAOImpl.class);

    private static final String TRAINEE = "trainee";
    private static final String TRAINER = "trainer";

    private final TransactionHandler transactionHandler;
    private final TrainingCriteriaBuilder criteriaBuilder;

    @Override
    public Training create(Training training) {
        return transactionHandler.performReturningWithinSession(entityManager -> {
            entityManager.persist(training);

            log.info("Created Training with ID: {}", training.getId());

            return training;
        });
    }

    @Override
    public Optional<Training> findById(Long id) {
        return transactionHandler.performReturningWithinSession(entityManager -> {
            Training training = entityManager.find(Training.class, id);

            log.debug("Training found with ID: {}", id);

            return Optional.ofNullable(training);
        });
    }

    @Override
    public List<Training> findAll() {
        return transactionHandler.performReturningWithinSession(entityManager -> {
            List<Training> trainings = entityManager.createQuery("FROM Training", Training.class)
                    .getResultList();

            log.debug("Retrieved all trainings. Count: {}", trainings.size());

            return trainings;
        });
    }

    @Override
    public List<Training> findTraineeTrainingsByCriteria(String traineeUsername, LocalDate fromDate, LocalDate toDate,
                                                         String trainerName, String trainingType) {
        return transactionHandler.performReturningWithinSession(entityManager -> {
            log.debug("Finding trainee trainings for username: {}, from: {}, to: {}, trainer: {}, type: {}",
                    traineeUsername, fromDate, toDate, trainerName, trainingType);

            List<Training> results = criteriaBuilder.findTrainingsByCriteria(
                    entityManager, traineeUsername, TRAINEE, fromDate, toDate,
                    trainerName, false, trainingType
            );

            log.debug("Found {} trainings for trainee", results.size());

            return results;
        });
    }

    @Override
    public List<Training> findTrainerTrainingsByCriteria(String trainerUsername, LocalDate fromDate, LocalDate toDate,
                                                         String traineeName) {
        return transactionHandler.performReturningWithinSession(entityManager -> {
            log.debug("Finding trainer trainings for username: {}, from: {}, to: {}, trainee: {}",
                    trainerUsername, fromDate, toDate, traineeName);

            List<Training> results = criteriaBuilder.findTrainingsByCriteria(
                    entityManager, trainerUsername, TRAINER, fromDate, toDate,
                    traineeName, true, null
            );

            log.debug("Found {} trainings for trainer", results.size());

            return results;
        });
    }
}
