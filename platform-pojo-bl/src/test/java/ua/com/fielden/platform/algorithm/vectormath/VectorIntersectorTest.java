package ua.com.fielden.platform.algorithm.vectormath;

import static org.junit.Assert.assertEquals;

import java.math.BigDecimal;
import java.math.RoundingMode;

import org.junit.Test;

import ua.com.fielden.platform.utils.Pair;

/**
 * A test for {@link VectorIntersector}.
 *
 * @author TG Team
 *
 */
public class VectorIntersectorTest {
    public VectorIntersectorTest() {
    }

    private static BigDecimal v(final int w) {
	return BigDecimal.valueOf(w).setScale(15, RoundingMode.HALF_UP);
    }

    // Primary end
    @Test
    public void test_tangent_intersection_in_primary_end() {
	assertEquals("Should be equal.", new Pair<>(true, new Pair<>(v(3), v(-3))),
		VectorIntersector.intersect(v(1), v(-2), v(3), v(-3), v(2), v(-1), v(4), v(-5)));
    }

    @Test
    public void test_tangent_not_intersection_in_primary_end() {
	assertEquals("Should be equal.", new Pair<>(false, null),
		VectorIntersector.intersect(v(1), v(-2), v(3), v(-3), v(1), v(-3), v(4), v(-3)));
    }

    @Test
    public void test_tangent_not_intersection_if_collinear_in_primary_end() {
	assertEquals("Should be equal.", new Pair<>(false, null),
		VectorIntersector.intersect(v(1), v(-2), v(3), v(-3), v(3), v(-3), v(7), v(-5)));
    }

    // Primary start
    @Test
    public void test_tangent_intersection_in_primary_start() {
	assertEquals("Should be equal.", new Pair<>(true, new Pair<>(v(1), v(-2))),
		VectorIntersector.intersect(v(1), v(-2), v(3), v(-3), v(1), v(-2), v(0), v(-2)));
    }

    @Test
    public void test_tangent_not_intersection_in_primary_start() {
	assertEquals("Should be equal.", new Pair<>(false, null),
		VectorIntersector.intersect(v(1), v(-2), v(3), v(-3), v(1), v(-2), v(0), v(-1)));
    }

    @Test
    public void test_tangent_not_intersection_if_collinear_in_primary_start() {
	assertEquals("Should be equal.", new Pair<>(false, null),
		VectorIntersector.intersect(v(1), v(-2), v(3), v(-3), v(-1), v(-1), v(1), v(-2)));
    }

    // Middle
    @Test
    public void test_intersection_in_the_middle() {
	assertEquals("Should be equal.", new Pair<>(true, new Pair<>(v(2), BigDecimal.valueOf(-2.5).setScale(15, RoundingMode.HALF_UP))),
		VectorIntersector.intersect(v(1), v(-2), v(3), v(-3), v(3), v(-1), v(1), v(-4)));
    }

    @Test
    public void test_not_intersection_in_the_middle() {
	assertEquals("Should be equal.", new Pair<>(false, null),
		VectorIntersector.intersect(v(1), v(-2), v(3), v(-3), v(1), v(-3), v(3), v(-1)));
    }

    @Test
    public void test_not_intersection_outside() {
	assertEquals("Should be equal.", new Pair<>(false, null),
		VectorIntersector.intersect(v(1), v(-2), v(3), v(-3), v(4), v(-2), v(3), v(-4)));
    }
}
