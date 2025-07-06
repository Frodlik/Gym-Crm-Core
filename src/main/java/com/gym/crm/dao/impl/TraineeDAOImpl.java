package com.gym.crm.dao.impl;

import com.gym.crm.dao.TraineeDAO;
import com.gym.crm.exception.DaoException;
import com.gym.crm.model.Trainee;
import com.gym.crm.util.HibernateUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class TraineeDAOImpl implements TraineeDAO {
    private static final Logger log = LoggerFactory.getLogger(TraineeDAOImpl.class);

    @Override
    public Trainee create(Trainee trainee) {
        return HibernateUtil.performReturningWithinSession(entityManager -> {
            entityManager.persist(trainee);

            log.info("Created Trainee with ID: {}", trainee.getId());

            return trainee;
        });
    }

    @Override
    public Optional<Trainee> findById(Long id) {
        return HibernateUtil.performReturningWithinSession(entityManager -> {
            Trainee trainee = entityManager.find(Trainee.class, id);

            log.debug("Found trainee with ID: {}", id);

            return Optional.ofNullable(trainee);
        });
    }

    @Override
    public List<Trainee> findAll() {
        return HibernateUtil.performReturningWithinSession(entityManager -> {
            List<Trainee> trainees = entityManager.createQuery("FROM Trainee", Trainee.class)
                    .getResultList();

            log.debug("Retrieved all trainees. Count: {}", trainees.size());

            return trainees;
        });
    }

    @Override
    public Trainee update(Trainee trainee) {
        return HibernateUtil.performReturningWithinSession(entityManager -> {
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
    public boolean delete(Long id) {
        return HibernateUtil.performReturningWithinSession(entityManager -> {
            Trainee trainee = entityManager.find(Trainee.class, id);

            if (trainee != null) {
                entityManager.remove(trainee);

                log.info("Trainee deleted with ID: {}", id);

                return true;
            }

            log.warn("Trainee not found for deletion with ID: {}", id);

            return false;
        });
    }
}
