/*
 * Copyright (c) 2017 Andrey Antipov. All Rights Reserved.
 */
package entities;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import java.util.UUID;

/**
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
}