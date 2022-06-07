package ua.com.fielden.platform.processors.metamodel;

import static java.lang.String.format;

import javax.lang.model.element.TypeElement;

import ua.com.fielden.platform.annotations.metamodel.DomainEntity;
import ua.com.fielden.platform.entity.annotation.MapEntityTo;
import ua.com.fielden.platform.processors.metamodel.models.EntityMetaModel;

public abstract class MetaModelConstants {

    public static final Class<EntityMetaModel> METAMODEL_SUPERCLASS = EntityMetaModel.class;

    public static final String METAMODELS_CLASS_SIMPLE_NAME = "MetaModels";
    public static final String METAMODELS_CLASS_PKG_NAME = "metamodels";
    public static final String METAMODELS_CLASS_QUAL_NAME = format("%s.%s", METAMODELS_CLASS_PKG_NAME, METAMODELS_CLASS_SIMPLE_NAME);

    public static final String META_MODEL_PKG_NAME_SUFFIX = ".meta";
    public static final String META_MODEL_NAME_SUFFIX = "MetaModel";

    /**
     * A predicate that determines whether {@code element} represent a domain entity type.
     *
     * @param element
     * @return
     */
    public static boolean isDomainEntity(final TypeElement element) {
        return element.getAnnotation(MapEntityTo.class) != null ||
               element.getAnnotation(DomainEntity.class) != null;
    }

}