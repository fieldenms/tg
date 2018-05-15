package ua.com.fielden.platform.domaintree.impl;

import static java.lang.String.format;
import static java.util.Arrays.stream;
import static ua.com.fielden.platform.domaintree.ILocatorManager.Phase.USAGE_PHASE;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.fetchOnly;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.from;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.select;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;

import org.apache.log4j.Logger;
import org.joda.time.DateTime;
import org.joda.time.Period;

import com.google.inject.Inject;

import ua.com.fielden.platform.domaintree.IGlobalDomainTreeManager;
import ua.com.fielden.platform.domaintree.IGlobalDomainTreeRepresentation;
import ua.com.fielden.platform.domaintree.centre.ICentreDomainTreeManager.ICentreDomainTreeManagerAndEnhancer;
import ua.com.fielden.platform.domaintree.centre.ICentreDomainTreeRepresentation.IAddToCriteriaTickRepresentation;
import ua.com.fielden.platform.domaintree.centre.impl.CentreDomainTreeManagerAndEnhancer;
import ua.com.fielden.platform.domaintree.centre.impl.CentreDomainTreeManagerAndEnhancer.AddToCriteriaTickManagerAndEnhancer;
import ua.com.fielden.platform.domaintree.exceptions.DomainTreeException;
import ua.com.fielden.platform.domaintree.master.IMasterDomainTreeManager;
import ua.com.fielden.platform.domaintree.master.impl.MasterDomainTreeManager;
import ua.com.fielden.platform.entity.AbstractBatchAction;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.annotation.EntityType;
import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.entity.query.model.EntityResultQueryModel;
import ua.com.fielden.platform.reflection.AnnotationReflector;
import ua.com.fielden.platform.reflection.Finder;
import ua.com.fielden.platform.reflection.Reflector;
import ua.com.fielden.platform.reflection.asm.impl.DynamicEntityClassLoader;
import ua.com.fielden.platform.security.user.IUser;
import ua.com.fielden.platform.security.user.IUserProvider;
import ua.com.fielden.platform.security.user.User;
import ua.com.fielden.platform.serialisation.api.ISerialiser;
import ua.com.fielden.platform.serialisation.api.ISerialiser0;
import ua.com.fielden.platform.ui.config.EntityCentreAnalysisConfig;
import ua.com.fielden.platform.ui.config.EntityCentreConfig;
import ua.com.fielden.platform.ui.config.EntityMasterConfig;
import ua.com.fielden.platform.ui.config.IEntityCentreAnalysisConfig;
import ua.com.fielden.platform.ui.config.MainMenuItem;
import ua.com.fielden.platform.ui.config.api.IEntityCentreConfig;
import ua.com.fielden.platform.ui.config.api.IEntityLocatorConfig;
import ua.com.fielden.platform.ui.config.api.IEntityMasterConfig;
import ua.com.fielden.platform.ui.config.api.IMainMenuItem;
import ua.com.fielden.platform.utils.EntityUtils;
import ua.com.fielden.platform.utils.Pair;

/**
 * The global domain tree manager implementation.
 *
 * @author TG Team
 *
 */
public class GlobalDomainTreeManager extends AbstractDomainTree implements IGlobalDomainTreeManager {
    private final static Logger logger = Logger.getLogger(GlobalDomainTreeManager.class);
    private final static String DEFAULT_CONFIG_DESC = "Default configuration";
    public final static String DEFAULT_CONFIG_TITLE = "[Default]";
    private final EntityFactory factory;
    private final IUserProvider userProvider;
    private final IGlobalDomainTreeRepresentation gdtr;
    private final IMainMenuItem mainMenuItemController;
    private final IEntityCentreConfig entityCentreConfigController;
    private final IEntityCentreAnalysisConfig entityCentreAnalysisConfigController;
    private final IEntityMasterConfig entityMasterConfigController;
    private final DomainTreeVersionMaintainer versionMaintainer;

    private EnhancementPropertiesMap<ICentreDomainTreeManagerAndEnhancer> persistentCentres;
    private final transient EnhancementPropertiesMap<ICentreDomainTreeManagerAndEnhancer> currentCentres;
    private final transient EnhancementPropertiesMap<ICentreDomainTreeManagerAndEnhancer> freezedCentres;
    private final transient EnhancementPropertiesMap<Boolean> centresOwning;

    private final EnhancementRootsMap<IMasterDomainTreeManager> persistentMasters;
    private final transient EnhancementRootsMap<IMasterDomainTreeManager> currentMasters;

    private Map<Class<?>, Map<String, List<String>>> initialCacheOfNonPrincipleItems = null;
    private final IUser coUser;

    @Inject
    public GlobalDomainTreeManager(final ISerialiser serialiser, final ISerialiser0 serialiser0, final EntityFactory factory, final IUserProvider userProvider, final IMainMenuItem mainMenuItemController, final IEntityCentreConfig entityCentreConfigController, final IEntityCentreAnalysisConfig entityCentreAnalysisConfigController, final IEntityMasterConfig entityMasterConfigController, final IEntityLocatorConfig entityLocatorConfigController, final IUser coUser) {
        super(serialiser);
        this.factory = factory;
        this.userProvider = userProvider;
        this.mainMenuItemController = mainMenuItemController;
        this.entityCentreConfigController = entityCentreConfigController;
        this.entityCentreAnalysisConfigController = entityCentreAnalysisConfigController;
        this.entityMasterConfigController = entityMasterConfigController;
        final GlobalDomainTreeRepresentation gdtr0 = new GlobalDomainTreeRepresentation(serialiser, serialiser0, factory, userProvider, entityCentreConfigController, entityMasterConfigController, entityLocatorConfigController);
        this.versionMaintainer = gdtr0.versionMaintainer();
        this.gdtr = gdtr0;
        // lazy stuff
        this.persistentCentres = createPropertiesMap();
        this.currentCentres = createPropertiesMap();
        this.freezedCentres = createPropertiesMap();
        this.centresOwning = createPropertiesMap();

        this.persistentMasters = createRootsMap();
        this.currentMasters = createRootsMap();
        this.coUser = coUser;
    }

    @Override
    public Map<String, List<String>> initialCacheOfNonPrincipleItems(final Class<?> menuItemType) {
        if (initialCacheOfNonPrincipleItems == null) {
            initialCacheOfNonPrincipleItems = loadEntityCentreSkeleton();
        }

        if (!initialCacheOfNonPrincipleItems.containsKey(menuItemType)) {
            return new LinkedHashMap<String, List<String>>();
        }
        return initialCacheOfNonPrincipleItems.get(menuItemType);
    }

