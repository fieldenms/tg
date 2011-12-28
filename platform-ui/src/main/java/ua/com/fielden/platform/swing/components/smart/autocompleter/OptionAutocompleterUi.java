package ua.com.fielden.platform.swing.components.smart.autocompleter;

import java.awt.Container;
import java.awt.Dialog.ModalityType;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;

import org.apache.log4j.Logger;
import org.jdesktop.jxlayer.JXLayer;
import org.jfree.ui.RefineryUtilities;
import org.jvnet.flamingo.common.icon.EmptyResizableIcon;

import ua.com.fielden.actionpanelmodel.ActionPanelBuilder;
import ua.com.fielden.platform.basic.autocompleter.DynamicEntityQueryCriteriaValueMatcher;
import ua.com.fielden.platform.dao.IDaoFactory;
import ua.com.fielden.platform.dao.IEntityAggregatesDao;
import ua.com.fielden.platform.dao.IEntityDao;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.entity.matcher.IValueMatcherFactory;
import ua.com.fielden.platform.reflection.TitlesDescsGetter;
import ua.com.fielden.platform.swing.actions.ActionChanger;
import ua.com.fielden.platform.swing.egi.models.builders.PropertyTableModelBuilder;
import ua.com.fielden.platform.swing.review.DynamicCriteriaModelBuilder;
import ua.com.fielden.platform.swing.review.DynamicCriteriaPersistentObjectUi;
import ua.com.fielden.platform.swing.review.DynamicEntityLocator;
import ua.com.fielden.platform.swing.review.DynamicEntityLocatorModel;
import ua.com.fielden.platform.swing.review.DynamicEntityQueryCriteria;
import ua.com.fielden.platform.swing.review.DynamicEntityReview;
import ua.com.fielden.platform.swing.review.DynamicEntityReviewModel;
import ua.com.fielden.platform.swing.review.IEntityMasterManager;
import ua.com.fielden.platform.swing.review.IOrderSetter;
import ua.com.fielden.platform.swing.review.IEntitySelectionListener;
import ua.com.fielden.platform.swing.review.LocatorPersistentObject;
import ua.com.fielden.platform.swing.review.factory.IEntityReviewFactory;
import ua.com.fielden.platform.swing.review.factory.IWizardModelFactory;
import ua.com.fielden.platform.swing.review.optionbuilder.ActionChangerBuilder;
import ua.com.fielden.platform.swing.review.wizard.AbstractWizardModel;
import ua.com.fielden.platform.swing.review.wizard.LocatorWizard;
import ua.com.fielden.platform.swing.review.wizard.LocatorWizardModel;
import ua.com.fielden.platform.utils.ResourceLoader;

public class OptionAutocompleterUi extends AutocompleterUi {
    private final Logger logger = Logger.getLogger(getClass());

    //Model builder for locators dialog, the locators modal dialog it self and it's chosen entities.
    private EntityLocatorCriteraModelBuilder dynamicModelBuilder;
    private final JDialog autocompleterDialog;
    private final List<AbstractEntity> selectedEntities = new ArrayList<AbstractEntity>();
    private boolean isOk = false;

    //Actions  for locators pop up button.
    private final ActionChanger<?> saveAction;
    private final ActionChanger<?> saveAsDefaultAction;

    //Properties those holds the information about the autocompleter state.
    private Object selectedString;
    private int startSelectedIndex, endSelectedIndex, previousCaretPosition;

    // Needed for DynamicCriteiraModelBuilder lazy loading
    private final EntityFactory entityFactory;
    private final IEntityMasterManager entityMasterFactory;
    private final IValueMatcherFactory vmf;
    private final IDaoFactory daoFactory;
    private final LocatorManager locatorManger;

