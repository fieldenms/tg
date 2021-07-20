package ua.com.fielden.platform.web.centre;

import static ua.com.fielden.platform.error.Result.failure;
import static ua.com.fielden.platform.web.centre.CentreConfigUtils.isDefaultOrLink;
import static ua.com.fielden.platform.web.centre.CentreConfigUtils.isInheritedFromBase;
import static ua.com.fielden.platform.web.centre.CentreConfigUtils.prepareDefaultCentre;

import com.google.inject.Inject;

import ua.com.fielden.platform.entity.DefaultEntityProducerWithContext;
import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.entity.factory.ICompanionObjectFinder;

/**
 * A producer for new instances of entity {@link CentreConfigDeleteAction}.
 *
 * @author TG Team
 *
 */
public class CentreConfigDeleteActionProducer extends DefaultEntityProducerWithContext<CentreConfigDeleteAction> {
    private static final String ERR_CANNOT_BE_DELETED = "Only saved and shared configurations can be deleted.";
    
    @Inject
    public CentreConfigDeleteActionProducer(final EntityFactory factory, final ICompanionObjectFinder companionFinder) {
        super(factory, CentreConfigDeleteAction.class, companionFinder);
    }
    
    @Override
    protected CentreConfigDeleteAction provideDefaultValues(final CentreConfigDeleteAction entity) {
        if (contextNotEmpty()) {
            // this will also throw early failure in case where current configuration was deleted
            if (isDefaultOrLink(selectionCrit().saveAsName()) || isInheritedFromBase(selectionCrit().saveAsName(), selectionCrit())) {
                throw failure(ERR_CANNOT_BE_DELETED);
            }
            selectionCrit().deleteCentre();
            entity.setCustomObject(prepareDefaultCentre(selectionCrit()));
        }
        return entity;
    }
    
}