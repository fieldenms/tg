package ua.com.fielden.platform.ui.config;

import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.fetchOnly;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.from;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.select;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.log4j.Logger;
import org.joda.time.DateTime;
import org.joda.time.Period;

import ua.com.fielden.platform.dao.CommonEntityDao;
import ua.com.fielden.platform.dao.annotations.SessionRequired;
import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.entity.query.IFilter;
import ua.com.fielden.platform.entity.query.model.EntityResultQueryModel;
import ua.com.fielden.platform.security.user.User;
import ua.com.fielden.platform.swing.review.annotations.EntityType;
import ua.com.fielden.platform.ui.config.api.IEntityCentreConfigController;
import ua.com.fielden.platform.ui.config.api.IMainMenuItemController;
import ua.com.fielden.platform.ui.config.api.IMainMenuItemInvisibilityController;
import ua.com.fielden.platform.ui.config.controller.mixin.MainMenuStructureFactory;
import ua.com.fielden.platform.utils.Pair;

import com.google.inject.Inject;

/**
 * DAO implementation for companion object {@link IMainMenu}.
 *
 * @author Developers
 *
 */
@EntityType(MainMenu.class)
public class MainMenuDao extends CommonEntityDao<MainMenu> implements IMainMenu {
    private final IMainMenuItemController mmiController;
    private final IEntityCentreConfigController eccController;
    private final IEntityCentreAnalysisConfig ecacController;
    private final IMainMenuItemInvisibilityController mmiiController;
    private final EntityFactory factory;
    private final Logger logger = Logger.getLogger(getClass());

    @Inject
    public MainMenuDao(final IFilter filter, final IMainMenuItemController mmiController, final IEntityCentreConfigController eccController, final IEntityCentreAnalysisConfig ecacController, final IMainMenuItemInvisibilityController mmiiController, final EntityFactory factory) {
        super(filter);
	this.mmiController = mmiController;
	this.eccController = eccController;
	this.ecacController = ecacController;
	this.mmiiController = mmiiController;
	this.factory = factory;
    }

    @Override
    @SessionRequired
    public MainMenu save(final MainMenu entity) {
	Pair<String, DateTime> newMessageAndNewSt = new Pair<String, DateTime>(null, new DateTime());

	newMessageAndNewSt = info(newMessageAndNewSt, "BUILD DEVELOPMENT ITEMS");
	final List<MainMenuItem> developmentMainMenuItems = new ArrayList<MainMenuItem>(new MainMenuStructureFactory(factory).pushAll(entity.getMenuItems()).build());
	final List<MainMenuItem> updatedMainMenuItems = new ArrayList<MainMenuItem>();

	newMessageAndNewSt = info(newMessageAndNewSt, "RETRIEVE all ECC, ECAC");
	final EntityResultQueryModel<EntityCentreAnalysisConfig> modelEcac = select(EntityCentreAnalysisConfig.class).model();
	final EntityResultQueryModel<EntityCentreConfig> modelEcc = select(EntityCentreConfig.class)./*where().prop("owner.key").eq().val(user.getKey()).*/model();
	// retrieve all EntityCentreConfig's, locally keep meta-info, and then purge them all
	final Map<EntityCentreConfigKey, EntityCentreConfigBody> centresKeysAndBodies = retrieveCentresKeysAndBodies(modelEcac, modelEcc);

	newMessageAndNewSt = info(newMessageAndNewSt, "PURGE all ECC, ECAC");
	purgeCentres(modelEcac, modelEcc);

	newMessageAndNewSt = info(newMessageAndNewSt, "RETRIEVE all MMII");
	// retrieve all MainMenuItemInvisibility's, locally keep meta-info, and then purge them all
	final EntityResultQueryModel<MainMenuItemInvisibility> modelMmii = select(MainMenuItemInvisibility.class)./*where().prop("owner.key").eq().val(user.getKey()).*/model();
	final List<MainMenuItemInvisibility> mmiis = mmiiController.getAllEntities(from(modelMmii).model());
	final Set<MainMenuItemInvisibilityKey> invisibilitiesKeys = new HashSet<MainMenuItemInvisibilityKey>();
	for (final MainMenuItemInvisibility mmii : mmiis) {
	    invisibilitiesKeys.add(new MainMenuItemInvisibilityKey(mmii.getOwner(), mmii.getMenuItem().getKey()));
	}

	newMessageAndNewSt = info(newMessageAndNewSt, "PURGE all MMII");
	mmiiController.delete(modelMmii);

	newMessageAndNewSt = info(newMessageAndNewSt, "PURGE all MMI");
	purgeAllMMI();

	newMessageAndNewSt = info(newMessageAndNewSt, "SAVE all MMI");
	// persist new menu items
	for (final MainMenuItem rootDevelopmentMainMenuItem : developmentMainMenuItems) {
	    updatedMainMenuItems.add(saveMenuItem(rootDevelopmentMainMenuItem));
	}

	newMessageAndNewSt = info(newMessageAndNewSt, "SAVE all ECC, ECAC");
	// persist old EntityCentreConfig's
	for (final Entry<EntityCentreConfigKey, EntityCentreConfigBody> centresKeyAndBody : centresKeysAndBodies.entrySet()) {
	    final MainMenuItem mmi = mmiController.findByKey(centresKeyAndBody.getKey().getMainMenuItemKey());
	    if (mmi != null) {
		final EntityCentreConfig ecc = factory.newByKey(EntityCentreConfig.class, centresKeyAndBody.getKey().getOwner(), centresKeyAndBody.getKey().getTitle(), mmi);
		ecc.setPrincipal(centresKeyAndBody.getValue().isPrincipal());
		ecc.setConfigBody(centresKeyAndBody.getValue().getConfigBody());
		final EntityCentreConfig newECC = eccController.save(ecc);
		for (final String analysisName : centresKeyAndBody.getKey().getAnalysesNames()) {
		    final EntityCentreAnalysisConfig ecac = factory.newByKey(EntityCentreAnalysisConfig.class, newECC, analysisName);
		    ecacController.save(ecac);
		}
	    } else {
		logger.warn("The Entity Centre Config for owner [" + centresKeyAndBody.getKey().getOwner() + "] and title " + centresKeyAndBody.getKey().getTitle() + " and item [" + centresKeyAndBody.getKey().getMainMenuItemKey() + "] has been purged due to non-existence of item [" + centresKeyAndBody.getKey().getMainMenuItemKey() + "] after update procedure.");
	    }
	}

	newMessageAndNewSt = info(newMessageAndNewSt, "SAVE all MMII");
	// persist old MainMenuItemInvisibility's
	for (final MainMenuItemInvisibilityKey invisibilityKey : invisibilitiesKeys) {
	    final MainMenuItem mmi = mmiController.findByKey(invisibilityKey.getMainMenuItemKey());
	    if (mmi != null) {
		final MainMenuItemInvisibility mmii = factory.newByKey(MainMenuItemInvisibility.class, invisibilityKey.getOwner(), mmi);
		mmiiController.save(mmii);
	    } else {
		logger.warn("The Main Menu Item Invisibility for owner [" + invisibilityKey.getOwner() + "] and item [" + invisibilityKey.getMainMenuItemKey() + "] has been purged due to non-existence of item [" + invisibilityKey.getMainMenuItemKey() + "] after update procedure.");
	    }
	}

	newMessageAndNewSt = info(newMessageAndNewSt, "DONE");
        return entity;
    }

