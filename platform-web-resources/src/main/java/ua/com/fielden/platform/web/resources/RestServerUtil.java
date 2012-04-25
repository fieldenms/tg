package ua.com.fielden.platform.web.resources;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.joda.time.DateTime;
import org.restlet.data.Form;
import org.restlet.data.MediaType;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.resource.InputRepresentation;
import org.restlet.resource.Representation;

import ua.com.fielden.platform.dao.QueryExecutionModel;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.DynamicallyTypedQueryContainer;
import ua.com.fielden.platform.equery.lifecycle.LifecycleModel;
import ua.com.fielden.platform.error.Result;
import ua.com.fielden.platform.roa.HttpHeaders;
import ua.com.fielden.platform.serialisation.api.ISerialiser;
import ua.com.fielden.platform.snappy.SnappyQuery;

/**
 * This is a convenience class providing some common routines used in the
 * implementation of web-resources.
 *
 * @author TG Team
 *
 */
public class RestServerUtil {

    private final ISerialiser serialiser;
    /** An application wide public key */
    private String appWidePublicKey;
    /** An application wide private key */
    private String appWidePrivateKey;

    public RestServerUtil(final ISerialiser serialiser) {
	this.serialiser = serialiser;
    }


    private final Logger logger = Logger.getLogger(RestServerUtil.class);
    /**
     * Creates a response header entry.
     *
     * @param response
     * @param headerEntry
     * @param value
     */
    public void setHeaderEntry(final Response response, final HttpHeaders headerEntry, final String value) {
	Form responseHeaders = (Form) response.getAttributes().get("org.restlet.http.headers");
	if (responseHeaders == null) {
	    responseHeaders = new Form();
	    response.getAttributes().put("org.restlet.http.headers", responseHeaders);
	}
	responseHeaders.add(headerEntry.value, value);
    }

    /**
     * Returns a header entry value if present. Otherwise, null.
     *
     * @param response
     * @param headerEntry
     * @return
     */
    public String getHeaderValue(final Request request, final HttpHeaders headerEntry) {
	final Form header = (Form) request.getAttributes().get("org.restlet.http.headers");
	return header != null && header.getFirst(headerEntry.value) != null ? header.getFirst(headerEntry.value).getValue() : null;
    }

    /**
     * Creates a representation of {@link Result} reporting a cause of some
     * error that could have occurred during request processing.
     *
     * @param string
     * @return
     */
    public Representation errorRepresentation(final String string) {
	final byte[] bytes = serialiser.serialise(new Result(null, new Exception(string)));
	return new InputRepresentation(new ByteArrayInputStream(bytes), MediaType.APPLICATION_OCTET_STREAM);
    }

    /**
     * Creates a representation of {@link Result} reporting a cause of some
     * error that could have occurred during request processing.
     *
     * @param string
     * @return
     */
    public Representation errorRepresentation(final Exception ex) {
	final byte[] bytes = serialiser.serialise(new Result(ex));
	return new InputRepresentation(new ByteArrayInputStream(bytes), MediaType.APPLICATION_OCTET_STREAM);
    }

    /**
     * Creates a representation of {@link Result}.
     *
     * @param string
     * @return
     */
    public Representation resultRepresentation(final Result result) {
	final byte[] bytes = serialiser.serialise(result);
	return new InputRepresentation(new ByteArrayInputStream(bytes), MediaType.APPLICATION_OCTET_STREAM);
    }

    /**
     * Creates a representation of {@link Result}.
     *
     * @param string
     * @return
     */
    public Representation snappyResultRepresentation(final List filteredEntities) {
	logger.debug("Start building snappy result representation:" + new DateTime());
	try {
	    // create a Result enclosing entity list
	    final byte[] bytes = serialiser.serialise(new Result(new ArrayList(filteredEntities), "Snappy pair is Ok"));
	    logger.debug("SIZE: " + bytes.length);
	    return new InputRepresentation(new ByteArrayInputStream(bytes), MediaType.APPLICATION_OCTET_STREAM);
	} catch (final Exception ex) {
	    logger.error(ex);
	    return errorRepresentation("The following error occurred during request processing:\n" + ex.getMessage());
	}
    }

