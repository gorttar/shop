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
 * @author Andrey Antipov (gorttar@gmail.com) (2017-03-02)
 */
public class ShopService {
    @Nonnull
    private final SessionManager sessionManager;

    public ShopService(@Nonnull SessionManager sessionManager) {
        this.sessionManager = requireNonNull(sessionManager);
    }

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

    public List<Item> listItems() {
        return sessionManager.applyWithSession(
                em -> em
                        .createQuery("select i from Item i order by i.name", Item.class)
                        .getResultList());
    }

    public boolean buy(@Nonnull Player player, @Nonnull Item item) {
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

    public void sell(@Nonnull Player player, @Nonnull Item item) {
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