    private String str(final char c, final int n) {
	return str0(c, n, "");
    }
    private String str0(final char c, final int n, final String accu) {
	if (n == 0) {
	    return accu;
	} else {
	    return str0(c, n - 1, accu + c);
	}
    }
    private String wrap(final String s, final int width) {
	final int partCount = (width - s.length()) / 2;
	final String part = str('=', partCount);
	final String all = part + s + part;
	return all.length() == width ? all : all + '=';
    }

    private Pair<String, DateTime> info(final Pair<String, DateTime> oldMessageAndOldSt, final String newMessage) {
	final Period pd = new Period(oldMessageAndOldSt.getValue(), new DateTime());
	final int width = 120;
	final String row = str('=', width);
	System.err.println(str('-', width));
	System.err.println(row);
	System.err.println(oldMessageAndOldSt.getKey() == null ? row : wrap(oldMessageAndOldSt.getKey() + "...done in " + pd.getSeconds() + " s " + pd.getMillis() + " ms", width));
	System.err.println(row);
	System.err.println(wrap(newMessage, width));
	System.err.println(row);
	System.err.println(row);
	System.err.println(row);
	System.err.println(str('-', width));
	return new Pair<String, DateTime>(newMessage, new DateTime()); // return newMessageAndNewSt
    }

    private static final class EntityCentreConfigKey {
	private final User owner;
	private final String title;
	private final String mainMenuItemKey;
	private final List<String> analysesNames;

	protected EntityCentreConfigKey(final User owner, final String title, final String mainMenuItemKey, final List<String> analysesNames) {
	    this.owner = owner;
	    this.title = title;
	    this.mainMenuItemKey = mainMenuItemKey;
	    this.analysesNames = new ArrayList<String>();
	    this.analysesNames.addAll(analysesNames);
	}

	public User getOwner() {
	    return owner;
	}

	public String getTitle() {
	    return title;
	}

	public String getMainMenuItemKey() {
	    return mainMenuItemKey;
	}

	public List<String> getAnalysesNames() {
	    return analysesNames;
	}
    }

    private static final class EntityCentreConfigBody {
	private final boolean principal;
	private final byte[] configBody;