    private Map<Class<?>, Map<String, List<String>>> loadEntityCentreSkeleton() {
        logger.info("Loading entity centre skeleton...");
        // load all non-principle entity centres for this user
        final List<EntityCentreConfig> centresFromTheCloud = entityCentreConfigController.getAllEntities(from(nonPrincipleECCmodel()).with(fetchOnly(EntityCentreConfig.class).with("title").with("menuItem", fetchOnly(MainMenuItem.class).with("key"))).model());

        final Map<Class<?>, Map<String, List<String>>> map = new LinkedHashMap<>();
        for (final EntityCentreConfig ecc : centresFromTheCloud) {
            final Class<?> miType = getMiType(ecc.getMenuItem().getKey());
            if (!map.containsKey(miType)) {
                map.put(miType, new TreeMap<String, List<String>>());
            }
            map.get(miType).put(ecc.getTitle(), new ArrayList<String>());
        }

        // load all non-principle entity centre analyses for this user
        final List<EntityCentreAnalysisConfig> centreAnalysesFromTheCloud = entityCentreAnalysisConfigController.getAllEntities(from(analysesForNonPrincipleECCmodel()).with(fetchOnly(EntityCentreAnalysisConfig.class).with("title").with("entityCentreConfig", fetchOnly(EntityCentreConfig.class).with("title").with("menuItem", fetchOnly(MainMenuItem.class).with("key")))).model());

        for (final EntityCentreAnalysisConfig ecac : centreAnalysesFromTheCloud) {
            final Class<?> miType = getMiType(ecac.getEntityCentreConfig().getMenuItem().getKey());
            map.get(miType).get(ecac.getEntityCentreConfig().getTitle()).add(ecac.getTitle());
        }
        logger.info("Loading entity centre skeleton...done");
        return map;
    }

    private Class<?> getMiType(final String miTypeName) {
        try {
            final Class<?> mmiType = ClassLoader.getSystemClassLoader().loadClass(miTypeName);
            try {
                validateMenuItemType(mmiType);
                validateMenuItemTypeRootType(mmiType);
                return mmiType;
            } catch (final DomainTreeException e) {
                return null;
            }
        } catch (final ClassNotFoundException ex) {
            // there are situation where menu items could have been renamed or replaced with some other type
            // in such cases can only "pretend" that all is good and return null...
            // however, the situation needs to be logged in case further problems arise and some analysis would be required
            logger.error(format("Unsuccessful attempt to load menu item type [%s].", miTypeName), ex);
            //throw new IllegalStateException(e);
            return null;
        }
    }

    @Override
    public List<Class<?>> entityCentreMenuItemTypes() {
        final List<MainMenuItem> mmItems = this.mainMenuItemController.getAllEntities(from(select(MainMenuItem.class).model()).model());
        final List<Class<?>> res = new ArrayList<Class<?>>();
        for (final MainMenuItem mmi : mmItems) {
            final Class<?> type = getMiType(mmi.getKey());
            if (type != null) {
                res.add(type);
            }
        }
        return res;
    }

    @Override
    public IUserProvider getUserProvider() {
        return userProvider;
    }

