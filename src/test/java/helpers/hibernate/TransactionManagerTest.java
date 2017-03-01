package helpers.hibernate;

import static java.util.Collections.singletonList;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.fail;

import entities.Player;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

/**
 * @author Andrey Antipov (gorttar@gmail.com) (2017-02-28)
 */
public class TransactionManagerTest {
    private final static EntityManagerFactory SHOP = Persistence.createEntityManagerFactory("shop");
    private final static TransactionManager TEST_OBJECT = new TransactionManager(SHOP);
    private final static Player TEST_PLAYER_1 = createTestPlayer("p1");

    private static void assertNoNewPlayers() {
        assertEquals(
                SHOP.createEntityManager().createQuery("select p from Player p", Player.class).getResultList(),
                singletonList(TEST_PLAYER_1));
    }

    private static void assertFailChecked(ERunnable toCheck) {
        try {
            toCheck.run();
            fail("Should throw IllegalStateException");
        } catch (IllegalStateException e) {
            assertEquals(TestException.class, e.getCause().getClass());
            assertEquals(e.getCause().getMessage(), "test error");
            assertNoNewPlayers();
        } catch (Throwable __) {
            fail("Should throw IllegalStateException");
        }
    }

    private static void assertFailUnchecked(ERunnable toCheck) {
        try {
            toCheck.run();
            fail("Should throw TestRuntimeException");
        } catch (TestRuntimeException e) {
            assertEquals(e.getMessage(), "test error");
            assertNoNewPlayers();
        } catch (Throwable __) {
            fail("Should throw TestRuntimeException");
        }

    }

    @BeforeMethod
    public void setUp() throws Exception {
        TEST_OBJECT.transactionAccept(
                em -> {
                    em.createNativeQuery("delete from player").executeUpdate();
                    em.persist(TEST_PLAYER_1);
                });
    }

    private static Player createTestPlayer(String name) {
        final Player player = new Player();
        player.setName(name);
        return player;
    }

    @Test
    public void testTransactionApply_success() throws Exception {
        assertEquals(
                TEST_OBJECT.transactionApply(em -> em.createQuery("select p from Player p", Player.class).getResultList()),
                singletonList(TEST_PLAYER_1));
    }

    @Test
    public void testTransactionApply_failChecked() throws Exception {
        assertFailChecked(
                () -> TEST_OBJECT.transactionApply(
                        em -> {
                            em.createQuery("select p from Player p", Player.class).getResultList();
                            throw new TestException("test error");
                        }));
    }

    @Test
    public void testTransactionApply_failUnchecked() throws Exception {
        assertFailUnchecked(
                () -> TEST_OBJECT.transactionApply(
                        em -> {
                            em.createQuery("select p from Player p", Player.class).getResultList();
                            throw new TestRuntimeException("test error");
                        }));
    }

    @Test
    public void testTransactionAccept_success() throws Exception {
        final Player player = createTestPlayer("p2");
        TEST_OBJECT.transactionAccept(em -> em.persist(player));
        assertEquals(SHOP.createEntityManager().createQuery("select p from Player p where p.name = 'p2'", Player.class).getResultList(), singletonList(player));
    }


    @Test
    public void testTransactionAccept_failChecked() throws Exception {
        assertFailChecked(
                () -> TEST_OBJECT.transactionAccept(
                        em -> {
                            final Player player = createTestPlayer("p2");
                            em.persist(player);
                            throw new TestException("test error");
                        }));
    }

    @Test
    public void testTransactionAccept_failUnchecked() throws Exception {
        assertFailUnchecked(
                () -> TEST_OBJECT.transactionAccept(
                        em -> {
                            final Player player = createTestPlayer("p2");
                            em.persist(player);
                            throw new TestRuntimeException("test error");
                        }));
    }


    @AfterClass
    public void tearDown() {
        SHOP.close();
    }

    private class TestException extends Exception {
        private TestException(String message) {
            super(message);
        }
    }

    private class TestRuntimeException extends RuntimeException {
        private TestRuntimeException(String message) {
            super(message);
        }
    }

    @FunctionalInterface
    private interface ERunnable {
        void run() throws Throwable;
    }
}