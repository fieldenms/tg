package ua.com.fielden.platform.migration;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Currency;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.hibernate.SessionFactory;

import ua.com.fielden.platform.dao.DynamicEntityDao;
import ua.com.fielden.platform.dao.IEntityDao;
import ua.com.fielden.platform.dao.annotations.Transactional;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.equery.fetchAll;
import ua.com.fielden.platform.error.Result;
import ua.com.fielden.platform.migration.dao.MigrationErrorDao;
import ua.com.fielden.platform.migration.dao.MigrationHistoryDao;
import ua.com.fielden.platform.reflection.Finder;
import ua.com.fielden.platform.reflection.PropertyTypeDeterminator;
import ua.com.fielden.platform.types.Money;
import ua.com.fielden.platform.utils.Pair;

import com.google.inject.Inject;

/**
 * A base class for all concrete retrievers.
 *
 * @author TG Team
 *
 * @param <T>
 */
public abstract class AbstractRetriever<T extends AbstractEntity> implements IRetriever<T> {
    private final Logger logger = Logger.getLogger(this.getClass());
    protected final IEntityDao<T> dao;
    @Inject private DynamicEntityDao dynamicDao;

    private Map<String, IPropertyValueRetriever> pvr = new HashMap<String, IPropertyValueRetriever>();

    protected AbstractRetriever(final IEntityDao<T> dao) {
	this.dao = dao;
    }

    public Class<T> type() {
	return dao.getEntityType();
    }

    private final MigrationError error(final EntityFactory factory, final String propName, final String propType, final Object propValue) {
	final MigrationError error = factory.newEntity(MigrationError.class);
	error.setErrorPropName(propName);
	error.setErrorPropType(propType);
	error.setErrorPropValue(propValue == null ? null : propValue.toString());
	return error;
    }

    /**
     * Creates entity instance based of the current row of the result set of data retrieved from legacy db.
     * @param rs
     * @param sessionFactory
     * @param retrievers
     * @return
     * @throws Exception
     */
    @Transactional
    private final SaveResult produceInstance(final ResultSet rs, final Collection<Container> containers, final List<String> keyPropNames, final EntityFactory factory)
	    throws Exception {
	final Map<String, Object> propAdoptedValues = new HashMap<String, Object>();

	for (final Container container : containers) {
	    propAdoptedValues.put(container.propName, container.getPropValue(this, rs, dynamicDao, factory));
	}

	T instance = dao.findByKeyAndFetch(new fetchAll(type()), getKeyPropsValues(keyPropNames, propAdoptedValues));
	Long originalVersion = null;
	boolean inserted = true;

	if (instance != null) {
	    inserted = false;
	    originalVersion = instance.getVersion();
	    for (final Map.Entry<String, Object> propValueEntry : propAdoptedValues.entrySet()) {
		if (!keyPropNames.contains(propValueEntry.getKey())) {
		    try {
			instance.set(propValueEntry.getKey(), propValueEntry.getValue());
		    } catch (final Exception e) {
		    }
		}
	    }
	} else if (updateOnly()) {
	    throw new IllegalStateException("There is no such entity found for update!");
	} else {
	    instance = factory.newEntity(type(), null);
	    for (final Map.Entry<String, Object> propValueEntry : propAdoptedValues.entrySet()) {
		try {
		    instance.set(propValueEntry.getKey(), propValueEntry.getValue());
		} catch (final Exception e) {
		    //ignoring exception here because the order of assignment is arbitrary and may result into errors,
		    //but once everything is assigned the revalidation will clear out any previously occurring set errors.
		}
	    }
	}

	final Result validation = instance.isValid();
	if (!validation.isSuccessful()) {
	    throw validation.getEx();
	}

	int exceptionCount = 0;
	boolean success = false;
	while (exceptionCount < 30 && !success) {
	    try {
		instance = dao.save(instance);
		success = true;
	    } catch (final Exception e) {
		if (org.hibernate.exception.LockAcquisitionException.class.equals(e.getClass())) {
		    exceptionCount++;
		    Thread.currentThread().sleep(1000);
		} else {
		    throw e;
		}
	    }
	}

	final boolean updated = originalVersion != null && !originalVersion.equals(instance.getVersion());

	return new SaveResult(inserted, updated);
    }

    private String produceInstanceRawDataStringRepresentation(final ResultSet rs, final Collection<Container> containers) throws Exception {
	final List<String> result = new ArrayList<String>();
	for (final Container container : containers) {
	    result.add(container.getRepresentation(rs));
	}
	return result.toString();
    }

    final private Object[] getKeyPropsValues(final List<String> keyPropsNames, final Map<String, Object> propAdoptedValues) throws Exception {
	final List<Object> result = new ArrayList<Object>();
	for (final String keyPropName : keyPropsNames) {
	    result.add(propAdoptedValues.get(keyPropName));
	}
	return result.toArray();
    }