    @Override
    public IGlobalDomainTreeRepresentation getGlobalRepresentation() {
        return gdtr;
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

    @Override
    public ICentreDomainTreeManagerAndEnhancer getEntityCentreManager(final Class<?> menuItemType, final String name) {
        validateMenuItemType(menuItemType);
        validateMenuItemTypeRootType(menuItemType);
        return currentCentres.get(key(menuItemType, name));
    }

    private User currentUser() {
        return userProvider.getUser();
    }

    private User baseOfTheCurrentUser() {
        return baseOfTheCurrentUser(currentUser());
    }

    private static User baseOfTheCurrentUser(final User currentUser) {
        return currentUser.isBase() ? currentUser : currentUser.getBasedOnUser();
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

    public static CentreManagerConfigurator createEntityCentreConfigurator(final Class<?> menuItemType, final Class<?> root) {
        try {
            final Method method = Reflector.getMethod(menuItemType, "createCentreConfigurator");
            final boolean isAccessible = method.isAccessible();
            method.setAccessible(true);
            final Object obj = method.invoke(null);
            method.setAccessible(isAccessible);
            return (CentreManagerConfigurator) obj;
        } catch (final NoSuchMethodException noSuchMethod) {
            logger.warn("No centre configurator has been specified for menu item [" + menuItemType
                    + "]. Please add method 'private static CentreManagerConfigurator createCentreConfigurator()' " + //
                    "into menu item (e.g. into MiDriver class) in case when custom defalt configuration is needed.");
            return new CentreManagerConfigurator((Class<? extends AbstractEntity<?>>) root); // legal situation -- no centre configurator has been specified
        } catch (final Throwable t) {
            t.printStackTrace();
            logger.error(t.getMessage());
            throw new DomainTreeException(t.getMessage());
        }
    }

    @Override
    public IGlobalDomainTreeManager initEntityCentreManager(final Class<?> menuItemType, final String name) {
        synchronized (this) {
            logger.info("Initialising entity-centre instance for type [" + menuItemType.getSimpleName() + "] with title [" + title(menuItemType, name) + "] for current user ["
                    + currentUser() + "]...");
            validateMenuItemType(menuItemType);
            final Class<?> root = validateMenuItemTypeRootType(menuItemType);
            if (isFreezedEntityCentreManager(menuItemType, name)) {
                error("Unable to Init the 'freezed' entity-centre instance for type [" + menuItemType.getSimpleName() + "] with title [" + title(menuItemType, name)
                        + "] for current user [" + currentUser() + "].");
            }
            final CentreManagerConfigurator centreConfigurator = createEntityCentreConfigurator(menuItemType, root);

            final String title = title(menuItemType, name);
            final String menuItemTypeName = menuItemType.getName();
            
            // for the new generation of Web UI centres (where persistentCentres == null and avoidPersistentCentres switch is on)
            // the model for initialised configuration existence should not look on base configurations.
            final EntityResultQueryModel<EntityCentreConfig> model = persistentCentres == null ? modelForCurrentUser(menuItemTypeName, title) : modelForCurrentAndBaseUsers(menuItemTypeName, title);
            final int count = entityCentreConfigController.count(model);
            if (count == 1) { // the persistence layer contains a entity-centre, so it should be retrieved and deserialised
                retrieveAndInit(menuItemType, name, root, centreConfigurator, model);
            } else if (count < 1) { // there is no entity-centre
                if (name == null) { // principle entity-centre
                    // Principle entity-centre should be initialised and then saved. This can be done naturally by base user.
                    // But if base user haven't done that yet, it will be done by non-base user automatically.
                    final boolean owning = currentUser().isBase();
                    init(menuItemType, name, createDefaultCentre(centreConfigurator, root, menuItemType), owning);
                } else {
                    error("Unable to initialise a non-existent entity-centre instance for type [" + menuItemType.getSimpleName() + "] with title [" + title + "] for current user ["
                            + currentUser() + "].");
                }
            } else if (count == 2) {
                final EntityResultQueryModel<EntityCentreConfig> model1 = modelForCurrentUser(menuItemTypeName, title);
                final int count1 = entityCentreConfigController.count(model1);
                if (count1 == 1) { // for current user => 1 entity-centre, for base => another one with same title
                    // initialise an instance for current user (base configuration will be ignored)
                    retrieveAndInit(menuItemType, name, root, centreConfigurator, model1);
                } else {
                    error("There are more than one entity-centre instance for type [" + menuItemType.getSimpleName() + "] with title [" + title + "] for current user ["
                            + currentUser() + "].");
                }
            } else {
                error("There are more than one entity-centre instance for type [" + menuItemType.getSimpleName() + "] with title [" + title + "] for current user [" + currentUser()
                        + "].");
            }
            logger.info("Initialised_ entity-centre instance for type [" + menuItemType.getSimpleName() + "] with title [" + title(menuItemType, name) + "] for current user ["
                    + currentUser() + "]...done");
            return this;
        }
    }

    /**
     * Creates a model to retrieve {@link EntityCentreConfig} instances for the current user with a <code>title</code> and <code>menuItemTypeName</code> specified.
     *
     * @param menuItemTypeName
     * @param title
     * @return
     */
    protected static EntityResultQueryModel<EntityCentreConfig> modelForCurrentAndBaseUsers(final String menuItemTypeName, final String title, final User currentUser) {
        final EntityResultQueryModel<EntityCentreConfig> model =
                /*    */select(EntityCentreConfig.class).where().//
                /*    */begin().prop("owner").eq().val(currentUser).or().prop("owner").eq().val(baseOfTheCurrentUser(currentUser)).end().and().// look for entity-centres for both users (current and its base)
                /*    */prop("title").eq().val(title).and().//
                /*    */prop("menuItem.key").eq().val(menuItemTypeName).model();
        return model;
    }

    /**
     * Creates a model to retrieve {@link EntityCentreConfig} instances for the current user with a <code>title</code> and <code>menuItemTypeName</code> specified.
     *
     * @param menuItemTypeName
     * @param title
     * @return
     */
    protected final EntityResultQueryModel<EntityCentreConfig> modelForCurrentAndBaseUsers(final String menuItemTypeName, final String title) {
        return modelForCurrentAndBaseUsers(menuItemTypeName, title, currentUser());
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
     * Creates a model to retrieve {@link EntityCentreConfig} instances for the current user and its base user with a <code>title</code> and <code>menuItemTypeName</code>
     * specified.
     *
     * @param menuItemTypeName
     * @param title
     * @return
     */
    private EntityResultQueryModel<EntityCentreConfig> modelForCurrentAndBaseUsersNonPrincipal(final String menuItemTypeName) {
        final EntityResultQueryModel<EntityCentreConfig> model =
                /*    */select(EntityCentreConfig.class).where().//
                /*    */begin().prop("owner").eq().val(currentUser()).or().prop("owner").eq().val(baseOfTheCurrentUser()).end().and().// look for entity-centres for both users (current and its base)
                /*    */prop("menuItem.key").eq().val(menuItemTypeName).and().
                        /*    */prop("principal").eq().val(false).model();
        return model;
    }

    /**
     * Creates a model to retrieve all {@link EntityCentreConfig} instances for the current user and its base user.
     *
     * @param menuItemTypeName
     * @param title
     * @return
     */
    private EntityResultQueryModel<EntityCentreConfig> nonPrincipleECCmodel() {
        final EntityResultQueryModel<EntityCentreConfig> model =
                /*    */select(EntityCentreConfig.class).where().//
                /*    */begin().prop("owner").eq().val(currentUser()).or().prop("owner").eq().val(baseOfTheCurrentUser()).end().and().// look for entity-centres for both users (current and its base)
                /*    */prop("principal").eq().val(false).model();
        return model;
    }

    /**
     * Creates a model to retrieve all {@link EntityCentreAnalysisConfig} instances for the current user and its base user.
     *
     * @param menuItemTypeName
     * @param title
     * @return
     */
    private EntityResultQueryModel<EntityCentreAnalysisConfig> analysesForNonPrincipleECCmodel() {
        final EntityResultQueryModel<EntityCentreAnalysisConfig> model =
                /*    */select(EntityCentreAnalysisConfig.class).where().//
                /*    */prop("entityCentreConfig").in().model(nonPrincipleECCmodel()).model();
        return model;
    }

    /**
     * Creates a model to retrieve all {@link EntityCentreAnalysisConfig} instances for the current user and its base user.
     *
     * @param menuItemTypeName
     * @param title
     * @return
     */
    private EntityResultQueryModel<EntityCentreAnalysisConfig> analysesForConcreteECCmodel(final EntityCentreConfig ecc) {
        final EntityResultQueryModel<EntityCentreAnalysisConfig> model =
                /*    */select(EntityCentreAnalysisConfig.class).where().//
                /*    */prop("entityCentreConfig").eq().val(ecc).model();
        return model;
    }

    /**
     * Creates a model to retrieve {@link EntityMasterConfig} instances for the current user with a <code>rootName</code> specified.
     *
     * @param rootName
     * @return
     */
    private EntityResultQueryModel<EntityMasterConfig> masterModelForCurrentUser(final String rootName) {
        final EntityResultQueryModel<EntityMasterConfig> model =
                /*    */select(EntityMasterConfig.class).where().//
                /*    */prop("owner").eq().val(currentUser()).and().// look for entity-masters for only current user
                /*    */prop("masterType").eq().val(rootName).model();
        return model;
    }

    /**
     * Creates a model to retrieve {@link EntityMasterConfig} instances for the base of the current user with a <code>rootName</code> specified.
     *
     * @param rootName
     * @return
     */
    private EntityResultQueryModel<EntityMasterConfig> masterModelForBaseUser(final String rootName) {
        final EntityResultQueryModel<EntityMasterConfig> model =
                /*    */select(EntityMasterConfig.class).where().//
                /*    */prop("owner").eq().val(baseOfTheCurrentUser()).and().// look for entity-masters for only base of the current user
                /*    */prop("masterType").eq().val(rootName).model();
        return model;
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

    /**
     * Retrieves and initialises a instance of manager.
     *
     * @param menuItemType
     * @param name
     * @param model
     */
    private void retrieveAndInit(final Class<?> menuItemType, final String name, final Class<?> root, final CentreManagerConfigurator centreConfigurator, final EntityResultQueryModel<EntityCentreConfig> model) {
        final EntityCentreConfig ecc = entityCentreConfigController.getEntity(from(model).model());
        final boolean owning = ecc.getOwner().equals(currentUser());
        try {
            init(menuItemType, name, versionMaintainer.maintainCentreVersion(ecc), owning);
            return;
        } catch (final Exception e) {
            logger.error("============================================ CENTRE DESERIALISATION HAS FAILED ============================================");
            logger.error("Unable to deserialise a entity-centre instance for type [" + menuItemType.getSimpleName() + "] with title [" + title(menuItemType, name)
            + "] for current user [" + currentUser() + "]. The exception is the following: ", e);

            logger.error("Started creation of default entity-centre configuration for type [" + menuItemType.getSimpleName() + "] with title [" + title(menuItemType, name)
            + "] for current user [" + currentUser() + "].");

            init(menuItemType, name, createDefaultCentre(centreConfigurator, root, menuItemType), owning);

            logger.error("Started saving of default entity-centre configuration for type [" + menuItemType.getSimpleName() + "] with title [" + title(menuItemType, name)
            + "] for current user [" + currentUser() + "].");

            ecc.setConfigBody(getSerialiser().serialise(getEntityCentreManager(menuItemType, name)));
            saveCentre(getEntityCentreManager(menuItemType, name), ecc);

            logger.error("Ended creation and saving of default entity-centre configuration for type [" + menuItemType.getSimpleName() + "] with title [" + title(menuItemType, name)
            + "] for current user [" + currentUser() + "]. For now it can be used.");
            logger.error("============================================ CENTRE DESERIALISATION HAS FAILED [END] ============================================");
        }
    }

    /**
     * Checks whether the centre is stale on this server node, i.e. its version is lower than the one in the database. It is needed to update the centre instance in case where
     * the centre is stale to be able to use relevant configuration for a) centre criteria changing b) previouslyRun centre context restoration etc.
     * <p>
     * Please note that the version / ID are not copied during centre copying process, that is why currentCentres' instance should be used instead of persistentCentre instance.
     *
     * @param centre
     * @return
     */
    public boolean isStale(final CentreDomainTreeManagerAndEnhancer centre) {
        final Long id = centre.getSavedEntityId();
        final Long version = centre.getSavedEntityVersion();

        if (id == null) {
            logger.error("ID should exist.");
            throw new DomainTreeException("ID should exist.");
        }
        if (version == null) {
            logger.error("Version should exist.");
            throw new DomainTreeException("Version should exist.");
        }
        // logger.debug(String.format("Checking the staleness of the centre with ID [%s] and version [%s]: started...", id, version));
        final boolean stale = entityCentreConfigController.isStale(id, version);
        // logger.debug(String.format("Checking the staleness of the centre with ID [%s] and version [%s]: %s.", id, version, stale));
        return stale;
    }

    protected ICentreDomainTreeManagerAndEnhancer createDefaultCentre(final CentreManagerConfigurator centreConfigurator, final Class<?> root, final Class<?> menuItemType) {
        return centreConfigurator.configCentre(createEmptyCentre(root, getSerialiser()));
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
     * Retrieves and initialises a instance of master manager.
     *
     * @param root
     * @param name
     * @param model
     */
    private void retrieveAndInitMaster(final Class<?> root, final EntityResultQueryModel<EntityMasterConfig> model) {
        final EntityMasterConfig emc = entityMasterConfigController.getEntity(from(model).model());
        try {
            initMaster(root, versionMaintainer.maintainMasterVersion(emc));
            return;
        } catch (final Exception e) {
            e.printStackTrace();
            final String message = "Unable to deserialise a entity-master instance for type [" + root.getSimpleName() + "] for current user [" + currentUser() + "].";
            error(message);
        }
    }

    /**
     * Makes a necessary references on "global stuff" (and perhaps other stuff) inside <code>mgr</code> instance.
     *
     * @param mgr
     * @return
     */
    public ICentreDomainTreeManagerAndEnhancer initCentreManagerCrossReferences(final ICentreDomainTreeManagerAndEnhancer mgr) {
        final LocatorManager locatorManager = (((AddToCriteriaTickManagerAndEnhancer) mgr.getFirstTick()).base()).locatorManager();
        final IGlobalDomainTreeRepresentation globalRepresentation = this.getGlobalRepresentation();
        initLocatorManagerCrossReferences(locatorManager, globalRepresentation);
        return mgr;
    }

    /**
     * Makes a necessary references on "global stuff" (and perhaps other stuff) inside <code>mgr</code> instance.
     *
     * @param mgr
     * @return
     */
    public IMasterDomainTreeManager initMasterManagerCrossReferences(final IMasterDomainTreeManager mgr) {
        final LocatorManager locatorManager = ((MasterDomainTreeManager) mgr).locatorManager();
        final IGlobalDomainTreeRepresentation globalRepresentation = this.getGlobalRepresentation();
        initLocatorManagerCrossReferences(locatorManager, globalRepresentation);
        return mgr;
    }

    public static void initLocatorManagerCrossReferences(final LocatorManager0 locatorManager, final IGlobalDomainTreeRepresentation globalRepresentation) {
        // initialise the references on this instance in its children
        try {
            final Field dtrField = Finder.findFieldByName(LocatorManager0.class, "globalRepresentation");
            final boolean isAccessible = dtrField.isAccessible();
            dtrField.setAccessible(true);
            dtrField.set(locatorManager, globalRepresentation);
            dtrField.setAccessible(isAccessible);
        } catch (final Exception e) {
            e.printStackTrace();
            error(e.getMessage());
        }
    }

    public static void initLocatorManagerCrossReferences(final LocatorManager locatorManager, final IGlobalDomainTreeRepresentation globalRepresentation) {
        // initialise the references on this instance in its children
        try {
            final Field dtrField = Finder.findFieldByName(LocatorManager.class, "globalRepresentation");
            final boolean isAccessible = dtrField.isAccessible();
            dtrField.setAccessible(true);
            dtrField.set(locatorManager, globalRepresentation);
            dtrField.setAccessible(isAccessible);
        } catch (final Exception e) {
            e.printStackTrace();
            error(e.getMessage());
        }
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

    private IMasterDomainTreeManager copyMaster(final IMasterDomainTreeManager master) {
        return initMasterManagerCrossReferences(EntityUtils.deepCopy(master, getSerialiser()));
    }

    /**
     * Initiates an application instances with a new <code>mgr</code> instance.
     *
     * @param menuItemType
     * @param name
     * @param mgr
     * @param owning
     */
    public void init(final Class<?> menuItemType, final String name, final ICentreDomainTreeManagerAndEnhancer mgr, final boolean owning) {
        final ICentreDomainTreeManagerAndEnhancer fullyDefinedMgr = initCentreManagerCrossReferences(mgr);
        currentCentres.put(key(menuItemType, name), fullyDefinedMgr);
        if (persistentCentres != null) {
            persistentCentres.put(key(menuItemType, name), copyCentre(fullyDefinedMgr));
        }
        centresOwning.put(key(menuItemType, name), owning);
    }

    public void overrideCentre(final Class<?> menuItemType, final String name, final ICentreDomainTreeManagerAndEnhancer mgr) {
        if (mgr == null) {
            currentCentres.remove(key(menuItemType, name));
        } else {
            currentCentres.put(key(menuItemType, name), initCentreManagerCrossReferences(mgr));
        }
    }

    /**
     * Initiates an application instances with a new <code>mgr</code> instance.
     *
     * @param root
     * @param name
     * @param mgr
     * @param owning
     */
    private void initMaster(final Class<?> root, final IMasterDomainTreeManager mgr) {
        final IMasterDomainTreeManager fullyDefinedMgr = initMasterManagerCrossReferences(mgr);
        currentMasters.put(root, fullyDefinedMgr);
        persistentMasters.put(root, copyMaster(fullyDefinedMgr));
    }

    @Override
    public IGlobalDomainTreeManager discardEntityCentreManager(final Class<?> menuItemType, final String name) {
        if (persistentCentres != null) {
            validateMenuItemType(menuItemType);
            validateMenuItemTypeRootType(menuItemType);
            final ICentreDomainTreeManagerAndEnhancer persistentCentre = persistentCentres.get(key(menuItemType, name));
            notInitiliasedError(persistentCentre, menuItemType, name);
            currentCentres.put(key(menuItemType, name), copyCentre(persistentCentre));

            if (isFreezedEntityCentreManager(menuItemType, name)) {
                unfreeze(menuItemType, name);
            }
            return this;
        } else {
            throw new UnsupportedOperationException("avoidPersistentCentres switch is on.");
        }
    }

    @Override
    public IGlobalDomainTreeManager freezeEntityCentreManager(final Class<?> menuItemType, final String name) {
        if (persistentCentres != null) {
            validateMenuItemType(menuItemType);
            validateMenuItemTypeRootType(menuItemType);
            if (isFreezedEntityCentreManager(menuItemType, name)) {
                error("Unable to freeze the entity-centre instance more than once for type [" + menuItemType.getSimpleName() + "] with title [" + title(menuItemType, name)
                + "] for current user [" + currentUser() + "].");
            }
            notInitiliasedError(currentCentres.get(key(menuItemType, name)), menuItemType, name);
            notInitiliasedError(persistentCentres.get(key(menuItemType, name)), menuItemType, name);

            freezedCentres.put(key(menuItemType, name), persistentCentres.remove(key(menuItemType, name)));
            persistentCentres.put(key(menuItemType, name), copyCentre(currentCentres.get(key(menuItemType, name))));
            currentCentres.put(key(menuItemType, name), copyCentre(currentCentres.get(key(menuItemType, name)))); // this is necessary to dispose current manager with listeners and get equal "fresh" instance
            return this;
        } else {
            throw new UnsupportedOperationException("avoidPersistentCentres switch is on.");
        }
    }

    /**
     * Returns <code>true</code> if the centre instance is in 'freezed' state, <code>false</code> otherwise.
     *
     * @param menuItemType
     * @param name
     * @return
     */
    @Override
    public boolean isFreezedEntityCentreManager(final Class<?> menuItemType, final String name) {
        validateMenuItemType(menuItemType);
        validateMenuItemTypeRootType(menuItemType);

        return freezedCentres.get(key(menuItemType, name)) != null;
    }

    /**
     * Unfreezes the centre instance that is currently freezed.
     *
     * @param menuItemType
     * @param name
     */
    protected void unfreeze(final Class<?> menuItemType, final String name) {
        if (persistentCentres != null) {
            if (!isFreezedEntityCentreManager(menuItemType, name)) {
                error("Unable to unfreeze the entity-centre instance that is not 'freezed' for type [" + menuItemType.getSimpleName() + "] with title [" + title(menuItemType, name)
                + "] for current user [" + currentUser() + "].");
            }
            persistentCentres.put(key(menuItemType, name), freezedCentres.remove(key(menuItemType, name)));
        } else {
            throw new UnsupportedOperationException("avoidPersistentCentres switch is on.");
        }
    }

    /**
     * Throws an error when the instance is <code>null</code> (not initialised).
     *
     * @param mgr
     * @param menuItemType
     * @param name
     */
    private void notInitiliasedError(final ICentreDomainTreeManagerAndEnhancer mgr, final Class<?> menuItemType, final String name) {
        if (mgr == null) {
            error("Unable to perform this operation on the entity-centre instance, that wasn't initialised, for type [" + menuItemType.getSimpleName() + "] with title ["
                    + title(menuItemType, name) + "] for current user [" + currentUser() + "].");
        }
    }

    /**
     * Throws an error when the instance is <code>null</code> (not initialised).
     *
     * @param mgr
     * @param root
     * @param name
     */
    private void masterNotInitiliasedError(final IMasterDomainTreeManager mgr, final Class<?> root) {
        if (mgr == null) {
            error("Unable to perform this operation on the entity-master instance, that wasn't initialised, for type [" + root.getSimpleName() + "] for current user ["
                    + currentUser() + "].");
        }
    }

    @Override
    public IGlobalDomainTreeManager saveEntityCentreManager(final Class<?> menuItemType, final String name, final String newDesc) {
        validateMenuItemType(menuItemType);
        validateMenuItemTypeRootType(menuItemType);
        if (isFreezedEntityCentreManager(menuItemType, name)) {
            unfreeze(menuItemType, name);

            currentCentres.put(key(menuItemType, name), copyCentre(currentCentres.get(key(menuItemType, name)))); // this is necessary to dispose current manager with listeners and get equal "fresh" instance
        } else {
            final ICentreDomainTreeManagerAndEnhancer currentMgr = getEntityCentreManager(menuItemType, name);
            validateBeforeSaving(currentMgr, menuItemType, name);

            // save an instance of EntityCentreConfig with overridden body, which should exist in DB
            final String title = title(menuItemType, name);

            final EntityResultQueryModel<EntityCentreConfig> model = modelForCurrentUser(menuItemType.getName(), title);
            final int count = entityCentreConfigController.count(model);
            if (count == 1) { // for current user => 1 entity-centre
                final EntityCentreConfig ecc = entityCentreConfigController.getEntity(from(model).model());
                ecc.setConfigBody(getSerialiser().serialise(currentMgr));
                if (newDesc != null) {
                    ecc.setDesc(newDesc);
                }
                saveCentre(currentMgr, ecc);
                // TODO entityCentreAnalysisConfigController.save(entity)
                if (persistentCentres != null) {
                    persistentCentres.put(key(menuItemType, name), copyCentre(currentMgr));
                }
            } else if (count < 1) { // there is no saved entity-centre
                if (name == null) { // principle centre
                    if (!isEntityCentreManagerOwner(menuItemType, null)) {
                        error("Unable to save PRINCIPLE entity-centre instance for type [" + menuItemType.getSimpleName() + "] for current NON-base user [" + currentUser() + "].");
                    } else {
                        // In this case the centre can exist virtually, without its cloud counter-part
                        // But explicit save has been requested by base user => in this case save should be done.
                        final EntityCentreConfig ecc = factory.newByKey(EntityCentreConfig.class, baseOfTheCurrentUser(), title, mainMenuItemController.findByKey(menuItemType.getName()));
                        ecc.setPrincipal(true);
                        final ICentreDomainTreeManagerAndEnhancer centre = getEntityCentreManager(menuItemType, null);
                        ecc.setConfigBody(getSerialiser().serialise(centre));
                        ecc.setDesc(newDesc == null ? DEFAULT_CONFIG_DESC : newDesc);
                        saveCentre(centre, ecc);
                        if (persistentCentres != null) {
                            persistentCentres.put(key(menuItemType, null), copyCentre(currentMgr));
                        }
                    }
                } else {
                    if (!isEntityCentreManagerOwner(menuItemType, null)) {
                        error("Unable to save a NON-PRINCIPLE entity-centre instance for type [" + menuItemType.getSimpleName() + "] with title [" + title + "] for current user ["
                                + currentUser() + "] -- the base user [id = " + baseOfTheCurrentUser().getId() + "] owns this entity centre.");
                    } else {
                        error("Unable to save non-existent entity-centre instance for type [" + menuItemType.getSimpleName() + "] with title [" + title + "] for current user ["
                                + currentUser() + "].");
                    }
                }
            } else { // > 1
                error("There are more than one entity-centre instance for type [" + menuItemType.getSimpleName() + "] with title [" + title + "] for current user ["
                        + currentUser() + "].");
            }
        }
        return this;
    }

    /**
     * Checks if an instance of manager has been initialised and its inner parts (locators, analyses) have been fully accepted/discarded.
     *
     * @param currentMgr
     * @param menuItemType
     * @param name
     */
    private void validateBeforeSaving(final ICentreDomainTreeManagerAndEnhancer currentMgr, final Class<?> menuItemType, final String name) {
        notInitiliasedError(currentMgr, menuItemType, name);
        // let's iterate through all locators and ask if they are in USAGE phase!
        for (final Pair<Class<?>, String> locatorKey : currentMgr.getFirstTick().locatorKeys()) {
            if (USAGE_PHASE != currentMgr.getFirstTick().phaseAndTypeOfLocatorManager(locatorKey.getKey(), locatorKey.getValue()).getKey()) {
                error("The inner part of entity centre [locator " + locatorKey + "] is not in Usage phase. It should be accepted or discarded before save operation.");
            }
        }
        // let's iterate through all analyses and ask if they are accepted
        for (final String analysisKey : currentMgr.analysisKeys()) {
            if (currentMgr.isChangedAnalysisManager(analysisKey)) {
                error("The inner part of entity centre [analysis " + analysisKey + "] is not accepted. It should be accepted or discarded before save operation.");
            }
        }
    }

    /**
     * Checks if an instance of manager has been initialised and its inner parts (locators, analyses) have been fully accepted/discarded.
     *
     * @param currentMgr
     * @param root
     * @param name
     */
    private void masterValidateBeforeSaving(final IMasterDomainTreeManager currentMgr, final Class<?> root) {
        masterNotInitiliasedError(currentMgr, root);
        // let's iterate through all locators and ask if they are in USAGE_PHASE!
        for (final Pair<Class<?>, String> locatorKey : currentMgr.locatorKeys()) {
            if (USAGE_PHASE != currentMgr.phaseAndTypeOfLocatorManager(locatorKey.getKey(), locatorKey.getValue()).getKey()) {
                error("The inner part of entity centre [locator " + locatorKey + "] is not in Usage phase. It should be accepted or discarded before save operation.");
            }
        }
    }

    @Override
    public IGlobalDomainTreeManager saveAsEntityCentreManager(final Class<?> menuItemType, final String originalName, final String newName, final String newDesc) {
        synchronized (this) {
            validateMenuItemType(menuItemType);
            validateMenuItemTypeRootType(menuItemType);

            if (isFreezedEntityCentreManager(menuItemType, originalName)) {
                error("Unable to SaveAs the 'freezed' entity-centre instance for type [" + menuItemType.getSimpleName() + "] with title [" + title(menuItemType, originalName)
                        + "] for current user [" + currentUser() + "].");
            }
            final ICentreDomainTreeManagerAndEnhancer originationMgr = getEntityCentreManager(menuItemType, originalName);
            validateBeforeSaving(originationMgr, menuItemType, originalName);
            // create a copy of current instance of entity centre
            final ICentreDomainTreeManagerAndEnhancer copyMgr = copyCentre(originationMgr);

            // save an instance of EntityCentreConfig with overridden body, which should exist in DB
            final String menuItemTypeName = menuItemType.getName();
            final String newTitle = title(menuItemType, newName);
            
            // for the new generation of Web UI centres (where persistentCentres == null and avoidPersistentCentres switch is on)
            // the model for saveAs configuration existence should not look on base configurations.
            final EntityResultQueryModel<EntityCentreConfig> model = persistentCentres == null ? modelForCurrentUser(menuItemTypeName, newTitle) : modelForCurrentAndBaseUsers(menuItemTypeName, newTitle);
            // entityCentreConfigController.getAllEntities(from(model).model());
            
            final int count = entityCentreConfigController.count(model);
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
                ecc.setDesc(newDesc == null ? DEFAULT_CONFIG_DESC : newDesc);
                ecc.setConfigBody(getSerialiser().serialise(copyMgr));
                saveCentre(copyMgr, ecc);
                init(menuItemType, newName, copyMgr, true);
            } else { // > 1
                error("There are at least one entity-centre instance for type [" + menuItemType.getSimpleName() + "] with title [" + newTitle + "] for current user [" + currentUser()
                        + (persistentCentres == null ? "]" : "] or its base [id = " + baseOfTheCurrentUser().getId()) + "].");
            }
            return this;
        }
    }

    private void saveCentre(final ICentreDomainTreeManagerAndEnhancer copyMgr, final EntityCentreConfig ecc) {
        if (ecc.isPersisted()) {
            entityCentreAnalysisConfigController.delete(analysesForConcreteECCmodel(ecc));
        }

        final EntityCentreConfig newECC = entityCentreConfigController.save(ecc);
        // populate Id and Version to be able to determine staleness of the centre
        ((CentreDomainTreeManagerAndEnhancer) copyMgr).setSavedEntityId(newECC.getId());
        ((CentreDomainTreeManagerAndEnhancer) copyMgr).setSavedEntityVersion(newECC.getVersion());

        for (final String analysisName : copyMgr.analysisKeys()) {
            final EntityCentreAnalysisConfig ecac = factory.newByKey(EntityCentreAnalysisConfig.class, newECC, analysisName);
            entityCentreAnalysisConfigController.save(ecac);
        }
    }

    @Override
    public boolean isChangedEntityCentreManager(final Class<?> menuItemType, final String name) {
        if (persistentCentres != null) {
            validateMenuItemType(menuItemType);
            validateMenuItemTypeRootType(menuItemType);

            notInitiliasedError(persistentCentres.get(key(menuItemType, name)), menuItemType, name);
            return !EntityUtils.equalsEx(currentCentres.get(key(menuItemType, name)), persistentCentres.get(key(menuItemType, name)));
        } else {
            throw new UnsupportedOperationException("avoidPersistentCentres switch is on.");
        }
    }

    @Override
    public IGlobalDomainTreeManager removeEntityCentreManager(final Class<?> menuItemType, final String name) {
        if (persistentCentres != null) {
            validateMenuItemType(menuItemType);
            validateMenuItemTypeRootType(menuItemType);

            if (isFreezedEntityCentreManager(menuItemType, name)) {
                error("Unable to Remove the 'freezed' entity-centre instance for type [" + menuItemType.getSimpleName() + "] with title [" + title(menuItemType, name)
                + "] for current user [" + currentUser() + "].");
            }
            notInitiliasedError(persistentCentres.get(key(menuItemType, name)), menuItemType, name);
            if (name == null) {
                error("Unable to remove a principle entity-centre for type [" + menuItemType.getSimpleName() + "].");
            } else if (Boolean.FALSE.equals(centresOwning.get(key(menuItemType, name)))) { // the report not owns by current user. It can not be removed by current user.
                error("Unable to remove the entity-centre instance, that current user does not own. The type [" + menuItemType.getSimpleName() + "] with title ["
                        + title(menuItemType, name) + "] for current user [" + currentUser() + "].");
            } else {
                removeCentre(menuItemType, name);

                final EntityResultQueryModel<EntityCentreConfig> model = modelForCurrentUser(menuItemType.getName(), title(menuItemType, name));
                final EntityCentreConfig ecc = entityCentreConfigController.getEntity(from(model).model());

                // remove all analyses dependencies
                entityCentreAnalysisConfigController.delete(analysesForConcreteECCmodel(ecc));

                // remove an instance of EntityCentreConfig which should exist in DB
                entityCentreConfigController.delete(ecc);
            }
            return this;
        } else {
            throw new UnsupportedOperationException("avoidPersistentCentres switch is on.");
        }
    }
    
    /**
     * Removes locally cached instances of centre configuration on current server node.
     * 
     * @param menuItemType
     * @param names
     */
    public void removeCentresLocally(final Class<?> menuItemType, final String ... names) {
        for (final String name: names) {
            currentCentres.remove(key(menuItemType, name));
        }
    }
    
    /**
     * Removes centre configurations from persistent storage.
     * 
     * @param menuItemType
     * @param names
     */
    public void removeCentres(final Class<?> menuItemType, final String ... names) {
        entityCentreConfigController.delete(multiModelForCurrentUser(menuItemType.getName(), stream(names).map(name -> title(menuItemType, name)).toArray(String[]::new)));
    }
    
    public void removeCentre(final Class<?> menuItemType, final String name) {
        if (persistentCentres != null) {
            currentCentres.remove(key(menuItemType, name));
            persistentCentres.remove(key(menuItemType, name));
            centresOwning.remove(key(menuItemType, name));
        } else {
            throw new UnsupportedOperationException("avoidPersistentCentres switch is on.");
        }
    }

    @Override
    public List<String> nonPrincipleEntityCentreNames(final Class<?> menuItemType) {
        validateMenuItemType(menuItemType);
        validateMenuItemTypeRootType(menuItemType);

        final List<String> names = new ArrayList<String>();

        final List<EntityCentreConfig> nonPricipalCentresFromTheCloud = entityCentreConfigController.getAllEntities(from(modelForCurrentAndBaseUsersNonPrincipal(menuItemType.getName())).with(fetchOnly(EntityCentreConfig.class).with("title").with("principal")).model());
        for (final EntityCentreConfig ecc : nonPricipalCentresFromTheCloud) {
            if (ecc.isPrincipal()) {
                throw new IllegalStateException("There should be no principal entity centre retrieved.");
            } else {
                names.add(ecc.getTitle());
            }
        }
        // sort 'names' by ascending:
        Collections.sort(names);
        return Collections.unmodifiableList(names);
    }

    @Override
    public boolean isEntityCentreManagerOwner(final Class<?> menuItemType, final String name) {
        return centresOwning.get(key(menuItemType, name));
    }

    /////////////////////////////////////////////////////////////////////////////////
    ///////////////////////////// Master management /////////////////////////////////
    /////////////////////////////////////////////////////////////////////////////////

    @Override
    public IMasterDomainTreeManager getMasterDomainTreeManager(final Class<?> root) {
        return currentMasters.get(root);
    }

    @Override
    public IGlobalDomainTreeManager initMasterDomainTreeManager(final Class<?> root) {
        final String rootName = root.getName();
        final EntityResultQueryModel<EntityMasterConfig> model = masterModelForCurrentUser(rootName);
        final int count = entityMasterConfigController.count(model);
        if (count == 1) { // the persistence layer contains an entity-master, so it should be retrieved and deserialised
            retrieveAndInitMaster(root, model);
            return this;
        } else if (count < 1) { // there is no own entity-master -- should be initialised by default.
            initMasterDomainTreeManagerByDefault(root);
        } else {
            error("There are more than one entity-master instance for type [" + root.getSimpleName() + "] for current user [" + currentUser() + "].");
        }
        return this;
    }

    @Override
    public IGlobalDomainTreeManager discardMasterDomainTreeManager(final Class<?> root) {
        masterNotInitiliasedError(persistentMasters.get(root), root);

        final String rootName = root.getName();
        final EntityResultQueryModel<EntityMasterConfig> model = masterModelForCurrentUser(rootName);
        final int count = entityMasterConfigController.count(model);
        if (count == 1) { // the persistence layer contains own entity-master, so the changes should be rejected
            currentMasters.put(root, copyMaster(persistentMasters.get(root)));
        } else if (count < 1) { // there is no own entity-master -- should be resetted to empty!
            currentMasters.remove(root);
            persistentMasters.remove(root);
        } else {
            error("There are more than one entity-master instance for type [" + root.getSimpleName() + "] for current user [" + currentUser() + "].");
        }
        return this;
    }

    @Override
    public IGlobalDomainTreeManager saveMasterDomainTreeManager(final Class<?> root) {
        final IMasterDomainTreeManager currentMgr = getMasterDomainTreeManager(root);
        masterValidateBeforeSaving(currentMgr, root);

        // save an instance of EntityMasterConfig with overridden body
        final EntityResultQueryModel<EntityMasterConfig> model = masterModelForCurrentUser(root.getName());
        final int count = entityMasterConfigController.count(model);
        if (count == 1) { // for current user => 1 entity-centre
            final EntityMasterConfig emc = entityMasterConfigController.getEntity(from(model).model());
            emc.setConfigBody(getSerialiser().serialise(currentMgr));
            entityMasterConfigController.save(emc);

            persistentMasters.put(root, copyMaster(currentMgr));
        } else if (count < 1) { // there is no entity-centre
            // save a new instance of EntityMasterConfig
            final EntityMasterConfig emc = factory.newByKey(EntityMasterConfig.class, currentUser(), root.getName());
            emc.setConfigBody(getSerialiser().serialise(currentMgr));
            entityMasterConfigController.save(emc);

            persistentMasters.put(root, copyMaster(currentMgr));
        } else { // > 1
            error("There are more than one entity-master instance for type [" + root.getSimpleName() + "] for current user [" + currentUser() + "].");
        }
        return this;
    }

    //    @Override
    //    public boolean isChangedEntityMasterManager(final Class<?> root) {
    //	masterNotInitiliasedError(persistentMasters.get(root), root);
    //	return !EntityUtils.equalsEx(currentMasters.get(root), persistentMasters.get(root));
    //    }

    @Override
    public IGlobalDomainTreeManager initMasterDomainTreeManagerByDefault(final Class<?> root) {
        final String rootName = root.getName();
        final EntityResultQueryModel<EntityMasterConfig> model = masterModelForBaseUser(rootName);
        final int count = entityMasterConfigController.count(model);
        if (count == 1) { // the persistence layer contains an entity-master for base user, so it should be retrieved and deserialised
            retrieveAndInitMaster(root, model);
            return this;
        } else if (count < 1) { // there is own entity-master -- should be initialised by default.
            // Entity-master should be initialised and then saved. This can be done naturally by base user.
            // But if base user haven't did it yet, it will be done by non-base user automatically.
            initMaster(root, new MasterDomainTreeManager(getSerialiser(), new HashSet<Class<?>>() {
                {
                    add(root);
                }
            }));
            // save a new instance of EntityMasterConfig
            final EntityMasterConfig emc = factory.newByKey(EntityMasterConfig.class, baseOfTheCurrentUser(), rootName);
            emc.setConfigBody(getSerialiser().serialise(getMasterDomainTreeManager(root)));
            entityMasterConfigController.save(emc);
            return this;
        } else {
            error("There are more than one entity-master instance for type [" + root.getSimpleName() + "] for current user [" + baseOfTheCurrentUser() + "].");
        }
        return this;
    }

    @Override
    public void copyDefaults(final Class<?> menuItemType, final String name) {
        if (persistentCentres != null) {
            final Class<?> root = validateMenuItemTypeRootType(menuItemType);
            final IAddToCriteriaTickRepresentation ctr = currentCentres.get(key(menuItemType, name)).getRepresentation().getFirstTick();
            final IAddToCriteriaTickRepresentation ptr = persistentCentres.get(key(menuItemType, name)).getRepresentation().getFirstTick();
            ptr.setValuesByDefault(root, ctr.getValuesByDefault(root));
            ptr.setValues2ByDefault(root, ctr.getValues2ByDefault(root));
        } else {
            throw new UnsupportedOperationException("avoidPersistentCentres switch is on.");
        }
    }

    /**
     * Turns on the switch of 'not using persistentCentres' for {@link #isChangedEntityCentreManager(Class, String)}, {@link #discardEntityCentreManager(Class, String)} and other logic.
     * <p>
     * This is to be used for performance reasons not to use heavy {@link #copyCentre(ICentreDomainTreeManagerAndEnhancer)} logic where it is not necessary.
     */
    protected void avoidPersistentCentres() {
        this.persistentCentres = null;
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
        return entityCentreConfigController.getEntity(
            from(modelForCurrentUser(menuItemType.getName(), title(menuItemType, name))).model()
        );
    }
    
}
