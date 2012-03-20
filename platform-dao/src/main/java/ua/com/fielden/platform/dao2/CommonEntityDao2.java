package ua.com.fielden.platform.dao2;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.zip.Deflater;

import org.apache.log4j.Logger;
import org.apache.log4j.helpers.DateTimeDateFormat;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFFont;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.hibernate.Session;
import org.hibernate.exception.ConstraintViolationException;
import org.joda.time.DateTime;

import ua.com.fielden.platform.dao.UsernameSetterMixin;
import ua.com.fielden.platform.dao.annotations.SessionRequired;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.annotation.TransactionDate;
import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.entity.meta.MetaProperty;
import ua.com.fielden.platform.entity.meta.PropertyDescriptor;
import ua.com.fielden.platform.entity.query.AggregatesFetcher;
import ua.com.fielden.platform.entity.query.EntityAggregates;
import ua.com.fielden.platform.entity.query.EntityFetcher;
import ua.com.fielden.platform.entity.query.IFilter;
import ua.com.fielden.platform.entity.query.fetch;
import ua.com.fielden.platform.entity.query.model.AggregatedResultQueryModel;
import ua.com.fielden.platform.entity.query.model.EntityResultQueryModel;
import ua.com.fielden.platform.error.Result;
import ua.com.fielden.platform.pagination.IPage2;
import ua.com.fielden.platform.reflection.Finder;
import ua.com.fielden.platform.reflection.Reflector;
import ua.com.fielden.platform.reflection.TitlesDescsGetter;
import ua.com.fielden.platform.security.user.IUserDao2;
import ua.com.fielden.platform.security.user.User;
import ua.com.fielden.platform.serialisation.GZipOutputStreamEx;

import com.google.inject.Inject;

import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.from;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.select;

/**
 * This is a most common Hibernate-based implementation of the {@link IEntityDao2}.
 * <p>
 * It should not be used directly -- more preferred way is to inherit it for implementation of a more specific DAO.
 * <p>
 * Property <code>session</code> is used to allocation session whenever is appropriate -- all data access methods should use this session. It is envisaged that the real class usage
 * will include Guice method intercepter that would assign session instance dynamically before executing calls to methods annotated with {@link SessionRequired}.
 *
 * @author TG Team
 *
 * @param <T>
 *            -- entity type
 * @param <K>
 *            -- entitie's key type
 */
public abstract class CommonEntityDao2<T extends AbstractEntity<?>> extends AbstractEntityDao2<T> {

    private Logger logger = Logger.getLogger(this.getClass());

    private ThreadLocal<Session> threadLocalSession = new ThreadLocal<Session>();

    private MappingsGenerator mappingsGenerator;

    private EntityFactory entityFactory;

    private String username;

    private final IFilter filter;

    @Inject
    private IUserDao2 userDao;


    /**
     * A principle constructor.
     *
     * @param entityType
     */
    @Inject
    protected CommonEntityDao2(final IFilter filter) {
	this.filter = filter;
    }

    /**
     * A setter for injection of entityFactory instance.
     *
     * @param entityFactory
     */
    @Inject
    protected void setEntityFactory(final EntityFactory entityFactory) {
	this.entityFactory = entityFactory;
    }

    /**
     * A separate setter is used in order to avoid enforcement of providing mapping generator as one of constructor parameter in descendant classes.
     *
     * @param mappingExtractor
     */
    @Inject
    protected void setMappingsGenerator(final MappingsGenerator mappingsGenerator) {
	this.mappingsGenerator = mappingsGenerator;
    }

    @Override
    public final void setUsername(final String username) {
	try {
	    UsernameSetterMixin.setUsername(username, this, Finder.findFieldByName(getClass(), "username"));
	} catch (final Exception e) {
	    throw new IllegalStateException(e);
	}
    }

    @Override
    public final String getUsername() {
	return username;
    }

