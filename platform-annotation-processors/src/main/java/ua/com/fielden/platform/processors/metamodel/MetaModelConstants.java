package ua.com.fielden.platform.processors.metamodel;

import static java.lang.String.format;

import java.lang.annotation.Annotation;
import java.util.Set;

import com.squareup.javapoet.ClassName;

import ua.com.fielden.platform.annotations.metamodel.DomainEntity;
import ua.com.fielden.platform.annotations.metamodel.WithMetaModel;
import ua.com.fielden.platform.entity.annotation.MapEntityTo;
import ua.com.fielden.platform.processors.metamodel.models.EntityMetaModel;

/**
 * This class stores constant values that are used for generation of meta-models and validation of their underlying entities.
 * 
 * @author TG Team
 */

public abstract class MetaModelConstants {

    public static final Set<Class<? extends Annotation>> ANNOTATIONS_THAT_TRIGGER_META_MODEL_GENERATION = Set.of(MapEntityTo.class, DomainEntity.class, WithMetaModel.class);

    public static final Class<EntityMetaModel> METAMODEL_SUPERCLASS = EntityMetaModel.class;
    public static final ClassName METAMODEL_SUPERCLASS_CLASSNAME = ClassName.get(METAMODEL_SUPERCLASS);

    public static final String METAMODELS_CLASS_SIMPLE_NAME = "MetaModels";
    public static final String METAMODELS_CLASS_PKG_NAME = "metamodels";
    public static final String METAMODELS_CLASS_QUAL_NAME = format("%s.%s", METAMODELS_CLASS_PKG_NAME, METAMODELS_CLASS_SIMPLE_NAME);

    public static final String META_MODEL_PKG_NAME_SUFFIX = ".meta";
    public static final String META_MODEL_NAME_SUFFIX = "MetaModel";

}