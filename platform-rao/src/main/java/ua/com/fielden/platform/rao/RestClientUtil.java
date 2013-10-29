package ua.com.fielden.platform.rao;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentMap;
import java.util.zip.GZIPInputStream;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.joda.time.DateTime;
import org.joda.time.Period;
import org.restlet.Client;
import org.restlet.Message;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.data.Form;
import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.data.Protocol;
import org.restlet.data.Status;
import org.restlet.engine.header.Header;
import org.restlet.representation.InputRepresentation;
import org.restlet.representation.Representation;
import org.restlet.util.Series;

import ua.com.fielden.platform.attachment.Attachment;
import ua.com.fielden.platform.cypher.Cypher;
import ua.com.fielden.platform.dao.QueryExecutionModel;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.DynamicallyTypedQueryContainer;
import ua.com.fielden.platform.entity.query.model.EntityResultQueryModel;
import ua.com.fielden.platform.equery.lifecycle.LifecycleQueryContainer;
import ua.com.fielden.platform.error.Result;
import ua.com.fielden.platform.reflection.asm.impl.DynamicEntityClassLoader;
import ua.com.fielden.platform.reflection.asm.impl.DynamicTypeNamingService;
import ua.com.fielden.platform.roa.HttpHeaders;
import ua.com.fielden.platform.security.provider.IUserController;
import ua.com.fielden.platform.security.user.IUserProvider;
import ua.com.fielden.platform.security.user.User;
import ua.com.fielden.platform.serialisation.api.ISerialiser;
import ua.com.fielden.platform.snappy.SnappyQuery;
import ua.com.fielden.platform.utils.Pair;

/**
 * <code>RestClientUtil</code> provides a HTTP communication configuration point. In most cases there should only be one instance of <code>RestClientUtil</code> for the whole
 * application.
 * <p>
 * Provides methods for composing HTTP URIs based on entity type, instance and property name, and also methods for building/restoring relevant envelope representation.
 * <p>
 * There are two predefined parts of the URI that gets composed:
 * <ul>
 * <li>Root URI (getRootUri()) -- http(s)://{hostname}:{port}/{version}
 * <li>Base URI (getBaseUri()) -- http(s)://{hostname}:{port}/{version}/users/{user}
 * </ul>
 *
 * @author TG Team
 *
 */
public final class RestClientUtil implements IUserProvider {

    private final Logger logger = Logger.getLogger(RestClientUtil.class);

    private final Protocol protocol;
    private final String host;
    private final int port;
    private final String version;
    private String username;
    private final String rootUri;
    private final String systemUri;
    private ISerialiser serialiser;

    /** This is a user specific private key -- not to be confused with application wide key pair. */
    private String privateKey;

    /** User controller is required to be able to retrieve user by it name to ensure correct user association upon username change. */
    private IUserController userController;
    private User user;

    public RestClientUtil(final Protocol protocol, final String host, final int port, final String version, final String user) {
	this.protocol = protocol;
	this.host = host;
	this.port = port;
	this.version = version;
	this.username = user;
	rootUri = protocol.getName().toLowerCase() + "://" + host + ":" + port + "/" + version;
	systemUri = protocol.getName().toLowerCase() + "://" + host + ":" + port + "/system";
    }

    public boolean isUserConfigured() {
	return !StringUtils.isEmpty(username) && !StringUtils.isEmpty(privateKey);
    }

    public void initSerialiser(final ISerialiser serialiser) {
	this.serialiser = serialiser;
    }

    /**
     * Sends a HEAD request to the base URI. Usually should be used only once at the start of the client application in order to kick-off the server app if it was not yet started.
     * The cost of running HEAD is extremely cheap.
     */
    public void warmUp() {
	final Response response = send(newRequest(Method.HEAD, getSystemUri() + "/info"));
	if (!Status.SUCCESS_OK.equals(response.getStatus())) {
	    throw new IllegalStateException(response.getStatus().toString());
	}
    }

    public String getHost() {
	return host;
    }

    public int getPort() {
	return port;
    }

    public String getVersion() {
	return version;
    }

    public String getUsername() {
	return username;
    }

