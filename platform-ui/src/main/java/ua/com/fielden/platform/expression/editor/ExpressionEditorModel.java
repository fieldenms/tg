package ua.com.fielden.platform.expression.editor;

import java.awt.event.ActionEvent;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.HashMap;
import java.util.Map;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ButtonGroup;
import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.KeyStroke;

import net.miginfocom.swing.MigLayout;
import ua.com.fielden.platform.basic.IValueMatcher;
import ua.com.fielden.platform.domaintree.impl.CalculatedProperty;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.error.Result;
import ua.com.fielden.platform.expression.IExpressionErrorPosition;
import ua.com.fielden.platform.reflection.Reflector;
import ua.com.fielden.platform.swing.actions.Command;
import ua.com.fielden.platform.swing.components.NotificationLayer.MessageType;
import ua.com.fielden.platform.swing.components.bind.development.BoundedValidationLayer;
import ua.com.fielden.platform.swing.components.bind.development.ComponentFactory;
import ua.com.fielden.platform.swing.components.bind.development.ComponentFactory.EditorCase;
import ua.com.fielden.platform.swing.components.bind.development.ComponentFactory.IOnCommitAction;
import ua.com.fielden.platform.swing.dialogs.DialogWithDetails;
import ua.com.fielden.platform.swing.ei.editors.ILightweightPropertyBinder;
import ua.com.fielden.platform.swing.ei.editors.IPropertyEditor;
import ua.com.fielden.platform.swing.ei.editors.LabelAndTooltipExtractor;
import ua.com.fielden.platform.swing.model.UModel;
import ua.com.fielden.platform.swing.model.UmState;
import ua.com.fielden.platform.swing.utils.DummyBuilder;
import ua.com.fielden.platform.utils.Pair;
import ua.com.fielden.platform.utils.ResourceLoader;

import com.jgoodies.binding.value.Trigger;

/**
 * Model for expression editor.
 *
 * @author TG Team
 *
 */
public class ExpressionEditorModel extends UModel<CalculatedProperty, CalculatedProperty, Object> {

    private final ExpressionPropertyEditor expressionEditor;
    private final OriginationPropertyEditor originationEditor;
    private final IPropertyProvider propertySelectionModel;


    /**
     * Initiates expression editor with specific {@link ExpressionEntity} instance and appropriate {@link ILightweightPropertyBinder}.
     *
     * @param entity - specific expression entity.
     * @param propertyBinder - appropriate property binder that binds entity.
     */
    public ExpressionEditorModel(final CalculatedProperty entity, final ILightweightPropertyBinder<CalculatedProperty> propertyBinder) {
	super(entity, null, propertyBinder, false);
	this.expressionEditor = new ExpressionPropertyEditor(entity);
	this.originationEditor = new OriginationPropertyEditor(entity);
	this.propertySelectionModel = new PropertyProvider();

	//Configuring edit buttons of two property editors: expression editor and origination property editor.
	final ButtonGroup buttonGroup = new ButtonGroup();
	buttonGroup.add(expressionEditor.getEditButton());
	buttonGroup.add(originationEditor.getEditButton());
	expressionEditor.getEditButton().setSelected(true);

	//Configuring property selection model. Added listener that updates one of two editors: expression editor or origination property editor.
	this.propertySelectionModel.addPropertySelectionListener(createPropertySelectionListener());

	final Map<String, IPropertyEditor> editors = new HashMap<String, IPropertyEditor>(getEditors());
	editors.put("contextualExpression", expressionEditor);
	editors.put("originationProperty", originationEditor);
	setEditors(editors);
    }


    /**
     * Returns the associated property selection model.
     *
     * @return
     */
    public final IPropertyProvider getPropertySelectionModel() {
	return propertySelectionModel;
    }

    @Override
    protected void notifyActionStageChange(final ActionStage actionState) {
	// TODO Auto-generated method stub

    }

    @Override
    protected Map<String, IPropertyEditor> buildEditors(final CalculatedProperty entity, final Object controller, final ILightweightPropertyBinder<CalculatedProperty> propertyBinder) {
	return propertyBinder.bind(entity);
    }

    @Override
    protected CalculatedProperty getManagedEntity() {
	return getEntity();
    }

