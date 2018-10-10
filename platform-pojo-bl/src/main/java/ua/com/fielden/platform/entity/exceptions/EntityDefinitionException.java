package ua.com.fielden.platform.entity.exceptions;

/**
 * A runtime exception that indicates incorrect entity definition.
 * 
 * @author TG Team
 *
 */
public class EntityDefinitionException extends RuntimeException {
    private static final long serialVersionUID = 1L;
    
    public static final String INVALID_USE_OF_NUMERIC_PARAMS_MSG = "Property [%s] in [%s] is not numeric, but has number-specific parameters as part of its annotation @IsProperty.";
    public static final String INVALID_VALUES_FOR_PRECITION_AND_SCALE_MSG = "Property [%s] in [%s] has precision less or equal to scale, which is not permitted.";
    public static final String INVALID_USE_FOR_PRECITION_AND_SCALE_MSG = "Property [%s] in [%s] should have both precision and scale specified as non-negative integers.";
    public static final String INVALID_USE_OF_PARAM_LENGTH_MSG = "Property [%s] in [%s] is not string or array, but has length specified as part of its annotation @IsProperty.";
    public static final String COLLECTIONAL_PROP_MISSING_TYPE_MSG = "Property [%s] in [%s] is collectional (or a property descriptor), but has missing type argument, which should be specified as part of annotation @IsProperty.";
    public static final String COLLECTIONAL_PROP_MISSING_LINK_MSG = "Property [%s] in entity [%s] is collectional, but has missing <b>link property</b> argument, which should be specified as part of annotation IsProperty or through composite key relation.";
    public static final String INVALID_ONE2ONE_ASSOCIATION_MSG = "Property [%s] in entity [%s] has AE key type, but it does not form correct one2one association due to non-parent type of property key.";
    
    public EntityDefinitionException(final String msg) {
        super(msg);
    }
    
    public EntityDefinitionException(final String msg, final Throwable cause) {
        super(msg, cause);
    }
}
