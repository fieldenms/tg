/**
 *
 */
package ua.com.fielden.platform.algorithm.vectormath;

import static org.junit.Assert.assertEquals;

import java.math.BigDecimal;
import java.math.RoundingMode;

import org.junit.Test;

/**
 * A test for {@link Vector}.
 *
 * @author TG Team
 *
 */
public class VectorTest {
    public VectorTest() {
    }

    private final Vector v1 = v(1, 2),
	    		v2 = v(3, 1);

    private static BigDecimal v(final int x) {
	return BigDecimal.valueOf(x).setScale(15, RoundingMode.HALF_UP);
    }

    private static Vector v(final int x, final int y) {
	return new Vector(v(x), v(y));
    }

    @Test
    public void test_addition() {
	assertEquals("Should be equal.", v(4, 3), v1.add(v2));
    }

    @Test
    public void test_negation() {
	assertEquals("Should be equal.", v(-1, -2), v1.negate());
    }

    @Test
    public void test_subtraction() {
	assertEquals("Should be equal.", v(-2, 1), v1.sub(v2));
    }

    @Test
    public void test_scalar_multiplication() {
	assertEquals("Should be equal.", v(3, 6), v1.mult(v(3)));
    }

    @Test
    public void test_cross_product() {
	assertEquals("Should be equal.", v(0), v(1, 1).crossProduct(v(1, 1)));
	assertEquals("Should be equal.", v(1), v(1, 1).crossProduct(v(0, 1)));
	assertEquals("Should be equal.", v(2), v(1, 1).crossProduct(v(-1, 1)));
	assertEquals("Should be equal.", v(1), v(1, 1).crossProduct(v(-1, 0)));
	assertEquals("Should be equal.", v(0), v(1, 1).crossProduct(v(-1, -1)));
	assertEquals("Should be equal.", v(-1), v(1, 1).crossProduct(v(0, -1)));
	assertEquals("Should be equal.", v(-2), v(1, 1).crossProduct(v(1, -1)));
	assertEquals("Should be equal.", v(-1), v(1, 1).crossProduct(v(1, 0)));
    }

}
