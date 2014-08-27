package ua.com.fielden.platform.dao;

import java.util.ArrayList;
import java.util.List;
import java.util.SortedMap;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.model.EntityResultQueryModel;

public class ModelledEntityMetadata<ET extends AbstractEntity<?>> extends AbstractEntityMetadata<ET> {
    private final List<EntityResultQueryModel<ET>> models = new ArrayList<EntityResultQueryModel<ET>>();

    public ModelledEntityMetadata(final List<EntityResultQueryModel<ET>> models, final Class<ET> type, final SortedMap<String, PropertyMetadata> props) {
        super(type, props);

        if (models != null) {
            if (models.size() == 0) {
                throw new IllegalArgumentException("Zero models for entity type: " + type);
            }
            this.models.addAll(models);
        }
    }

    public List<EntityResultQueryModel<ET>> getModels() {
        return models;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + ((models == null) ? 0 : models.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (!super.equals(obj))
            return false;
        if (!(obj instanceof ModelledEntityMetadata))
            return false;
        ModelledEntityMetadata other = (ModelledEntityMetadata) obj;
        if (models == null) {
            if (other.models != null)
                return false;
        } else if (!models.equals(other.models))
            return false;
        return true;
    }
}