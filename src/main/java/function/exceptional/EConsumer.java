/*
 * Copyright (c) 2017 Andrey Antipov. All Rights Reserved.
 */
package function.exceptional;

import java.util.function.Consumer;

/**
 * extension of {@link Consumer} and {@link EFunction} interfaces which
 * can be implemented by lambdas throwing any {@link Throwable}
 *
 * @author Andrey Antipov (gorttar@gmail.com) (2017-02-28)
 */
@FunctionalInterface
public interface EConsumer<A> extends Consumer<A>, EFunction<A, Void> {
    /**
     * Performs this {@link EConsumer} on the given argument
     *
     * @param a the input argument
     * @throws Throwable in case of {@link EConsumer} implementation throws it
     */
    void eAccept(A a) throws Throwable;

    /**
     * implementation of {@link EFunction#eApply(Object)} in terms of {@link #eAccept(Object)}
     *
     * @param a the {@link EFunction} argument
     * @return null
     * @throws Throwable in case of {@link #eAccept(Object)} throws it
     */
    @Override
    default Void eApply(A a) throws Throwable {
        eAccept(a);
        return null;
    }

    /**
     * implementation of {@link Consumer#accept(Object)} in terms of {@link EFunction#apply(Object)}
     * <p>
     * rethrows unchecked {@link Throwable} instances thrown from invocation of {@link #eAccept(Object)} as is
     * <p>
     * wraps to {@link RuntimeException} and rethrows checked {@link Throwable} instances
     * thrown from invocation of {@link #eAccept(Object)}
     *
     * @param a the input argument
     */
    @Override
    default void accept(A a) {
        apply(a);
    }
}