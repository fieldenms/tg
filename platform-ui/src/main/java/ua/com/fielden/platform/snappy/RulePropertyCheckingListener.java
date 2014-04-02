package ua.com.fielden.platform.snappy;

//import it.cnr.imaa.essi.lablib.gui.checkboxtree.TreeCheckingEvent;
//import it.cnr.imaa.essi.lablib.gui.checkboxtree.TreeCheckingListener;
//
//import java.awt.Color;
//import java.awt.event.ActionEvent;
//import java.lang.reflect.Field;
//import java.util.Arrays;
//import java.util.Date;
//import java.util.List;
//
//import javax.swing.Action;
//import javax.swing.JTabbedPane;
//import javax.swing.tree.DefaultMutableTreeNode;
//import javax.swing.tree.TreePath;
//
//import ua.com.fielden.platform.entity.AbstractEntity;
//import ua.com.fielden.platform.reflection.AnnotationReflector;
//import ua.com.fielden.platform.reflection.Finder;
//import ua.com.fielden.platform.reflection.PropertyTypeDeterminator;
//import ua.com.fielden.platform.reflection.TitlesDescsGetter;
//import ua.com.fielden.platform.swing.actions.BlockingCommand;
//import ua.com.fielden.platform.swing.components.blocking.BlockingIndefiniteProgressPane;
//import ua.com.fielden.platform.treemodel.EntitiesTreeModel.TitledObject;
//import ua.com.fielden.platform.types.Money;
//import ua.com.fielden.snappy.model.Type;
//import ua.com.fielden.snappy.query.ConditionedHotProcessor;
//import ua.com.fielden.snappy.storing.ApplicationModel;
//import ua.com.fielden.snappy.view.block.BlockNode;
//import ua.com.fielden.snappy.view.block.SegmentedExpandableBlock;
//import ua.com.fielden.snappy.view.block.Slot;
//import ua.com.fielden.snappy.view.blocks.properties.CollectionPropertyHotBlock;
//import ua.com.fielden.snappy.view.blocks.properties.PropertyBlock;
//import ua.com.fielden.snappy.view.blocks.properties.PropertyBlock.SlotForPropertyBlock;
//import ua.com.fielden.snappy.view.blocks.properties.PropertyHotBlock;
//import ua.com.fielden.snappy.view.blocks.properties.PropertyNamed;
//import ua.com.fielden.snappy.view.blocks.toplevel.ConditionedHotBlock;
//import ua.com.fielden.snappy.view.blocks.toplevel.RuleBlock;

/**
 * This listener used to </br> add/remove the blocks corresponding to added/removed checking path (immediate synchronization between rule blocks and entities tree).
 * 
 * TODO Snappy integration logic has been commented until snappy related stuff will be migrated to TG platform. TODO Snappy integration logic has been commented until snappy
 * related stuff will be migrated to TG platform. TODO Snappy integration logic has been commented until snappy related stuff will be migrated to TG platform. TODO Snappy
 * integration logic has been commented until snappy related stuff will be migrated to TG platform. TODO Snappy integration logic has been commented until snappy related stuff will
 * be migrated to TG platform. TODO Snappy integration logic has been commented until snappy related stuff will be migrated to TG platform. TODO Snappy integration logic has been
 * commented until snappy related stuff will be migrated to TG platform. TODO Snappy integration logic has been commented until snappy related stuff will be migrated to TG
 * platform. TODO Snappy integration logic has been commented until snappy related stuff will be migrated to TG platform. TODO Snappy integration logic has been commented until
 * snappy related stuff will be migrated to TG platform. TODO Snappy integration logic has been commented until snappy related stuff will be migrated to TG platform. TODO Snappy
 * integration logic has been commented until snappy related stuff will be migrated to TG platform. TODO Snappy integration logic has been commented until snappy related stuff will
 * be migrated to TG platform. TODO Snappy integration logic has been commented until snappy related stuff will be migrated to TG platform. TODO Snappy integration logic has been
 * commented until snappy related stuff will be migrated to TG platform. TODO Snappy integration logic has been commented until snappy related stuff will be migrated to TG
 * platform. TODO Snappy integration logic has been commented until snappy related stuff will be migrated to TG platform.
 * 
 * @author Jhou
 * 
 */
