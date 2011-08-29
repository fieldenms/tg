package ua.com.fielden.platform.expression.editor;

import java.awt.event.ActionEvent;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.util.HashMap;
import java.util.Map;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.KeyStroke;

import net.miginfocom.swing.MigLayout;
import ua.com.fielden.platform.basic.IValueMatcher;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.error.Result;
import ua.com.fielden.platform.expression.entity.ExpressionEntity;
import ua.com.fielden.platform.swing.actions.Command;
import ua.com.fielden.platform.swing.components.NotificationLayer.MessageType;
import ua.com.fielden.platform.swing.components.bind.BoundedValidationLayer;
import ua.com.fielden.platform.swing.components.bind.ComponentFactory;
import ua.com.fielden.platform.swing.dialogs.DialogWithDetails;
import ua.com.fielden.platform.swing.ei.editors.ILightweightPropertyBinder;
import ua.com.fielden.platform.swing.ei.editors.IPropertyEditor;
import ua.com.fielden.platform.swing.ei.editors.LabelAndTooltipExtractor;
import ua.com.fielden.platform.swing.model.UModel;
import ua.com.fielden.platform.swing.model.UmState;
import ua.com.fielden.platform.swing.utils.DummyBuilder;
import ua.com.fielden.platform.utils.Pair;

import com.jgoodies.binding.value.Trigger;

/**
 * Model for expression editor.
 * 
 * @author TG Team
 *
 */
public class ExpressionEditorModel extends UModel<ExpressionEntity, ExpressionEntity, Object> {

    private final ExpressionPropertyEditor expressionEditor;
    private final IPropertyProvider propertySelectionModel;

    /**
     * Initiates expression editor with specific {@link ExpressionEntity} instance and appropriate {@link ILightweightPropertyBinder}.
     * 
     * @param entity - specific expression entity.
     * @param propertyBinder - appropriate property binder that binds entity.
     */
    public ExpressionEditorModel(final ExpressionEntity entity, final ILightweightPropertyBinder<ExpressionEntity> propertyBinder) {
	super(entity, null, propertyBinder, false);
	this.expressionEditor = new ExpressionPropertyEditor(entity);
	this.propertySelectionModel = new PropertyProvider();
	this.propertySelectionModel.addPropertySelectionListener(getPropertySelectionListener());
	final Map<String, IPropertyEditor> editors = new HashMap<String, IPropertyEditor>(getEditors());
	editors.put("expression", expressionEditor);
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
    protected Map<String, IPropertyEditor> buildEditors(final ExpressionEntity entity, final Object controller, final ILightweightPropertyBinder<ExpressionEntity> propertyBinder) {
	return propertyBinder.bind(entity);
    }

    @Override
    protected ExpressionEntity getManagedEntity() {
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

	};
	action.setEnabled(true);
	action.putValue(Action.SHORT_DESCRIPTION, "Create new");
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

	};
	action.setEnabled(canEdit());
	action.putValue(Action.SHORT_DESCRIPTION, "Edit");
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

		    //TODO must also create new calculated property for specified entity.

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

	    @Override
	    public void actionPerformed(final ActionEvent e) {
		// TODO Auto-generated method stub

	    }
	};
    }

    @Override
    protected Action createDeleteAction() {
	return new AbstractAction() {

	    @Override
	    public void actionPerformed(final ActionEvent e) {
		// TODO Auto-generated method stub

	    }
	};
    }

    /**
     * Creates {@link Action} for function buttons.
     * 
     * @param function
     * @return
     */
    protected Action getFunctionAction(final String title, final String desc, final String insertionText, final int relativeIndex){
	final Action functionAction= new AbstractAction(title) {

	    private static final long serialVersionUID = 8346239807039308077L;

	    @Override
	    public void actionPerformed(final ActionEvent e) {
		expressionEditor.insertText(insertionText, false, relativeIndex);
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
    private IPropertySelectionListener getPropertySelectionListener(){
	return new IPropertySelectionListener() {

	    @Override
	    public void propertyStateChanged(final String property, final boolean isSelected) {
		if(isSelected){
		    expressionEditor.insertText(property, true, property.length());
		}
	    }
	};
    }

    private static class ExpressionPropertyEditor implements IPropertyEditor{

	private final JLabel label;
	private final BoundedValidationLayer<JTextField> editor;
	private final Trigger commitTrigger;

	private ExpressionEntity entity;

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
	 * relative index of the caret position.
	 */
	private int relativeCaretPosition;


	public ExpressionPropertyEditor(final ExpressionEntity entity){
	    this.entity = entity;
	    this.commitTrigger = new Trigger();
	    final Pair<String, String> titleAndDesc = LabelAndTooltipExtractor.extract("expression", entity.getType());

	    label = DummyBuilder.label(titleAndDesc.getKey());
	    label.setToolTipText(titleAndDesc.getValue());


	    final BoundedValidationLayer<JTextField> component = ComponentFactory.createTriggeredStringTextField(entity, "expression", commitTrigger, false, entity.getProperty("expression").getDesc());
	    editor = component;
	    final JTextField field = editor.getView();
	    field.addFocusListener(createExpressionFocusListener(editor.getView()));
	    final String actionName = "Enter action";
	    field.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), actionName);
	    field.getActionMap().put(actionName, createCommiteAction());
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
	    final JTextField field = editor.getView();
	    this.textToInsert = text;
	    this.select = select;
	    this.relativeCaretPosition=relativeCaretPosition;
	    startIndex = 0;
	    endIndex = field.getText().length();
	    field.requestFocusInWindow();
	}

	/**
	 * Inserts specified text at the caret position or replaces selected text.
	 * 
	 * @param textToInsert - specified text to insert.
	 * @param select - indicates whether select inserted text or not.
	 */
	public void insertText(final String textToInsert, final boolean select, final int relativeCaretPosition){
	    final JTextField field = editor.getView();
	    this.textToInsert = textToInsert;
	    this.select = select;
	    this.relativeCaretPosition = relativeCaretPosition;
	    if(field.getSelectionStart() == field.getSelectionEnd()){
		startIndex = endIndex = field.getCaretPosition();
	    } else {
		startIndex = field.getSelectionStart();
		endIndex = field.getSelectionEnd();
	    }
	    field.requestFocusInWindow();
	}

	@Override
	public ExpressionEntity getEntity() {
	    return entity;
	}

	@Override
	public String getPropertyName() {
	    return "expression";
	}

	@Override
	public void bind(final AbstractEntity<?> entity) {
	    this.entity = (ExpressionEntity)entity;
	    this.editor.rebindTo(entity);
	}

	@Override
	public JLabel getLabel() {
	    return label;
	}

	@Override
	public BoundedValidationLayer<JTextField> getEditor() {
	    return editor;
	}

	@Override
	public JPanel getDefaultLayout() {
	    final JPanel panel = new JPanel(new MigLayout("fill, insets 0", "[]5[]", "[c]"));
	    panel.add(label);
	    panel.add(editor, "growx");
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

	private FocusListener createExpressionFocusListener(final JTextField textField) {
	    return new FocusListener() {

		@Override
		public void focusLost(final FocusEvent e) {
		    // is ignored for now
		}

		@Override
		public void focusGained(final FocusEvent e) {
		    if(textToInsert != null){
			final String previousText = textField.getText();
			textField.setText(previousText.substring(0, startIndex) + textToInsert + previousText.substring(endIndex));
			textField.setCaretPosition(startIndex+relativeCaretPosition);
			if(select){
			    textField.select(startIndex, startIndex + textToInsert.length());
			}
			textToInsert = null;
		    }
		}
	    };
	}

    }
}
