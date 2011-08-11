package ua.com.fielden.platform.swing.review;

import java.awt.Container;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.HashMap;

import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.JOptionPane;

import org.jvnet.flamingo.common.icon.EmptyResizableIcon;

import ua.com.fielden.actionpanelmodel.ActionPanelBuilder;
import ua.com.fielden.platform.dao.IDaoFactory;
import ua.com.fielden.platform.dao.IEntityAggregatesDao;
import ua.com.fielden.platform.dao.IEntityDao;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.entity.matcher.IValueMatcherFactory;
import ua.com.fielden.platform.error.Result;
import ua.com.fielden.platform.swing.actions.ActionChanger;
import ua.com.fielden.platform.swing.actions.Command;
import ua.com.fielden.platform.swing.analysis.IAnalysisReportPersistentObject;
import ua.com.fielden.platform.swing.dialogs.DialogWithDetails;
import ua.com.fielden.platform.swing.dynamicreportstree.AbstractTree;
import ua.com.fielden.platform.swing.review.factory.CriteriaWizardModelFactory;
import ua.com.fielden.platform.swing.review.factory.IEntityReviewFactory;
import ua.com.fielden.platform.swing.review.factory.IWizardModelFactory;
import ua.com.fielden.platform.swing.review.optionbuilder.ActionChangerBuilder;
import ua.com.fielden.platform.swing.review.persistens.DynamicCriteriaPersistentObject;
import ua.com.fielden.platform.swing.review.persistens.DynamicCriteriaPersistentObject.InvalidReportsFileVersionException;
import ua.com.fielden.platform.swing.review.wizard.AbstractWizard;
import ua.com.fielden.platform.swing.review.wizard.AbstractWizardModel;
import ua.com.fielden.platform.swing.view.BasePanel;
import ua.com.fielden.platform.ui.config.api.interaction.IConfigurationController;

public class DynamicCriteriaModelBuilder<T extends AbstractEntity, DAO extends IEntityDao<T>, R extends AbstractEntity> {

    //Manages configuration loading and saving process.
    private CriteriaManager reader;
    //Manages save as facility.
    private ISaveAsContract saveAsContract;
    //Key that identifies configuration.
    private final String key;
    //controller that manages saving and loading DynamicCriteriaPersistentObjectUi instances.
    private final IConfigurationController configurationController;

    //Resultant entity class.
    private final Class<R> resultantEntityClass;

    // needed for the configure and cancel buttons
    private DynamicEntityReview<T, DAO, R> previousEntityReview;

    // IEntityReviewfactory that allows one to create specific entity centers and their models.
    private final IEntityReviewFactory<T, DAO, R> entityReviewFactory;
    // Allows one to create special center's wizard models.
    private final IWizardModelFactory<T, DAO, R> wizardModelFactory;

    //Provides initial ordering.
    private final IOrderSetter orderSetter;

    /**
     * Tracks the state of the model builder.
     */
    private DynamicCriteriaModelBuilderState builderState;
    /**
     * Holds the reference on the current visible panel.
     */
    private JComponent currentBuilderPanel;

    /**
     * A convenient constructor, which utilises the default property model.
     * 
     * @param entityFactory
     *            TODO
     * @param vmf
     * @param dao
     * @param entityAggregatesDao
     *            TODO
     * @param resultantEntityClass
     * @param associatedFileName
     * @param classPath
     * @param packageName
     */
    public DynamicCriteriaModelBuilder(final EntityFactory entityFactory,//
	    final IValueMatcherFactory vmf,//
	    final IDaoFactory daoFactory, //
	    final DAO dao,//
	    final IEntityAggregatesDao entityAggregatesDao, //
	    final Class<R> resultantEntityClass, //
	    final String key,//
	    final IConfigurationController configurationController,//
	    final IOrderSetter orderSetter, final IEntityReviewFactory<T, DAO, R> entityReviewModelFactory) {
	this(entityFactory, vmf, daoFactory, dao, entityAggregatesDao, resultantEntityClass, key, configurationController, orderSetter, entityReviewModelFactory, new CriteriaWizardModelFactory<T, DAO, R>());
    }

