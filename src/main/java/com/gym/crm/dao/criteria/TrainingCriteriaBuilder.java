package com.gym.crm.dao.criteria;

import com.gym.crm.model.Training;
import io.micrometer.common.lang.Nullable;
import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class TrainingCriteriaBuilder {
    private static final String TRAINEE = "trainee";
    private static final String TRAINER = "trainer";
    private static final String USER = "user";
    private static final String TRAINING_TYPE = "trainingType";
    private static final String USERNAME = "username";
    private static final String FIRST_NAME = "firstName";
    private static final String LAST_NAME = "lastName";
    private static final String TRAINING_DATE = "trainingDate";
    private static final String TRAINING_TYPE_NAME = "trainingTypeName";

    public List<Training> findTrainingsByCriteria(
            EntityManager entityManager,
            String userUsername,
            String userRole,
            LocalDate fromDate,
            LocalDate toDate,
            String nameFilter,
            boolean isSearchingByTrainer,
            @Nullable String trainingType
    ) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Training> query = buildTrainingCriteriaQuery(
                cb, userUsername, userRole, fromDate, toDate,
                nameFilter, isSearchingByTrainer, trainingType
        );

        return entityManager.createQuery(query).getResultList();
    }

    private CriteriaQuery<Training> buildTrainingCriteriaQuery(
            CriteriaBuilder cb,
            String userUsername,
            String userRole,
            LocalDate fromDate,
            LocalDate toDate,
            String nameFilter,
            boolean isSearchingByTrainer,
            @Nullable String trainingType
    ) {
        CriteriaQuery<Training> query = cb.createQuery(Training.class);
        Root<Training> root = query.from(Training.class);

        TrainingJoins joins = createJoins(root, trainingType);
        List<Predicate> predicates = buildPredicates(cb, root, joins, userUsername, userRole,
                fromDate, toDate, nameFilter, isSearchingByTrainer, trainingType);

        query.where(predicates.toArray(new Predicate[0]));
        query.orderBy(cb.desc(root.get(TRAINING_DATE)));

        return query;
    }

    private TrainingJoins createJoins(Root<Training> root, @Nullable String trainingType) {
        Join<Object, Object> trainerJoin = root.join(TRAINER, JoinType.LEFT);
        Join<Object, Object> trainerUserJoin = trainerJoin.join(USER, JoinType.LEFT);
        Join<Object, Object> traineeJoin = root.join(TRAINEE, JoinType.LEFT);
        Join<Object, Object> traineeUserJoin = traineeJoin.join(USER, JoinType.LEFT);

        Join<Object, Object> trainingTypeJoin = null;
        if (trainingType != null) {
            trainingTypeJoin = root.join(TRAINING_TYPE, JoinType.LEFT);
        }

        return new TrainingJoins(trainerUserJoin, traineeUserJoin, trainingTypeJoin);
    }

    private List<Predicate> buildPredicates(
            CriteriaBuilder cb,
            Root<Training> root,
            TrainingJoins joins,
            String userUsername,
            String userRole,
            LocalDate fromDate,
            LocalDate toDate,
            String nameFilter,
            boolean isSearchingByTrainer,
            @Nullable String trainingType
    ) {
        List<Predicate> predicates = new ArrayList<>();

        addUsernamePredicate(cb, predicates, joins, userUsername, userRole);
        addDateRangePredicates(cb, predicates, root, fromDate, toDate);
        addNameFilterPredicate(cb, predicates, joins, nameFilter, isSearchingByTrainer);
        addTrainingTypePredicate(cb, predicates, joins, trainingType);

        return predicates;
    }

    private void addUsernamePredicate(
            CriteriaBuilder cb,
            List<Predicate> predicates,
            TrainingJoins joins,
            String userUsername,
            String userRole
    ) {
        if (userUsername == null || userUsername.trim().isEmpty()) {
            return;
        }

        if (TRAINEE.equalsIgnoreCase(userRole)) {
            predicates.add(cb.equal(joins.traineeUserJoin().get(USERNAME), userUsername));
        } else {
            predicates.add(cb.equal(joins.trainerUserJoin().get(USERNAME), userUsername));
        }
    }

    private void addDateRangePredicates(
            CriteriaBuilder cb,
            List<Predicate> predicates,
            Root<Training> root,
            LocalDate fromDate,
            LocalDate toDate
    ) {
        if (fromDate != null) {
            predicates.add(cb.greaterThanOrEqualTo(root.get(TRAINING_DATE), fromDate));
        }
        if (toDate != null) {
            predicates.add(cb.lessThanOrEqualTo(root.get(TRAINING_DATE), toDate));
        }
    }

    private void addNameFilterPredicate(
            CriteriaBuilder cb,
            List<Predicate> predicates,
            TrainingJoins joins,
            String nameFilter,
            boolean isSearchingByTrainer
    ) {
        if (nameFilter == null || nameFilter.trim().isEmpty()) {
            return;
        }

        String pattern = "%" + nameFilter.toLowerCase() + "%";
        Join<Object, Object> userJoin = isSearchingByTrainer ? joins.traineeUserJoin() : joins.trainerUserJoin();

        Predicate firstNamePredicate = cb.like(cb.lower(userJoin.get(FIRST_NAME)), pattern);
        Predicate lastNamePredicate = cb.like(cb.lower(userJoin.get(LAST_NAME)), pattern);
        Predicate fullNamePredicate = cb.like(
                cb.lower(cb.concat(cb.concat(userJoin.get(FIRST_NAME), " "), userJoin.get(LAST_NAME))),
                pattern
        );

        predicates.add(cb.or(firstNamePredicate, lastNamePredicate, fullNamePredicate));
    }

    private void addTrainingTypePredicate(
            CriteriaBuilder cb,
            List<Predicate> predicates,
            TrainingJoins joins,
            @Nullable String trainingType
    ) {
        if (trainingType == null || trainingType.trim().isEmpty() || joins.trainingTypeJoin() == null) {
            return;
        }

        predicates.add(cb.like(
                cb.lower(joins.trainingTypeJoin().get(TRAINING_TYPE_NAME)),
                "%" + trainingType.toLowerCase() + "%"
        ));
    }

    private record TrainingJoins(
            Join<Object, Object> trainerUserJoin,
            Join<Object, Object> traineeUserJoin,
            @Nullable Join<Object, Object> trainingTypeJoin
    ) {}
}
