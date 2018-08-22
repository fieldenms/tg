package ua.com.fielden.platform.domaintree.impl;

import static java.lang.String.format;
import static java.util.Arrays.stream;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.from;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.select;
import static ua.com.fielden.platform.types.either.Either.left;
import static ua.com.fielden.platform.types.either.Either.right;
import static ua.com.fielden.platform.utils.EntityUtils.fetchWithKeyAndDesc;

import java.lang.annotation.Annotation;
import java.util.HashSet;
import java.util.Optional;
import org.apache.log4j.Logger;
import org.joda.time.DateTime;
import org.joda.time.Period;

import com.google.inject.Inject;

import ua.com.fielden.platform.domaintree.IGlobalDomainTreeManager;
import ua.com.fielden.platform.domaintree.centre.ICentreDomainTreeManager.ICentreDomainTreeManagerAndEnhancer;
import ua.com.fielden.platform.domaintree.centre.impl.CentreDomainTreeManagerAndEnhancer;
import ua.com.fielden.platform.domaintree.exceptions.DomainTreeException;
import ua.com.fielden.platform.entity.AbstractBatchAction;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.annotation.EntityType;
import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.entity.query.model.EntityResultQueryModel;
import ua.com.fielden.platform.reflection.AnnotationReflector;
import ua.com.fielden.platform.reflection.asm.impl.DynamicEntityClassLoader;
import ua.com.fielden.platform.security.user.IUser;
import ua.com.fielden.platform.security.user.IUserProvider;
import ua.com.fielden.platform.security.user.User;
import ua.com.fielden.platform.serialisation.api.ISerialiser;
import ua.com.fielden.platform.types.either.Either;
import ua.com.fielden.platform.ui.config.EntityCentreConfig;
import ua.com.fielden.platform.ui.config.MainMenuItem;
import ua.com.fielden.platform.ui.config.api.IEntityCentreConfig;
import ua.com.fielden.platform.ui.config.api.IMainMenuItem;
import ua.com.fielden.platform.utils.EntityUtils;

/**
 * The global domain tree manager implementation.
 *
 * @author TG Team
 *
 */
public class GlobalDomainTreeManager extends AbstractDomainTree implements IGlobalDomainTreeManager {
    private static final Logger logger = Logger.getLogger(GlobalDomainTreeManager.class);
    
    /**
     * The surrogate title of not yet known configuration. This is used during first time centre loading.
     */
    public static final String UNDEFINED_CONFIG_TITLE = "_______________________undefined";
    
    /**
     * The surrogate title of centre 'link' configuration. This is used when link with centre parameters opens.
     */
    public static final String LINK_CONFIG_TITLE = "_______________________link";
    
    private final EntityFactory factory;
    private final IUserProvider userProvider;
    private final IMainMenuItem mainMenuItemController;
    private final IEntityCentreConfig coEntityCentreConfig;
    
    private final IUser coUser;
    
    @Inject
    public GlobalDomainTreeManager(
            final ISerialiser serialiser,
            final EntityFactory factory,
            final IUserProvider userProvider,
            final IMainMenuItem mainMenuItemController,
            final IEntityCentreConfig coEntityCentreConfig,
            final IUser coUser) {
        super(serialiser);
        this.factory = factory;
        this.userProvider = userProvider;
        this.mainMenuItemController = mainMenuItemController;
        this.coEntityCentreConfig = coEntityCentreConfig;
        this.coUser = coUser;
    }
    
    @Override
    public IUserProvider getUserProvider() {
        return userProvider;
    }
    
    /**
     * Validates the type of menu item (a part of centre key) to be actually "menu item type".
     *
     * @param menuItemType
     */
    protected void validateMenuItemType(final Class<?> menuItemType) {
        try {
            final Class<?> parentClassForMenuItems = ClassLoader.getSystemClassLoader().loadClass("ua.com.fielden.platform.ui.menu.MiWithConfigurationSupport");
            if (!parentClassForMenuItems.isAssignableFrom(menuItemType)) {
                error("The menu item type " + menuItemType.getSimpleName()
                        + " is not 'MiWithConfigurationSupport' descendant, which should be a parent type for menu items for all entity centres.");
            }
        } catch (final ClassNotFoundException e) {
            e.printStackTrace();
            error("There are no loaded class 'MiWithConfigurationSupport', which should be a parent type for menu items for entity centres. [" + e.getMessage() + "]");
        }
    }
    
