package ua.com.fielden.platform.entity.query;

import static ua.com.fielden.platform.entity.query.fluent.fetch.FetchCategory.NONE;

import java.util.Map.Entry;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.exceptions.EqlException;
import ua.com.fielden.platform.entity.query.fluent.fetch;
import ua.com.fielden.platform.entity.query.metadata.DomainMetadataAnalyser;

public class EntityAggregatesRetrievalModel<T extends AbstractEntity<?>> extends AbstractRetrievalModel<T> {

    public EntityAggregatesRetrievalModel(final fetch<T> originalFetch, final DomainMetadataAnalyser domainMetadataAnalyser) {
        super(originalFetch, domainMetadataAnalyser);

        validateModel();

        for (final String propName : originalFetch.getIncludedProps()) {
            addPrimProp(propName);
        }

        for (final Entry<String, fetch<? extends AbstractEntity<?>>> entry : originalFetch.getIncludedPropsWithModels().entrySet()) {
            addEntityPropsModel(entry.getKey(), entry.getValue());
        }
    }

    private void validateModel() {
        if (NONE != getOriginalFetch().getFetchCategory()) {
            throw new EqlException("The only acceptable category for EntityAggregates entity type fetch model creation is NONE. Use EntityQueryUtils.fetchAggregates(..) method for obtaining correct fetch model.");
        }

        if (getOriginalFetch().getExcludedProps().size() > 0) {
            throw new EqlException("The possibility to exclude certain properties can't be applied for EntityAggregates entity type fetch model!");
        }

        if (getOriginalFetch().getIncludedPropsWithModels().size() + getOriginalFetch().getIncludedProps().size() == 0) {
            throw new EqlException("Can't accept empty fetch model for EntityAggregates entity type fetching!");
        }
    }

    private void addEntityPropsModel(final String propName, final fetch<? extends AbstractEntity<?>> model) {
        final fetch<?> existingFetch = getFetchModels().get(propName);
        addEntityPropFetchModel(propName, existingFetch != null ? existingFetch.unionWith(model) : model);
    }
}