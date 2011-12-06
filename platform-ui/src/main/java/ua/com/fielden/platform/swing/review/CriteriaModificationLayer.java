package ua.com.fielden.platform.swing.review;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.geom.Rectangle2D;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComponent;
import javax.swing.JMenu;
import javax.swing.JPopupMenu;
import javax.swing.JSeparator;

import org.apache.commons.lang.StringUtils;
import org.jdesktop.jxlayer.JXLayer;
import org.jdesktop.jxlayer.plaf.AbstractLayerUI;

import ua.com.fielden.platform.entity.annotation.CritOnly;
import ua.com.fielden.platform.entity.annotation.CritOnly.Type;
import ua.com.fielden.platform.error.Result;
import ua.com.fielden.platform.reflection.AnnotationReflector;
import ua.com.fielden.platform.reflection.EntityDescriptor;
import ua.com.fielden.platform.swing.actions.Command;
import ua.com.fielden.platform.swing.components.bind.BoundedValidationLayer;
import ua.com.fielden.platform.swing.components.bind.ComponentFactory.IOnCommitAction;
import ua.com.fielden.platform.swing.ei.editors.IPropertyEditor;
import ua.com.fielden.platform.swing.ei.editors.RangePropertyEditor;
import ua.com.fielden.platform.swing.utils.Utils2D;
import ua.com.fielden.platform.utils.Pair;
import ua.com.fielden.snappy.DateRangePrefixEnum;
import ua.com.fielden.snappy.MnemonicEnum;

/**
 * Represents a high-level {@link IPropertyEditor}'s "editor" wrapper which controls and displays criteria modifications such as:
 * <p>
 * 1. criteria negation;
 * <p>
 * 2. "missing value" modification, which means "criterion or is NULL" if "criterion" is not empty and simply "is NULL" otherwise;
 * <p>
 * 3. property boundary exclusiveness;
 * <p>
 * 4. date mnemonics;
 * <p>
 * 5. ALL/ANY collectional criteria.
 *
 * @author TG Team
 *
 */
public class CriteriaModificationLayer extends JXLayer<JComponent> implements ItemListener {
    private static final long serialVersionUID = -9123603131112707930L;

    private final static String MISSING_VALUE = "Missing value", NOT = "Not", EXCLUSIVE = "Exclusive", ALL = "All";

    private final BoundedValidationLayer<?> leftValidationLayer, rightValidationLayer;
    private final String conventionalPropertyName;
    private final DynamicEntityQueryCriteria<?, ?> deqc;
    private final IPropertyEditor propertyEditor;

    // menu related items:
    private final JPopupMenu popup;
    private final JCheckBoxMenuItem nullMenuItem, notMenuItem, fromExclusiveMenuItem, toExclusiveMenuItem, allMenuItem;
    private final JSeparator dateMenusSeparator;
    private final JMenu prevMenu, currMenu, nextMenu;
    private final Map<DateState, DateCheckBoxMenuItem> dateStateItems;

    ////////////////////////// state related fields: //////////////////////////
    private Boolean fromExclusive = null, toExclusive = null, isInFromEditorHierarchy = null;
    private Boolean all = null;
    private Boolean not = null;
    private Boolean orNull = null;
    private final DateState dateState = new DateState();

    /**
     * A class representing a date mnemonic state, for e.g. "Previous + Day + AndAfter" or "Current + Month".
     *
     * @author TG Team
     *
     */
    private class DateState {
	private DateRangePrefixEnum datePrefix = null;
	private MnemonicEnum dateMnemonic = null;
	/**
	 * "Null" means [a; b) mnemonic interval, <code>true</code> means [-infinity; b) and <code>false</code> means [a; +infinity)
	 */
	private Boolean andBefore = null;

	public DateState() {
	}

	public DateState(final DateRangePrefixEnum datePrefix, final MnemonicEnum dateMnemonic, final Boolean andBefore) {
	    this.datePrefix = datePrefix;
	    this.dateMnemonic = dateMnemonic;
	    this.andBefore = andBefore;
	}

	public DateState(final DateState dateState) {
	    this(dateState.getDatePrefix(), dateState.getDateMnemonic(), dateState.getAndBefore());
	}

