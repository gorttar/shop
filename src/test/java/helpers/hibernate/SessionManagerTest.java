package helpers.hibernate;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;

import entities.Player;
import helpers.exceptions.TestException;
import org.testng.Assert.ThrowingRunnable;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.Persistence;
import java.util.List;

/**
 * @author Andrey Antipov (gorttar@gmail.com) (2017-02-28)
 */
public class SessionManagerTest {
    private static final EntityManagerFactory SHOP = Persistence.createEntityManagerFactory("shop");
    private static final SessionManager TEST_OBJECT = new SessionManager(SHOP);
    private static final Player TEST_PLAYER_1 = Player.create("p1", 0);
    private static final String TEST_ERROR_MESSAGE = "test error";

    private static void assertNoNewPlayers() {
        assertEquals(
                SHOP.createEntityManager().createQuery("select p from Player p", Player.class).getResultList(),
                singletonList(TEST_PLAYER_1));
    }

    @BeforeMethod
    public void setUp() throws Exception {
        final EntityManager em = SHOP.createEntityManager();
        try {
            final EntityTransaction tx = em.getTransaction();
            tx.begin();
            em.createNativeQuery("delete from player").executeUpdate();
            em.persist(TEST_PLAYER_1);
            tx.commit();
        } finally {
            em.close();
        }
    }

    @Test
    public void testApplyWithTransaction_success() throws Exception {
        final Player player = Player.create("p2", 0);
        final List<Player> actual = TEST_OBJECT.applyWithTransaction(
                em -> {
                    em.persist(player);
                    return em.createQuery("select p from Player p order by p.name", Player.class).getResultList();
                });
        assertEquals(
                actual,
                asList(TEST_PLAYER_1, player));
    }

    @Test
    public void testApplyWithTransaction_fail() throws Exception {
        final Player player = Player.create("p2", 0);
        ThrowingRunnable toCheck = () -> TEST_OBJECT.applyWithTransaction(
                em -> {
                    em.persist(player);
                    em.createQuery("select p from Player p", Player.class).getResultList();
                    throw new TestException(TEST_ERROR_MESSAGE);
                });
        try {
            toCheck.run();
            fail("Should throw RuntimeException");
        } catch (RuntimeException __) {
            assertNoNewPlayers();
        } catch (Throwable __) {
            fail("Should throw RuntimeException");
        }
    }

    @Test
    public void testAcceptWithTransaction_success() throws Exception {
        final Player player = Player.create("p2", 0);
        TEST_OBJECT.acceptWithTransaction(em -> em.persist(player));
        assertEquals(
                SHOP.createEntityManager().createQuery("select p from Player p order by p.name", Player.class).getResultList(),
                asList(TEST_PLAYER_1, player));
    }

    @Test
    public void testAcceptWithTransaction_fail() throws Exception {
        ThrowingRunnable toCheck = () -> TEST_OBJECT.acceptWithTransaction(
                em -> {
                    final Player player = Player.create("p2", 0);
                    em.persist(player);
                    em.createQuery("select p from Player p", Player.class).getResultList();
                    throw new TestException(TEST_ERROR_MESSAGE);
                });
        try {
            toCheck.run();
            fail("Should throw RuntimeException");
        } catch (RuntimeException __) {
            assertNoNewPlayers();
        } catch (Throwable __) {
            fail("Should throw RuntimeException");
        }
    }

    @Test
    public void testApplyWithSession_success() throws Exception {
        final EntityManager exposed = TEST_OBJECT.applyWithSession(
                em -> {
                    assertTrue(em.isOpen());
                    return em;
                });
        assertFalse(exposed.isOpen());
    }

    @Test
    public void testApplyWithSession_fail() throws Exception {
        EntityManager[] exposed = new EntityManager[1];
        try {
            TEST_OBJECT.applyWithSession(
                    em -> {
                        assertTrue(em.isOpen());
                        exposed[0] = em;
                        throw new TestException(TEST_ERROR_MESSAGE);
                    });
            fail("Should throw RuntimeException");
        } catch (RuntimeException __) {
            assertFalse(exposed[0].isOpen());
        } catch (Throwable __) {
            fail("Should throw RuntimeException");
        }
    }

    @AfterClass
    public void tearDown() {
        SHOP.close();
    }
}