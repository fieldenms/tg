package ua.com.fielden.platform.rao;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.GZIPInputStream;

import org.apache.commons.lang.StringUtils;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.data.Method;
import org.restlet.data.Status;
import org.restlet.representation.Representation;

import ua.com.fielden.platform.dao.IEntityDao;
import ua.com.fielden.platform.dao.IGeneratedEntityController;
import ua.com.fielden.platform.dao.QueryExecutionModel;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.DynamicallyTypedQueryContainer;
import ua.com.fielden.platform.entity.query.fluent.fetch;
import ua.com.fielden.platform.entity.query.model.EntityResultQueryModel;
import ua.com.fielden.platform.error.Result;
import ua.com.fielden.platform.pagination.IPage;
import ua.com.fielden.platform.rao.CommonEntityRao.PageInfo;
import ua.com.fielden.platform.reflection.AnnotationReflector;
import ua.com.fielden.platform.reflection.CompanionObjectAutobinder;
import ua.com.fielden.platform.roa.HttpHeaders;
import ua.com.fielden.platform.security.Authorise;
import ua.com.fielden.platform.security.IAuthorisationModel;
import ua.com.fielden.platform.utils.Pair;

import com.google.inject.Inject;

import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.from;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.select;

/**
 * This RAO is applicable for executing queries based on dynamically generated entity types.
 *
 * @author TG Team
 *
 */
public class GeneratedEntityRao<T extends AbstractEntity<?>> implements IGeneratedEntityController<T> {

    private Class<T> entityType;
    private Class<? extends Comparable> keyType;

    protected final RestClientUtil restUtil;
    private final IAuthorisationModel authModel;
    private Class<? extends IEntityDao<?>> coType;

    /**
     * Needed for reflective instantiation.
     */
    @Inject
    public GeneratedEntityRao(final RestClientUtil util, final IAuthorisationModel authModel) {
	this.restUtil = util;
	this.authModel = authModel;
    }

    /** This constructor is intended for testing purposes only. */
    public GeneratedEntityRao(final RestClientUtil util) {
	this(util, null);
    }

    protected WebResourceType getDefaultWebResourceType() {
	return WebResourceType.VERSIONED;
    }

    @Override
    public void setEntityType(final Class<T> type) {
	this.entityType = type;
	this.keyType = AnnotationReflector.getKeyType(entityType);
	this.coType = CompanionObjectAutobinder.companionObjectType(getEntityType());
    }

    @Override
    public Class<T> getEntityType() {
	return entityType;
    }

    @Override
    public Class<? extends Comparable> getKeyType() {
	return keyType;
    }

    /**
     * A general purpose logic for manual authorisation of method calls on companion entity object based on the security model.
     *
     * @param methodName
     */
    private void authCall(final String methodName) {
	if (authModel != null && coType != null) {
	    final java.lang.reflect.Method[] methods = coType.getDeclaredMethods();
	    for (final java.lang.reflect.Method method : methods) {
		if (methodName.equalsIgnoreCase(method.getName())) {
		    final Authorise annotation = method.getAnnotation(Authorise.class);
		    if (annotation != null) {
			authModel.start();
			final Result result = authModel.authorise(annotation.value());
			try {
			    if (!result.isSuccessful()) {
				throw result;
			    }
			} finally {
			    authModel.stop();
			}
		    }
		}
	    }
	}
    }


    @Override
    public T findById(final Long id, final fetch<T> fetchModel, final List<byte[]> binaryTypes) {
	authCall("findById");
	return fetchOneEntityInstance(id, fetchModel, binaryTypes);
    }

    @Override
    public T findById(final Long id, final List<byte[]> binaryTypes) {
	authCall("findById");
	return fetchOneEntityInstance(id, null, binaryTypes);
    }

    @Override
    public T getEntity(final QueryExecutionModel<T, ?> model, final List<byte[]> binaryTypes) {
	authCall("getEntity");
	final List<T> data = getFirstEntities(model, IEntityDao.DEFAULT_PAGE_CAPACITY, binaryTypes);
	if (data.size() > 1) {
	    throw new IllegalArgumentException("The provided query model leads to retrieval of more than one entity (" + data.size() + ").");
	}
	return data.size() == 1 ? data.get(0) : null;
    }

