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
public class EFunctionTest {
    private static final String TEST_ERROR_MESSAGE = "test error";

    @Test
    public void testApply_success() throws Exception {
        final EFunction<Integer, String> testObject = x -> "" + (x + 1);
        assertEquals(testObject.apply(2), "3");
    }

    @Test
    public void testApply_failChecked() throws Exception {
        final EFunction<Integer, String> testObject = x -> {
            throw new TestException(TEST_ERROR_MESSAGE);
        };
        try {
            testObject.apply(2);
            fail("Should throw RuntimeException");
        } catch (RuntimeException e) {
            assertEquals(TestException.class, e.getCause().getClass());
            assertEquals(e.getCause().getMessage(), TEST_ERROR_MESSAGE);
        } catch (Throwable __) {
            fail("Should throw RuntimeException");
        }
    }

    @Test
    public void testApply_failInterrupted() throws Exception {
        final EFunction<Integer, String> testObject = x -> {
            throw new InterruptedException(TEST_ERROR_MESSAGE);
        };
        try {
            testObject.apply(2);
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
    public void testApply_failUnchecked() throws Exception {
        final EFunction<Integer, String> testObject = x -> {
            throw new TestRuntimeException(TEST_ERROR_MESSAGE);
        };
        try {
            testObject.apply(2);
            fail("Should throw TestRuntimeException");
        } catch (TestRuntimeException e) {
            assertEquals(e.getMessage(), TEST_ERROR_MESSAGE);
        } catch (Throwable __) {
            fail("Should throw TestRuntimeException");
        }
    }
}