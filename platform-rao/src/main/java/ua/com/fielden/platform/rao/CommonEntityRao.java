package ua.com.fielden.platform.rao;

import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.from;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.zip.GZIPInputStream;

import org.apache.commons.lang.StringUtils;
import org.joda.time.DateTime;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.data.Method;
import org.restlet.data.Status;
import org.restlet.representation.Representation;

import ua.com.fielden.platform.dao.AbstractEntityDao;
import ua.com.fielden.platform.dao.IEntityDao;
import ua.com.fielden.platform.dao.QueryExecutionModel;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.EntityAggregates;
import ua.com.fielden.platform.entity.query.model.AggregatedResultQueryModel;
import ua.com.fielden.platform.entity.query.model.EntityResultQueryModel;
import ua.com.fielden.platform.error.Result;
import ua.com.fielden.platform.pagination.IPage;
import ua.com.fielden.platform.roa.HttpHeaders;
import ua.com.fielden.platform.security.user.User;
import ua.com.fielden.platform.utils.Pair;

/**
 * Base class for all RAO implementations. Provides REST-driven implementation of all {@link IEntityDao} functionality including Entity Query API and pagination.
 * <p>
 * Can be used as is when an ad-hoc instance of RAO is needed and entity type is known.
 *
 * @author TG Team
 *
 * @param <T>
 * @param <K>
 */
public class CommonEntityRao<T extends AbstractEntity<?>> extends AbstractEntityDao<T> {

    protected final RestClientUtil restUtil;

    /** Used to uniquely identify a companion resource. */
    private String coToken;

    public CommonEntityRao(final RestClientUtil restUtil) {
        this.restUtil = restUtil;
    }

    protected WebResourceType getDefaultWebResourceType() {
        return WebResourceType.VERSIONED;
    }

    @Override
    protected boolean getFilterable() {
        return true;
    }

    /**
     * Sends a HEAD request to /entity-type-alias/{entity-id}. The response should contain attribute "exists" with value "true" or "false" to indicate entity existence.
     */
    @Override
    public boolean entityExists(final T entity) {
        if (entity == null || entity.getId() == null) {
            return false;
        }

        return entityExists(entity.getId());
    }

    @Override
    public boolean entityExists(final Long id) {
        if (id == null) {
            return false;
        }
        final String uri = restUtil.getUri(getEntityType(), getDefaultWebResourceType()) + "/" + id;
        final Response response = restUtil.send(new Request(Method.HEAD, uri));
        if (!Status.isSuccess(response.getStatus().getCode())) {
            throw new IllegalStateException(response.getStatus().toString());
        } else if (!StringUtils.isEmpty(restUtil.getHeaderValue(response, HttpHeaders.ERROR))) {
            throw new IllegalStateException(restUtil.getHeaderValue(response, HttpHeaders.ERROR));
        }
        return "true".equalsIgnoreCase(restUtil.getHeaderValue(response, HttpHeaders.EXISTS));
    }

    /**
     * Sends a GET request.
     */
    @Override
    public IPage<T> firstPage(final int pageCapacity) {
        return new EntityQueryPage(0, pageCapacity);
    }

    /**
     * Sends a POST request with {@link QueryExecutionModel} in the envelope.
     */
    @Override
    public IPage<T> firstPage(final QueryExecutionModel<T, ?> model, final int pageCapacity) {
        return new EntityQueryPage(model, new PageInfo(0, 0, pageCapacity));
    }

    @Override
    public T getEntity(final QueryExecutionModel<T, ?> model) {
        final List<T> data = getFirstEntities(model, DEFAULT_PAGE_CAPACITY);
        if (data.size() > 1) {
            throw new IllegalArgumentException("The provided query model leads to retrieval of more than one entity (" + data.size() + ").");
        }
        return data.size() == 1 ? data.get(0) : null;
    }

    /**
     * Sends a POST request with {@link QueryExecutionModel} in the envelope.
     */
    @Override
    public IPage<T> getPage(final QueryExecutionModel<T, ?> model, final int pageNo, final int pageCapacity) {
        return new EntityQueryPage(model, new PageInfo(pageNo, 0, pageCapacity));
    }

    @Override
    public IPage<T> getPage(final QueryExecutionModel<T, ?> model, final int pageNo, final int pageCount, final int pageCapacity) {
        return new EntityQueryPage(model, new PageInfo(pageNo, pageCount, pageCapacity));
    }

    /**
     * Sends a GET request.
     */
    @Override
    public IPage<T> getPage(final int pageNo, final int pageCapacity) {
        return new EntityQueryPage(pageNo, pageCapacity);
    }

