package ua.com.fielden.platform.dao;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.hibernate.Hibernate;
import org.hibernate.cfg.Configuration;
import org.hibernate.mapping.Any;
import org.hibernate.mapping.Collection;
import org.hibernate.mapping.Column;
import org.hibernate.mapping.DependantValue;
import org.hibernate.mapping.KeyValue;
import org.hibernate.mapping.List;
import org.hibernate.mapping.ManyToOne;
import org.hibernate.mapping.OneToMany;
import org.hibernate.mapping.OneToOne;
import org.hibernate.mapping.PersistentClass;
import org.hibernate.mapping.Property;
import org.hibernate.mapping.RootClass;
import org.hibernate.mapping.SimpleValue;
import org.hibernate.mapping.SingleTableSubclass;
import org.hibernate.mapping.ToOne;
import org.hibernate.type.AnyType;
import org.hibernate.type.CompositeCustomType;
import org.hibernate.type.CustomType;
import org.hibernate.type.EntityType;
import org.hibernate.type.ManyToOneType;
import org.hibernate.type.NullableType;
import org.hibernate.type.Type;
import org.hibernate.usertype.CompositeUserType;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.equery.ColumnInfoForEntityProp;
import ua.com.fielden.platform.equery.ColumnInfoForPrimProp;
import ua.com.fielden.platform.equery.ColumnInfoForUnionEntityProp;
import ua.com.fielden.platform.equery.ColumnInfo;
import ua.com.fielden.platform.equery.interfaces.IEntityMapper;
import ua.com.fielden.platform.equery.interfaces.IMappingExtractor;
import ua.com.fielden.platform.reflection.PropertyTypeDeterminator;

/**
 * Helper class that retrieves from Hibernate configuration info, which is necessary for implementation of entity query fetching with models.
 *
 * @author nc
 *
 */
public class MappingExtractor implements IMappingExtractor {
    private final Configuration cfg;

    public MappingExtractor(final Configuration cfg) {
	this.cfg = cfg;
    }

    /**
     * Checks whether given property of the entity class is collectional.
     *
     * @param entityClass
     * @param propertyName
     * @return
     */
    public boolean isCollection(final Class<? extends AbstractEntity> entityClass, final String propertyName) {
	return getProperty(entityClass, propertyName).getValue() instanceof Collection;
    }

    /**
     * Checks whether given property of the entity class is mapped as one-to-one association.
     *
     * @param entityClass
     * @param propertyName
     * @return
     */
    public boolean isOneToOne(final Class<? extends AbstractEntity> entityClass, final String propertyName) {
	return getProperty(entityClass, propertyName).getValue() instanceof OneToOne;
    }

    /**
     * Checks whether given property of the entity class is mapped as many-to-one association.
     *
     * @param entityClass
     * @param propertyName
     * @return
     */
    public boolean isManyToOne(final Class<? extends AbstractEntity> entityClass, final String propertyName) {
	if (getProperty(entityClass, propertyName).getValue() instanceof ManyToOne) {
	    final ManyToOne prop = (ManyToOne) getProperty(entityClass, propertyName).getValue();

	    //System.out.println(getProperty(entityClass, propertyName).getMetaAttribute("unique"));
	    return true;
	}
	return false;
    }

    /**
     * Checks whether given property of the entity class is mapped as any association.
     *
     * @param entityClass
     * @param propertyName
     * @return
     */
    public boolean isAny(final Class<? extends AbstractEntity> entityClass, final String propertyName) {
	return getProperty(entityClass, propertyName).getValue() instanceof Any;
    }

    public boolean isSimpleValue(final Class<? extends AbstractEntity> entityClass, final String propertyName) {
	return getProperty(entityClass, propertyName).getValue() instanceof SimpleValue;
    }

    /**
     * Checks whether given property of the entity class is represented by list.
     *
     * @param entityClass
     * @param propertyName
     * @return
     */
    public boolean isList(final Class<? extends AbstractEntity> entityClass, final String propertyName) {
	if (getProperty(entityClass, propertyName).getValue() instanceof List) {
	    return true;
	} else {
	    return false;
	}
    }

