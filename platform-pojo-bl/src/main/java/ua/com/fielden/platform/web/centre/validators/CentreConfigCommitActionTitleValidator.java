package ua.com.fielden.platform.web.centre.validators;

import static java.lang.String.format;
import static java.util.Optional.empty;
import static java.util.regex.Pattern.quote;
import static java.util.stream.Collectors.partitioningBy;
import static org.apache.commons.lang3.StringUtils.uncapitalize;
import static ua.com.fielden.platform.error.Result.failure;
import static ua.com.fielden.platform.error.Result.failuref;
import static ua.com.fielden.platform.error.Result.successful;
import static ua.com.fielden.platform.error.Result.warning;
import static ua.com.fielden.platform.web.centre.WebApiUtils.LINK_CONFIG_TITLE;
import static ua.com.fielden.platform.web.centre.WebApiUtils.UNDEFINED_CONFIG_TITLE;
import static ua.com.fielden.platform.web.utils.EntityResourceUtils.WARN_CENTRE_CONFIG_CONFLICT;

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
import ua.com.fielden.platform.web.centre.AbstractCentreConfigCommitAction;
import ua.com.fielden.platform.web.centre.CentreConfigEditAction;
import ua.com.fielden.platform.web.centre.LoadableCentreConfig;
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
            return failuref("Only alphanumeric characters, spaces and %s are allowed.", specialCharacters);
        } else {
            final AbstractCentreConfigCommitAction entity = property.getEntity();
            final EnhancedCentreEntityQueryCriteria<?, ?> criteriaEntity = criteriaEntityRestorer.restoreCriteriaEntity(entity.getCentreContextHolder());
            final Optional<String> saveAsName = criteriaEntity.saveAsName();
            
            // find possible intersections -- configuration entities with 'inherited' marker to be compared with 'newValue' title
            // in case of EDIT action (unlike SAVEAS) we need to skip currently loaded configuration when determining 'possible intersections' -- this is because 
            //  when EDIT is performed the user could leave title unchanged and change only description
            final boolean titleCanBeCurrent = CentreConfigEditAction.class.isAssignableFrom(entity.getType());
            final Map<Boolean, List<LoadableCentreConfig>> possibleIntersections = criteriaEntity.loadableCentreConfigs().apply(empty()).stream()
                .filter(config -> titleCanBeCurrent ? saveAsName.map(name -> !config.getKey().equals(name)).orElse(true) : true)
                .collect(partitioningBy(LoadableCentreConfig::isInherited)); // split possible intersections by 'inherited' marker
            
            final Optional<LoadableCentreConfig> inheritedIntersection = possibleIntersections.get(true).stream().filter(config -> config.getKey().equalsIgnoreCase(newTitle)).findAny();
            if (inheritedIntersection.isPresent()) { // inherited configuration conflict should fail title editing
                return failure((inheritedIntersection.get().isShared() ? "Shared" : "Base") + " " + uncapitalize(WARN_CENTRE_CONFIG_CONFLICT));
            } else if (possibleIntersections.get(false).stream().anyMatch(config -> config.getKey().equalsIgnoreCase(newTitle))) { // owned configuration should provide warning and, if saved, there should be a prompt about 'configuration override'
                return warning(WARN_CENTRE_CONFIG_CONFLICT);
            }
        }
        return successful("ok");
    }
    
}