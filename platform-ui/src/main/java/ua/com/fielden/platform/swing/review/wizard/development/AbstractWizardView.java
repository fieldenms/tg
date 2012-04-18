package ua.com.fielden.platform.swing.review.wizard.development;

import javax.swing.JButton;
import javax.swing.JPanel;

import net.miginfocom.swing.MigLayout;
import ua.com.fielden.platform.domaintree.IDomainTreeManager;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.swing.review.development.SelectableBasePanel;
import ua.com.fielden.platform.swing.review.report.configuration.AbstractConfigurationView;
import ua.com.fielden.platform.swing.review.report.configuration.AbstractConfigurationView.BuildAction;
import ua.com.fielden.platform.swing.review.report.configuration.AbstractConfigurationView.CancelAction;
import ua.com.fielden.platform.swing.review.report.interfaces.IWizard;
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

    private final AbstractConfigurationView<?, ? extends AbstractWizardView<T>> owner;

    private final DomainTreeEditorView<T> treeEditorView;

    private final String domainEditorCaption;

    //Parts of the action panel. (i.e. the panel with build and cancel buttons and other controls).
    private final JPanel actionPanel;
    private final BuildAction buildAction;
    private final CancelAction cancelAction;

    /**
     * Initiates this {@link AbstractWizardView} and creates main parts of the entity review wizard (domain tree editor and action panel).
     *
     * @param treeEditorModel
     * @param progressLayer
     */
    public AbstractWizardView(final AbstractConfigurationView<?, ? extends AbstractWizardView<T>> owner, final DomainTreeEditorModel<T> treeEditorModel, final String domainEditorCaption){
	this.owner = owner;
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
     * Returns the {@link AbstractConfigurationView} instance that owns this wizard.
     * 
     * @return
     */
    public AbstractConfigurationView<?, ? extends AbstractWizardView<T>> getOwner() {
	return owner;
    }

    @Override
    public String getInfo() {
	return "An wizard for entity review.";
    }

    @Override
    public BuildAction getBuildAction() {
	return buildAction;
    }

    @Override
    public CancelAction getCancelAction() {
	return cancelAction;
    }

    /**
     * Might be overridden to provide custom build action (see {@link #getBuildAction()} for more information about the purpose of this action).
     *
     * @return
     */
    abstract protected BuildAction createBuildAction();

    /**
     * Might be overridden to provide custom cancel action (see {@link #getCancelAction()} for more information about the purpose of this action).
     *
     * @return
     */
    abstract protected CancelAction createCancelAction();

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
}
