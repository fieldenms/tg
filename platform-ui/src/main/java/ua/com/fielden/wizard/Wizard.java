package ua.com.fielden.wizard;

import java.awt.CardLayout;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JButton;
import javax.swing.JPanel;

import net.miginfocom.swing.MigLayout;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.matcher.IValueMatcherFactory;
import ua.com.fielden.platform.error.Result;
import ua.com.fielden.platform.swing.actions.BlockingLayerCommand;
import ua.com.fielden.platform.swing.actions.Command;
import ua.com.fielden.platform.swing.components.bind.development.BoundedValidationLayer;
import ua.com.fielden.platform.swing.components.blocking.BlockingIndefiniteProgressLayer;
import ua.com.fielden.platform.swing.ei.development.MasterPropertyBinder;
import ua.com.fielden.platform.swing.ei.editors.development.ILightweightPropertyBinder;
import ua.com.fielden.platform.swing.ei.editors.development.IPropertyEditor;
import ua.com.fielden.platform.swing.ei.editors.development.ReadonlyEntityPropertyViewer;
import ua.com.fielden.platform.swing.utils.Dialogs;
import ua.com.fielden.platform.swing.utils.SwingUtilitiesEx;
import ua.com.fielden.platform.swing.view.BasePanel;

public class Wizard<T extends AbstractEntity<?>> extends BasePanel {

    private static final long serialVersionUID = 1L;
    private final String title;
    private final String info;
    private final T model;
    protected final ILightweightPropertyBinder<T> propBinder;
    protected final Map<String, IPropertyEditor> editors;
    protected final Map<String, IPropertyEditor> aliasedEditors = new HashMap<>(); // cannot be rebound by standard means

    private IWizState<T> currState;
    private IWizState<T> startState;

    private final JPanel holdingPanel = new JPanel(new MigLayout("fill", "[fill, grow]", "[c,grow,fill][fill,c]"));
    private final CardLayout cardLayout = new CardLayout();
    private final JPanel pagePanel = new JPanel(cardLayout);
    private final JPanel navPanel = new JPanel(new MigLayout("fill, insets 0", "[:50:]20:push[:50:][:50:]", "[c]"));
    private final BlockingIndefiniteProgressLayer blockingLayer = new BlockingIndefiniteProgressLayer(holdingPanel, "");

    private final Command<IWizState<T>> next;
    private final Command<IWizState<T>> prev;
    private final Command<IWizState<T>> cancel;
    private final JButton nextButton;

    private final List<IWizState<T>> states = new ArrayList<>();

    public Wizard(//
    final String title,
    /*	  */final String info,//
	    final T model, //
	    final IValueMatcherFactory valueMatcherFactory, //
	    final List<IWizState<T>> states) {
	super.setLayout(new MigLayout("fill, insets 0", "[c,fill,grow]", "[c,grow,fill]"));
	super.add(blockingLayer);

	this.states.addAll(states);

	holdingPanel.add(pagePanel, "wrap");
	holdingPanel.add(navPanel);

	this.title = title;
	this.info = info;
	this.model = model;
	this.propBinder = MasterPropertyBinder.<T> createPropertyBinderWithoutLocatorSupport(valueMatcherFactory);
	this.editors = buildEditors(model, propBinder);

	next = createNextCommand();
	prev = createPrevCommand();
	cancel = createCancelCommand();

	navPanel.add(new JButton(cancel));
	navPanel.add(new JButton(prev));
	navPanel.add(nextButton = new JButton(next));

	startState = states.get(0);
	setCurrState(startState);
    }

    public void buildUi() {
	// initialise all pages by building their UI
	for (final IWizState<T> state : states) {
	    final AbstractWizPage<T> page = state.view();
	    page.buildUi(this);
	    pagePanel.add(page, state.name());
	}
    }

    protected Command<IWizState<T>> createNextCommand() {
	final Command<IWizState<T>> command = new BlockingLayerCommand<IWizState<T>>("Next", blockingLayer) {

	    private Result result = Result.successful(currState);

	    @Override
	    protected boolean preAction() {
		result = Result.successful(currState);
		setMessage("Validating...");
		commitEditors();

		return super.preAction();
	    }

	    @Override
	    protected IWizState<T> action(final ActionEvent e) throws Exception {
		result = currState.isValid();
		if (!result.isSuccessful()) {
		    return currState;
		}

		final IWizState<T> prevState = currState;
		final IWizState<T> nextState;
		if (currState instanceof IWizStartState) {
		    setMessage("Next...");
		    nextState = ((IWizStartState<T>) currState).next();
		} else if (currState instanceof IWizTransState) {
		    setMessage("Next...");
		    nextState = ((IWizTransState<T>) currState).next();
		} else if (currState instanceof IWizFinalState) {
		    setMessage("Finishing...");
		    nextState = ((IWizFinalState<T>) currState).finish();
		} else {
		    nextState = null;
		}

		if (nextState != null && nextState != prevState) {
		    nextState.setTransitionedFrom(prevState);
		}
		return nextState;
	    }

	    @Override
	    protected void postAction(final IWizState<T> nextState) {
		if (!result.isSuccessful()) {
		    Dialogs.showMessageDialog(Wizard.this, //
			    "<html>There are validation errors. Please correct them and try again."
			    + "<br><br>" + result.getMessage() + "</html>", "Wizard Validaton Errors", Dialogs.ERROR_MESSAGE);
		    super.postAction(nextState);
		} else if (nextState != currState && nextState != null) {
		    setCurrState(nextState);
		    cardLayout.show(pagePanel, nextState.name());
		    super.postAction(nextState);

		    // focus the preferred property editor if it was specified
		    final IPropertyEditor editor = editors.get(model.getPreferredProperty());
		    if (editor != null) {
			editor.getEditor().requestFocusInWindow();
		    }
		} else {
		    model.restoreToOriginal();
		    rebindEditors();
		    setCurrState(startState);
		    cardLayout.show(pagePanel, currState.name());
		    super.postAction(nextState);
		}
	    }
	};
	return command;
    }

