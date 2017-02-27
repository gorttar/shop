/*
 * Copyright (c) 2017 Andrey Antipov. All Rights Reserved.
 */
package helpers.hibernate;

import function.exceptional.EC;
import function.exceptional.EF;

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

    public void transactionAccept(EC<? super EntityManager> transactionBody) {
        transactionApply(transactionBody);
    }

    public <T> T transactionApply(EF<? super EntityManager, ? extends T> transactionBody) {
        final EntityManager em = entityManagerFactory.createEntityManager();
        final EntityTransaction tx = em.getTransaction();
        final T result;
        try {
            tx.begin();
            result = transactionBody.uApply(em);
            tx.commit();
        } catch (Throwable t) {
            tx.rollback();
            throw new IllegalStateException("Exception during transaction execution", t);
        } finally {
            em.close();
        }
        return result;
    }

}