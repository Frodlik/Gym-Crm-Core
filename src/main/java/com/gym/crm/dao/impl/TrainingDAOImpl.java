package com.gym.crm.dao.impl;

import com.gym.crm.dao.TrainingDAO;
import com.gym.crm.model.Training;
import com.gym.crm.util.HibernateUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class TrainingDAOImpl implements TrainingDAO {
    private static final Logger log = LoggerFactory.getLogger(TrainingDAOImpl.class);

    @Override
    public Training create(Training training) {
        return HibernateUtil.performReturningWithinSession(entityManager -> {
            entityManager.persist(training);

            log.info("Created Training with ID: {}", training.getId());

            return training;
        });
    }

    @Override
    public Optional<Training> findById(Long id) {
        return HibernateUtil.performReturningWithinSession(entityManager -> {
            Training training = entityManager.find(Training.class, id);

            log.debug("Training found with ID: {}", id);

            return Optional.ofNullable(training);
        });
    }

    @Override
    public List<Training> findAll() {
        return HibernateUtil.performReturningWithinSession(entityManager -> {
            List<Training> trainings = entityManager.createQuery("FROM Training", Training.class)
                    .getResultList();

            log.debug("Retrieved all trainings. Count: {}", trainings.size());

            return trainings;
        });
    }
}