    public static Class<?> validateMenuItemTypeRootType(final Class<?> menuItemType) {
        final EntityType etAnnotation = menuItemType.getAnnotation(EntityType.class);
        if (etAnnotation == null || etAnnotation.value() == null) {
            error("The menu item type " + menuItemType.getSimpleName() + " has no 'EntityType' annotation, which is necessary to specify the root type of the centre.");
        }
        final Class<? extends AbstractEntity> root = etAnnotation.value();
        validateRootType(root);
        return getValidatedRootIfAssociation(root);
    }
    
    /**
     * Returns the key type of the entity if it is a association batch action entity.
     *
     * @param value
     * @return
     */
    @SuppressWarnings("rawtypes")
    private static Class<?> getValidatedRootIfAssociation(final Class<? extends AbstractEntity> value) {
        if (AbstractBatchAction.class.isAssignableFrom(value)) {
            final Class<?> root = AnnotationReflector.getKeyType(value);
            validateRootType(root);
            return root;
        }
        return value;
    }
    
    private User currentUser() {
        return userProvider.getUser();
    }
    
    /**
     * Gets a {@link EntityCentreConfig#getTitle()} component in {@link EntityCentreConfig}'s key from {@link IGlobalDomainTreeManager}'s contract for entity-centre naming -- 'menu
     * item type name' or 'name' (in case of 'saveAs' centre).
     *
     * @param menuItemType
     * @param name
     * @return
     */
    protected static String title(final Class<?> menuItemType, final String name) {
        // validateMenuItemType(menuItemType);
        validateMenuItemTypeRootType(menuItemType);
        return name == null ? menuItemType.getName() : name;
    }
    
    @Override
    public Either<ICentreDomainTreeManagerAndEnhancer, DomainTreeException> initEntityCentreManager(final Class<?> menuItemType, final String name) {
        synchronized (this) {
            logger.info("Initialising entity-centre instance for type [" + menuItemType.getSimpleName() + "] with title [" + title(menuItemType, name) + "] for current user ["
                    + currentUser() + "]...");
            validateMenuItemType(menuItemType);
            final Class<?> root = validateMenuItemTypeRootType(menuItemType);
            
            final String title = title(menuItemType, name);
            final String menuItemTypeName = menuItemType.getName();
            
            final EntityResultQueryModel<EntityCentreConfig> model = modelForCurrentUser(menuItemTypeName, title);
            final int count = coEntityCentreConfig.count(model);
            final Either<ICentreDomainTreeManagerAndEnhancer, DomainTreeException> centreOrEx;
            if (count == 1) { // the persistence layer contains a entity-centre, so it should be retrieved and deserialised
                centreOrEx = left(retrieveAndInit(menuItemType, name, root, model));
            } else if (count < 1) { // there is no entity-centre
                if (name == null) { // principle entity-centre
                    // Principle entity-centre should be initialised and then saved. This can be done naturally by base user.
                    // But if base user haven't done that yet, it will be done by non-base user automatically.
                    final boolean owning = currentUser().isBase();
                    centreOrEx = left(createDefaultCentre(root, menuItemType));
                } else {
                    centreOrEx = right(new DomainTreeException("Unable to initialise a non-existent entity-centre instance for type [" + menuItemType.getSimpleName() + "] with title [" + title + "] for current user ["
                            + currentUser() + "]."));
                }
            } else if (count == 2) {
                final EntityResultQueryModel<EntityCentreConfig> model1 = modelForCurrentUser(menuItemTypeName, title);
                final int count1 = coEntityCentreConfig.count(model1);
                if (count1 == 1) { // for current user => 1 entity-centre, for base => another one with same title
                    // initialise an instance for current user (base configuration will be ignored)
                    centreOrEx = left(retrieveAndInit(menuItemType, name, root, model1));
                } else {
                    centreOrEx = right(new DomainTreeException("There are more than one entity-centre instance for type [" + menuItemType.getSimpleName() + "] with title [" + title + "] for current user ["
                            + currentUser() + "]."));
                }
            } else {
                centreOrEx = right(new DomainTreeException("There are more than one entity-centre instance for type [" + menuItemType.getSimpleName() + "] with title [" + title + "] for current user [" + currentUser()
                        + "]."));
            }
            logger.info("Initialised_ entity-centre instance for type [" + menuItemType.getSimpleName() + "] with title [" + title(menuItemType, name) + "] for current user ["
                    + currentUser() + "]...done");
            return centreOrEx;
        }
    }
    
