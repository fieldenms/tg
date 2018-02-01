package ua.com.fielden.platform.entity.validation;

import static java.lang.String.format;
import static ua.com.fielden.platform.error.Result.failure;
import static ua.com.fielden.platform.error.Result.successful;
import static ua.com.fielden.platform.utils.EntityUtils.equalsEx;

import java.lang.annotation.Annotation;
import java.util.Set;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.meta.MetaProperty;
import ua.com.fielden.platform.entity.validation.annotation.Final;
import ua.com.fielden.platform.error.Result;
import ua.com.fielden.platform.reflection.TitlesDescsGetter;

/**
 * BCE implementation to enforce {@link Final} semantics.
 *
 * @author TG Team
 *
 */
public class FinalValidator implements IBeforeChangeEventHandler<Object> {
    
    private final boolean persistentOnly;
    
    public FinalValidator(final boolean persistentOnly) {
        this.persistentOnly = persistentOnly;
    }
    
    @Override
    public Result handle(final MetaProperty<Object> property, final Object newValue, final Set<Annotation> mutatorAnnotations) {
        final AbstractEntity<?> entity = property.getEntity();

        if (!isPropertyFinalised(property, persistentOnly) ||
            equalsEx(property.getValue(), newValue)) { // i.e. there is no actual change
            return successful("Value is being assigned for the first time.");
        }

        final String entityTitle = TitlesDescsGetter.getEntityTitleAndDesc(entity.getType()).getKey();
        return failure(entity, format("Reassigning a value for property [%s] in entity [%s] is not permitted.", property.getTitle(), entityTitle));
    }
    
    /**
     * This method capture the meaning of a property being <code>finalised</code>.
     * 
     * @param property
     * @param persistentOnly
     * @return
     */
    public static boolean isPropertyFinalised(final MetaProperty<?> property, final boolean persistentOnly) {
        final AbstractEntity<?> entity = property.getEntity();
        
        if (persistentOnly) {
            if (entity.isPersisted() && property.getOriginalValue() != null) {
                return true;
            }
        } else if (property.getValue() != null) {
            return true;
        }
        
        return false;
    }
   
}