    @Override
    protected Action createNewAction() {
	final Command<Void> action = new Command<Void>("New") {
	    private static final long serialVersionUID = 1L;

	    @Override
	    protected boolean preAction() {
		notifyActionStageChange(ActionStage.NEW_PRE_ACTION);
		setState(UmState.UNDEFINED);
		return super.preAction();
	    }

	    @Override
	    protected Void action(final ActionEvent event) throws Exception {
		notifyActionStageChange(ActionStage.NEW_ACTION);
		return null;
	    }

	    @Override
	    protected void postAction(final Void newEntity) {
		setState(UmState.NEW);
		notifyActionStageChange(ActionStage.NEW_POST_ACTION);
	    }

	    @Override
	    protected void handlePreAndPostActionException(final Throwable ex) {
		super.handlePreAndPostActionException(ex);
		setState(UmState.VIEW);
	    }

	};
	action.setEnabled(true);
	final Icon icon = ResourceLoader.getIcon("images/add.png");
	action.putValue(Action.LARGE_ICON_KEY, icon);
	action.putValue(Action.SHORT_DESCRIPTION, "Create new calculated property");
	action.putValue(Action.MNEMONIC_KEY, KeyEvent.VK_N);
	return action;
    }

    @Override
    protected Action createEditAction() {
	final Command<Void> action = new Command<Void>("Edit") {
	    private static final long serialVersionUID = 1L;

	    @Override
	    protected boolean preAction() {
		notifyActionStageChange(ActionStage.EDIT_PRE_ACTION);
		setState(UmState.UNDEFINED);
		return super.preAction();
	    }

	    @Override
	    protected Void action(final ActionEvent arg0) throws Exception {
		notifyActionStageChange(ActionStage.EDIT_ACTION);
		return null;
	    }

	    @Override
	    protected void postAction(final Void entity) {
		setState(UmState.EDIT);
		notifyActionStageChange(ActionStage.EDIT_POST_ACTION);
	    }

	    @Override
	    protected void handlePreAndPostActionException(final Throwable ex) {
		super.handlePreAndPostActionException(ex);
		setState(UmState.VIEW);
	    }

	};
	action.setEnabled(canEdit());
	final Icon icon = ResourceLoader.getIcon("images/calculator_edit.png");
	action.putValue(Action.LARGE_ICON_KEY, icon);
	action.putValue(Action.SHORT_DESCRIPTION, "Edit selected calculated proeprty");
	action.putValue(Action.MNEMONIC_KEY, KeyEvent.VK_E);
	return action;
    }

    @Override
    protected Action createSaveAction() {
	final Command<Result> action = new Command<Result>("Apply") {
	    private static final long serialVersionUID = 1L;

	    @Override
	    protected boolean preAction() {
		expressionEditor.commitEditorValue();
		notifyActionStageChange(ActionStage.SAVE_PRE_ACTION);
		setState(UmState.UNDEFINED);
		getCancelAction().setEnabled(false);

		lockBlockingLayerIfProvided(true);
		setMessageForBlockingLayerIfProvided("Applying...");

		return super.preAction();
	    }

	    @Override
	    protected Result action(final ActionEvent arg0) throws Exception {
		notifyActionStageChange(ActionStage.SAVE_ACTION);
		try {
		    return getManagedEntity().isValid();
		} catch (final Exception ex) {
		    return new Result(getManagedEntity(), ex);
		}
	    }

	    @Override
	    protected void postAction(final Result result) {
		try {
		    if (result.isSuccessful()) {
			if (getView() != null) {
			    getView().notify("", MessageType.NONE);
			}
			setState(UmState.VIEW);
			notifyActionStageChange(ActionStage.SAVE_POST_ACTION_SUCCESSFUL);
		    } else {
			if (getView() != null) {
			    getView().notify(result.getMessage(), MessageType.ERROR);
			} else {
			    new DialogWithDetails(null, "Apply", result.getEx()).setVisible(true);
			}
			getCancelAction().setEnabled(true);
			super.postAction(result);
			notifyActionStageChange(ActionStage.SAVE_POST_ACTION_FAILED);
		    }
		} finally {
		    lockBlockingLayerIfProvided(false);
		    super.postAction(result);
		}
	    }

	};
	action.setEnabled(true);
	action.putValue(Action.SHORT_DESCRIPTION, "Creates new calculated property");
	action.putValue(Action.MNEMONIC_KEY, KeyEvent.VK_A);
	return action;
    }