    /**
     * Sends either POST or PUT request to update or save an entity respectively. Returns an enhanced entity instance obtained from the response.
     */
    @Override
    public T save(final T entity) {
        // validate entity before attempting to save it
        final Result validationResult = entity.isValid();
        if (!validationResult.isSuccessful()) {
            throw validationResult;
        }
        // create request based on the need to save or update an entity
        final Request request = entity.isPersisted() ? restUtil.newRequest(Method.POST, entity, getDefaultWebResourceType())
                : restUtil.newRequest(Method.PUT, getEntityType(), getDefaultWebResourceType());
        final Representation envelope = restUtil.represent(entity);
        request.setEntity(envelope);
        // perform request and enhance returned entity
        final Result result = restUtil.process(request).getValue();
        if (result.isSuccessful()) {
            return getEntityType().cast(result.getInstance());
        }
        throw result;
    }

    public RestClientUtil getRestUtil() {
        return restUtil;
    }

    /** This is a very dangerous method -- should be used judicially */
    @Override
    public void delete(final T entity) {
        if (entity == null) {
            throw new Result(new IllegalArgumentException("Null is not an acceptable value for an entity instance."));
        }
        if (!entity.isPersisted()) {
            throw new Result(new IllegalArgumentException("Only persisted entity instances can be deleted."));
        }

        final String uri = restUtil.getUri(getEntityType(), getDefaultWebResourceType()) + "/" + entity.getId();
        final Request request = new Request(Method.DELETE, uri);
        final Response response = restUtil.send(request);
        if (!Status.isSuccess(response.getStatus().getCode())) {
            throw new IllegalStateException("Failed with error: " + restUtil.getHeaderValue(response, HttpHeaders.ERROR));
        } else if (!StringUtils.isEmpty(restUtil.getHeaderValue(response, HttpHeaders.ERROR))) {
            throw new IllegalStateException(restUtil.getHeaderValue(response, HttpHeaders.ERROR));
        }
    }

