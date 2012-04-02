package ua.com.fielden.platform.swing.review.wizard.development;

import java.awt.event.ActionEvent;

import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JPanel;

import net.miginfocom.swing.MigLayout;
import ua.com.fielden.platform.domaintree.IDomainTreeManager;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.swing.actions.BlockingLayerCommand;
import ua.com.fielden.platform.swing.components.blocking.BlockingIndefiniteProgressLayer;
import ua.com.fielden.platform.swing.review.development.SelectableBasePanel;
import ua.com.fielden.platform.swing.review.report.events.WizardEvent;
import ua.com.fielden.platform.swing.review.report.events.WizardEvent.WizardAction;
import ua.com.fielden.platform.swing.review.report.interfaces.IWizard;
import ua.com.fielden.platform.swing.review.report.interfaces.IWizardEventListener;
import ua.com.fielden.platform.swing.review.wizard.tree.editor.DomainTreeEditorModel;
import ua.com.fielden.platform.swing.review.wizard.tree.editor.DomainTreeEditorView;
import ua.com.fielden.platform.swing.utils.DummyBuilder;

/**
 * Generic implementation for domain tree wizard. This wizard defines basic user interface and functionality that might be extended only for configuring purposes.
 *
 * @author TG Team
 *
 * @param <T>
 */
public abstract class AbstractWizardView<T extends AbstractEntity<?>> extends SelectableBasePanel implements IWizard{

    private static final long serialVersionUID = 268187881676011630L;

    private final BlockingIndefiniteProgressLayer progressLayer;

    private final DomainTreeEditorView<T> treeEditorView;

    private final String domainEditorCaption;

    //Parts of the action panel. (i.e. the panel with build and cancel buttons and other controls).
    private final JPanel actionPanel;
    private final Action buildAction, cancelAction;

    /**
     * Initiates this {@link AbstractWizardView} and creates main parts of the entity review wizard (domain tree editor and action panel).
     *
     * @param treeEditorModel
     * @param progressLayer
     */
    public AbstractWizardView(final DomainTreeEditorModel<T> treeEditorModel, final String domainEditorCaption, final BlockingIndefiniteProgressLayer progressLayer){
	this.progressLayer = progressLayer;
	this.domainEditorCaption = domainEditorCaption;
	//Initiates wizards main parts and components.
	this.treeEditorView = new DomainTreeEditorView<T>(treeEditorModel);
	this.buildAction = createBuildAction();
	this.cancelAction = createCancelAction();
	this.actionPanel = createActionPanel();
    }

    /**
     * Returns the {@link IDomainTreeManager} associated with {@link DomainTreeEditorModel}.
     * 
     * @return
     */
    public IDomainTreeManager getDomainTreeManager(){
	return getTreeEditorView().getModel().getDomainTreeManagerAndEnhancer();
    }

    /**
     * Returns the domain tree editor for this {@link AbstractWizardView}.
     *
     * @return
     */
    public final DomainTreeEditorView<T> getTreeEditorView() {
	return treeEditorView;
    }

    /**
     * Returns the action panel for this {@link AbstractWizardView}.
     *
     * @return
     */
    public final JPanel getActionPanel() {
	return actionPanel;
    }

    /**
     * Returns action that allows one to cancel changes made in the domain tree editor view and switch back to report view.
     *
     * @return
     */
    public final Action getCancelAction() {
	return buildAction;
    }

    /**
     * Returns an action that allows one to apply changes made in the domain tree editor and build report view.
     *
     * @return
     */
    public final Action getBuildAction() {
	return cancelAction;
    }

    @Override
    public String getInfo() {
	return "An wizard for entity review.";
    }

    @Override
    public void addWizardEventListener(final IWizardEventListener l) {
	listenerList.add(IWizardEventListener.class, l);
    }

    @Override
    public void removeWizardEventListener(final IWizardEventListener l) {
	listenerList.remove(IWizardEventListener.class, l);
    }

    /**
     * Might be overridden to provide custom build action (see {@link #getBuildAction()} for more information about the purpose of this action).
     *
     * @return
     */
    protected Action createBuildAction(){
	return createWizardAction(true, "Build");
    }

    /**
     * Might be overridden to provide custom cancel action (see {@link #getCancelAction()} for more information about the purpose of this action).
     *
     * @return
     */
    protected Action createCancelAction(){
	return createWizardAction(false, "Cancel");
    }

    /**
     * Might be overridden if there is need to add some other controls to the action panel.
     *
     * @return
     */
    protected JPanel createActionPanel() {
	final JPanel actionPanel = new JPanel(new MigLayout("fill, insets 0", "push[fill, :100:][fill, :100:]", "[c]"));
	actionPanel.add(new JButton(getBuildAction()));
	actionPanel.add(new JButton(getCancelAction()));
	return actionPanel;
    }

    /**
     * Layouts the components of this wizard view.
     */
    protected void layoutComponents(){
	setLayout(new MigLayout("fill, insets 5", "[fill, grow]", "[][fill, grow][]"));

	add(DummyBuilder.label(domainEditorCaption), "wrap");
	add(getTreeEditorView(), "wrap");
	add(getActionPanel());
    }

    /**
     * Notifies all the registered listeners with specified {@link WizardEvent} instance.
     *
     * @param ev
     * @return
     */
    protected boolean notifyWizardAction(final WizardEvent ev) {
	// Guaranteed to return a non-null array
	final IWizardEventListener[] listeners = getListeners(IWizardEventListener.class);
	// Process the listeners last to first, notifying
	// those that are interested in this event
	boolean result = true;

	for (final IWizardEventListener listener : listeners) {
	    result &= listener.wizardActionPerformed(ev);
	}
	return result;
    }

    /**
     * Creates an specific wizard's action (i.e. creates build or cancel actions).
     *
     * @param build
     * @param name
     * @return
     */
    private Action createWizardAction(final boolean build, final String name){
	return new BlockingLayerCommand<Void>(name, progressLayer){

	    private static final long serialVersionUID = 4502256665545168359L;

	    @Override
	    protected boolean preAction() {
		final boolean result = super.preAction();
		if(!result){
		    return false;
		}
		return notifyWizardAction(new WizardEvent(AbstractWizardView.this, build ? WizardAction.PRE_BUILD : WizardAction.PRE_CANCEL));
	    }

	    @Override
	    protected Void action(final ActionEvent e) throws Exception {
		notifyWizardAction(new WizardEvent(AbstractWizardView.this, build ? WizardAction.BUILD : WizardAction.CANCEL));
		return null;
	    }

	    @Override
	    protected void postAction(final Void value) {
		notifyWizardAction(new WizardEvent(AbstractWizardView.this, build ? WizardAction.POST_BUILD : WizardAction.POST_CANCEL));
		super.postAction(value);
	    }

	};
    }
}