	public DateRangePrefixEnum getDatePrefix() {
	    return datePrefix;
	}
	public void setDatePrefix(final DateRangePrefixEnum datePrefix) {
	    this.datePrefix = datePrefix;
	}
	public MnemonicEnum getDateMnemonic() {
	    return dateMnemonic;
	}
	public void setDateMnemonic(final MnemonicEnum dateMnemonic) {
	    this.dateMnemonic = dateMnemonic;
	}
	/** "Null" means [a; b) mnemonic interval, <code>true</code> means [-infinity; b) and <code>false</code> means [a; +infinity). */
	public Boolean getAndBefore() {
	    return andBefore;
	}
	public void setAndBefore(final Boolean andBefore) {
	    this.andBefore = andBefore;
	}

	/**
	 * Returns <code>true</code> when no mnemonic has been chosen for this date state, <code>false</code> otherwise.
	 * <br><br>
	 * Throws {@link IllegalStateException} if date state is incorrect.
	 *
	 * @return
	 */
	private boolean isEmpty() {
	    if (datePrefix == null && dateMnemonic != null || (datePrefix != null && dateMnemonic == null)) {
		throw new IllegalStateException("Incorrect date state.");
	    }
	    return datePrefix == null && dateMnemonic == null;
	}

	@Override
	public int hashCode() {
	    final int prime = 31;
	    int result = 1;
	    result = prime * result + getOuterType().hashCode();
	    result = prime * result + ((andBefore == null) ? 0 : andBefore.hashCode());
	    result = prime * result + ((dateMnemonic == null) ? 0 : dateMnemonic.hashCode());
	    result = prime * result + ((datePrefix == null) ? 0 : datePrefix.hashCode());
	    return result;
	}

	@Override
	public boolean equals(final Object obj) {
	    if (this == obj)
		return true;
	    if (obj == null)
		return false;
	    if (getClass() != obj.getClass())
		return false;
	    final DateState other = (DateState) obj;
	    if (!getOuterType().equals(other.getOuterType()))
		return false;
	    if (andBefore == null) {
		if (other.andBefore != null)
		    return false;
	    } else if (!andBefore.equals(other.andBefore))
		return false;
	    if (dateMnemonic != other.dateMnemonic)
		return false;
	    if (datePrefix != other.datePrefix)
		return false;
	    return true;
	}

	private CriteriaModificationLayer getOuterType() {
	    return CriteriaModificationLayer.this;
	}
    }

