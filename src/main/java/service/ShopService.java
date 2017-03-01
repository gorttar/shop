/*
 * Copyright (c) 2017 Andrey Antipov. All Rights Reserved.
 */
package service;

import entities.Item;
import entities.Player;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Optional;

/**
 * @author Andrey Antipov (gorttar@gmail.com) (2017-03-02)
 */
public class ShopService {
    public Optional<Player> findPlayer(@Nonnull String name) {
        throw new AssertionError("NI");
    }

    public Optional<Item> findItem(@Nonnull String name) {
        throw new AssertionError("NI");
    }

    public List<Item> listItems() {
        throw new AssertionError("NI");
    }

    public boolean buy(@Nonnull Player player, @Nonnull Item item) {
        throw new AssertionError("NI");
    }

    public void sell(@Nonnull Player player, @Nonnull Item item) {
        throw new AssertionError("NI");
    }
}