    public String getBaseUri(final WebResourceType rt) {
	if (StringUtils.isEmpty(username)) {
	    throw new Result(new IllegalStateException("Cannot compose base URI without a user name."));
	}

	return getRootUri(rt) + "/users/" + username;
    }

    /**
     * Composes a complete URI based on the provided entity type.
     *
     * @param type
     * @return
     */
    public String getUri(final Class<? extends AbstractEntity> type, final WebResourceType rt) {
	if (type.getName().contains(DynamicTypeNamingService.APPENDIX)) {
	    throw new IllegalArgumentException("Dynamically generated types do not have explicitly registered web resources.");
	}
	return getBaseUri(rt) + "/" + type.getSimpleName();
    }

    /**
     * Composes a complete URI for entity aggregate request.
     *
     * @return
     */
    public String getUriForAggregates(final WebResourceType rt) {
	return getBaseUri(rt) + "/entity-aggregates";
    }

    /**
     * Constructs a URI for EQL requests. A distinction is made when constructing URI for coded entity types and generated ones.
     *
     * @param type
     *            -- could be a type of either coded or generated entity type
     * @param rt
     * @return
     */
    public String getQueryUri(final Class<? extends AbstractEntity> type, final WebResourceType rt) {
	if (type.getName().contains(DynamicTypeNamingService.APPENDIX)) {
	    return getBaseUri(rt) + "/query/generated-type/" + DynamicEntityClassLoader.getOriginalType(type).getName();
	} else {
	    return getBaseUri(rt) + "/query/" + type.getSimpleName();
	}
    }

    /**
     * Constructs a URI for data export requests. A distinction is made when constructing URI for coded entity types and generated ones.
     *
     * @param type
     * @return
     */
    public String getExportUri(final Class<? extends AbstractEntity> type) {
	if (type.getName().contains(DynamicTypeNamingService.APPENDIX)) {
	    return getBaseUri(WebResourceType.VERSIONED) + "/export/generated-type/" + DynamicEntityClassLoader.getOriginalType(type).getName();
	} else {
	    return getBaseUri(WebResourceType.VERSIONED) + "/export/" + type.getSimpleName();
	}
    }

    public String getSnappyQueryUri() {
	return getBaseUri(WebResourceType.VERSIONED) + "/snappyquery";
    }

    public String getLifecycleUri(final Class<? extends AbstractEntity> type) {
	return getBaseUri(WebResourceType.VERSIONED) + "/lifecycle/" + type.getSimpleName();
    }

    public String getDownloadAttachmentUri(final Long id) {
	return getBaseUri(WebResourceType.VERSIONED) + "/download/" + Attachment.class.getSimpleName() + "/" + id;
    }

    public String getReportUri() {
	return getBaseUri(WebResourceType.VERSIONED) + "/report";
    }

    /**
     * Composes a complete URI based on the provided entity type and id.
     *
     * @param type
     * @return
     */
    public String getUri(final Class<? extends AbstractEntity> type, final Long id, final WebResourceType rt) {
	return getBaseUri(rt) + "/" + type.getSimpleName() + "/" + id;
    }

    /**
     * Composes a complete URI based on the provided entity instance.
     *
     * @param type
     * @return
     */
    public String getUri(final AbstractEntity<?> entity, final WebResourceType rt) {
	return getBaseUri(rt) + "/" + entity.getType().getSimpleName() + "/" + entity.getId();
    }

    /**
     * Composes a complete URI based on the property for provided entity instance.
     *
     * @param type
     * @return
     */
    public String getUri(final AbstractEntity<?> entity, final String propertyName, final WebResourceType rt) {
	return getBaseUri(rt) + "/" + entity.getType().getSimpleName() + "/" + entity.getId() + "/" + propertyName;
    }

    public Protocol getProtocol() {
	return protocol;
    }

    public Request newRequest(final Method method, final Class<? extends AbstractEntity> type, final WebResourceType rt) {
	return new Request(method, getUri(type, rt));
    }

    public Request newRequest(final Method method, final AbstractEntity<?> entity, final WebResourceType rt) {
	return new Request(method, getUri(entity, rt));
    }