    /**
     * Creates a modification layer for a property editor which represents a complete concept of entity property editor (it could be "range" editor, boolean, single or just simple
     * one).
     *
     * @param propertyEditor
     */
    public CriteriaModificationLayer(final IPropertyEditor propertyEditor) {
	super(propertyEditor.getEditor());
	setUI(new CriteriaModificationUi());

	this.propertyEditor = propertyEditor;

	this.deqc = (DynamicEntityQueryCriteria<?, ?>) this.propertyEditor.getEntity();
	final IPropertyEditor leftPropertyEditor = (this.propertyEditor instanceof RangePropertyEditor) ? ((RangePropertyEditor) this.propertyEditor).getFromEditor()
		: this.propertyEditor;
	final IPropertyEditor rightPropertyEditor = (this.propertyEditor instanceof RangePropertyEditor) ? ((RangePropertyEditor) this.propertyEditor).getToEditor() : null;
	conventionalPropertyName = EntityDescriptor.getPropertyNameWithoutKeyPart(EntityDescriptor.enhanceDynamicCriteriaPropertyEditorKey(leftPropertyEditor.getPropertyName(), deqc.getEntityClass()));
	leftValidationLayer = (BoundedValidationLayer<?>) leftPropertyEditor.getEditor();
	rightValidationLayer = rightPropertyEditor != null ? (BoundedValidationLayer<?>) rightPropertyEditor.getEditor() : null;

	// update an initial state of criteria modification layer:
	final Pair<String, String> propertyNames = deqc.getPropertyNames(conventionalPropertyName);
	final DynamicProperty mainProperty = deqc.getEditableProperty(propertyNames.getKey());
	not = mainProperty.getNot(); // update Not state
	orNull = mainProperty.getOrNull(); // update OrNull state
	dateState.setDatePrefix(mainProperty.getDatePrefix()); // update date prefix
	dateState.setDateMnemonic(mainProperty.getDateMnemonic()); // update date mnemonic
	dateState.setAndBefore(mainProperty.getAndBefore()); // update andBefore
	fromExclusive = mainProperty.getExclusive(); // update left exclusiveness
	if (!StringUtils.isEmpty(propertyNames.getValue()) && deqc.getEditableProperty(propertyNames.getValue()) != null) {
	    final DynamicProperty secondaryProperty = deqc.getEditableProperty(propertyNames.getValue());
	    toExclusive = secondaryProperty.getExclusive(); // update right exclusiveness
	}
	all = mainProperty.getAll(); // update ALL

	// create popup menu and its items and update their initial state:
	popup = new JPopupMenu();

	nullMenuItem = new JCheckBoxMenuItem(MISSING_VALUE);
	nullMenuItem.setMnemonic(KeyEvent.VK_H);
	nullMenuItem.setSelected(Boolean.TRUE.equals(orNull));
	nullMenuItem.addItemListener(this);
	final CritOnly critOnly = AnnotationReflector.getPropertyAnnotation(CritOnly.class, deqc.getEntityClass(), conventionalPropertyName);
	final boolean isCritOnly = critOnly != null;
	final boolean isNotBoolOrCritOnlyDate = !(this.propertyEditor instanceof RangePropertyEditor && ( ((RangePropertyEditor) this.propertyEditor).isBool() || ((RangePropertyEditor) this.propertyEditor).isDate() && isCritOnly));
	if (isNotBoolOrCritOnlyDate) { // all criteria could be altered by emptiness except boolean criteria
	    popup.add(nullMenuItem);
	}
	notMenuItem = new JCheckBoxMenuItem(NOT);
	notMenuItem.setMnemonic(KeyEvent.VK_N);
	notMenuItem.setSelected(Boolean.TRUE.equals(not));
	notMenuItem.addItemListener(this);
	if (isNotBoolOrCritOnlyDate) { // all criteria could be negated except single (TODO should it be?) /boolean criteria
	    popup.add(notMenuItem);
	}
	allMenuItem = new JCheckBoxMenuItem(ALL);
	allMenuItem.setMnemonic(KeyEvent.VK_A);
	allMenuItem.setSelected(Boolean.TRUE.equals(all));
	allMenuItem.addItemListener(this);
	if (mainProperty.isWithinCollectionalHierarchy()) {
	    popup.add(allMenuItem);
	}
	fromExclusiveMenuItem = new JCheckBoxMenuItem(EXCLUSIVE);
	fromExclusiveMenuItem.setMnemonic(KeyEvent.VK_X);
	fromExclusiveMenuItem.setSelected(Boolean.TRUE.equals(fromExclusive));
	fromExclusiveMenuItem.addItemListener(this);
	toExclusiveMenuItem = new JCheckBoxMenuItem(EXCLUSIVE);
	toExclusiveMenuItem.setMnemonic(KeyEvent.VK_X);
	toExclusiveMenuItem.setSelected(Boolean.TRUE.equals(toExclusive));
	toExclusiveMenuItem.addItemListener(this);

	// create date related menu items:
	dateMenusSeparator = new JSeparator();
	prevMenu = new JMenu("Previous");
	currMenu = new JMenu("Current");
	nextMenu = new JMenu("Next");
	dateStateItems = new HashMap<DateState, CriteriaModificationLayer.DateCheckBoxMenuItem>();
	final boolean isDate = this.propertyEditor instanceof RangePropertyEditor && ((RangePropertyEditor) this.propertyEditor).isDate();
	if (isDate) {
	    fillDateStateItems();
	}
	final MouseListener popupShower = new PopupListener();
	final boolean isCritOnlyAndSingle = isCritOnly && Type.SINGLE.equals(critOnly.value());
	if (!isCritOnlyAndSingle) { // no criteria modifications are permitted for Single critOnly properties
	    if (this.propertyEditor instanceof RangePropertyEditor) {
		final RangePropertyEditor rpe = (RangePropertyEditor) this.propertyEditor;
		rpe.getEditor().addMouseListener(popupShower);
		assignTo(popupShower, rpe.getFromEditor(), rpe.getToEditor());
	    } else {
		assignTo(popupShower, this.propertyEditor);
	    }
	}

	// update an initial state to be sure that saved state is correct and to make sure it will be rendered properly
	updateState();
    }


    /**
     * Creates and register a menu structure for [Prefix + Mnemonic] with three sub-items: "Full period item", "And before item" and "And after item".
     *
     * @param prefix
     * @param mnemonic
     * @param mnemonicTitle
     * @param prefixWithMnemonicTitle
     * @return
     */
    private JMenu createDateMnemonicSubMenu(final DateRangePrefixEnum prefix, final MnemonicEnum mnemonic, final String mnemonicTitle, final String prefixWithMnemonicTitle) {
	final JMenu menu = new JMenu(mnemonicTitle);

	final DateCheckBoxMenuItem fullPeriod = new DateCheckBoxMenuItem(new DateState(prefix, mnemonic, null), prefixWithMnemonicTitle);
	menu.add(fullPeriod);
	dateStateItems.put(fullPeriod.getDateState(), fullPeriod);

	final DateCheckBoxMenuItem andBefore = new DateCheckBoxMenuItem(new DateState(prefix, mnemonic, Boolean.TRUE), prefixWithMnemonicTitle + " and before");
	menu.add(andBefore);
	dateStateItems.put(andBefore.getDateState(), andBefore);

	final DateCheckBoxMenuItem andAfter = new DateCheckBoxMenuItem(new DateState(prefix, mnemonic, Boolean.FALSE), prefixWithMnemonicTitle + " and after");
	menu.add(andAfter);
	dateStateItems.put(andAfter.getDateState(), andAfter);

	return menu;
    }

