/*
 * Copyright (c) 2017 Andrey Antipov. All Rights Reserved.
 */
package helpers.hibernate;

import function.exceptional.EConsumer;
import function.exceptional.EFunction;

import javax.annotation.Nonnull;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import java.util.Objects;

/**
 * @author Andrey Antipov (gorttar@gmail.com) (2017-02-28)
 */
public class SessionManager {
    @Nonnull
    private final EntityManagerFactory entityManagerFactory;

    public SessionManager(@Nonnull EntityManagerFactory entityManagerFactory) {
        Objects.requireNonNull(entityManagerFactory);
        this.entityManagerFactory = entityManagerFactory;
    }

    public void acceptWithTransaction(EConsumer<? super EntityManager> transactionBody) {
        applyWithTransaction(transactionBody);
    }

    public <T> T applyWithTransaction(EFunction<? super EntityManager, ? extends T> transactionBody) {
        return applyWithSession(withTransaction(transactionBody));
    }

    public <T> T applyWithSession(EFunction<? super EntityManager, ? extends T> body) {
        final EntityManager em = entityManagerFactory.createEntityManager();
        final T result;
        // shouldn't use try-with-resources because EntityManager is not subclass of AutoCloseable
        try {
            result = body.apply(em);
        } finally {
            em.close();
        }
        return result;
    }

    private static <T> EFunction<EntityManager, T> withTransaction(EFunction<? super EntityManager, ? extends T> transactionBody) {
        return em -> {
            final T result;
            final EntityTransaction tx = em.getTransaction();
            try {
                tx.begin();
                result = transactionBody.apply(em);
                tx.commit();
            } catch (RuntimeException e) {
                tx.rollback();
                throw e;
            }
            return result;
        };
    }
}