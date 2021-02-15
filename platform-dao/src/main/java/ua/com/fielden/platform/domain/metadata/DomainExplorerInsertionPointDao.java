package ua.com.fielden.platform.domain.metadata;

import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.from;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.select;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.google.inject.Inject;

import ua.com.fielden.platform.dao.CommonEntityDao;
import ua.com.fielden.platform.entity.annotation.EntityType;
import ua.com.fielden.platform.entity.query.IFilter;
import ua.com.fielden.platform.entity.query.model.EntityResultQueryModel;
import ua.com.fielden.platform.entity.query.model.QueryModel;

/**
 * DAO for {@link DomainExplorerInsertionPoint}.
 *
 * @author TG Team
 */
@EntityType(DomainExplorerInsertionPoint.class)
public class DomainExplorerInsertionPointDao extends CommonEntityDao<DomainExplorerInsertionPoint> implements DomainExplorerInsertionPointCo {

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
        select(DomainProperty.class).where().
            prop("holder.key").eq().val(entity.getDomainTypeName());
        return null;
    }

    private List<DomainTreeEntity> loadTypes(final DomainExplorerInsertionPoint entity) {
        final QueryModel<DomainProperty> subPropModel = select(DomainProperty.class).where().prop("holder").eq().extProp("id").model();
        final EntityResultQueryModel<DomainType> queryModel = select(DomainType.class).where().exists(subPropModel).model();
        try(final Stream<DomainType> stream = co(DomainType.class).stream(from(queryModel).model())) {
            return stream.map(domainType -> createDomainType(domainType)).collect(Collectors.toList());
        }
    }

    private DomainTreeEntity createDomainType(final DomainType domainType) {
        final DomainTreeEntity entry = new DomainTreeEntity();
        entry.setKey(domainType.getDesc());
        entry.setInternalName(domainType.getKey());
        return entry;
    }
}
