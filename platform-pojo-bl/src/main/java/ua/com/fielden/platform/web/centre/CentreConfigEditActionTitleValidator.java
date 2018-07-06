package ua.com.fielden.platform.web.centre;

import static java.lang.String.format;
import static java.util.regex.Pattern.quote;
import static java.util.stream.Collectors.partitioningBy;
import static ua.com.fielden.platform.domaintree.impl.GlobalDomainTreeManager.LINK_CONFIG_TITLE;
import static ua.com.fielden.platform.domaintree.impl.GlobalDomainTreeManager.UNDEFINED_CONFIG_TITLE;
import static ua.com.fielden.platform.error.Result.failuref;
import static ua.com.fielden.platform.error.Result.successful;
import static ua.com.fielden.platform.error.Result.warning;
import static ua.com.fielden.platform.web.utils.EntityResourceUtils.CENTRE_CONFIG_CONFLICT_ERROR;
import static ua.com.fielden.platform.web.utils.EntityResourceUtils.CENTRE_CONFIG_CONFLICT_WARNING;

import java.lang.annotation.Annotation;
import java.util.List;
import java.util.Map;
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
            final AbstractCentreConfigEditAction entity = property.getEntity();
            if (entity.isSkipUi()) {
                return successful("ok");
            }
            final EnhancedCentreEntityQueryCriteria<?, ?> criteriaEntity = criteriaEntityRestorer.restoreCriteriaEntity(entity.getCentreContextHolder());
            
            final boolean titleCanBeCurrent = CentreConfigEditAction.class.isAssignableFrom(entity.getType());
            final Map<Boolean, List<LoadableCentreConfig>> possibleIntersections = criteriaEntity.loadableCentresSupplier().get().stream()
                .filter(lcc -> titleCanBeCurrent ? criteriaEntity.saveAsNameSupplier().get().map(name -> !lcc.getKey().equals(name)).orElse(true) : true)
                .collect(partitioningBy(lcc -> lcc.isInherited()));
            if (possibleIntersections.get(true).stream().anyMatch(lcc -> lcc.getKey().equals(newValue))) {
                return failuref(CENTRE_CONFIG_CONFLICT_ERROR);
            } else if (possibleIntersections.get(false).stream().anyMatch(lcc -> lcc.getKey().equals(newValue))) {
                return warning(CENTRE_CONFIG_CONFLICT_WARNING);
            }
        }
        return successful("ok");
    }
    
}