    @Override
    protected Action createCancelAction() {
	final Command<Void> action = new Command<Void>("Discard") {
	    private static final long serialVersionUID = 1L;

	    @Override
	    protected boolean preAction() {
		expressionEditor.setText("", false, 0);
		notifyActionStageChange(ActionStage.CANCEL_PRE_ACTION);
		setState(UmState.UNDEFINED);
		getSaveAction().setEnabled(false);
		return super.preAction();
	    }

	    @Override
	    protected Void action(final ActionEvent arg0) throws Exception {
		notifyActionStageChange(ActionStage.CANCEL_ACTION);
		return null;
	    }

	    @Override
	    protected void postAction(final Void entity) {
		if (getView() != null) {
		    getView().notify("", MessageType.NONE);
		}
		setState(UmState.VIEW);
		notifyActionStageChange(ActionStage.CANCEL_POST_ACTION);
		super.postAction(entity);
	    }
	};
	action.setEnabled(true);
	action.putValue(Action.SHORT_DESCRIPTION, "Discards calculated property.");
	action.putValue(Action.MNEMONIC_KEY, KeyEvent.VK_D);
	return action;
    }

    @Override
    protected Action createRefreshAction() {
	return new AbstractAction() {

	    private static final long serialVersionUID = 2827446269276890662L;

	    @Override
	    public void actionPerformed(final ActionEvent e) {
		// TODO Auto-generated method stub

	    }
	};
    }

    @Override
    protected Action createDeleteAction() {
	final Command<Void> action = new Command<Void>("Delete") {
	    private static final long serialVersionUID = 1L;

	    @Override
	    protected boolean preAction() {
		notifyActionStageChange(ActionStage.DELETE_PRE_ACTION);
		setState(UmState.UNDEFINED);
		return super.preAction();
	    }

	    @Override
	    protected Void action(final ActionEvent event) throws Exception {
		notifyActionStageChange(ActionStage.DELETE_ACTION);
		return null;
	    }

	    @Override
	    protected void postAction(final Void entity) {
		setState(UmState.VIEW);
		notifyActionStageChange(ActionStage.DELETE_POST_ACTION);
	    }

	    @Override
	    protected void handlePreAndPostActionException(final Throwable ex) {
		super.handlePreAndPostActionException(ex);
		setState(UmState.VIEW);
	    }

	};
	action.setEnabled(true);
	final Icon icon = ResourceLoader.getIcon("images/delete.png");
	action.putValue(Action.LARGE_ICON_KEY, icon);
	action.putValue(Action.SHORT_DESCRIPTION, "Delete selected calculated property");
	action.putValue(Action.MNEMONIC_KEY, KeyEvent.VK_D);
	return action;
    }



    /**
     * Creates {@link Action} for function buttons.
     *
     * @param function
     * @return
     */
    protected Action getFunctionAction(final String title, final String desc, final String insertionText, final TextInsertionType insertionType, final boolean select, final int selectionStartIndex, final int charNumberToSelect, final int relativeIndex){
	final Action functionAction= new AbstractAction(title) {

	    private static final long serialVersionUID = 8346239807039308077L;

	    @Override
	    public void actionPerformed(final ActionEvent e) {
		expressionEditor.insertText(insertionText, insertionType, select, selectionStartIndex, charNumberToSelect, relativeIndex);
	    }
	};
	functionAction.putValue(Action.SHORT_DESCRIPTION, desc);
	return functionAction;
    }

    /**
     * Creates {@link IPropertySelectionListener} that listens property selection event and adds property name to expression.
     *
     * @return
     */
    private IPropertySelectionListener createPropertySelectionListener(){
	return new IPropertySelectionListener() {

	    @Override
	    public void propertyStateChanged(final String property, final boolean isSelected) {
		if(isSelected){
		    final String textToInsert = Reflector.fromAbsotule2RelativePath(getManagedEntity().getContextPath(), property);
		    if(expressionEditor.isChecked()){
			expressionEditor.insertText(textToInsert, TextInsertionType.REPLACE, true, 0, textToInsert.length(), textToInsert.length());
		    }else if (originationEditor.isChecked()){
			originationEditor.setText(textToInsert, true, textToInsert.length());
		    }
		}
	    }
	};
    }