    @Override
    @SessionRequired
    public T findById(final Long id, final fetch<T> fetchModel) {
	return super.findById(id, fetchModel);
    }

    @Override
    @SessionRequired
    public T findByKeyAndFetch(final fetch<T> fetchModel, final Object... keyValues) {
	return super.findByKeyAndFetch(fetchModel, keyValues);
    }

    @Override
    @SessionRequired
    public T findByKey(final Object... keyValues) {
	return super.findByKey(keyValues);
    }

    /**
     * Saves the provided entity. This method checks entity version and throws StaleObjectStateException if the provided entity is stale.
     */
    @Override
    @SessionRequired
    public T save(final T entity) {
	if (entity == null) {
	    throw new IllegalArgumentException("Entity should not be null when saving.");
	}
	logger.info("Start saving entity " + entity + " (ID = " + entity.getId() + ")");
	try {
	    if (!entity.isPersisted()) {
		// first check if the passed in entity is valid
		final Result isValid = entity.isValid();
		if (!isValid.isSuccessful()) {
		    throw isValid;
		}
		// let's also make sure that duplicate entities are not allowed
		final Integer count = count(createQueryByKey(entity.getKey()), Collections.<String, Object> emptyMap());
		if (count > 0) {
		    throw new Result(entity, new IllegalArgumentException("Such " + TitlesDescsGetter.getEntityTitleAndDesc(entity.getType()).getKey() + " already exists."));
		}

		// check and assign properties annotated with @TransactionDate
		try {
		    assignTransactionDate(entity);
		} catch (final Exception e) {
		    throw new IllegalStateException("Could not assign transaction date properties.", e);
		}

		// save the entity
		getSession().save(entity);
	    } else if (!entity.getDirtyProperties().isEmpty()) {
		// let's also make sure that duplicate entities are not allowed
		final AggregatedResultQueryModel model = select(createQueryByKey(entity.getKey())).yield().prop("id").as("id").modelAsAggregate();
		final List<EntityAggregates> ids = new AggregatesFetcher(getSession(), getEntityFactory(), mappingsGenerator, null, null, null).list(from(model).build());
		final int count = ids.size();
		if (count == 1 && !(entity.getId().longValue() == ((Number) ids.get(0).get("id")).longValue())) {
		    throw new Result(entity, new IllegalArgumentException("Such " + TitlesDescsGetter.getEntityTitleAndDesc(entity.getType()).getKey() + " entity already exists."));
		}

		// If entity with id exists then the returned instance is proxied since it is retrieved using standard Hibernate session get method,
		// and thus is associated with current Hibernate session.
		// This seems to be advantageous since entity validation would work relying on standard Hibernate lazy initialisation.

		final T persistedEntity = (T) getSession().load(getEntityType(), entity.getId());
		// first check any concurrent modification
		if (persistedEntity.getVersion() != null && persistedEntity.getVersion() > entity.getVersion()) {
		    throw new Result(entity, new IllegalStateException("Cannot save a stale entity " + entity.getKey() + " (" + TitlesDescsGetter.getEntityTitleAndDesc(getEntityType()).getKey() + ") -- another user has changed it."));
		}
		// if there are changes persist them
		// Setting modified values triggers any associated validation.
		// An interesting case is with validation based on associative properties such as a work order for purchase order item.
		// If a purchase order item is being persisted some of its property validation might depend on the state of the associated work order.
		// When this purchase order item was validated at the client side it might have been using a stale work order.
		// In here revalidation occurs, which would definitely work with the latest data.
		for (final Object obj : entity.getDirtyProperties()) {
		    final String propName = ((MetaProperty) obj).getName();
//		    logger.error("is dirty: " + propName + " of " + getEntityType().getSimpleName() + " old = " + ((MetaProperty) obj).getOriginalValue() + " new = " + ((MetaProperty) obj).getValue());
		    final Object value = entity.get(propName);
		    // it is essential that if a property is of an entity type it should be re-associated with the current session before being set
		    // the easiest way to do that is to load entity be id using the current session
		    if (value instanceof AbstractEntity && !(value instanceof PropertyDescriptor)) {
			persistedEntity.set(propName, getSession().load(((AbstractEntity) value).getType(), ((AbstractEntity) value).getId()));
		    } else {
			persistedEntity.set(propName, value);
		    }
		}
		// check if entity is valid after changes
		final Result res = persistedEntity.isValid();
		if (res.isSuccessful()) {
		    // during the update a StaleObjectStateException might be thrown.
		    // getSession().flush();
		    getSession().update(persistedEntity);
		} else {
		    throw res;
		}
		// set incremented version
		try {
		    final Method setVersion = Reflector.getMethod(entity/* .getType() */, "setVersion", Long.class);
		    setVersion.setAccessible(true);
		    setVersion.invoke(entity, entity.getVersion() + 1);
		} catch (final Exception e) {
		    throw new IllegalStateException("Could not set updated entity version.");
		}

	    }

	    entity.setDirty(false);
	    entity.resetMetaState();
	} finally {
	    logger.info("Finished saving entity " + entity + " (ID = " + entity.getId() + ")");
	}

	getSession().flush();
	getSession().clear();

	return entity;
    }

