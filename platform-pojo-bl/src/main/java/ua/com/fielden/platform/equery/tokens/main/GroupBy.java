package ua.com.fielden.platform.equery.tokens.main;

import java.util.ArrayList;
import java.util.List;

import ua.com.fielden.platform.equery.RootEntityMapper;
import ua.com.fielden.platform.equery.interfaces.IClon;
import ua.com.fielden.platform.equery.tokens.properties.GroupByProperty;

public final class GroupBy implements /*IQueryToken,*/ IClon<GroupBy> {
    private final ArrayList<GroupByProperty> properties = new ArrayList<GroupByProperty>();

    public GroupBy() {
    }

    private GroupBy(final List<GroupByProperty> properties) {
	this.properties.addAll(properties);
    }

    public List<GroupByProperty> getProperties() {
	return properties;
    }

    //    public void add(final String ... addedProperties) {
    //	for (final String property : addedProperties) {
    //	    if (property != null) {
    //		this.properties.add(new GroupByProperty("[" + property + "]"));
    //	    }
    //	}
    //    }

    @Override
    public GroupBy clon() {
	final List<GroupByProperty> clonedProperties = new ArrayList<GroupByProperty>();
	for (final GroupByProperty groupByProperty : properties) {
	    clonedProperties.add(groupByProperty.clon());
	}
	return new GroupBy(clonedProperties);
    }

    public List<String> getPropertiesAsSql(final RootEntityMapper alias) {
	final List<String> result = new ArrayList<String>();
	for (final GroupByProperty property : properties) {
	    result.add(property.getSql(alias));
	}
	return result;
    }

//    @Override
//    public String getSql(final RootEntityMapper alias) {
//	final StringBuffer sb = new StringBuffer();
//	if (properties.size() > 0) {
//	    sb.append("\n   GROUP BY ");
//
//	    for (final Iterator<GroupByProperty> iterator = properties.iterator(); iterator.hasNext();) {
//		final GroupByProperty property = iterator.next();
//		sb.append(property.getSql(alias));
//		if (iterator.hasNext()) {
//		    sb.append(", ");
//		}
//	    }
//	}
//	return sb.toString();
//    }
}