    public OptionAutocompleterUi(final EntityFactory entityFactory,//
	    final OptionAutocompleterTextFieldLayer<?> layer, //
	    final String caption,//
	    final IValueMatcherFactory vmf,//
	    final IDaoFactory daoFactory,//
	    final IEntityMasterManager entityMasterFactory, final LocatorManager locatorManger) {
	super(layer, caption);

	this.entityFactory = entityFactory;
	this.vmf = vmf;
	this.daoFactory = daoFactory;
	this.locatorManger = locatorManger;
	this.entityMasterFactory = entityMasterFactory;
	this.saveAction = createSaveAction();
	this.saveAsDefaultAction = createSaveAsDefaultAction();

	autocompleterDialog = new JDialog(SwingUtilities.windowForComponent(layer.getAutocompleter().getTextComponent()), TitlesDescsGetter.getEntityTitleAndDesc(locatorManger.getResultantEntityClass()).getKey()
		+ " Entity locator", ModalityType.APPLICATION_MODAL);
	autocompleterDialog.setIconImage(ResourceLoader.getImage("images/tg-icon.png"));
	autocompleterDialog.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
	autocompleterDialog.addWindowListener(createWindowCloseHandler());
	getLayer().getValueMatcher().setBeforeFindMatchesAction(new Runnable() {
	    public void run() {
		// an autocompleter's value matcher should be updated before actual usage. And also dynamic model builder should be initialised.
		// initialises dynamic model builder and updates value matcher if it wasn't updated yet.
		if (dynamicModelBuilder == null) {
		    getDynamicModelBuilder();
		} else {
		    getDynamicModelBuilder().setExistingFile();
		    getDynamicModelBuilder().reload();
		}
		updateValueMatcher();
	    }
	});
	layer.getAutocompleter().getTextComponent().addFocusListener(createComponentFocusListener());

    }

    /**
     * Returns dynamic model builder. It will be lazy initialised (it is heavy-weight operation!!) when first invocation is performing.
     * 
     * @return
     */
    public EntityLocatorCriteraModelBuilder getDynamicModelBuilder() {
	if (dynamicModelBuilder == null) {
	    logger.debug("\t\tInitialising model builder...");
	    dynamicModelBuilder = new EntityLocatorCriteraModelBuilder(entityFactory, vmf, daoFactory, null, locatorManger, null, createEntityLocatorFactory(), createWizardModelFactory());
	    logger.debug("\t\tInitialising model builder...done");
	}
	return dynamicModelBuilder;
    }

    @Override
    public OptionAutocompleterTextFieldLayer getLayer() {
	return (OptionAutocompleterTextFieldLayer) super.getLayer();
    }

    /**
     * Wizard model factory for entity locator.
     * 
     * @author TG Team
     * 
     * @param <T>
     * @param <DAO>
     * @param <R>
     */
    private class LocatorWizardModelFactory<T extends AbstractEntity, DAO extends IEntityDao<T>, R extends AbstractEntity> implements IWizardModelFactory<T, DAO, R> {
	@Override
	public LocatorWizardModel<T, DAO, R> createWizardModel(final DynamicEntityQueryCriteria<T, DAO> dynamicCriteria, final Class<R> resultantEntityClass, final DynamicCriteriaPersistentObjectUi persistentObject, final DynamicCriteriaModelBuilder<T, DAO, R> modelBuilder) {
	    return new LocatorWizardModel<T, DAO, R>(dynamicCriteria, resultantEntityClass, persistentObject, modelBuilder);
	}

	@Override
	public LocatorWizard createWizardPanel(final AbstractWizardModel wizardModel, final Action buildAction, final Action cancelAction) {
	    return new LocatorWizard((LocatorWizardModel) wizardModel, buildAction, cancelAction);
	}
    }

    private IWizardModelFactory createWizardModelFactory() {
	return new LocatorWizardModelFactory();
    }