    final private SortedMap<String, Pair<Class, Integer>> getResultEntityMetadata(final ResultSetMetaData md, final Class<?> entityType) throws Exception {
	final SortedMap<String, Pair<Class, Integer>> props = new TreeMap<String, Pair<Class, Integer>>();
	for (int index = 1; index <= md.getColumnCount(); index++) {
	    final String propName = decodePropertyName(md.getColumnLabel(index));
	    final Class propType = PropertyTypeDeterminator.determinePropertyType(entityType, propName);
	    props.put(propName, new Pair<Class, Integer>(propType, index));
	}
	return props;
    }

    final private Map<String, Container> getContainers(final SortedMap<String, Pair<Class, Integer>> props, final Class<?> entityType) {
	final Map<String, Container> result = new HashMap<String, Container>();

	SortedMap<String, Pair<Class, Integer>> propsGroup = new TreeMap<String, Pair<Class, Integer>>();
	String groupProp = null;

	for (final Map.Entry<String, Pair<Class, Integer>> entry : props.entrySet()) {
	    final int dotPosition = entry.getKey().indexOf(".");
	    if (groupProp != null && entry.getKey().startsWith(groupProp + ".")) {
		propsGroup.put(entry.getKey().substring(groupProp.length() + 1), entry.getValue());
	    } else {
		// if group already exist - flush it
		if (groupProp != null) {
		    final Class groupPropType = PropertyTypeDeterminator.determinePropertyType(entityType, groupProp);
		    result.put(groupProp, new Container(groupProp, groupPropType, getContainers(propsGroup, groupPropType)));
		    groupProp = null;
		}

		if (dotPosition == -1) {
		    result.put(entry.getKey(), new Container(entry.getKey(), entry.getValue().getKey(), entry.getValue().getValue()));
		} else {
		    // start new group
		    propsGroup = new TreeMap<String, Pair<Class, Integer>>();
		    groupProp = entry.getKey().substring(0, dotPosition);
		    propsGroup.put(entry.getKey().substring(groupProp.length() + 1), entry.getValue());
		}
	    }
	}

	return result;
    }

    private static class Container {
	String propName;
	Class propType;
	Integer propValueIndex;
	List<Container> compositeMembers = new ArrayList<Container>();

	public Container(final String propName, final Class propType, final Integer propValueIndex) {
	    this.propName = propName;
	    this.propType = propType;
	    this.propValueIndex = propValueIndex;
	}

	public Container(final String propName, final Class propType, final Map<String, Container> compositeProps) {
	    this.propName = propName;
	    this.propType = propType;

	    final List<String> keyPropNames = Finder.getFieldNames(Finder.getKeyMembers(propType));
	    for (final String keyPropName : keyPropNames) {
		final Container keyPropContainer = compositeProps.get(keyPropName);
		if (keyPropContainer != null) {
		    compositeMembers.add(keyPropContainer);
		} else {
		    throw new RuntimeException("missing part of composite key");
		}
	    }
	}

	private String getRepresentation(final ResultSet rs) {
	    try {
		if (propValueIndex != null) {
		    return propName + "=" + rs.getObject(propValueIndex);
		} else {
		    final List<String> keyRepresentations = new ArrayList<String>();
		    for (final Container keyMember : compositeMembers) {
			keyRepresentations.add(keyMember.getRepresentation(rs));
		    }
		    return propName + "=" + keyRepresentations;
		}
	    } catch (final SQLException e) {
		return e.toString();
	    }
	}

	private Object getPropValue(final AbstractRetriever ret, final ResultSet rs, final DynamicEntityDao dynamicDao, final EntityFactory factory) throws Exception {
	    if (propValueIndex != null) {

		Object adoptedValue = null;
		final Object rawValue = rs.getObject(propValueIndex);
		try {
		    adoptedValue = ret.getAdoptedPropValue(propName, propType, rawValue);
		} catch (final Exception ex) {
		    throw new Result(ret.error(factory, propName, propType.getName(), rawValue), ex);
		}

		if (rawValue != null && adoptedValue == null) {
		    throw new Result(ret.error(factory, propName, propType.getName(), rawValue), new IllegalStateException("Not exists"));
		} else {
		    return adoptedValue;
		}
	    } else {
		final List<Object> compositeMembersValues = new ArrayList<Object>();
		for (final Container container : compositeMembers) {
		    final Object propValue = container.getPropValue(ret, rs, dynamicDao, factory);
		    if (propValue != null) {
			compositeMembersValues.add(propValue);
		    } else {
			return null;
		    }
		}
		dynamicDao.setEntityType(propType);
		return dynamicDao.findByKey(compositeMembersValues.toArray());
	    }
	}
    }


