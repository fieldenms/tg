package ua.com.fielden.platform.web.resources;

import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.from;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.select;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.restlet.Context;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.data.CharacterSet;
import org.restlet.data.Form;
import org.restlet.ext.json.JsonRepresentation;
import org.restlet.representation.Representation;
import org.restlet.resource.ResourceException;
import org.restlet.resource.ServerResource;

import ua.com.fielden.platform.dao.DynamicEntityDao;
import ua.com.fielden.platform.dao.IEntityDao;
import ua.com.fielden.platform.dao.QueryExecutionModel;
import ua.com.fielden.platform.domaintree.centre.ICentreDomainTreeManager.ICentreDomainTreeManagerAndEnhancer;
import ua.com.fielden.platform.domaintree.centre.impl.CentreDomainTreeManagerAndEnhancer;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.entity.factory.ICompanionObjectFinder;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.ICompleted;
import ua.com.fielden.platform.entity.query.fluent.fetch;
import ua.com.fielden.platform.entity.query.model.EntityResultQueryModel;
import ua.com.fielden.platform.entity.query.model.OrderingModel;
import ua.com.fielden.platform.serialisation.api.ISerialiser;
import ua.com.fielden.platform.serialisation.json.TgObjectMapper;
import ua.com.fielden.platform.serialisation.json.deserialiser.JsonToCentreConfigDeserialiser.LightweightCentre;
import ua.com.fielden.platform.swing.review.DynamicFetchBuilder;
import ua.com.fielden.platform.swing.review.DynamicOrderingBuilder;
import ua.com.fielden.platform.swing.review.DynamicParamBuilder;
import ua.com.fielden.platform.swing.review.DynamicQueryBuilder;
import ua.com.fielden.platform.swing.review.DynamicQueryBuilder.QueryProperty;
import ua.com.fielden.platform.ui.config.EntityCentreConfig;
import ua.com.fielden.platform.ui.config.api.IEntityCentreConfigController;
import ua.com.fielden.platform.utils.Pair;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.google.inject.Injector;

public class QueryPageResource extends ServerResource {

    private final Injector injector;

    private final String centreName;
    private final String page;
    private final int pageNo;
    private final int pageCapacity;
    private final String queryStr;
    private final TgObjectMapper mapper;
    private final String username;

    public QueryPageResource(//
    final Injector injector, //
            final Context context, //
            final Request request, //
            final Response response, //
            final String username) throws JsonParseException, JsonMappingException, IOException {
        this.username = username;
        init(context, request, response);
        this.injector = injector;
        this.centreName = (String) request.getAttributes().get("centreName");
        this.page = (String) request.getAttributes().get("page");
        //Reading and decoding query form (in URL it is a part after '?' sign)
        final Form queryForm = request.getOriginalRef().getQueryAsForm(CharacterSet.UTF_8);
        //Getting page number and page capacity from query form.
        this.pageNo = Integer.parseInt(queryForm.getFirstValue("pageNo"));
        this.pageCapacity = Integer.parseInt(queryForm.getFirstValue("pageCapacity"));
        this.queryStr = queryForm.getFirstValue("query");
        //The deserialiser for query.
        this.mapper = new TgObjectMapper(injector.getInstance(EntityFactory.class));

    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Override
    protected Representation get() throws ResourceException {
        try {
            final LightweightCentre queryEntity = mapper.readValue(queryStr, LightweightCentre.class);
            final ICentreDomainTreeManagerAndEnhancer cdtmae = getCentre();
            final Class<AbstractEntity<?>> entityType = queryEntity.getEntityType();
            final Class<AbstractEntity<?>> managedType = (Class<AbstractEntity<?>>) cdtmae.getEnhancer().getManagedType(entityType);
            final List<QueryProperty> queryProps = queryEntity.getQueryProperties(managedType);
            final ICompleted<AbstractEntity<?>> query = DynamicQueryBuilder.createQuery(managedType, queryProps);
            final fetch<AbstractEntity<?>> fetchModel = DynamicFetchBuilder.createFetchOnlyModel(managedType, queryEntity.createFetchProps());
            final Set<String> summaryProps = "first".equals(page) ? queryEntity.createSummaryProps() : null;
            final fetch<AbstractEntity<?>> total = summaryProps == null || summaryProps.isEmpty() ? null : DynamicFetchBuilder.createTotalFetchModel(managedType, summaryProps);
            final OrderingModel queryOrdering = DynamicOrderingBuilder.createOrderingModel(managedType, queryEntity.createOrderingProps());
            final Map<String, Pair<Object, Object>> paramMap = queryEntity.createParamMap();
            IEntityDao<AbstractEntity<?>> controller = injector.getInstance(ICompanionObjectFinder.class).find(entityType);
            controller = controller == null ? injector.getInstance(DynamicEntityDao.class) : controller;
            final TgObjectMapper serialiser = new TgObjectMapper(injector.getInstance(EntityFactory.class));

            final QueryExecutionModel queryModel = from(query.model()).//
            with(fetchModel).//
            with(queryOrdering).//
            with(DynamicParamBuilder.buildParametersMap(managedType, paramMap)).model();
            queryModel.setLightweight(true);
            if ("first".equals(page) && total != null) {
                final QueryExecutionModel totalModel = from(query.model()).//
                with(total).//
                with(DynamicParamBuilder.buildParametersMap(managedType, paramMap)).model();
                return new JsonRepresentation(serialiser.writeValueAsString(controller.firstPage(queryModel, totalModel, pageCapacity)));
            }

            return new JsonRepresentation(serialiser.writeValueAsString(controller.getPage(queryModel, pageNo, pageCapacity)));
        } catch (final Exception e) {
            throw new ResourceException(e);
        }
    }

    private ICentreDomainTreeManagerAndEnhancer getCentre() throws Exception {
        final IEntityCentreConfigController eccc = injector.getInstance(IEntityCentreConfigController.class);
        final ISerialiser serialiser = injector.getInstance(ISerialiser.class);
        final EntityCentreConfig entityCentre = eccc.getEntity(from(modelSuUser()).model());
        final ICentreDomainTreeManagerAndEnhancer cdtmae = serialiser.deserialise(entityCentre.getConfigBody(), CentreDomainTreeManagerAndEnhancer.class);
        return cdtmae;
    }

    private EntityResultQueryModel<EntityCentreConfig> modelSuUser() {
        final EntityResultQueryModel<EntityCentreConfig> model =
        /*    */select(EntityCentreConfig.class).where().//
        /*    */prop("owner.key").eq().val(username).and().// look for entity-centres for both users (current and its base)
        /*    */prop("title").eq().val(centreName).and().//
        /*    */prop("menuItem.key").eq().val(centreName).model();
        return model;
    }
}
