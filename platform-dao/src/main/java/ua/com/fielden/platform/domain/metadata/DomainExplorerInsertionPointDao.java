package ua.com.fielden.platform.domain.metadata;

import com.google.inject.Inject;
import ua.com.fielden.platform.dao.CommonEntityDao;
import ua.com.fielden.platform.domain.metadata.exceptions.DomainGenerationException;
import ua.com.fielden.platform.entity.annotation.EntityType;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IWhere0;
import ua.com.fielden.platform.entity.query.fluent.fetch;
import ua.com.fielden.platform.entity.query.model.EntityResultQueryModel;
import ua.com.fielden.platform.entity.query.model.OrderingModel;
import ua.com.fielden.platform.error.Result;
import ua.com.fielden.platform.security.IAuthorisationModel;
import ua.com.fielden.platform.security.provider.ISecurityTokenProvider;
import ua.com.fielden.platform.types.RichText;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.lang.String.format;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.*;
import static ua.com.fielden.platform.error.Result.successful;
import static ua.com.fielden.platform.security.tokens.Template.READ_MODEL;
import static ua.com.fielden.platform.utils.EntityUtils.isUnionEntityType;

/**
 * DAO for {@link DomainExplorerInsertionPoint}.
 *
 * @author TG Team
 */
@EntityType(DomainExplorerInsertionPoint.class)
public class DomainExplorerInsertionPointDao extends CommonEntityDao<DomainExplorerInsertionPoint> implements DomainExplorerInsertionPointCo {

    private static final String COULD_NOT_IDENTIFY_HOLDER_TYPE_ERR = "Could not find %s holder type";

    private final IAuthorisationModel authModel;
    private final ISecurityTokenProvider tokenProvider;
    
    @Inject
    protected DomainExplorerInsertionPointDao(final IAuthorisationModel authModel, final ISecurityTokenProvider tokenProvider) {
        this.authModel = authModel;
        this.tokenProvider = tokenProvider;
    }

    @Override
    public DomainExplorerInsertionPoint save(final DomainExplorerInsertionPoint entity) {
        tokenProvider.getTokenByName(format(READ_MODEL.forClassName(), "")).map(authModel::authorise).orElse(successful("Access by default")).ifFailure(Result::throwRuntime);

        if (entity.getDomainTypeName() == null) {
            entity.setGeneratedHierarchy(loadTypes(entity));
        } else {
            entity.setGeneratedHierarchy(loadProperties(entity));
        }
        return super.save(entity);
    }

    /**
     * Identifies whether {@code type} represents a union entity type or a component type with more than one attribute.
     * <p>
     * The implication of this is that properties of such types would have their structure mapped to the same table as the entity where they are declared.
     * And this requires a different query to retrieve the information and a different representation comparing to how domain metadata is handled for domain entities.
     *
     * @param type
     * @return
     */
    private boolean isUnionOrComponentWithMoreThanOneAttribute(final Class<?> type) {
        return isUnionEntityType(type) || RichText.class == type;
    }

    private List<DomainPropertyTreeEntity> loadProperties(final DomainExplorerInsertionPoint entity) {
        try {
            final IWhere0<DomainProperty> partialQueryModel = select(DomainProperty.class).where();
            final Class<?> type = Class.forName(entity.getDomainTypeName());
            final EntityResultQueryModel<DomainProperty> queryModel;
            if (isUnionOrComponentWithMoreThanOneAttribute(type)) {
                queryModel = partialQueryModel.prop("holder.domainProperty").eq().val(entity.getDomainPropertyHolderId()).model();
            } else {
                queryModel = partialQueryModel.prop("holder.domainType").eq().val(entity.getDomainTypeHolderId()).model();
            }
            final fetch<DomainProperty> fetch = fetchAll(DomainProperty.class).with("domainType", fetchKeyAndDescOnly(DomainType.class).with("entity").with("dbTable").with("propsCount"));
            try (final Stream<DomainProperty> stream = co(DomainProperty.class).stream(from(queryModel).with(fetch).with(orderProperies()).model())) {
                return stream.map(domainProperty -> createDomainProperty(domainProperty)).collect(Collectors.toList());
            }
        } catch (final ClassNotFoundException ex) {
            throw new DomainGenerationException(COULD_NOT_IDENTIFY_HOLDER_TYPE_ERR.formatted(entity.getDomainTypeName()), ex);
        }

    }

