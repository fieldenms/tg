package ua.com.fielden.platform.rao;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.zip.GZIPInputStream;

import org.restlet.data.Method;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.resource.Representation;

import ua.com.fielden.platform.dao.QueryExecutionModel;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.model.EntityResultQueryModel;
import ua.com.fielden.platform.error.Result;
import ua.com.fielden.platform.pagination.IPage;
import ua.com.fielden.platform.reflection.AnnotationReflector;
import ua.com.fielden.platform.swing.review.annotations.EntityType;
import ua.com.fielden.platform.utils.Pair;

import com.google.inject.Inject;

/**
 * This RAO is applicable for executing queries based on dynamically generated entity types.
 *
 * @author TG Team
 *
 */
@EntityType(AbstractEntity.class)
public class GeneratedEntityRao extends CommonEntityRao {

    private Class<? extends AbstractEntity> entityType;
    private Class<? extends Comparable> keyType;

    /**
     * Needed for reflective instantiation.
     */
    @Inject
    public GeneratedEntityRao(final RestClientUtil util) {
	super(util);
    }

    public void setEntityType(final Class<? extends AbstractEntity> type) {
	this.entityType = type;
	this.keyType = AnnotationReflector.getKeyType(entityType);
    }

    @Override
    public Class<? extends AbstractEntity> getEntityType() {
	return entityType;
    }

    @Override
    public Class<? extends Comparable> getKeyType() {
	return keyType;
    }


    @Override
    public List getAllEntities(final QueryExecutionModel query) {
	// create request envelope containing Entity Query
	final Representation envelope = restUtil.represent(query);
	// create a request URI containing page capacity and number
	final String uri = restUtil.getQueryUri(getEntityType(), getDefaultWebResourceType()) + "?page-capacity=all";
	final Request request = new Request(Method.POST, uri, envelope);
	// process request
	final Pair<Response, Result> res = restUtil.process(request);
	final Result result = res.getValue();
	// process result
	if (result.isSuccessful()) {
	    return (List) result.getInstance();
	} else {
	    throw result;
	}
    }



    @Override
    public byte[] export(final QueryExecutionModel query, final String[] propertyNames, final String[] propertyTitles) throws IOException {
	// create request envelope containing Entity Query
	final List<Object> requestContent = new ArrayList<Object>();
	requestContent.add(query);
	requestContent.add(propertyNames);
	requestContent.add(propertyTitles);
	final Representation envelope = restUtil.represent(requestContent);
	// create a request URI containing page capacity and number
	final String uri = restUtil.getExportUri(getEntityType()) + "?type=excel";
	// send request
	final Response response = restUtil.send(new Request(Method.POST, uri, envelope));
	if (!Status.SUCCESS_OK.equals(response.getStatus())) {
	    throw new IllegalStateException(response.getStatus().toString());
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
     * Implements pagination based on the provided query for either generated or coded entity type.
     *
     * @author TG Team
     *
     */
    private class EntityQueryPage implements IPage<AbstractEntity<?>> {
	private int pageNumber; // zero-based
	private int numberOfPages = 0;
	private final int pageCapacity;
	private final List<AbstractEntity<?>> data;
	private final QueryExecutionModel<?, ?> model;
	private final AbstractEntity<?> summary;
	private final QueryExecutionModel<?, ?> summaryModel;

	/** This constructor should be used when summary is not required. */
	public EntityQueryPage(final QueryExecutionModel<?, ?> model, final PageInfo pageInfo) {
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
	public EntityQueryPage(final QueryExecutionModel<?, ?> model, final QueryExecutionModel<?, ?> summaryModel, final PageInfo pageInfo) {
	    data = list(model, pageInfo);
	    summary = summaryModel != null ? calcSummary(summaryModel) : null;
	    pageNumber = pageInfo.pageNumber;
	    setNumberOfPages(pageInfo.numberOfPages);
	    pageCapacity = pageInfo.pageCapacity;
	    this.model = model;
	    this.summaryModel = summaryModel;
	}

	/** This constructor is required purely for navigation implementation in case where summary was calculated and needs to be preserved in another page. */
	private EntityQueryPage(final QueryExecutionModel<?, ?> model, final AbstractEntity summary, final PageInfo pageInfo) {
	    data = list(model, pageInfo);
	    pageNumber = pageInfo.pageNumber;
	    setNumberOfPages(pageInfo.numberOfPages);
	    pageCapacity = pageInfo.pageCapacity;
	    this.model = model;
	    summaryModel = null;
	    this.summary = summary;
	}

	@Override
	public AbstractEntity<?> summary() {
	    return summary;
	}

	public void setPageNo(final int pageNo) {
	    pageNumber = pageNo;
	}

	@Override
	public List<AbstractEntity<?>> data() {
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
	public IPage<AbstractEntity<?>> next() {
	    if (hasNext()) {
		if (model != null && summary != null) {
		    return new EntityQueryPage(model, summary, new PageInfo(no() + 1, numberOfPages(), capacity()));
		} else if (model != null) {
		    return new EntityQueryPage(model, new PageInfo(no() + 1, numberOfPages(), capacity()));
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
	public IPage<AbstractEntity<?>> prev() {
	    if (hasPrev()) {
		if (model != null && summary != null) {
		    return new EntityQueryPage(model, summary, new PageInfo(no() - 1, numberOfPages(), capacity()));
		} else if (model != null) {
		    return new EntityQueryPage(model, new PageInfo(no() - 1, numberOfPages(), capacity()));
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
	public IPage<AbstractEntity<?>> first() {
	    if (hasPrev()) {
		if (model != null && summary != null) {
		    return new EntityQueryPage(model, summary, new PageInfo(0, numberOfPages(), capacity()));
		} else if (model != null) {
		    return new EntityQueryPage(model, new PageInfo(0, numberOfPages(), capacity()));
		} else {
		    throw new IllegalStateException("There was no query provided to retrieve the data.");
		}
	    }
	    return null;
	}

	@Override
	public IPage<AbstractEntity<?>> last() {
	    if (hasNext()) {
		if (model != null && summary != null) {
		    return new EntityQueryPage(model, summary, new PageInfo(-1, numberOfPages(), capacity()));
		} else if (model != null) {
		    return new EntityQueryPage(model, new PageInfo(-1, numberOfPages(), capacity()));
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
























    @Override
    public IPage firstPage(final int pageCapacity) {
	throw new UnsupportedOperationException("Generated type do not have a default model. This exception suggests a programming mistake.");
    }

    @Override
    public IPage getPage(final int pageNo, final int pageCapacity) {
	throw new UnsupportedOperationException("Generated type do not have a default model. This exception suggests a programming mistake.");
    }

    @Override
    public boolean entityExists(final AbstractEntity entity) {
	throw new UnsupportedOperationException("");
    }

    @Override
    public boolean entityExists(final Long id) {
	throw new UnsupportedOperationException("");
    }

    @Override
    public AbstractEntity save(final AbstractEntity entity) {
	throw new UnsupportedOperationException("");
    }

    @Override
    public void delete(final AbstractEntity entity) {
	throw new UnsupportedOperationException("");
    }

    @Override
    public void delete(final EntityResultQueryModel model) {
	throw new UnsupportedOperationException("");
    }

    @Override
    public void delete(final EntityResultQueryModel query, final Map params) {
	throw new UnsupportedOperationException("");
    }
}
