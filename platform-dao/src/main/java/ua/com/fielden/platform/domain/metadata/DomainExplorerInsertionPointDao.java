package ua.com.fielden.platform.domain.metadata;

import static java.lang.String.format;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.fetchAll;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.fetchKeyAndDescOnly;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.from;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.orderBy;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.select;
import static ua.com.fielden.platform.utils.EntityUtils.isUnionEntityType;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.google.inject.Inject;

import ua.com.fielden.platform.dao.CommonEntityDao;
import ua.com.fielden.platform.domain.metadata.exceptions.DomainGenerationException;
import ua.com.fielden.platform.entity.annotation.EntityType;
import ua.com.fielden.platform.entity.query.IFilter;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IWhere0;
import ua.com.fielden.platform.entity.query.fluent.fetch;
import ua.com.fielden.platform.entity.query.model.EntityResultQueryModel;
import ua.com.fielden.platform.entity.query.model.OrderingModel;

/**
 * DAO for {@link DomainExplorerInsertionPoint}.
 *
 * @author TG Team
 */
@EntityType(DomainExplorerInsertionPoint.class)
public class DomainExplorerInsertionPointDao extends CommonEntityDao<DomainExplorerInsertionPoint> implements DomainExplorerInsertionPointCo {

    private static final String COULD_NOT_IDENTIFY_HOLDER_TYPE_ERR = "Could not find %s holder type";

    @Inject
    protected DomainExplorerInsertionPointDao(final IFilter filter) {
        super(filter);
    }

    @Override
        public DomainExplorerInsertionPoint save(final DomainExplorerInsertionPoint entity) {
            if (entity.getDomainTypeName() == null) {
                entity.setGeneratedHierarchy(loadTypes(entity));
            } else {
                entity.setGeneratedHierarchy(loadProperties(entity));
            }
            return super.save(entity);
    }

    private List<DomainPropertyTreeEntity> loadProperties(final DomainExplorerInsertionPoint entity) {
        try {
            final IWhere0<DomainProperty> partialQueryModel = select(DomainProperty.class).where();
            final Class<?> domainType = Class.forName(entity.getDomainTypeName());
            final EntityResultQueryModel<DomainProperty> queryModel;
            if (isUnionEntityType(domainType)) {
                queryModel = partialQueryModel.prop("holder.domainProperty").eq().val(entity.getDomainPropertyHolderId()).model();
            } else {
                queryModel = partialQueryModel.prop("holder.domainType").eq().val(entity.getDomainTypeHolderId()).model();
            }
            final fetch<DomainProperty> fetch = fetchAll(DomainProperty.class).with("domainType", fetchKeyAndDescOnly(DomainType.class).with("entity").with("dbTable"));
            try(final Stream<DomainProperty> stream = co(DomainProperty.class).stream(from(queryModel).with(fetch).model())) {
                return stream.map(domainProperty -> createDomainProperty(domainProperty)).collect(Collectors.toList());
            }
        } catch (final ClassNotFoundException e) {
            throw new DomainGenerationException(format(COULD_NOT_IDENTIFY_HOLDER_TYPE_ERR, entity.getDomainTypeName()), e);
        }

    }

    private DomainPropertyTreeEntity createDomainProperty(final DomainProperty domainProperty) {
        Class<?> domainType;
        try {
            domainType = Class.forName(domainProperty.getDomainType().getKey());
        } catch (final ClassNotFoundException e) {
            domainType = null;
        }
        final DomainPropertyTreeEntity entry = new DomainPropertyTreeEntity();
        entry.setEntityId(domainProperty.getId());
        entry.setKey(domainProperty.getTitle());
        entry.setDesc(domainProperty.getDesc());
        entry.setPropertyType(domainProperty.getDomainType());
        entry.setInternalName(domainProperty.getName());
        entry.setDbSchema(domainProperty.getDbColumn());
        entry.setRefTable(domainProperty.getDomainType().getDbTable());
        entry.setHasChildren(domainProperty.getDomainType().getEntity() || isUnionEntityType(domainType));
        entry.setUnion(isUnionEntityType(domainType));
        entry.setIsKey(domainProperty.getKeyIndex() != null);
        entry.setKeyOrder(domainProperty.getKeyIndex());
        entry.setIsRequired(domainProperty.getRequired());
        return entry;


    }

    private List<DomainTreeEntity> loadTypes(final DomainExplorerInsertionPoint entity) {
        final EntityResultQueryModel<DomainType> queryModel = select(DomainType.class).where().prop("entity").eq().val(true).model();
        final OrderingModel orderingModel = orderBy().yield("desc").asc().model();
        try(final Stream<DomainType> stream = co(DomainType.class).stream(from(queryModel).with(orderingModel).model())) {
            return stream.map(domainType -> createDomainType(domainType)).collect(Collectors.toList());
        }
    }

    private DomainTreeEntity createDomainType(final DomainType domainType) {
        final DomainTreeEntity entry = new DomainTreeEntity();
        entry.setEntityId(domainType.getId());
        entry.setKey(domainType.getDesc());
        entry.setDesc(domainType.getEntityTypeDesc());
        entry.setInternalName(domainType.getKey());
        entry.setDbSchema(domainType.getDbTable());
        entry.setHasChildren(true);
        return entry;
    }
}
