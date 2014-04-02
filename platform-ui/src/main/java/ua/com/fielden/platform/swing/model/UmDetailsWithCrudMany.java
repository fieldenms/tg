package ua.com.fielden.platform.swing.model;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.Action;
import javax.swing.DefaultListSelectionModel;
import javax.swing.JOptionPane;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import ua.com.fielden.platform.dao.IMasterDetailsDao;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.fluent.fetch;
import ua.com.fielden.platform.error.Result;
import ua.com.fielden.platform.reflection.TitlesDescsGetter;
import ua.com.fielden.platform.swing.actions.Command;
import ua.com.fielden.platform.swing.components.NotificationLayer.MessageType;
import ua.com.fielden.platform.swing.components.bind.development.Binder;
import ua.com.fielden.platform.swing.components.blocking.BlockingIndefiniteProgressPane;
import ua.com.fielden.platform.swing.dialogs.DialogWithDetails;
import ua.com.fielden.platform.swing.egi.models.PropertyTableModel;
import ua.com.fielden.platform.swing.ei.editors.development.ILightweightPropertyBinder;
import ua.com.fielden.platform.swing.utils.Dialogs;

import com.jidesoft.grid.TableModelWrapperUtils;

/**
 * This is a CRUD UI model representing one-to-many master/details association. It has the following properties:
 * <ul>
 * <li>All instances of details entities are kept in {@link PropertyTableModel}.
 * <li>Only one instance can be edited or saved at the time.
 * <li>A details instance becomes a managed entity (thus get's associated with editors) upon selection of a row representing this instance in EGI associated with model's table
 * model.
 * <li>After instantiation, model should be provided with an instance of the associated view in order to be able to provided some feedback to users.
 * </ul>
 * 
 * @author TG Team
 * 
 * @param <M>
 * @param <D>
 * @param <C>
 */
public abstract class UmDetailsWithCrudMany<M extends AbstractEntity<?>, D extends AbstractEntity<?>, C extends IMasterDetailsDao<M, D>> extends UmDetailsWithCrud<M, D, C> {
    /** Table model representing all instances of details entities. */
    private final PropertyTableModel<D> tableModel;

    /** Listener responsible for an action happening upon row change in the grid of details entities. */
    private final ListSelectionListener onRowSelect;
    // a helper state to assist in correct handling of the programmatically enforced row change even, which occurs upon cancel action.
    // its value should be changed strictly on EDT in order to ensure thread safety
    private boolean isModifying = false;

    protected UmDetailsWithCrudMany(final M entity, final C companion, final ILightweightPropertyBinder<D> propertyBinder, final fetch<D> fm, final PropertyTableModel<D> tableModel, final boolean lazy) {
        super(entity, companion, propertyBinder, fm, lazy);
        this.tableModel = tableModel;
        onRowSelect = new ListSelectionListener() {
            private int oldIndex = -1;

            @Override
            public void valueChanged(final ListSelectionEvent e) {
                if (!e.getValueIsAdjusting()) {
                    final int actualIndex = TableModelWrapperUtils.getActualRowAt(getTableModel(), ((DefaultListSelectionModel) e.getSource()).getMaxSelectionIndex());

                    if (canClose() != null && isModifying) {
                        if (getView() != null) {
                            getView().notify(whyCannotClose(), MessageType.WARNING);
                        } else {
                            new DialogWithDetails(null, "Entity selection", new IllegalStateException(whyCannotClose())).setVisible(true);
                        }
                        getTableModel().selectRow(oldIndex);
                    } else {
                        final D selectedEntity = getTableModel().instance(actualIndex);
                        if (selectedEntity == null) {
                            return;
                        }
                        oldIndex = actualIndex;
                        if (actualIndex >= 0) {
                            setManagedEntity(selectedEntity);
                        }
                        setState(UmState.VIEW);
                    }
                }
            }
        };

    }

    /** A convenient method for loading details. Could potentially be overridden, but unlikely there should be a reason for that. */
    protected List<D> loadDetails() {
        return getCompanion().findDetails(getEntity(), getFetchModel(), Integer.MAX_VALUE).data();
    }

    //////////////////////////////////////////////////////////////
    /////////////////////// INITIALISATION ///////////////////////
    //////////////////////////////////////////////////////////////
    /** A field for temporal holding of details entities retrieved upon initialisation. */
    private final List<D> tmpDetails = new ArrayList<D>();

    @Override
    protected void preInit(final BlockingIndefiniteProgressPane blockingPane) {
        blockingPane.setText("Loading...");
    }

    @Override
    protected void doInit(final BlockingIndefiniteProgressPane blockingPane) {
        tmpDetails.clear();
        tmpDetails.addAll(loadDetails());
    }