    private FocusListener createComponentFocusListener() {
	return new FocusAdapter() {
	    @Override
	    public void focusGained(final FocusEvent e) {
		if (isOk && selectedEntities.size() > 0) {
		    selectedString = getLayer().getAutocompleter().getSelectedHint(selectedEntities, startSelectedIndex, endSelectedIndex, previousCaretPosition);
		    getLayer().getAutocompleter().acceptHint(selectedString);
		    selectedEntities.clear();
		    isOk = false;
		}
	    }
	};
    }

    private WindowListener createWindowCloseHandler() {
	return new WindowAdapter() {
	    @Override
	    public void windowClosing(final WindowEvent e) {
		if (getDynamicModelBuilder().isCriteriaModelChanged()) {
		    final Object options[] = { "Save", "Save as default", "No" };
		    final int chosenOption = JOptionPane.showOptionDialog(autocompleterDialog, "This locator has been changed, would you like to save it?", "Save entity locator configuration", JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, options[0]);
		    switch (chosenOption) {
		    case JOptionPane.YES_OPTION:
			saveAction.actionPerformed(null);
			break;
		    case JOptionPane.NO_OPTION:
			saveAsDefaultAction.actionPerformed(null);
			break;
		    }
		}
		autocompleterDialog.dispose();
		SwingUtilities.windowForComponent(getLayer().getAutocompleter().getTextComponent()).setVisible(true);
		getLayer().getAutocompleter().getTextComponent().requestFocusInWindow();
	    }

	};
    }

    private IEntityReviewFactory createEntityLocatorFactory() {
	return new IEntityReviewFactory() {

	    @Override
	    public DynamicEntityReviewModel createModel(final DynamicEntityQueryCriteria criteria, final PropertyTableModelBuilder builder, final ActionChangerBuilder actionChangerBuilder, final ActionPanelBuilder panelBuilder, final int columns, final Map criteriaProperties, final LocatorPersistentObject locatorPersistentObject, final Runnable... afterRunActions) {
		final boolean useForAutocompleter = ((LocatorWizardModel) getDynamicModelBuilder().getWizardModel()).isUseForAutocompleter();
		final boolean searchByKey = ((LocatorWizardModel) getDynamicModelBuilder().getWizardModel()).isSearchByKey();
		final boolean searchByDesc = ((LocatorWizardModel) getDynamicModelBuilder().getWizardModel()).isSearchByDesc();
		return new DynamicEntityLocatorModel(criteria, builder, actionChangerBuilder, columns, criteriaProperties, createSelectionListener(), createSelectAction(), createCancelAction(), useForAutocompleter, searchByKey, searchByDesc, afterRunActions);
	    }

	    @Override
	    public DynamicEntityReview createView(final DynamicEntityReviewModel model, final boolean loadRecordByDefault, final DynamicCriteriaModelBuilder modelBuilder, final boolean isPrinciple) {
		final int selectionMode = isMultiValued() ? ListSelectionModel.MULTIPLE_INTERVAL_SELECTION : ListSelectionModel.SINGLE_SELECTION;
		return new DynamicEntityLocator((DynamicEntityLocatorModel) model, loadRecordByDefault, modelBuilder) {
		    @Override
		    protected JComponent buildActionChanger() {
			final List<String> actionOrder = new ArrayList<String>();
			actionOrder.add("Configure");
			actionOrder.add("Save");
			actionOrder.add("Save as default");
			actionOrder.add("Load default");
			return getEntityReviewModel().getActionChangerBuilder().buildActionChanger(actionOrder);
		    }
		};
	    }

	};
    }

