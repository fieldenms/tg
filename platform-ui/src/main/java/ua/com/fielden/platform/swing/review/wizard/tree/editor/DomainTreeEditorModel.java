package ua.com.fielden.platform.swing.review.wizard.tree.editor;

import javax.swing.event.EventListenerList;

import org.apache.commons.lang.StringUtils;

import ua.com.fielden.actionpanelmodel.ActionPanelBuilder;
import ua.com.fielden.platform.domaintree.EntitiesTreeModel2;
import ua.com.fielden.platform.domaintree.IDomainTreeManager.IDomainTreeManagerAndEnhancer;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.annotation.Calculated;
import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.expression.editor.ExpressionEditorModel;
import ua.com.fielden.platform.expression.editor.IPropertySelectionListener;
import ua.com.fielden.platform.expression.entity.ExpressionEntity;
import ua.com.fielden.platform.reflection.AnnotationReflector;
import ua.com.fielden.platform.swing.ei.LightweightPropertyBinder;
import ua.com.fielden.platform.swing.ei.editors.ILightweightPropertyBinder;

/**
 * Wizard that allows one to create/edit calculated properties and configure entity review.
 * 
 * @author TG Team
 *
 */
public class DomainTreeEditorModel<T extends AbstractEntity> {

    private final ExpressionEditorModel expressionModel;

    private final EventListenerList listenerList;
    private final CalculatedPropertySelectModel propertySelectionModel;

    private final IDomainTreeManagerAndEnhancer dtme;

    public DomainTreeEditorModel(final EntityFactory factory, final IDomainTreeManagerAndEnhancer dtme, final Class<T> rootType){
	final ExpressionEntity entity = factory.newEntity(ExpressionEntity.class, 0L);
	entity.setEntityClass(rootType);
	//entity.setName(generateNextPropertyName());
	this.dtme = dtme;
	this.expressionModel = new ExpressionEditorModelForWizard(entity, new LightweightPropertyBinder<ExpressionEntity>(null, null, "key", "name"));
	this.listenerList = new EventListenerList();
	this.propertySelectionModel = new CalculatedPropertySelectModel();
	this.propertySelectionModel.addPropertySelectionListener(createPropertySelectedListener());
    }

    /**
     * Returns value that indicates whether specified property name in the entityClass is calculated or not.
     * 
     * @param entityClass
     * @param propertyName
     * @return
     */
    private static boolean isCalculated(final Class<? extends AbstractEntity> entityClass, final String propertyName){
	return StringUtils.isEmpty(propertyName) ? false : AnnotationReflector.isPropertyAnnotationPresent(Calculated.class, entityClass, propertyName);
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
		expressionModel.getEditAction().setEnabled(isSelected && isCalculated(getEntityClass(), property));
	    }
	};
    }


    /**
     * Generates next property name for calculated property.
     * 
     * @return
     */
    private String generateNextPropertyName() {
	return "_1$";
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

    public ActionPanelBuilder getPropertyManagementActionPanel() {
	return new ActionPanelBuilder().addButton(expressionModel.getNewAction()).addButton(expressionModel.getEditAction());
    }

    public EntitiesTreeModel2 createTreeModel() {
	return new EntitiesTreeModel2(dtme);
	//return new CriteriaTreeModel(getEntityClass(), new DefaultDynamicCriteriaPropertyFilter(), null);
    }

    public Class<? extends AbstractEntity> getEntityClass() {
	return expressionModel.getEntity().getEntityClass();
    }

    //    protected IPropertySelectionListener getCalculatedPropertySelectListener() {
    //	return new IPropertySelectionListener() {
    //
    //	    @Override
    //	    public void propertyStateChanged(final String property, final boolean isSelected) {
    //
    //	    }
    //	};
    //    }

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
     * {@link ExpressionEditorModel} that handles new/edit/cancel/save actions.
     * 
     * @author TG Team
     *
     */
    private class ExpressionEditorModelForWizard extends ExpressionEditorModel{

	public ExpressionEditorModelForWizard(final ExpressionEntity entity, final ILightweightPropertyBinder<ExpressionEntity> propertyBinder) {
	    super(entity, propertyBinder);
	}

	@Override
	protected void notifyActionStageChange(final ActionStage actionState) {
	    super.notifyActionStageChange(actionState);
	    switch(actionState){
	    case NEW_ACTION:
		final Class<? extends AbstractEntity> rootType = getEntity().getEntityClass();
		getEntity().restoreToOriginal();
		getEntity().setInitialising(true);
		getEntity().setName(generateNextPropertyName());
		getEntity().setEntityClass(rootType);
		getEntity().setInitialising(false);
		break;
	    case EDIT_ACTION:
		//TODO must implement edit action.
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
	    case CANCEL_POST_ACTION:
	    case SAVE_POST_ACTION_SUCCESSFUL:
		firePropertyProcessAction(new IPropertyProcessingAction(){

		    @Override
		    public void processPropertyEditAction(final IPropertyEditListener listener) {
			listener.finishEdit();
		    }
		});
		break;
	    }
	}

	@Override
	protected boolean canEdit() {
	    return propertySelectionModel != null && propertySelectionModel.isPropertySelected() && isCalculated(getEntityClass(), propertySelectionModel.getSelectedProperty());
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
