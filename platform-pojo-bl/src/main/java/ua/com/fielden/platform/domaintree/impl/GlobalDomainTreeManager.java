package ua.com.fielden.platform.domaintree.impl;

import static ua.com.fielden.platform.domaintree.ILocatorManager.Phase.USAGE_PHASE;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.fetchOnly;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.from;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.select;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;

import ua.com.fielden.platform.domaintree.IGlobalDomainTreeManager;
import ua.com.fielden.platform.domaintree.IGlobalDomainTreeRepresentation;
import ua.com.fielden.platform.domaintree.centre.ICentreDomainTreeManager.ICentreDomainTreeManagerAndEnhancer;
import ua.com.fielden.platform.domaintree.centre.impl.CentreDomainTreeManagerAndEnhancer;
import ua.com.fielden.platform.domaintree.centre.impl.CentreDomainTreeManagerAndEnhancer.AddToCriteriaTickManagerAndEnhancer;
import ua.com.fielden.platform.domaintree.master.IMasterDomainTreeManager;
import ua.com.fielden.platform.domaintree.master.impl.MasterDomainTreeManager;
import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.entity.query.model.EntityResultQueryModel;
import ua.com.fielden.platform.reflection.Finder;
import ua.com.fielden.platform.security.user.IUserProvider;
import ua.com.fielden.platform.security.user.User;
import ua.com.fielden.platform.serialisation.api.ISerialiser;
import ua.com.fielden.platform.swing.review.annotations.EntityType;
import ua.com.fielden.platform.ui.config.EntityCentreConfig;
import ua.com.fielden.platform.ui.config.EntityMasterConfig;
import ua.com.fielden.platform.ui.config.MainMenuItem;
import ua.com.fielden.platform.ui.config.api.IEntityCentreConfigController;
import ua.com.fielden.platform.ui.config.api.IEntityLocatorConfigController;
import ua.com.fielden.platform.ui.config.api.IEntityMasterConfigController;
import ua.com.fielden.platform.ui.config.api.IMainMenuItemController;
import ua.com.fielden.platform.utils.EntityUtils;
import ua.com.fielden.platform.utils.Pair;

import com.google.inject.Inject;

/**
 * The global domain tree manager implementation.
 *
 * @author TG Team
 *
 */
public class GlobalDomainTreeManager extends AbstractDomainTree implements IGlobalDomainTreeManager {
    private final static Logger logger = Logger.getLogger(GlobalDomainTreeManager.class);
    private final EntityFactory factory;
    private final IUserProvider userProvider;
    private final IGlobalDomainTreeRepresentation gdtr;
    private final IMainMenuItemController mainMenuItemController;
    private final IEntityCentreConfigController entityCentreConfigController;
    private final IEntityMasterConfigController entityMasterConfigController;

    private final EnhancementPropertiesMap<ICentreDomainTreeManagerAndEnhancer> persistentCentres;
    private final transient EnhancementPropertiesMap<ICentreDomainTreeManagerAndEnhancer> currentCentres;
    private final transient EnhancementPropertiesMap<ICentreDomainTreeManagerAndEnhancer> freezedCentres;
    private final transient EnhancementPropertiesMap<Boolean> centresOwning;

    private final EnhancementRootsMap<IMasterDomainTreeManager> persistentMasters;
    private final transient EnhancementRootsMap<IMasterDomainTreeManager> currentMasters;

    @Inject
    public GlobalDomainTreeManager(final ISerialiser serialiser, final EntityFactory factory, final IUserProvider userProvider, final IMainMenuItemController mainMenuItemController, final IEntityCentreConfigController entityCentreConfigController, final IEntityMasterConfigController entityMasterConfigController, final IEntityLocatorConfigController entityLocatorConfigController) {
	super(serialiser);
	this.factory = factory;
	this.userProvider = userProvider;
	this.mainMenuItemController = mainMenuItemController;
	this.entityCentreConfigController = entityCentreConfigController;
	this.entityMasterConfigController = entityMasterConfigController;
	this.gdtr = new GlobalDomainTreeRepresentation(serialiser, factory, userProvider, entityLocatorConfigController);

	// lazy stuff
	this.persistentCentres = createPropertiesMap();
	this.currentCentres = createPropertiesMap();
	this.freezedCentres = createPropertiesMap();
	this.centresOwning = createPropertiesMap();

	this.persistentMasters = createRootsMap();
	this.currentMasters = createRootsMap();
    }