    private IEntitySelectionListener createSelectionListener() {
	selectedEntities.clear();
	return new IEntitySelectionListener() {

	    @Override
	    public boolean isSelected(final AbstractEntity entityToCheck) {
		return selectedEntities.contains(entityToCheck);
	    }

	    @Override
	    public void performDeselect(final AbstractEntity selectedObject) {
		selectedEntities.remove(selectedObject);
	    }

	    @Override
	    public void performSelection(final AbstractEntity selectedObject) {
		if (isMultiselection()) {
		    if (!isSelected(selectedObject)) {
			selectedEntities.add(selectedObject);
		    }
		} else {
		    selectedEntities.clear();
		    selectedEntities.add(selectedObject);
		}
	    }

	    @Override
	    public void clearSelection() {
		selectedEntities.clear();
	    }

	    @Override
	    public boolean isMultiselection() {
		return isMultiValued();
	    }

	    @Override
	    public boolean isSelectionEmpty() {
		return selectedEntities.isEmpty();
	    }

	};
    }

    @Override
    protected void processMouseEvent(final MouseEvent event, final JXLayer<JTextField> layer) {
	if (event.getID() == MouseEvent.MOUSE_CLICKED && (event.getModifiers() & InputEvent.CTRL_MASK) != 0) {
	    getState().ctrlAction(this);
	} else {
	    super.processMouseEvent(event, layer);
	}
    }

    private Action createSelectAction() {
	return new AbstractAction("Select") {

	    private static final long serialVersionUID = -3378557699833066556L;

	    {
		putValue(Action.SHORT_DESCRIPTION, "Closes this dialog and updates the text of autocompleter.");
		putValue(Action.MNEMONIC_KEY, KeyEvent.VK_S);
	    }

	    @Override
	    public void actionPerformed(final ActionEvent e) {
		isOk = true;
		postDialogCloseEvent();
	    }

	};
    }

    private Action createCancelAction() {
	return new AbstractAction("Close") {

	    private static final long serialVersionUID = -8079302762902025907L;

	    {
		putValue(Action.SHORT_DESCRIPTION, "Closes this dialog.");
		putValue(Action.MNEMONIC_KEY, KeyEvent.VK_C);
	    }

	    @Override
	    public void actionPerformed(final ActionEvent e) {
		postDialogCloseEvent();
	    }

	};
    }

    private ActionChanger<Void> createSaveAction() {
	return new ActionChanger<Void>("Save") {

	    private static final long serialVersionUID = -4300115473173789981L;

	    {
		putValue(SHORT_DESCRIPTION, "Saves the locator");
		putValue(LARGE_ICON_KEY, new EmptyResizableIcon(new Dimension(0, 0)));
	    }

	    @Override
	    protected Void action(final ActionEvent e) throws Exception {
		return null;
	    }

	    @Override
	    protected void postAction(final Void value) {
		super.postAction(value);
		getDynamicModelBuilder().saveLocalConfiguration();
		autocompleterDialog.setTitle(TitlesDescsGetter.getEntityTitleAndDesc(locatorManger.getResultantEntityClass()).getKey() + " entity locator");
	    }

	};
    }

    private ActionChanger<Void> createSaveAsDefaultAction() {
	return new ActionChanger<Void>("Save as default") {

	    private static final long serialVersionUID = -4300115473173789981L;

	    {
		putValue(SHORT_DESCRIPTION, "Saves the locator as default and updates local configuration");
		putValue(LARGE_ICON_KEY, new EmptyResizableIcon(new Dimension(0, 0)));
	    }

	    @Override
	    protected Void action(final ActionEvent e) throws Exception {
		return null;
	    }

	    @Override
	    protected void postAction(final Void value) {
		super.postAction(value);
		getDynamicModelBuilder().saveAsDefaultConfiguration();
		autocompleterDialog.setTitle(TitlesDescsGetter.getEntityTitleAndDesc(locatorManger.getResultantEntityClass()).getKey() + " default entity locator");
	    }

	};
    }