    private void assignTransactionDate(final T entity) throws Exception {
	final List<Field> transactionDateProperties = Finder.findRealProperties(entity.getType(), TransactionDate.class);

	for (final Field property : transactionDateProperties) {
	    property.setAccessible(true);
	    final Object value = property.get(entity);
	    if (value == null) {
		if (Date.class.isAssignableFrom(property.getType())) {
		    property.set(entity, new Date());
		} else if (DateTime.class.isAssignableFrom(property.getType())) {
		    property.set(entity, new DateTime());
		} else {
		    throw new IllegalArgumentException("The type of property " + entity.getType().getName() + "@" + property.getName()
			    + " is not valid for annotation TransactionDate.");
		}
	    }
	}
    }

    @Override
    @SessionRequired
    public boolean isStale(final Long entityId, final Long version) {
	if (entityId == null) {
	    return false;
	}

	final Integer count = ((Number) getSession().createQuery("select count(*) from " + getEntityType().getName() + " where id = :id and version = :version")//
	.setParameter("id", entityId).setParameter("version", version).uniqueResult()).intValue();

	return count != 1;
    }

    /**
     * TODO Need to consider the case of polymorphic associations such as Rotable, which can be both Bogie and/or Wheelset.
     */
    @Override
    @SessionRequired
    public boolean entityExists(final T entity) {
	return entityExists(entity.getId());
    }

    @Override
    @SessionRequired
    public boolean entityExists(final Long id) {
	if (id == null) {
	    return false;
	}
	return getSession().createQuery("select id from " + getEntityType().getName() + " where id = :in_id").setLong("in_id", id).uniqueResult() != null;
    }

    public int count(final EntityResultQueryModel<T> model, final Map<String, Object> paramValues) {
	return evalNumOfPages(model, paramValues, 1);
    }

    public int count(final EntityResultQueryModel<T> model) {
	return count(model, Collections.<String, Object> emptyMap());
    }

    /**
     * Fetches the results of the specified page based on the request of the given instance of {@link QueryExecutionModel}.
     *
     * @param queryModel
     * @param pageNumber
     * @param pageCapacity
     * @return
     */
    @SessionRequired
    protected List<T> list(final QueryExecutionModel<T> queryModel, final Integer pageNumber, final Integer pageCapacity) {
	return new EntityFetcher(getSession(), getEntityFactory(), mappingsGenerator, null, filter, getUsername()).list(queryModel, pageNumber, pageCapacity);
    }

    @Override
    public List<T> getEntities(final QueryExecutionModel<T> query) {
	return list(query, null, null);
    }