    /**
     * Sends a POST request to /entity-type-alias?page-capacity=pageCapacity&page-no=pageNumber with an envelope containing instance of {@link QueryExecutionModel}. The response
     * suppose to return an envelope containing entities resulting from the query.
     *
     * @param pageNumber
     *            -- numbers from a set of N+{0} indicate a page number to be retrieved; value of -1 (negative one) indicates the need for the last page.
     */
    public List<T> list(final QueryExecutionModel<T, ?> query, final PageInfo pageInfo) {
        query.getQueryModel().setFilterable(true);

        // create request envelope containing Entity Query
        final Representation envelope = restUtil.represent(query);
        // create a request URI containing page capacity and number
        coToken = makeCoToken();
        final String uri = restUtil.getQueryUri(getEntityType(), getDefaultWebResourceType()) + "?page-capacity=" + pageInfo.pageCapacity + "&page-no=" + pageInfo.pageNumber
                + "&page-count=" + pageInfo.numberOfPages + "&co-token=" + coToken;
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

    /** Implements the deletion by query model. Relies on the fact the DAO counterpart has method {@link IEntityDao#delete(EntityResultQueryModel)} implemented. */
    @Override
    public void delete(final EntityResultQueryModel<T> query, final Map<String, Object> params) {
        final Representation envelope = restUtil.represent(from(query).with(params).model());
        // create a request URI containing the deletion flag
        final String uri = restUtil.getQueryUri(getEntityType(), getDefaultWebResourceType()) + "?deletion=true";
        final Request request = new Request(Method.POST, uri, envelope);
        // process request
        final Pair<Response, Result> res = restUtil.process(request);
        final Result result = res.getValue();
        // process result
        if (!result.isSuccessful()) {
            throw result;
        }
    }

    /**
     * Sends a GET request to /entity-type-alias?page-capacity=pageCapacity&page-no=pageNumber with no envelope. The response suppose to return an envelope containing entities
     * resulting from the query.
     *
     * @param pageNumber
     *            -- numbers from a set of N+{0} indicate a page number to be retrieved; value of -1 (negative one) indicates the need for the last page.
     */
    private List<T> list(final EntityQueryPage page, final Integer pageNumber, final Integer pageCapacity) {
        // create a request URI containing page capacity and number
        coToken = makeCoToken();
        final String url = restUtil.getUri(getEntityType(), getDefaultWebResourceType()) + "?page-capacity=" + pageCapacity + "&page-no=" + pageNumber + "&co-token=" + coToken;
        final Request request = restUtil.newRequest(Method.GET, url);
        // process request
        final Pair<Response, Result> res = restUtil.process(request);
        final Response response = res.getKey();
        final Result result = res.getValue();

        if (result.isSuccessful()) {
            // process header values
            final String pageNo = restUtil.getHeaderValue(response, HttpHeaders.PAGE_NO);
            if (!StringUtils.isEmpty(pageNo)) {
                page.setPageNo(Integer.parseInt(pageNo));
            }
            final String numberOfPages = restUtil.getHeaderValue(response, HttpHeaders.PAGES);
            if (!StringUtils.isEmpty(numberOfPages)) {
                page.setNumberOfPages(Integer.parseInt(numberOfPages));
            }
            return (List) result.getInstance();
        } else {
            throw result;
        }
    }

    /**
     * Delegates the request to EntityAggregates RAO to obtain the result of the query. Expects only one EntityAggregates in the result list.
     */
    protected EntityAggregates calcSummary(final QueryExecutionModel<EntityAggregates, AggregatedResultQueryModel> model) {
        final List<EntityAggregates> list = new CommonEntityAggregatesRao(restUtil).getAllEntities(model);
        return list.size() == 1 ? list.get(0) : null;
    }

    @Override
    public int count(final EntityResultQueryModel<T> model) {
        return count(model, Collections.<String, Object> emptyMap());
    }

    @Override
    public int count(final EntityResultQueryModel<T> model, final Map<String, Object> paramValues) {
        return count(from(model).with(paramValues).model());
    }

    protected int count(final QueryExecutionModel<T, ?> model) {
        model.getQueryModel().setFilterable(true);
        // create request envelope containing Entity Query
        final Representation envelope = restUtil.represent(model);
        final String uri = restUtil.getQueryUri(getEntityType(), getDefaultWebResourceType());
        final Request request = new Request(Method.POST, uri, envelope);
        // process request
        final Response response = restUtil.send(request);
        if (!Status.isSuccess(response.getStatus().getCode())) {
            throw new IllegalStateException(response.getStatus().toString());
        }
        return Integer.parseInt(restUtil.getHeaderValue(response, HttpHeaders.COUNT));
    }

    /**
     * A convenient class capturing page stateful information, which is updated and reused when navigating between pages.
     *
     * @author TG Team
     */
    public static class PageInfo {
        int pageNumber;
        int numberOfPages;
        final int pageCapacity;

        public PageInfo(final int pageNumber, final int numberOfPages, final int pageCapacity) {
            this.pageNumber = pageNumber;
            this.numberOfPages = numberOfPages;
            this.pageCapacity = pageCapacity;
        }

    }

    /**
     * Implements pagination based on the provided query.
     * <p>
     * If query is provided then page instantiation results in a POST request to /entity-type-alias/query?page-capacity=pageCapacity&page-no=pageNumber with an envelope containing
     * serialised query.
     * <p>
     * If query is not provided (i.e. it is null) then page instantiation results in a GET method /entity-type-alias?page-capacity=pageCapacity&page-no=pageNumber.
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
        private final EntityAggregates summary;
        private final QueryExecutionModel<EntityAggregates, AggregatedResultQueryModel> summaryModel;

        /** This constructor should be used when summary is not required. */
        public EntityQueryPage(final QueryExecutionModel<T, ?> model, final PageInfo pageInfo) {
            data = list(model, pageInfo);
            pageNumber = pageInfo.pageNumber;
            setNumberOfPages(pageInfo.numberOfPages);
            pageCapacity = pageInfo.pageCapacity;
            this.model = model;
            summaryModel = null;
            summary = null;
        }

        /**
         * This constructor should be used when both data and summary should be retrieved. Passing <code>null</code> for a summary model is handled gracefully.
         */
        public EntityQueryPage(final QueryExecutionModel<T, ?> model, final QueryExecutionModel<EntityAggregates, AggregatedResultQueryModel> summaryModel, final PageInfo pageInfo) {
            data = list(model, pageInfo);
            summary = summaryModel != null ? calcSummary(summaryModel) : null;
            pageNumber = pageInfo.pageNumber;
            setNumberOfPages(pageInfo.numberOfPages);
            pageCapacity = pageInfo.pageCapacity;
            this.model = model;
            this.summaryModel = summaryModel;
        }

        /** This constructor is required purely for navigation implementation in case where summary was calculated and needs to be preserved in another page. */
        private EntityQueryPage(final QueryExecutionModel<T, ?> model, final EntityAggregates summary, final PageInfo pageInfo) {
            data = list(model, pageInfo);
            pageNumber = pageInfo.pageNumber;
            setNumberOfPages(pageInfo.numberOfPages);
            pageCapacity = pageInfo.pageCapacity;
            this.model = model;
            summaryModel = null;
            this.summary = summary;
        }

        /** Should be used when no model is provided. */
        public EntityQueryPage(final int pageNumber, final int pageCapacity) {
            this.pageNumber = pageNumber;
            this.pageCapacity = pageCapacity;
            this.model = null;
            summaryModel = null;
            summary = null;
            data = list(this, pageNumber, pageCapacity);
        }

        @Override
        public T summary() {
            return null;
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
                    return new EntityQueryPage(model, summary, new PageInfo(no() + 1, numberOfPages(), capacity()));
                } else if (model != null) {
                    return new EntityQueryPage(model, new PageInfo(no() + 1, numberOfPages(), capacity()));
                } else {
                    return new EntityQueryPage(no() + 1, capacity());
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
                    return new EntityQueryPage(model, summary, new PageInfo(no() - 1, numberOfPages(), capacity()));
                } else if (model != null) {
                    return new EntityQueryPage(model, new PageInfo(no() - 1, numberOfPages(), capacity()));
                } else {
                    return new EntityQueryPage(no() - 1, capacity());
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
                    return new EntityQueryPage(model, summary, new PageInfo(0, numberOfPages(), capacity()));
                } else if (model != null) {
                    return new EntityQueryPage(model, new PageInfo(0, numberOfPages(), capacity()));
                } else {
                    return new EntityQueryPage(0, capacity());
                }
            }
            return null;
        }

        @Override
        public IPage<T> last() {
            if (hasNext()) {
                if (model != null && summary != null) {
                    return new EntityQueryPage(model, summary, new PageInfo(-1, numberOfPages(), capacity()));
                } else if (model != null) {
                    return new EntityQueryPage(model, new PageInfo(-1, numberOfPages(), capacity()));
                } else {
                    return new EntityQueryPage(-1, capacity());
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

    @Override
    public byte[] export(final QueryExecutionModel<T, ?> query, final String[] propertyNames, final String[] propertyTitles) throws IOException {
        // create request envelope containing Entity Query
        final List<Object> requestContent = new ArrayList<Object>();
        requestContent.add(query);
        requestContent.add(propertyNames);
        requestContent.add(propertyTitles);
        final Representation envelope = restUtil.represent(requestContent);
        // create a request URI containing page capacity and number
        coToken = makeCoToken();
        final String uri = restUtil.getExportUri(getEntityType()) + "?type=excel" + "&co-token=" + coToken;
        ;
        // send request
        final Response response = restUtil.send(new Request(Method.POST, uri, envelope));
        if (!Status.isSuccess(response.getStatus().getCode())) {
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

    @Override
    public List<T> getAllEntities(final QueryExecutionModel<T, ?> query) {
        // create request envelope containing Entity Query
        final Representation envelope = restUtil.represent(query);
        // create a request URI containing page capacity and number
        coToken = makeCoToken();
        final String uri = restUtil.getQueryUri(getEntityType(), getDefaultWebResourceType()) + "?page-capacity=all" + "&co-token=" + coToken;
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

    @Override
    public List<T> getFirstEntities(final QueryExecutionModel<T, ?> query, final int numberOfEntities) {
        // create request envelope containing Entity Query
        final Representation envelope = restUtil.represent(query);
        // create a request URI containing page capacity and number
        coToken = makeCoToken();
        final String uri = restUtil.getQueryUri(getEntityType(), getDefaultWebResourceType()) + "?page-capacity=" + numberOfEntities + "&page-no=first" + "&co-token=" + coToken;
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

    @Override
    public boolean isStale(final Long entityId, final Long version) {
        if (entityId == null) {
            return false;
        }
        if (version == null) {
            throw new IllegalArgumentException("Value version should not be null.");
        }

        final String uri = restUtil.getUri(getEntityType(), entityId, getDefaultWebResourceType()) + "?version=" + version;

        final Response response = restUtil.send(restUtil.newRequest(Method.HEAD, uri));
        if (!Status.isSuccess(response.getStatus().getCode())) {
            throw new IllegalStateException(response.getStatus().toString());
        } else if (!StringUtils.isEmpty(restUtil.getHeaderValue(response, HttpHeaders.ERROR))) {
            throw new IllegalStateException(restUtil.getHeaderValue(response, HttpHeaders.ERROR));
        }
        return "true".equalsIgnoreCase(restUtil.getHeaderValue(response, HttpHeaders.STALE));
    }

    @Override
    public String getUsername() {
        return restUtil.getUsername();
    }

    @Override
    public User getUser() {
        return restUtil.getUser();
    }

    @Override
    public boolean stop() {
        final String uri = restUtil.getBaseUri(getDefaultWebResourceType()) + "/companions/" + coToken;
        final Request request = new Request(Method.POST, uri);
        try {
            final Pair<Response, Result> res = restUtil.process(request);
            return true;
        } catch (final Exception ex) {
            ex.printStackTrace();
            return false;
        }
    }

    @Override
    public Integer progress() {
        return null;
    }

    /**
     * Generates unique for a user token intended to register a companion web resource at the server end.
     *
     * @return
     */
    private String makeCoToken() {
        return new DateTime().getMillis() + "";
    }

}