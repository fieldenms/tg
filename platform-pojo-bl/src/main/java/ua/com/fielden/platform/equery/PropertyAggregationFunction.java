package ua.com.fielden.platform.equery;

import java.math.BigInteger;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.types.Money;

public enum PropertyAggregationFunction implements IPropertyAggregationFunction {

    NONE("", AbstractEntity.class, Date.class, Number.class, Money.class, String.class) {
	@Override
	public String createQueryString(final String arg0) {
	    return "";
	}

	@Override
	public Class getReturnedType(final Class propertyClass) {
	    return null;
	}
    }, //
    SUM("Sum", Number.class, Money.class), //
    DISTINCT_COUNT("Distinct count", AbstractEntity.class, String.class) {
	@Override
	public String createQueryString(final String key) {
	    return "COUNT(DISTINCT [" + key + "])";
	}

	@Override
	public Class getReturnedType(final Class propertyClass) {
	    return BigInteger.class;
	}
    }, //
    DISTINCT_COUNT_DAY("Distinct count (Day)", Date.class) {
	@Override
	public String createQueryString(final String arg0) {
	    return "COUNT(DISTINCT YEAR([" + arg0 + "]) * 10000 + MONTH([" + arg0 + "]) * 100 + DAY([" + arg0 + "]))";
	}

	@Override
	public Class getReturnedType(final Class propertyClass) {
	    return BigInteger.class;
	}
    }, //
    DISTINCT_COUNT_MONTH("Distinct count (Month)", Date.class) {
	@Override
	public String createQueryString(final String arg0) {
	    return "COUNT(DISTINCT YEAR([" + arg0 + "]) * 100 + MONTH([" + arg0 + "]))";
	}

	@Override
	public Class getReturnedType(final Class propertyClass) {
	    return BigInteger.class;
	}
    }, //
    DISTINCT_COUNT_YEAR("Distinct count (Year)", Date.class) {
	@Override
	public String createQueryString(final String arg0) {
	    return "COUNT(DISTINCT YEAR([" + arg0 + "]))";
	}

	@Override
	public Class getReturnedType(final Class propertyClass) {
	    return BigInteger.class;
	}
    }, //
    AVG("Average", Number.class, Money.class), //
    COUNT("Count", AbstractEntity.class) {
	@Override
	public Class getReturnedType(final Class propertyClass) {
	    return BigInteger.class;
	}
    }, //
    MIN("Minimum", Number.class, Money.class, Date.class, String.class), //
    MAX("Maximum", Number.class, Money.class, Date.class, String.class);

    private final String name;
    private static Map<Class, Vector<PropertyAggregationFunction>> classFunctionAssociation;

    private PropertyAggregationFunction(final String name, final Class<?>... types) {
	this.name = name;

	for (final Class<?> type : types) {
	    addPropertyFunctionToType(type);
	}
    }

    private void addPropertyFunctionToType(final Class<?> type) {
	if (classFunctionAssociation == null) {
	    classFunctionAssociation = new HashMap<Class, Vector<PropertyAggregationFunction>>();
	}
	Vector<PropertyAggregationFunction> functions = classFunctionAssociation.get(type);
	if (functions == null) {
	    functions = new Vector<PropertyAggregationFunction>();
	}
	if (!functions.contains(this)) {
	    functions.add(this);
	}
	classFunctionAssociation.put(type, functions);
    }

    public static Vector<PropertyAggregationFunction> getFunctionForType(final Class<?> type) {
	Vector<PropertyAggregationFunction> functions = null;
	if (type != null && AbstractEntity.class.isAssignableFrom(type)) {
	    functions = classFunctionAssociation.get(AbstractEntity.class);
	} else if (type != null && Number.class.isAssignableFrom(type)) {
	    functions = classFunctionAssociation.get(Number.class);
	} else {
	    functions = classFunctionAssociation.get(type);
	}
	return functions == null ? new Vector<PropertyAggregationFunction>() : functions;
    }

    @Override
    public String toString() {
	return name;
    }

    @Override
    public String createQueryString(final String key) {
	return name() + "([" + key + "])";
    }

    @Override
    public Class<?> getReturnedType(final Class<?> propertyClass) {
	return propertyClass;
    }
}
