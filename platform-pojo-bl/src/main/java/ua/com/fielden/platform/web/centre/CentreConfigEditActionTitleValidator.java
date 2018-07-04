package ua.com.fielden.platform.web.centre;

import static java.lang.String.format;
import static java.util.regex.Pattern.quote;
import static ua.com.fielden.platform.domaintree.impl.GlobalDomainTreeManager.LINK_CONFIG_TITLE;
import static ua.com.fielden.platform.domaintree.impl.GlobalDomainTreeManager.UNDEFINED_CONFIG_TITLE;
import static ua.com.fielden.platform.error.Result.failuref;
import static ua.com.fielden.platform.error.Result.successful;
import static ua.com.fielden.platform.error.Result.warning;
import static ua.com.fielden.platform.web.centre.CentreConfigEditAction.EditKind.EDIT;
import static ua.com.fielden.platform.web.centre.CentreConfigEditAction.EditKind.valueOf;
import static ua.com.fielden.platform.web.utils.EntityResourceUtils.CENTRE_CONFIG_CONFLICT_WARNING;

import java.lang.annotation.Annotation;
import java.util.Set;

import com.google.inject.Inject;

import ua.com.fielden.platform.entity.meta.MetaProperty;
import ua.com.fielden.platform.entity.validation.IBeforeChangeEventHandler;
import ua.com.fielden.platform.entity_centre.review.criteria.EnhancedCentreEntityQueryCriteria;
import ua.com.fielden.platform.error.Result;
import ua.com.fielden.platform.web.utils.ICriteriaEntityRestorer;

/**
 * Validator for centre configuration title in {@link CentreConfigEditAction}.
 * 
 * @author TG Team
 *
 */
public class CentreConfigEditActionTitleValidator implements IBeforeChangeEventHandler<String> {
    private final ICriteriaEntityRestorer criteriaEntityRestorer;
    
    @Inject
    public CentreConfigEditActionTitleValidator(final ICriteriaEntityRestorer criteriaEntityRestorer) {
        this.criteriaEntityRestorer = criteriaEntityRestorer;
    }
    
    @Override
    public Result handle(final MetaProperty<String> property, final String newValue, final Set<Annotation> mutatorAnnotations) {
        final String spaceQuoted = quote(" "); // spaces are allowed and they must be encoded using %20 in URIs
        final String specialCharacters = "$-_.+!*'(),"; // these special characters are not required to be encoded in URIs (see https://perishablepress.com/stop-using-unsafe-characters-in-urls/ for more details)
        final String specialCharactersQuoted = quote(specialCharacters);
        if (newValue == null || UNDEFINED_CONFIG_TITLE.equals(newValue) || LINK_CONFIG_TITLE.equals(newValue) || !newValue.matches(format("[\\w%s%s]*", specialCharactersQuoted, spaceQuoted))) {
            return failuref("Only alfanumeric characters, spaces and %s are allowed.", specialCharacters);
        } else {
            final CentreConfigEditAction entity = property.getEntity();
            if (entity.isSkipUi()) {
                return successful("ok");
            }
            final EnhancedCentreEntityQueryCriteria<?, ?> criteriaEntity = criteriaEntityRestorer.restoreCriteriaEntity(entity.getCentreContextHolder());
            
            final boolean titleCanBeCurrent = EDIT.equals(valueOf(entity.getEditKind()));
            if (criteriaEntity.saveAsNameSupplier().get().isPresent() 
                && criteriaEntity.loadableCentresSupplier().get().stream()
                .filter(lcc -> titleCanBeCurrent ? !lcc.getKey().equals(criteriaEntity.saveAsNameSupplier().get().get()) : true)
                .anyMatch(lcc -> lcc.getKey().equals(newValue)))
            {
                return warning(CENTRE_CONFIG_CONFLICT_WARNING);
            }
        }
        return successful("ok");
    }
    
}