    @Override
    protected void postInit(final BlockingIndefiniteProgressPane blockingPane) {
        setEditors(buildEditors(getEntity(), getCompanion(), getPropertyBinder()));
        getView().buildUi();

        getTableModel().clearInstances();
        // weird cast occurs here because it is not possible to create array with elements of type D
        ((PropertyTableModel) getTableModel()).addInstances(tmpDetails.toArray(new AbstractEntity[] {}));
        tmpDetails.clear();
        getTableModel().fireTableDataChanged();

        if (getTableModel().instances().size() > 0) {
            final int row = getTableModel().getRowOf(getManagedEntity());
            getTableModel().selectRow(row >= 0 ? row : 0);
        } else {
            setManagedEntity(determineManagedEntity(getTableModel().instances()));
        }

        setState(UmState.VIEW);
    }

    /**
     * A convenient method for determining what details entity should be managed.
     * 
     * @param data
     * @return
     */
    protected D determineManagedEntity(final List<D> data) {
        // if there are no indirect charges associated with a work order then create a new one
        return data.size() > 0 ? data.get(0) : newEntity(getEntity().getEntityFactory());
    }

    //////////////////////////////////////////////////////////////
    ////////////////////////// ACTIONS ///////////////////////////
    //////////////////////////////////////////////////////////////

    /**
     * Determines whether managed entity should be restored by setting its properties to the original values.
     */
    protected boolean shouldRestore(final D managedEntity) {
        return getManagedEntity().isPersisted() && (getManagedEntity().isDirty() || !getManagedEntity().isValid().isSuccessful());
    }

    @Override
    protected Action createCancelAction() {
        final Command<D> action = new Command<D>("Cancel") {
            private static final long serialVersionUID = 1L;

            @Override
            protected boolean preAction() {
                notifyActionStageChange(ActionStage.CANCEL_PRE_ACTION);
                getSaveAction().setEnabled(false);
                if (super.preAction()) {
                    disableEditors(true);
                    return true;
                } else {
                    return false;
                }
            }

            @Override
            protected D action(final ActionEvent event) throws Exception {
                notifyActionStageChange(ActionStage.CANCEL_ACTION);

                // wait for all the values entered into controls to be flushed (committed)
                final Result res = getManagedEntity().isValid();
                if (shouldRestore(getManagedEntity())) {
                    return (D) getManagedEntity().restoreToOriginal();
                } else {
                    return determineManagedEntity(getTableModel().instances());
                }
            }

            @Override
            protected void postAction(final D entity) {
                if (getView() != null) {
                    getView().notify("", MessageType.NONE);
                }
                setManagedEntity(entity);
                // before re-associating data with the model change set the flag to indicate that all entity changes are completed, which ensures correct row change event execution
                isModifying = false;
                final int selectedRow = getTableModel().getSelectedRow();
                getTableModel().fireTableDataChanged();
                final List<D> data = getTableModel().instances();
                if (data.size() > 0) {
                    final int row = selectedRow >= 0 ? selectedRow : getTableModel().getRowOf(getManagedEntity());
                    getTableModel().selectRow(row >= 0 ? row : 0); // changing row will  trigger onRowSelect listener, which sets the VIEW status
                } else {
                    setState(UmState.VIEW); // reset the state again to re-bind editors to the new managed entity.
                }

                notifyActionStageChange(ActionStage.CANCEL_POST_ACTION);
            }

        };
        action.setEnabled(true);
        action.putValue(Action.SHORT_DESCRIPTION, "Cancel");
        action.putValue(Action.MNEMONIC_KEY, KeyEvent.VK_C);
        return action;
    }

    @Override
    protected Action createEditAction() {
        final Command<UmState> action = new Command<UmState>("Edit") {
            private static final long serialVersionUID = 1L;

            @Override
            protected boolean preAction() {
                final Result editability = getManagedEntity().isEditable();
                if (!editability.isSuccessful()) {
                    Dialogs.showMessageDialog(getView(), editability.getMessage(), "Edit action", Dialogs.WARNING_MESSAGE);
                    return false;
                }

                getNewAction().setEnabled(false);
                getEditAction().setEnabled(false);
                getSaveAction().setEnabled(true);
                getCancelAction().setEnabled(true);
                getRefreshAction().setEnabled(false);
                notifyActionStageChange(ActionStage.EDIT_PRE_ACTION);
                return super.preAction();
            }

            @Override
            protected UmState action(final ActionEvent event) throws Exception {
                notifyActionStageChange(ActionStage.EDIT_ACTION);
                return UmState.EDIT;
            }

            @Override
            protected void postAction(final UmState uiModelState) {
                setState(uiModelState);
                notifyActionStageChange(ActionStage.EDIT_POST_ACTION);
                // indicate that entity modification is in progress, which ensures correct row change event execution
                isModifying = true;
            }

        };
        action.setEnabled(true);
        action.putValue(Action.SHORT_DESCRIPTION, "Edit selected entity");
        action.putValue(Action.MNEMONIC_KEY, KeyEvent.VK_E);
        return action;
    }

