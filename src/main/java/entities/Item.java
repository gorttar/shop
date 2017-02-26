/*
 * Copyright (c) 2017 Andrey Antipov. All Rights Reserved.
 */
package entities;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;

/**
 * @author Andrey Antipov (gorttar@gmail.com) (2017-02-26)
 */
@Entity
public class Item {
    @Id
    private String name;

    @Column
    private long price;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getPrice() {
        return price;
    }

    public void setPrice(long price) {
        this.price = price;
    }
}