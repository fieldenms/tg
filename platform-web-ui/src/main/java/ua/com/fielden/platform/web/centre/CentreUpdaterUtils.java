package ua.com.fielden.platform.web.centre;

import static java.lang.String.format;
import static ua.com.fielden.platform.domaintree.impl.AbstractDomainTree.validateRootType;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.fetchOnly;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.from;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.select;
import static ua.com.fielden.platform.reflection.AnnotationReflector.getKeyType;
import static ua.com.fielden.platform.types.either.Either.left;
import static ua.com.fielden.platform.types.either.Either.right;
import static ua.com.fielden.platform.utils.CollectionUtil.setOf;

import static ua.com.fielden.platform.utils.EntityUtils.fetchWithKeyAndDesc;

import java.util.List;

import org.apache.log4j.Logger;

import ua.com.fielden.platform.domaintree.centre.ICentreDomainTreeManager.ICentreDomainTreeManagerAndEnhancer;
import ua.com.fielden.platform.domaintree.centre.impl.CentreDomainTreeManagerAndEnhancer;
import ua.com.fielden.platform.domaintree.exceptions.DomainTreeException;
import ua.com.fielden.platform.entity.AbstractBatchAction;
import ua.com.fielden.platform.entity.annotation.EntityType;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.ICompoundCondition0;
import ua.com.fielden.platform.entity.query.model.EntityResultQueryModel;
import ua.com.fielden.platform.security.user.User;
import ua.com.fielden.platform.serialisation.api.ISerialiser;
import ua.com.fielden.platform.types.either.Either;
import ua.com.fielden.platform.ui.config.EntityCentreConfig;
import ua.com.fielden.platform.ui.config.api.IEntityCentreConfig;
import ua.com.fielden.platform.web.app.IWebUiConfig;

/**
 * This utility class contains additional methods applicable to {@link CentreUpdater}.
 *
 * @author TG Team
 *
 */
public class CentreUpdaterUtils extends CentreUpdater {
    private final static Logger logger = Logger.getLogger(CentreUpdaterUtils.class);
    
    /** Protected default constructor to prevent instantiation. */
    protected CentreUpdaterUtils() {
    }
    
    ///////////////////////////// CENTRE CREATION /////////////////////////////
    protected static ICentreDomainTreeManagerAndEnhancer createDefaultCentre(
            final Class<?> menuItemType,
            final IWebUiConfig webUiConfig) {
        final EntityCentre entityCentre = webUiConfig.getCentres().get(menuItemType);
        if (entityCentre != null) {
            return entityCentre.createDefaultCentre();
        } else {
            throw errorf("EntityCentre instance could not be found for [%s] menu item type.", menuItemType.getSimpleName());
        }
    }
    
    public static ICentreDomainTreeManagerAndEnhancer createEmptyCentre(final Class<?> root, final ISerialiser serialiser) {
        // TODO next line of code must take in to account that the menu item is for association centre.
        final CentreDomainTreeManagerAndEnhancer centre = new CentreDomainTreeManagerAndEnhancer(serialiser, setOf(root));
        // initialise checkedProperties tree to make it more predictable in getting meta-info from "checkedProperties"
        centre.getFirstTick().checkedProperties(root);
        centre.getSecondTick().checkedProperties(root);
        return centre;
    }
    
    ///////////////////////////// ERROR REPORTING /////////////////////////////
    /**
     * Logs and returns an {@link DomainTreeException} error with specified message.
     *
     * @param message
     */
    private static DomainTreeException error(final String message) {
        logger.error(message);
        return new DomainTreeException(message); // TODO migrate to CentreUpdaterException? don't forget to adjust the usage of DomainTreeException.
    }
    
    private static DomainTreeException errorf(final String message, final Object... args) {
        return error(format(message, args));
    }
    
    ///////////////////////////// VALIDATION /////////////////////////////
    /**
     * Validates the type of menu item (a part of centre key) to be actually "menu item type".
     *
     * @param menuItemType
     */
    private static void validateMenuItemType(final Class<?> menuItemType) {
        try {
            final Class<?> parentClassForMenuItems = ClassLoader.getSystemClassLoader().loadClass("ua.com.fielden.platform.ui.menu.MiWithConfigurationSupport");
            if (!parentClassForMenuItems.isAssignableFrom(menuItemType)) {
                throw errorf("The menu item type %s is not 'MiWithConfigurationSupport' descendant, which should be a parent type for menu items for all entity centres.", menuItemType.getSimpleName());
            }
        } catch (final ClassNotFoundException e) {
            throw errorf("There are no loaded class 'MiWithConfigurationSupport', which should be a parent type for menu items for entity centres. [%s]", e.getMessage());
        }
    }
    