    @Override
    public List<Class<?>> entityCentreMenuItemTypes() {
	final List<MainMenuItem> mmItems = this.mainMenuItemController.getAllEntities(from(select(MainMenuItem.class).model()).model());
	final List<Class<?>> res = new ArrayList<Class<?>>();
	for (final MainMenuItem mmi : mmItems) {
	    try {
		final Class<?> mmiType = ClassLoader.getSystemClassLoader().loadClass(mmi.getKey());
		try {
		    validateMenuItemType(mmiType);
		    validateMenuItemTypeRootType(mmiType);
		    res.add(mmiType);
		} catch (final IllegalArgumentException e) {
		}
	    } catch (final ClassNotFoundException e) {
		throw new IllegalStateException(e);
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
	    final Class<?> parentClassForMenuItems = ClassLoader.getSystemClassLoader().loadClass("ua.com.fielden.platform.swing.menu.MiWithConfigurationSupport");
	    if (!parentClassForMenuItems.isAssignableFrom(menuItemType)) {
		error("The menu item type " + menuItemType.getSimpleName() + " is not 'MiWithConfigurationSupport' descendant, which should be a parent type for menu items for all entity centres.");
	    }
	} catch (final ClassNotFoundException e) {
	    e.printStackTrace();
	    error("There are no loaded class 'MiWithConfigurationSupport', which should be a parent type for menu items for entity centres. [" + e.getMessage() + "]");
	}
    }

    private static Class<?> validateMenuItemTypeRootType(final Class<?> menuItemType) {
	final EntityType etAnnotation = menuItemType.getAnnotation(EntityType.class);
	if (etAnnotation == null || etAnnotation.value() == null) {
	    error("The menu item type " + menuItemType.getSimpleName() + " has no 'EntityType' annotation, which is necessary to specify the root type of the centre.");
	}
	final Class<?> root = etAnnotation.value();
	validateRootType(root);
	return root;
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
     * Gets a {@link EntityCentreConfig#getTitle()} component in {@link EntityCentreConfig}'s key from {@link IGlobalDomainTreeManager}'s contract
     * for entity-centre naming -- 'menu item type name' or 'name' (in case of 'saveAs' centre).
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
    public void initEntityCentreManager(final Class<?> menuItemType, final String name) {
	validateMenuItemType(menuItemType);
	final Class<?> root = validateMenuItemTypeRootType(menuItemType);
	if (isFreezedEntityCentreManager(menuItemType, name)) {
	    error("Unable to Init the 'freezed' entity-centre instance for type [" + menuItemType.getSimpleName() + "] with title [" + title(menuItemType, name) + "] for current user [" + currentUser() + "].");
	}
	final String title = title(menuItemType, name);
	final String menuItemTypeName = menuItemType.getName();
	final EntityResultQueryModel<EntityCentreConfig> model = modelForCurrentAndBaseUsers(menuItemTypeName, title);
	final int count = entityCentreConfigController.count(model);
	if (count == 1) { // the persistence layer contains a entity-centre, so it should be retrieved and deserialised
	    retrieveAndInit(menuItemType, name, model);
	    return;
	} else if (count < 1) { // there is no entity-centre
	    if (name == null) { // principle entity-centre
		// Principle entity-centre should be initialised and then saved. This can be done naturally by base user.
		// But if base user haven't done that yet, it will be done by non-base user automatically.
		final boolean owning = currentUser().isBase();
		init(menuItemType, name, new CentreDomainTreeManagerAndEnhancer(getSerialiser(), new HashSet<Class<?>>() {{ add(root); }}), owning);
		// save a new instance of EntityCentreConfig
		final EntityCentreConfig ecc = factory.newByKey(EntityCentreConfig.class, baseOfTheCurrentUser(), title, mainMenuItemController.findByKey(menuItemTypeName));
		ecc.setPrincipal(true);
		ecc.setConfigBody(getSerialiser().serialise(getEntityCentreManager(menuItemType, null)));
		entityCentreConfigController.save(ecc);
		return;
	    } else {
		error("Unable to initialise a non-existent entity-centre instance for type [" + menuItemType.getSimpleName() + "] with title [" + title + "] for current user [" + currentUser() + "].");
	    }
	} else if (count == 2) {
	    final EntityResultQueryModel<EntityCentreConfig> model1 = modelForCurrentUser(menuItemTypeName, title);
	    final int count1 = entityCentreConfigController.count(model1);
	    if (count1 == 1) { // for current user => 1 entity-centre, for base => another one with same title
		// initialise an instance for current user (base configuration will be ignored)
		retrieveAndInit(menuItemType, name, model1);
	    } else {
		error("There are more than one entity-centre instance for type [" + menuItemType.getSimpleName() + "] with title [" + title + "] for current user [" + currentUser() + "].");
	    }
	} else {
	    error("There are more than one entity-centre instance for type [" + menuItemType.getSimpleName() + "] with title [" + title + "] for current user [" + currentUser() + "].");
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
     * Creates a model to retrieve {@link EntityCentreConfig} instances for the current user and its base user with a <code>title</code> and <code>menuItemTypeName</code> specified.
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
     * Creates a model to retrieve {@link EntityCentreConfig} instances for the current user and its base user with a <code>title</code> and <code>menuItemTypeName</code> specified.
     *
     * @param menuItemTypeName
     * @param title
     * @return
     */
    private EntityResultQueryModel<EntityCentreConfig> modelForCurrentAndBaseUsers(final String menuItemTypeName) {
	final EntityResultQueryModel<EntityCentreConfig> model =
	    /*    */select(EntityCentreConfig.class).where().//
	    /*    */begin().prop("owner").eq().val(currentUser()).or().prop("owner").eq().val(baseOfTheCurrentUser()).end().and().// look for entity-centres for both users (current and its base)
	    /*    */prop("menuItem.key").eq().val(menuItemTypeName).model();
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
     * Logs and throws an {@link IllegalArgumentException} error with specified message.
     *
     * @param message
     */
    private static void error(final String message) {
	logger.error(message);
	throw new IllegalArgumentException(message);
    }

    /**
     * Retrieves and initialises a instance of manager.
     *
     * @param menuItemType
     * @param name
     * @param model
     */
    private void retrieveAndInit(final Class<?> menuItemType, final String name, final EntityResultQueryModel<EntityCentreConfig> model) {
	final EntityCentreConfig ecc = entityCentreConfigController.getEntity(from(model).model());
	final boolean owning = ecc.getOwner().equals(currentUser());
	try {
	    init(menuItemType, name, getSerialiser().deserialise(ecc.getConfigBody(), ICentreDomainTreeManagerAndEnhancer.class), owning);
	    return;
	} catch (final Exception e) {
	    e.printStackTrace();
	    final String message = "Unable to deserialise a entity-centre instance for type [" + menuItemType.getSimpleName() + "] with title [" + title(menuItemType, name) + "] for current user [" + currentUser() + "].";
	    error(message);
	}
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
	    initMaster(root, getSerialiser().deserialise(emc.getConfigBody(), IMasterDomainTreeManager.class));
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
	final LocatorManager locatorManager = (((AddToCriteriaTickManagerAndEnhancer)mgr.getFirstTick()).base()).locatorManager();
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
	final LocatorManager locatorManager = ((MasterDomainTreeManager)mgr).locatorManager();
	final IGlobalDomainTreeRepresentation globalRepresentation = this.getGlobalRepresentation();
	initLocatorManagerCrossReferences(locatorManager, globalRepresentation);
	return mgr;
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
     * A copy method for entity centre that copies also "transient" stuff like currentAnalyses and freezedAnalyses.
     * It has been done to take care about copying entity centre with some changed / freezed analyses (all that changes
     * will be promoted to copies).
     *
     * @param centre
     * @return
     */
    private ICentreDomainTreeManagerAndEnhancer copyCentre(final ICentreDomainTreeManagerAndEnhancer centre) {
	// final TgKryo kryo = (TgKryo) getSerialiser();
	// TODO kryo.register(CentreDomainTreeManager.class, new CentreDomainTreeManagerSerialiserWithTransientAnalyses(kryo));
	final ICentreDomainTreeManagerAndEnhancer copy = initCentreManagerCrossReferences(EntityUtils.deepCopy(centre, getSerialiser()));
	// TODO kryo.register(CentreDomainTreeManager.class);
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
    private void init(final Class<?> menuItemType, final String name, final ICentreDomainTreeManagerAndEnhancer mgr, final boolean owning) {
	final ICentreDomainTreeManagerAndEnhancer fullyDefinedMgr = initCentreManagerCrossReferences(mgr);
	currentCentres.put(key(menuItemType, name), fullyDefinedMgr);
	persistentCentres.put(key(menuItemType, name), copyCentre(fullyDefinedMgr));
	centresOwning.put(key(menuItemType, name), owning);
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
    public void discardEntityCentreManager(final Class<?> menuItemType, final String name) {
	validateMenuItemType(menuItemType);
	validateMenuItemTypeRootType(menuItemType);
	final ICentreDomainTreeManagerAndEnhancer persistentCentre = persistentCentres.get(key(menuItemType, name));
	notInitiliasedError(persistentCentre, menuItemType, name);
	currentCentres.put(key(menuItemType, name), copyCentre(persistentCentre));

	if (isFreezedEntityCentreManager(menuItemType, name)) {
	    unfreeze(menuItemType, name);
	}
    }

    @Override
    public void freezeEntityCentreManager(final Class<?> menuItemType, final String name) {
	validateMenuItemType(menuItemType);
	validateMenuItemTypeRootType(menuItemType);
	if (isFreezedEntityCentreManager(menuItemType, name)) {
	    error("Unable to freeze the entity-centre instance more than once for type [" + menuItemType.getSimpleName() + "] with title [" + title(menuItemType, name) + "] for current user [" + currentUser() + "].");
	}
	notInitiliasedError(currentCentres.get(key(menuItemType, name)), menuItemType, name);
	notInitiliasedError(persistentCentres.get(key(menuItemType, name)), menuItemType, name);

	freezedCentres.put(key(menuItemType, name), persistentCentres.remove(key(menuItemType, name)));
	persistentCentres.put(key(menuItemType, name), copyCentre(currentCentres.get(key(menuItemType, name))));
	currentCentres.put(key(menuItemType, name), copyCentre(currentCentres.get(key(menuItemType, name)))); // this is necessary to dispose current manager with listeners and get equal "fresh" instance
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
	if (!isFreezedEntityCentreManager(menuItemType, name)) {
	    error("Unable to unfreeze the entity-centre instance that is not 'freezed' for type [" + menuItemType.getSimpleName() + "] with title [" + title(menuItemType, name) + "] for current user [" + currentUser() + "].");
	}
	persistentCentres.put(key(menuItemType, name), freezedCentres.remove(key(menuItemType, name)));
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
	    error("Unable to perform this operation on the entity-centre instance, that wasn't initialised, for type [" + menuItemType.getSimpleName() + "] with title [" + title(menuItemType, name) + "] for current user [" + currentUser() + "].");
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
	    error("Unable to perform this operation on the entity-master instance, that wasn't initialised, for type [" + root.getSimpleName() + "] for current user [" + currentUser() + "].");
	}
    }

    @Override
    public void saveEntityCentreManager(final Class<?> menuItemType, final String name) {
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
		entityCentreConfigController.save(ecc);

		persistentCentres.put(key(menuItemType, name), copyCentre(currentMgr));
	    } else if (count < 1) { // there is no entity-centre
		error("Unable to save a non-existent (non-visible for current user) entity-centre instance for type [" + menuItemType.getSimpleName() + "] with title [" + title + "] for current user [" + currentUser() + "].");
	    } else { // > 1
		error("There are more than one entity-centre instance for type [" + menuItemType.getSimpleName() + "] with title [" + title + "] for current user [" + currentUser() + "].");
	    }
	}
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
    public void saveAsEntityCentreManager(final Class<?> menuItemType, final String originalName, final String newName) {
	validateMenuItemType(menuItemType);
	validateMenuItemTypeRootType(menuItemType);

	if (isFreezedEntityCentreManager(menuItemType, originalName)) {
	    error("Unable to SaveAs the 'freezed' entity-centre instance for type [" + menuItemType.getSimpleName() + "] with title [" + title(menuItemType, originalName) + "] for current user [" + currentUser() + "].");
	}
	final ICentreDomainTreeManagerAndEnhancer originationMgr = getEntityCentreManager(menuItemType, originalName);
	validateBeforeSaving(originationMgr, menuItemType, originalName);
	// create a copy of current instance of entity centre
	final ICentreDomainTreeManagerAndEnhancer copyMgr = copyCentre(originationMgr);

	// save an instance of EntityCentreConfig with overridden body, which should exist in DB
	final String menuItemTypeName = menuItemType.getName();
	final String newTitle = title(menuItemType, newName);

	final EntityResultQueryModel<EntityCentreConfig> model = modelForCurrentAndBaseUsers(menuItemTypeName, newTitle);
	entityCentreConfigController.getAllEntities(from(model).model());

	final int count = entityCentreConfigController.count(model);
	if (count == 0) { // for current user or its base => there are no entity-centres, so persist a copy with a new title
	    final EntityCentreConfig ecc = factory.newByKey(EntityCentreConfig.class, currentUser(), newTitle, mainMenuItemController.findByKey(menuItemTypeName));
	    ecc.setConfigBody(getSerialiser().serialise(copyMgr));
	    entityCentreConfigController.save(ecc);
	    init(menuItemType, newName, copyMgr, true);
	} else { // > 1
	    error("There are at least one entity-centre instance for type [" + menuItemType.getSimpleName() + "] with title [" + newTitle + "] for current user [" + currentUser() + "] or its base [" + baseOfTheCurrentUser() + "].");
	}
    }

    @Override
    public boolean isChangedEntityCentreManager(final Class<?> menuItemType, final String name) {
	validateMenuItemType(menuItemType);
	validateMenuItemTypeRootType(menuItemType);

	notInitiliasedError(persistentCentres.get(key(menuItemType, name)), menuItemType, name);
	return !EntityUtils.equalsEx(currentCentres.get(key(menuItemType, name)), persistentCentres.get(key(menuItemType, name)));
    }

    @Override
    public void removeEntityCentreManager(final Class<?> menuItemType, final String name) {
	validateMenuItemType(menuItemType);
	validateMenuItemTypeRootType(menuItemType);

	if (isFreezedEntityCentreManager(menuItemType, name)) {
	    error("Unable to Remove the 'freezed' entity-centre instance for type [" + menuItemType.getSimpleName() + "] with title [" + title(menuItemType, name) + "] for current user [" + currentUser() + "].");
	}
	notInitiliasedError(persistentCentres.get(key(menuItemType, name)), menuItemType, name);
	if (name == null) {
	    error("Unable to remove a principle entity-centre for type [" + menuItemType.getSimpleName() + "].");
	} else if (Boolean.FALSE.equals(centresOwning.get(key(menuItemType, name)))) { // the report not owns by current user. It can not be removed by current user.
	    error("Unable to remove the entity-centre instance, that current user does not own. The type [" + menuItemType.getSimpleName() + "] with title [" + title(menuItemType, name) + "] for current user [" + currentUser() + "].");
	} else {
	    currentCentres.remove(key(menuItemType, name));
	    persistentCentres.remove(key(menuItemType, name));
	    centresOwning.remove(key(menuItemType, name));

	    // remove an instance of EntityCentreConfig which should exist in DB
	    entityCentreConfigController.delete(modelForCurrentUser(menuItemType.getName(), title(menuItemType, name)));
	}
    }

    @Override
    public Set<String> entityCentreNames(final Class<?> menuItemType) {
	validateMenuItemType(menuItemType);
	validateMenuItemTypeRootType(menuItemType);

	final Set<String> names = new HashSet<String>();

	final List<EntityCentreConfig> centresFromTheCloud = entityCentreConfigController.getAllEntities(from(modelForCurrentAndBaseUsers(menuItemType.getName())).with(fetchOnly(EntityCentreConfig.class).with("title").with("principal")).model());
	for (final EntityCentreConfig ecc : centresFromTheCloud) {
	    if (ecc.isPrincipal()) {
		names.add(null);
	    } else {
		names.add(ecc.getTitle());
	    }
	}
//	for (final Pair<Class<?>, String> key : currentCentres.keySet()) {
//	    if (menuItemType.equals(key.getKey())) {
//		names.add(key.getValue());
//	    }
//	}

	return Collections.unmodifiableSet(names);
    }

    /////////////////////////////////////////////////////////////////////////////////
    ///////////////////////////// Master management /////////////////////////////////
    /////////////////////////////////////////////////////////////////////////////////

    @Override
    public IMasterDomainTreeManager getEntityMasterManager(final Class<?> root) {
	return currentMasters.get(root);
    }

    @Override
    public void initEntityMasterManager(final Class<?> root) {
	final String rootName = root.getName();
	final EntityResultQueryModel<EntityMasterConfig> model = masterModelForCurrentUser(rootName);
	final int count = entityMasterConfigController.count(model);
	if (count == 1) { // the persistence layer contains an entity-master, so it should be retrieved and deserialised
	    retrieveAndInitMaster(root, model);
	    return;
	} else if (count < 1) { // there is no own entity-master -- should be initialised by default.
	    initEntityMasterManagerByDefault(root);
	}  else {
	    error("There are more than one entity-master instance for type [" + root.getSimpleName() + "] for current user [" + currentUser() + "].");
	}
    }

    @Override
    public void discardEntityMasterManager(final Class<?> root) {
	masterNotInitiliasedError(persistentMasters.get(root), root);

	final String rootName = root.getName();
	final EntityResultQueryModel<EntityMasterConfig> model = masterModelForCurrentUser(rootName);
	final int count = entityMasterConfigController.count(model);
	if (count == 1) { // the persistence layer contains own entity-master, so the changes should be rejected
	    currentMasters.put(root, copyMaster(persistentMasters.get(root)));
	} else if (count < 1) { // there is no own entity-master -- should be resetted to empty!
	    currentMasters.remove(root);
	    persistentMasters.remove(root);
	}  else {
	    error("There are more than one entity-master instance for type [" + root.getSimpleName() + "] for current user [" + currentUser() + "].");
	}
    }

    @Override
    public void saveEntityMasterManager(final Class<?> root) {
	final IMasterDomainTreeManager currentMgr = getEntityMasterManager(root);
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
    }

    //    @Override
    //    public boolean isChangedEntityMasterManager(final Class<?> root) {
    //	masterNotInitiliasedError(persistentMasters.get(root), root);
    //	return !EntityUtils.equalsEx(currentMasters.get(root), persistentMasters.get(root));
    //    }

    @Override
    public void initEntityMasterManagerByDefault(final Class<?> root) {
	final String rootName = root.getName();
	final EntityResultQueryModel<EntityMasterConfig> model = masterModelForBaseUser(rootName);
	final int count = entityMasterConfigController.count(model);
	if (count == 1) { // the persistence layer contains an entity-master for base user, so it should be retrieved and deserialised
	    retrieveAndInitMaster(root, model);
	    return;
	} else if (count < 1) { // there is own entity-master -- should be initialised by default.
	    // Entity-master should be initialised and then saved. This can be done naturally by base user.
	    // But if base user haven't did it yet, it will be done by non-base user automatically.
	    initMaster(root, new MasterDomainTreeManager(getSerialiser(), new HashSet<Class<?>>() {{ add(root); }}));
	    // save a new instance of EntityMasterConfig
	    final EntityMasterConfig emc = factory.newByKey(EntityMasterConfig.class, baseOfTheCurrentUser(), rootName);
	    emc.setConfigBody(getSerialiser().serialise(getEntityMasterManager(root)));
	    entityMasterConfigController.save(emc);
	    return;
	}  else {
	    error("There are more than one entity-master instance for type [" + root.getSimpleName() + "] for current user [" + baseOfTheCurrentUser() + "].");
	}
    }
}