    /**
     * The principle constructor.
     * 
     * @param entityFactory
     *            TODO
     * @param vmf
     * @param dao
     * @param entityAggregatesDao
     *            TODO
     * @param resultantEntityClass
     * @param propertyFilter
     * @param associatedFileName
     * @param classPath
     * @param packageName
     */
    public DynamicCriteriaModelBuilder(final EntityFactory entityFactory,//
	    final IValueMatcherFactory vmf,//
	    final IDaoFactory daoFactory, //
	    final DAO dao,//
	    final IEntityAggregatesDao entityAggregatesDao,//
	    final Class<R> resultantEntityClass, //
	    final String key,//
	    final IConfigurationController configurationController,//
	    final IOrderSetter orderSetter,//
	    final IEntityReviewFactory<T, DAO, R> entityReviewFactory, final IWizardModelFactory<T, DAO, R> wizardModelFactory) {
	this.resultantEntityClass = resultantEntityClass;
	this.key = key;
	this.configurationController = configurationController;
	this.entityReviewFactory = entityReviewFactory;
	this.wizardModelFactory = wizardModelFactory;
	this.orderSetter = orderSetter;
	reader = new CriteriaManager(entityFactory, vmf, daoFactory, dao, entityAggregatesDao, resultantEntityClass);

    }

    public void init(final Container owner, final ActionChangerBuilder actionChangerBuilder, final ActionPanelBuilder panelBuilder, final boolean showRecords, final boolean isPrinciple) {
	reload();
	owner.removeAll();
	currentBuilderPanel = getWizardPanel(owner, actionChangerBuilder, panelBuilder, showRecords, isPrinciple);
	owner.add(currentBuilderPanel);
	owner.invalidate();
	owner.validate();
	owner.repaint();
    }

    /**
     * Reloads file configuration.
     */
    public void reload() {
	reader = new CriteriaManager(reader.entityFactory,//
		reader.vmf,//
		reader.daoFactory,//
		reader.dao, //
		reader.entityAggregatesDao, reader.resultantEntityClass);
    }

    public Class<R> getResultantEntityClass() {
	return resultantEntityClass;
    }

    /**
     * Enables saving criteria in to another places specified with filename.
     * 
     * @param saveAsContract
     */
    public void setSaveAsContract(final ISaveAsContract saveAsContract) {
	this.saveAsContract = saveAsContract;
    }

    public IOrderSetter getOrderSetter() {
	return orderSetter;
    }

    protected DynamicEntityQueryCriteria<T, DAO> getCustomDynamicCriteria(final EntityFactory entityFactory, final DAO dao, final IEntityAggregatesDao entityAggregatesDao, final IDaoFactory daoFactory, final IValueMatcherFactory vmf, final DynamicCriteriaPersistentObject persistentObject) {
	return new DynamicEntityQueryCriteria<T, DAO>(entityFactory, daoFactory, dao, entityAggregatesDao, vmf, persistentObject);
    }

    public String getKey() {
	return key;
    }

    public IConfigurationController getConfigurationController() {
	return configurationController;
    }

    public boolean isConfigurationExists() {
	return getConfigurationController().exists(getKey());
    }

