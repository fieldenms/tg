package ua.com.fielden.platform.processors.metamodel;

import static java.lang.String.format;

import ua.com.fielden.platform.processors.metamodel.models.EntityMetaModel;

/**
 * This class stores constant values that are used for generation of meta-models and validation of their underlying entities.
 * 
 * @author TG Team
 */

public abstract class MetaModelConstants {

    public static final Class<EntityMetaModel> METAMODEL_SUPERCLASS = EntityMetaModel.class;

    public static final String METAMODELS_CLASS_SIMPLE_NAME = "MetaModels";
    public static final String METAMODELS_CLASS_PKG_NAME = "metamodels";
    public static final String METAMODELS_CLASS_QUAL_NAME = format("%s.%s", METAMODELS_CLASS_PKG_NAME, METAMODELS_CLASS_SIMPLE_NAME);

    public static final String META_MODEL_PKG_NAME_SUFFIX = ".meta";
    public static final String META_MODEL_NAME_SUFFIX = "MetaModel";

}