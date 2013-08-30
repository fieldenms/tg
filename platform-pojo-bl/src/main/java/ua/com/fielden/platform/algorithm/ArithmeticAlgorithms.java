package ua.com.fielden.platform.algorithm;

import java.util.Arrays;

/**
 * This is a class providing a number of trivial arithmetic algorithms as static methods.
 *
 * @author TG Team
 *
 */
public class ArithmeticAlgorithms {
    private ArithmeticAlgorithms() {
    }

    /**
     * Returns an index of the minimum element in the passed in array.
     *
     * @param values
     * @return
     */
    public static Integer minIndex(final Integer... values) {
	if (values == null) {
	    throw new IllegalArgumentException("Null values passed instead of an array with values.");
	}
	if (values.length == 0) {
	    throw new IllegalArgumentException("There should be at least one value.");
	}

	Integer minIndex = 0;
	Integer min = values[minIndex];
	for (int index = 1; index < values.length; index++) {
	    if (values[index] == null) {
		throw new IllegalArgumentException("The passed in array should not contain null values.");
	    }
	    if (min > values[index]) {
		minIndex = index;
		min = values[minIndex];
	    }
	}

	return minIndex;
    }

    /**
     * Returns true if all values in the passed in array are equal.
     *
     * @param values
     * @return
     */
    public static boolean areValuesEqual(final Object... values) {
	if (values == null) {
	    throw new IllegalArgumentException("Null values passed instead of an array with values.");
	}
	if (values.length == 0) {
	    throw new IllegalArgumentException("There should be at least one value.");
	}

	final Object value = values[0];
	for (int index = 1; index < values.length; index++) {
	    if (value == null && values[index] != null) {
		return false;
	    } else if (value != null && !value.equals(values[index])) {
		return false;
	    }

	}
	return true;
    }

    /**
     * Calculates Least Common Multiplier for a given number of integers.
     *
     * @param values
     * @return
     */
    public static Integer lcm(final Integer... values) {
	if (values == null) {
	    throw new IllegalArgumentException("Null values passed instead of an array with values.");
	}
	if (values.length == 0) {
	    throw new IllegalArgumentException("There should be at least one value.");
	}

	// the algorithm body
	final Integer[] current = Arrays.copyOf(values, values.length);
	while (!areValuesEqual((Object[])current)) {
	    final Integer minIndex = minIndex(current);
	    current[minIndex] += values[minIndex];
	}
	return current[0];

    }
}
