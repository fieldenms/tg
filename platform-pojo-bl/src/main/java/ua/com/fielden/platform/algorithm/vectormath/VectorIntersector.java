package ua.com.fielden.platform.algorithm.vectormath;

import java.math.BigDecimal;
import java.math.RoundingMode;

import ua.com.fielden.platform.utils.Pair;

public class VectorIntersector {
    /**
     * Intersects two vectors by a right-handed rule.
     * 
     * @return "true with intersected point" (intersected) or "false with null" (not intersected).
     * 
     * @see http://stackoverflow.com/questions/563198/how-do-you-detect-where-two-line-segments-intersect
     * @see http://www.mathsisfun.com/algebra/vectors.html
     */
    public static Pair<Boolean, Pair<BigDecimal, BigDecimal>> intersect(final BigDecimal x0, final BigDecimal y0, final BigDecimal X0, final BigDecimal Y0, final BigDecimal x, final BigDecimal y, final BigDecimal X, final BigDecimal Y) {
        final Vector p = new Vector(x0, y0);
        final Vector q = new Vector(x, y);
        final Vector pEnd = new Vector(X0, Y0);
        final Vector qEnd = new Vector(X, Y);
        final Vector r = pEnd.sub(p);
        final Vector s = qEnd.sub(q);

        final BigDecimal crossRandSProduct = r.crossProduct(s);
        if (crossRandSProduct.compareTo(BigDecimal.ZERO) <= 0) { // parallel or bad intersection angle > 180
            return new Pair<>(false, null);
        }
        final Vector qSubP = q.sub(p);

        final BigDecimal t = qSubP.crossProduct(s).divide(crossRandSProduct, 15, RoundingMode.HALF_UP);
        final BigDecimal u = qSubP.crossProduct(r).divide(crossRandSProduct, 15, RoundingMode.HALF_UP);
        if (0.0 <= t.doubleValue() && t.doubleValue() <= 1.0 && 0.0 <= u.doubleValue() && u.doubleValue() <= 1.0) {
            final Vector intersectedV = p.add(r.mult(t)); // or "q + u*s"
            return new Pair<>(true, new Pair<>(intersectedV.getX(), intersectedV.getY()));
        } else { // intersects, but outside of both vector segments (not interested in this case)
            return new Pair<>(false, null);
        }
    }

}
