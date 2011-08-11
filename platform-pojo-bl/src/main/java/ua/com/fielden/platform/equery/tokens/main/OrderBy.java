package ua.com.fielden.platform.equery.tokens.main;

import java.util.ArrayList;
import java.util.List;

import ua.com.fielden.platform.equery.Ordering;
import ua.com.fielden.platform.equery.RootEntityMapper;
import ua.com.fielden.platform.equery.interfaces.IClon;
import ua.com.fielden.platform.equery.tokens.properties.OrderByProperty;

public final class OrderBy implements /*IQueryToken,*/ IClon<OrderBy> {
    private final ArrayList<OrderByProperty> properties = new ArrayList<OrderByProperty>();

    public OrderBy() {
    }

    private OrderBy(final List<OrderByProperty> properties) {
	this.properties.addAll(properties);
    }

    public List<OrderByProperty> getProperties() {
	return properties;
    }

    public void add(final String... otherProperties) {
	for (final String property : otherProperties) {
	    if (property != null) {
		final String[] propParts = property.split(" ");
		if (propParts.length == 1) {
		    properties.add(new OrderByProperty("[" + property + "]", Ordering.ASC));
		} else {
		    if ("desc".equalsIgnoreCase(propParts[1])) {
			properties.add(new OrderByProperty("[" + propParts[0] + "]", Ordering.DESC));
		    } else {
			properties.add(new OrderByProperty("[" + propParts[0] + "]", Ordering.ASC));
		    }
		}
	    }
	}
    }

//    @Override
//    public String getSql(final RootEntityMapper alias) {
//	final StringBuffer sb = new StringBuffer();
//	if (properties.size() > 0) {
//	    sb.append("\n   ORDER BY ");
//
//	    for (final Iterator<OrderByProperty> iterator = properties.iterator(); iterator.hasNext();) {
//		final OrderByProperty property = iterator.next();
//		sb.append(property.getSql(alias));
//		if (iterator.hasNext()) {
//		    sb.append(", ");
//		}
//	    }
//	}
//	return sb.toString();
//    }

    public List<String> getPropertiesAsSql(final RootEntityMapper alias) {
	final List<String> result = new ArrayList<String>();
	for (final OrderByProperty property : properties) {
	    result.add(property.getSql(alias));
	}
	return result;
    }

    @Override
    public OrderBy clon() {
	final List<OrderByProperty> clonedProperties = new ArrayList<OrderByProperty>();
	for (final OrderByProperty orderByProperty : properties) {
	    clonedProperties.add(orderByProperty.clon());
	}
	return new OrderBy(clonedProperties);
    }
}
