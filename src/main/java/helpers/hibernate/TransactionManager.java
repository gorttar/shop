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
        final EntityTransaction tx = em.getTransaction();
        final T result;
        try {
            tx.begin();
            result = transactionBody.uApply(em);
            tx.commit();
            // unchecked throwable instances can be rethrown as is
        } catch (RuntimeException | Error e) {
            throw e;
            // checked ones should be wrapped to illegal state exception
        } catch (Throwable t) {
            throw new IllegalStateException("Exception during transaction execution", t);
        } finally {
            em.close();
        }
        return result;
    }

}