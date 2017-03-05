/*
 * Copyright (c) 2017 Andrey Antipov. All Rights Reserved.
 */
package service;

import static java.util.Objects.requireNonNull;

import entities.Item;
import entities.OwnedItem;
import entities.Player;
import helpers.hibernate.SessionManager;

import javax.annotation.Nonnull;
import javax.persistence.EntityManager;
import java.util.List;
import java.util.Optional;

/**
 * service to serve shop's operations
 *
 * @author Andrey Antipov (gorttar@gmail.com) (2017-03-02)
 */
public class ShopService {
    @Nonnull
    private final SessionManager sessionManager;

    public ShopService(@Nonnull SessionManager sessionManager) {
        this.sessionManager = requireNonNull(sessionManager);
    }

    /**
     * searches for player with given name
     *
     * @param name of player to be found
     * @return search result wrapped to {@link Optional}
     */
    public Optional<Player> findPlayer(@Nonnull String name) {
        return sessionManager.applyWithSession(em -> _findPlayer(name, em));
    }

    private static Optional<Player> _findPlayer(@Nonnull String name, EntityManager em) {
        return em
                .createQuery("select p from Player p where p.name=:name", Player.class)
                .setParameter("name", requireNonNull(name))
                .getResultList()
                .stream()
                .findFirst();
    }

    /**
     * searches for item with given name
     *
     * @param name of item to be found
     * @return search result wrapped to {@link Optional}
     */
    public Optional<Item> findItem(@Nonnull String name) {
        return sessionManager.applyWithSession(em -> _findItem(name, em));
    }

    private static Optional<Item> _findItem(@Nonnull String name, EntityManager em) {
        return em
                .createQuery("select i from Item i where i.name=:name", Item.class)
                .setParameter("name", requireNonNull(name))
                .getResultList()
                .stream()
                .findFirst();
    }

    /**
     * @return list of items available in shop
     */
    public List<Item> listItems() {
        return sessionManager.applyWithSession(
                em -> em
                        .createQuery("select i from Item i order by i.name", Item.class)
                        .getResultList());
    }

    /**
     * buy given item by given player
     *
     * @param player which wants to buy item
     * @param item   to buy
     * @return true if item was bought by player and false otherwise
     * @throws IllegalStateException in case of wrong arguments
     */
    public boolean buy(@Nonnull Player player, @Nonnull Item item) throws IllegalStateException {
        return sessionManager.applyWithTransaction(
                em -> {
                    final Player reloadedPlayer = _findPlayer(requireNonNull(player).getName(), em)
                            .orElseThrow(
                                    () -> new IllegalStateException(
                                            String.format("Player %s not found in database", player)));
                    final Item reloadedItem = _findItem(requireNonNull(item).getName(), em)
                            .orElseThrow(
                                    () -> new IllegalStateException(
                                            String.format("Item %s not found in database", item)));
                    final int money = reloadedPlayer.getMoney();
                    final int price = reloadedItem.getPrice();
                    final boolean isEnoughMoney = price <= money;
                    if (isEnoughMoney) {
                        em.persist(OwnedItem.create(reloadedPlayer, reloadedItem));
                        reloadedPlayer.setMoney(money - price);
                        em.merge(reloadedPlayer);
                    }
                    return isEnoughMoney;
                });
    }

    /**
     * sell given item by given player
     *
     * @param player which wants to sell item
     * @param item   to sell
     * @throws IllegalStateException in case of wrong arguments
     */
    public void sell(@Nonnull Player player, @Nonnull Item item) throws IllegalStateException {
        requireNonNull(item);
        sessionManager.acceptWithTransaction(
                em -> {
                    final Player reloadedPlayer = _findPlayer(requireNonNull(player).getName(), em)
                            .orElseThrow(
                                    () -> new IllegalStateException(
                                            String.format("Player %s not found in database", player)));
                    final OwnedItem toSell = reloadedPlayer
                            .getOwnedItems()
                            .stream()
                            .filter(ownedItem -> ownedItem.getItem().equals(item))
                            .findFirst()
                            .orElseThrow(
                                    () -> new IllegalStateException(
                                            String.format("Item %s is not owned by player %s", item, player)));
                    reloadedPlayer.setMoney(reloadedPlayer.getMoney() + toSell.getItem().getPrice());
                    em.remove(toSell);
                    em.merge(reloadedPlayer);
                });
    }
}