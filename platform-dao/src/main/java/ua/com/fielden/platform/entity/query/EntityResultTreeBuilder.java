package ua.com.fielden.platform.entity.query;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ua.com.fielden.platform.dao.MappingsGenerator;
import ua.com.fielden.platform.dao.PropertyPersistenceInfo;
import ua.com.fielden.platform.reflection.PropertyTypeDeterminator;
import ua.com.fielden.platform.utils.EntityUtils;

final class EntityResultTreeBuilder {
    private MappingsGenerator mappingsGenerator;
    private ResultIndex index = new ResultIndex();

    protected EntityResultTreeBuilder(final MappingsGenerator mappingsGenerator) {
	this.mappingsGenerator = mappingsGenerator;
    }

    /*DONE*/
    protected EntityTree buildTree(final Class resultType, final Collection<PropertyPersistenceInfo> properties) throws Exception {
	final EntityTree result = new EntityTree(resultType);

	final List<PropertyPersistenceInfo> singleProps = getFirstLevelSingleProps(properties);
	final Map<String, Collection<PropertyPersistenceInfo>> compositeProps = getFirstLevelCompositeProps(properties);

	for (final PropertyPersistenceInfo propInfo : singleProps) {
	    result.getSingles().put(new PropertyPersistenceInfo.Builder(propInfo.getName(), propInfo.getJavaType()). //
		    column(propInfo.getColumn()). //
		    hibType(mappingsGenerator.determinePropertyHibType(resultType, propInfo.getName()) != null ? mappingsGenerator.determinePropertyHibType(resultType, propInfo.getName()) :
			(mappingsGenerator.determinePropertyHibUserType(resultType, propInfo.getName()) != null ? mappingsGenerator.determinePropertyHibUserType(resultType, propInfo.getName()) : null)).
		    build(), index.getNext());
	    //result.getSingles().put(new PropColumn(propInfo.getName(), propInfo.getColumn()/*getSqlAlias()*/, mappingsGenerator.determinePropertyHibType(resultType, propInfo.getName()), mappingsGenerator.determinePropertyHibUserType(resultType, propInfo.getName())), index.getNext());
	}

	for (final Map.Entry<String, Collection<PropertyPersistenceInfo>> propEntry : compositeProps.entrySet()) {
	    final String subtreePropName = propEntry.getKey();
	    final Class subtreeType = determinePropertyType(resultType, subtreePropName);
	    final Class subtreeHibType = mappingsGenerator.determinePropertyHibCompositeUserType(resultType, subtreePropName);
	    result.getComposites().put(subtreePropName, buildTree((subtreeHibType == null ? subtreeType : subtreeHibType), propEntry.getValue()));
	}

	return result;
    }

    /*DONE*/
    /**
     * Extract explicit and implicit property groups; Explicit property groups are provided with property type.
     *
     * @param allProps
     * @return
     */
    private List<PropertyPersistenceInfo> getFirstLevelSingleProps(final Collection<PropertyPersistenceInfo> allProps) {
	final List<PropertyPersistenceInfo> result = new ArrayList<PropertyPersistenceInfo>();

	for (final PropertyPersistenceInfo prop : allProps) {
	    if ((prop.getType() == null || !EntityUtils.isPersistedEntityType(prop.getJavaType())) && !prop.getName().contains(".")) {
		result.add(prop);
	    }
	}

	return result;
    }

    /*DONE*/
    private Map<String, Collection<PropertyPersistenceInfo>> getFirstLevelCompositeProps(final Collection<PropertyPersistenceInfo> allProps) {
	final Map<String, Collection<PropertyPersistenceInfo>> result = new HashMap<String, Collection<PropertyPersistenceInfo>>();

	for (final PropertyPersistenceInfo prop : allProps) {
	    if (prop.getName().contains(".")) {
		final int firstDotIndex = prop.getName().indexOf(".");
		final String group = prop.getName().substring(0, firstDotIndex);
		if (!result.containsKey(group)) {
		    result.put(group, new ArrayList<PropertyPersistenceInfo>());
		}
		result.get(group).add(new PropertyPersistenceInfo.Builder(prop.getName().substring(firstDotIndex + 1), prop.getJavaType()). //
			column(prop.getColumn()). //
			hibType(prop.getHibType()). //
			build());
	    } else if (prop.getType() != null && EntityUtils.isPersistedEntityType(prop.getJavaType())) {
		final List<PropertyPersistenceInfo> subprops = new ArrayList<PropertyPersistenceInfo>();
		subprops.add(new PropertyPersistenceInfo.Builder("id", Long.class). //
			column(prop.getColumn()). //
			hibType(prop.getHibType()). //
			build());
		result.put(prop.getName(), subprops);
	    }
	}

	return result;
    }

    /*DONE*/
    private Class determinePropertyType(final Class<?> parentType, final String propName) {
	if (EntityAggregates.class.equals(parentType)) {
	    return null;
	} else {
	    return PropertyTypeDeterminator.determinePropertyType(parentType, propName);
	}
    }

    /*DONE*/
    private static class ResultIndex {
	private int count = -1;

	int getNext() {
	    count = count + 1;
	    return count;
	}
    }
}