    /**
     * Returns a first page holding up to <code>pageCapacity</code> instance of entities retrieved by query with no filtering conditions. Useful for things like autocompleters.
     */
    @Override
    public IPage2<T> firstPage(final int pageCapacity) {
	return new EntityQueryPage(getDefaultQueryExecutionModel(), 0, pageCapacity, evalNumOfPages(getDefaultQueryExecutionModel().getQueryModel(), Collections.<String, Object> emptyMap(), pageCapacity));
    }

    /**
     * Returns a first page holding up to <code>size</code> instance of entities retrieved by the provided query model. This allows a query based pagination.
     */
    @Override
    public IPage2<T> firstPage(final QueryExecutionModel<T> model, final int pageCapacity) {
	return new EntityQueryPage(model, 0, pageCapacity, evalNumOfPages(model.getQueryModel(), model.getParamValues(), pageCapacity));
    }

    @Override
    public IPage2<T> getPage(final QueryExecutionModel<T> model, final int pageNo, final int pageCapacity) {
	return getPage(model, pageNo, 0, pageCapacity);
    }

    @Override
    public IPage2<T> getPage(final QueryExecutionModel<T> model, final int pageNo, final int pageCount, final int pageCapacity) {
	final int numberOfPages = pageCount > 0 ? pageCount : evalNumOfPages(model.getQueryModel(), model.getParamValues(), pageCapacity);
	final int pageNumber = pageNo < 0 ? numberOfPages - 1 : pageNo;
	return new EntityQueryPage(model, pageNumber, pageCapacity, numberOfPages);
    }

    @Override
    public T getEntity(final QueryExecutionModel<T> model) {
	final List<T> data = new EntityQueryPage(model, 0, DEFAULT_PAGE_CAPACITY, 1).data();
	if (data.size() > 1) {
	    throw new IllegalArgumentException("The provided query model leads to retrieval of more than one entity (" + data.size() + ").");
	}
	return data.size() == 1 ? data.get(0) : null;
    }

    @Override
    public IPage2<T> getPage(final int pageNo, final int pageCapacity) {
	final int numberOfPages = evalNumOfPages(getDefaultQueryExecutionModel().getQueryModel(), Collections.<String, Object> emptyMap(), pageCapacity);
	final int pageNumber = pageNo < 0 ? numberOfPages - 1 : pageNo;
	return new EntityQueryPage(getDefaultQueryExecutionModel(), pageNumber, pageCapacity, numberOfPages);
    }

    public Session getSession() {
	final Session session = threadLocalSession.get();
	if (session == null) {
	    throw new RuntimeException("Someone forgot to annotate some method with SessionRequired!");
	}
	return session;
    }

    public void setSession(final Session session) {
	threadLocalSession.set(session);
    }

    /**
     * Calculates the number of pages of the given size required to fit the whole result set.
     *
     *
     * @param model
     * @param pageCapacity
     * @return
     */
    @SessionRequired
    protected int evalNumOfPages(final EntityResultQueryModel<T> model, final Map<String, Object> paramValues, final int pageCapacity) {
	final AggregatedResultQueryModel countQuery = select(model).yield().countAll().as("count").modelAsAggregate();
	final AggregatesQueryExecutionModel countModel = from(countQuery).with(paramValues).lightweight(true).build();
	final List<EntityAggregates> counts = new AggregatesFetcher(getSession(), getEntityFactory(), mappingsGenerator, null, filter, getUsername()). //
		list(countModel, null, null);
	final int resultSize = ((Number) counts.get(0).get("count")).intValue();

	return resultSize % pageCapacity == 0 ? resultSize / pageCapacity : resultSize / pageCapacity + 1;
    }