    private ActionChanger<Void> createLoadDefaultAction() {
	return new ActionChanger<Void>("Load default") {

	    private static final long serialVersionUID = -4300115473173789981L;

	    {
		putValue(SHORT_DESCRIPTION, "Loads default locator configuration and updates local configuration");
		putValue(LARGE_ICON_KEY, new EmptyResizableIcon(new Dimension(0, 0)));
	    }

	    @Override
	    protected Void action(final ActionEvent e) throws Exception {
		return null;
	    }

	    @Override
	    protected void postAction(final Void value) {
		super.postAction(value);
		getDynamicModelBuilder().loadDefaultConfiguration(autocompleterDialog.getContentPane(), saveAction, saveAsDefaultAction, createLoadDefaultAction());
		autocompleterDialog.setTitle(TitlesDescsGetter.getEntityTitleAndDesc(locatorManger.getResultantEntityClass()).getKey() + " default entity locator");
	    }

	};
    }

    private void updateValueMatcher() {
	final DynamicEntityQueryCriteriaValueMatcher dynamicValueMatcher = getLayer().getValueMatcher();
	final boolean searchByKey = ((LocatorWizardModel) dynamicModelBuilder.getWizardModel()).isSearchByKey();
	final boolean searchByDesc = ((LocatorWizardModel) dynamicModelBuilder.getWizardModel()).isSearchByDesc();
	final boolean useForAutocompleter = ((LocatorWizardModel) dynamicModelBuilder.getWizardModel()).isUseForAutocompleter();
	dynamicValueMatcher.setUseQueryCriteira(useForAutocompleter);
	dynamicValueMatcher.setSearchByDesc(searchByDesc);
	dynamicValueMatcher.setSearchByKey(searchByKey);
	dynamicValueMatcher.setBaseCriteria(dynamicModelBuilder.getWizardModel().getDynamicCriteria());
	if (useForAutocompleter) {
	    getLayer().highlightFirstHintValue(searchByKey);
	    getLayer().highlightSecondHintValue(searchByDesc);
	} else {
	    getLayer().highlightFirstHintValue(true);
	    getLayer().highlightSecondHintValue(false);
	}
    }

    private void postDialogCloseEvent() {
	final WindowEvent wev = new WindowEvent(autocompleterDialog, WindowEvent.WINDOW_CLOSING);
	Toolkit.getDefaultToolkit().getSystemEventQueue().postEvent(wev);
    }

    public void showAutocompleterDialog() {
	previousCaretPosition = getComponent().getCaretPosition();
	startSelectedIndex = getComponent().getSelectionStart();
	endSelectedIndex = getComponent().getSelectionEnd();
	isOk = false;
	autocompleterDialog.setPreferredSize(new Dimension(800, 600));
	autocompleterDialog.pack();
	getDynamicModelBuilder().setExistingFile();
	autocompleterDialog.setTitle(TitlesDescsGetter.getEntityTitleAndDesc(locatorManger.getResultantEntityClass()).getKey()
		+ (getDynamicModelBuilder().isGlobalFile() ? " default entity" : " entity") + " locator");
	initDynamicBuilder();
	RefineryUtilities.centerFrameOnScreen(autocompleterDialog);
	autocompleterDialog.setVisible(true);
    }

    private void initDynamicBuilder() {
	final ActionChangerBuilder actionChangerBuilder = new ActionChangerBuilder();
	actionChangerBuilder.setAction(saveAction);
	actionChangerBuilder.setAction(saveAsDefaultAction);
	actionChangerBuilder.setAction(createLoadDefaultAction());
	getDynamicModelBuilder().init(autocompleterDialog.getContentPane(), actionChangerBuilder, null, false, true);
    }

    private static class EntityLocatorCriteraModelBuilder extends DynamicCriteriaModelBuilder {

	private String currentConfigurationKey;

	public EntityLocatorCriteraModelBuilder(final EntityFactory entityFactory, final IValueMatcherFactory vmf, final IDaoFactory daoFactory, final IEntityAggregatesDao entityAggregatesDao, final LocatorManager locatorManager, final IOrderSetter orderSetter, final IEntityReviewFactory entityReviewModelFactory, final IWizardModelFactory wizardModelFactory) {
	    super(entityFactory, vmf, daoFactory, daoFactory.newDao((Class<AbstractEntity>) locatorManager.getResultantEntityClass()), entityAggregatesDao, locatorManager.getResultantEntityClass(), locatorManager.getLocalKey(), locatorManager, orderSetter, entityReviewModelFactory, wizardModelFactory);
	}