    /**
     * Creates a model to retrieve {@link EntityCentreConfig} instances for the current user and its base user with a <code>title</code> and <code>menuItemTypeName</code>
     * specified.
     *
     * @param menuItemTypeName
     * @param title
     * @return
     */
    private EntityResultQueryModel<EntityCentreConfig> modelForCurrentUser(final String menuItemTypeName, final String title) {
        final EntityResultQueryModel<EntityCentreConfig> model1 =
                /*    */select(EntityCentreConfig.class).where().//
                /*    */prop("owner").eq().val(currentUser()).and().// look for entity-centres for only current user
                /*    */prop("title").eq().val(title).and().//
                /*    */prop("menuItem.key").eq().val(menuItemTypeName).model();
        return model1;
    }
    
    /**
     * Creates a model to retrieve {@link EntityCentreConfig} instances for the current user and its base user with <code>titles</code> and <code>menuItemTypeName</code>
     * specified.
     *
     * @param menuItemTypeName
     * @param titles
     * @return
     */
    private EntityResultQueryModel<EntityCentreConfig> multiModelForCurrentUser(final String menuItemTypeName, final String ... titles) {
        final EntityResultQueryModel<EntityCentreConfig> model1 =
                /*    */select(EntityCentreConfig.class).where().//
                /*    */prop("owner").eq().val(currentUser()).and().// look for entity-centres for only current user
                /*    */prop("title").in().values(titles).and().//
                /*    */prop("menuItem.key").eq().val(menuItemTypeName).model();
        return model1;
    }
    
    /**
     * Logs and throws an {@link DomainTreeException} error with specified message.
     *
     * @param message
     */
    private static void error(final String message) {
        logger.error(message);
        throw new DomainTreeException(message);
    }

    private static void errorf(final String message, final Object... args) {
        error(format(message, args));
    }

    /**
     * Retrieves a instance of manager.
     *
     * @param menuItemType
     * @param name
     * @param model
     */
    private ICentreDomainTreeManagerAndEnhancer retrieveAndInit(final Class<?> menuItemType, final String name, final Class<?> root, final EntityResultQueryModel<EntityCentreConfig> model) {
        final EntityCentreConfig ecc = coEntityCentreConfig.getEntity(from(model).model());
        try {
            final CentreDomainTreeManagerAndEnhancer cdtmae = getSerialiser().deserialise(ecc.getConfigBody(), CentreDomainTreeManagerAndEnhancer.class);
            // populate Id and Version to be able to determine staleness of the centre
            cdtmae.setSavedEntityId(ecc.getId());
            cdtmae.setSavedEntityVersion(ecc.getVersion());
            
            return initCentreManagerCrossReferences(cdtmae);
        } catch (final Exception e) {
            logger.error("============================================ CENTRE DESERIALISATION HAS FAILED ============================================");
            logger.error("Unable to deserialise a entity-centre instance for type [" + menuItemType.getSimpleName() + "] with title [" + title(menuItemType, name)
            + "] for current user [" + currentUser() + "]. The exception is the following: ", e);

            logger.error("Started creation of default entity-centre configuration for type [" + menuItemType.getSimpleName() + "] with title [" + title(menuItemType, name)
            + "] for current user [" + currentUser() + "].");

            final ICentreDomainTreeManagerAndEnhancer newCentreManager = createDefaultCentre(root, menuItemType);

            logger.error("Started saving of default entity-centre configuration for type [" + menuItemType.getSimpleName() + "] with title [" + title(menuItemType, name)
            + "] for current user [" + currentUser() + "].");

            ecc.setConfigBody(getSerialiser().serialise(newCentreManager));
            saveCentre(newCentreManager, ecc);

            logger.error("Ended creation and saving of default entity-centre configuration for type [" + menuItemType.getSimpleName() + "] with title [" + title(menuItemType, name)
            + "] for current user [" + currentUser() + "]. For now it can be used.");
            logger.error("============================================ CENTRE DESERIALISATION HAS FAILED [END] ============================================");
            return newCentreManager;
        }
    }
    
