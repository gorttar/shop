package service;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import entities.Item;
import entities.OwnedItem;
import entities.Player;
import helpers.hibernate.SessionManager;
import org.testng.annotations.AfterClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.testng.internal.collections.Pair;

import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author Andrey Antipov (gorttar@gmail.com) (2017-03-02)
 */
public class ShopServiceTest {
    private static final EntityManagerFactory SHOP = Persistence.createEntityManagerFactory("shop");
    private static final SessionManager SESSION_MANAGER = new SessionManager(SHOP);
    private static final ShopService TEST_OBJECT = new ShopService(SESSION_MANAGER);

    private static void cleanDb() {
        SESSION_MANAGER.acceptWithTransaction(
                em -> {
                    em.createNativeQuery("delete from OwnedItem").executeUpdate();
                    em.createNativeQuery("delete from Item").executeUpdate();
                    em.createNativeQuery("delete from Player").executeUpdate();
                });
    }

    private static void prepareTestDb(Pair<Player, Item> pair) {
        cleanDb();
        SESSION_MANAGER.acceptWithTransaction(
                em -> {
                    final Player player = pair.first();
                    em.persist(player);
                    player
                            .getOwnedItems()
                            .forEach(
                                    ownedItem -> {
                                        em.persist(ownedItem.getItem());
                                        em.persist(ownedItem);
                                    });
                    em.persist(pair.second());
                });
    }

    private static Map<Item, Integer> getCountsOfOwnedItems(Player player) {
        return player
                .getOwnedItems()
                .stream()
                .map(OwnedItem::getItem)
                .collect(Collectors.toMap(x -> x, __ -> 1, Integer::sum));
    }

    private static Player reloadPlayer() {
        return SESSION_MANAGER.applyWithSession(
                em -> em
                        .createQuery("select p from Player p", Player.class)
                        .getSingleResult());
    }

    @DataProvider(name = "testFindPlayer")
    public Object[][] data4testFindPlayer() {
        cleanDb();

        final Player player1 = Player.create("player1", 0);
        final Player player2 = Player.create("player2", 0);
        SESSION_MANAGER.acceptWithTransaction(
                em -> {
                    em.persist(player1);
                    em.persist(player2);
                });

        return new Object[][]{
                {"player1", Optional.of(player1)},
                {"player2", Optional.of(player2)},
                {"absent", Optional.empty()}
        };
    }

    @Test(dataProvider = "testFindPlayer")
    public void testFindPlayer(String name,
                               @SuppressWarnings("OptionalUsedAsFieldOrParameterType") Optional<Player> expected)
            throws Exception {
        assertEquals(TEST_OBJECT.findPlayer(name), expected);
    }

    @DataProvider(name = "testFindItem")
    public Object[][] data4testFindItem() {
        cleanDb();

        final Item item1 = Item.create("item1", 0);
        final Item item2 = Item.create("item2", 0);
        SESSION_MANAGER.acceptWithTransaction(
                em -> {
                    em.persist(item1);
                    em.persist(item2);
                });

        return new Object[][]{
                {"item1", Optional.of(item1)},
                {"item2", Optional.of(item2)},
                {"absent", Optional.empty()}
        };
    }

    @Test(dataProvider = "testFindItem")
    public void testFindItem(String name,
                             @SuppressWarnings("OptionalUsedAsFieldOrParameterType") Optional<Item> expected)
            throws Exception {
        assertEquals(TEST_OBJECT.findItem(name), expected);
    }

    @DataProvider(name = "testListItems")
    public Iterator<Object[]> data4testListItems() {
        return Stream
                .of(
                        emptyList(),
                        singletonList(Item.create("item1", 0)),
                        asList(Item.create("item2", 0), Item.create("item1", 0), Item.create("item3", 0)))
                .peek(
                        items -> {
                            cleanDb();
                            SESSION_MANAGER.acceptWithTransaction(em -> items.forEach(em::persist));
                        })
                .map(items -> new Object[]{items})
                .iterator();
    }

    @Test(dataProvider = "testListItems")
    public void testListItems(List<Item> expected) throws Exception {
        assertEquals(
                TEST_OBJECT.listItems(),
                expected
                        .stream()
                        .sorted(Comparator.comparing(Item::getName))
                        .collect(Collectors.toList()));
    }

    @DataProvider(name = "testBuy")
    public Iterator<Object[]> data4testBuy() {
        final Item item4Case3 = Item.create("item", 50);
        final Player richPlayerWithSameItem = Player.create(
                "rich player owning the same item he wants to buy", 100, item4Case3);

        return Stream
                .of(
                        Pair.of(Player.create("rich player without items", 100), Item.create("item", 50)),
                        Pair.of(
                                Player.create(
                                        "rich player owning item differ from one he wants to buy",
                                        100,
                                        Item.create("ownedItem", 50)),
                                Item.create("item", 50)),
                        Pair.of(richPlayerWithSameItem, item4Case3))
                .peek(ShopServiceTest::prepareTestDb)
                .map(pair -> new Object[]{pair.first(), pair.second()})
                .iterator();
    }

