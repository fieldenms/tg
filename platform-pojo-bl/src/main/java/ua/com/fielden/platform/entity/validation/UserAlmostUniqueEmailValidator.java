package ua.com.fielden.platform.entity.validation;

import static java.lang.String.format;
import static ua.com.fielden.platform.entity.AbstractEntity.ID;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.select;
import static ua.com.fielden.platform.error.Result.failure;
import static ua.com.fielden.platform.error.Result.successful;

import java.lang.annotation.Annotation;
import java.util.Set;

import com.google.inject.Inject;

import ua.com.fielden.platform.entity.exceptions.EntityException;
import ua.com.fielden.platform.entity.factory.ICompanionObjectFinder;
import ua.com.fielden.platform.entity.meta.MetaProperty;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.ICompoundCondition0;
import ua.com.fielden.platform.entity.query.model.EntityResultQueryModel;
import ua.com.fielden.platform.error.Result;
import ua.com.fielden.platform.security.user.IUser;
import ua.com.fielden.platform.security.user.User;

/**
 * A validator to check that a passed in value is unique for an entity/property combination.
 *
 * IMPORTANT: value <code>null</code> is always considered valid and permissible for multiple entity instances (i.e. <code>NULL = NULL</code> is <code>false</code>).
 *
 * @author TG Team
 *
 */
public class UserAlmostUniqueEmailValidator implements IBeforeChangeEventHandler<String> {

    public static final String validationErrorTemplate = "Value [%s] must be unique for property [%s] in entity [%s].";
    
    private final ICompanionObjectFinder coFinder;

    @Inject
    public UserAlmostUniqueEmailValidator(final ICompanionObjectFinder coFinder) {
        this.coFinder = coFinder;
    }

    @Override
    public Result handle(final MetaProperty<String> property, final String newValue, final Set<Annotation> mutatorAnnotations) {
        final User user = property.getEntity();
        // multiple NULL email addresses are permitted
        if (newValue == null) {
            return successful(user);
        }

        final IUser co = coFinder.find(User.class);
        if (co == null) {
            throw new EntityException(format("Companion object is missing for entity [%s].", User.class.getName()));
        }

        // if non-base then check uniqueness globally
        // if base then check uniqueness outside of the base-users club
        final ICompoundCondition0<User> q = user.isPersisted() 
                        ? select(User.class).where().prop(ID).ne().val(user.getId()).and().prop(property.getName()).eq().val(newValue)
                        : select(User.class).where().prop(property.getName()).eq().val(newValue);
        final EntityResultQueryModel<User> query = !user.isBase() ? q.model() : q.and().prop("base").eq().val(false).model();

        if (co.exists(query)) {
            return failure(format(validationErrorTemplate, newValue, property.getName(), User.class.getName()));
        }
        
        return successful(user);
    }

}
