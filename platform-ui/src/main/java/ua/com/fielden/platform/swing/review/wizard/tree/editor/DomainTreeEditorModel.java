package ua.com.fielden.platform.swing.review.wizard.tree.editor;

import java.awt.event.ActionEvent;

import javax.swing.Action;
import javax.swing.event.EventListenerList;

import org.apache.commons.lang.StringUtils;

import ua.com.fielden.platform.domaintree.IDomainTreeEnhancer.IncorrectCalcPropertyKeyException;
import ua.com.fielden.platform.domaintree.IDomainTreeManager.IDomainTreeManagerAndEnhancer;
import ua.com.fielden.platform.domaintree.impl.CalculatedProperty;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.expression.editor.ExpressionEditorModel;
import ua.com.fielden.platform.expression.editor.IPropertySelectionListener;
import ua.com.fielden.platform.reflection.PropertyTypeDeterminator;
import ua.com.fielden.platform.swing.actions.Command;
import ua.com.fielden.platform.swing.ei.development.MasterPropertyBinder;
import ua.com.fielden.platform.swing.ei.editors.development.ILightweightPropertyBinder;
import ua.com.fielden.platform.swing.model.UmState;
import ua.com.fielden.platform.swing.treewitheditors.domaintree.development.EntitiesTreeModel2;
import ua.com.fielden.platform.utils.EntityUtils;
import ua.com.fielden.platform.utils.ResourceLoader;

/**
 * Wizard that allows one to create/edit calculated properties and configure entity review.
 *
 * @author TG Team
 *
 */
public class DomainTreeEditorModel<T extends AbstractEntity> {

    private final ExpressionEditorModelForWizard expressionModel;

    private final EventListenerList listenerList;
    private final CalculatedPropertySelectModel propertySelectionModel;

    private final IDomainTreeManagerAndEnhancer dtme;
    private final EntityFactory factory;

    /**
     * Copy the selected generated property.
     */
    private final Action copyAction;
    /**
     * The entity type that is enhancing.
     */
    private final Class<T> rootType;

    public DomainTreeEditorModel(final EntityFactory factory, final IDomainTreeManagerAndEnhancer dtme, final Class<T> rootType){
	this.rootType = rootType;
	this.dtme = dtme;
	this.factory = factory;
	final CalculatedProperty entity = CalculatedProperty.createEmpty(factory, rootType, "", dtme.getEnhancer());
	this.expressionModel = new ExpressionEditorModelForWizard(entity, MasterPropertyBinder.<CalculatedProperty>createPropertyBinderWithoutLocatorSupport(null));
	this.listenerList = new EventListenerList();
	this.propertySelectionModel = new CalculatedPropertySelectModel();
	this.propertySelectionModel.addPropertySelectionListener(createPropertySelectedListener());
	this.copyAction = expressionModel.getCopyAction();
    }

    /**
     * Returns the action that copy the selected calculated property.
     *
     * @return
     */
    public final Action getCopyAction() {
	return copyAction;
    }

    /**
     * Returns the selection model associated with this {@link DomainTreeEditorModel}
     *
     * @return
     */
    public final CalculatedPropertySelectModel getPropertySelectionModel() {
	return propertySelectionModel;
    }

    /**
     * Returns associated {@link ExpressionEditorModel} instance.
     *
     * @return
     */
    public final ExpressionEditorModel getExpressionModel() {
	return expressionModel;
    }

    public EntitiesTreeModel2 createTreeModel() {
	return new EntitiesTreeModel2(dtme, //
		getExpressionModel().getNewAction(), getExpressionModel().getEditAction(),//
		getCopyAction(), getExpressionModel().getDeleteAction(), "selection criteria", "result set");
    }

    /**
     * Adds {@link IPropertyEditListener} to the list of listeners to be notified when the edit calculated property action will take place.
     *
     * @param l
     */
    public void addPropertyEditListener(final IPropertyEditListener l) {
	listenerList.add(IPropertyEditListener.class, l);
    }

    /**
     * Removes the {@link IPropertyEditListener} from the list of listeners.
     *
     * @param l
     */
    public void removePropertyEditListener(final IPropertyEditListener l) {
	listenerList.remove(IPropertyEditListener.class, l);
    }

    /**
     * Returns value that indicates whether specified property name in the root type is generated or not.
     *
     * @param entityClass
     * @param propertyName
     * @return
     */
    private boolean isGenerated(final String propertyName){
	try{
	    dtme.getEnhancer().getCalculatedProperty(rootType, propertyName);
	    return true;
	}catch (final IncorrectCalcPropertyKeyException ex){
	    return false;
	}
    }

    /**
     * Notify all listeners that have registered interest for notification on the edit event.
     *
     * @param action.
     */
    private void firePropertyProcessAction(final IPropertyProcessingAction action) {
	// Guaranteed to return a non-null array
	final Object[] listeners = listenerList.getListenerList();
	// Process the listeners last to first, notifying
	// those that are interested in this event
	for (int i = listeners.length-2; i>=0; i-=2) {
	    if (listeners[i]==IPropertyEditListener.class) {
		// Process the listener:
		action.processPropertyEditAction((IPropertyEditListener)listeners[i+1]);
	    }
	}
    }

    /**
     * Creates the {@link IPropertySelectionListener} that listens the entity property selection change event.
     *
     * @return
     */
    private IPropertySelectionListener createPropertySelectedListener() {
	return new IPropertySelectionListener() {

	    @Override
	    public void propertyStateChanged(final String property, final boolean isSelected) {
		expressionModel.getEditAction().setEnabled(isSelected && isGenerated(property));
	    }
	};
    }