    @Test(dataProvider = "testBuy")
    public void testBuy(Player player, Item item) throws Exception {
        final int moneyBeforeBuy = player.getMoney();

        Map<Item, Integer> expectedCountsOfOwnedItems = getCountsOfOwnedItems(player);

        assertTrue(TEST_OBJECT.buy(player, item));

        final Player reloadedPlayer = reloadPlayer();

        expectedCountsOfOwnedItems.compute(item, (__, prev) -> prev != null ? prev + 1 : 1);

        assertEquals(reloadedPlayer.getMoney(), moneyBeforeBuy - item.getPrice());
        assertEquals(getCountsOfOwnedItems(reloadedPlayer), expectedCountsOfOwnedItems);
    }

    @Test
    public void testBuy_notEnoughMoney() {
        cleanDb();
        final Player player = Player.create("poor player without items", 25);
        final Item item = Item.create("item", 50);
        SESSION_MANAGER.acceptWithTransaction(
                em -> {
                    em.persist(player);
                    em.persist(item);
                });

        final int expectedMoney = player.getMoney();
        final Map<Item, Integer> expectedCountsOfOwnedItems = getCountsOfOwnedItems(player);

        assertFalse(TEST_OBJECT.buy(player, item));
        final Player reloadedPlayer = reloadPlayer();
        assertEquals(reloadedPlayer.getMoney(), expectedMoney);
        assertEquals(getCountsOfOwnedItems(reloadedPlayer), expectedCountsOfOwnedItems);
    }

    @Test(
            expectedExceptions = IllegalStateException.class,
            expectedExceptionsMessageRegExp = "Player .* not found in database")
    public void testBuy_failNoPlayer() throws Exception {
        cleanDb();
        final Player player = Player.create("absent", 100);
        final Item item = Item.create("item", 50);
        SESSION_MANAGER.acceptWithTransaction(em -> em.persist(item));
        TEST_OBJECT.buy(player, item);
    }

    @Test(
            expectedExceptions = IllegalStateException.class,
            expectedExceptionsMessageRegExp = "Item .* not found in database")
    public void testBuy_failNoItem() throws Exception {
        cleanDb();
        final Player player = Player.create("player", 100);
        final Item item = Item.create("absent", 50);
        SESSION_MANAGER.acceptWithTransaction(em -> em.persist(player));
        TEST_OBJECT.buy(player, item);
    }

    @DataProvider(name = "testSell")
    public Iterator<Object[]> data4testSell() {
        final Item item4Case1 = Item.create("item", 50);
        final Item item4Case2 = Item.create("item", 50);

        return Stream
                .of(
                        Pair.of(
                                Player.create(
                                        "player owning one copy of item to sell",
                                        100,
                                        Item.create("item1", 25),
                                        item4Case1),
                                item4Case1),
                        Pair.of(
                                Player.create(
                                        "player owning two copies of item to sell",
                                        100,
                                        item4Case2,
                                        item4Case2),
                                item4Case2))
                .peek(ShopServiceTest::prepareTestDb)
                .map(pair -> new Object[]{pair.first(), pair.second()})
                .iterator();
    }

    @Test(dataProvider = "testSell")
    public void testSell(Player player, Item item) throws Exception {
        final int moneyBeforeSell = player.getMoney();
        final Map<Item, Integer> expectedCountsOfOwnedItems = getCountsOfOwnedItems(player);

        TEST_OBJECT.sell(player, item);

        if (expectedCountsOfOwnedItems.get(item) > 1) {
            expectedCountsOfOwnedItems.compute(item, (__, prev) -> prev - 1);
        } else {
            expectedCountsOfOwnedItems.remove(item);
        }

        final Player reloadedPlayer = reloadPlayer();
        assertEquals(reloadedPlayer.getMoney(), moneyBeforeSell + item.getPrice());
        assertEquals(getCountsOfOwnedItems(reloadedPlayer), expectedCountsOfOwnedItems);
    }

    @Test(
            expectedExceptions = IllegalStateException.class,
            expectedExceptionsMessageRegExp = "Player .* not found in database")
    public void testSell_failNoPlayer() throws Exception {
        cleanDb();
        final Player player = Player.create("absent", 100);
        final Item item = Item.create("item", 50);
        SESSION_MANAGER.acceptWithTransaction(em -> em.persist(item));
        TEST_OBJECT.sell(player, item);
    }

    @Test(
            expectedExceptions = IllegalStateException.class,
            expectedExceptionsMessageRegExp = "Item .* is not owned by player .*")
    public void testSell_failNotOwningItem() throws Exception {
        cleanDb();
        final Player player = Player.create("player", 100);
        final Item item = Item.create("item", 50);
        SESSION_MANAGER.acceptWithTransaction(
                em -> {
                    em.persist(player);
                    em.persist(item);
                });
        TEST_OBJECT.sell(player, item);
    }


    @AfterClass
    public void tearDown() {
        cleanDb();
        SHOP.close();
    }
}