    private String getSubsetSql(final String subset) {
	final String baseSql = selectSql().toUpperCase();
	final int orderByStart = baseSql.indexOf("ORDER BY");
	final String unorderedSql = orderByStart != -1 ? baseSql.substring(0, orderByStart) : baseSql;
	final String orderBySql = orderByStart != -1 ? baseSql.substring(orderByStart) : "";
	final String splitPropertyColumn = AbstractRetriever.encodePropertyName(splitProperty());
	return "SELECT * FROM (" + unorderedSql + ") A WHERE A." + splitPropertyColumn + " IN " + subset + " " +  orderBySql;
    }

    /**
     * Creates and saves entities of the corresponding type based on the retrieved data using an SQL returned from method sql().
     */
    @Override
    public Result populateData(final SessionFactory sessionFactory, final Connection conn, final EntityFactory factory, final MigrationErrorDao errorDao, final MigrationHistoryDao histDao, final MigrationRun migrationRun, final String subset)
	    throws Exception {
	logger.info("Started executing " + getClass().getName() + "...");
	final Date started = new Date();

	final MigrationHistory hist = factory.newEntity(MigrationHistory.class);
	hist.setMigrationRun(migrationRun);
	hist.setRetrieverTypeName(getClass().getName());
	hist.setThreadName(Thread.currentThread().getName());
	hist.setEntityTypeName(type().getName());
	hist.setStarted(started);
	histDao.save(hist);

	Integer retrievedCount = 0;
	Integer failedCount = 0;
	final Integer skippedCount = 0;
	Integer updatedCount = 0;
	Integer insertedCount = 0;
	final String legacyDataSql = subset == null ? selectSql() : getSubsetSql(subset);
	if (!StringUtils.isEmpty(legacyDataSql)) {
	    final Statement st = conn.createStatement();
	    final ResultSet rs = st.executeQuery(legacyDataSql);
	    final List<String> keyPropNames = Finder.getFieldNames(Finder.getKeyMembers(type()));
	    final Collection<Container> containers = getContainers(getResultEntityMetadata(rs.getMetaData(), type()), type()).values();

	    while (rs.next()) {
		retrievedCount = retrievedCount + 1;
		try {
		    final SaveResult saveResult = produceInstance(rs, containers, keyPropNames, factory);
		    if (saveResult.inserted) {
			insertedCount = insertedCount + 1;
		    } else if (saveResult.updated) {
			updatedCount = updatedCount + 1;
		    }
		} catch (final Exception ex) {
		    failedCount = failedCount + 1;
		    final MigrationError error = ex instanceof Result ? (MigrationError) ((Result) ex).getInstance() : error(factory, null, null, null);
		    final Exception resultEx = ex instanceof Result ? ((Result) ex).getEx() : ex;

		    error.setRawData(produceInstanceRawDataStringRepresentation(rs, containers));
		    error.setMigrationHistory(hist);
		    error.setErrorNo(failedCount);
		    error.setErrorType(resultEx.getClass().getName());
		    error.setErrorText(resultEx.getMessage());
		    error.setErrorPreCause(resultEx.getCause() != null && resultEx.getCause().getCause() != null ? resultEx.getCause().getCause().toString() : null);
		    errorDao.save(error);
		}
	    }
	    rs.close();
	    st.close();
	}

	hist.setFinished(new Date());
	hist.setRetrievedCount(retrievedCount);
	hist.setFailedCount(failedCount);
	hist.setSkippedCount(skippedCount);
	hist.setInsertedCount(insertedCount);
	hist.setUpdatedCount(updatedCount);

	histDao.save(hist);

	logger.info("Finished executing " + getClass().getName());
	return new Result(hist.toString());
    }

    protected Object getAdoptedPropValue(final String propName, final Class<?> propType, final Object rawValue ) throws Exception {
	final Object propValue = convertValue(propName, propType, rawValue);
	if (propValue == null) {
	    return null;
	} else if (hasPvr(propName)) {
	    return findValue(propName, propValue);
	} else if (AbstractEntity.class.isAssignableFrom(propType)) {
	    dynamicDao.setEntityType((Class<? extends AbstractEntity>) propType);
	    return dynamicDao.findByKey(propValue);
	} else if (Money.class.isAssignableFrom(propType)) {
	    return rawValue != null ? new Money(new BigDecimal(rawValue.toString()), Currency.getInstance(Locale.getDefault())) : null;
	} else {
	    return propValue;
	}
    }

