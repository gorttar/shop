/*
 * Copyright (c) 2017 Andrey Antipov. All Rights Reserved.
 */
package entities;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import java.util.List;

/**
 * @author Andrey Antipov (gorttar@gmail.com) (2017-02-26)
 */
@Entity
public class Player {
    @Id
    private String name;

    @Column
    private long money;

    @OneToMany(mappedBy = "owner", fetch = FetchType.EAGER)
    private List<OwnedItem> ownedItems;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getMoney() {
        return money;
    }

    public void setMoney(long money) {
        this.money = money;
    }

    public List<OwnedItem> getOwnedItems() {
        return ownedItems;
    }

    public void setOwnedItems(List<OwnedItem> ownedItems) {
        this.ownedItems = ownedItems;
    }
}