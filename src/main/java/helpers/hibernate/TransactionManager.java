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
public final class TransactionManager {
    @Nonnull
    private final EntityManagerFactory entityManagerFactory;

    public TransactionManager(@Nonnull EntityManagerFactory entityManagerFactory) {
        Objects.requireNonNull(entityManagerFactory);
        this.entityManagerFactory = entityManagerFactory;
    }

    public void transactionAccept(EConsumer<? super EntityManager> transactionBody) {
        transactionApply(transactionBody);
    }

    public <T> T transactionApply(EFunction<? super EntityManager, ? extends T> transactionBody) {
        final EntityManager em = entityManagerFactory.createEntityManager();
        final T result;
        // shouldn't use try-with-resources because EntityManager is not subclass of AutoCloseable
        final EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            result = transactionBody.apply(em);
            tx.commit();
        } catch (RuntimeException e) {
            tx.rollback();
            throw e;
        } finally {
            em.close();
        }
        return result;
    }

}