    public Request newRequest(final Method method, final String uri) {
	return new Request(method, uri);
    }

    /**
     * Send the provided request and return a response.
     *
     * @param request
     * @return
     * @throws Exception
     */
    public Response send(final Request request) throws Result {
	if (!StringUtils.isEmpty(getPrivateKey())) {
	    try {
		final String token = createSecurityToken(request, getPrivateKey());
		setChallengeResponse(request, token);
	    } catch (final Exception e) {
		throw new Result(e);
	    }
	}
	final Client client = new Client(getProtocol());
	return client.handle(request);
    }

    /**
     * Sends a request with a challenge based on the provided token.
     *
     * @param request
     * @param token
     * @return
     * @throws Exception
     */
    public Response send(final Request request, final String token) throws Exception {
	setChallengeResponse(request, token);
	return new Client(getProtocol()).handle(request);
    }

    /**
     * Creates a security token as {username}::{URI}, where {URI} part is encoded with the provided private key. The URI is obtained from the passed in request.
     *
     * @param request
     * @param privateKey
     * @return
     * @throws Exception
     */
    protected String createSecurityToken(final Request request, final String privateKey) throws Exception {
	// prepend the user name to complete security token creation
	return username + "::" + new Cypher().encrypt(request.getResourceRef().toString(), privateKey);
    }

    /**
     * Sets a provide security token for the request.
     *
     * @param request
     * @param token
     * @throws Exception
     */
    protected void setChallengeResponse(final Request request, final String token) throws Exception {
	getMessageHeaders(request).add(HttpHeaders.AUTHENTICATION.value, token);
    }

    /**
     * Executes the provided request using {@link #send(Request)} and interprets an envelope returned by response.
     *
     * Response envelope is always a serialised instance of {@link Result} bearing all necessary information such as an entity instance or list of entities and/or error
     * information.
     *
     * @param request
     * @return
     * @throws Exception
     */
    public Pair<Response, Result> process(final Request request) {
	if (request.getMethod() != Method.POST && request.getMethod() != Method.PUT && request.getMethod() != Method.GET) {
	    throw new IllegalArgumentException("Processing of requests with method " + request.getMethod().getName() + " is not supported.");
	}

	final DateTime requestInitiated = new DateTime();
	final Response response = send(request);

	if (!Status.SUCCESS_OK.equals(response.getStatus()) && !Status.CLIENT_ERROR_CONFLICT.equals(response.getStatus())
		&& !Status.CLIENT_ERROR_UNAUTHORIZED.equals(response.getStatus())) {
	    throw new IllegalStateException(response.getStatus().toString());
	}

	final Period duration = new Period(requestInitiated, new DateTime());
	logger.debug("Request/response duration: " + duration.getMinutes() + ":" + duration.getSeconds() + ":" + duration.getMillis());

	final Result res = process(response);
	return new Pair<Response, Result>(response, res);
    }

    /**
     * De-serialises response to an instance of Result.
     *
     * @param response
     * @return
     */
    public Result process(final Response response) {
	logger.debug("Start response envelop processing...");
	final DateTime startEnvelopeConverion = new DateTime();
	try {

	    final DateTime st = new DateTime();
	    final InputStream stream = response.getEntity().getStream();
	    final Result res = process(stream);
	    final Period pd = new Period(st, new DateTime());
	    logger.debug("Read and deserialising:" + pd.getMinutes() + ":" + pd.getSeconds() + ":" + pd.getMillis());
	    return res;
	} catch (final Exception e) {
	    e.printStackTrace();
	    logger.error("Failed to process response from request to " + response.getLocationRef().getPath() + ":" + e.getMessage(), e);
	    throw new EResponceProcessing("Failed to process response from request to " + response.getLocationRef().getPath() + ":" + e.getMessage(), e);
	} finally {
	    final Period pd = new Period(startEnvelopeConverion, new DateTime());
	    logger.debug("Finish response envelope processing: " + pd.getMinutes() + ":" + pd.getSeconds() + ":" + pd.getMillis());
	}
    }

