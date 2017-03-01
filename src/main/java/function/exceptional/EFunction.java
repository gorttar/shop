/*
 * Copyright (c) 2017 Andrey Antipov. All Rights Reserved.
 */
package function.exceptional;

import java.util.function.Function;

/**
 * @author Andrey Antipov (gorttar@gmail.com) (2017-02-28)
 */
@FunctionalInterface
public interface EFunction<A, Rt> extends Function<A, Rt> {
    Rt uApply(A a) throws Throwable;

    @Override
    default Rt apply(A a) {
        try {
            return uApply(a);
        } catch (RuntimeException | Error e) {
            // unchecked throwable instances can be rethrown as is
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