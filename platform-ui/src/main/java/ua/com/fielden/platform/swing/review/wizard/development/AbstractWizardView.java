package ua.com.fielden.platform.swing.review.wizard.development;

import java.awt.event.ActionEvent;

import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JPanel;

import net.miginfocom.swing.MigLayout;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.swing.actions.BlockingLayerCommand;
import ua.com.fielden.platform.swing.components.blocking.BlockingIndefiniteProgressLayer;
import ua.com.fielden.platform.swing.review.report.events.WizardEvent;
import ua.com.fielden.platform.swing.review.report.interfaces.IWizard;
import ua.com.fielden.platform.swing.review.report.interfaces.IWizardEventListener;
import ua.com.fielden.platform.swing.review.wizard.tree.editor.DomainTreeEditorModel;
import ua.com.fielden.platform.swing.review.wizard.tree.editor.DomainTreeEditorView;
import ua.com.fielden.platform.swing.view.BasePanel;

/**
 * Generic implementation for domain tree wizard. This wizard defines basic user interface and functionality that might be extended only for configuring purposes.
 * 
 * @author TG Team
 *
 * @param <T>
 */
public abstract class AbstractWizardView<T extends AbstractEntity> extends BasePanel implements IWizard{

    private static final long serialVersionUID = 268187881676011630L;

    private final DomainTreeEditorModel<T> treeEditorModel;
    private final BlockingIndefiniteProgressLayer progressLayer;
    private final Action buildAction, cancelAction;

    public AbstractWizardView(final DomainTreeEditorModel<T> treeEditorModel, final BlockingIndefiniteProgressLayer progressLayer){
	this.treeEditorModel = treeEditorModel;
	this.progressLayer = progressLayer;

	this.buildAction = createBuildAction();
	this.cancelAction = createCancelAction();

	initView();


	//	final JPanel actionPanel = new JPanel(new MigLayout("fill, insets 0", "[][][]30:push[fill, :100:][fill, :100:]", "[c]"));
	//	actionPanel.add(DummyBuilder.label("Columns"));
	//	actionPanel.add(new JSpinner(createSpinnerModel()));
    }

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

    //TODO must write documentation for that method.
    protected void initView(){
	setLayout(new MigLayout("fill, insets 0", "[fill, grow]", "[fill, grow][]"));

	//Create domain tree editor (i.e. domain property tree with expression editor).
	final DomainTreeEditorView<T> domainTreeEditorView = new DomainTreeEditorView<T>(getTreeEditorModel());

	//Creates action panel with build and cancel actions.
	final JPanel actionPanel = new JPanel(new MigLayout("fill, insets 10", "push[fill, :100:][fill, :100:]", "[c]"));
	actionPanel.add(new JButton(getBuildAction()));
	actionPanel.add(new JButton(getCancelAction()));

	add(domainTreeEditorView, "wrap");
	add(actionPanel);
    }

    protected Action createBuildAction(){
	return createWizardAction(true, "Build");
    }

    protected Action createCancelAction(){
	return createWizardAction(false, "Cancel");
    }

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

    public final Action getCancelAction() {
	return buildAction;
    }

    public final Action getBuildAction() {
	return cancelAction;
    }

    public DomainTreeEditorModel<T> getTreeEditorModel() {
	return treeEditorModel;
    }

    @Override
    public String getInfo() {
	return "Abstract wizard view";
    }

    @Override
    public void addWizardEventListener(final IWizardEventListener l) {
	listenerList.add(IWizardEventListener.class, l);
    }

    @Override
    public void removeWizardEventListener(final IWizardEventListener l) {
	listenerList.remove(IWizardEventListener.class, l);
    }

    //    private SpinnerModel createSpinnerModel() {
    //	final SpinnerModel spinnerModel = new SpinnerNumberModel(1, 1, 4, 1);
    //	spinnerModel.addChangeListener(new ChangeListener() {
    //
    //	    @Override
    //	    public void stateChanged(final ChangeEvent e) {
    //		getModel().
    //	    }
    //	});
    //	return spinnerModel;
    //    }
}
