/*
 * Copyright (c) 2017 Andrey Antipov. All Rights Reserved.
 */
package function.exceptional;

import java.util.function.Function;

/**
 * extension of {@link Function} interface which can be implemented by lambdas throwing any {@link Throwable}
 *
 * @author Andrey Antipov (gorttar@gmail.com) (2017-02-28)
 */
@FunctionalInterface
public interface EFunction<A, Rt> extends Function<A, Rt> {
    /**
     * Applies this {@link EFunction} to the given argument
     *
     * @param a the {@link EFunction} argument
     * @return the {@link EFunction} result
     * @throws Throwable in case of {@link EFunction} implementation throws it
     */
    Rt eApply(A a) throws Throwable;

    /**
     * implementation of {@link Function#apply(Object)} in terms of {@link #eApply(Object)}
     * <p>
     * rethrows unchecked {@link Throwable} instances thrown from invocation of {@link #eApply(Object)} as is
     * <p>
     * wraps to {@link RuntimeException} and rethrows checked {@link Throwable} instances
     * thrown from invocation of {@link #eApply(Object)}
     *
     * @param a the {@link EFunction} argument
     * @return the {@link EFunction} result
     */
    @Override
    default Rt apply(A a) {
        try {
            return eApply(a);
        } catch (RuntimeException | Error e) {
            // unchecked throwable instances should be rethrown as is
            throw e;
        } catch (InterruptedException e) {
            // interrupted exception should be wrapped and rethrown after setting interrupted flag
            Thread.currentThread().interrupt();
            throw new RuntimeException(e);
        } catch (Throwable t) {
            // checked ones should be wrapped to RuntimeException
            throw new RuntimeException(t);
        }
    }
}