    @Override
    public IPage<T> firstPage(final QueryExecutionModel<T, ?> qem, final int pageCapacity, final List<byte[]> binaryTypes) {
	authCall("firstPage");
	return new EntityQueryPage(qem, new PageInfo(0, 0, pageCapacity), binaryTypes);
    }

    @Override
    public IPage<T> firstPage(final QueryExecutionModel<T, ?> qem, final QueryExecutionModel<T, ?> summaryModel, final int pageCapacity, final List<byte[]> binaryTypes) {
	authCall("firstPage");
	return new EntityQueryPage(qem, summaryModel, new PageInfo(0, 0, pageCapacity), binaryTypes);
    }

    @Override
    public IPage<T> getPage(final QueryExecutionModel<T, ?> qem, final int pageNo, final int pageCapacity, final List<byte[]> binaryTypes) {
	authCall("getPage");
	return new EntityQueryPage(qem, new PageInfo(pageNo, 0, pageCapacity), binaryTypes);
    }

    @Override
    public IPage<T> getPage(final QueryExecutionModel<T, ?> model, final int pageNo, final int pageCount, final int pageCapacity, final List<byte[]> binaryTypes) {
	authCall("getPage");
	return new EntityQueryPage(model, new PageInfo(pageNo, pageCount, pageCapacity), binaryTypes);
    }

    /**
     * Sends a POST request to /query/generated-type??page-capacity=all with an envelope containing instance of {@link QueryExecutionModel} and binary representation of generated
     * types. The response suppose to return an envelope containing all entities resulting from the query.
     */

    @Override
    public List<T> getAllEntities(final QueryExecutionModel<T, ?> qem, final List<byte[]> binaryTypes) {
	authCall("getAllEntities");
	qem.getQueryModel().setFilterable(true);

	// create request envelope containing Entity Query
	final Representation envelope = restUtil.represent(qem, binaryTypes);
	// create a request URI containing page capacity and number
	final String uri = restUtil.getQueryUri(getEntityType(), getDefaultWebResourceType()) + "?page-capacity=all";
	final Request request = new Request(Method.POST, uri, envelope);
	// process request
	final Pair<Response, Result> res = restUtil.process(request);
	final Result result = res.getValue();
	// process result
	if (result.isSuccessful()) {
	    return (List<T>) result.getInstance();
	} else {
	    throw result;
	}

    }

    /**
     * Sends a POST request to /query/generated-type??page-no=first with an envelope containing instance of {@link QueryExecutionModel} and binary representation of generated
     * types. The response suppose to return an envelope containing first entities resulting from the query.
     */

    @Override
    public List<T> getFirstEntities(final QueryExecutionModel<T, ?> qem, final int numberOfEntities, final List<byte[]> binaryTypes) {
	authCall("getFirstEntities");
	qem.getQueryModel().setFilterable(true);

	// create request envelope containing Entity Query
	final Representation envelope = restUtil.represent(qem, binaryTypes);
	// create a request URI containing page capacity and number
	final String uri = restUtil.getQueryUri(getEntityType(), getDefaultWebResourceType()) + "?page-capacity=" + numberOfEntities + "&page-no=first";
	final Request request = new Request(Method.POST, uri, envelope);
	// process request
	final Pair<Response, Result> res = restUtil.process(request);
	final Result result = res.getValue();
	// process result
	if (result.isSuccessful()) {
	    return (List<T>) result.getInstance();
	} else {
	    throw result;
	}
    }

