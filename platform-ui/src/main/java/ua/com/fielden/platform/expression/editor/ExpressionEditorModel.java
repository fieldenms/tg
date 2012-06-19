package ua.com.fielden.platform.expression.editor;

import java.awt.event.ActionEvent;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.util.HashMap;
import java.util.Map;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.PlainDocument;

import net.miginfocom.swing.MigLayout;
import ua.com.fielden.platform.basic.IValueMatcher;
import ua.com.fielden.platform.domaintree.impl.CalculatedProperty;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.error.Result;
import ua.com.fielden.platform.reflection.Reflector;
import ua.com.fielden.platform.swing.actions.Command;
import ua.com.fielden.platform.swing.components.NotificationLayer.MessageType;
import ua.com.fielden.platform.swing.components.bind.development.BoundedValidationLayer;
import ua.com.fielden.platform.swing.components.bind.development.ComponentFactory;
import ua.com.fielden.platform.swing.components.bind.development.ComponentFactory.EditorCase;
import ua.com.fielden.platform.swing.dialogs.DialogWithDetails;
import ua.com.fielden.platform.swing.ei.editors.development.ILightweightPropertyBinder;
import ua.com.fielden.platform.swing.ei.editors.development.IPropertyEditor;
import ua.com.fielden.platform.swing.ei.editors.development.LabelAndTooltipExtractor;
import ua.com.fielden.platform.swing.model.UModel;
import ua.com.fielden.platform.swing.model.UmState;
import ua.com.fielden.platform.swing.utils.DummyBuilder;
import ua.com.fielden.platform.utils.Pair;
import ua.com.fielden.platform.utils.ResourceLoader;

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

    private ITextSetter currentTextSetter = null;


    /**
     * Initiates expression editor with specific {@link ExpressionEntity} instance and appropriate {@link ILightweightPropertyBinder}.
     *
     * @param entity - specific expression entity.
     * @param propertyBinder - appropriate property binder that binds entity.
     */
    public ExpressionEditorModel(final CalculatedProperty entity, final ILightweightPropertyBinder<CalculatedProperty> propertyBinder) {
	super(entity, null, propertyBinder, false);
	this.expressionEditor = new ExpressionPropertyEditor(entity);
	this.expressionEditor.getEditor().getView().addFocusListener(createTextSetterSwitch(expressionEditor));
	this.originationEditor = new OriginationPropertyEditor(entity);
	this.originationEditor.getEditor().getView().addFocusListener(createTextSetterSwitch(originationEditor));
	this.currentTextSetter = expressionEditor;
	this.propertySelectionModel = new PropertyProvider();

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
		getEditors().get("contextualExpression").getEditor().requestFocusInWindow();
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
		getEditors().get("contextualExpression").getEditor().requestFocusInWindow();
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

	    private UmState restoreState;

	    @Override
	    protected boolean preAction() {
		this.restoreState = getState();
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

	    @Override
	    protected void handlePreAndPostActionException(final Throwable ex) {
		super.handlePreAndPostActionException(ex);
		setState(restoreState);
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

	    private UmState restoreState;

	    @Override
	    protected boolean preAction() {
		this.restoreState = getState();
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

	    @Override
	    protected void handlePreAndPostActionException(final Throwable ex) {
		super.handlePreAndPostActionException(ex);
		setState(restoreState);
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
     * {@link FocusListener} that determines into which component text must be set.
     *
     * @param textSetter
     * @return
     */
    private FocusListener createTextSetterSwitch(final ITextSetter textSetter) {
        return new FocusListener() {

            @Override
            public void focusLost(final FocusEvent e) {
            }

            @Override
            public void focusGained(final FocusEvent e) {
        	currentTextSetter = textSetter;
            }
        };
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
		    currentTextSetter.setText(textToInsert);
		}
	    }
	};
    }

    /**
     * The contract for anything that is interested in setting text.
     *
     * @author TG Team
     *
     */
    private static interface ITextSetter{

	/**
	 * Set the specified text.
	 *
	 * @param text
	 */
	void setText(String text);
    }

    private static class ExpressionPropertyEditor implements IPropertyEditor, ITextSetter{

	private final JLabel label;
	private final BoundedValidationLayer<JTextField> textEditor;

	private CalculatedProperty entity;
	private final String propertyName;

	/**
	 * Allows to set manually specified text.
	 */
	private final ManualTextSetter manualTextSetter;

	public ExpressionPropertyEditor(final CalculatedProperty entity){
	    this.entity = entity;
	    this.propertyName = "contextualExpression";
	    final Pair<String, String> titleAndDesc = LabelAndTooltipExtractor.extract(propertyName, entity.getType());

	    label = DummyBuilder.label(titleAndDesc.getKey());
	    label.setToolTipText(titleAndDesc.getValue());

	    textEditor = ComponentFactory.createCustomStringTextField(entity, propertyName, true, entity.getProperty(propertyName).getDesc(), EditorCase.MIXED_CASE, createExpressionEditorDocument());
	    manualTextSetter = new ManualTextSetter(textEditor, entity, propertyName);
	}

	private PlainDocument createExpressionEditorDocument(){
	    return new PlainDocument(){

		private static final long serialVersionUID = -6048781732872266049L;

		{
		    addDocumentListener(new DocumentListener() {

		        @Override
		        public void removeUpdate(final DocumentEvent e) {
		    	// TODO Auto-generated method stub

		        }

		        @Override
		        public void insertUpdate(final DocumentEvent e) {
		            System.out.println("this listener");
		        }

		        @Override
		        public void changedUpdate(final DocumentEvent e) {
		    	// TODO Auto-generated method stub

		        }
		    });
		}

		@Override
		public void replace(final int offset, final int length, final String text, final AttributeSet attrs) throws BadLocationException {
		    super.replace(offset, length, text, attrs);
		    if (manualTextSetter != null) {
			manualTextSetter.updateComponent();
		    }
		}

		@Override
		public void insertString(final int offs, final String str, final AttributeSet a) throws BadLocationException {
		    super.insertString(offs, str, a);
		    if (manualTextSetter != null) {
			manualTextSetter.updateComponent();
		    }
		}
	    };
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
	    this.manualTextSetter.setEntity(entity);
	}

	@Override
	public JLabel getLabel() {
	    return label;
	}

	@Override
	public BoundedValidationLayer<JTextField> getEditor() {
	    return textEditor;
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
	public void setText(final String text) {
	    insertText(text, TextInsertionType.REPLACE, true, 0, text.length(), text.length());
	}
    }

    private static class OriginationPropertyEditor implements IPropertyEditor, ITextSetter{

	private final JLabel label;
	private final BoundedValidationLayer<JTextField> textEditor;

	private CalculatedProperty entity;
	private final String propertyName;

	public OriginationPropertyEditor(final CalculatedProperty entity){
	    this.entity = entity;
	    this.propertyName = "originationProperty";

	    final Pair<String, String> titleAndDesc = LabelAndTooltipExtractor.extract(propertyName, entity.getType());
	    label = DummyBuilder.label(titleAndDesc.getKey());
	    label.setToolTipText(titleAndDesc.getValue());

	    textEditor = ComponentFactory.createStringTextField(entity, propertyName, true, entity.getProperty(propertyName).getDesc(), EditorCase.MIXED_CASE);
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
	public BoundedValidationLayer<JTextField> getEditor() {
	    return textEditor;
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
	public void setText(final String text) {
	    entity.setOriginationProperty(text);
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
	 * The name of the property for which the text must be set.
	 */
	private final String propertyName;

	/**
	 * The binded entity.
	 */
	private AbstractEntity<?> entity;

	/**
	 * Indicates whether select inserted text or not.
	 */
	private boolean select = false;

	/**
	 * Holds the start and end index of the place where the textToSet should be inserted.
	 */
	private int startIndex = 0;
	private int endIndex = 0;

	/**
	 * Determines the selection region to select after insertion.
	 */
	private int selectionStartIndex, charNumberToSelect;

	/**
	 * Relative index of the caret position.
	 */
	private int relativeCaretPosition;

	/**
	 * Current action that must be performed after text was set to the model.
	 */
	private AfterTextSetAction textSetAction = AfterTextSetAction.NONE;

	/**
	 * Initiates this {@link ManualTextSetter} with specified {@link BoundedValidationLayer} instance.
	 *
	 * @param textComponent
	 */
	public ManualTextSetter(final BoundedValidationLayer<JTextField> textComponent, final AbstractEntity<?> entity, final String propertyName){
	    this.textComponent = textComponent;
	    this.propertyName = propertyName;
	    this.entity = entity;
	}

	/**
	 * Updates component after text was inserted in to the text editor.
	 */
	public void updateComponent() {
	    final JTextField field = textComponent.getView();

	    if(textSetAction == AfterTextSetAction.UPDATE_COMPONENT){
		field.setCaretPosition(startIndex + relativeCaretPosition);
		if (select) {
		    field.select(startIndex + selectionStartIndex, startIndex + selectionStartIndex + charNumberToSelect);
		} else {
		    field.select(field.getCaretPosition(), field.getCaretPosition());
		}
		startIndex = endIndex = relativeCaretPosition = charNumberToSelect = selectionStartIndex = 0;
		select = false;
		textSetAction = AfterTextSetAction.NONE;
		field.requestFocusInWindow();
            }
	}

	/**
	 * Set the binded entity.
	 *
	 * @param entity
	 */
	public void setEntity(final AbstractEntity<?> entity) {
	    this.entity = entity;
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
	    final String textToSet;
	    if(field.getSelectionStart() == field.getSelectionEnd()){
		startIndex = endIndex = field.getCaretPosition();
		textToSet = textToInsert;
		this.select = TextInsertionType.APPLY == insertionType ? false : select;
		this.selectionStartIndex = selectionStartIndex;
		this.charNumberToSelect = charNumberToSelect;
		this.relativeCaretPosition = relativeCaretPosition;
	    } else {
		this.select = select;
		switch(insertionType){
		case APPLY:
		    startIndex = field.getSelectionStart();
		    endIndex = field.getSelectionEnd();
		    this.selectionStartIndex = selectionStartIndex;

		    textToSet = textToInsert.substring(0, relativeCaretPosition) //
			    /*              */+ field.getText().substring(startIndex, endIndex) + textToInsert.substring(relativeCaretPosition);
		    this.relativeCaretPosition = textToSet.length();
		    this.charNumberToSelect = field.getText().substring(startIndex, endIndex).length() + charNumberToSelect;
		    break;
		case APPEND:
		    startIndex = endIndex = field.getCaretPosition();
		    textToSet = textToInsert;
		    this.relativeCaretPosition = relativeCaretPosition;
		    this.selectionStartIndex = selectionStartIndex;
		    this.charNumberToSelect = charNumberToSelect;
		    break;
		case REPLACE:
		    startIndex = field.getSelectionStart();
		    endIndex = field.getSelectionEnd();
		    textToSet = textToInsert;
		    this.relativeCaretPosition = relativeCaretPosition;
		    this.selectionStartIndex = selectionStartIndex;
		    this.charNumberToSelect = charNumberToSelect;
		    break;
		default:
		    textToSet = null;
		    break;
		}
	    }
	    final String previousText = field.getText();
	    textSetAction = AfterTextSetAction.UPDATE_COMPONENT;
	    entity.set(propertyName, previousText.substring(0, startIndex) + textToSet + previousText.substring(endIndex));
	}

	/**
	 * Determines the action that must be performed after text was set to the model.
	 *
	 * @author TG Team
	 *
	 */
	private static enum AfterTextSetAction{
	    UPDATE_COMPONENT, NONE;
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