    /**
     * Gets the name of the property in the collectional entity, which is a reference to its parent entity (e.g. property "wagon" in class WagonSlot).
     *
     * @param entityClass - parental class
     * @param propertyName - collectional property name
     * @return
     */
    public String getParentPropertyName(final Class<? extends AbstractEntity> entityClass, final String propertyName) {
	final Collection collectionalProperty = (Collection) getProperty(entityClass, propertyName).getValue();
	// e.g. slots in Wagon
	final OneToMany element = (OneToMany) collectionalProperty.getElement();
	final DependantValue key = (DependantValue) collectionalProperty.getKey();
	final Column keyColumn = (Column) key.getColumnIterator().next();
	final PersistentClass parentClass = collectionalProperty.getOwner();

	// e.g. WagonSlot
	final RootClass collectionalPropClass = (RootClass) element.getAssociatedClass();
	for (final Iterator iterator = collectionalPropClass.getPropertyIterator(); iterator.hasNext();) {
	    final Property prop = (Property) iterator.next();
	    if (prop.getValue() instanceof ManyToOne) {
		final ManyToOne propValue = (ManyToOne) prop.getValue();
		final ManyToOneType propType = (ManyToOneType) prop.getType();
		final Column propColumn = (Column) propValue.getColumnIterator().next();
		if (propType.getName().equals(parentClass.getClassName()) && propColumn.getName().equalsIgnoreCase(keyColumn.getName())) {
		    return prop.getNodeName();
		}
	    }
	}

	return null;
    }

    /**
     * Gets the name of the property in the entity, which mapped as one-to-one association that serves as identity with "foreign" generation strategy (e.g. property "key" in the class VehicleTechDetails pointing to a vehicle).
     *
     * @param entityClass
     * @param propertyName
     * @return
     */
    public String getParentPropertyNameForOneToOne(final Class<? extends AbstractEntity> entityClass, final String propertyName) {
	final Class<?> oneToOnePropertyType = PropertyTypeDeterminator.determinePropertyType(entityClass, propertyName);
	final SimpleValue identifier = (SimpleValue) getIdentifier(oneToOnePropertyType);
	if ("foreign".equalsIgnoreCase(identifier.getIdentifierGeneratorStrategy())) {
	    return identifier.getIdentifierGeneratorProperties().getProperty("property");
	}

	return null;
    }

    /**
     * Identifies whether an entity type is a details side of the one-to-one master/details association.
     *
     * For example, in the association Vehicle/VehicleTechDetails, Vehicle is a master side and VehicleTechDetails is a details side.
     *
     * @param entityType
     * @param propertyName
     * @return
     */
    public boolean isOneToOneDetails(final Class<? extends AbstractEntity> entityType, final String currPropName) {
	if (isOneToOne(entityType, currPropName)) {
	    final SimpleValue identifier = (SimpleValue) getIdentifier(entityType);
	    return "foreign".equalsIgnoreCase(identifier.getIdentifierGeneratorStrategy());
	}
	return false;
    }


    /**
     * Confirms that provided entity type is not persisted by Hibernate -- i.e. is synthetic entity type
     * @param entityType
     * @return
     */
    public boolean isNotPersisted(final Class<? extends AbstractEntity> entityType) {
	return getPersistentClass(entityType) == null;
    }


    /**
     * Gets the name of the property of the collectional entity, which is responsible for indexing in case that collection is a list -- otherwise null is returned.
     *
     * @param entityClass
     * @param propertyName
     * @return
     */
    public String getIndexPropertyName(final Class<? extends AbstractEntity> entityClass, final String propertyName) {
	return getIndexPropertyName((List) getProperty(entityClass, propertyName).getValue());
    }

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * Gets hibernate identifier for an entity class.
     *
     * @param entityClass
     * @return
     */
    private KeyValue getIdentifier(final Class entityClass) {
	final PersistentClass persistentClass = getPersistentClass(entityClass);
	if (persistentClass != null && persistentClass.getClassName().equals(entityClass.getName())) {
	    return persistentClass.getIdentifier();
	} else {
	    return null;
	}
    }