    /**
     * Sends a POST request to /export/generated-type with an envelope containing instance of {@link QueryExecutionModel} and binary representation of generated types. The response
     * suppose to return an file with exported information.
     */
    @Override
    public byte[] export(//
	    final QueryExecutionModel<T, ?> qem,//
	    final String[] propertyNames,//
	    final String[] propertyTitles,//
	    final List<byte[]> binaryTypes) throws IOException {

	authCall("export");
	qem.getQueryModel().setFilterable(true);

	// create request envelope containing Entity Query
	final List<Object> requestContent = new ArrayList<Object>();
	requestContent.add(new DynamicallyTypedQueryContainer(binaryTypes, qem));
	requestContent.add(propertyNames);
	requestContent.add(propertyTitles);
	final Representation envelope = restUtil.represent(requestContent);
	// create a request URI containing page capacity and number
	final String uri = restUtil.getExportUri(getEntityType()) + "?type=excel";
	// send request
	final Response response = restUtil.send(new Request(Method.POST, uri, envelope));
	if (!Status.SUCCESS_OK.equals(response.getStatus())) {
	    final Result result = restUtil.process(response);
	    throw new IllegalStateException(result.getEx());
	}
	final InputStream content = response.getEntity().getStream();
	final GZIPInputStream stream = new GZIPInputStream(content);
	final ByteArrayOutputStream oStream = new ByteArrayOutputStream();
	int i = stream.read();
	while (i != -1) {
	    oStream.write(i);
	    i = stream.read();
	}
	oStream.flush();
	oStream.close();
	stream.close();

	return oStream.toByteArray();
    }

    /**
     * Sends a POST request to /query/generated-type?page-capacity=pageCapacity&page-no=pageNumber with an envelope containing instance of {@link QueryExecutionModel} and binary
     * representation of generated types. The response suppose to return an envelope containing entities resulting from the query. The number of returned entities is constrained by
     * the information provided as part of <code>pageInfo</code> argument.
     */
    protected List<T> list(final QueryExecutionModel<T, ?> query, final PageInfo pageInfo, final List<byte[]> binaryTypes) {
	authCall("list");
	query.getQueryModel().setFilterable(true);
	// create request envelope containing Entity Query
	final Representation envelope = restUtil.represent(query, binaryTypes);
	// create a request URI containing page capacity and number
	final String uri = restUtil.getQueryUri(getEntityType(), getDefaultWebResourceType()) + "?page-capacity=" + pageInfo.pageCapacity + "&page-no=" + pageInfo.pageNumber
		+ "&page-count=" + pageInfo.numberOfPages;
	final Request request = new Request(Method.POST, uri, envelope);
	// process request
	final Pair<Response, Result> res = restUtil.process(request);

	final Response response = res.getKey();
	final Result result = res.getValue();
	// process result
	if (result.isSuccessful()) {
	    // process header values
	    final String pageNo = restUtil.getHeaderValue(response, HttpHeaders.PAGE_NO);
	    if (!StringUtils.isEmpty(pageNo)) {
		pageInfo.pageNumber = Integer.parseInt(pageNo);
	    }
	    final String numberOfPages = restUtil.getHeaderValue(response, HttpHeaders.PAGES);
	    if (!StringUtils.isEmpty(numberOfPages)) {
		pageInfo.numberOfPages = Integer.parseInt(numberOfPages);
	    }
	    return (List<T>) result.getInstance();
	} else {
	    throw result;
	}
    }

    protected T calcSummary(final QueryExecutionModel<T, ?> qem, final List<byte[]> binaryTypes) {
	final List<T> list = getAllEntities(qem, binaryTypes);
	return list.size() == 1 ? list.get(0) : null;
    }

    private T fetchOneEntityInstance(final Long id, final fetch<T> fetchModel, final List<byte[]> binaryTypes) {
	try {
	    final EntityResultQueryModel<T> query = select(getEntityType()).where().prop(AbstractEntity.ID).eq().val(id).model();
	    return getEntity(from(query).with(fetchModel).model(), binaryTypes);
	} catch (final Exception e) {
	    throw new IllegalStateException(e);
	}
    }

    /**
     * Implements pagination based on the provided query for either generated or coded entity type.
     *
     * @author TG Team
     *
     */
    private class EntityQueryPage implements IPage<T> {
	private int pageNumber; // zero-based
	private int numberOfPages = 0;
	private final int pageCapacity;
	private final List<T> data;
	private final QueryExecutionModel<T, ?> model;
	private final T summary;
	private final QueryExecutionModel<T, ?> summaryModel;
	private final List<byte[]> binaryTypes;

	/** This constructor should be used when summary is not required. */
	public EntityQueryPage(final QueryExecutionModel<T, ?> model, final PageInfo pageInfo, final List<byte[]> binaryTypes) {
	    data = list(model, pageInfo, binaryTypes);
	    pageNumber = pageInfo.pageNumber;
	    this.binaryTypes = binaryTypes;
	    setNumberOfPages(pageInfo.numberOfPages);
	    pageCapacity = pageInfo.pageCapacity;
	    this.model = model;
	    summaryModel = null;
	    summary = null;
	}