    protected Command<IWizState<T>> createPrevCommand() {
	final Command<IWizState<T>> command = new BlockingLayerCommand<IWizState<T>>("Prev", blockingLayer) {

	    @Override
	    protected IWizState<T> action(final ActionEvent e) throws Exception {
		commitEditors();

		if (currState instanceof IWizTransState) {
		    setMessage("Previous...");
		    setCurrState(((IWizTransState<T>) currState).prev());
		} else if (currState instanceof IWizFinalState) {
		    setMessage("Previous...");
		    setCurrState(((IWizFinalState<T>) currState).prev());
		}
		return currState;
	    }

	    @Override
	    protected void postAction(final IWizState<T> state) {
		if (state != null) {
		    cardLayout.show(pagePanel, state.name());
		}
		super.postAction(state);
		setCurrState(state);
		nextButton.requestFocusInWindow();
	    }
	};
	return command;
    }

    protected Command<IWizState<T>> createCancelCommand() {
	final Command<IWizState<T>> command = new BlockingLayerCommand<IWizState<T>>("Cancel", blockingLayer) {

	    @Override
	    protected IWizState<T> action(final ActionEvent e) throws Exception {
		setMessage("Cancelling...");

		commitEditors();

		if (currState instanceof IWizTransState) {
		    setCurrState(((IWizTransState<T>) currState).cancel());
		} else if (currState instanceof IWizFinalState) {
		    setCurrState(((IWizFinalState<T>) currState).cancel());
		}
		return currState;
	    }

	    @Override
	    protected void postAction(final IWizState<T> state) {
		if (state != null) {
		    model.restoreToOriginal();
		    rebindEditors();
		    cardLayout.show(pagePanel, state.name());
		}
		super.postAction(state);
		setCurrState(state);
		nextButton.requestFocusInWindow();
	    }
	};
	return command;
    }

    protected void rebindEditors() {
	propBinder.rebind(editors, model);
	for (final IPropertyEditor editor : aliasedEditors.values()) {
	    editor.bind(model);
	}
    }

    protected void commitEditors() {
	for (final IPropertyEditor component : editors.values()) {
	    if (component.getEditor() instanceof BoundedValidationLayer) {
		final BoundedValidationLayer bvl = (BoundedValidationLayer) component.getEditor();
		if (bvl.canCommit()) {
		    bvl.commit();
		}
	    }
	}
    }


    protected void setCurrState(final IWizState<T> state) {
	this.currState = state;

	SwingUtilitiesEx.invokeLater(new Runnable() {
	    @Override
	    public void run() {
		if (currState instanceof IWizStartState) {
		    cancel.setEnabled(false);
		    next.setEnabled(true);
		    prev.setEnabled(false);
		} else if (currState instanceof IWizTransState) {
		    cancel.setEnabled(true);
		    next.setEnabled(true);
		    prev.setEnabled(true);
		} else if (currState instanceof IWizFinalState) {
		    cancel.setEnabled(true);
		    next.setEnabled(true);
		    prev.setEnabled(true);
		}
	    }
	});
    }

    public void lock() {
	blockingLayer.setLocked(true);
    }

    /**
     * Unlocks the panel. Must be invoked on EDT.
     */
    public void unlock() {
	blockingLayer.setLocked(false);
    }

    /**
     * Sets message on the blocking layer, which is displayed while panel is locked.
     *
     * @param msg
     */
    public void setBlockingMessage(final String msg) {
	blockingLayer.setText(msg);
    }

    public Map<String, IPropertyEditor> getEditors() {
	final Map<String, IPropertyEditor> allEditors = new HashMap<>(editors);
	allEditors.putAll(aliasedEditors);
	return allEditors;
    }

    public final void addPropertyViewer(final String dotNotatatedPropertyName, final String alias) {
	aliasedEditors.put(alias, new ReadonlyEntityPropertyViewer(model, dotNotatatedPropertyName));
    }

    public final void addPropertyViewer(final String dotNotatatedPropertyName) {
	editors.put(dotNotatatedPropertyName, new ReadonlyEntityPropertyViewer(model, dotNotatatedPropertyName));
    }

    protected Map<String, IPropertyEditor> buildEditors(final T entity, final ILightweightPropertyBinder<T> propertyBinder) {
	return propertyBinder.bind(entity);
    }

    @Override
    public String getInfo() {
	return info;
    }

    @Override
    public String toString() {
	return title;
    }

}