	@Override
	public LocatorManager getConfigurationController() {
	    return (LocatorManager) super.getConfigurationController();
	}

	/**
	 * Saves the current configuration as local configuration.
	 */
	public void saveLocalConfiguration() {
	    currentConfigurationKey = getConfigurationController().getLocalKey();
	    saveCriteria(false);
	}

	/**
	 * Saves the current configuration as default. Also it saves in to the local configuration store.
	 */
	public void saveAsDefaultConfiguration() {
	    currentConfigurationKey = getConfigurationController().getLocalKey();
	    saveCriteria(false);
	    currentConfigurationKey = getConfigurationController().getDefaultKey();
	    saveCriteria(false);
	}

	public void loadDefaultConfiguration(final Container container, final ActionChanger<?> saveAction, final ActionChanger<?> saveAsDefaultAction, final ActionChanger<?> loadDefaultAction) {
	    currentConfigurationKey = getConfigurationController().getDefaultKey();
	    if (!isConfigurationExists()) {
		JOptionPane.showMessageDialog(container, "Default configuration doesn't exist. Save it as default first.", "Message", JOptionPane.INFORMATION_MESSAGE);
		currentConfigurationKey = getConfigurationController().getLocalKey();
		return;
	    }
	    final int chosenOption = JOptionPane.showConfirmDialog(container, "Local configuration will be overriden. Would you like to continue?", "Information", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
	    if (chosenOption == JOptionPane.NO_OPTION) {
		currentConfigurationKey = getConfigurationController().getLocalKey();
		return;
	    }
	    init(saveAction, saveAsDefaultAction, loadDefaultAction, container);
	    currentConfigurationKey = getConfigurationController().getLocalKey();
	    saveCriteria(false);
	    currentConfigurationKey = getConfigurationController().getDefaultKey();
	}

	/**
	 * Initiates this {@link EntityLocatorCriteraModelBuilder} with locator's main actions (i. e. save, save as default and load default actions.)
	 * 
	 * @param saveAction
	 * @param saveAsDefaultAction
	 * @param loadDefaultAction
	 * @param container
	 */
	public void init(final ActionChanger<?> saveAction, final ActionChanger<?> saveAsDefaultAction, final ActionChanger<?> loadDefaultAction, final Container container) {
	    final ActionChangerBuilder actionChangerBuilder = new ActionChangerBuilder();
	    actionChangerBuilder.setAction(saveAction);
	    actionChangerBuilder.setAction(saveAsDefaultAction);
	    actionChangerBuilder.setAction(loadDefaultAction);
	    init(container, actionChangerBuilder, null, false, true);
	}

	/**
	 * Returns value that indicates whether this {@link DynamicCriteriaModelBuilder} was created for global entity locator's configuration file or not.
	 * 
	 * @return
	 */
	public boolean isGlobalFile() {
	    return getConfigurationController().getDefaultKey().equals(getKey());
	}

	@Override
	public String getKey() {
	    currentConfigurationKey = currentConfigurationKey == null ? getExistingKey() : currentConfigurationKey;
	    return currentConfigurationKey;
	}

	private String getExistingKey() {
	    if (getConfigurationController().exists(getConfigurationController().getLocalKey())) {
		return getConfigurationController().getLocalKey();
	    } else if (getConfigurationController().exists(getConfigurationController().getDefaultKey())) {
		return getConfigurationController().getDefaultKey();
	    }
	    return getConfigurationController().getLocalKey();
	}

	public void setExistingFile() {
	    currentConfigurationKey = getExistingKey();
	}

    }

}