	/**
	 * This constructor should be used when both data and summary should be retrieved. Passing <code>null</code> for a summary model is handled gracefully.
	 */
	public EntityQueryPage(final QueryExecutionModel<T, ?> model, final QueryExecutionModel<T, ?> summaryModel, final PageInfo pageInfo, final List<byte[]> binaryTypes) {
	    data = list(model, pageInfo, binaryTypes);
	    summary = summaryModel != null ? calcSummary(summaryModel, binaryTypes) : null;
	    pageNumber = pageInfo.pageNumber;
	    this.binaryTypes = binaryTypes;
	    setNumberOfPages(pageInfo.numberOfPages);
	    pageCapacity = pageInfo.pageCapacity;
	    this.model = model;
	    this.summaryModel = summaryModel;
	}

	/** This constructor is required purely for navigation implementation in case where summary was calculated and needs to be preserved in another page. */
	private EntityQueryPage(final QueryExecutionModel<T, ?> model, final T summary, final PageInfo pageInfo, final List<byte[]> binaryTypes) {
	    data = list(model, pageInfo, binaryTypes);
	    pageNumber = pageInfo.pageNumber;
	    this.binaryTypes = binaryTypes;
	    setNumberOfPages(pageInfo.numberOfPages);
	    pageCapacity = pageInfo.pageCapacity;
	    this.model = model;
	    summaryModel = null;
	    this.summary = summary;
	}

	@Override
	public T summary() {
	    return summary;
	}

	public void setPageNo(final int pageNo) {
	    pageNumber = pageNo;
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
	    return no() != 0;
	}

	@Override
	public IPage<T> next() {
	    if (hasNext()) {
		if (model != null && summary != null) {
		    return new EntityQueryPage(model, summary, new PageInfo(no() + 1, numberOfPages(), capacity()), binaryTypes);
		} else if (model != null) {
		    return new EntityQueryPage(model, new PageInfo(no() + 1, numberOfPages(), capacity()), binaryTypes);
		} else {
		    throw new IllegalStateException("There was no query provided to retrieve the data.");
		}
	    }
	    return null;
	}

	@Override
	public int no() {
	    return pageNumber;
	}

	@Override
	public IPage<T> prev() {
	    if (hasPrev()) {
		if (model != null && summary != null) {
		    return new EntityQueryPage(model, summary, new PageInfo(no() - 1, numberOfPages(), capacity()), binaryTypes);
		} else if (model != null) {
		    return new EntityQueryPage(model, new PageInfo(no() - 1, numberOfPages(), capacity()), binaryTypes);
		} else {
		    throw new IllegalStateException("There was no query provided to retrieve the data.");
		}
	    }
	    return null;
	}

	@Override
	public int capacity() {
	    return pageCapacity;
	}

	@Override
	public IPage<T> first() {
	    if (hasPrev()) {
		if (model != null && summary != null) {
		    return new EntityQueryPage(model, summary, new PageInfo(0, numberOfPages(), capacity()), binaryTypes);
		} else if (model != null) {
		    return new EntityQueryPage(model, new PageInfo(0, numberOfPages(), capacity()), binaryTypes);
		} else {
		    throw new IllegalStateException("There was no query provided to retrieve the data.");
		}
	    }
	    return null;
	}

	@Override
	public IPage<T> last() {
	    if (hasNext()) {
		if (model != null && summary != null) {
		    return new EntityQueryPage(model, summary, new PageInfo(-1, numberOfPages(), capacity()), binaryTypes);
		} else if (model != null) {
		    return new EntityQueryPage(model, new PageInfo(-1, numberOfPages(), capacity()), binaryTypes);
		} else {
		    throw new IllegalStateException("There was no query provided to retrieve the data.");
		}
	    }
	    return null;
	}

	@Override
	public int numberOfPages() {
	    return numberOfPages;
	}

	public void setNumberOfPages(final int numberOfPages) {
	    this.numberOfPages = numberOfPages;
	}

	@Override
	public String toString() {
	    return "Page " + (no() + 1) + " of " + numberOfPages;
	}
    }
}