    /**
     * Creates and adds sub-menus (related to date mnemonics, except "DAY") to appropriate <code>groupMenu</code>.
     *
     * @param drpe
     * @param groupMenu
     * @param pref
     */
    private void addNonDayMnemonics(final DateRangePrefixEnum drpe, final JMenu groupMenu, final String pref) {
	groupMenu.add(createDateMnemonicSubMenu(drpe, MnemonicEnum.WEEK, "Week", pref + " week"));
	groupMenu.add(createDateMnemonicSubMenu(drpe, MnemonicEnum.MONTH, "Month", pref + " month"));
	groupMenu.add(createDateMnemonicSubMenu(drpe, MnemonicEnum.QRT1, "1-st quarter", pref + " year's 1-st quarter"));
	groupMenu.add(createDateMnemonicSubMenu(drpe, MnemonicEnum.QRT2, "2-nd quarter", pref + " year's 2-nd quarter"));
	groupMenu.add(createDateMnemonicSubMenu(drpe, MnemonicEnum.QRT3, "3-rd quarter", pref + " year's 3-rd quarter"));
	groupMenu.add(createDateMnemonicSubMenu(drpe, MnemonicEnum.QRT4, "4-th quarter", pref + " year's 4-th quarter"));
	groupMenu.add(createDateMnemonicSubMenu(drpe, MnemonicEnum.YEAR, "Year", pref + " year"));
	groupMenu.add(createDateMnemonicSubMenu(drpe, MnemonicEnum.OZ_FIN_YEAR, "Financial year", pref + " financial year"));
    }

    /**
     * Creates date group menu sub-items and adds them to appropriate Prev, Curr or Next group menu items.
     *
     * @return
     */
    private void fillDateStateItems() {
	// PREV
	prevMenu.add(createDateMnemonicSubMenu(DateRangePrefixEnum.PREV, MnemonicEnum.DAY, "Day", "Yesterday"));
	addNonDayMnemonics(DateRangePrefixEnum.PREV, prevMenu, "Previous");

	// CURR
	currMenu.add(createDateMnemonicSubMenu(DateRangePrefixEnum.CURR, MnemonicEnum.DAY, "Day", "Today"));
	addNonDayMnemonics(DateRangePrefixEnum.CURR, currMenu, "Current");

	// NEXT
	nextMenu.add(createDateMnemonicSubMenu(DateRangePrefixEnum.NEXT, MnemonicEnum.DAY, "Day", "Tomorrow"));
	addNonDayMnemonics(DateRangePrefixEnum.NEXT, nextMenu, "Next");
    }

    /**
     * A {@link JCheckBoxMenuItem} for date editor's range state modification.
     *
     * @author TG Team
     *
     */
    private class DateCheckBoxMenuItem extends JCheckBoxMenuItem {
	private static final long serialVersionUID = 4121216244336230700L;
	private final DateState dateState;
	private final String stateCaption;

	public DateCheckBoxMenuItem(final DateState dateState, final String stateCaption) {
	    super(stateCaption);
	    if (dateState == null || dateState.getDatePrefix() == null || dateState.getDateMnemonic() == null) {
		throw new RuntimeException("Incorrect date state [" + dateState + "] for date mnemonic menu item.");
	    }
	    this.dateState = dateState;

	    this.stateCaption = stateCaption;

	    // this.setMnemonic(KeyEvent.VK_X);
	    // update state of item according to initial CML state:
	    this.setSelected(this.dateState.equals(CriteriaModificationLayer.this.dateState));
	    // add a listener for this item:
	    this.addItemListener(CriteriaModificationLayer.this);
	}

	public DateState getDateState() {
	    return dateState;
	}

	public String getStateCaption() {
	    return stateCaption;
	}
    }