    private String getIndexPropertyName(final List collectionalProperty) {
	// e.g. slots in Wagon
	final OneToMany element = (OneToMany) collectionalProperty.getElement();
	final SimpleValue index = (SimpleValue) collectionalProperty.getIndex();
	final Column indexColumn = (Column) index.getColumnIterator().next();

	//final PersistentClass parentClass = collectionalProperty.getOwner();

	// e.g. WagonSlot
	final RootClass collectionalPropClass = (RootClass) element.getAssociatedClass();
	for (final Iterator<Property> iterator = collectionalPropClass.getPropertyIterator(); iterator.hasNext();) {
	    final Property prop = iterator.next();
	    if (prop.getValue() instanceof SimpleValue) {
		final SimpleValue propValue = (SimpleValue) prop.getValue();
		final Column propColumn = (Column) propValue.getColumnIterator().next();
		if (propColumn.getName().equalsIgnoreCase(indexColumn.getName())) {
		    return prop.getNodeName();
		}
	    }
	}

	return null;
    }

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public Map<String, ColumnInfo> getColumns(final Class<? extends AbstractEntity> entityType, final IEntityMapper parent) {
	final Map<String, ColumnInfo> result = new HashMap<String, ColumnInfo>();

	final PersistentClass persistentClass = getPersistentClass(entityType);
	if (persistentClass == null) {
	    throw new RuntimeException("Couldn't determine hibernate persistent class for entity type: " + entityType);
	    // TODO implement support for polymorphic entities
	} else {
	    result.putAll(getColumnsForIdentifier(persistentClass, parent));

	    result.putAll(getProps(persistentClass, parent));

	    if (persistentClass instanceof SingleTableSubclass) {
		result.putAll(getProps(((SingleTableSubclass) persistentClass).getSuperclass(), parent));
	    }
	}

	return result;
    }

    /**
     * Gets hibernate property instance by its name and its owner entity class.
     *
     * @param entityClass
     * @param propertyName
     * @return
     */
    private Property getProperty(final Class<? extends AbstractEntity> entityClass, final String propertyName) {
	final PersistentClass persistentClass = getPersistentClass(entityClass);
	if (persistentClass != null ) {
	    return persistentClass.getProperty(propertyName);
	} else {
	    return null;
	}
    }

    private Map<String, Class> getPolymorphicTypes(final Any any) {
	final Map<String, Class> result = new HashMap<String, Class>();
	try {
	    for (final Iterator iterator = any.getMetaValues().entrySet().iterator(); iterator.hasNext();) {
		final Map.Entry<String, String> entry = (Map.Entry<String, String>) iterator.next();
		result.put(entry.getKey(), Class.forName(entry.getValue()));
	    }
	} catch (final ClassNotFoundException e) {
	    throw new RuntimeException(e);
	}
	return result;
    }

    private Map<String, ColumnInfo> getColumnsForIdentifier(final PersistentClass persistentClass, final IEntityMapper parent) {
	final Map<String, ColumnInfo> result = new HashMap<String, ColumnInfo>();
	final Property identifierProperty = persistentClass.getIdentifierProperty();
	final String identifierColumn = getIdentifierColumnName(persistentClass);
	result.put(identifierProperty.getName(), new ColumnInfoForPrimProp(identifierColumn, identifierProperty.getType().getClass().getName(), parent));

	return result;
    }

    private Map<String, ColumnInfo> getColumnsForOneToOneBasedOnKey(final PersistentClass persistentClass, final IEntityMapper parent) {
	final Map<String, ColumnInfo> result = new HashMap<String, ColumnInfo>();
	final Property identifierProperty = persistentClass.getIdentifierProperty();

	final String identifierColumn = getIdentifierColumnName(persistentClass);

	if ("foreign".equalsIgnoreCase(getIdentifierGeneratorStrategy(persistentClass))) {
	    final String propName = getIdentifierGeneratorProperty(persistentClass, "property");
	    final Property one2one = persistentClass.getProperty(propName);
	    result.put(propName,  new ColumnInfoForEntityProp(identifierColumn, identifierProperty.getType().getClass().getName(), ((ToOne) one2one.getValue()).getReferencedEntityName(), parent));
	}

	return result;

    }

