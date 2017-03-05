/*
 * Copyright (c) 2017 Andrey Antipov. All Rights Reserved.
 */
package entities;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import java.util.Objects;
import java.util.UUID;

/**
 * entity to represent ownership relation between item and player
 *
 * @author Andrey Antipov (gorttar@gmail.com) (2017-02-26)
 */
@Entity
public class OwnedItem {
    @Id
    @GeneratedValue
    private UUID id;

    @ManyToOne(optional = false)
    private Player owner;

    @ManyToOne(optional = false)
    private Item item;


    public Player getOwner() {
        return owner;
    }

    public void setOwner(Player owner) {
        this.owner = owner;
    }

    public Item getItem() {
        return item;
    }

    public void setItem(Item item) {
        this.item = item;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof OwnedItem)) return false;
        OwnedItem ownedItem = (OwnedItem) o;
        return Objects.equals(id, ownedItem.id) &&
                Objects.equals(owner, ownedItem.owner) &&
                Objects.equals(item, ownedItem.item);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, owner, item);
    }

    @Override
    public String toString() {
        return item.toString();
    }

    /**
     * factory method to create new {@link OwnedItem}
     *
     * @param owner of item
     * @param item  owned by owner
     * @return new {@link OwnedItem} for given owner and item
     */
    public static OwnedItem create(Player owner, Item item) {
        final OwnedItem ownedItem = new OwnedItem();
        ownedItem.setOwner(owner);
        ownedItem.setItem(item);
        return ownedItem;
    }
}