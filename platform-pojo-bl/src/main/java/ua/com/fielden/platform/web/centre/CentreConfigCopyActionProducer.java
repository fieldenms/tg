package ua.com.fielden.platform.web.centre;

import static ua.com.fielden.platform.domaintree.impl.GlobalDomainTreeManager.DEFAULT_CONFIG_TITLE;

import java.util.Map;

import com.google.inject.Inject;

import ua.com.fielden.platform.dao.IEntityDao;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.DefaultEntityProducerWithContext;
import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.entity.factory.ICompanionObjectFinder;
import ua.com.fielden.platform.entity_centre.review.criteria.EnhancedCentreEntityQueryCriteria;
import ua.com.fielden.platform.error.Result;

/**
 * A producer for new instances of entity {@link CentreConfigCopyAction}.
 *
 * @author TG Team
 *
 */
public class CentreConfigCopyActionProducer extends DefaultEntityProducerWithContext<CentreConfigCopyAction> {
    public static final String KEY_PREFIX = "default";
    private static final String TITLE_SUFFIX = " (copy)";
    
    @Inject
    public CentreConfigCopyActionProducer(final EntityFactory factory, final ICompanionObjectFinder companionFinder) {
        super(factory, CentreConfigCopyAction.class, companionFinder);
    }
    
    @Override
    protected CentreConfigCopyAction provideDefaultValues(final CentreConfigCopyAction entity) {
        if (contextNotEmpty()) {
            // centre context holder is needed to restore criteria entity during saving and to perform 'centreCopier' closure
            entity.setCentreContextHolder(selectionCrit().centreContextHolder());
            
            final EnhancedCentreEntityQueryCriteria<?, ?> previouslyRunSelectionCrit = selectionCrit();
            
            // get modifHolder and apply it against 'fresh' centre to be able to identify validity of 'fresh' centre
            final Map<String, Object> freshModifHolder = previouslyRunSelectionCrit.centreContextHolder().getModifHolder();
            
            final EnhancedCentreEntityQueryCriteria<AbstractEntity<?>, ? extends IEntityDao<AbstractEntity<?>>> appliedFreshSelectionCrit = previouslyRunSelectionCrit.freshCentreApplier().apply(freshModifHolder);
            
            // 'saveAs' will not be created if current recently applied 'fresh' centre configuration is invalid
            appliedFreshSelectionCrit.isValid().ifFailure(Result::throwRuntime);
            
            entity.setKey(KEY_PREFIX + chosenProperty()); // need some prefix to make 'key' non-empty
            if (chosenPropertyRepresentsThisColumn()) {
                // remove brackets from title when copying 'default' centre configuration; brackets are not allowed as per CentreConfigCopyActionTitleValidator
                entity.setTitle(DEFAULT_CONFIG_TITLE.replace("[", "").replaceAll("]", "") + TITLE_SUFFIX);
            } else {
                entity.setTitle(chosenProperty() + TITLE_SUFFIX);
            }
        }
        return entity;
    }
    
}