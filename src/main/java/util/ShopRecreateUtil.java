/*
 * Copyright (c) 2017 Andrey Antipov. All Rights Reserved.
 */
package util;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import entities.Item;
import entities.Player;
import helpers.hibernate.SessionManager;
import helpers.xml.ItemsReader;

import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.stream.Collectors;

/**
 * utility class to reinitialise shop's database
 * !!! reinitialisation cleans up all previous data from database !!!
 *
 * @author Andrey Antipov (gorttar@gmail.com) (2017-03-05)
 */
public final class ShopRecreateUtil {
    private static final Gson GSON = new Gson();
    private static final ClassLoader CLASS_LOADER = ShopRecreateUtil.class.getClassLoader();
    private static final EntityManagerFactory SHOP = Persistence.createEntityManagerFactory("shop");
    private static final SessionManager SESSION_MANAGER = new SessionManager(SHOP);

    private ShopRecreateUtil() {
    }

    public static void main(String[] args) {
        try {
            List<Player> players = GSON.fromJson(
                    new Scanner(CLASS_LOADER.getResourceAsStream("players.json"), "UTF-8")
                            .useDelimiter("\\A")
                            .next(),
                    new TypeToken<ArrayList<Player>>() {
                    }.getType());
            final List<Item> items = ItemsReader
                    .readItems("items.xml")
                    .stream()
                    .map(xml.Item::toEntity)
                    .collect(Collectors.toList());
            SESSION_MANAGER.acceptWithTransaction(
                    em -> {
                        em.createNativeQuery("delete from OwnedItem").executeUpdate();
                        em.createNativeQuery("delete from Item").executeUpdate();
                        em.createNativeQuery("delete from Player").executeUpdate();

                        players.forEach(em::persist);
                        items.forEach(em::persist);
                    });
        } finally {
            SHOP.close();
        }
    }
}