package ua.com.fielden.platform.snappy;

//import java.awt.event.ActionEvent;
//import java.awt.event.KeyEvent;
//import java.io.File;
//import java.io.IOException;
//import java.util.ArrayList;
//import java.util.Collections;
//import java.util.Comparator;
//import java.util.HashMap;
//import java.util.Iterator;
//import java.util.List;
//import java.util.Map;
//
//import javax.swing.AbstractAction;
//import javax.swing.Action;
//import javax.swing.JComponent;
//import javax.swing.JFileChooser;
//import javax.swing.filechooser.FileFilter;
//import javax.swing.tree.TreePath;
//
//import ua.com.fielden.platform.entity.AbstractEntity;
//import ua.com.fielden.platform.pagination.IPage;
//import ua.com.fielden.platform.reflection.ClassesRetriever;
//import ua.com.fielden.platform.reflection.TitlesDescsGetter;
//import ua.com.fielden.platform.snappy.RulePropertyCheckingListener.PropertyType;
//import ua.com.fielden.platform.swing.actions.BlockingCommand;
//import ua.com.fielden.platform.swing.actions.Command;
//import ua.com.fielden.platform.swing.components.blocking.BlockingIndefiniteProgressPane;
//import ua.com.fielden.platform.swing.review.EntityQueryCriteria;
//import ua.com.fielden.platform.utils.Pair;
//import ua.com.fielden.snappy.Result;
//import ua.com.fielden.snappy.model.rule.Rule;
//import ua.com.fielden.snappy.query.ChildrenFetchingStrategy;
//import ua.com.fielden.snappy.query.ConditionedHotProcessor;
//import ua.com.fielden.snappy.storing.ApplicationModel;
//import ua.com.fielden.snappy.storing.Zipper;
//import ua.com.fielden.snappy.view.blocks.toplevel.ConditionedHotBlock;
//import ua.com.fielden.snappy.view.blocks.toplevel.RuleBlock;
//
//import com.google.inject.Injector;
//import com.thoughtworks.xstream.persistence.FilePersistenceStrategy;
//import com.thoughtworks.xstream.persistence.XmlArrayList;

/**
 * Snappy application model for TG systems.
 *
 * TODO Snappy integration logic has been commented until snappy related stuff will be migrated to TG platform.
 * TODO Snappy integration logic has been commented until snappy related stuff will be migrated to TG platform.
 * TODO Snappy integration logic has been commented until snappy related stuff will be migrated to TG platform.
 * TODO Snappy integration logic has been commented until snappy related stuff will be migrated to TG platform.
 * TODO Snappy integration logic has been commented until snappy related stuff will be migrated to TG platform.
 * TODO Snappy integration logic has been commented until snappy related stuff will be migrated to TG platform.
 * TODO Snappy integration logic has been commented until snappy related stuff will be migrated to TG platform.
 * TODO Snappy integration logic has been commented until snappy related stuff will be migrated to TG platform.
 * TODO Snappy integration logic has been commented until snappy related stuff will be migrated to TG platform.
 * TODO Snappy integration logic has been commented until snappy related stuff will be migrated to TG platform.
 * TODO Snappy integration logic has been commented until snappy related stuff will be migrated to TG platform.
 * TODO Snappy integration logic has been commented until snappy related stuff will be migrated to TG platform.
 * TODO Snappy integration logic has been commented until snappy related stuff will be migrated to TG platform.
 * TODO Snappy integration logic has been commented until snappy related stuff will be migrated to TG platform.
 * TODO Snappy integration logic has been commented until snappy related stuff will be migrated to TG platform.
 * TODO Snappy integration logic has been commented until snappy related stuff will be migrated to TG platform.
 * TODO Snappy integration logic has been commented until snappy related stuff will be migrated to TG platform.
 *
 * @author TG Team
 *
 */
