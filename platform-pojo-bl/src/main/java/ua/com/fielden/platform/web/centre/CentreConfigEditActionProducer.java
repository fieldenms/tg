package ua.com.fielden.platform.web.centre;

import static ua.com.fielden.platform.domaintree.impl.GlobalDomainTreeManager.DEFAULT_CONFIG_TITLE;
import static ua.com.fielden.platform.utils.EntityUtils.equalsEx;
import static ua.com.fielden.platform.web.centre.CentreConfigEditAction.EditKind.COPY;

import java.util.Map;
import java.util.Optional;

import com.google.inject.Inject;

import ua.com.fielden.platform.dao.IEntityDao;
import ua.com.fielden.platform.domaintree.IGlobalDomainTreeManager;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.DefaultEntityProducerWithContext;
import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.entity.factory.ICompanionObjectFinder;
import ua.com.fielden.platform.entity_centre.review.criteria.EnhancedCentreEntityQueryCriteria;
import ua.com.fielden.platform.error.Result;
import ua.com.fielden.platform.types.tuples.T2;
import ua.com.fielden.platform.types.tuples.T3;
import ua.com.fielden.platform.ui.menu.MiWithConfigurationSupport;
import ua.com.fielden.platform.web.interfaces.DeviceProfile;

/**
 * A producer for new instances of entity {@link CentreConfigEditAction}.
 *
 * @author TG Team
 *
 */
public class CentreConfigEditActionProducer extends DefaultEntityProducerWithContext<CentreConfigEditAction> {
    private static final String COPY_ACTION_SUFFIX = " (copy)";
    
    @Inject
    public CentreConfigEditActionProducer(final EntityFactory factory, final ICompanionObjectFinder companionFinder) {
        super(factory, CentreConfigEditAction.class, companionFinder);
    }
    
    @Override
    protected CentreConfigEditAction provideDefaultValues(final CentreConfigEditAction entity) {
        if (contextNotEmpty()) {
            // centre context holder is needed to restore criteria entity during saving and to perform 'centreCopier' closure
            entity.setCentreContextHolder(selectionCrit().centreContextHolder());
            
            final EnhancedCentreEntityQueryCriteria<?, ?> previouslyRunSelectionCrit = selectionCrit();
            
            // get modifHolder and apply it against 'fresh' centre to be able to identify validity of 'fresh' centre
            final Map<String, Object> freshModifHolder = previouslyRunSelectionCrit.centreContextHolder().getModifHolder();
            
            final EnhancedCentreEntityQueryCriteria<AbstractEntity<?>, ? extends IEntityDao<AbstractEntity<?>>> appliedFreshSelectionCrit = previouslyRunSelectionCrit.freshCentreApplier().apply(freshModifHolder);
            
            // configuration copy / edit will not be performed if current recently applied 'fresh' centre configuration is invalid
            appliedFreshSelectionCrit.isValid().ifFailure(Result::throwRuntime);
            
            entity.setEditKind(chosenProperty());
            final T2<String, String> titleAndDesc = previouslyRunSelectionCrit.centreTitleAndDescGetter().get();
            final String title = titleAndDesc._1;
            final String desc = titleAndDesc._2;
            
            final boolean copyAction = COPY.name().equals(entity.getEditKind()) || DEFAULT_CONFIG_TITLE.equals(title);
            final String actionKindSuffix = copyAction ? COPY_ACTION_SUFFIX : "";
            if (DEFAULT_CONFIG_TITLE.equals(title)) {
                // remove brackets from title when copying 'default' centre configuration; brackets are not allowed as per CentreConfigEditActionTitleValidator
                entity.setTitle(title.replace("[", "").replace("]", "") + actionKindSuffix);
            } else {
                entity.setTitle(title + actionKindSuffix);
            }
            if (!copyAction) {
                final T3<Class<? extends MiWithConfigurationSupport<?>>, IGlobalDomainTreeManager, DeviceProfile> miTypeGdtmAndDevice = previouslyRunSelectionCrit.miTypeGdtmAndDeviceSupplier().get();
                final Optional<String> preferredConfig = retrievePreferredConfigName(miTypeGdtmAndDevice._2, miTypeGdtmAndDevice._1, miTypeGdtmAndDevice._3);
                final Optional<String> editedConfig = previouslyRunSelectionCrit.saveAsNameSupplier().get();
                entity.setPreferred(equalsEx(editedConfig, preferredConfig));
            }
            entity.setDesc(desc + actionKindSuffix);
        }
        return entity;
    }
    
    /**
     * Determines the preferred configuration <code>saveAsName</code> for the current user (defined by <code>gdtm.getUserProvider().getUser()</code>), the specified <code>device</code> and concrete 
     * <code>miType</code>'ed menu item.
     * 
     * @param gdtm
     * @param miType
     * @param device
     * @return
     */
    public static Optional<String> retrievePreferredConfigName(final IGlobalDomainTreeManager gdtm, final Class<? extends MiWithConfigurationSupport<?>> miType, final DeviceProfile device) {
        return Optional.of("Default (x)"); // TODO implement
    }
    
}