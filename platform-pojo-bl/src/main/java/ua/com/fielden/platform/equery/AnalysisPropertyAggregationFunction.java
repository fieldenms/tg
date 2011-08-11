package ua.com.fielden.platform.equery;

import java.math.BigInteger;

/**
 * Aggregation functions for Analysis reports.
 * 
 * @author oleh
 * 
 */
public enum AnalysisPropertyAggregationFunction implements IPropertyAggregationFunction {
    SUM("SUM", "Sum"), //
    COUNT("COUNT", "Count") {
	@Override
	public boolean isDifferentValueAxis() {
	    return true;
	}

	@Override
	public Class<?> getReturnedType(final Class<?> propertyClass) {
	    return BigInteger.class;
	}
    },
    DISTINCT_COUNT("COUNT DISTINCT", "Count Distinct") {

	@Override
	public String createQueryString(final String key) {
	    return "COUNT(DISTINCT [" + key + "])";
	}

	@Override
	public boolean isDifferentValueAxis() {
	    return true;
	}

	@Override
	public Class<?> getReturnedType(final Class<?> propertyClass) {
	    return BigInteger.class;
	}
    }, //
    AVG("AVG", "Average"), //
    MIN("MIN", "Minimum"), //
    MAX("MAX", "Maximum");

    private final String name;
    private final String key;

    private AnalysisPropertyAggregationFunction(final String key, final String name) {
	this.key = key;
	this.name = name;
    }

    public String getKey() {
	return key;
    }

    @Override
    public String toString() {
	return name;
    }

    public String createQueryString(final String key) {
	return name() + "([" + key + "])";
    }

    public boolean isDifferentValueAxis() {
	return false;
    }

    @Override
    public Class<?> getReturnedType(final Class<?> propertyClass) {
	return propertyClass;
    }

}
