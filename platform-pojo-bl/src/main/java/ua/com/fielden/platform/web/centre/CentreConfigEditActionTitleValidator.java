package ua.com.fielden.platform.web.centre;

import static ua.com.fielden.platform.error.Result.failure;
import static ua.com.fielden.platform.error.Result.successful;
import static ua.com.fielden.platform.error.Result.warning;
import static ua.com.fielden.platform.web.centre.CentreConfigEditAction.EditKind.EDIT;
import static ua.com.fielden.platform.web.centre.CentreConfigEditAction.EditKind.valueOf;

import java.lang.annotation.Annotation;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import com.google.inject.Inject;

import ua.com.fielden.platform.domaintree.impl.GlobalDomainTreeManager;
import ua.com.fielden.platform.entity.meta.MetaProperty;
import ua.com.fielden.platform.entity.validation.IBeforeChangeEventHandler;
import ua.com.fielden.platform.error.Result;
import ua.com.fielden.platform.types.tuples.T2;
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
        if (newValue != null && (newValue.contains("[") || newValue.contains("]"))) {
            return failure("Brackets are not allowed.");
        } else if (newValue != null && (newValue.contains("{") || newValue.contains("}"))) {
            return failure("Curly braces are not allowed.");
        } else {
            final CentreConfigEditAction entity = property.getEntity();
            final T2<List<LoadableCentreConfig>, Optional<String>> configsAndSaveAsName = 
                criteriaEntityRestorer.restoreCriteriaEntity(entity.getCentreContextHolder())
                .loadableCentresSupplier().get();
            final Optional<String> saveAsName = configsAndSaveAsName._2;
            final String currentTitle = saveAsName.map(name -> name).orElse(GlobalDomainTreeManager.DEFAULT_CONFIG_TITLE);
            
            final boolean titleCanBeCurrent = EDIT.equals(valueOf(entity.getEditKind()));
            if (configsAndSaveAsName._1.stream()
                .filter(lcc -> titleCanBeCurrent ? !lcc.getKey().equals(currentTitle) : true)
                .anyMatch(lcc -> lcc.getKey().equals(newValue)))
            {
                return warning("Configuration with this title already exists.");
            }
        }
        return successful("ok");
    }
    
}