    /**
     * Should be overridden in order to provide necessary conversion. For example, converting string value "2.8L" to double value 2.8.
     *
     * @param propertyName
     * @param value
     * @return
     */
    protected Object convertValue(final String propName, final Class<?> propType, final Object value) {

//	if (propType.equals(Gender.class)) {
//	    if ("F".equalsIgnoreCase(value + "")) {
//		return Gender.F;
//	    }
//
//	    return Gender.M;
//	}

	if ((propType == boolean.class || propType == Boolean.class)) {
	    if (value == null || (value instanceof String && StringUtils.isEmpty(((String) value).trim()))) {
		return false;
	    } else if (value instanceof String) {
		final String strValue = ((String) value).trim();
		if ("a".equalsIgnoreCase(strValue) || "y".equalsIgnoreCase(strValue) || "yes".equalsIgnoreCase(strValue) || "1".equalsIgnoreCase(strValue) || "true".equalsIgnoreCase(strValue)
			|| "t".equalsIgnoreCase(strValue)) {
		    return true;
		}

		if ("b".equalsIgnoreCase(strValue) || "n".equalsIgnoreCase(strValue) || "no".equalsIgnoreCase(strValue) || "0".equalsIgnoreCase(strValue) || "false".equalsIgnoreCase(strValue)
			|| "f".equalsIgnoreCase(strValue)) {
		    return false;
		}

		throw new IllegalArgumentException("Incorrect value " + strValue + ", which could not be converted to boolean.");
	    }
	}

	if (value == null) {
	    return null;
	} else {
	    if (value instanceof String && StringUtils.isEmpty(((String) value).trim())) {
		return null;
	    }

	    if (value instanceof String && ((String) value).contains("\n")) {
		return ((String) value).replace("\n", "; ");
	    }

	    if (Number.class.isAssignableFrom(value.getClass())) {
		final Number valueAsNumber = (Number) value;

		if (propType == BigDecimal.class) {
		    if (value instanceof BigDecimal) {
			return value;
		    } else {
			return new BigDecimal(value.toString());
		    }
		}

		if (propType == Double.class) {
		    if (value instanceof Double) {
			return value;
		    } else {
			return valueAsNumber.doubleValue();
		    }
		}

		if (propType == Integer.class) {
		    if (value instanceof Integer) {
			return value;
		    } else {
			return valueAsNumber.intValue();
		    }
		}

		if (propType == Long.class) {
		    if (value instanceof Long) {
			return value;
		    } else {
			return valueAsNumber.longValue();
		    }
		}
	    }

	    return value;
	}
    }

    /** Associated property value retriever with a specific property. */
    public final void registerPropertyValueRetriever(final String propertyName, final IPropertyValueRetriever pvrInstance) {
	pvr.put(propertyName, pvrInstance);
    }

    /** A convenient method to determine PVR existence for the specified property. */
    protected boolean hasPvr(final String propertyName) {
	return pvr.containsKey(propertyName);
    }

    /** A convenient method for invoking PVR find method. */
    protected Object findValue(final String propertyName, final Object legacyValue) {
	if (!hasPvr(propertyName)) {
	    throw new IllegalArgumentException("Property " + propertyName + " in entity " + type().getName() + " does not have PVR.");
	}

	return pvr.get(propertyName).find(legacyValue);
    }

    /**
     * A helper method to decode column aliases as entity property names.
     *
     * @param columnAlias
     * @return
     */
    public static String decodePropertyName(final String columnAlias) {
	// replacing second underscore with dot to introduce dot.notation for potential sub-properties
	final String preName = columnAlias.trim().toLowerCase().replace("__", "_.");
	String name = !preName.endsWith("_") ? preName : preName.substring(0, preName.length() - 1);
	int index = name.indexOf("_");
	while (index >= 0) {
	    name = name.substring(0, index) + name.substring(index + 1, index + 2).toUpperCase() + name.substring(index + 2);
	    index = name.indexOf("_");
	}

	return name;
    }

    public static String encodePropertyName(final String propertyName) {
	final char[] chars = propertyName.toCharArray();
	final StringBuffer result = new StringBuffer();

	for (int i = 0; i < chars.length; i++) {
	    final char c = chars[i];
	    if (Character.isUpperCase(c)) {
		result.append("_");
	    } else if (".".charAt(0) == c) {
		result.append("__");
	    }
	    result.append(Character.toLowerCase(c));
	}

	result.append("_");

	return result.toString().replace(".", "");
    }

    protected boolean updateOnly() {
	return false;
    }

    @Override
    public String splitProperty() {
	return null;
    }

    public DynamicEntityDao getDynamicDao() {
        return dynamicDao;
    }

    public void setDynamicDao(final DynamicEntityDao dynamicDao) {
        this.dynamicDao = dynamicDao;
    }

    static class SaveResult {
	boolean inserted;
	boolean updated;

	SaveResult (final boolean inserted, final boolean updated) {
	    this.inserted = inserted;
	    this.updated = updated;
	}
    }
}