public class RulePropertyCheckingListener {
}
//implements TreeCheckingListener {
//    private final Color keyColor = new Color(132, 132, 201);
//    private final TgSnappyApplicationModel tgApplicationModel;
//    protected final JTabbedPane rulesTabbedPane;
//
//    public RulePropertyCheckingListener(final JTabbedPane rulesTabbedPane, final TgSnappyApplicationModel tgApplicationModel) {
//	this.tgApplicationModel = tgApplicationModel;
//	this.rulesTabbedPane = rulesTabbedPane;
//    }
//
//    /**
//     * Represents enumeration for snappy property types.
//     *
//     * @author Jhou
//     *
//     */
//    public enum PropertyType {
//	/**
//	 * Indicates property that could represent some constraint - could have some condition.
//	 */
//	CONDITIONED,
//
//	/**
//	 * Indicates invisible property that marks parent hot property as one to be fetched. No condition could be applied - it is just auxiliary marker.
//	 */
//	FETCHING
//    }
//
//    /**
//     * Adds/removes the blocks corresponding to added/removed checking path (immediate synchronization between rule blocks and entities tree).
//     */
//    @Override
//    public void valueChanged(final TreeCheckingEvent e) {
//	if (e.isCheckedPath()) {
//	    addPathBlocks(e.getPath(), PropertyType.CONDITIONED);
//	} else {
//	    removePathBlocks(e.getPath(), PropertyType.CONDITIONED);
//	}
//    }
//
//    private void synchronizeRootRuleBlockAndTab(final TreePath path) {
//	final Class<? extends AbstractEntity<?>> rootKlass = (Class<? extends AbstractEntity<?>>) ((TitledObject) (((DefaultMutableTreeNode) path.getPathComponent(1)).getUserObject())).getObject();
//	final RuleBlock<?> rootRuleBlock = tgApplicationModel.getRuleBlock(rootKlass);
//	final String tabCaption = TitlesDescsGetter.getEntityTitleAndDesc(rootKlass).getKey() + ApplicationModel.RULE_NAME_SUFFIX;
//	final int rootTabIndex = TgSnappyComponentsActivator.findRuleTabIndex(rulesTabbedPane, tabCaption);
//	if (rootRuleBlock == null) {
//	    throw new RuntimeException("rootRuleBlock == null.");
//	}
//	if (isEmpty(rootRuleBlock)) {
//	    tgApplicationModel.removeRuleBlock(rootKlass);
//	    rootRuleBlock.removeFromParent();
//	    if (rootTabIndex >= 0) {
//		rulesTabbedPane.removeTabAt(rootTabIndex);
//	    }
//	} else {
//	    if (rootTabIndex < 0) {
//		rulesTabbedPane.addTab(tabCaption, new RuleResultsTab(rootKlass, getTgApplicationModel()));
//	    }
//	}
//    }
//
//    private boolean isEmpty(final RuleBlock<?> ruleBlock) {
//	if (ruleBlock.slots().get(0).isEmpty() || ((ConditionedHotBlock<?>) ruleBlock.slots().get(0).block()).slots().size() <= 1) {
//	    return true;
//	}
//	return false;
//    }
//
//    /**
//     * Removes the block tree corresponding to the <code>path</code>. For now the added "entity" does not removes from the view.
//     *
//     * @param path
//     */
//    protected void removePathBlocks(final TreePath path, final PropertyType propertyType) {
//	if (path.getPathCount() <= 2) { // ["Entities", klass] or ["Entities"] path
//	    return;
//	}
//	new SleepingBlockingCommand("Synchronization...", tgApplicationModel.getComponentsActivator().getBlockingIndefiniteProgressPane()) {
//	    private static final long serialVersionUID = 985541110851542292L;
//
//	    @Override
//	    protected void postAction(final Boolean value) {
//		final Class<?> rootKlass = (Class<?>) ((TitledObject) (((DefaultMutableTreeNode) path.getPathComponent(1)).getUserObject())).getObject();
//		final RuleBlock<?> rootRuleBlock = tgApplicationModel.getOrCreateRuleBlock(rootKlass);
//
//		final BlockNode<?> blockToRemove = findCorrespondingBlock(rootKlass, (SegmentedExpandableBlock<?>) rootRuleBlock.slots().get(0).block(), Arrays.copyOfRange(path.getPath(), 2, path.getPath().length), propertyType);
//
//		if (blockToRemove instanceof PropertyBlock<?>) {
//		    final BlockNode<?> parent = blockToRemove.getSlot().parent();
//		    blockToRemove.removeItself();
//		    if (parent instanceof PropertyHotBlock<?>) {
//			((PropertyHotBlock<?>) parent).removeEmptyAndTraverseUp();
//		    }
//		} else if (blockToRemove instanceof PropertyHotBlock<?>) {
//		    ((PropertyHotBlock<?>) blockToRemove).setFetching(false);
//		    ((PropertyHotBlock<?>) blockToRemove).removeEmptyAndTraverseUp();
//		}
//		rootRuleBlock.reshapeAllHierarchy();
//
//		synchronizeRootRuleBlockAndTab(path);
//		super.postAction(value);
//	    }
//	}.actionPerformed(null);
//    }
//
//    /**
//     * Finds the property block corresponding to specified path of the propertyNames (like "Advice.advicePositions.position") in specified "hot" block. The property should exist
//     * inside "rootClass" hierarchy.
//     *
//     * @param rootKlass
//     * @param hot
//     * @param propertyNamePath
//     * @return
//     */
//    private BlockNode<?> findCorrespondingBlock(final Class<?> rootKlass, final SegmentedExpandableBlock<?> hot, final Object[] propertyNamePath, final PropertyType propertyType) {
//	final String propertyName = (String) ((TitledObject) (((DefaultMutableTreeNode) propertyNamePath[0]).getUserObject())).getObject();
//	return (propertyNamePath.length == 1) //
//	/*    */? findPropertyBlock(hot, propertyName, propertyType) //
//		: findCorrespondingBlock(rootKlass, (SegmentedExpandableBlock<?>) findPropertyBlock(hot, propertyName, propertyType), Arrays.copyOfRange(propertyNamePath, 1, propertyNamePath.length), propertyType);
//    }
//
//    /**
//     * Finds the direct child block of<code>parentHot</code> with the specified <code>propertyName</code> if exists.
//     *
//     * @param parentHot
//     * @param propertyName
//     * @return
//     *
//     */
//    private BlockNode<?> findPropertyBlock(final SegmentedExpandableBlock<?> parentHot, final String propertyName, final PropertyType propertyType) {
//	for (final Slot slot : parentHot.slots()) {
//	    if (slot.block() != null && slot.block() instanceof PropertyNamed && ((PropertyNamed) slot.block()).getPropertyName().equals(propertyName)) {
//		final BlockNode<?> block = slot.block();
//		if (PropertyType.CONDITIONED == propertyType) {
//		    if (block instanceof PropertyBlock<?>) {
//			if (!((PropertyBlock<?>) block).isFetchingProperty()) {
//			    return block;
//			}
//		    } else if (block instanceof PropertyHotBlock<?>) {
//			if (!((PropertyHotBlock<?>) block).isPossiblyFetching()) {
//			    return block;
//			}
//		    }
//		} else if (PropertyType.FETCHING == propertyType) {
//		    if (block instanceof PropertyBlock<?>) {
//			if (((PropertyBlock<?>) block).isFetchingProperty()) {
//			    return block;
//			}
//		    } else if (block instanceof PropertyHotBlock<?>) {
//			if (((PropertyHotBlock<?>) block).isPossiblyFetching()) {
//			    return block;
//			}
//		    }
//		}
//	    }
//	}
//	return null;
//    }
//
//    protected class SleepingBlockingCommand extends BlockingCommand<Boolean> {
//	private static final long serialVersionUID = 8262459916306446489L;
//
//	public SleepingBlockingCommand(final String name, final BlockingIndefiniteProgressPane pane) {
//	    super(name, pane);
//	}
//
//	@Override
//	protected boolean preAction() {
//	    setMessage((String) getValue(Action.NAME));
//	    return super.preAction();
//	}
//
//	@Override
//	protected Boolean action(final ActionEvent e) throws Exception {
//	    Thread.sleep(200);
//	    return true;
//	}
//    }
//
//    /**
//     * Adds the block tree corresponding to the <code>path</code>.
//     *
//     * @param path
//     */
//    protected void addPathBlocks(final TreePath path, final PropertyType propertyType) {
//	if (path.getPathCount() <= 1) { // ["Entities"] path
//	    return;
//	}
//	new SleepingBlockingCommand("Synchronization...", tgApplicationModel.getComponentsActivator().getBlockingIndefiniteProgressPane()) {
//	    private static final long serialVersionUID = 985541110851542292L;
//
//	    @Override
//	    protected void postAction(final Boolean value) {
//		final Class<?> rootKlass = ((Class<?>) ((TitledObject) (((DefaultMutableTreeNode) path.getPathComponent(1)).getUserObject())).getObject());
//		final RuleBlock<?> rootRuleBlock = tgApplicationModel.getOrCreateRuleBlock(rootKlass);
//		final String dotNotName = ConditionedHotProcessor.uncapitalize(rootKlass.getSimpleName()) + "."
//			+ createDotNotationName(Arrays.copyOfRange(path.getPath(), 2, path.getPath().length));
//		addSubProperty(rootKlass, rootKlass, (SegmentedExpandableBlock<?>) rootRuleBlock.slots().get(0).block(), Arrays.copyOfRange(path.getPath(), 2, path.getPath().length), dotNotName, propertyType);
//		rootRuleBlock.reshapeAllHierarchy();
//		synchronizeRootRuleBlockAndTab(path);
//		super.postAction(value);
//	    }
//	}.actionPerformed(null);
//    }
//
//    /**
//     * Creates dot-notated name fro list of treePath's nodes. NOTE : sub-properties in the collectional hot tree is ignored!
//     *
//     * @param propertyNamePath
//     * @return
//     */
//    protected static String createDotNotationName(final Object[] propertyNamePath) {
//	if (propertyNamePath.length <= 0) {
//	    return null;
//	}
//	final StringBuilder dotName = new StringBuilder();
//	for (int i = 0; i < propertyNamePath.length; i++) {
//	    final TitledObject titledObject = ((TitledObject) (((DefaultMutableTreeNode) propertyNamePath[i]).getUserObject()));
//	    if (titledObject.isCollectional() && i < propertyNamePath.length - 1) {
//		return null; // !!!
//	    }
//	    final String propertyName = (String) titledObject.getObject();
//	    dotName.append(propertyName);
//	    if (i + 1 < propertyNamePath.length) {
//		dotName.append(".");
//	    }
//	}
//	return dotName.toString();
//    }
//
//    /**
//     * Creates convenient hql alias for sub-property of <code>rootKlass</code> which hierarchy specified in <code>propertyNamePath</code>.
//     *
//     * @param rootKlass
//     * @param propertyNamePath
//     * @return
//     */
//    protected static String createHqlPropertyPath(final Class<?> rootKlass, final Object[] propertyNamePath) {
//	return ConditionedHotProcessor.mutateDotNotationName(ConditionedHotProcessor.uncapitalize(rootKlass.getSimpleName()) + "." + createDotNotationName(propertyNamePath));
//    }
//
//    /**
//     * Adds the blocks corresponding to property path recursively.
//     *
//     * @param parentKlass
//     * @param parentHot
//     * @param propertyNamePath
//     */
//    private void addSubProperty(final Class<?> rootKlass, final Class<?> parentKlass, final SegmentedExpandableBlock<?> parentHot, final Object[] propertyNamePath, final String dotNotationName, final PropertyType propertyType) {
//	if (propertyNamePath.length <= 0) {
//	    return;
//	}
//	final String propertyName = ((String) ((TitledObject) (((DefaultMutableTreeNode) propertyNamePath[0]).getUserObject())).getObject());
//
//	BlockNode<?> existingPropertyBlock = findPropertyBlock(parentHot, propertyName, propertyType);
//	if (existingPropertyBlock == null) {
//	    existingPropertyBlock = createPropertyBlock(rootKlass, parentKlass, propertyName, dotNotationName, propertyType);
//	    final BlockNode<?> block = existingPropertyBlock;
//	    // Append newly created property block:
//	    final Slot slot = new ConditionedHotBlock.SlotWhichAcceptsPropertyBlocks();
//	    parentHot.append(slot);
//	    slot.snapIn(block);
//	}
//	// From now ExistingPropertyBlock is not null, so we can add recursively its subProperty according to "path".
//	if (existingPropertyBlock instanceof PropertyHotBlock) {
//	    final PropertyHotBlock<?> hot = (PropertyHotBlock<?>) existingPropertyBlock;
//	    if (propertyNamePath.length > 1) {
//		addSubProperty(rootKlass, hot.getElementClass(), hot, Arrays.copyOfRange(propertyNamePath, 1, propertyNamePath.length), dotNotationName, propertyType);
//	    } else if (hot.isPossiblyFetching() && PropertyType.FETCHING == propertyType) {
//		hot.setFetching(true);
//	    }
//	}
//    }
//
//    /**
//     * Creates the property block corresponding using the information of "parentClass" and "propertyName".
//     *
//     * @param parentKlass
//     * @param propertyName
//     * @return
//     */
//    private BlockNode<?> createPropertyBlock(final Class<?> rootKlass, final Class<?> parentKlass, final String propertyName, final String dotNotationName, final PropertyType snappyPropertyType) {
//	final List<Field> keys = Finder.getKeyMembers(parentKlass);
//	final Field field = Finder.getFieldByName(parentKlass, propertyName);
//	final String propertyTitle = TitlesDescsGetter.getTitleAndDesc(propertyName, parentKlass).getKey();
//	final String propertyCaption = propertyTitle.isEmpty() ? propertyName : propertyTitle;
//	final String pathOfTitles = dotNotationName.contains("null") ? propertyName
//		: TitlesDescsGetter.getFullTitleAndDesc(dotNotationName.substring(dotNotationName.indexOf('.') + 1), rootKlass).getKey();
//	final Class<?> propertyType = PropertyTypeDeterminator.determineClass(parentKlass, propertyName, true, false);
//	if (AbstractEntity.class.isAssignableFrom(propertyType)) { // hot
//	    final PropertyHotBlock<?> propertyHot = new PropertyHotBlock<Object>(propertyCaption + "(" + propertyType.getSimpleName() + ")", pathOfTitles, propertyName, 0, PropertyType.FETCHING == snappyPropertyType);
//	    propertyHot.setElementClass(propertyType);
//	    propertyHot.updateModel();
//	    if (keys.contains(field) && PropertyType.CONDITIONED == snappyPropertyType) {
//		propertyHot.setBackgroundColor(keyColor);
//	    }
//	    return propertyHot;
//	} else if (java.util.Collection.class.isAssignableFrom(propertyType)) { // hot collection
//	    final Class<?> elementClass = PropertyTypeDeterminator.determineCollectionElementClass(field);
//	    if (AbstractEntity.class.isAssignableFrom(elementClass)) {
//		final CollectionPropertyHotBlock<?> collPropertyHot = new CollectionPropertyHotBlock<Object>(propertyCaption + "(" + elementClass.getSimpleName() + ")", pathOfTitles, propertyName, 0, PropertyType.FETCHING == snappyPropertyType);
//		collPropertyHot.setElementClass(elementClass);
//		collPropertyHot.updateModel();
//		return collPropertyHot;
//	    } else {
//		return null;
//	    }
//	} else { // simple type
//	    final PropertyBlock<?> propertyBlock = new PropertyBlock<Object>(propertyCaption, pathOfTitles, propertyName, ConditionedHotProcessor.mutateDotNotationName(dotNotationName), chooseSnappyType(field, parentKlass), new SlotForPropertyBlock(), true, PropertyType.FETCHING == snappyPropertyType);
//	    if (keys.contains(field) && PropertyType.CONDITIONED == snappyPropertyType) {
//		propertyBlock.setBackgroundColor(keyColor);
//	    }
//	    return propertyBlock;
//	}
//    }
//
//    /**
//     * Chooses appropriate "snappy-driven" type according to specified "field".
//     *
//     * @param field
//     * @param clazz
//     * @return
//     */
//    private static Type chooseSnappyType(final Field field, final Class<?> clazz) {
//	// TODO improve!
//	final Class<?> type = ("key".equals(field.getName())) ? AnnotationReflector.getKeyType(clazz) : field.getType();
//	if (type == null) {
//	    return null;
//	} else if (Date.class.isAssignableFrom(type)) {
//	    return Type.DATE;
//	} else if (String.class.isAssignableFrom(type)) {
//	    return Type.STRING;
//	} else if (Boolean.class.isAssignableFrom(type) || boolean.class.isAssignableFrom(type)) {
//	    return Type.BOOLEAN;
//	} else if (Number.class.isAssignableFrom(type) || Money.class.isAssignableFrom(type)) {
//	    return Type.NUMBER;
//	} else {
//	    return null;
//	}
//    }
//
//    public TgSnappyApplicationModel getTgApplicationModel() {
//	return tgApplicationModel;
//    }
//
//}