    /** Assigns popupShower to a list of {@link BoundedValidationLayer}s. */
    private void assignTo(final MouseListener popupShower, final IPropertyEditor... editors) {
	for (final IPropertyEditor editor : editors) {
	    final BoundedValidationLayer<?> bvl = (BoundedValidationLayer<?>) editor.getEditor();
	    // add "after commit" property change listener to update state of this crit-modif layer:
	    bvl.addOnCommitAction(new IOnCommitAction() {
		@Override
		public void postSuccessfulCommitAction() {
		}

		@Override
		public void postCommitAction() {
		    updateState();
		}

		@Override
		public void postNotSuccessfulCommitAction() {
		}
	    });
	    deqc.getChangeSupport().addPropertyChangeListener(DynamicEntityQueryCriteria.DEFAULT + editor.getPropertyName(), new PropertyChangeListener() {
		@Override
		public void propertyChange(final PropertyChangeEvent evt) {
		    // "default" crit-modif state, after the value has been defaulted:
		    notMenuItem.setSelected(false);
		    nullMenuItem.setSelected(false);

		    // "default" (remove) date specific mnemonics, after the value has been defaulted:
		    setDateState(new DateState());
		}
	    });

	    bvl.addIncapsulatedMouseListener(popupShower);
	}
    }

    /**
     * Updates a state of crit-modif layer according to an ignore state property editor which is wrapped. Also triggers repainting of a component which state has been updated.
     */
    public void updateState() {
	System.out.print("Update state for property = [" + propertyEditor.getPropertyName() + "]. ");
	if (propertyEditor instanceof RangePropertyEditor) {
	    final RangePropertyEditor rpe = (RangePropertyEditor) propertyEditor;
	    if (!rpe.isSingle() && !rpe.isBool()) { // update exclusiveness according to an ignore state of each sub-editor:
		if (rpe.getFromEditor().isIgnored() && Boolean.TRUE.equals(fromExclusive)) {
		    System.out.print("Remove from exclusiveness. ");
		    isInFromEditorHierarchy = true;
		    fromExclusiveMenuItem.setSelected(false);
		    isInFromEditorHierarchy = null;
		}
		if (rpe.getToEditor().isIgnored() && Boolean.TRUE.equals(toExclusive)) {
		    System.out.print("Remove to exclusiveness. ");
		    isInFromEditorHierarchy = false;
		    toExclusiveMenuItem.setSelected(false);
		    isInFromEditorHierarchy = null;
		}
	    }
	}

	if (Boolean.TRUE.equals(not) && isIgnored()) { // update negation according to an ignore state of property editor:
	    System.out.print("Remove negation.");
	    notMenuItem.setSelected(false);
	}

	if (Boolean.TRUE.equals(all) && isIgnored()) { // update ALL according to an ignore state of property editor:
	    System.out.print("Remove ALL parameter.");
	    allMenuItem.setSelected(false);
	}

	// update a colour of exlusiveness
	leftValidationLayer.setColour(Boolean.TRUE.equals(fromExclusive) ? BoundedValidationLayer.WARNING_COLOUR : null);
	if (rightValidationLayer != null) {
	    rightValidationLayer.setColour(Boolean.TRUE.equals(toExclusive) ? BoundedValidationLayer.WARNING_COLOUR : null);
	}

	// trigger repainting:
	repaint();
    }

    /**
     * Returns <code>true</code> if underlying property editor is empty and no date mnemonics have been assigned (in case of date range editor) and "missing value" modification is not applied, otherwise returns <code>false</code>.
     *
     * @return
     */
    public boolean isIgnored(){
	return propertyEditor.isIgnored() && dateState.isEmpty() && !Boolean.TRUE.equals(orNull);
    }

    /**
     * Popup menu invocator.
     *
     * @author TG Team
     *
     */
    private class PopupListener extends MouseAdapter {
	@Override
	public void mousePressed(final MouseEvent e) {
	    maybeShowPopup(e);
	}

	@Override
	public void mouseReleased(final MouseEvent e) {
	    maybeShowPopup(e);
	}

	private void exclusiveAdd(final boolean add, final boolean from) {
	    final JCheckBoxMenuItem mi = from ? fromExclusiveMenuItem : toExclusiveMenuItem;
	    if (add) {
		if (popup.getComponentIndex(mi) < 0) {
		    popup.add(mi);
		}
	    } else {
		if (popup.getComponentIndex(mi) >= 0) {
		    popup.remove(mi);
		}
	    }
	}