    private BasePanel getWizardPanel(//
	    /*	    */final Container owner,//
	    /*	    */final ActionChangerBuilder actionChangerBuilder,//
	    /*	    */final ActionPanelBuilder panelBuilder, //
	    /*	    */final boolean showRecords,//
	    /*	    */final boolean isPrinciple) {
	actionChangerBuilder.setAction(createConfigureAction(owner, actionChangerBuilder, panelBuilder, showRecords, isPrinciple));
	if (isConfigurationExists() && reader.getWizardModel().getDynamicCriteria().getRestoreError() == null) {
	    if (canBuildReview()) {
		previousEntityReview = reader.getWizardModel().getEntityReview(actionChangerBuilder, panelBuilder, showRecords, isPrinciple);
		builderState = DynamicCriteriaModelBuilderState.REVIEW;
		return previousEntityReview;
	    } else {
		JOptionPane.showMessageDialog(owner, "Please choose properties for criteria and result", "Information", JOptionPane.WARNING_MESSAGE);
	    }
	}
	if (reader.getWizardModel().getDynamicCriteria().getRestoreError() != null) {
	    JOptionPane.showMessageDialog(owner, reader.getWizardModel().getDynamicCriteria().getRestoreError().getMessage(), "Information", JOptionPane.WARNING_MESSAGE);
	}
	builderState = DynamicCriteriaModelBuilderState.WIZARD;
	return wizardModelFactory.createWizardPanel(reader.getWizardModel(), createBuildAction(owner, actionChangerBuilder, panelBuilder, showRecords, isPrinciple), createCancelAction(owner));
    }

    private Action createBuildAction(final Container owner, final ActionChangerBuilder actionChangerBuilder, final ActionPanelBuilder panelBuilder, final boolean showRecords, final boolean isPrinciple) {
	return new Command<Void>("Build Criteria") {

	    private static final long serialVersionUID = 2213924783884905070L;

	    {
		putValue(Action.SHORT_DESCRIPTION, "Builds dynamic criteria");
		putValue(Action.MNEMONIC_KEY, KeyEvent.VK_B);
	    }

	    @Override
	    protected boolean preAction() {
		if (!canBuildReview()) {
		    JOptionPane.showMessageDialog(owner, "Please choose properties for criteria and result", "Information", JOptionPane.WARNING_MESSAGE);
		    return false;
		}
		return super.preAction();
	    }

	    @Override
	    protected Void action(final ActionEvent e) throws Exception {
		return null;
	    }

	    @Override
	    protected void postAction(final Void value) {
		super.postAction(value);

		previousEntityReview = reader.getWizardModel().getEntityReview(actionChangerBuilder, panelBuilder, showRecords, isPrinciple);
		currentBuilderPanel = previousEntityReview;

		owner.removeAll();
		owner.add(previousEntityReview);
		owner.invalidate();
		owner.validate();
		owner.repaint();

		builderState = DynamicCriteriaModelBuilderState.REVIEW;
	    }

	};
    }

    /**
     * Saves the criteria using {@link ISaveAsContract} contract.
     * 
     * @param isClosing
     * @return
     */
    public boolean saveAsCriteria(final boolean isClosing) {
	if (saveAsContract == null) {
	    throw new UnsupportedOperationException("In order to use configurable saveAsCriteria routine, set the appropriate ISaveAsContract");
	}
	if (!saveAsContract.beforeSave()) {
	    return false;
	}
	if (previousEntityReview != null) {
	    previousEntityReview.saveValues();
	}
	try {
	    reader.saveAsCriteria(saveAsContract.getKeyToSave());
	    saveAsContract.afterSave(isClosing);
	} catch (final Exception ex) {
	    new DialogWithDetails(null, "Exception while saving", ex).setVisible(true);
	}
	return true;
    }

    public void remove() {
	reader.removeConfiguration();
    }