    private String getIdentifierColumnName(final PersistentClass persistentClass) {
	final Iterator<Column> columnsIter = ((SimpleValue) persistentClass.getIdentifier()).getColumnIterator();
	final String result = columnsIter.next().getName();
	if (columnsIter.hasNext()) {
	    throw new RuntimeException("It is quite unexpected for identifier to consists of multiple columns!");
	} else {
	    return result;
	}
    }

    private String getIdentifierGeneratorStrategy(final PersistentClass persistentClass) {
	return ((SimpleValue) persistentClass.getIdentifier()).getIdentifierGeneratorStrategy();
    }

    private String getIdentifierGeneratorProperty(final PersistentClass persistentClass, final String propertyName) {
	return (String) ((SimpleValue) persistentClass.getIdentifier()).getIdentifierGeneratorProperties().get(propertyName);
    }

    private Map<String, ColumnInfo> getColumnInfoForProperty(final Property property, final PersistentClass persistentClass, final IEntityMapper parent) {
	final Map<String, ColumnInfo> result = new HashMap<String, ColumnInfo>();
	if (property.getValue() instanceof SimpleValue) { // e.g. not collection
	    final Type hibernateType = property.getValue().getType();
	    final java.util.List<String> columns = new ArrayList<String>();
	    final Iterator<Column> iter = property.getColumnIterator();
	    while (iter.hasNext()) {
		final Column column = iter.next();
		columns.add(column.getName());
	    }

	    // TODO need to branch based on HibernateType and not just columns.size()

	    if (columns.size() == 0 && !result.containsKey(property.getName())) {
//		if (OneToOneType.class.isAssignableFrom(hibernateType.getClass())) {
//		}
		final String propName = getIdentifierGeneratorProperty(persistentClass, "property");
		if (property.getName().equalsIgnoreCase(propName)) {
		    result.putAll(getColumnsForOneToOneBasedOnKey(persistentClass, parent));
		} else {
		    throw new RuntimeException("Property " + property.getName() + " of " + persistentClass.getClassName() + " has no columns!");
		}
	    } else if (columns.size() == 1 && !CompositeCustomType.class.isAssignableFrom(hibernateType.getClass())) {
		if (NullableType.class.isAssignableFrom(hibernateType.getClass())) {
		    result.put(property.getName(), new ColumnInfoForPrimProp(columns.get(0), hibernateType.getClass().getName(), parent));
		} else if (CustomType.class.isAssignableFrom(hibernateType.getClass())) {
		    result.put(property.getName(), new ColumnInfoForPrimProp(columns.get(0), ((CustomType) hibernateType).getName(), parent));
		} else if (CompositeCustomType.class.isAssignableFrom(hibernateType.getClass())) {
		    result.put(property.getName(), new ColumnInfoForPrimProp(columns.get(0), ((CompositeCustomType) hibernateType).getName(), parent));
		} else if (EntityType.class.isAssignableFrom(hibernateType.getClass())) {
		    result.put(property.getName(), new ColumnInfoForEntityProp(columns.get(0), property.getPersistentClass().getIdentifierProperty().getType().getClass().getName(),
			    ((ToOne) property.getValue()).getReferencedEntityName(), parent));
		} else {
		    throw new RuntimeException("Unexpected hibernate type: " + hibernateType.getClass() + " for property: " + property);
		}
	    } else {
		if (property.getValue() instanceof Any) {
		    final Any any = (Any) property.getValue();
		    final AnyType anyType = ((AnyType) any.getType());
		    if (columns.size() == 2) {
			final ColumnInfoForUnionEntityProp classColumnInfo = new ColumnInfoForUnionEntityProp(columns.get(0), Hibernate.STRING.getClass().getName(), getPolymorphicTypes(any), parent);
			//classColumnInfo.getPolymorphicTypes().putAll(getPolymorphicTypes(any));
			result.put(property.getName() + ".class", classColumnInfo);
			result.put(property.getName() + "", new ColumnInfoForPrimProp(columns.get(1), anyType.getSubtypes()[1].getClass().getName(), parent));
		    }
		} else {
		    // final Class propertyType = property.getValue().getType().getReturnedClass();
		    Class propertyType = null;
		    try {
			propertyType = Class.forName(property.getValue().getType().getName());
		    } catch (final Exception e) {
			e.printStackTrace();
		    }
		    if (CompositeUserType.class.isAssignableFrom(propertyType)) {
			try {
			    final CompositeUserType propertyCompUserType = ((CompositeUserType) propertyType.newInstance());
			    final String[] propNames = propertyCompUserType.getPropertyNames();
			    final Type[] propTypes = propertyCompUserType.getPropertyTypes();
			    int i = 0;
			    for (final String columnName : columns) {
				result.put(property.getName() + "." + propNames[i], new ColumnInfoForEntityProp(columnName, propTypes[i].getClass().getName(), propertyType.getName(), parent));
				i = i + 1;
			    }
			} catch (final Exception e) {
			    e.printStackTrace();
			}
		    }
		}

	    }
	}
	return result;
    }