    private DomainPropertyTreeEntity createDomainProperty(final DomainProperty domainProperty) {
        Class<?> type;
        try {
            type = Class.forName(domainProperty.getDomainType().getKey());
        } catch (final ClassNotFoundException e) {
            type = null;
        }
        final DomainPropertyTreeEntity entry = new DomainPropertyTreeEntity();
        entry.setEntityId(domainProperty.getId());
        entry.setKey(domainProperty.getTitle());
        entry.setDesc(domainProperty.getDesc());
        entry.setPropertyType(domainProperty.getDomainType());
        entry.setInternalName(domainProperty.getName());
        entry.setDbSchema(domainProperty.getDbColumn());
        entry.setRefTable(domainProperty.getDomainType().getDbTable());
        entry.setHasChildren(domainProperty.getDomainType().getPropsCount() > 0);
        entry.setUnionEntityOrComponentWithMoreThanOneAttribute(isUnionOrComponentWithMoreThanOneAttribute(type));
        entry.setIsKey(domainProperty.getKeyIndex() != null);
        entry.setKeyOrder(domainProperty.getKeyIndex());
        entry.setIsRequired(domainProperty.getRequired());
        return entry;
    }

    private List<DomainTreeEntity> loadTypes(final DomainExplorerInsertionPoint entity) {
        final EntityResultQueryModel<DomainType> queryModel = select(DomainType.class).where().prop("entity").eq().val(true).model();
        final OrderingModel orderingModel = orderBy().yield("desc").asc().model();
        try (final Stream<DomainType> stream = co(DomainType.class).stream(from(queryModel).with(orderingModel).model())) {
            final List<DomainTreeEntity> types = stream.map(domainType -> createDomainType(domainType)).collect(Collectors.toList());
            final Map<Long, List<DomainPropertyTreeEntity>> groupedProperties = loadProperties();
            types.forEach(type -> {
                type.setChildren(groupedProperties.getOrDefault(type.getEntityId(), new ArrayList<>()));
            });
            return types;
        }
    }

    private Map<Long, List<DomainPropertyTreeEntity>> loadProperties() {
        final EntityResultQueryModel<DomainProperty> queryModel = select(DomainProperty.class).where().prop("holder.domainType").isNotNull().model();
        final fetch<DomainProperty> fetch = fetchAll(DomainProperty.class).with("holder").with("domainType", fetchKeyAndDescOnly(DomainType.class).with("entity").with("dbTable"));
        try (final Stream<DomainProperty> stream = co(DomainProperty.class).stream(from(queryModel).with(fetch).with(orderProperies()).model())) {
            return stream.collect(Collectors.groupingBy(domainProp -> domainProp.getHolder().getDomainType().getId(), Collectors.mapping(domainProperty -> createDomainProperty(domainProperty), Collectors.toList())));
        }
    }

    private DomainTreeEntity createDomainType(final DomainType domainType) {
        final DomainTreeEntity entry = new DomainTreeEntity();
        entry.setEntityId(domainType.getId());
        entry.setKey(domainType.getDesc());
        entry.setDesc(domainType.getEntityTypeDesc());
        entry.setInternalName(domainType.getKey());
        entry.setDbSchema(domainType.getDbTable());
        entry.setHasChildren(domainType.getPropsCount() > 0);
        return entry;
    }

    private OrderingModel orderProperies() {
        return orderBy().yield("position").asc().model();
    }
}
