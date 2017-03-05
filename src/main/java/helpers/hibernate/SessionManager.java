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
 * helper to deal with {@link EntityManager} sessions both transactional and not
 *
 * @author Andrey Antipov (gorttar@gmail.com) (2017-02-28)
 */
public class SessionManager {
    @Nonnull
    private final EntityManagerFactory entityManagerFactory;

    public SessionManager(@Nonnull EntityManagerFactory entityManagerFactory) {
        Objects.requireNonNull(entityManagerFactory);
        this.entityManagerFactory = entityManagerFactory;
    }

    /**
     * preforms transactional operation represented by transaction body
     *
     * @param transactionBody to be performed under {@link EntityTransaction}
     */
    public void acceptWithTransaction(EConsumer<? super EntityManager> transactionBody) {
        applyWithTransaction(transactionBody);
    }

    /**
     * evaluates transactional function represented by transaction body and returns it's result
     *
     * @param transactionBody to be evaluated under {@link EntityTransaction}
     * @param <T>             result type
     * @return transaction body evaluation result
     */
    public <T> T applyWithTransaction(EFunction<? super EntityManager, ? extends T> transactionBody) {
        return applyWithSession(withTransaction(transactionBody));
    }

    /**
     * evaluates function represented by body and returns it's result
     *
     * @param body to be evaluated with {@link EntityManager} as argument
     * @param <T>  result type
     * @return body evaluation result
     */
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

    /**
     * decorates given body with transaction handling
     *
     * @param body to be decorated
     * @param <T>  decorated function's result type
     * @return decorated function with transaction handling (transactional function)
     */
    private static <T> EFunction<EntityManager, T> withTransaction(EFunction<? super EntityManager, ? extends T> body) {
        return em -> {
            final T result;
            final EntityTransaction tx = em.getTransaction();
            try {
                tx.begin();
                result = body.apply(em);
                tx.commit();
            } catch (RuntimeException e) {
                tx.rollback();
                throw e;
            }
            return result;
        };
    }
}