    private static Class<?> validateMenuItemTypeRootType(final Class<?> menuItemType) {
        final EntityType etAnnotation = menuItemType.getAnnotation(EntityType.class);
        if (etAnnotation == null || etAnnotation.value() == null) {
            throw errorf("The menu item type %s has no 'EntityType' annotation, which is necessary to specify the root type of the centre.", menuItemType.getSimpleName());
        }
        final Class<?> root = etAnnotation.value();
        validateRootType(root);
        return getValidatedRootIfAssociation(root);
    }
    
    /**
     * Returns the key type of the entity if it is a association batch action entity.
     *
     * @param value
     * @return
     */
    private static Class<?> getValidatedRootIfAssociation(final Class<?> value) {
        if (AbstractBatchAction.class.isAssignableFrom(value)) {
            final Class<?> root = getKeyType(value);
            validateRootType(root);
            return root;
        }
        return value;
    }
    
    
    
    ///////////////////////////// CENTRE MAINTENANCE /////////////////////////////
    public Either<ICentreDomainTreeManagerAndEnhancer, DomainTreeException> initEntityCentreManager(final User user, final Class<?> menuItemType, final String name, final IEntityCentreConfig eccCompanion) {
        synchronized (this) { // TODO remove!
            logger.info(format("Initialising entity-centre instance for type [%s] with name [%s] for user [%s]...", menuItemType.getSimpleName(), name, user));
            
            validateMenuItemType(menuItemType);
            final Class<?> root = validateMenuItemTypeRootType(menuItemType);
            
            final String menuItemTypeName = menuItemType.getName();
            
            final EntityResultQueryModel<EntityCentreConfig> model = modelFor(user, menuItemTypeName, name);
            
            // final int count = coEntityCentreConfig.count(model);
            final List<EntityCentreConfig> firstTwoConfigs = eccCompanion.getFirstEntities(from(model).model(), 2);
            if (firstTwoConfigs.size() > 1) { 
                
            }
            
            
            
            final Either<ICentreDomainTreeManagerAndEnhancer, DomainTreeException> centreOrEx;
            if (count == 1) { // the persistence layer contains a entity-centre, so it should be retrieved and deserialised
                centreOrEx = left(retrieveAndInit(menuItemType, name, root, model));
            } else if (count < 1) { // there is no entity-centre
                centreOrEx = right(new DomainTreeException("Unable to initialise a non-existent entity-centre instance for type [" + menuItemType.getSimpleName() + "] with title [" + title + "] for current user ["
                        + currentUser() + "]."));
            } else {
                centreOrEx = right(new DomainTreeException("There are more than one entity-centre instance for type [" + menuItemType.getSimpleName() + "] with title [" + title + "] for current user [" + currentUser()
                        + "]."));
            }
            logger.info("Initialised_ entity-centre instance for type [" + menuItemType.getSimpleName() + "] with title [" + title(menuItemType, name) + "] for current user ["
                    + currentUser() + "]...done");
            return centreOrEx;
        }
    }
    
//    /**
//     * Gets a {@link EntityCentreConfig#getTitle()} component in {@link EntityCentreConfig}'s key from {@link IGlobalDomainTreeManager}'s contract for entity-centre naming -- 'menu
//     * item type name' or 'name' (in case of 'saveAs' centre).
//     *
//     * @param menuItemType
//     * @param name
//     * @return
//     */
//    private static String title(final Class<?> menuItemType, final String name) {
//        // validateMenuItemType(menuItemType);
//        validateMenuItemTypeRootType(menuItemType);
//        return name == null ? menuItemType.getName() : name;
//    }
    
    ///////////////////////////// EQL MODELS /////////////////////////////
    /**
     * Creates partial model to retrieve {@link EntityCentreConfig} instances for specified <code>user</code> and <code>menuItemTypeName</code>.
     *
     * @param user
     * @param menuItemTypeName
     * 
     * @return
     */
    private ICompoundCondition0<EntityCentreConfig> modelFor(final User user, final String menuItemTypeName) {
        return select(EntityCentreConfig.class).where()
            .prop("owner").eq().val(user).and() // look for entity-centres for only current user
            .prop("menuItem.key").eq().val(menuItemTypeName);
    }
    
    /**
     * Creates a model to retrieve {@link EntityCentreConfig} instances for specified <code>user</code>, <code>title</code> and <code>menuItemTypeName</code>.
     *
     * @param user
     * @param menuItemTypeName
     * @param title
     * 
     * @return
     */
    private EntityResultQueryModel<EntityCentreConfig> modelFor(final User user, final String menuItemTypeName, final String title) {
        return modelFor(user, menuItemTypeName).and()
            .prop("title").eq().val(title).model();
    }
    
    /**
     * Creates a model to retrieve {@link EntityCentreConfig} instances for specified <code>user</code>, <code>titles</code> and <code>menuItemTypeName</code>.
     *
     * @param user
     * @param menuItemTypeName
     * @param titles
     * 
     * @return
     */
    private EntityResultQueryModel<EntityCentreConfig> multiModelFor(final User user, final String menuItemTypeName, final String... titles) {
        return modelFor(user, menuItemTypeName).and()
            .prop("title").in().values(titles).model();
    }
    
}