    /**
     * Processing an input stream expecting a zipped application octet stream carrying an instance of {@link Result}.
     *
     * @param stream
     * @return
     */
    public Result process(final InputStream stream) {
	try {
	    return serialiser.deserialise(stream, Result.class);
	} catch (final Exception e) {
	    throw new IllegalStateException("Could not to process input stream.", e);
	}
    }

    /**
     * Save content of the input stream into the specified file.
     *
     * @param content
     * @param path
     * @throws Exception
     */
    protected void saveToFile(final InputStream content, final String path) throws Exception {
	final GZIPInputStream stream = new GZIPInputStream(content);
	final File file = new File(path);
	if (!file.exists()) {
	    file.createNewFile();
	}
	final FileOutputStream fo = new FileOutputStream(file);
	int i = stream.read();
	while (i != -1) {
	    fo.write(i);
	    i = stream.read();
	}
	fo.flush();
	stream.close();
	fo.close();
    }

    /**
     * Produces {@link EntityQuery} representation, which can be used as a request envelope.
     *
     * @param <T>
     * @param <K>
     * @param query
     * @return
     */
    public <T extends AbstractEntity<?>> Representation represent(final QueryExecutionModel<T, ?> query) {
	// IMPORTANT: One should be very careful when composing queries not to set any of the parameters as entity instances.
	// Doing so, causes two issues:
	// 1. Presentation, and thus the request envelope, grows significantly in size.
	// 2. Conversion of entities into XML (serialisation) is a time consuming operation;
	query.setLightweight(true);
	final byte[] bytes = serialiser.serialise(query);
	return new InputRepresentation(new ByteArrayInputStream(bytes), MediaType.APPLICATION_OCTET_STREAM, bytes.length);
    }

    /**
     * Produces {@link DynamicallyTypedQueryContainer} representation, which can be used as a request envelope.
     *
     * @param query
     * @param dynamicTypes
     * @return
     */
    public Representation represent(final QueryExecutionModel<?, ?> query, final List<byte[]> dynamicTypes) {
	query.setLightweight(true);

	final DynamicallyTypedQueryContainer container = new DynamicallyTypedQueryContainer(dynamicTypes, query);

	final byte[] bytes = serialiser.serialise(container);
	return new InputRepresentation(new ByteArrayInputStream(bytes), MediaType.APPLICATION_OCTET_STREAM, bytes.length);
    }

    /**
     * Produces {@link LifecycleQueryContainer} representation, which can be used as a request envelop.
     *
     * @param model
     * @param binaryTypes
     * @param propertyName
     * @param from
     * @param to
     * @return
     */
    public Representation represent(final EntityResultQueryModel<? extends AbstractEntity<?>> model, final List<byte[]> binaryTypes, final List<String> distributionProperties, final String propertyName, final DateTime from, final DateTime to) {

	final LifecycleQueryContainer container = new LifecycleQueryContainer(model, binaryTypes, distributionProperties, propertyName, from, to);

	final byte[] bytes = serialiser.serialise(container);
	return new InputRepresentation(new ByteArrayInputStream(bytes), MediaType.APPLICATION_OCTET_STREAM, bytes.length);
    }

    /**
     * Produces {@link SnappyQuery} representation, which can be used as a request envelope.
     *
     * @param <T>
     * @param <K>
     * @param query
     * @return
     */
    public Representation represent(final SnappyQuery query) {
	final byte[] bytes = serialiser.serialise(query);
	return new InputRepresentation(new ByteArrayInputStream(bytes), MediaType.APPLICATION_OCTET_STREAM, bytes.length);
    }

    /**
     * A convenient method for preparing a representation of a specialised map to be included as part of a request envelope.
     *
     * @param map
     * @return
     */
    public Representation represent(final Map<?, ?> map) {
	final byte[] bytes = serialiser.serialise(map);
	return new InputRepresentation(new ByteArrayInputStream(bytes), MediaType.APPLICATION_OCTET_STREAM, bytes.length);
    }

