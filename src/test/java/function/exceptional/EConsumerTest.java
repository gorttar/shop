package function.exceptional;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;

import helpers.exceptions.TestException;
import helpers.exceptions.TestRuntimeException;
import org.testng.annotations.Test;

/**
 * @author Andrey Antipov (gorttar@gmail.com) (2017-03-02)
 */
public class EConsumerTest {
    private static final String TEST_ERROR_MESSAGE = "test error";

    @Test
    public void testAccept_success() throws Exception {
        int[] y = new int[1];
        final EConsumer<Integer> testObject = x -> y[0] = x + 1;
        testObject.accept(2);
        assertEquals(y[0], 3);
    }

    @Test
    public void testAccept_failChecked() throws Exception {
        final EConsumer<Integer> testObject = x -> {
            throw new TestException(TEST_ERROR_MESSAGE);
        };
        try {
            testObject.accept(2);
            fail("Should throw RuntimeException");
        } catch (RuntimeException e) {
            assertEquals(TestException.class, e.getCause().getClass());
            assertEquals(e.getCause().getMessage(), TEST_ERROR_MESSAGE);
        } catch (Throwable __) {
            fail("Should throw RuntimeException");
        }
    }

    @Test
    public void testAccept_failInterrupted() throws Exception {
        final EConsumer<Integer> testObject = x -> {
            throw new InterruptedException(TEST_ERROR_MESSAGE);
        };
        try {
            testObject.accept(2);
            fail("Should throw RuntimeException");
        } catch (RuntimeException e) {
            assertTrue(Thread.interrupted());
            assertEquals(InterruptedException.class, e.getCause().getClass());
            assertEquals(e.getCause().getMessage(), TEST_ERROR_MESSAGE);
        } catch (Throwable __) {
            fail("Should throw RuntimeException");
        }
    }

    @Test
    public void testAccept_failUnchecked() throws Exception {
        final EConsumer<Integer> testObject = x -> {
            throw new TestRuntimeException(TEST_ERROR_MESSAGE);
        };
        try {
            testObject.accept(2);
            fail("Should throw TestRuntimeException");
        } catch (TestRuntimeException e) {
            assertEquals(e.getMessage(), TEST_ERROR_MESSAGE);
        } catch (Throwable __) {
            fail("Should throw TestRuntimeException");
        }
    }
}