    private static class ExpressionPropertyEditor implements IPropertyEditor{

	private final JLabel label;
	private final BoundedValidationLayer<JTextField> textEditor;
	private final JToggleButton editButton;
	private final JPanel editor;
	private final Trigger commitTrigger;

	private CalculatedProperty entity;
	private final String propertyName;

	/**
	 * Allows to set manually specified text.
	 */
	private final ManualTextSetter manualTextSetter;

	public ExpressionPropertyEditor(final CalculatedProperty entity){
	    this.entity = entity;
	    this.propertyName = "contextualExpression";
	    entity.getProperty(getPropertyName()).addValidationResultsChangeListener(createExpressionValidationListener());
	    this.commitTrigger = new Trigger();
	    final Pair<String, String> titleAndDesc = LabelAndTooltipExtractor.extract(propertyName, entity.getType());

	    label = DummyBuilder.label(titleAndDesc.getKey());
	    label.setToolTipText(titleAndDesc.getValue());

	    textEditor = ComponentFactory.createTriggeredStringTextField(entity, propertyName, commitTrigger, false, entity.getProperty(propertyName).getDesc());
	    editButton = new JToggleButton(ResourceLoader.getIcon("images/cursor.png"));
	    editor = new JPanel(new MigLayout("fill, insets 0", "[fill, grow]5[]", "[fill, grow]"));
	    editor.add(textEditor);
	    editor.add(editButton);
	    manualTextSetter = new ManualTextSetter(textEditor, false);

	    final JTextField field = textEditor.getView();
	    final String actionName = "Enter action";
	    field.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), actionName);
	    field.getActionMap().put(actionName, createCommiteAction());
	}

	private PropertyChangeListener createExpressionValidationListener() {
	    return new PropertyChangeListener() {

		@Override
		public void propertyChange(final PropertyChangeEvent evt) {
		    final Object  obj = evt.getNewValue();
		    final Result res = obj instanceof Result ? (Result) obj : null;
		    if(res != null && !res.isSuccessful() && res.getEx() instanceof IExpressionErrorPosition){
			final Exception ex = res.getEx();
			final IExpressionErrorPosition errorPosition = (IExpressionErrorPosition) ex;
			manualTextSetter.setCaretPosition(errorPosition.position().intValue());
		    }

		}
	    };

	}

	/**
	 * Returns action that commits this property value.
	 *
	 * @return
	 */
	private Action createCommiteAction() {
	    return new AbstractAction() {

		private static final long serialVersionUID = 7762859719401177647L;

		@Override
		public void actionPerformed(final ActionEvent e) {
		    commitEditorValue();
		}
	    };
	}

	/**
	 * Set the specified text in to the editor.
	 *
	 * @param text
	 * @param select
	 * @param relativeCaretPosition
	 */
	public void setText(final String text, final boolean select, final int relativeCaretPosition){
	    final JTextField field = textEditor.getView();
	    field.selectAll();
	    manualTextSetter.insertText(text, TextInsertionType.REPLACE, select, 0, text.length(), relativeCaretPosition);
	}

	/**
	 * Inserts specified text at the caret position or replaces selected text.
	 *
	 * @param textToInsert - specified text to insert.
	 * @param insertionType
	 * @param select - indicates whether select inserted text or not.
	 */
	public void insertText(final String textToInsert, final TextInsertionType insertionType, final boolean select, final int selectionStartIndex, final int charNumberToSelect, final int relativeCaretPosition){
	    manualTextSetter.insertText(textToInsert, insertionType, select, selectionStartIndex, charNumberToSelect, relativeCaretPosition);
	}

	@Override
	public CalculatedProperty getEntity() {
	    return entity;
	}

	@Override
	public String getPropertyName() {
	    return propertyName;
	}

	@Override
	public void bind(final AbstractEntity<?> entity) {
	    this.entity = (CalculatedProperty)entity;
	    this.textEditor.rebindTo(entity);
	}

	@Override
	public JLabel getLabel() {
	    return label;
	}

	@Override
	public JPanel getEditor() {
	    return editor;
	}

	/**
	 * Returns the toggle button that is associated with this property editor.
	 * 
	 * @return
	 */
	public JToggleButton getEditButton() {
	    return editButton;
	}

	/**
	 * Returns the value that indicates whether this property editor is selected to receive input.
	 * 
	 * @return
	 */
	public boolean isChecked(){
	    return getEditButton().isSelected();
	}

	@Override
	public JPanel getDefaultLayout() {
	    final JPanel panel = new JPanel(new MigLayout("fill, insets 0", "[]5[]", "[c]"));
	    panel.add(label);
	    panel.add(textEditor, "growx");
	    return panel;
	}

	@Override
	public IValueMatcher<?> getValueMatcher() {
	    throw new UnsupportedOperationException("Value matcher are not applicable for ordinary properties.");
	}

	@Override
	public boolean isIgnored() {
	    return false;
	}

	/**
	 * Triggers commit action for this property editor.
	 */
	public void commitEditorValue(){
	    commitTrigger.triggerCommit();
	}
    }

    private static class OriginationPropertyEditor implements IPropertyEditor{

	private final JLabel label;
	private final BoundedValidationLayer<JTextField> textEditor;
	private final JToggleButton editButton;
	private final JPanel editor;

	private CalculatedProperty entity;
	private final String propertyName;

	/**
	 * Allows to set manually specified text.
	 */
	private final ManualTextSetter manualTextSetter;

	public OriginationPropertyEditor(final CalculatedProperty entity){
	    this.entity = entity;
	    this.propertyName = "originationProperty";

	    final Pair<String, String> titleAndDesc = LabelAndTooltipExtractor.extract(propertyName, entity.getType());
	    label = DummyBuilder.label(titleAndDesc.getKey());
	    label.setToolTipText(titleAndDesc.getValue());

	    textEditor = ComponentFactory.createStringTextField(entity, propertyName, true, entity.getProperty(propertyName).getDesc(), EditorCase.MIXED_CASE);
	    editButton = new JToggleButton(ResourceLoader.getIcon("images/cursor.png"));
	    editor = new JPanel(new MigLayout("fill, insets 0", "[fill, grow]5[]", "[fill, grow]"));
	    editor.add(textEditor);
	    editor.add(editButton);
	    manualTextSetter = new ManualTextSetter(textEditor, true);
	}

	/**
	 * Set the specified text in to the editor.
	 *
	 * @param text
	 * @param select
	 * @param relativeCaretPosition
	 */
	public void setText(final String text, final boolean select, final int relativeCaretPosition){
	    final JTextField field = textEditor.getView();
	    field.selectAll();
	    manualTextSetter.insertText(text, TextInsertionType.REPLACE, select, 0, text.length(), relativeCaretPosition);
	}

	/**
	 * Inserts specified text at the caret position or replaces selected text.
	 *
	 * @param textToInsert - specified text to insert.
	 * @param insertionType
	 * @param select - indicates whether select inserted text or not.
	 */
	public void insertText(final String textToInsert, final TextInsertionType insertionType, final boolean select, final int selectionStartIndex, final int charNumberToSelect, final int relativeCaretPosition){
	    manualTextSetter.insertText(textToInsert, insertionType, select, selectionStartIndex, charNumberToSelect, relativeCaretPosition);
	}

	@Override
	public CalculatedProperty getEntity() {
	    return entity;
	}

	@Override
	public String getPropertyName() {
	    return propertyName;
	}

	@Override
	public void bind(final AbstractEntity<?> entity) {
	    this.entity = (CalculatedProperty)entity;
	    this.textEditor.rebindTo(entity);
	}

	@Override
	public JLabel getLabel() {
	    return label;
	}

	@Override
	public JPanel getEditor() {
	    return editor;
	}

	/**
	 * Returns the toggle button that is associated with this property editor.
	 * 
	 * @return
	 */
	public JToggleButton getEditButton() {
	    return editButton;
	}

	/**
	 * Returns the value that indicates whether this property editor is selected to receive input.
	 * 
	 * @return
	 */
	public boolean isChecked(){
	    return getEditButton().isSelected();
	}

	@Override
	public JPanel getDefaultLayout() {
	    final JPanel panel = new JPanel(new MigLayout("fill, insets 0", "[]5[]", "[c]"));
	    panel.add(label);
	    panel.add(textEditor, "growx");
	    return panel;
	}

	@Override
	public IValueMatcher<?> getValueMatcher() {
	    throw new UnsupportedOperationException("Value matcher are not applicable for ordinary properties.");
	}

	@Override
	public boolean isIgnored() {
	    return false;
	}

    }

    /**
     * Wraps the specific {@link BoundedValidationLayer} with text component and provides API to set text manually.
     * 
     * @author TG Team
     *
     */
    private static class ManualTextSetter{

	/**
	 * The wrapped {@link BoundedValidationLayer} with text component.
	 */
	private final BoundedValidationLayer<JTextField> textComponent;

	/**
	 * Determines whether trigger commit after the text was inserted or not.
	 */
	private final boolean triggerCommitOnTextInsertion;

	/**
	 * Temporary holds text to insert in to the expression editor. After the text was inserted, the property value can be set to null.
	 */
	private String textToInsert = null;

	/**
	 * Indicates whether select inserted text or not.
	 */
	private boolean select = false;

	/**
	 * Indexes, those points on to the beginning and on to the end of the place where text should be inserted.
	 */
	private int startIndex, endIndex;

	/**
	 * Determines the selection region to select after insertion.
	 */
	private int selectionStartIndex, charNumberToSelect;

	/**
	 * Relative index of the caret position.
	 */
	private int relativeCaretPosition;

	/**
	 * Determines the operation that must be performed after the {@link #textComponent} gained the input focus.
	 */
	private FocusGainedOperation focusGainedOperation = FocusGainedOperation.NONE;

	/**
	 * Determines the operation that must be performed after the on commit action was performed.
	 */
	private AfterCommitActions afterCommitAction = AfterCommitActions.NONE;

	/**
	 * Initiates this {@link ManualTextSetter} with specified {@link BoundedValidationLayer} instance.
	 * 
	 * @param textComponent
	 */
	public ManualTextSetter(final BoundedValidationLayer<JTextField> textComponent, final boolean triggerCommitOnTextInsertion){
	    this.textComponent = textComponent;
	    this.triggerCommitOnTextInsertion = triggerCommitOnTextInsertion;
	    final JTextField field = textComponent.getView();
	    field.addFocusListener(createExpressionFocusListener(textComponent.getView()));
	    if(triggerCommitOnTextInsertion){
		textComponent.addOnCommitAction(createSelectionOnCommitAction(textComponent.getView()));
	    }
	}

	/**
	 * Set the specified caret position for wrapped text component.
	 * 
	 * @param position
	 */
	public void setCaretPosition(final int position){
	    final JTextField field = textComponent.getView();
	    if(field.hasFocus()){
		field.setCaretPosition(position);
	    }else{
		focusGainedOperation = FocusGainedOperation.CARRET_CONTROL;
		relativeCaretPosition = position;
		field.requestFocusInWindow();
	    }
	}

	/**
	 * Inserts specified text at the caret position or replaces selected text.
	 *
	 * @param textToInsert - specified text to insert.
	 * @param insertionType
	 * @param select - indicates whether select inserted text or not.
	 */
	public void insertText(final String textToInsert, final TextInsertionType insertionType, final boolean select, final int selectionStartIndex, final int charNumberToSelect, final int relativeCaretPosition){
	    final JTextField field = textComponent.getView();
	    if(field.getSelectionStart() == field.getSelectionEnd()){
		startIndex = endIndex = field.getCaretPosition();
		this.textToInsert = textToInsert;
		this.select = TextInsertionType.APPLY == insertionType ? false : select;
		this.selectionStartIndex = selectionStartIndex;
		this.charNumberToSelect = charNumberToSelect;
		this.relativeCaretPosition = relativeCaretPosition;
	    } else {
		this.select = select;
		switch(insertionType){
		case APPLY:
		    this.startIndex = field.getSelectionStart();
		    this.endIndex = field.getSelectionEnd();
		    this.selectionStartIndex = selectionStartIndex;

		    this.textToInsert = textToInsert.substring(0, relativeCaretPosition) //
			    /*              */+ field.getText().substring(startIndex, endIndex) + textToInsert.substring(relativeCaretPosition);
		    this.relativeCaretPosition = this.textToInsert.length();
		    this.charNumberToSelect = field.getText().substring(startIndex, endIndex).length() + charNumberToSelect;
		    break;
		case APPEND:
		    this.startIndex = this.endIndex = field.getCaretPosition();
		    this.textToInsert = textToInsert;
		    this.relativeCaretPosition = relativeCaretPosition;
		    this.selectionStartIndex = selectionStartIndex;
		    this.charNumberToSelect = charNumberToSelect;
		    break;
		case REPLACE:
		    this.startIndex = field.getSelectionStart();
		    this.endIndex = field.getSelectionEnd();
		    this.relativeCaretPosition = relativeCaretPosition;
		    this.textToInsert = textToInsert;
		    this.selectionStartIndex = selectionStartIndex;
		    this.charNumberToSelect = charNumberToSelect;
		    break;
		}
	    }
	    focusGainedOperation = FocusGainedOperation.TEXT_INSERTION;
	    field.requestFocusInWindow();
	}


	private IOnCommitAction createSelectionOnCommitAction(final JTextField textField) {
	    return new IOnCommitAction() {

		@Override
		public void postSuccessfulCommitAction() {
		    //Ignored for now.
		}

		@Override
		public void postNotSuccessfulCommitAction() {
		    //Ignored for now.
		}

		@Override
		public void postCommitAction() {
		    if(AfterCommitActions.SELECT == afterCommitAction){
			if(select){
			    textField.select(startIndex + selectionStartIndex, startIndex + selectionStartIndex + charNumberToSelect);
			}else{
			    textField.select(textField.getCaretPosition(), textField.getCaretPosition());
			}
		    }
		    afterCommitAction = AfterCommitActions.NONE;
		}
	    };
	}

	/**
	 * Returns the {@link FocusListener} that listens the focus gained event for the specified {@link JTextField} instance.
	 * This event handler also performs specific actions: text insert or caret position change.
	 * 
	 * @param textField
	 * @return
	 */
	private FocusListener createExpressionFocusListener(final JTextField textField) {
	    return new FocusListener() {

		@Override
		public void focusLost(final FocusEvent e) {
		    // is ignored for now
		}

		@Override
		public void focusGained(final FocusEvent e) {
		    switch(focusGainedOperation){
		    case TEXT_INSERTION:
			final String previousText = textField.getText();
			textField.setText(previousText.substring(0, startIndex) + textToInsert + previousText.substring(endIndex));
			textField.setCaretPosition(startIndex+relativeCaretPosition);
			if(triggerCommitOnTextInsertion){
			    afterCommitAction = AfterCommitActions.SELECT;
			    textComponent.commit();
			}else{
			    if(select){
				textField.select(startIndex + selectionStartIndex, startIndex + selectionStartIndex + charNumberToSelect);
			    }else{
				textField.select(textField.getCaretPosition(), textField.getCaretPosition());
			    }
			}
			break;
		    case CARRET_CONTROL:
			textField.setCaretPosition(relativeCaretPosition);
			break;
		    }
		    focusGainedOperation = FocusGainedOperation.NONE;
		}
	    };
	}

	/**
	 * Determines the operation that should be performed after the text field gained the focus. There are three type of operation:
	 * text insertion, caret position controlling or there was no operation to perform.
	 *
	 * @author TG Team
	 *
	 */
	private static enum FocusGainedOperation{
	    TEXT_INSERTION, CARRET_CONTROL, NONE;
	}

	private static enum AfterCommitActions{
	    SELECT, NONE;
	}
    }

    /**
     * Determines the way the text will be inserted in to the editor. There are three ways: Apply, Append and Replace.
     * The first type - Apply is used for functions like: YEAR(), SUM(). The second type: Append is used for operators like: +, /.
     * The Third type - Replace is used to replace selected text in the editor.
     *
     * @author TG Team
     *
     */
    public static enum TextInsertionType{
	APPLY, APPEND, REPLACE;
    }
}
