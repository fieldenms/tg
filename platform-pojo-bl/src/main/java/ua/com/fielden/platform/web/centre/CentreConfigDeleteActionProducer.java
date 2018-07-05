package ua.com.fielden.platform.web.centre;

import static java.util.Optional.empty;
import static ua.com.fielden.platform.error.Result.failure;
import static ua.com.fielden.platform.web.centre.CentreConfigDeleteAction.DeleteKind.DELETE;
import static ua.com.fielden.platform.web.centre.CentreConfigUtils.getCustomObject;
import static ua.com.fielden.platform.web.centre.CentreConfigUtils.isDefaultOrInherited;

import com.google.inject.Inject;

import ua.com.fielden.platform.entity.DefaultEntityProducerWithContext;
import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.entity.factory.ICompanionObjectFinder;
import ua.com.fielden.platform.entity_centre.review.criteria.EnhancedCentreEntityQueryCriteria;

/**
 * A producer for new instances of entity {@link CentreConfigDeleteAction}.
 *
 * @author TG Team
 *
 */
public class CentreConfigDeleteActionProducer extends DefaultEntityProducerWithContext<CentreConfigDeleteAction> {
    private static final String ERR_CANNOT_BE_DELETED = "Can not be deleted.";
    
    @Inject
    public CentreConfigDeleteActionProducer(final EntityFactory factory, final ICompanionObjectFinder companionFinder) {
        super(factory, CentreConfigDeleteAction.class, companionFinder);
    }
    
    @Override
    protected CentreConfigDeleteAction provideDefaultValues(final CentreConfigDeleteAction entity) {
        if (contextNotEmpty()) {
            entity.setDeleteKind(chosenProperty());
            
            final EnhancedCentreEntityQueryCriteria<?, ?> selectionCrit = selectionCrit();
            if (DELETE.name().equals(entity.getDeleteKind())) {
                if (isDefaultOrInherited(selectionCrit.saveAsNameSupplier().get(), selectionCrit)) {
                    throw failure(ERR_CANNOT_BE_DELETED);
                }
                selectionCrit.centreDeleter().run();
            }
            selectionCrit.defaultCentreClearer().run();
            selectionCrit.preferredConfigMaker().accept(empty());
            entity.setCustomObject(getCustomObject(selectionCrit, selectionCrit.criteriaValidationPrototypeCreator().apply(empty()), empty()));
        }
        return entity;
    }
    
}