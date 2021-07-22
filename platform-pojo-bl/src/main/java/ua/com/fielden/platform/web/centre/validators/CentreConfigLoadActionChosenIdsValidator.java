package ua.com.fielden.platform.web.centre.validators;

import static java.lang.String.format;
import static ua.com.fielden.platform.error.Result.successful;
import static ua.com.fielden.platform.error.Result.warning;
import static ua.com.fielden.platform.utils.EntityUtils.equalsEx;
import static ua.com.fielden.platform.web.centre.CentreConfigUtils.isDefault;

import java.lang.annotation.Annotation;
import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;

import com.google.inject.Inject;

import ua.com.fielden.platform.domaintree.centre.ICentreDomainTreeManager.ICentreDomainTreeManagerAndEnhancer;
import ua.com.fielden.platform.entity.meta.MetaProperty;
import ua.com.fielden.platform.entity.validation.IBeforeChangeEventHandler;
import ua.com.fielden.platform.entity_centre.review.criteria.EnhancedCentreEntityQueryCriteria;
import ua.com.fielden.platform.error.Result;
import ua.com.fielden.platform.web.centre.CentreConfigLoadAction;
import ua.com.fielden.platform.web.utils.ICriteriaEntityRestorer;

/**
 * Validator for {@code chosenIds} in {@link CentreConfigLoadAction}.
 * <p>
 * Calculates and assigns possible warning to 'centreConfigurations' in case if current configuration is default and has some changes.
 * Such configuration with changes can not be accessed again (one exception: manually clear uuid after transition) and the changes would be lost after transition.
 * Ways to return to default config include New, Duplicate and Delete actions -- they clears / overrides default configuration's changes.
 * 
 * @author TG Team
 *
 */
public class CentreConfigLoadActionChosenIdsValidator implements IBeforeChangeEventHandler<LinkedHashSet<String>> {
    private static final String WRN_DEFAULT_CONFIG_CHANGES_WILL_BE_LOST = "Changes in the %s for the default configuration will be lost upon loading another.";
    private final ICriteriaEntityRestorer criteriaEntityRestorer;
    
    @Inject
    public CentreConfigLoadActionChosenIdsValidator(final ICriteriaEntityRestorer criteriaEntityRestorer) {
        this.criteriaEntityRestorer = criteriaEntityRestorer;
    }
    
    @Override
    public Result handle(final MetaProperty<LinkedHashSet<String>> property, final LinkedHashSet<String> chosenIds, final Set<Annotation> mutatorAnnotations) {
        final CentreConfigLoadAction action = property.getEntity();
        final EnhancedCentreEntityQueryCriteria<?, ?> criteriaEntity = criteriaEntityRestorer.restoreCriteriaEntity(action.getCentreContextHolder());
        final Optional<String> saveAsName = criteriaEntity.saveAsName();
        final Result success = successful(chosenIds);
        if (isDefault(saveAsName) && !chosenIds.isEmpty()) { // provide warning only if some collectional editor config title has been chosen and current configuration is default
            final ICentreDomainTreeManagerAndEnhancer freshCentre = criteriaEntity.freshCentre();
            final ICentreDomainTreeManagerAndEnhancer savedCentre = criteriaEntity.savedCentre();
            final boolean selectionCritChanged = !equalsEx(freshCentre.getFirstTick(), savedCentre.getFirstTick());
            final boolean runAutomaticallyChanged = !equalsEx(criteriaEntity.centreRunAutomatically(saveAsName), criteriaEntity.defaultRunAutomatically());
            final boolean resultSetChanged = !equalsEx(freshCentre.getSecondTick(), savedCentre.getSecondTick());
            if (selectionCritChanged || runAutomaticallyChanged || resultSetChanged) {
                final String selCritPart = selectionCritChanged || runAutomaticallyChanged ? "selection criteria" : ""; // if runAutomatically changed then also show changes in selection criteria
                final String selCritPartAnd = "".equals(selCritPart) ? "" : selCritPart + " and ";
                final String bothParts = resultSetChanged ? selCritPartAnd + "result-set" : selCritPart;
                action.getProperty("centreConfigurations").setDomainValidationResult(
                    warning(format(WRN_DEFAULT_CONFIG_CHANGES_WILL_BE_LOST, bothParts))
                );
                return success;
            }
        }
        action.getProperty("centreConfigurations").setDomainValidationResult(success);
        return success;
    }
    
}