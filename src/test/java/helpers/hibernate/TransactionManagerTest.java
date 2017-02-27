package helpers.hibernate;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.fail;

import entities.Player;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.Persistence;

/**
 * @author Andrey Antipov (gorttar@gmail.com) (2017-02-28)
 */
public class TransactionManagerTest {
    private final static EntityManagerFactory SHOP = Persistence.createEntityManagerFactory("shop");
    private final static TransactionManager TEST_OBJECT = new TransactionManager(SHOP);

    @BeforeMethod
    public void setUp() throws Exception {
        final EntityManager em = SHOP.createEntityManager();
        final EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            em.createNativeQuery("delete from player").executeUpdate();
            tx.commit();
        } finally {
            em.close();
        }
    }

    @Test
    public void testTransactionAccept_success() throws Exception {
        final Player player = new Player();
        player.setName("p1");
        TEST_OBJECT.transactionAccept(em -> em.persist(player));
        TEST_OBJECT.transactionAccept(
                em -> assertEquals(
                        em.createQuery("select p from Player p", Player.class).getResultList(),
                        singletonList(player)));
    }

    @Test
    public void testTransactionAccept_fail() throws Exception {
        final Player player = new Player();
        player.setName("p1");
        try {
            TEST_OBJECT.transactionAccept(
                    em -> {
                        em.persist(player);
                        throw new Throwable("test error");
                    });
            fail("Should throw exception");
        } catch (IllegalStateException e) {
            TEST_OBJECT.transactionAccept(
                    em -> assertEquals(
                            em.createQuery("select p from Player p", Player.class).getResultList(),
                            emptyList()));
        }
    }

    @AfterClass
    public void tearDown() {
        SHOP.close();
    }
}