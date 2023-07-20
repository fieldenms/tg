package ua.com.fielden.platform.entity.validation;

import static java.lang.String.format;
import static ua.com.fielden.platform.entity.validation.annotation.Final.ERR_REASSIGNMENT;
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
    private final boolean nullIsValueForPersisted;

    public FinalValidator(final boolean persistentOnly, final boolean nullIsValueForPersisted) {
        this.persistentOnly = persistentOnly;
        this.nullIsValueForPersisted = nullIsValueForPersisted;
    }

    @Override
    public Result handle(final MetaProperty<Object> property, final Object newValue, final Set<Annotation> mutatorAnnotations) {
        final AbstractEntity<?> entity = property.getEntity();

        if (!isPropertyFinalised(property, persistentOnly, nullIsValueForPersisted) ||
            equalsEx(property.getValue(), newValue)) { // i.e. there is no actual change
            return successful();
        }

        final String entityTitle = TitlesDescsGetter.getEntityTitleAndDesc(entity.getType()).getKey();
        return failure(entity, format(ERR_REASSIGNMENT, property.getTitle(), entityTitle));
    }
    
    /**
     * This method capture the meaning of a property being <code>finalised</code>.
     * 
     * @param property
     * @param persistedOnly
     * @param nullIsValueForPersisted
     * @return
     */
    public static boolean isPropertyFinalised(final MetaProperty<?> property, final boolean persistedOnly, final boolean nullIsValueForPersisted) {
        final AbstractEntity<?> entity = property.getEntity();

        if (entity.isPersisted()) {
            return nullIsValueForPersisted || property.getOriginalValue() != null || (!persistedOnly && property.getValue() != null);
        } else {
            return !persistedOnly && property.getValue() != null;
        }
    }

}