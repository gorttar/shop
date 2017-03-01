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
        } catch (Throwable t) {
            throw new RuntimeException(t);
        }
    }
}