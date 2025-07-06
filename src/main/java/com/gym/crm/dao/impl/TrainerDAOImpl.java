package com.gym.crm.dao.impl;

import com.gym.crm.dao.TrainerDAO;
import com.gym.crm.exception.DaoException;
import com.gym.crm.model.Trainer;
import com.gym.crm.util.HibernateUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class TrainerDAOImpl implements TrainerDAO {
    private static final Logger log = LoggerFactory.getLogger(TrainerDAOImpl.class);

    @Override
    public Trainer create(Trainer trainer) {
        return HibernateUtil.performReturningWithinSession(entityManager -> {
            entityManager.persist(trainer);

            log.info("Created Trainer with ID: {}", trainer.getId());

            return trainer;
        });
    }

    @Override
    public Optional<Trainer> findById(Long id) {
        return HibernateUtil.performReturningWithinSession(entityManager -> {
            Trainer trainer = entityManager.find(Trainer.class, id);

            log.debug("Trainer found with ID: {}", id);

            return Optional.ofNullable(trainer);
        });
    }

    @Override
    public List<Trainer> findAll() {
        return HibernateUtil.performReturningWithinSession(entityManager -> {
            List<Trainer> trainers = entityManager.createQuery("FROM Trainer", Trainer.class)
                    .getResultList();

            log.debug("Retrieved all trainers. Count: {}", trainers.size());

            return trainers;
        });
    }

    @Override
    public Trainer update(Trainer trainer) {
        return HibernateUtil.performReturningWithinSession(entityManager -> {
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