    protected ICentreDomainTreeManagerAndEnhancer createDefaultCentre(final Class<?> root, final Class<?> menuItemType) {
        return createEmptyCentre(root, getSerialiser());
    }
    
    public static ICentreDomainTreeManagerAndEnhancer createEmptyCentre(final Class<?> root, final ISerialiser serialiser) {
        // TODO next line of code must take in to account that the menu item is for association centre.
        final CentreDomainTreeManagerAndEnhancer c = new CentreDomainTreeManagerAndEnhancer(serialiser, new HashSet<Class<?>>() {
            {
                add(root);
            }
        });
        // initialise checkedProperties tree to make it more predictable in getting meta-info from "checkedProperties"
        c.getFirstTick().checkedProperties(root);
        c.getSecondTick().checkedProperties(root);
        
        return c;
    }
    
    /**
     * Makes a necessary references on "global stuff" (and perhaps other stuff) inside <code>mgr</code> instance.
     *
     * @param mgr
     * @return
     */
    public ICentreDomainTreeManagerAndEnhancer initCentreManagerCrossReferences(final ICentreDomainTreeManagerAndEnhancer mgr) {
        // TODO consider this if something does not work; final LocatorManager locatorManager = (((AddToCriteriaTickManagerAndEnhancer) mgr.getFirstTick()).base()).locatorManager();
        // TODO consider this if something does not work; final IGlobalDomainTreeRepresentation globalRepresentation = this.getGlobalRepresentation();
        // TODO consider this if something does not work; initLocatorManagerCrossReferences(locatorManager, globalRepresentation);
        return mgr;
    }
    
    /**
     * A copy method for entity centre that copies also "transient" stuff like currentAnalyses and freezedAnalyses. It has been done to take care about copying entity centre with
     * some changed / freezed analyses (all that changes will be promoted to copies).
     *
     * @param centre
     * @return
     */
    public ICentreDomainTreeManagerAndEnhancer copyCentre(final ICentreDomainTreeManagerAndEnhancer centre) {
        logger.debug(String.format("\t\t\tCopying centre..."));
        final DateTime start = new DateTime();
        // final TgKryo kryo = (TgKryo) getSerialiser();
        // TODO kryo.register(CentreDomainTreeManager.class, new CentreDomainTreeManagerSerialiserWithTransientAnalyses(kryo));
        final ICentreDomainTreeManagerAndEnhancer copy = initCentreManagerCrossReferences(EntityUtils.deepCopy(centre, getSerialiser()));
        // TODO kryo.register(CentreDomainTreeManager.class);
        
        // Performs copying of all defined custom annotations on generated types to provide the copy with the same annotations as original centre have.
        for (final Class<?> root: centre.getRepresentation().rootTypes()) {
            final Class<?> managedType = centre.getEnhancer().getManagedType(root);
            if (DynamicEntityClassLoader.isGenerated(managedType)) {
                final Annotation[] annotationsToCopy = managedType.getAnnotations();
                copy.getEnhancer().adjustManagedTypeAnnotations(root, annotationsToCopy);
            }
        }
        
        final DateTime end = new DateTime();
        final Period pd = new Period(start, end);
        logger.debug(String.format("\t\t\tCopying centre... done in [%s].", pd.getSeconds() + " s " + pd.getMillis() + " ms"));
        return copy;
    }

    @Override
    public IGlobalDomainTreeManager saveEntityCentreManager(final Class<?> menuItemType, final String name, final ICentreDomainTreeManagerAndEnhancer centre, final String newDesc) {
        validateMenuItemType(menuItemType);
        validateMenuItemTypeRootType(menuItemType);
        final ICentreDomainTreeManagerAndEnhancer currentMgr = centre;
        
        // save an instance of EntityCentreConfig with overridden body, which should exist in DB
        final String title = title(menuItemType, name);
        
        final EntityResultQueryModel<EntityCentreConfig> model = modelForCurrentUser(menuItemType.getName(), title);
        final int count = coEntityCentreConfig.count(model);
        if (count == 1) { // for current user => 1 entity-centre
            final EntityCentreConfig ecc = coEntityCentreConfig.getEntity(from(model).model());
            ecc.setConfigBody(getSerialiser().serialise(currentMgr));
            if (newDesc != null) {
                ecc.setDesc(newDesc);
            }
            saveCentre(currentMgr, ecc);
        } else if (count < 1) { // there is no saved entity-centre
            error("Unable to save non-existent entity-centre instance for type [" + menuItemType.getSimpleName() + "] with title [" + title + "] for current user [" + currentUser() + "].");
        } else { // > 1
            error("There are more than one entity-centre instance for type [" + menuItemType.getSimpleName() + "] with title [" + title + "] for current user [" + currentUser() + "].");
        }
        return this;
    }
    