public class TgSnappyApplicationModel {}
//extends ApplicationModel<TgSnappyComponentsActivator> {
//    private final List<Class<? extends AbstractEntity>> entityClasses;
//    private final Map<Class<?>, RuleBlock<?>> entityRules = new HashMap<Class<?>, RuleBlock<?>>();
//    private final Injector injector;
//
//    public List<Class<? extends AbstractEntity>> getEntityClasses() {
//	return entityClasses;
//    }
//
//    /**
//     * Creates snappy application model for TG systems based on classes located in [classesLocation] in specified [classesPackage].
//     *
//     * @param dbUrl
//     * @param classesLocation
//     * @param classesPackage
//     * @param domainName
//     *            - domain name for TG systems e.g. "Fleet system" or "Rotable management system"
//     */
//    public TgSnappyApplicationModel(final String classesLocation, final String classesPackage, final String domainName, final Injector injector) {
//	super(null, domainName);
//	this.injector = injector;
//	System.out.print("Loading entity classes...");
//	final List<Class<?>> classes;
//	try {
//	    classes = ClassesRetriever.getAllClassesInPackageDerivedFrom(classesLocation, classesPackage, AbstractEntity.class);
//	    classes.removeAll(ClassesRetriever.getAllClassesInPackageDerivedFrom(classesLocation, classesPackage, EntityQueryCriteria.class));
//	} catch (final Exception e) {
//	    throw new RuntimeException(e);
//	}
//	entityClasses = initEntityClasses(classes);
//	System.out.println("loaded");
//    }
//
//    /**
//     * Creates snappy application model for TG systems based on specified <code>classes</code>.
//     *
//     * @param dbUrl
//     * @param classesLocation
//     * @param classesPackage
//     * @param domainName
//     *            - domain name for TG systems e.g. "Fleet system" or "Rotable management system"
//     */
//    public TgSnappyApplicationModel(final List<Class<?>> classes, final String domainName, final Injector injector) {
//	super(null, domainName);
//	this.injector = injector;
//	entityClasses = initEntityClasses(classes);
//    }
//
//    protected List<Class<? extends AbstractEntity>> initEntityClasses(final List<Class<?>> classes) {
//	Collections.sort(classes, new Comparator<Class<?>>() {
//	    @Override
//	    public int compare(final Class<?> class1, final Class<?> class2) {
//		return class1.getSimpleName().compareTo(class2.getSimpleName());
//	    }
//	});
//	final List<Class<? extends AbstractEntity>> aeClasses = new ArrayList<Class<? extends AbstractEntity>>();
//	for (final Class<?> klass : classes){
//	    aeClasses.add((Class<? extends AbstractEntity>)klass);
//	}
//	return aeClasses;
//    }
//
//    private static JFileChooser createFileChooser() {
//	final JFileChooser chooser = new JFileChooser(new File(ApplicationModel.STORAGE_URL));
//	chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
//	chooser.setFileFilter(new FileFilter() {
//	    @Override
//	    public boolean accept(final File file) {
//		if (file.isDirectory()) {
//		    return true;
//		} else {
//		    final String path = file.getAbsolutePath().toLowerCase();
//		    if ((path.endsWith(EXTENSION) && (path.charAt(path.length() - EXTENSION.length() - 1)) == '.')) {
//			return true;
//		    }
//		}
//		return false;
//	    }
//
//	    @Override
//	    public String getDescription() {
//		return "." + EXTENSION + " files";
//	    }
//	});
//	return chooser;
//    }
//
//    /**
//     * Creates save action.
//     *
//     * @param blockingComponent
//     * @return
//     */
//    protected AbstractAction createSaveAction(final JComponent blockingComponent) {
//	final Command<Pair<List<TreePath>, List<TreePath>>> action = new BlockingCommand<Pair<List<TreePath>, List<TreePath>>>("Save", (BlockingIndefiniteProgressPane) blockingComponent) {
//	    private static final long serialVersionUID = -8247027477900897941L;
//	    private File file;
//	    private JFileChooser jfcSaveFile;
//	    private RuntimeException lastError;
//
//	    @Override
//	    protected boolean preAction() {
//		lastError = null;
//		if (!isAssociatedWithFile()) {
//		    if (jfcSaveFile == null) {
//			jfcSaveFile = createFileChooser();
//		    }
//		    if (JFileChooser.APPROVE_OPTION != jfcSaveFile.showSaveDialog(blockingComponent)) {
//			return false;
//		    }
//		    file = jfcSaveFile.getSelectedFile().exists() ? jfcSaveFile.getSelectedFile() : new File(jfcSaveFile.getSelectedFile().getPath() + "." + EXTENSION);
//		    if (file == null) {
//			return false;
//		    }
//		    setCurrentRulesFile(file);
//		    try {
//			getComponentsActivator().changeTitle(ApplicationModel.MAIN_CAPTION + " - " + getDomainName() + " - " + getCurrentRulesFile().getCanonicalPath());
//		    } catch (final IOException e) {
//			e.printStackTrace();
//		    }
//		}
//		final boolean b = super.preAction();
//		getComponentsActivator().enableButtons(false);
//		setMessage("Saving...");
//		clearEntityRules();
//		getComponentsActivator().clearEntitiesTree();
//		return b;
//	    }
//
//	    @Override
//	    protected Pair<List<TreePath>, List<TreePath>> action(final ActionEvent event) throws Exception {
//		// provide temporary directory :
//		final String tempDirUrl = getCurrentRulesFile().getPath() + TEMP_DIR_SUFFIX;
//		deleteTempDirectory(tempDirUrl);
//		final File tempDirFile = new File(tempDirUrl);
//		tempDirFile.mkdir();
//		// assign temporary directory with xstream list:
//		final List<Rule> xmlRuleList = new XmlArrayList(new FilePersistenceStrategy(tempDirFile));
//		for (int i = 0; i < getNodeLayer().getChildrenCount(); i++) {
//		    if (getNodeLayer().getChild(i) instanceof RuleBlock) {
//			final RuleBlock<?> ruleBlock = (RuleBlock<?>) getNodeLayer().getChild(i);
//			// update model by actual rule block and add updated model to list:
//			try {
//			    ruleBlock.updateModel();
//			} catch (final Throwable e) {
//			    lastError = new RuntimeException("<html><b>" + ((RuleBlock<?>) getNodeLayer().getChild(i)).getCaption()
//				    + "</b> rule is incorrectly constructed. Please complete and correct.\n\n<b>Message :</b> <i>" + e.getMessage() + "</i></html>", e);
//			}
//			xmlRuleList.add(ruleBlock.model());
//		    }
//		}
//		if (lastError == null) {
//		    // zip xml models into single file and remove temporary directory
//		    Zipper.zip(tempDirUrl, getCurrentRulesFile());
//		}
//		deleteTempDirectory(tempDirUrl);
//		return synchronizeAll();
//	    }
//
//	    @Override
//	    protected void postAction(final Pair<List<TreePath>, List<TreePath>> allTreePaths) {
//		if (lastError == null) {
//		    for (int i = 0; i < getNodeLayer().getChildrenCount(); i++) {
//			if (getNodeLayer().getChild(i) instanceof RuleBlock) {
//			    final RuleBlock<?> ruleBlock = (RuleBlock<?>) getNodeLayer().getChild(i);
//			    // update ruleBlock by new updated model:
//			    ruleBlock.updateBlockTreeFrom(ruleBlock.model());
//			    ruleBlock.reshapeAllHierarchy();
//			}
//		    }
//		}
//		// synchronization :
//		getComponentsActivator().clearSkeletons();
//		getComponentsActivator().synchronizeAllInView(allTreePaths);
//		getComponentsActivator().refreshSkeletonsAndResults();
//
//		getComponentsActivator().enableButtons(true);
//
//		if (lastError != null) {
//		    getComponentsActivator().changeStatus("Saving failed.");
//		    throw lastError;
//		}
//		getComponentsActivator().changeStatus("Successfully saved.");
//		super.postAction(allTreePaths);
//	    }
//
//	};
//	action.putValue(Action.MNEMONIC_KEY, KeyEvent.VK_S);
//	action.putValue(Action.SHORT_DESCRIPTION, "Save rules");
//	action.setEnabled(true);
//	return action;
//    }
//
//    /**
//     * BlockingCommand for "open" and "reload" actions.
//     *
//     * @author TG Team
//     *
//     */
//    private class LoadingBlockingCommand extends BlockingCommand<List<RuleBlock<?>>> {
//	private static final long serialVersionUID = 1L;
//	private final JFileChooser jfcLoadFile;
//	private final BlockingIndefiniteProgressPane blockingPane;
//	private final boolean openFile;
//	private final String blockingMessage;
//	private final String finalStatusMessage;
//	private Pair<List<TreePath>, List<TreePath>> allTreePaths;
//
//	public LoadingBlockingCommand(final String name, final BlockingIndefiniteProgressPane pane, final boolean openFile, final String blockingMessage, final String finalStatusMessage) {
//	    super(name, pane);
//	    this.jfcLoadFile = createFileChooser();
//	    this.blockingPane = pane;
//	    this.openFile = openFile;
//	    this.blockingMessage = blockingMessage;
//	    this.finalStatusMessage = finalStatusMessage;
//	}
//
//	@Override
//	protected boolean preAction() {
//	    allTreePaths = null;
//	    if (!openFile) {
//		if (!isAssociatedWithFile()) {
//		    return false;
//		}
//	    }
//	    if (openFile) {
//		if (JFileChooser.APPROVE_OPTION != jfcLoadFile.showOpenDialog(blockingPane)) {
//		    return false;
//		}
//		final File file = jfcLoadFile.getSelectedFile().exists() ? jfcLoadFile.getSelectedFile() : new File(jfcLoadFile.getSelectedFile().getPath() + "." + EXTENSION);
//		if (file == null) {
//		    return false;
//		}
//		setCurrentRulesFile(file);
//	    }
//	    final boolean b = super.preAction();
//	    getComponentsActivator().enableButtons(false);
//	    setMessage(blockingMessage); // "Opening..."
//	    clearEntityRules();
//	    getComponentsActivator().clearEntitiesTree();
//	    getComponentsActivator().clearCanvas();
//	    if (openFile) {
//		getComponentsActivator().clearResults();
//	    }
//	    getComponentsActivator().activateAll();
//	    try {
//		getComponentsActivator().changeTitle(ApplicationModel.MAIN_CAPTION + " - " + getDomainName() + " - " + getCurrentRulesFile().getCanonicalPath());
//	    } catch (final IOException e) {
//		e.printStackTrace();
//	    }
//	    return b;
//	}
//
//	@Override
//	protected List<RuleBlock<?>> action(final ActionEvent e) throws Exception {
//	    final String tempDirUrl = getCurrentRulesFile().getPath() + TEMP_DIR_SUFFIX;
//	    deleteTempDirectory(tempDirUrl);
//	    final File tempDirFile = new File(tempDirUrl);
//	    tempDirFile.mkdir();
//
//	    System.out.print("1. Unzipping... ");
//	    if (getCurrentRulesFile().isFile() && getCurrentRulesFile().exists()) {
//		Zipper.unzip(getCurrentRulesFile(), tempDirUrl);
//	    }
//	    System.out.println("done");
//
//	    System.out.print("2. Xml rules loading [" + tempDirUrl + "]... ");
//	    final List<Rule> xmlRuleList = new XmlArrayList(new FilePersistenceStrategy(tempDirFile));
//	    System.out.println("done");
//
//	    System.out.print("3. Construct rules blocks... ");
//	    final List<RuleBlock<?>> ruleBlocks = new ArrayList<RuleBlock<?>>();
//	    for (final Iterator it = xmlRuleList.iterator(); it.hasNext();) {
//		ruleBlocks.add(new RuleBlock<Object>((Rule) it.next()));
//	    }
//	    System.out.println("done");
//	    deleteTempDirectory(tempDirUrl);
//	    allTreePaths = synchronizeAll(ruleBlocks);
//	    return ruleBlocks;
//	}
//
//	@Override
//	protected void postAction(final List<RuleBlock<?>> ruleBlocks) {
//	    for (final RuleBlock<?> ruleBlock : ruleBlocks) {
//		ruleBlock.reshapeAllHierarchy();
//		ruleBlock.translate(findMaxXCoordinateForRulesBlocks(getNodeLayer()), 0);
//		getNodeLayer().addChild(ruleBlock);
//	    }
//	    ruleBlocks.clear();
//
//	    // synchronization :
//	    getComponentsActivator().clearSkeletons();
//	    getComponentsActivator().synchronizeAllInView(allTreePaths);
//	    getComponentsActivator().refreshSkeletonsAndResults();
//	    allTreePaths.getKey().clear();
//	    allTreePaths.getValue().clear();
//
//	    getComponentsActivator().enableButtons(true);
//	    super.postAction(ruleBlocks);
//	    getComponentsActivator().changeStatus(finalStatusMessage); // "Successfully opened."
//	}
//    }
//
//    /**
//     * Creates open action.
//     *
//     * @param blockingComponent
//     * @return
//     */
//    protected AbstractAction createOpenAction(final JComponent blockingComponent) {
//	final Command<List<RuleBlock<?>>> action = new LoadingBlockingCommand("Open/New", (BlockingIndefiniteProgressPane) blockingComponent, true, "Opening...", "Successfully opened.");
//	action.putValue(Action.MNEMONIC_KEY, KeyEvent.VK_O);
//	action.putValue(Action.SHORT_DESCRIPTION, "Open or create new rules file");
//	action.setEnabled(true);
//	return action;
//    }
//
//    /**
//     * Creates reLoad action.
//     *
//     * @param blockingComponent
//     * @return
//     */
//    protected AbstractAction createReloadAction(final JComponent blockingComponent) {
//	final Command<List<RuleBlock<?>>> action = new LoadingBlockingCommand("ReLoad", (BlockingIndefiniteProgressPane) blockingComponent, false, "Reloading...", "Successfully reloaded.");
//	action.putValue(Action.MNEMONIC_KEY, KeyEvent.VK_L);
//	action.putValue(Action.SHORT_DESCRIPTION, "Reload current rules file");
//	action.setEnabled(true);
//	return action;
//    }
//
//    /**
//     * Creates discard action.
//     *
//     * @param blockingComponent
//     * @return
//     */
//    protected AbstractAction createDiscardAction(final JComponent blockingComponent) {
//	final Command<Pair<List<TreePath>, List<TreePath>>> action = new BlockingCommand<Pair<List<TreePath>, List<TreePath>>>("Discard", (BlockingIndefiniteProgressPane) blockingComponent) {
//	    private static final long serialVersionUID = 1L;
//
//	    @Override
//	    protected boolean preAction() {
//		if (!isAssociatedWithFile()) {
//		    return false;
//		}
//		final boolean b = super.preAction();
//		getComponentsActivator().enableButtons(false);
//		setMessage("Discarding...");
//		//		getComponentsActivator().clearResults();
//		clearEntityRules();
//		getComponentsActivator().clearEntitiesTree();
//		for (int i = 0; i < getNodeLayer().getChildrenCount(); i++) {
//		    if (getNodeLayer().getChild(i) instanceof RuleBlock) {
//			final RuleBlock<?> ruleBlock = (RuleBlock<?>) getNodeLayer().getChild(i);
//			if (ruleBlock.model() != null) {
//			    ruleBlock.updateBlockTreeFrom(ruleBlock.model());
//			    ruleBlock.doAfterAttachAll();
//			} else {
//			    ruleBlock.removeFromParent();
//			}
//		    }
//		}
//		return b;
//	    }
//
//	    @Override
//	    protected Pair<List<TreePath>, List<TreePath>> action(final ActionEvent e) throws Exception {
//		return synchronizeAll();
//	    }
//
//	    @Override
//	    protected void postAction(final Pair<List<TreePath>, List<TreePath>> allTreePaths) {
//		// synchronization :
//		getComponentsActivator().clearSkeletons();
//		getComponentsActivator().synchronizeAllInView(allTreePaths);
//		getComponentsActivator().refreshSkeletonsAndResults();
//
//		getComponentsActivator().enableButtons(true);
//		getComponentsActivator().changeStatus("Successfully discarded.");
//		super.postAction(allTreePaths);
//	    }
//
//	};
//	action.putValue(Action.MNEMONIC_KEY, KeyEvent.VK_D);
//	action.putValue(Action.SHORT_DESCRIPTION, "Discards changed rules");
//	action.setEnabled(true);
//	return action;
//    }
//
//    /**
//     * Creates run action.
//     *
//     * @param blockingComponent
//     * @return
//     */
//    protected AbstractAction createRunAction(final JComponent blockingComponent) {
//	final Command<List<RuleResult>> action = new BlockingCommand<List<RuleResult>>("Run", (BlockingIndefiniteProgressPane) blockingComponent) {
//	    private static final long serialVersionUID = 1L;
//	    private RuntimeException lastError;
//
//	    @Override
//	    protected boolean preAction() {
//		lastError = null;
//		if (getNodeLayer().getChildrenCount() <= 0) {
//		    return false;
//		}
//		final boolean b = super.preAction();
//		getComponentsActivator().enableButtons(false);
//		setMessage("Running...");
//		return b;
//	    }
//
//	    @Override
//	    protected List<RuleResult> action(final ActionEvent event) throws Exception {
//		final List<RuleResult> results = new ArrayList<RuleResult>();
//		for (int i = 0; i < getNodeLayer().getChildrenCount(); i++) {
//		    if (getNodeLayer().getChild(i) instanceof RuleBlock) {
//			final RuleBlock<?> ruleBlock = (RuleBlock<?>) getNodeLayer().getChild(i);
//			Rule rule;
//			try {
//			    rule = ruleBlock.createModel();
//			} catch (final Throwable e) {
//			    final Class<?> klass = Class.forName(((ConditionedHotBlock<?>) ruleBlock.slots().get(0).block()).getFullCorrespondingClassName());
//			    getComponentsActivator().showFailure(TitlesDescsGetter.getEntityTitleAndDesc(klass).getKey() + ApplicationModel.RULE_NAME_SUFFIX);
//			    lastError = new RuntimeException("<html><b>" + ruleBlock.getCaption()
//				    + "</b> rule is incorrectly constructed. Please complete and correct.\n\n<b>Message :</b> <i>" + e.getMessage() + "</i></html>", e);
//			    continue;
//			}
//			for (int j = 0; j < rule.hots().size(); j++) {
//			    final ConditionedHotProcessor chp;
//			    final Class<?> klass = Class.forName(rule.hots().get(j).fullCorrespondingClassName());
//			    final String ruleName = TitlesDescsGetter.getEntityTitleAndDesc(klass).getKey() + ApplicationModel.RULE_NAME_SUFFIX;
//			    try {
//				chp = new ConditionedHotProcessor(rule.hots().get(j), ChildrenFetchingStrategy.FETCH_FETCHING_OR_CONDITIONED_ASSOCIATIONS, false);
//			    } catch (final Throwable e) {
//				getComponentsActivator().showFailure(ruleName);
//				lastError = new RuntimeException("<html><b>" + ruleName + "</b> rule generation failed. Please complete and correct.\n\n<b>Message :</b> <i>"
//					+ e.getCause().getMessage() + "</i></html>", e);
//				continue;
//			    }
//			    try {
//				final ISnappyDao dao = injector.getInstance(ISnappyDao.class);
//				final SnappyQuery snappyQuery = new SnappyQuery(chp.getMainQueryString()/* JOptionPane.showInputDialog(null, "Type query:", "Enter query string:", JOptionPane.QUESTION_MESSAGE)*/, chp.getSecondaryQueryString(), chp.getAggrAccessors());
//				final Pair<Result, IPage> resAndFirstPage = dao.process(snappyQuery);
//				results.add(new RuleResult(ruleName, resAndFirstPage.getKey(), resAndFirstPage.getValue()));
//			    } catch (final Throwable e) {
//				getComponentsActivator().showFailure(ruleName);
//				lastError = new RuntimeException("<html><b>" + ruleName + "</b> rule processing failed.</html>", e);
//			    }
//			}
//		    }
//		}
//		return results;
//	    }
//
//	    @Override
//	    protected void postAction(final List<RuleResult> results) {
//		for (final RuleResult ruleResult : results) {
//		    getComponentsActivator().showResult(ruleResult.getResult(), ruleResult.getFiltEntitiesPage(), ruleResult.getRuleName());
//		}
//		getComponentsActivator().enableButtons(true);
//		if (lastError != null) {
//		    getComponentsActivator().changeStatus("Running failed.");
//		    throw lastError;
//		}
//		getComponentsActivator().changeStatus("Running completed.");
//		super.postAction(results);
//	    }
//	};
//	action.putValue(Action.MNEMONIC_KEY, KeyEvent.VK_R);
//	action.putValue(Action.SHORT_DESCRIPTION, "Runs constructed rules");
//	action.setEnabled(true);
//	return action;
//    }
//
//    /**
//     * Simple class for rule processing results.
//     *
//     * @author TG Team
//     *
//     */
//    private class RuleResult {
//	private final String ruleName;
//	private final Result result;
//	private final IPage filtEntitiesPage;
//
//	public RuleResult(final String ruleName, final Result result, final IPage filtEntitiesPage) {
//	    this.ruleName = ruleName;
//	    this.result = result;
//	    this.filtEntitiesPage = filtEntitiesPage;
//	}
//
//	public String getRuleName() {
//	    return ruleName;
//	}
//
//	public Result getResult() {
//	    return result;
//	}
//
//	public IPage getFiltEntitiesPage() {
//	    return filtEntitiesPage;
//	}
//
//    }
//
//    /**
//     * Returns created earlier RuleBlock corresponding to specified "rootKlass" or creates new one.
//     *
//     * @param rootKlass
//     * @return
//     */
//    public RuleBlock<?> getOrCreateRuleBlock(final Class<?> rootKlass) {
//	if (!entityRules.containsKey(rootKlass)) {
//	    final String entityTitle = TitlesDescsGetter.getEntityTitleAndDesc(rootKlass).getKey();
//	    final ConditionedHotBlock<?> condHot = new ConditionedHotBlock<Object>(entityTitle, rootKlass.getSimpleName(), rootKlass.getName(), 0);
//	    final RuleBlock<?> ruleBlock = new RuleBlock<Object>(entityTitle + ApplicationModel.RULE_NAME_SUFFIX);
//	    ruleBlock.slots().get(0).snapIn(condHot);
//	    ruleBlock.reshapeAllHierarchy();
//	    ruleBlock.translate(ApplicationModel.findMaxXCoordinateForRulesBlocks(getNodeLayer()), 0);
//	    getNodeLayer().addChild(ruleBlock);
//	    entityRules.put(rootKlass, ruleBlock);
//	}
//	return entityRules.get(rootKlass);
//    }
//
//    /**
//     * Returns created earlier RuleBlock corresponding to specified "rootKlass".
//     *
//     * @param rootKlass
//     * @return
//     */
//    public RuleBlock<?> getRuleBlock(final Class<?> rootKlass) {
//	return entityRules.get(rootKlass);
//    }
//
//    public RuleBlock<?> removeRuleBlock(final Class<?> rootKlass) {
//	return entityRules.remove(rootKlass);
//    }
//
//    public void putExistingRuleBlock(final Class<?> rootKlass, final RuleBlock<?> ruleBlock) {
//	if (!entityRules.containsKey(rootKlass)) {
//	    entityRules.put(rootKlass, ruleBlock);
//	}
//    }
//
//    public void clearEntityRules() {
//	entityRules.clear();
//    }
//
//    public Pair<List<TreePath>, List<TreePath>> synchronizeAll() {
//	final List<TreePath> conditionedPaths = new ArrayList<TreePath>();
//	final List<TreePath> fetchingPaths = new ArrayList<TreePath>();
//	for (int i = 0; i < getNodeLayer().getChildrenCount(); i++) {
//	    if (getNodeLayer().getChild(i) instanceof RuleBlock) {
//		conditionedPaths.addAll(getComponentsActivator().synchronize((RuleBlock<?>) getNodeLayer().getChild(i), this, PropertyType.CONDITIONED));
//		fetchingPaths.addAll(getComponentsActivator().synchronize((RuleBlock<?>) getNodeLayer().getChild(i), this, PropertyType.FETCHING));
//	    }
//	}
//	return new Pair<List<TreePath>, List<TreePath>>(conditionedPaths, fetchingPaths);
//    }
//
//    public Pair<List<TreePath>, List<TreePath>> synchronizeAll(final List<RuleBlock<?>> ruleBlocks) {
//	final List<TreePath> conditionedPaths = new ArrayList<TreePath>();
//	final List<TreePath> fetchingPaths = new ArrayList<TreePath>();
//	for (final RuleBlock<?> ruleBlock : ruleBlocks) {
//	    conditionedPaths.addAll(getComponentsActivator().synchronize(ruleBlock, this, PropertyType.CONDITIONED));
//	    fetchingPaths.addAll(getComponentsActivator().synchronize(ruleBlock, this, PropertyType.FETCHING));
//	}
//	return new Pair<List<TreePath>, List<TreePath>>(conditionedPaths, fetchingPaths);
//    }
//
//}
