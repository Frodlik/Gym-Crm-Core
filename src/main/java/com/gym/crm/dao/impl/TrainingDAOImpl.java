package com.gym.crm.dao.impl;

import com.gym.crm.dao.TrainingDAO;
import com.gym.crm.model.Training;
import com.gym.crm.dao.hibernate.TransactionHandler;
import io.micrometer.common.lang.Nullable;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class TrainingDAOImpl implements TrainingDAO {
    private static final Logger log = LoggerFactory.getLogger(TrainingDAOImpl.class);

    private static final String TRAINEE = "trainee";
    private static final String TRAINER = "trainer";

    private final TransactionHandler transactionHandler;

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
        return findTrainingsByCriteria(traineeUsername, TRAINEE, fromDate, toDate, trainerName, false, trainingType);
    }

    @Override
    public List<Training> findTrainerTrainingsByCriteria(String trainerUsername, LocalDate fromDate, LocalDate toDate,
                                                         String traineeName) {
        return findTrainingsByCriteria(trainerUsername, TRAINER, fromDate, toDate, traineeName, true, null);
    }


    private List<Training> findTrainingsByCriteria(
            String userUsername, String userRole,
            LocalDate fromDate,
            LocalDate toDate,
            String nameFilter,
            boolean isSearchingByTrainer,
            @Nullable String trainingType
    ) {
        return transactionHandler.performReturningWithinSession(entityManager -> {
            CriteriaBuilder cb = entityManager.getCriteriaBuilder();
            CriteriaQuery<Training> query = cb.createQuery(Training.class);
            Root<Training> root = query.from(Training.class);

            Join<Object, Object> trainerJoin = root.join(TRAINER, JoinType.LEFT);
            Join<Object, Object> trainerUserJoin = trainerJoin.join("user", JoinType.LEFT);
            Join<Object, Object> traineeJoin = root.join(TRAINEE, JoinType.LEFT);
            Join<Object, Object> traineeUserJoin = traineeJoin.join("user", JoinType.LEFT);

            Join<Object, Object> trainingTypeJoin = null;
            if (trainingType != null) {
                trainingTypeJoin = root.join("trainingType", JoinType.LEFT);
            }

            List<Predicate> predicates = new ArrayList<>();

            if (userUsername != null && !userUsername.trim().isEmpty()) {
                if (TRAINEE.equalsIgnoreCase(userRole)) {
                    predicates.add(cb.equal(traineeUserJoin.get("username"), userUsername));
                } else {
                    predicates.add(cb.equal(trainerUserJoin.get("username"), userUsername));
                }
            }

            if (fromDate != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("trainingDate"), fromDate));
            }
            if (toDate != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("trainingDate"), toDate));
            }

            if (nameFilter != null && !nameFilter.trim().isEmpty()) {
                String pattern = "%" + nameFilter.toLowerCase() + "%";

                Join<Object, Object> userJoin = isSearchingByTrainer ? traineeUserJoin : trainerUserJoin;
                Predicate firstNamePredicate = cb.like(cb.lower(userJoin.get("firstName")), pattern);
                Predicate lastNamePredicate = cb.like(cb.lower(userJoin.get("lastName")), pattern);
                Predicate fullNamePredicate = cb.like(
                        cb.lower(cb.concat(cb.concat(userJoin.get("firstName"), " "), userJoin.get("lastName"))), pattern);

                predicates.add(cb.or(firstNamePredicate, lastNamePredicate, fullNamePredicate));
            }

            if (trainingType != null && !trainingType.trim().isEmpty() && trainingTypeJoin != null) {
                predicates.add(cb.like(cb.lower(trainingTypeJoin.get("trainingTypeName")),
                        "%" + trainingType.toLowerCase() + "%"));
            }

            query.where(predicates.toArray(new Predicate[0]));
            query.orderBy(cb.desc(root.get("trainingDate")));

            return entityManager.createQuery(query).getResultList();
        });
    }
}
