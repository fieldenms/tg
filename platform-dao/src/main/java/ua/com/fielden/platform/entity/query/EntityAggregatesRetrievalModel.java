package ua.com.fielden.platform.entity.query;

import java.util.Map.Entry;

import ua.com.fielden.platform.dao.DomainMetadataAnalyser;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.fluent.fetch;
import ua.com.fielden.platform.entity.query.fluent.fetch.FetchCategory;

public class EntityAggregatesRetrievalModel<T extends AbstractEntity<?>> extends AbstractRetrievalModel<T> implements IRetrievalModel<T> {

    public EntityAggregatesRetrievalModel(final fetch<T> originalFetch, final DomainMetadataAnalyser domainMetadataAnalyser) {
        super(originalFetch, domainMetadataAnalyser);

        validateModel();

        for (final String propName : originalFetch.getIncludedProps()) {
            with(propName, false);
        }

        for (final Entry<String, fetch<? extends AbstractEntity<?>>> entry : originalFetch.getIncludedPropsWithModels().entrySet()) {
            with(entry.getKey(), entry.getValue());
        }
    }

    private void validateModel() {
        if (!FetchCategory.ID_AND_VERSTION.equals(getOriginalFetch().getFetchCategory())) {
            throw new IllegalArgumentException("The only acceptable category for EntityAggregates entity type fetch model creation is NONE. Use EntityQueryUtils.fetchOnly(..) method for obtaining correct fetch model.");
        }

        if (getOriginalFetch().getExcludedProps().size() > 0) {
            throw new IllegalArgumentException("The possibility to exclude certain properties can't be applied for EntityAggregates entity type fetch model!");
        }

        if (getOriginalFetch().getIncludedPropsWithModels().size() + getOriginalFetch().getIncludedProps().size() == 0) {
            throw new IllegalArgumentException("Can't accept empty fetch model for EntityAggregates entity type fetching!");
        }

    }

    private void with(final String propName, final boolean skipEntities) {
        getPrimProps().add(propName);
    }

    private void addEntityPropsModel(final String propName, final fetch<?> model) {
        final fetch<?> existingFetch = getEntityProps().get(propName);
        getEntityProps().put(propName, existingFetch != null ? existingFetch.unionWith(model) : model);
    }

    private void with(final String propName, final fetch<? extends AbstractEntity<?>> fetchModel) {
        if (AbstractEntity.class.isAssignableFrom(fetchModel.getEntityType())) {
            addEntityPropsModel(propName, fetchModel);
        } else {
            throw new IllegalArgumentException(propName + " has fetch model for type " + fetchModel.getEntityType().getName() + ". Fetch model with entity type is required.");
        }
    }
}