    private Map<String, ColumnInfo> getProps(final PersistentClass persistentClass, final IEntityMapper parent) {
	final Map<String, ColumnInfo> result = new HashMap<String, ColumnInfo>();
	final Iterator<Property> propertyIter = persistentClass.getPropertyIterator();
	while (propertyIter.hasNext()) {
	    final Property property = propertyIter.next();
	    result.putAll(getColumnInfoForProperty(property, persistentClass, parent));
	}

	return result;
    }

    private PersistentClass getPersistentClass(final Class<? extends AbstractEntity> entityType) {
	final Iterator<?> it = cfg.getClassMappings();
	while (it.hasNext()) {
	    final PersistentClass pc = (PersistentClass) it.next();
	    if (pc.getClassName().equals(entityType.getName())) {
		return pc;
	    }
	}
	return null;
    }

    @Override
    public String getTableClause(final Class<? extends AbstractEntity> entityType) {
	final PersistentClass persistentClass = getPersistentClass(entityType);
	if (persistentClass == null) {
	    return null;
	} else {
	    if (persistentClass instanceof SingleTableSubclass) {
		final String discValue = ((SingleTableSubclass) persistentClass).getDiscriminatorValue();
		final RootClass superClass = ((SingleTableSubclass) persistentClass).getRootClass();
		final String tableName = superClass.getDiscriminator().getTable().getName();
		final Column column = (Column) superClass.getDiscriminator().getColumnIterator().next();
		final String discColumnName = column.getName();
		return "(SELECT * FROM " + tableName + " WHERE " + discColumnName + " = '" + discValue + "')";
	    }

	    if (persistentClass.getWhere() == null) {
		return persistentClass.getTable().getName();
	    } else {
		return "(SELECT * FROM " + persistentClass.getTable().getName() + " WHERE " + persistentClass.getWhere() + ")";
	    }
	}

	// TODO implement support for polymorphic entities: e.g. RotableLocation
	// 1. need to determine all mapped classes that are descendants of RotableLocation
	// 2. need to construct superset of all mapped properties of all these classes
	// 2.a only properties (from hibernate mappings) that all classes have in common may be used in eQuery (SELECT, WHERE, GROUPBY, ORDERBY)
	// 3. for each class need to build sql statement retrieving all properties (superset), having null values for properties not-relevant for a particular class and class itself (full class name)
	// 3.a. information from 3. will allow to instantiate entity properties of different types without additional fetching via fetch models (performance optimisation)
	// 3.b during instantiation column with class value will serve for choosing right class type for instantiation.
	// 4. return all these 'SELECT' statements UNIONED ALL and enclosed into braces

	// this method getTableClause may be involved in two cases: first -- LEFT JOINed for polymorphic property (e.g rotableLocation in Wheelset entity), second -- as a stand-alone request to entity (SELECT * FROM RotableLocation ...)

	// This sql works: select * from (select ol1.* from ORG_LEVEL2 ol2 left join ORG_LEVEL1 ol1 on ol1._ID = ol2.ID_ORG_LEVEL1) as AB group by _ID

	// while knowing that we need wheelset that are in workshops (i.e. rotableLocation type is Workshop), somehow only WORKSHOP table should be left-joined instead of UNIONED-ALL bunch of all tables
    }
}