    /**
     * Should return a byte array representation the exported data in a format envisaged by the specific implementation.
     * <p>
     * For example it could be a byte array of GZipped Excel data.
     *
     * @param query
     *            -- query result of which should be exported.
     * @param propertyNames
     *            -- names of properties, including dot notated properties, which should be used in the export.
     * @param propertyTitles
     *            -- titles corresponding to the properties being exported, which are used as headers of columns.
     * @return
     * @throws IOException
     */
    @Override
    public byte[] export(final QueryExecutionModel<T> query, final String[] propertyNames, final String[] propertyTitles) throws IOException {
	final HSSFWorkbook wb = new HSSFWorkbook();
	final HSSFSheet sheet = wb.createSheet("Exported Data");
	// Create a header row.
	final HSSFRow headerRow = sheet.createRow(0);
	// Create a new font and alter it
	final HSSFFont font = wb.createFont();
	font.setFontHeightInPoints((short) 12);
	font.setFontName("Courier New");
	font.setBoldweight((short) 1000);
	// Fonts are set into a style so create a new one to use
	final HSSFCellStyle headerCellStyle = wb.createCellStyle();
	headerCellStyle.setFont(font);
	headerCellStyle.setBorderBottom(HSSFCellStyle.BORDER_THIN);
	final HSSFCellStyle headerInnerCellStyle = wb.createCellStyle();
	headerInnerCellStyle.setFont(font);
	headerInnerCellStyle.setBorderBottom(HSSFCellStyle.BORDER_THIN);
	headerInnerCellStyle.setBorderRight(HSSFCellStyle.BORDER_HAIR);
	// Create cells and put column names there
	for (int index = 0; index < propertyTitles.length; index++) {
	    final HSSFCell cell = headerRow.createCell(index);
	    cell.setCellValue(propertyTitles[index]);
	    cell.setCellStyle(index < propertyTitles.length - 1 ? headerInnerCellStyle : headerCellStyle);
	}

	// let's make cell style to handle borders
	final HSSFCellStyle dataCellStyle = wb.createCellStyle();
	dataCellStyle.setBorderRight(HSSFCellStyle.BORDER_HAIR);
	// run the query and iterate through result exporting the data
	final List<T> result = list(query, null, null);
	for (int index = 0; index < result.size(); index++) {
	    final HSSFRow row = sheet.createRow(index + 1); // new row starting with 1
	    // iterate through values in the current table row and populate the sheet row
	    for (int propIndex = 0; propIndex < propertyNames.length; propIndex++) {
		final HSSFCell cell = row.createCell(propIndex); // create new cell
		if (propIndex < propertyNames.length - 1) { // the last column should not have right border
		    cell.setCellStyle(dataCellStyle);
		}
		final Object value = result.get(index).get(propertyNames[propIndex]); // get the value
		// need to try to do the best job with types
		if (value instanceof Date) {
		    cell.setCellValue(DateTimeDateFormat.getDateTimeInstance().format(value));
		} else if (value instanceof DateTime) {
		    cell.setCellValue(DateTimeDateFormat.getDateTimeInstance().format(value));
		} else if (value instanceof Number) {
		    cell.setCellType(HSSFCell.CELL_TYPE_NUMERIC);
		    cell.setCellValue(((Number) value).doubleValue());
		} else if (value instanceof Boolean) {
		    cell.setCellType(HSSFCell.CELL_TYPE_BOOLEAN);
		    cell.setCellValue((Boolean) value);
		} else if (value == null) { // if null then leave call blank
		    cell.setCellType(HSSFCell.CELL_TYPE_BLANK);
		} else { // otherwise treat value as String
		    cell.setCellType(HSSFCell.CELL_TYPE_STRING);
		    cell.setCellValue(value.toString());
		}
	    }
	}

	final ByteArrayOutputStream oStream = new ByteArrayOutputStream();
	final GZipOutputStreamEx zOut = new GZipOutputStreamEx(oStream, Deflater.BEST_COMPRESSION);

	wb.write(zOut);

	zOut.flush();
	zOut.close();
	oStream.flush();
	oStream.close();

	return oStream.toByteArray();
    }

