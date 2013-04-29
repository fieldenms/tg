package ua.com.fielden.platform.algorithm.vectormath;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * A simple vector representation with known math operations
 * (like add, subtract, cross product etc) in Cartesian coordinate system.
 *
 * @author TG Team
 *
 * @see http://stackoverflow.com/questions/563198/how-do-you-detect-where-two-line-segments-intersect
 * @see http://www.mathsisfun.com/algebra/vectors.html
 *
 */
public class Vector {
    private final BigDecimal x, y;

    private static BigDecimal sc(final BigDecimal num) {
	return num.setScale(15, RoundingMode.HALF_UP);
    }

    public Vector(final BigDecimal x, final BigDecimal y) {
	this.x = sc(x);
	this.y = sc(y);
    }

    public BigDecimal getX() {
        return x;
    }

    public BigDecimal getY() {
        return y;
    }

    public Vector add(final Vector that) {
	return new Vector(this.x.add(that.x), this.y.add(that.y));
    }

    public Vector negate() {
	return new Vector(this.x.negate(), this.y.negate());
    }

    public Vector sub(final Vector that) {
	return this.add(that.negate());
    }

    public Vector mult(final BigDecimal scalar) {
	return new Vector(this.x.multiply(scalar), this.y.multiply(scalar));
    }

    /**
     * A cross product operation.
     * <p>
     * <code>this</code> is a primary vector (right-handed forefinger) and
     * <code>that</code> is a secondary vector (right-handed middle finger)
     * and a result vector (right-handed thumb).
     * <p>
     * If result is equal zero -- two vectors are parallel (or even collinear).
     * If result is lower than zero then angle between vectors is in (180; 360) range.
     * If result is upper than zero then angle between vectors is in (0; 180) range.
     *
     * @param that
     * @return
     */
    public BigDecimal crossProduct(final Vector that) {
	return sc(
		(this.x.multiply(that.y)).
		subtract(this.y.multiply(that.x))
		);
    }

    @Override
    public int hashCode() {
	final int prime = 31;
	int result = 1;
	result = prime * result + ((x == null) ? 0 : x.hashCode());
	result = prime * result + ((y == null) ? 0 : y.hashCode());
	return result;
    }

    @Override
    public boolean equals(final Object obj) {
	if (this == obj)
	    return true;
	if (obj == null)
	    return false;
	if (getClass() != obj.getClass())
	    return false;
	final Vector other = (Vector) obj;
	if (x == null) {
	    if (other.x != null)
		return false;
	} else if (!x.equals(other.x))
	    return false;
	if (y == null) {
	    if (other.y != null)
		return false;
	} else if (!y.equals(other.y))
	    return false;
	return true;
    }
}