    /**
     * A convenient method for preparing a representation of a list of arbitrary instances.
     *
     * @param list
     * @return
     */
    public Representation represent(final List<?> list) {
	// need to ensure that elements of type IQueryOrderedModel are provided with additional attributes
	for (final Object el : list) {
	    if (el instanceof QueryExecutionModel) {
		((QueryExecutionModel<?, ?>) el).setLightweight(true);
	    }
	}
	// now serialise and make a representation
	final byte[] bytes = serialiser.serialise(list);
	return new InputRepresentation(new ByteArrayInputStream(bytes), MediaType.APPLICATION_OCTET_STREAM, bytes.length);
    }

    /**
     * Produces entity representation. Takes care of stripping off the instance prior to serialisation.
     *
     * @param <T>
     * @param entity
     * @return
     */
    public <T extends AbstractEntity> Representation represent(final T entity) {
	final byte[] bytes = serialiser.serialise(entity);
	return new InputRepresentation(new ByteArrayInputStream(bytes), MediaType.APPLICATION_OCTET_STREAM, bytes.length);
    }

    /**
     * Method to check if {@link Response} header contains an entry with the given name and value.
     *
     * @param response
     * @param headerEntry
     * @param value
     * @return
     */
    public boolean hasHeaderValue(final Response response, final HttpHeaders headerEntry, final String value) {
	final Form header = (Form) response.getAttributes().get("org.restlet.http.headers");
	return header != null && header.getFirst(headerEntry.value) != null && value.equals(header.getFirst(headerEntry.value).getValue());
    }


    private static final String HEADERS_KEY = "org.restlet.http.headers";

    private static Series<Header> getMessageHeaders(final Message message) {
	final ConcurrentMap<String, Object> attrs = message.getAttributes();
	Series<Header> headers = (Series<Header>) attrs.get(HEADERS_KEY);
	if (headers == null) {
	    headers = new Series<Header>(Header.class);
	    final Series<Header> prev = (Series<Header>) attrs.putIfAbsent(HEADERS_KEY, headers);
	    if (prev != null) { headers = prev; }
	}
	return headers;
    }

    /**
     * Creates a response header entry.
     *
     * @param response
     * @param headerEntry
     * @param value
     */
    public void setHeaderEntry(final Request request, final HttpHeaders headerEntry, final String value) {
	getMessageHeaders(request).add(headerEntry.value, value);
    }

    /**
     * Returns a header entry value if present. Otherwise, null.
     *
     * @param response
     * @param headerEntry
     * @return
     */
    public String getHeaderValue(final Response response, final HttpHeaders headerEntry) {
	final Series<Header> header = getMessageHeaders(response);
	return header!= null && header.getFirst(headerEntry.value) != null ? header.getFirst(headerEntry.value).getValue() : null;
    }

    public ISerialiser getSerialiser() {
	return serialiser;
    }

    public String getRootUri(final WebResourceType rt) {
	return rt == WebResourceType.VERSIONED ? rootUri : systemUri;
    }

    public String getPrivateKey() {
	return privateKey;
    }

    /**
     * Assigns private key to be used for authentication of HTTP communication. Null value is not assigned and silently ignored.
     *
     * @param privateKey
     */
    public void setPrivateKey(final String privateKey) {
	this.privateKey = privateKey != null ? privateKey : this.privateKey;
    }

    /**
     * Assigns username to be used in HTTP communication. Null value is not assigned and silently ignored.
     *
     * @param username
     */
    public void setUsername(final String username) {
	if (username != null) {
	    this.username = username;
	    if (userController != null) {
		try {
		    user = userController.findUser(username);
		} catch (final Exception e) {
		    logger.warn("User " + username + " could not be found.", e);
		}
	    }
	}
    }

    /** Resets username and private key by assigning null values. */
    public void resetUser() {
	privateKey = null;
	username = null;
	user = null;
    }

    public void setUserController(final IUserController controller) {
	if (userController != null) {
	    throw new IllegalStateException("User controller should be assigned only once.");
	}

	userController = controller;
    }

    @Override
    public User getUser() {
	return user;
    }

    public String getSystemUri() {
	return systemUri;
    }

    public void updateLoginInformation(final String username, final String privateKey) {
	setPrivateKey(privateKey);
	setUsername(username);
    }

    @Override
    public void setUsername(final String username, final IUserController controller) {
	setUserController(controller);
	setUsername(username);
    }

}