    /**
     * Saves the current view in to the file.
     * 
     * @param isClosing
     */
    public void saveCriteria(final boolean isClosing) {
	final Result configurationresult = getConfigurationController().canSave(getKey());
	if (!configurationresult.isSuccessful()) {
	    JOptionPane.showMessageDialog(null, configurationresult.getMessage(), "Information", JOptionPane.INFORMATION_MESSAGE);
	    return;
	}
	if (previousEntityReview != null) {
	    previousEntityReview.saveValues();
	    final DynamicCriteriaPersistentObjectUi previousPersistentObject = reader.getPreviousPersistentObject();
	    if (previousEntityReview.isPrinciple() && analysesChanged(previousPersistentObject) && saveAsContract != null) {
		final int chosenOption = JOptionPane.showConfirmDialog(previousEntityReview, "Analysis can not be saved. Would you like to save it as not principle report?", "Save report", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
		if (chosenOption == JOptionPane.YES_OPTION) {
		    saveAsCriteria(isClosing);
		}
		if (previousEntityReview.isPrinciple()) {
		    getWizardModel().setAnalysis(new HashMap<String, IAnalysisReportPersistentObject>());
		}
	    }
	}
	reader.saveCriteria();
    }

    private boolean analysesChanged(final DynamicCriteriaPersistentObjectUi previousPersistentObject) {
	return previousPersistentObject == null ? false : !previousPersistentObject.isAnalysisEqual(getWizardModel().getAnalysis());
    }

    private ActionChanger<?> createConfigureAction(final Container owner, final ActionChangerBuilder actionChangerBuilder,//
	    final ActionPanelBuilder panelBuilder, final boolean showRecords, final boolean isPrinciple) {
	return new ActionChanger<Void>("Configure") {

	    private static final long serialVersionUID = 5821607026256750433L;

	    {
		putValue(Action.SHORT_DESCRIPTION, "Configure criteria");
		putValue(LARGE_ICON_KEY, new EmptyResizableIcon(new Dimension(0, 0)));
	    }

	    @Override
	    protected boolean preAction() {
		final boolean result = super.preAction();
		if (!result) {
		    return false;
		}
		final Result configurationresult = getConfigurationController().canConfigure(getKey());
		if (!configurationresult.isSuccessful()) {
		    JOptionPane.showMessageDialog(owner, configurationresult.getMessage(), "Information", JOptionPane.INFORMATION_MESSAGE);
		    return false;
		}
		return true;
	    }

	    @Override
	    protected Void action(final ActionEvent e) throws Exception {
		return null;
	    }

	    @Override
	    protected void postAction(final Void value) {
		super.postAction(value);

		previousEntityReview.saveValues();
		reader.saveWizardModel();

		final AbstractWizard<? extends AbstractWizardModel, ? extends AbstractTree> wizardPanel = wizardModelFactory.createWizardPanel(reader.getWizardModel(), createBuildAction(owner, actionChangerBuilder, panelBuilder, showRecords, isPrinciple), createCancelAction(owner));

		owner.removeAll();
		owner.add(wizardPanel);
		owner.invalidate();
		owner.validate();
		owner.repaint();

		currentBuilderPanel = wizardPanel;
		builderState = DynamicCriteriaModelBuilderState.WIZARD;
	    }

	};
    }

    public ActionChanger<Void> createSaveAction() {
	return new ActionChanger<Void>("Save") {

	    private static final long serialVersionUID = -4300115473173789981L;

	    {
		putValue(SHORT_DESCRIPTION, "Saves the report");
		putValue(LARGE_ICON_KEY, new EmptyResizableIcon(new Dimension(0, 0)));
	    }

	    @Override
	    protected Void action(final ActionEvent e) throws Exception {
		return null;
	    }

	    @Override
	    protected void postAction(final Void value) {
		super.postAction(value);
		saveCriteria(false);
	    }

	};
    }

    public Action createCancelAction(final Container owner) {
	return new Command<Void>("Cancel") {

	    private static final long serialVersionUID = -8758921327059495895L;

	    {
		putValue(Action.SHORT_DESCRIPTION, "Cancel criteria modifactions");
		putValue(Action.MNEMONIC_KEY, KeyEvent.VK_C);
		setEnabled(previousEntityReview != null);
	    }

	    @Override
	    protected Void action(final ActionEvent e) throws Exception {
		return null;
	    }

	    @Override
	    protected void postAction(final Void value) {
		super.postAction(value);
		reader.restorePreviousWizardModel();
		owner.removeAll();
		owner.add(previousEntityReview);
		owner.invalidate();
		owner.validate();
		owner.repaint();

		currentBuilderPanel = previousEntityReview;
		builderState = DynamicCriteriaModelBuilderState.REVIEW;
	    }

	};
    }

    public AbstractWizardModel<T, DAO, R> getWizardModel() {
	return reader.getWizardModel();
    }

    /**
     * Returns value that indicates whether current configuration of the criteria has changed from the previous one, or not.
     * 
     * @return
     */
    public boolean isCriteriaModelChanged() {
	if (builderState == DynamicCriteriaModelBuilderState.REVIEW) {
	    previousEntityReview.saveValues();
	}
	return reader.getWizardModel().createPersistentObject().isChanged(reader.getPreviousPersistentObject());
    }

    public boolean canBuildReview() {
	return reader.getWizardModel().getSelectedTableHeaders().size() != 0;
    }

    public CloseReportOptions canClose(final String caption) {

	final boolean isReview = builderState == DynamicCriteriaModelBuilderState.REVIEW;

	final Result res = getConfigurationController().canSave(getKey());
	if (!res.isSuccessful()) {
	    return CloseReportOptions.EQUAL;
	}

	if (!isCriteriaModelChanged()) {
	    savePreviousPersistentObject();
	    return CloseReportOptions.EQUAL;
	}
	final int chosenOption = JOptionPane.showConfirmDialog(currentBuilderPanel, "Would you like to save changes"
		+ (caption != null && !"".equals(caption) ? " for the " + caption : "") + " before closing?", "Save report", JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);
	if (chosenOption == JOptionPane.YES_OPTION) {
	    if (!isReview && canBuildReview()) {
		final boolean isPrinciple = previousEntityReview != null ? previousEntityReview.isPrinciple() : true;
		previousEntityReview = getWizardModel().getEntityReview(previousEntityReview.getEntityReviewModel().getActionChangerBuilder(), previousEntityReview.getEntityReviewModel().getActionPanelBuilder(), false, isPrinciple);
	    }
	    try {
		saveCriteria(true);
	    } catch (final Exception ex) {
		new DialogWithDetails(null, "Exception while saving", ex).setVisible(true);
	    }
	    return CloseReportOptions.YES;
	} else if (chosenOption == JOptionPane.NO_OPTION) {
	    savePreviousPersistentObject();
	    return CloseReportOptions.NO;
	} else {
	    return CloseReportOptions.CANCEL;
	}
    }

    private void savePreviousPersistentObject() {
	try {
	    if (isConfigurationExists()) {
		reader.savePreviousPersistentObject();
	    }
	} catch (final Exception ex) {
	    new DialogWithDetails(null, "Exception while saving", ex).setVisible(true);
	}
    }

    /**
     * Returns {@link DynamicEntityReview} that was previously build.
     * 
     * @return
     */
    public DynamicEntityReview<T, DAO, R> getPreviousEntityReview() {
	return previousEntityReview;
    }

    /**
     * Returns explanation, why does ad hoc report can not be close
     * 
     * @return
     */
    public String whyCannotClose() {
	return "Please save or cancel all changes to the ad hoc report";
    }

    private class CriteriaManager {

	private AbstractWizardModel<T, DAO, R> wizardModel;

	private byte[] previousPersistentObject;
	// needed only for configure and cancel actions of the wizard.
	private AbstractWizardModel<T, DAO, R> previousWizardModel;
	private final IValueMatcherFactory vmf;
	private final IDaoFactory daoFactory;
	private final EntityFactory entityFactory;
	private final DAO dao;
	private final IEntityAggregatesDao entityAggregatesDao;
	private final Class<R> resultantEntityClass;

	public CriteriaManager(final EntityFactory entityFactory, final IValueMatcherFactory vmf, final IDaoFactory daoFactory, final DAO dao, final IEntityAggregatesDao entityAggregatesDao, final Class<R> resultantEntityClass) {
	    this.entityFactory = entityFactory;
	    this.vmf = vmf;
	    this.daoFactory = daoFactory;
	    this.dao = dao;
	    this.entityAggregatesDao = entityAggregatesDao;
	    this.resultantEntityClass = resultantEntityClass;
	}

	private AbstractWizardModel<T, DAO, R> createWiz(final IValueMatcherFactory vmf, final IDaoFactory daoFactory, final DAO dao, final IEntityAggregatesDao entityAggregatesDao, final Class<R> resultantEntityClass) {
	    final AbstractWizardModel<T, DAO, R> wizardModel = createWizardModel(vmf, daoFactory, dao, entityAggregatesDao, resultantEntityClass);
	    if (previousPersistentObject == null) {
		final DynamicCriteriaPersistentObjectUi persistentObject = wizardModel.createPersistentObject();
		previousPersistentObject = getConfigurationController().getSerialiser().serialise(persistentObject);
	    }
	    return wizardModel;
	}

	public final AbstractWizardModel<T, DAO, R> getWizardModel() {
	    if (wizardModel == null) {
		wizardModel = createWiz(vmf, daoFactory, dao, entityAggregatesDao, resultantEntityClass);
	    }
	    return wizardModel;
	}

	private void savePreviousPersistentObject() {
	    final DynamicCriteriaPersistentObjectUi persistentObject = getPreviousPersistentObject();
	    persistentObject.updatePersistentObjectFrom(wizardModel.createPersistentObject());
	    wizardModel.setAnalysis(new HashMap<String, IAnalysisReportPersistentObject>(persistentObject.getAnalysis()));
	    final byte[] newCriteriaToSave = getConfigurationController().getSerialiser().serialise(persistentObject);
	    getConfigurationController().save(getKey(), newCriteriaToSave);
	}

	private DynamicEntityQueryCriteria<T, DAO> createDynamicCriteria(//
		final IValueMatcherFactory vmf,//
		final IDaoFactory daoFactory,//
		final DAO dao, //
		final IEntityAggregatesDao entityAggregatesDao,//
		final DynamicCriteriaPersistentObject persistentObject) {
	    final DynamicEntityQueryCriteria<T, DAO> dynamicCriteria = getCustomDynamicCriteria(entityFactory, dao, entityAggregatesDao, daoFactory, vmf, persistentObject);
	    return dynamicCriteria;
	}

	private AbstractWizardModel<T, DAO, R> createWizardModel(final IValueMatcherFactory vmf, final IDaoFactory daoFactory, final DAO dao, final IEntityAggregatesDao entityAggregatesDao, final Class<R> resultantEntityClass) {
	    if (!isConfigurationExists()) {
		final DynamicEntityQueryCriteria<T, DAO> dynamicCriteria = createDynamicCriteria(vmf, daoFactory, dao, entityAggregatesDao, null);
		return wizardModelFactory.createWizardModel(dynamicCriteria, resultantEntityClass, null, DynamicCriteriaModelBuilder.this);
	    }
	    try {
		previousPersistentObject = getConfigurationController().load(getKey());
		final DynamicCriteriaPersistentObjectUi persistentObject = getConfigurationController().getSerialiser().deserialise(previousPersistentObject, DynamicCriteriaPersistentObjectUi.class);

		if (!persistentObject.hasValidVersion()) {
		    throw new InvalidReportsFileVersionException(getKey());
		}

		final DynamicEntityQueryCriteria<T, DAO> dynamicCriteria = getCustomDynamicCriteria(entityFactory, dao, entityAggregatesDao, daoFactory, vmf, persistentObject);

		return wizardModelFactory.createWizardModel(dynamicCriteria, resultantEntityClass, persistentObject, DynamicCriteriaModelBuilder.this);
	    } catch (final InvalidReportsFileVersionException e) {
		System.err.println("Reports file '" + e.getFileName() + "' has old version and thus be discarded.");
	    } catch (final Exception e) {
		e.printStackTrace();
	    }
	    final DynamicEntityQueryCriteria<T, DAO> dynamicCriteria = createDynamicCriteria(vmf, daoFactory, dao, entityAggregatesDao, null);
	    return wizardModelFactory.createWizardModel(dynamicCriteria, resultantEntityClass, null, DynamicCriteriaModelBuilder.this);
	}

	public void saveCriteria() {
	    saveCriteria(getKey(), false);
	}

	public void saveAsCriteria(final String key) {
	    saveCriteria(key, true);
	}

	public void saveCriteria(final String key, final boolean saveNew) {
	    //final String fileToSave = key.endsWith(".dcf") ? key : key + ".dcf";
	    //createDirsIfNeeded(fileToSave);
	    final DynamicCriteriaPersistentObjectUi persistentObject = wizardModel.createPersistentObject();
	    final byte[] newCriteriaToSave = getConfigurationController().getSerialiser().serialise(persistentObject);
	    if (!saveNew) {
		previousPersistentObject = newCriteriaToSave;
	    }
	    getConfigurationController().save(key, newCriteriaToSave);
	}

	public void removeConfiguration() {
	    getConfigurationController().removeConfiguration(getKey());
	}

	public DynamicCriteriaPersistentObjectUi getPreviousPersistentObject() {
	    try {
		final DynamicCriteriaPersistentObjectUi persistentObject = getConfigurationController().getSerialiser().deserialise(previousPersistentObject, DynamicCriteriaPersistentObjectUi.class);
		return persistentObject;
	    } catch (final Exception e) {
		e.printStackTrace();
	    }
	    return null;
	}

	public void saveWizardModel() {
	    final byte[] persistedWizardModel = getConfigurationController().getSerialiser().serialise(getWizardModel().createPersistentObject());
	    previousWizardModel = getWizardModel();
	    reloadWizardModel(persistedWizardModel);
	}

	public void restorePreviousWizardModel() {
	    wizardModel = previousWizardModel;
	}

	private void reloadWizardModel(final byte[] data) {
	    DynamicCriteriaPersistentObjectUi persistentObject;
	    try {
		persistentObject = getConfigurationController().getSerialiser().deserialise(data, DynamicCriteriaPersistentObjectUi.class);
		final DynamicEntityQueryCriteria<T, DAO> dynamicCriteria = getCustomDynamicCriteria(entityFactory, getWizardModel().getDynamicCriteria().getDao(), getWizardModel().getDynamicCriteria().getEntityAggregatesDao(), getWizardModel().getDynamicCriteria().getDaoFactory(), getWizardModel().getDynamicCriteria().getValueMatcherFactory(), persistentObject);
		wizardModel = wizardModelFactory.createWizardModel(dynamicCriteria, getWizardModel().getResultantEntityClass(), persistentObject, DynamicCriteriaModelBuilder.this);
	    } catch (final Exception e) {
		e.printStackTrace();
	    }

	}
    }

    public IEntityReviewFactory<T, DAO, R> getEntityReviewFactory() {
	return entityReviewFactory;
    }

    /**
     * Contract for anything that wants to provide special behaviour for the save as action.
     * 
     * @author oleh
     * 
     */
    public static interface ISaveAsContract {

	/**
	 * Returns value that indicates whether continue save as process or not.
	 * 
	 * @return
	 */
	boolean beforeSave();

	/**
	 * Returns path where criteria configuration should be saved.
	 * 
	 * @return
	 */
	String getKeyToSave();

	/**
	 * Performs any additional tasks after saving action.
	 * 
	 * @param isClosing
	 */
	void afterSave(boolean isClosing);
    }

}
