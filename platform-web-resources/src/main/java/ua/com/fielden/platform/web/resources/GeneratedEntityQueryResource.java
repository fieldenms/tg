package ua.com.fielden.platform.web.resources;

import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.log4j.Logger;
import org.joda.time.DateTime;
import org.joda.time.Period;
import org.restlet.Context;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.data.MediaType;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.representation.Variant;
import org.restlet.resource.Post;
import org.restlet.resource.Resource;
import org.restlet.resource.ResourceException;
import org.restlet.resource.ServerResource;
import org.restlet.routing.Router;

import ua.com.fielden.platform.dao.IComputationMonitor;
import ua.com.fielden.platform.dao.IEntityDao;
import ua.com.fielden.platform.dao.QueryExecutionModel;
import ua.com.fielden.platform.entity.query.DynamicallyTypedQueryContainer;
import ua.com.fielden.platform.entity.query.model.EntityResultQueryModel;
import ua.com.fielden.platform.pagination.IPage;
import ua.com.fielden.platform.roa.HttpHeaders;
import ua.com.fielden.platform.security.user.IUserProvider;
import ua.com.fielden.platform.web.factories.CompanionResourceFactory;

import com.google.inject.Injector;

/**
 * Represents a web resource mapped to URI /query/generated-type.
 * 
 * @author TG Team
 */
public class GeneratedEntityQueryResource extends ServerResource implements IComputationMonitor {
    private static final Logger logger = Logger.getLogger(GeneratedEntityQueryResource.class);
    private static final String FIRST = "first";
    // the following properties are determined from request
    private final Integer pageCapacity;
    private final int pageNo;
    private final int pageCount;
    /** Indicates whether response should return count. */
    private final boolean shouldReturnCount;
    /** Indicates whether response should return the whole result. */
    private final boolean shouldReturnAll;
    /** Indicates whether response should return only first items (number limited by page capacity. */
    private final boolean shouldReturnFirst;

    private final IEntityDao companion; // not typed deliberately
    private final RestServerUtil restUtil;

    /** The following fields are required to support companion resource. */
    private final Router router;
    private final CompanionResourceFactory coResourceFactory;
    private AtomicBoolean stopped = new AtomicBoolean();
    private final String username;

    /**
     * The main resource constructor accepting a DAO instance in addition to the standard {@link Resource} parameters.
     * 
     * @param dao
     * @param context
     * @param request
     * @param response
     */
    public GeneratedEntityQueryResource(final Router router, final Injector injector, final IEntityDao companion, final RestServerUtil restUtil, final Context context, final Request request, final Response response) {
        init(context, request, response);
        setNegotiated(false);
        getVariants().add(new Variant(MediaType.APPLICATION_OCTET_STREAM));
        //this.dao = dao;
        this.companion = companion;
        this.restUtil = restUtil;

        final String pageCapacityParam = request.getResourceRef().getQueryAsForm().getFirstValue("page-capacity");
        final String pageNoParam = request.getResourceRef().getQueryAsForm().getFirstValue("page-no");

        username = injector.getInstance(IUserProvider.class).getUser().getKey(); // or username = (String) request.getAttributes().get("username");

        pageCapacity = initPageCapacity(pageCapacityParam);
        pageNo = initPageNoOrCount(pageNoParam);
        pageCount = initPageNoOrCount(request.getResourceRef().getQueryAsForm().getFirstValue("page-count"));

        shouldReturnCount = (pageCapacity == null) && !"all".equalsIgnoreCase(pageCapacityParam) && !FIRST.equalsIgnoreCase(pageNoParam);
        shouldReturnAll = "all".equalsIgnoreCase(pageCapacityParam);
        shouldReturnFirst = FIRST.equalsIgnoreCase(pageNoParam);

        // let's now create and route a companion resource factory
        this.router = router;
        final String companionToken = request.getResourceRef().getQueryAsForm().getFirstValue("co-token");
        coResourceFactory = new CompanionResourceFactory(this, injector);
        router.attach("/users/{username}/companions/" + companionToken, coResourceFactory);
    }

    /**
     * Initialisation of property <code>pageCapacity</code>.
     * 
     * @param pageCapacityParamName
     * @return
     */
    private Integer initPageCapacity(final String pageCapacityParamName) {
        try {
            return pageCapacityParamName != null ? Integer.parseInt(pageCapacityParamName) : null;
        } catch (final Exception e) {
            return null;
        }
    }

    /**
     * Initialisation of property <code>pageNo</code>.
     * 
     * @param pageNoParamName
     * @return
     */
    private int initPageNoOrCount(final String pageNoParamName) {
        try {
            return pageNoParamName != null && !pageNoParamName.equalsIgnoreCase(FIRST) ? Integer.parseInt(pageNoParamName) : 0;
        } catch (final Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    // /////////////////////////////////////////////////////////////////
    // //////////////////// request handlers ///////////////////////////
    // /////////////////////////////////////////////////////////////////

    /**
     * Handles POST request resulting from RAO call. It is expected that envelope is a serialised representation of {@link DynamicallyTypedQueryContainer}.
     */
    @Post
    @Override
    public Representation post(final Representation envelope) throws ResourceException {
        try {
            final QueryExecutionModel<?, EntityResultQueryModel<?>> qem = (QueryExecutionModel<?, EntityResultQueryModel<?>>) restUtil.restoreQueryExecutionModelForGeneratedType(envelope);
            if (shouldReturnCount) {
                final int count = companion.count(qem.getQueryModel(), qem.getParamValues());
                restUtil.setHeaderEntry(getResponse(), HttpHeaders.COUNT, count + "");
                return new StringRepresentation("count");
            } else if (shouldReturnAll) {
                //getResponse().setEntity(restUtil.listRepresentation(dao.getAllEntities(qem)));
                return restUtil.listRepresentation(companion.getAllEntities(qem));
            } else if (shouldReturnFirst) {
                return restUtil.listRepresentation(companion.getFirstEntities(qem, pageCapacity));
            } else {
                // TODO enhance logging of user action perhaps with (IP address, port) or some other additional information
                final DateTime st = new DateTime();
                logger.info("User [" + username + "] is trying to get [" + (pageNo + 1) + "] page of [" + pageCapacity + "] capacity using query model: " + qem);
                final IPage<?> page = companion.getPage(qem, pageNo, pageCount, pageCapacity);
                restUtil.setHeaderEntry(getResponse(), HttpHeaders.PAGES, page.numberOfPages() + "");
                restUtil.setHeaderEntry(getResponse(), HttpHeaders.PAGE_NO, page.no() + "");
                final Representation repr = restUtil.listRepresentation(page.data());
                final Period pd = new Period(st, new DateTime());
                logger.info("User [" + username + "] has got [" + (pageNo + 1) + "] page of [" + pageCapacity + "] capacity in " + pd.getSeconds() + " s " + pd.getMillis()
                        + " ms.");
                return repr;
            }
        } catch (final Exception ex) {
            ex.printStackTrace();
            if (stopped.get()) {
                return restUtil.errorRepresentation("Request was cancelled.");
            } else {
                return restUtil.errorRepresentation(ex);
            }
        } finally {
            // need to detach companion resource
            router.detach(coResourceFactory);
        }
    }

    @Override
    public boolean stop() {
        try {
            stopped.set(companion.stop());
            return stopped.get();
        } catch (final Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public Integer progress() {
        return null;
    }
}
