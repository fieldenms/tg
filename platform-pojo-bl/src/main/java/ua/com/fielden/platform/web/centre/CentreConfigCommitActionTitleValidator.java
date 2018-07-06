package ua.com.fielden.platform.web.centre;

import static java.lang.String.format;
import static java.util.regex.Pattern.quote;
import static java.util.stream.Collectors.partitioningBy;
import static ua.com.fielden.platform.domaintree.impl.GlobalDomainTreeManager.LINK_CONFIG_TITLE;
import static ua.com.fielden.platform.domaintree.impl.GlobalDomainTreeManager.UNDEFINED_CONFIG_TITLE;
import static ua.com.fielden.platform.error.Result.failure;
import static ua.com.fielden.platform.error.Result.failuref;
import static ua.com.fielden.platform.error.Result.successful;
import static ua.com.fielden.platform.error.Result.warning;
import static ua.com.fielden.platform.web.utils.EntityResourceUtils.CENTRE_CONFIG_CONFLICT_ERROR;
import static ua.com.fielden.platform.web.utils.EntityResourceUtils.CENTRE_CONFIG_CONFLICT_WARNING;

import java.lang.annotation.Annotation;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import com.google.inject.Inject;

import ua.com.fielden.platform.entity.meta.MetaProperty;
import ua.com.fielden.platform.entity.validation.IBeforeChangeEventHandler;
import ua.com.fielden.platform.entity_centre.review.criteria.EnhancedCentreEntityQueryCriteria;
import ua.com.fielden.platform.error.Result;
import ua.com.fielden.platform.web.utils.ICriteriaEntityRestorer;

/**
 * Validator for centre configuration title in {@link AbstractCentreConfigCommitAction} descendants.
 * 
 * @author TG Team
 *
 */
public class CentreConfigCommitActionTitleValidator implements IBeforeChangeEventHandler<String> {
    private final ICriteriaEntityRestorer criteriaEntityRestorer;
    
    @Inject
    public CentreConfigCommitActionTitleValidator(final ICriteriaEntityRestorer criteriaEntityRestorer) {
        this.criteriaEntityRestorer = criteriaEntityRestorer;
    }
    
    @Override
    public Result handle(final MetaProperty<String> property, final String newTitle, final Set<Annotation> mutatorAnnotations) {
        final String spaceQuoted = quote(" "); // spaces are allowed and they must be encoded using %20 in URIs
        final String specialCharacters = "$-_.+!*'(),"; // these special characters are not required to be encoded in URIs (see https://perishablepress.com/stop-using-unsafe-characters-in-urls/ for more details)
        final String specialCharactersQuoted = quote(specialCharacters);
        if (newTitle == null || UNDEFINED_CONFIG_TITLE.equals(newTitle) || LINK_CONFIG_TITLE.equals(newTitle) || !newTitle.matches(format("[\\w%s%s]*", specialCharactersQuoted, spaceQuoted))) {
            return failuref("Only alfanumeric characters, spaces and %s are allowed.", specialCharacters);
        } else {
            final AbstractCentreConfigCommitAction entity = property.getEntity();
            final EnhancedCentreEntityQueryCriteria<?, ?> criteriaEntity = criteriaEntityRestorer.restoreCriteriaEntity(entity.getCentreContextHolder());
            final Optional<String> saveAsName = criteriaEntity.saveAsNameSupplier().get();
            
            // find possible intersections -- configuration entities with 'inherited' marker to be compared with 'newValue' title
            // in case of EDIT action (unlike SAVEAS) we need to skip currently loaded configuration when determining 'possible intersections' -- this is because 
            //  when EDIT is performed the user could leave title unchanged and change only description
            final boolean titleCanBeCurrent = CentreConfigEditAction.class.isAssignableFrom(entity.getType());
            final Map<Boolean, List<LoadableCentreConfig>> possibleIntersections = criteriaEntity.loadableCentresSupplier().get().stream()
                .filter(config -> titleCanBeCurrent ? saveAsName.map(name -> !config.getKey().equals(name)).orElse(true) : true)
                .collect(partitioningBy(config -> config.isInherited())); // split possible intersections by 'inherited' marker
            
            if (match(newTitle, possibleIntersections.get(true))) { // inherited configuration conflict should fail title editing
                return failure(CENTRE_CONFIG_CONFLICT_ERROR);
            } else if (match(newTitle, possibleIntersections.get(false))) { // owned configuration should provide warning and, if saved, there should be a prompt about 'configuration override'
                return warning(CENTRE_CONFIG_CONFLICT_WARNING);
            }
        }
        return successful("ok");
    }
    
    /**
     * Returns <code>true</code> if <code>title</code> matches the key of some config from <code>intersections</code>, <code>false</code> otherwise.
     * 
     * @param title
     * @param intersections
     * @return
     */
    private static boolean match(final String title, final List<LoadableCentreConfig> intersections) {
        return intersections.stream().anyMatch(config -> config.getKey().equals(title));
    }
    
}