	private void addDateMenuItems(final RangePropertyEditor rpe, final boolean add) {
	    if (add) {
		if (popup.getComponentIndex(dateMenusSeparator) < 0) {
		    popup.add(dateMenusSeparator);
		    popup.add(prevMenu);
		    popup.add(currMenu);
		    popup.add(nextMenu);
		}
	    } else {
		if (popup.getComponentIndex(dateMenusSeparator) >= 0) {
		    popup.remove(nextMenu);
		    popup.remove(currMenu);
		    popup.remove(prevMenu);
		    popup.remove(dateMenusSeparator);
		}
	    }
	}

	private void maybeShowPopup(final MouseEvent e) {
	    if (e.isPopupTrigger()) {
		// invoke committing process for all buffered sub-editors before showing popup:
		if (leftValidationLayer.canCommit()) {
		    leftValidationLayer.commit();
		}
		if (rightValidationLayer != null && rightValidationLayer.canCommit()) {
		    rightValidationLayer.commit();
		}
		// invoke a background "waiter" that waits until all committing logic to be completed and then updates/shows popup menu:
		new Command<Result>("Update and invoke popup menu") {
		    private static final long serialVersionUID = -193882193087849022L;

		    @Override
		    protected Result action(final ActionEvent e) throws Exception {
			// invoke a background "waiter" that waits until all committing logic to be completed:
			return deqc.isValid();
		    }

		    @Override
		    protected void postAction(final Result value) {
			super.postAction(value);
			// delete popup menu "date" items:
			if (propertyEditor instanceof RangePropertyEditor) { // only non-single date-criteria could be altered by date mnemonics, not single/boolean criteria.
			    final RangePropertyEditor rpe = (RangePropertyEditor) propertyEditor;
			    if (!rpe.isSingle() && rpe.isDate()) {
				addDateMenuItems(rpe, false);
			    }
			}

			// update popup menu "exclusiveness" items:
			if (propertyEditor instanceof RangePropertyEditor) { // only range-criteria could be altered by boundary exclusiveness, not single/boolean criteria.
			    final RangePropertyEditor rpe = (RangePropertyEditor) propertyEditor;
			    if (!rpe.isSingle() && !rpe.isBool()) {
				isInFromEditorHierarchy = isInFromEditorHierarchy(e.getComponent());
				if (isInFromEditorHierarchy != null) {
				    exclusiveAdd(true, isInFromEditorHierarchy);
				    exclusiveAdd(false, !isInFromEditorHierarchy);
				} else {
				    exclusiveAdd(false, true);
				    exclusiveAdd(false, false);
				}
			    } else {
				exclusiveAdd(false, true);
				exclusiveAdd(false, false);
			    }
			    final boolean mnemonicDeselected = dateState.isEmpty();
			    fromExclusiveMenuItem.setEnabled(!rpe.getFromEditor().isIgnored() && mnemonicDeselected);
			    toExclusiveMenuItem.setEnabled(!rpe.getToEditor().isIgnored() && mnemonicDeselected);
			} else {
			    exclusiveAdd(false, true);
			    exclusiveAdd(false, false);
			}

			// update popup menu "date" items:
			if (propertyEditor instanceof RangePropertyEditor) { // only non-single date-criteria could be altered by date mnemonics, not single/boolean criteria.
			    final RangePropertyEditor rpe = (RangePropertyEditor) propertyEditor;
			    if (!rpe.isSingle() && rpe.isDate()) {
				addDateMenuItems(rpe, true);
			    } else {
				addDateMenuItems(rpe, false);
			    }
			}

			// update popup menu "negation" item:
			notMenuItem.setEnabled(!isIgnored());
			// update popup menu "all" item:
			allMenuItem.setEnabled(!isIgnored());
			// invoke popup menu:
			popup.show(e.getComponent(), e.getX(), e.getY());
		    }
		}.actionPerformed(null);
	    }
	}

	private Boolean isInFromEditorHierarchy(final Component component) {
	    final RangePropertyEditor rpe = (RangePropertyEditor) propertyEditor;

	    Component comp = component;
	    while (comp != null) {
		if (comp.equals(rpe.getFromEditor().getEditor())) {
		    return true;
		} else if (comp.equals(rpe.getToEditor().getEditor())) {
		    return false;
		}
		comp = comp.getParent();
	    }

	    return null;
	}
    }

    @Override
    public void itemStateChanged(final ItemEvent e) {
	final Object source = e.getItemSelectable();
	if (source == nullMenuItem) {
	    setOrNull(e.getStateChange() == ItemEvent.SELECTED);
	} else if (source == notMenuItem) {
	    setNot(e.getStateChange() == ItemEvent.SELECTED);
	} else if (source == allMenuItem) {
	    setAll(e.getStateChange() == ItemEvent.SELECTED);
	} else if (source == fromExclusiveMenuItem || source == toExclusiveMenuItem) {
	    setExclusive(e.getStateChange() == ItemEvent.SELECTED);
	} else if (source instanceof DateCheckBoxMenuItem) {
	    final DateCheckBoxMenuItem dcbmi = (DateCheckBoxMenuItem) source;
	    setDateState((e.getStateChange() == ItemEvent.SELECTED) ? dcbmi.getDateState() : new DateState());
	}
    }

