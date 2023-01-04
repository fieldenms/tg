package ua.com.fielden.platform.entity.query.metadata;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.SortedMap;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.model.EntityResultQueryModel;
import ua.com.fielden.platform.eql.exceptions.EqlMetadataGenerationException;

public class ModelledEntityMetadata<ET extends AbstractEntity<?>> extends AbstractEntityMetadata<ET> {
    private final List<EntityResultQueryModel<ET>> models = new ArrayList<EntityResultQueryModel<ET>>();

    public ModelledEntityMetadata(final List<EntityResultQueryModel<ET>> models, final Class<ET> type, final SortedMap<String, PropertyMetadata> props) {
        super(type, props);

        if (models != null && !models.isEmpty()) {
            this.models.addAll(models);
        } else {
            throw new EqlMetadataGenerationException("No model definitions were found for synthetic entity [%s].".formatted(type));
        }
    }

    public List<EntityResultQueryModel<ET>> getModels() {
        return models;
    }

    @Override
    public int hashCode() {
        return 31 * models.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof ModelledEntityMetadata)) {
            return false;
        }
        final ModelledEntityMetadata<?> that = (ModelledEntityMetadata<?>) obj;
        return Objects.equals(this.models, that.models);
    }

}