    /**
     * Composes representation of a list of entities.
     *
     * @return
     */
    public <T extends AbstractEntity> Representation listRepresentation(final List<T> entities) {
	logger.debug("Start building representation:" + new DateTime());
	try {
	    // create a Result enclosing entity list
	    final Result result = new Result(new ArrayList<T>(entities), "All is cool");
	    final byte[] bytes = serialiser.serialise(result);
	    logger.debug("SIZE: " + bytes.length);
	    return new InputRepresentation(new ByteArrayInputStream(bytes), MediaType.APPLICATION_OCTET_STREAM);
	} catch (final Exception ex) {
	    logger.error(ex);
	    return errorRepresentation("The following error occurred during request processing:\n" + ex.getMessage());
	}
    }

    /**
     * Composes representation of a lifecycle data.
     *
     * @return
     */
    public <T extends AbstractEntity> Representation lifecycleRepresentation(final LifecycleModel<T> lifecycleModel) {
	logger.debug("Start building lifecycle representation:" + new DateTime());
	try {
	    // create a Result enclosing lifecycle data
	    final Result result = new Result(lifecycleModel, "All is cool");
	    final byte[] bytes = serialiser.serialise(result);
	    logger.debug("SIZE: " + bytes.length);
	    return new InputRepresentation(new ByteArrayInputStream(bytes), MediaType.APPLICATION_OCTET_STREAM);
	} catch (final Exception ex) {
	    logger.error(ex);
	    return errorRepresentation("The following error occurred during request processing:\n" + ex.getMessage());
	}
    }

    /**
     * Composes representation of an entity.
     *
     * @return
     */
    public <T extends AbstractEntity> Representation singleRepresentation(final T entity) {
	try {
	    // create a Result enclosing entity list
	    final Result result = entity != null ? new Result(entity, "OK") : new Result(null, new Exception("Could not find entity."));
	    final byte[] bytes = serialiser.serialise(result);
	    return new InputRepresentation(new ByteArrayInputStream(bytes), MediaType.APPLICATION_OCTET_STREAM);
	} catch (final Exception ex) {
	    logger.error(ex);
	    return errorRepresentation("The following error occurred during request processing:\n" + ex.getMessage());
	}
    }

    /**
     * Converts representation of {@link QueryExecutionModel} to an actual instance.
     *
     * @param representation
     * @return
     * @throws Exception
     */
    public QueryExecutionModel<?, ?> restoreQueryExecutionModel(final Representation representation) throws Exception {
	return serialiser.deserialise(representation.getStream(), QueryExecutionModel.class);
    }

    /**
     * Converts representation of {@link DynamicallyTypedQueryContainer} to an instance of {@link QueryExecutionModel}.
     *
     * @param <T>
     * @param representation
     * @return
     * @throws Exception
     */
    public QueryExecutionModel<?, ?> restoreDynamicQueryExecutionModel(final Representation representation) throws Exception {
	return serialiser.deserialise(representation.getStream(), DynamicallyTypedQueryContainer.class).getQem();
    }

    public SnappyQuery restoreSnappyQuery(final Representation representation) throws Exception {
	return serialiser.deserialise(representation.getStream(), SnappyQuery.class);
    }

    /**
     * Converts representation of the export request representation in to a
     * list.
     *
     * @param representation
     * @return
     * @throws Exception
     */
    public List<?> restoreList(final Representation representation) throws Exception {
	return serialiser.deserialise(representation.getStream(), List.class);
    }

    /**
     * Converts representation of an entity to an actual instance.
     *
     * @param <T>
     * @param <K>
     * @param representation
     * @param type
     * @return
     * @throws Exception
     */
    public <T extends AbstractEntity> T restoreEntity(final Representation representation, final Class<T> type) throws Exception {
	return serialiser.deserialise(representation.getStream(), type);
    }

    /**
     * Deserialises representation into a special map.
     *
     * @param representation
     * @return
     * @throws Exception
     */
    public Map<String, List<Long>> restoreMap(final Representation representation) throws Exception {
	return serialiser.deserialise(representation.getStream(), Map.class);
    }

    public String getAppWidePublicKey() {
	return appWidePublicKey;
    }

    public void setAppWidePublicKey(final String publicKey) {
	this.appWidePublicKey = publicKey;
    }

    public String getAppWidePrivateKey() {
	return appWidePrivateKey;
    }

    public void setAppWidePrivateKey(final String privateKey) {
	this.appWidePrivateKey = privateKey;
    }

    public ISerialiser getSerialiser() {
	return serialiser;
    }

}
