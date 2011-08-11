package ua.com.fielden.platform.swing.menu;

import java.util.EventListener;
import java.util.EventObject;

import javax.swing.event.EventListenerList;

import net.miginfocom.swing.MigLayout;
import ua.com.fielden.actionpanelmodel.ActionPanelBuilder;
import ua.com.fielden.platform.dao.IEntityDao;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.swing.actions.ActionChanger;
import ua.com.fielden.platform.swing.components.NotificationLayer.MessageType;
import ua.com.fielden.platform.swing.model.DefaultUiModel;
import ua.com.fielden.platform.swing.model.ICloseGuard;
import ua.com.fielden.platform.swing.review.DynamicCriteriaModelBuilder;
import ua.com.fielden.platform.swing.review.DynamicEntityReview;
import ua.com.fielden.platform.swing.review.optionbuilder.ActionChangerBuilder;
import ua.com.fielden.platform.swing.review.wizard.AbstractWizard;
import ua.com.fielden.platform.swing.view.BaseNotifPanel;

/**
 * Ad hoc report wrapper. See {@link BaseNotifPanel} for more information.
 * 
 * @author TG Team
 * 
 */
public class DynamicReportWrapper<T extends AbstractEntity, DAO extends IEntityDao<T>, R extends AbstractEntity> extends BaseNotifPanel<DefaultUiModel> {

    private static final long serialVersionUID = 1655601830703524962L;
    private final DynamicCriteriaModelBuilder<T, DAO, R> modelBuilder;
    private final EventListenerList listenerList = new EventListenerList();
    private final ActionChangerBuilder actionChangerBuilder = new ActionChangerBuilder();

    private final TreeMenuWithTabs<?> treeMenu;

    private boolean isClosing = false;

    private ActionPanelBuilder panelBuilder;

    private final String description;

    /**
     * Creates new {@link DynamicReportWrapper} for the given {@link DynamicCriteriaModelBuilder} and with specified title and information about the wrapped report.
     * 
     * @param caption
     * @param description
     * @param modelBuilder
     */
    public DynamicReportWrapper(final String caption, final String description, final DynamicCriteriaModelBuilder<T, DAO, R> modelBuilder, final TreeMenuWithTabs<?> treeMenu) {
	super(caption, new DefaultUiModel(true));
	this.modelBuilder = modelBuilder;
	this.description = description;
	this.treeMenu = treeMenu;
	getModel().setView(this);
    }

    @Override
    public void buildUi() {
	getHoldingPanel().setLayout(new MigLayout("fill, insets 0", "[fill, grow]", "[c,grow,fill]"));
	getDynamicCriteriaModelBuilder().init(getHoldingPanel(), actionChangerBuilder, panelBuilder, false, !(getAssociatedTreeMenuItem() instanceof MiRemovableDynamicReport));
	treeMenu.updateSelectedMenuItem();
    }

    @Override
    public String getInfo() {
	return description;
    }

    public DynamicCriteriaModelBuilder<T, DAO, R> getDynamicCriteriaModelBuilder() {
	return modelBuilder;
    }

    public void setSaveAsAction(final ActionChanger<?> saveAsAction) {
	actionChangerBuilder.setAction(saveAsAction);
    }

    public void setSaveAction(final ActionChanger<?> saveAction) {
	actionChangerBuilder.setAction(saveAction);
    }

    public void setRemoveAction(final ActionChanger<?> removeAction) {
	actionChangerBuilder.setAction(removeAction);
    }

    public void setPanelBuilder(final ActionPanelBuilder panelBuilder) {
	this.panelBuilder = panelBuilder;
    }

    /**
     * Returns value that indicates whether wrapper holds review or wizard model.
     * 
     * @return
     */
    public boolean isReview() {
	return getHoldingPanel().getComponent(0) instanceof DynamicEntityReview ? true : false;
    }

    /**
     * Returns {@link DynamicEntityReview} instance if {@link #isReview()} is true otherwise returns null.
     * 
     * @return
     */
    public DynamicEntityReview<T, DAO, R> getView() {
	if (isReview()) {
	    return (DynamicEntityReview<T, DAO, R>) getHoldingPanel().getComponent(0);
	}
	return null;
    }

    /**
     * Returns {@link DynamicCriteriaWizard} instance if {@link #isReview()} is false otherwise it returns null.
     * 
     * @return
     */
    public AbstractWizard getWizard() {
	if (!isReview()) {
	    return (AbstractWizard) getHoldingPanel().getComponent(0);
	}
	return null;
    }

    @Override
    public ICloseGuard canClose() {
	isClosing = true;
	final ICloseGuard result = super.canClose();
	if (result != null) {
	    return result;
	}

	if (getDynamicCriteriaModelBuilder().isCriteriaModelChanged()) {
	    treeMenu.selectItemWithView(this);
	}
	ICloseGuard closeGuard = null;
	if (isReview()) {
	    final DynamicEntityReview<T, DAO, R> review = getView();
	    closeGuard = review.canClose();
	} else {
	    final AbstractWizard wizard = getWizard();
	    closeGuard = wizard.canClose();
	}

	if (closeGuard != null) {
	    isClosing = false;
	    return this;
	}
	isClosing = false;
	return null;
    }

    public boolean isClosing() {
	return isClosing;
    }

    public TreeMenuWithTabs<?> getTreeMenu() {
	return treeMenu;
    }

    @Override
    public void notify(final String message, final MessageType messageType) {
	boolean cancelClosing = false;
	if (isReview()) {
	    final DynamicEntityReview<T, DAO, R> review = getView();
	    cancelClosing = review.wasClosingCanceled();
	} else {
	    final AbstractWizard wizard = getWizard();
	    cancelClosing = wizard.wasClosingCanceled();
	}
	if (!cancelClosing) {
	    super.notify(message, messageType);
	}

    }

    @Override
    public void close() {
	super.close();
	fireCentreClosingEvent(new CentreClosingEvent(this));
    }

    @Override
    public boolean canLeave() {
	return true;
    }

    public void addCentreClosingListener(final CentreClosingListener l) {
	listenerList.add(CentreClosingListener.class, l);
    }

    public void removeFooListener(final CentreClosingListener l) {
	listenerList.remove(CentreClosingListener.class, l);
    }

    protected void fireCentreClosingEvent(final CentreClosingEvent event){
	final Object[] listeners = listenerList.getListenerList();
	// Process the listeners last to first, notifying
	// those that are interested in this event
	for (int i = listeners.length - 2; i >= 0; i -= 2) {
	    if (listeners[i] == CentreClosingListener.class) {
		((CentreClosingListener) listeners[i + 1]).centreClosing(event);
	    }
	}
    }

    /**
     * Contract for anything that is interested in receiving {@link CentreClosingEvent}.
     * 
     * @author TG Team
     *
     */
    public static interface CentreClosingListener extends EventListener{

	/**
	 * Invoked after centre was closed to perform custom additional task.
	 * 
	 * @param event
	 */
	void centreClosing(final CentreClosingEvent event);
    }

    /**
     * Represents centre closing event.
     * 
     * @author TG Team
     *
     */
    public static class CentreClosingEvent extends EventObject {

	private static final long serialVersionUID = 7671961023819217439L;

	/**
	 * Constructs centre closing event.
	 * 
	 * @param source
	 */
	public CentreClosingEvent(final Object source) {
	    super(source);
	}

    }
}