    /**
     * {@link ExpressionEditorModel} that handles new/edit/cancel/save actions.
     *
     * @author TG Team
     *
     */
    private class ExpressionEditorModelForWizard extends ExpressionEditorModel{

	private boolean isNew;

	private final Action copyAction;

	public ExpressionEditorModelForWizard(final CalculatedProperty entity, final ILightweightPropertyBinder<CalculatedProperty> propertyBinder) {
	    super(entity, propertyBinder);
	    this.copyAction = createCopyAction();
	}

	/**
	 * Returns the "copy calculated property" action.
	 * 
	 * @return
	 */
	public Action getCopyAction() {
	    return copyAction;
	}

	@Override
	protected void notifyActionStageChange(final ActionStage actionState) {
	    super.notifyActionStageChange(actionState);
	    switch(actionState){
	    case NEW_ACTION:
		//TODO if this implementation is incorrect then advise!
		if(propertySelectionModel != null && propertySelectionModel.isPropertySelected()){
		    final Class<?> propertyType = StringUtils.isEmpty(propertySelectionModel.getSelectedProperty()) ? rootType : PropertyTypeDeterminator.determinePropertyType(rootType, propertySelectionModel.getSelectedProperty());
		    if(EntityUtils.isEntityType(propertyType)){
			final CalculatedProperty entity = CalculatedProperty.createEmpty(factory, rootType, propertySelectionModel.getSelectedProperty(), /* null, null, null, CalculatedPropertyAttribute.NO_ATTR, null, */ dtme.getEnhancer());
			setEntity(entity);
			isNew = true;
		    } else {
			throw new IllegalStateException("The context property can not have type different then entity type!");
		    }
		} else {
		    throw new IllegalStateException("Please select property first to create new calculated property!");
		}
		break;
	    case EDIT_ACTION:
		if(canEdit()){
		    final CalculatedProperty entity = (CalculatedProperty)dtme.getEnhancer().getCalculatedProperty(rootType, propertySelectionModel.getSelectedProperty());
		    setEntity(entity);
		    isNew = false;
		} else {
		    throw new IllegalStateException("Please select generated property first to edit it");
		}
		break;
	    case DELETE_ACTION:
		if(propertySelectionModel != null && propertySelectionModel.isPropertySelected()){
		    dtme.getEnhancer().removeCalculatedProperty(rootType, propertySelectionModel.getSelectedProperty());
		    dtme.getEnhancer().apply();
		} else {
		    throw new IllegalStateException("Please select calculated property to remove!");
		}
		break;
	    case NEW_POST_ACTION:
	    case EDIT_POST_ACTION:
		firePropertyProcessAction(new IPropertyProcessingAction(){

		    @Override
		    public void processPropertyEditAction(final IPropertyEditListener listener) {
			listener.startEdit();
		    }
		});
		break;

	    case SAVE_POST_ACTION_SUCCESSFUL:
		//TODO must ask whether can add edited calculated property or not.
		if (isNew) {
		    dtme.getEnhancer().addCalculatedProperty(getEntity());
		}
		firePropertyProcessAction(new IPropertyProcessingAction(){

		    @Override
		    public void processPropertyEditAction(final IPropertyEditListener listener) {
			listener.finishEdit();
		    }
		});
		dtme.getEnhancer().apply();
		break;
	    case CANCEL_POST_ACTION:
		firePropertyProcessAction(new IPropertyProcessingAction(){

		    @Override
		    public void processPropertyEditAction(final IPropertyEditListener listener) {
			listener.finishEdit();
		    }
		});
		dtme.getEnhancer().discard();
		break;
	    }
	}

	@Override
	protected boolean canEdit() {
	    return propertySelectionModel != null && propertySelectionModel.isPropertySelected() && isGenerated(propertySelectionModel.getSelectedProperty());
	}

	private Action createCopyAction() {
	    return new Command<Void>("Copy") {
		private static final long serialVersionUID = 1L;

		{
		    putValue(Action.LARGE_ICON_KEY, ResourceLoader.getIcon("images/page_white_copy.png"));
		    putValue(Action.SHORT_DESCRIPTION, "Copy the selected calculated property");
		}

		@Override
		protected boolean preAction() {
		    setState(UmState.UNDEFINED);
		    return super.preAction();
		}

		@Override
		protected Void action(final ActionEvent event) throws Exception {
		    if(propertySelectionModel != null && propertySelectionModel.isPropertySelected()){
			final CalculatedProperty entity = (CalculatedProperty)dtme.getEnhancer().copyCalculatedProperty(rootType, propertySelectionModel.getSelectedProperty());
			setEntity(entity);
			isNew = true;
		    } else {
			throw new IllegalStateException("Please select calculated property to edit!");
		    }
		    return null;
		}

		@Override
		protected void postAction(final Void entity) {
		    setState(UmState.EDIT);
		    firePropertyProcessAction(new IPropertyProcessingAction(){

			@Override
			public void processPropertyEditAction(final IPropertyEditListener listener) {
			    listener.startEdit();
			}
		    });
		}

		@Override
		protected void handlePreAndPostActionException(final Throwable ex) {
		    super.handlePreAndPostActionException(ex);
		    setState(UmState.VIEW);
		}

	    };
	}

    }

    /**
     * A contract that allows to process edit listener.
     *
     * @author TG Team
     */
    private static interface IPropertyProcessingAction{

	/**
	 * Processes {@link IPropertyEditListener}.
	 *
	 * @param listener - specified listener to process.
	 */
	void processPropertyEditAction(IPropertyEditListener listener);
    }

}