    /**
     * A convenient default implementation for entity deletion, which should be used by overriding method {@link #delete(Long)}.
     *
     * @param entity
     */
    @SessionRequired
    protected void defaultDelete(final T entity) {
	if (entity == null) {
	    throw new Result(new IllegalArgumentException("Null is not an acceptable value for an entity instance."));
	}
	if (!entity.isPersisted()) {
	    throw new Result(new IllegalArgumentException("Only persisted entity instances can be deleted."));
	}
	try {
	    getSession().createQuery("delete " + getEntityType().getName() + " where id = " + entity.getId()).executeUpdate();
	} catch (final ConstraintViolationException e) {
	    throw new Result(new IllegalStateException("This entity could not be deleted due to existing dependencies."));
	}
    }

    /**
     * A convenient default implementation for deletion of entities specified by provided query model.
     *
     * @param entity
     */
    @SessionRequired
    protected void defaultDelete(final EntityResultQueryModel<T> model, final Map<String, Object> paramValues) {
	if (model == null) {
	    throw new Result(new IllegalArgumentException("Null is not an acceptable value for eQuery model."));
	}

	final List<T> toBeDeleted = getEntities(from(model).with(paramValues).lightweight(true).build());

	for (final T entity : toBeDeleted) {
	    defaultDelete(entity);
	}
    }

    /**
     * Implements pagination based on the provided query.
     *
     * @author 01es
     *
     */
    public class EntityQueryPage implements IPage2<T> {
	private final int pageNumber; // zero-based
	private final int numberOfPages;
	private final int pageCapacity;
	private final List<T> data;
	private final QueryExecutionModel<T> queryModel;

	/**
	 * Temporary constructor to be used with IQueryOrderedModel during transition phase.
	 *
	 * @param queryModel
	 * @param pageNumber
	 * @param pageCapacity
	 * @param numberOfPages
	 */
	public EntityQueryPage(final QueryExecutionModel<T> queryModel, final int pageNumber, final int pageCapacity, final int numberOfPages) {
	    this.pageNumber = pageNumber;
	    this.pageCapacity = pageCapacity;
	    this.numberOfPages = numberOfPages == 0 ? 1 : numberOfPages;
	    this.queryModel = queryModel;
	    data = list(queryModel, pageNumber, pageCapacity);
	}

	@Override
	public EntityAggregates summary() {
	    return null;
	}

	@Override
	public int capacity() {
	    return pageCapacity;
	}

	@Override
	public List<T> data() {
	    return hasNext() ? data.subList(0, capacity()) : data;
	}

	@Override
	public boolean hasNext() {
	    return pageNumber < numberOfPages - 1;
	}

	@Override
	public boolean hasPrev() {
	    return no() > 0;
	}

	@Override
	public IPage2<T> next() {
	    if (hasNext()) {
		return new EntityQueryPage(queryModel, no() + 1, capacity(), numberOfPages);
	    }
	    return null;
	}

	@Override
	public IPage2<T> prev() {
	    if (hasPrev()) {
		return new EntityQueryPage(queryModel, no() - 1, capacity(), numberOfPages);
	    }
	    return null;
	}

	@Override
	public IPage2<T> first() {
	    if (hasPrev()) {
		return new EntityQueryPage(queryModel, 0, capacity(), numberOfPages);
	    }
	    return null;
	}

	@Override
	public IPage2<T> last() {
	    if (hasNext()) {
		return new EntityQueryPage(queryModel, numberOfPages - 1, capacity(), numberOfPages);
	    }
	    return null;
	}

	@Override
	public int numberOfPages() {
	    return numberOfPages;
	}

	@Override
	public String toString() {
	    return "Page " + (no() + 1) + " of " + numberOfPages;
	}

	@Override
	public int no() {
	    return pageNumber;
	}
    }

    protected EntityFactory getEntityFactory() {
	return entityFactory;
    }

    public MappingsGenerator getMappingsGenerator() {
        return mappingsGenerator;
    }

    @Override
    public User getUser() {
	return userDao.findByKey(getUsername());
    }

    public IFilter getFilter() {
        return filter;
    }
}