    @Override
    protected Action createNewAction() {
        final Command<D> action = new Command<D>("New") {
            private static final long serialVersionUID = 1L;

            @Override
            protected boolean preAction() {
                setState(UmState.UNDEFINED);
                notifyActionStageChange(ActionStage.NEW_PRE_ACTION);
                return super.preAction();
            }

            @Override
            protected D action(final ActionEvent event) throws Exception {
                notifyActionStageChange(ActionStage.NEW_ACTION);
                // wait for all the values entered into controls to be flushed (committed)
                return newEntity(getEntity().getEntityFactory());
            }

            @Override
            protected void postAction(final D newEntity) {
                setManagedEntity(newEntity);
                setState(UmState.NEW);
                notifyActionStageChange(ActionStage.NEW_POST_ACTION);
                // indicate that entity modification is in progress, which ensures correct row change event execution
                isModifying = true;
            }

        };
        action.setEnabled(true);
        action.putValue(Action.SHORT_DESCRIPTION, "Create new entity");
        action.putValue(Action.MNEMONIC_KEY, KeyEvent.VK_N);
        return action;
    }

    /**
     * Ensures that all modified editors commit their values. This method can be overridden if need to flush not only the managed entity, but potentially other depended entities.
     */
    protected void flushPropertyEditors() {
        getManagedEntity().isValid();
    }

    @Override
    protected Action createRefreshAction() {
        final Command<List<D>> action = new Command<List<D>>("Refresh") {
            private static final long serialVersionUID = 1L;

            @Override
            protected boolean preAction() {
                notifyActionStageChange(ActionStage.REFRESH_PRE_ACTION);
                setState(UmState.UNDEFINED);
                Binder.commitFocusOwner();
                return super.preAction();
            }

            @Override
            protected List<D> action(final ActionEvent event) throws Exception {
                try {
                    notifyActionStageChange(ActionStage.REFRESH_ACTION);
                    // wait before the commit process finish
                    // this is required to ensure that the refreshed values are not overridden with un-flushed values
                    flushPropertyEditors();
                    return loadDetails();
                } catch (final Exception ex) {
                    setState(UmState.VIEW);
                    throw ex;
                }
            }

            @Override
            protected void postAction(final List<D> data) {
                getTableModel().clearInstances();
                // weird cast occurs here because it is not possible to create array with elements of type D
                ((PropertyTableModel) getTableModel()).addInstances(data.toArray(new AbstractEntity[] {}));
                getTableModel().fireTableDataChanged();

                if (data.size() > 0) {
                    final int row = getTableModel().getRowOf(getManagedEntity());
                    getTableModel().selectRow(row >= 0 ? row : 0); // changing row will  trigger onRowSelect listener, which sets the VIEW status
                } else {
                    setManagedEntity(determineManagedEntity(data));
                    setState(UmState.VIEW); // reset the state again to re-bind editors to the new managed entity.
                }

                notifyActionStageChange(ActionStage.REFRESH_POST_ACTION);
            }

        };
        action.setEnabled(true);
        action.putValue(Action.SHORT_DESCRIPTION, "Refresh entities");
        action.putValue(Action.MNEMONIC_KEY, KeyEvent.VK_R);
        return action;
    }

