package ua.com.fielden.platform.treemodel.rules.impl;

import static ua.com.fielden.platform.equery.equery.select;

import java.lang.reflect.Field;
import java.util.HashSet;

import org.apache.log4j.Logger;

import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.equery.interfaces.IQueryModel;
import ua.com.fielden.platform.reflection.Finder;
import ua.com.fielden.platform.security.user.IUserProvider;
import ua.com.fielden.platform.security.user.User;
import ua.com.fielden.platform.serialisation.api.ISerialiser;
import ua.com.fielden.platform.treemodel.rules.IGlobalDomainTreeManager;
import ua.com.fielden.platform.treemodel.rules.IGlobalDomainTreeRepresentation;
import ua.com.fielden.platform.treemodel.rules.criteria.ICriteriaDomainTreeManager.ICriteriaDomainTreeManagerAndEnhancer;
import ua.com.fielden.platform.treemodel.rules.criteria.ILocatorDomainTreeManager.ILocatorDomainTreeManagerAndEnhancer;
import ua.com.fielden.platform.treemodel.rules.criteria.impl.CriteriaDomainTreeManager.AddToCriteriaTickManager;
import ua.com.fielden.platform.treemodel.rules.criteria.impl.CriteriaDomainTreeManagerAndEnhancer;
import ua.com.fielden.platform.treemodel.rules.criteria.impl.CriteriaDomainTreeManagerAndEnhancer.AddToCriteriaTickManagerAndEnhancer;
import ua.com.fielden.platform.treemodel.rules.master.IMasterDomainTreeManager;
import ua.com.fielden.platform.treemodel.rules.master.impl.MasterDomainTreeManager;
import ua.com.fielden.platform.ui.config.EntityCentreConfig;
import ua.com.fielden.platform.ui.config.EntityMasterConfig;
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
    private final Logger logger = Logger.getLogger(getClass());
    private final EntityFactory factory;
    private final IUserProvider userProvider;
    private final IGlobalDomainTreeRepresentation gdtr;
    private final IMainMenuItemController mainMenuItemController;
    private final IEntityCentreConfigController entityCentreConfigController;
    private final IEntityMasterConfigController entityMasterConfigController;

    private final EnhancementPropertiesMap<ICriteriaDomainTreeManagerAndEnhancer> persistentCentres;
    private final transient EnhancementPropertiesMap<ICriteriaDomainTreeManagerAndEnhancer> currentCentres;
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
	this.centresOwning = createPropertiesMap();

	this.persistentMasters = createRootsMap();
	this.currentMasters = createRootsMap();
    }

    @Override
    public IUserProvider getUserProvider() {
	return userProvider;
    }

    @Override
    public IGlobalDomainTreeRepresentation getGlobalRepresentation() {
	return gdtr;
    }

    @Override
    public ICriteriaDomainTreeManagerAndEnhancer getEntityCentreManager(final Class<?> root, final String name) {
	return currentCentres.get(key(root, name));
    }

    private User currentUser() {
	return userProvider.getUser();
    }

    private User baseOfTheCurrentUser() {
	return currentUser().isBase() ? currentUser() : currentUser().getBasedOnUser();
    }

    /**
     * Gets a {@link EntityCentreConfig#getTitle()} component in {@link EntityCentreConfig}'s key from {@link IGlobalDomainTreeManager}'s contract for entity-centre naming --
     * [root class, name].
     *
     * @param root
     * @param name
     * @return
     */
    private String title(final Class<?> root, final String name) {
	return name == null ? root.getName() : name;
    }

    @Override
    public void initEntityCentreManager(final Class<?> root, final String name) {
	final String rootName = root.getName();
	final String title = title(root, name);
	final IQueryModel<EntityCentreConfig> model = modelForCurrentAndBaseUsers(rootName, title);
	final int count = entityCentreConfigController.count(model);
	if (count == 1) { // the persistence layer contains a entity-centre, so it should be retrieved and deserialised
	    retrieveAndInit(root, name, model);
	    return;
	} else if (count < 1) { // there is no entity-centre
	    if (name == null) { // principle entity-centre
		// Principle entity-centre should be initialised and then saved. This can be done naturally by base user.
		// But if base user haven't did it yet, it will be done by non-base user automatically.
		final boolean owning = currentUser().isBase();
		init(root, name, new CriteriaDomainTreeManagerAndEnhancer(getSerialiser(), new HashSet<Class<?>>() {{ add(root); }}), owning);
		// save a new instance of EntityCentreConfig
		final EntityCentreConfig ecc = factory.newByKey(EntityCentreConfig.class, baseOfTheCurrentUser(), title, mainMenuItemController.findByKey(rootName));
		ecc.setConfigBody(getSerialiser().serialise(getEntityCentreManager(root, null)));
		entityCentreConfigController.save(ecc);
		return;
	    } else {
		error("Unable to initialise a non-existent entity-centre instance for type [" + root.getSimpleName() + "] with title [" + title + "] for current user [" + currentUser() + "].");
	    }
	} else if (count == 2) {
	    final IQueryModel<EntityCentreConfig> model1 = modelForCurrentUser(rootName, title);
	    final int count1 = entityCentreConfigController.count(model1);
	    if (count1 == 1) { // for current user => 1 entity-centre, for base => another one with same title
		// initialise an instance for current user (base configuration will be ignored)
		retrieveAndInit(root, name, model1);
	    } else {
		error("There are more than one entity-centre instance for type [" + root.getSimpleName() + "] with title [" + title + "] for current user [" + currentUser() + "].");
	    }
	} else {
	    error("There are more than one entity-centre instance for type [" + root.getSimpleName() + "] with title [" + title + "] for current user [" + currentUser() + "].");
	}
    }

    /**
     * Creates a model to retrieve {@link EntityCentreConfig} instances for the current user with a <code>title</code> and <code>rootName</code> specified.
     *
     * @param rootName
     * @param title
     * @return
     */
    private IQueryModel<EntityCentreConfig> modelForCurrentAndBaseUsers(final String rootName, final String title) {
	final IQueryModel<EntityCentreConfig> model =
	/*    */select(EntityCentreConfig.class).where().//
	/*    */begin().prop("owner").eq().val(currentUser()).or().prop("owner").eq().val(baseOfTheCurrentUser()).end().and().// look for entity-centres for both users (current and its base)
	/*    */prop("title").eq().val(title).and().//
	/*    */prop("menuItem.key").eq().val(rootName).model();
	return model;
    }

    /**
     * Creates a model to retrieve {@link EntityCentreConfig} instances for the current user and its base user with a <code>title</code> and <code>rootName</code> specified.
     *
     * @param rootName
     * @param title
     * @return
     */
    private IQueryModel<EntityCentreConfig> modelForCurrentUser(final String rootName, final String title) {
	final IQueryModel<EntityCentreConfig> model1 =
	/*    */select(EntityCentreConfig.class).where().//
	/*    */prop("owner").eq().val(currentUser()).and().// look for entity-centres for only current user
	/*    */prop("title").eq().val(title).and().//
	/*    */prop("menuItem.key").eq().val(rootName).model();
	return model1;
    }

    /**
     * Creates a model to retrieve {@link EntityMasterConfig} instances for the current user with a <code>rootName</code> specified.
     *
     * @param rootName
     * @return
     */
    private IQueryModel<EntityMasterConfig> masterModelForCurrentUser(final String rootName) {
	final IQueryModel<EntityMasterConfig> model =
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
    private IQueryModel<EntityMasterConfig> masterModelForBaseUser(final String rootName) {
	final IQueryModel<EntityMasterConfig> model =
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
    private void error(final String message) {
	logger.error(message);
	throw new IllegalArgumentException(message);
    }

    /**
     * Retrieves and initialises a instance of manager.
     *
     * @param root
     * @param name
     * @param model
     */
    private void retrieveAndInit(final Class<?> root, final String name, final IQueryModel<EntityCentreConfig> model) {
	final EntityCentreConfig ecc = entityCentreConfigController.getEntity(model);
	final boolean owning = ecc.getOwner().equals(currentUser());
	try {
	    init(root, name, getSerialiser().deserialise(ecc.getConfigBody(), ICriteriaDomainTreeManagerAndEnhancer.class), owning);
	    return;
	} catch (final Exception e) {
	    e.printStackTrace();
	    final String message = "Unable to deserialise a entity-centre instance for type [" + root.getSimpleName() + "] with title [" + title(root, name) + "] for current user [" + currentUser() + "].";
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
    private void retrieveAndInitMaster(final Class<?> root, final IQueryModel<EntityMasterConfig> model) {
	final EntityMasterConfig emc = entityMasterConfigController.getEntity(model);
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
    private ICriteriaDomainTreeManagerAndEnhancer initCriteriaManagerCrossReferences(final ICriteriaDomainTreeManagerAndEnhancer mgr) {
	// initialise the references on this instance in its children
	try {
	    final Field dtrField = Finder.findFieldByName(LocatorManager.class, "globalRepresentation");
	    final boolean isAccessible = dtrField.isAccessible();
	    dtrField.setAccessible(true);
	    dtrField.set(((AddToCriteriaTickManager)((AddToCriteriaTickManagerAndEnhancer)mgr.getFirstTick()).base()).locatorManager(), this.getGlobalRepresentation());
	    dtrField.setAccessible(isAccessible);
	} catch (final Exception e) {
	    e.printStackTrace();
	    error(e.getMessage());
	}
	return mgr;
    }

    /**
     * Makes a necessary references on "global stuff" (and perhaps other stuff) inside <code>mgr</code> instance.
     *
     * @param mgr
     * @return
     */
    private IMasterDomainTreeManager initMasterManagerCrossReferences(final IMasterDomainTreeManager mgr) {
	// initialise the references on this instance in its children
	try {
	    final Field dtrField = Finder.findFieldByName(LocatorManager.class, "globalRepresentation");
	    final boolean isAccessible = dtrField.isAccessible();
	    dtrField.setAccessible(true);
	    dtrField.set(((MasterDomainTreeManager)mgr).locatorManager(), this.getGlobalRepresentation());
	    dtrField.setAccessible(isAccessible);
	} catch (final Exception e) {
	    e.printStackTrace();
	    error(e.getMessage());
	}
	return mgr;
    }


    /**
     * Initiates an application instances with a new <code>mgr</code> instance.
     *
     * @param root
     * @param name
     * @param mgr
     * @param owning
     */
    private void init(final Class<?> root, final String name, final ICriteriaDomainTreeManagerAndEnhancer mgr, final boolean owning) {
	final ICriteriaDomainTreeManagerAndEnhancer fullyDefinedMgr = initCriteriaManagerCrossReferences(mgr);
	currentCentres.put(key(root, name), fullyDefinedMgr);
	persistentCentres.put(key(root, name), initCriteriaManagerCrossReferences(EntityUtils.deepCopy(fullyDefinedMgr, getSerialiser())));
	centresOwning.put(key(root, name), owning);
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
	persistentMasters.put(root, initMasterManagerCrossReferences(EntityUtils.deepCopy(fullyDefinedMgr, getSerialiser())));
    }

    @Override
    public void discardEntityCentreManager(final Class<?> root, final String name) {
	notInitiliasedError(persistentCentres.get(key(root, name)), root, name);
	currentCentres.put(key(root, name), initCriteriaManagerCrossReferences(EntityUtils.deepCopy(persistentCentres.get(key(root, name)), getSerialiser())));
    }

    /**
     * Throws an error when the instance is <code>null</code> (not initialised).
     *
     * @param mgr
     * @param root
     * @param name
     */
    private void notInitiliasedError(final ICriteriaDomainTreeManagerAndEnhancer mgr, final Class<?> root, final String name) {
	if (mgr == null) {
	    error("Unable to perform this operation on the entity-centre instance, that wasn't initialised, for type [" + root.getSimpleName() + "] with title [" + title(root, name) + "] for current user [" + currentUser() + "].");
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
    public void saveEntityCentreManager(final Class<?> root, final String name) {
	final ICriteriaDomainTreeManagerAndEnhancer currentMgr = getEntityCentreManager(root, name);
	validateBeforeSaving(currentMgr, root, name);
	prepareInstanceBeforeSave(currentMgr);

	// save an instance of EntityCentreConfig with overridden body, which should exist in DB
	final String title = title(root, name);

	final IQueryModel<EntityCentreConfig> model = modelForCurrentUser(root.getName(), title);
	final int count = entityCentreConfigController.count(model);
	if (count == 1) { // for current user => 1 entity-centre
	    final EntityCentreConfig ecc = entityCentreConfigController.getEntity(model);
	    ecc.setConfigBody(getSerialiser().serialise(currentMgr));
	    entityCentreConfigController.save(ecc);

	    persistentCentres.put(key(root, name), initCriteriaManagerCrossReferences(EntityUtils.deepCopy(currentMgr, getSerialiser())));
	} else if (count < 1) { // there is no entity-centre
	    error("Unable to save a non-existent (non-visible for current user) entity-centre instance for type [" + root.getSimpleName() + "] with title [" + title + "] for current user [" + currentUser() + "].");
	} else { // > 1
	    error("There are more than one entity-centre instance for type [" + root.getSimpleName() + "] with title [" + title + "] for current user [" + currentUser() + "].");
	}
    }

    /**
     * Checks if an instance of manager has been initialised and its inner parts (locators, analyses) have been fully accepted/discarded.
     *
     * @param currentMgr
     * @param root
     * @param name
     */
    private void validateBeforeSaving(final ICriteriaDomainTreeManagerAndEnhancer currentMgr, final Class<?> root, final String name) {
	notInitiliasedError(currentMgr, root, name);
	// let's iterate through all locators and ask if they are accepted
	for (final Pair<Class<?>, String> locatorKey : currentMgr.getFirstTick().locatorKeys()) {
	    if (currentMgr.getFirstTick().isChangedLocatorManager(locatorKey.getKey(), locatorKey.getValue())) {
		error("The inner part of entity centre [locator " + locatorKey + "] is not accepted. It should be accepted or discarded before save operation.");
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
	// let's iterate through all locators and ask if they are accepted
	for (final Pair<Class<?>, String> locatorKey : currentMgr.locatorKeys()) {
	    if (currentMgr.isChangedLocatorManager(locatorKey.getKey(), locatorKey.getValue())) {
		error("The inner part of entity centre [locator " + locatorKey + "] is not accepted. It should be accepted or discarded before save operation.");
	    }
	}
    }

    @Override
    public void saveAsEntityCentreManager(final Class<?> root, final String originalName, final String newName) {
	final ICriteriaDomainTreeManagerAndEnhancer originationMgr = getEntityCentreManager(root, originalName);
	validateBeforeSaving(originationMgr, root, originalName);
	// create a copy of current instance of entity centre
	final ICriteriaDomainTreeManagerAndEnhancer copyMgr = initCriteriaManagerCrossReferences(EntityUtils.deepCopy(originationMgr, getSerialiser()));
	prepareInstanceBeforeSave(copyMgr);

	// save an instance of EntityCentreConfig with overridden body, which should exist in DB
	final String rootName = root.getName();
	final String newTitle = title(root, newName);

	final IQueryModel<EntityCentreConfig> model = modelForCurrentAndBaseUsers(rootName, newTitle);
	final int count = entityCentreConfigController.count(model);
	if (count == 0) { // for current user or its base => there are no entity-centres, so persist a copy with a new title
	    final EntityCentreConfig ecc = factory.newByKey(EntityCentreConfig.class, currentUser(), newTitle, mainMenuItemController.findByKey(rootName));
	    ecc.setConfigBody(getSerialiser().serialise(copyMgr));
	    entityCentreConfigController.save(ecc);
	    init(root, newName, copyMgr, true);
	} else { // > 1
	    error("There are at least one entity-centre instance for type [" + root.getSimpleName() + "] with title [" + newTitle + "] for current user [" + currentUser() + "] or its base [" + baseOfTheCurrentUser() + "].");
	}
    }

    /**
     * Prepares a manager instance to have empty locators when they are fully equal to current default locators. It helps a criteria manager to load "fresh" instances of locator every time.
     *
     * @param copyMgr
     */
    private void prepareInstanceBeforeSave(final ICriteriaDomainTreeManagerAndEnhancer copyMgr) {
	// let's iterate through all locators and make them 'null' in case when they are equal to 'default' produced instances
	for (final Pair<Class<?>, String> locatorKey : copyMgr.getFirstTick().locatorKeys()) {
	    final ILocatorDomainTreeManagerAndEnhancer producedLocatorManager = copyMgr.getFirstTick().produceLocatorManagerByDefault(locatorKey.getKey(), locatorKey.getValue());
	    if (producedLocatorManager.equals(copyMgr.getFirstTick().getLocatorManager(locatorKey.getKey(), locatorKey.getValue()))) {
		copyMgr.getFirstTick().resetLocatorManager(locatorKey.getKey(), locatorKey.getValue());
	    }
	}
    }

    /**
     * Prepares a manager instance to have empty locators when they are fully equal to current default locators. It helps a criteria manager to load "fresh" instances of locator every time.
     *
     * @param copyMgr
     */
    private void masterPrepareInstanceBeforeSave(final IMasterDomainTreeManager copyMgr) {
	// let's iterate through all locators and make them 'null' in case when they are equal to 'default' produced instances
	for (final Pair<Class<?>, String> locatorKey : copyMgr.locatorKeys()) {
	    final ILocatorDomainTreeManagerAndEnhancer producedLocatorManager = copyMgr.produceLocatorManagerByDefault(locatorKey.getKey(), locatorKey.getValue());
	    if (producedLocatorManager.equals(copyMgr.getLocatorManager(locatorKey.getKey(), locatorKey.getValue()))) {
		copyMgr.resetLocatorManager(locatorKey.getKey(), locatorKey.getValue());
	    }
	}
    }

    @Override
    public boolean isChangedEntityCentreManager(final Class<?> root, final String name) {
	notInitiliasedError(persistentCentres.get(key(root, name)), root, name);
	return !EntityUtils.equalsEx(currentCentres.get(key(root, name)), persistentCentres.get(key(root, name)));
    }

    @Override
    public void removeEntityCentreManager(final Class<?> root, final String name) {
	notInitiliasedError(persistentCentres.get(key(root, name)), root, name);
	if (name == null) {
	    error("Unable to remove a principle entity-centre for type [" + root.getSimpleName() + "].");
	} else if (Boolean.FALSE.equals(centresOwning.get(key(root, name)))) { // the report not owns by current user. It can not be removed by current user.
	    error("Unable to remove the entity-centre instance, that current user does not own. The type [" + root.getSimpleName() + "] with title [" + title(root, name) + "] for current user [" + currentUser() + "].");
	} else {
	    currentCentres.remove(key(root, name));
	    persistentCentres.remove(key(root, name));
	    centresOwning.remove(key(root, name));

	    // remove an instance of EntityCentreConfig which should exist in DB
	    entityCentreConfigController.delete(modelForCurrentUser(root.getName(), title(root, name)));
	}
    }

    /////////////////////////////////////////////////////////////////////////////////
    /////////////////////////////////////////////////////////////////////////////////
    /////////////////////////////////////////////////////////////////////////////////

    @Override
    public IMasterDomainTreeManager getEntityMasterManager(final Class<?> root) {
	return currentMasters.get(root);
    }

    @Override
    public void initEntityMasterManager(final Class<?> root) {
	final String rootName = root.getName();
	final IQueryModel<EntityMasterConfig> model = masterModelForCurrentUser(rootName);
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
	final IQueryModel<EntityMasterConfig> model = masterModelForCurrentUser(rootName);
	final int count = entityMasterConfigController.count(model);
	if (count == 1) { // the persistence layer contains own entity-master, so the changes should be rejected
	    currentMasters.put(root, initMasterManagerCrossReferences(EntityUtils.deepCopy(persistentMasters.get(root), getSerialiser())));
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
	masterPrepareInstanceBeforeSave(currentMgr);

	// save an instance of EntityMasterConfig with overridden body
	final IQueryModel<EntityMasterConfig> model = masterModelForCurrentUser(root.getName());
	final int count = entityMasterConfigController.count(model);
	if (count == 1) { // for current user => 1 entity-centre
	    final EntityMasterConfig emc = entityMasterConfigController.getEntity(model);
	    emc.setConfigBody(getSerialiser().serialise(currentMgr));
	    entityMasterConfigController.save(emc);

	    persistentMasters.put(root, initMasterManagerCrossReferences(EntityUtils.deepCopy(currentMgr, getSerialiser())));
	} else if (count < 1) { // there is no entity-centre
	    // save a new instance of EntityMasterConfig
	    final EntityMasterConfig emc = factory.newByKey(EntityMasterConfig.class, currentUser(), root.getName());
	    emc.setConfigBody(getSerialiser().serialise(currentMgr));
	    entityMasterConfigController.save(emc);

	    persistentMasters.put(root, initMasterManagerCrossReferences(EntityUtils.deepCopy(currentMgr, getSerialiser())));
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
	final IQueryModel<EntityMasterConfig> model = masterModelForBaseUser(rootName);
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