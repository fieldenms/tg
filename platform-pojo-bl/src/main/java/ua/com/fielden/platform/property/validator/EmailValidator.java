package ua.com.fielden.platform.property.validator;

import static java.lang.String.format;
import java.lang.annotation.Annotation;
import java.util.Set;

import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;

import ua.com.fielden.platform.entity.meta.MetaProperty;
import ua.com.fielden.platform.entity.validation.IBeforeChangeEventHandler;
import ua.com.fielden.platform.error.Result;

/**
 * Can be used to validate a property of type string to match a valid email address.
 *
 * @author TG Team
 *
 */
public class EmailValidator implements IBeforeChangeEventHandler<String> {

    public static final String validationErrorTemplate = "Value [%s] for property [%s] in entity [%s] is not a valid email address.";
    
    @Override
    public Result handle(final MetaProperty<String> property, final String newValue, final String oldValue, final Set<Annotation> mutatorAnnotations) {
        if (newValue != null && !isValidEmailAddress(newValue)) {
            return Result.failure(format(validationErrorTemplate, newValue, property.getName(), property.getEntity().getType().getSimpleName()));
        }

        return Result.successful(newValue);
    }
    
    public static boolean isValidEmailAddress(String email) {
        boolean result = true;
        try {
            new InternetAddress(email).validate();
        } catch (AddressException ex) {
           result = false;
        }
        return result;
     }


}