	protected EntityCentreConfigBody(final boolean principal, final byte[] configBody) {
	    this.principal = principal;
	    this.configBody = configBody;
	}

	public boolean isPrincipal() {
	    return principal;
	}

	public byte[] getConfigBody() {
	    return configBody;
	}
    }

    private static final class MainMenuItemInvisibilityKey {
	private final User owner;
	private final String mainMenuItemKey;

	protected MainMenuItemInvisibilityKey(final User owner, final String mainMenuItemKey) {
	    this.owner = owner;
	    this.mainMenuItemKey = mainMenuItemKey;
	}

	public User getOwner() {
	    return owner;
	}

	public String getMainMenuItemKey() {
	    return mainMenuItemKey;
	}
    }

    /**
     * Retrieves all centres and its analyses.
     *
     * @param modelEcac
     * @param modelEcc
     * @return
     */
    private Map<EntityCentreConfigKey, EntityCentreConfigBody> retrieveCentresKeysAndBodies(final EntityResultQueryModel<EntityCentreAnalysisConfig> modelEcac, final EntityResultQueryModel<EntityCentreConfig> modelEcc) {
	final Map<Long, List<String>> analysesMap = new LinkedHashMap<Long, List<String>>();
	final List<EntityCentreAnalysisConfig> ecacs = ecacController.getAllEntities(from(modelEcac).with(fetchOnly(EntityCentreAnalysisConfig.class).with("entityCentreConfig", fetchOnly(EntityCentreConfig.class).with("id")).with("title")).model());
	for (final EntityCentreAnalysisConfig ecac : ecacs) {
	    if (!analysesMap.containsKey(ecac.getEntityCentreConfig().getId())) {
		analysesMap.put(ecac.getEntityCentreConfig().getId(), new ArrayList<String>());
	    }
	    analysesMap.get(ecac.getEntityCentreConfig().getId()).add(ecac.getTitle());
	}

	final List<EntityCentreConfig> eccs = eccController.getAllEntities(from(modelEcc).model());
	final Map<EntityCentreConfigKey, EntityCentreConfigBody> centresKeysAndBodies = new LinkedHashMap<EntityCentreConfigKey, EntityCentreConfigBody>();
	for (final EntityCentreConfig ecc : eccs) {
	    final List<String> analyseNames = analysesMap.get(ecc.getId()) == null ? new ArrayList<String>() : analysesMap.get(ecc.getId());
	    centresKeysAndBodies.put(new EntityCentreConfigKey(ecc.getOwner(), ecc.getTitle(), ecc.getMenuItem().getKey(), analyseNames), new EntityCentreConfigBody(ecc.isPrincipal(), ecc.getConfigBody()));
	}
	return centresKeysAndBodies;
    }

    /**
     * Saves hierarchically menu item and its children.
     *
     * @param developmentMainMenuItem
     * @return
     */
    private MainMenuItem saveMenuItem(final MainMenuItem developmentMainMenuItem) {
	// System.err.println("Added menu item [" + developmentMainMenuItem + "].");
	final MainMenuItem itemToSave = factory.newByKey(MainMenuItem.class, developmentMainMenuItem.getKey());
	itemToSave.setDesc(developmentMainMenuItem.getDesc());
	itemToSave.setOrder(developmentMainMenuItem.getOrder());
	itemToSave.setTitle(developmentMainMenuItem.getTitle());
	itemToSave.setParent(developmentMainMenuItem.getParent() == null ? null : mmiController.findByKey(developmentMainMenuItem.getParent().getKey())); // should be updated instance!
	final MainMenuItem savedMainMenuItem = mmiController.save(itemToSave);

	// iterate through children hierarchy
	for (final MainMenuItem child : developmentMainMenuItem.getChildren()) {
	    savedMainMenuItem.addChild(saveMenuItem(child));
	}
	return savedMainMenuItem;
    }

    private void purgeAllMMI() {
	mmiController.delete(select(MainMenuItem.class).model());
    }

//    private void purgeAllMMI(final List<MainMenuItem> mmis) {
//	for (final MainMenuItem rootItem : mmis) {
//	    purgeAll(rootItem);
//	}
//    }
//    private void purgeAll(final MainMenuItem mmi) {
//	for (final MainMenuItem child : mmi.getChildren()) {
//	    if (child.isPersisted()) {
//		purgeAll(child);
//	    }
//	}
//	mmiController.delete(mmi);
//    }

    /**
     * Purges all centres and its analyses.
     *
     * @param modelEcac
     * @param modelEcc
     */
    public void purgeCentres(final EntityResultQueryModel<EntityCentreAnalysisConfig> modelEcac, final EntityResultQueryModel<EntityCentreConfig> modelEcc) {
	ecacController.delete(modelEcac);
	eccController.delete(modelEcc);
    }
}