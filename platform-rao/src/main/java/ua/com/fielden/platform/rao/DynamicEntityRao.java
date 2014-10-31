package ua.com.fielden.platform.rao;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.reflection.AnnotationReflector;
import ua.com.fielden.platform.swing.review.annotations.EntityType;

import com.google.inject.Inject;

/**
 * This RAO is applicable for instantiation of any entity class specified at runtime. However, it is not type safe -- there is no way at compile time to ensure correct values for
 * entity and key types.
 *
 * @author TG Team
 *
 */
@EntityType(AbstractEntity.class)
public class DynamicEntityRao<E extends AbstractEntity<?>> extends CommonEntityRao<E> {

    private Class<E> entityType;
    private Class<? extends Comparable> keyType;

    /**
     * Needed for reflective instantiation.
     */
    @Inject
    public DynamicEntityRao(final RestClientUtil util) {
        super(util);
    }

    public void setEntityType(final Class<E> type) {
        this.entityType = type;
        this.keyType = AnnotationReflector.getKeyType(entityType);
    }

    @Override
    public Class<E> getEntityType() {
        return entityType;
    }

    @Override
    public Class<? extends Comparable> getKeyType() {
        return keyType;
    }

    @Override
    public E save(final AbstractEntity entity) {
        throw new UnsupportedOperationException("");
    }
}