    /**
     * Sets date mnemonic (prefix + mnemonic, e.g. PREV+MONTH) for this "date" crit-modif layer.
     *
     * @param newDatePrefix
     * @param newDateMnemonic
     */
    private void setDateState(final DateState newDateState) {
	final boolean wasAll = Boolean.TRUE.equals(all);
	final boolean wasNot = Boolean.TRUE.equals(not);
	final boolean wasOrNull = Boolean.TRUE.equals(orNull);
	if (!dateState.isEmpty()) { // unselect previously selected item:
	    dateStateItems.get(dateState).setSelected(false);
	}

	dateState.setDatePrefix(newDateState.getDatePrefix());
	dateState.setDateMnemonic(newDateState.getDateMnemonic());
	dateState.setAndBefore(newDateState.getAndBefore());

	if (wasOrNull && !dateState.isEmpty()){
	    nullMenuItem.setSelected(true);
	}
	if (wasNot && !dateState.isEmpty()){
	    notMenuItem.setSelected(true);
	}
	if (wasAll && !dateState.isEmpty()) {
	    allMenuItem.setSelected(true);
	}

	updateDynamicPropertiesState();
	// update state according to editor emptiness:
	updateState();
    }

    /**
     * Sets an "exclusiveness" for currently selected boundary => "from" or "to".
     *
     * @param isExclusive
     */
    private void setExclusive(final boolean isExclusive) {
	if (isInFromEditorHierarchy != null && isInFromEditorHierarchy) {
	    fromExclusive = isExclusive;
	    updateDynamicPropertiesState();
	    // update state according to editor emptiness:
	    updateState();
	} else if (isInFromEditorHierarchy != null && !isInFromEditorHierarchy) {
	    toExclusive = isExclusive;
	    updateDynamicPropertiesState();
	    // update state according to editor emptiness:
	    updateState();
	} else {
	    throw new RuntimeException("Could not alter exclusiveness for undefined part of range.");
	}
    }

    /**
     * Sets an "Or null" parameter for crit-modif layer.
     *
     * @param isNot
     */
    private void setOrNull(final boolean isOrNull) {
	// if originally state was null (e.g. has been retrieved from persistent criteria) change it to OrNull = false state.
	if (orNull == null) {
	    orNull = false;
	}
	// update orNull parameter:
	orNull = isOrNull;
	updateDynamicPropertiesState();
	// update state according to editor emptiness:
	updateState();
    }

    /**
     * Sets an "ALL" parameter for crit-modif layer.
     *
     * @param isNot
     */
    private void setAll(final boolean isAll) {
	// if originally state was null (e.g. has been retrieved from persistent criteria) change it to ALL = false state.
	if (all == null) {
	    all = false;
	}
	// update ALL parameter:
	all = isAll;
	updateDynamicPropertiesState();
	// update state according to editor emptiness:
	updateState();
    }

    /**
     * Sets a "negation" for crit-modif layer.
     *
     * @param isNot
     */
    private void setNot(final boolean isNot) {
	// if originally state was null (e.g. has been retrieved from persistent criteria) change it to OrNull = false state.
	if (not == null) {
	    not = false;
	}
	// update orNull parameter:
	not = isNot;
	updateDynamicPropertiesState();
	// update state according to editor emptiness:
	updateState();
    }