    @Override
    public ICentreDomainTreeManagerAndEnhancer saveAsEntityCentreManager(final Class<?> menuItemType, final ICentreDomainTreeManagerAndEnhancer centre, final String newName, final String newDesc) {
        synchronized (this) {
            validateMenuItemType(menuItemType);
            validateMenuItemTypeRootType(menuItemType);
            
            final ICentreDomainTreeManagerAndEnhancer originationMgr = centre;
            // create a copy of current instance of entity centre
            final ICentreDomainTreeManagerAndEnhancer copyMgr = copyCentre(originationMgr);
            
            // save an instance of EntityCentreConfig with overridden body, which should exist in DB
            final String menuItemTypeName = menuItemType.getName();
            final String newTitle = title(menuItemType, newName);
            
            final EntityResultQueryModel<EntityCentreConfig> model = modelForCurrentUser(menuItemTypeName, newTitle);
            
            final int count = coEntityCentreConfig.count(model);
            if (count == 0) { // for current user [or its base] => there are no entity-centres, so persist a copy with a new title
                final User user = currentUser();
                final MainMenuItem menuItemToUse;
                final MainMenuItem menuItem = mainMenuItemController.findByKey(menuItemTypeName);
                if (menuItem != null) {
                    menuItemToUse = menuItem;
                } else {
                    menuItemToUse = mainMenuItemController.save(factory.newByKey(MainMenuItem.class, menuItemTypeName));
                }
                final EntityCentreConfig ecc = factory.newByKey(EntityCentreConfig.class, user, newTitle, menuItemToUse);
                ecc.setDesc(newDesc);
                ecc.setConfigBody(getSerialiser().serialise(copyMgr));
                saveCentre(copyMgr, ecc);
            } else { // > 1
                errorf("There are at least one entity-centre instance for type [%s] with title [%s] for current user [%s%s].", menuItemType.getSimpleName(), newTitle, currentUser(), "");
            }
            return copyMgr;
        }
    }

    private void saveCentre(final ICentreDomainTreeManagerAndEnhancer copyMgr, final EntityCentreConfig ecc) {
        final EntityCentreConfig newECC = coEntityCentreConfig.save(ecc);
        // populate Id and Version to be able to determine staleness of the centre
        ((CentreDomainTreeManagerAndEnhancer) copyMgr).setSavedEntityId(newECC.getId());
        ((CentreDomainTreeManagerAndEnhancer) copyMgr).setSavedEntityVersion(newECC.getVersion());
    }
    
    /**
     * Removes centre configurations from persistent storage.
     * 
     * @param menuItemType
     * @param names
     */
    public void removeCentres(final Class<?> menuItemType, final String ... names) {
        coEntityCentreConfig.delete(multiModelForCurrentUser(menuItemType.getName(), stream(names).map(name -> title(menuItemType, name)).toArray(String[]::new)));
    }
    
    @Override
    public Optional<IGlobalDomainTreeManager> basedOnManager() {
        throw new DomainTreeException("Non-applicable in this implementation. Need to be overridden in descendants.");
    }
    
    @Override
    public IUser coUser() {
        return coUser;
    }
    
    @Override
    public EntityCentreConfig findConfig(final Class<?> menuItemType, final String name) {
        return coEntityCentreConfig.getEntity(
            from(modelForCurrentUser(menuItemType.getName(), title(menuItemType, name))).with(fetchWithKeyAndDesc(EntityCentreConfig.class, true).with("preferred").fetchModel()).model()
        );
    }
    
    @Override
    public void saveConfig(final EntityCentreConfig config) {
        coEntityCentreConfig.quickSave(config);
    }
    
}