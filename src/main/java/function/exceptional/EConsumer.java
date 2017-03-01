/*
 * Copyright (c) 2017 Andrey Antipov. All Rights Reserved.
 */
package function.exceptional;

import java.util.function.Consumer;

/**
 * @author Andrey Antipov (gorttar@gmail.com) (2017-02-28)
 */
@FunctionalInterface
public interface EConsumer<A> extends Consumer<A>, EFunction<A, Void> {
    void uAccept(A a) throws Throwable;

    @Override
    default Void uApply(A a) throws Throwable {
        uAccept(a);
        return null;
    }

    @Override
    default void accept(A a) {
        apply(a);
    }
}