    /**
     * Updates a {@link DynamicProperty}s corresponding to high-level criteria modification layer with emptiness/negation/exclusiveness/dateValue.
     */
    private void updateDynamicPropertiesState() {
	final Pair<String, String> propertyNames = deqc.getPropertyNames(conventionalPropertyName);
	final DynamicProperty mainProperty = deqc.getEditableProperty(propertyNames.getKey());
	mainProperty.setNot(not); // update Not state
	mainProperty.setOrNull(orNull); // update "or null" parameter
	mainProperty.setExclusive(fromExclusive); // update exclusiveness
	mainProperty.setDatePrefix(dateState.getDatePrefix()); // update date prefix
	mainProperty.setDateMnemonic(dateState.getDateMnemonic()); // update date mnemonic
	mainProperty.setAndBefore(dateState.getAndBefore()); // update andBefore
	mainProperty.setAll(all); // update ALL parameter
	if (!StringUtils.isEmpty(propertyNames.getValue()) && deqc.getEditableProperty(propertyNames.getValue()) != null) {
	    final DynamicProperty secondaryProperty = deqc.getEditableProperty(propertyNames.getValue());
	    secondaryProperty.setNot(not); // update state
	    secondaryProperty.setOrNull(orNull); // update "or null" parameter
	    secondaryProperty.setExclusive(toExclusive); // update exclusiveness
	    secondaryProperty.setDatePrefix(dateState.getDatePrefix()); // update date prefix
	    secondaryProperty.setDateMnemonic(dateState.getDateMnemonic()); // update date mnemonic
	    secondaryProperty.setAndBefore(dateState.getAndBefore()); // update andBefore
	    secondaryProperty.setAll(all); // update ALL parameter
	}
    }

    /**
     * Provides painting logic.
     *
     * @author TG Team
     *
     */
    private class CriteriaModificationUi extends AbstractLayerUI<JComponent> {
	private CriteriaModificationUi() {
	}

	/**
	 * Paints the state.
	 */
	@Override
	protected void paintLayer(final Graphics2D g2, final JXLayer<JComponent> layer) {
	    super.paintLayer(g2, layer); // this paints layer as is

	    final Color transparentWhite = new Color(255, 255, 255, 240);
	    final Color moreTransparentWhite = new Color(255, 255, 255, 100);
//	    leftValidationLayer.setColour(Boolean.TRUE.equals(fromExclusive) ? BoundedValidationLayer.WARNING_COLOUR : null);
//	    if (rightValidationLayer != null) {
//		rightValidationLayer.setColour(Boolean.TRUE.equals(toExclusive) ? BoundedValidationLayer.WARNING_COLOUR : null);
//	    }
	    if (!dateState.isEmpty()){
		drawText(g2, layer, transparentWhite, dateStateItems.get(dateState).getStateCaption());
	    }
	    if (Boolean.TRUE.equals(orNull)) {
		paintColour(g2, BoundedValidationLayer.WARNING_COLOUR, layer.getView());
	    }
	    if (Boolean.TRUE.equals(not)) {
		paintColour(g2, moreTransparentWhite, layer.getView());
		g2.setColor(new Color(115, 164, 209));
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g2.drawLine(0, layer.getHeight(), layer.getWidth(), 0);
	    }
	    // paint collectional ALL criteria as green layer above all other criteria modifications
	    if (Boolean.TRUE.equals(all)) {
		paintColour(g2, new Color(0, 255, 0, 50), layer.getView());
	    }
	}

	private void drawText(final Graphics2D g2, final JXLayer<JComponent> layer, final Color backgroundColor, final String text) {
	    final JComponent component = layer.getView();
	    paintColour(g2, backgroundColor, component);

	    final int leftInset = leftValidationLayer.getIncapsulatedInsets().left;
	    // define how many characters in the caption can be drawn
	    final int actualInsets = leftInset - component.getInsets().left;

//	    // define how many characters in the caption can be drawn
//	    final FontMetrics fm = g2.getFontMetrics();
//	    Rectangle2D textBounds = fm.getStringBounds(text, g2);
//	    int count = text.length();
//	    while ((textBounds.getWidth() > fitToWidth) && (count > 4)) {
//		textBounds = fm.getStringBounds(text.substring(0, count--), g2);
//	    }
//	    return count == text.length() ? text : StringUtils.abbreviate(text, count);

	    final String textToDisplay = Utils2D.abbreviate(g2, text, component.getSize().width - actualInsets);

	    // paint the caption
	    g2.setColor(new Color(0f, 0f, 0f, 1.0f));
	    g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB);

	    final double xPos = leftInset;
	    final Rectangle2D textBounds = g2.getFontMetrics().getStringBounds(textToDisplay, g2);
	    final int h = component.getSize().height;
	    final double yPos = (h - textBounds.getHeight()) / 2. + g2.getFontMetrics().getAscent();
	    g2.drawString(textToDisplay, (float) xPos, (float) yPos);
	}

	private void paintColour(final Graphics2D g2, final Color backgroundColor, final JComponent component) {
	    g2.setColor(backgroundColor);
	    final int w = component.getSize().width - (component.getInsets().left + component.getInsets().right);
	    final int h = component.getSize().height - (component.getInsets().top + component.getInsets().bottom);
	    g2.fillRect(component.getInsets().left, component.getInsets().top, w, h);
	}

    }

}
