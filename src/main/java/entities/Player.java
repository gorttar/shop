/*
 * Copyright (c) 2017 Andrey Antipov. All Rights Reserved.
 */
package entities;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * entity to represent player with access to shop
 *
 * @author Andrey Antipov (gorttar@gmail.com) (2017-02-26)
 */
@Entity
public class Player {
    @Id
    private String name;

    @Column
    private int money;

    @OneToMany(mappedBy = "owner", fetch = FetchType.EAGER)
    private List<OwnedItem> ownedItems;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getMoney() {
        return money;
    }

    public void setMoney(int money) {
        this.money = money;
    }

    public List<OwnedItem> getOwnedItems() {
        return ownedItems;
    }

    @SuppressWarnings("WeakerAccess")
    public void setOwnedItems(List<OwnedItem> ownedItems) {
        this.ownedItems = ownedItems;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Player)) return false;
        Player player = (Player) o;
        return Objects.equals(name, player.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }

    @Override
    public String toString() {
        return "Player{" +
                "name='" + name + '\'' +
                ", money=" + money +
                ", ownedItems=" + ownedItems +
                '}';
    }

    /**
     * factory method to create new {@link Player}
     *
     * @param name       of created player
     * @param money      of created player
     * @param itemsToOwn by created player
     * @return new {@link Player} for given name, money and itemsToOwn
     */
    public static Player create(String name, int money, Item... itemsToOwn) {
        final Player player = new Player();
        player.setName(name);
        player.setMoney(money);
        player.setOwnedItems(
                Arrays
                        .stream(itemsToOwn)
                        .distinct()
                        .map(item -> OwnedItem.create(player, item))
                        .collect(Collectors.toList()));
        return player;
    }
}