    @Override
    protected Action createDeleteAction() {
        final Command<List<D>> action = new Command<List<D>>("Delete") {
            private static final long serialVersionUID = 1L;

            @Override
            protected boolean preAction() {
                notifyActionStageChange(ActionStage.DELETE_PRE_ACTION);
                Binder.commitFocusOwner();

                final boolean flag = super.preAction();
                if (!flag) {
                    return false;
                }

                // if the item is not persistent then it can be safely deleted -- the responsibility for deleting non-persistent entity relies on the relevant companion
                if (!getManagedEntity().isPersisted()) {
                    return true;
                } else {
                    return JOptionPane.showConfirmDialog(getView(), TitlesDescsGetter.getEntityTitleAndDesc(getManagedEntity().getType()).getKey() + " " + getManagedEntity()
                            + " will be deleted. Proceed?", "Delete", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION;
                }
            }

            @Override
            protected List<D> action(final ActionEvent event) throws Exception {
                notifyActionStageChange(ActionStage.DELETE_ACTION);
                // wait before the commit process finish
                getManagedEntity().isValid();

                getCompanion().deleteDetails(getEntity(), getManagedEntity());

                return loadDetails();
            }

            @Override
            protected void postAction(final List<D> data) {
                getTableModel().clearInstances();
                // weird cast occurs here because it is not possible to create array with elements of type D
                ((PropertyTableModel) getTableModel()).addInstances(data.toArray(new AbstractEntity[] {}));
                getTableModel().fireTableDataChanged();
                if (data.size() > 0) {
                    getTableModel().selectRow(0);
                } else {
                    setManagedEntity(determineManagedEntity(data));
                }
                setState(UmState.VIEW);
                notifyActionStageChange(ActionStage.DELETE_POST_ACTION);
            }

        };
        action.setEnabled(true);
        action.putValue(Action.SHORT_DESCRIPTION, "Delete current entity");
        action.putValue(Action.MNEMONIC_KEY, KeyEvent.VK_D);
        return action;
    }

    @Override
    protected SaveAction createSaveAction() {
        final SaveAction action = new SaveAction();
        action.setEnabled(true);
        action.putValue(Action.SHORT_DESCRIPTION, "Save changes");
        action.putValue(Action.MNEMONIC_KEY, KeyEvent.VK_S);
        return action;
    }

    /**
     * Convenient method for updating an existing list of entities with a specified one.
     * 
     * @return
     */
    private List<D> updateDetails(final D changedEntity) {
        final List<D> data = new ArrayList<D>(getTableModel().instances());
        boolean shouldAdd = true;
        for (int index = 0; index < data.size(); index++) {
            final D entity = data.get(index);
            if (entity.equals(changedEntity)) {
                data.set(index, changedEntity);
                shouldAdd = false;
                break;
            }
        }
        if (shouldAdd) {
            data.add(getManagedEntity());
        }
        return data;
    }

    public PropertyTableModel<D> getTableModel() {
        return tableModel;
    }

    public ListSelectionListener getOnRowSelect() {
        return onRowSelect;
    }

    protected void setModifying(final boolean isModifying) {
        this.isModifying = isModifying;
    }

    /**
     * Inner public class to be reused if necessary downstream for customizing save action.
     * 
     * @author TG Team
     * 
     */
    public class SaveAction extends Command<Result> {
        private static final long serialVersionUID = 1L;

        public SaveAction() {
            super("Save");
        }

        @Override
        protected boolean preAction() {
            notifyActionStageChange(ActionStage.SAVE_PRE_ACTION);
            getCancelAction().setEnabled(false);

            lockBlockingLayerIfProvided(true);
            setMessageForBlockingLayerIfProvided("Saving...");

            return super.preAction();
        }

        @Override
        protected Result action(final ActionEvent arg0) throws Exception {
            notifyActionStageChange(ActionStage.SAVE_ACTION);
            try {
                // wait for all the values entered into controls to be flushed (committed)
                final Result res = getManagedEntity().isValid();
                if (res.isSuccessful()) {
                    setManagedEntity(getCompanion().saveDetails(getEntity(), getManagedEntity()));
                    return new Result(getManagedEntity(), "All is cool.");
                } else {
                    return res;
                }
            } catch (final Exception ex) {
                return new Result(getEntity(), ex);
            }
        }

        @Override
        protected void postAction(final Result result) {
            try {
                if (result.isSuccessful()) {
                    // before re-associating data with the model change set the flag to indicate that all entity changes are completed, which ensures correct row change event execution
                    isModifying = false;
                    if (getView() != null) {
                        getView().notify("", MessageType.NONE);
                    }
                    final List<D> data = updateDetails(getManagedEntity());
                    getTableModel().clearInstances();
                    getTableModel().addInstances(data);
                    getTableModel().fireTableDataChanged();
                    final int row = data.indexOf(getManagedEntity());
                    if (row >= 0) {
                        getTableModel().selectRow(row);
                    } else {
                        setManagedEntity(determineManagedEntity(data));
                    }
                    setState(UmState.VIEW);
                    notifyActionStageChange(ActionStage.SAVE_POST_ACTION_SUCCESSFUL);
                } else {
                    if (getView() != null) {
                        getView().notify(result.getMessage(), MessageType.ERROR);
                    } else {
                        new DialogWithDetails(null, "Save", result.getEx()).setVisible(true);
                    }
                    getCancelAction().setEnabled(true);
                    super.postAction(result);
                    notifyActionStageChange(ActionStage.SAVE_POST_ACTION_FAILED);
                }
            } finally {
                lockBlockingLayerIfProvided(false);
            }
        }

    }

}
