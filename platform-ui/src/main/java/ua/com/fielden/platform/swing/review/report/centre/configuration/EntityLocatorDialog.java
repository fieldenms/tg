package ua.com.fielden.platform.swing.review.report.centre.configuration;

import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import org.jfree.ui.RefineryUtilities;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.reflection.TitlesDescsGetter;
import ua.com.fielden.platform.swing.components.blocking.BlockingIndefiniteProgressLayer;
import ua.com.fielden.platform.swing.components.smart.autocompleter.development.AutocompleterTextFieldLayerWithEntityLocator;
import ua.com.fielden.platform.swing.components.smart.autocompleter.development.AutocompleterUiWithEntityLocator;
import ua.com.fielden.platform.swing.review.report.events.LocatorConfigurationEvent;
import ua.com.fielden.platform.swing.review.report.events.LocatorEvent;
import ua.com.fielden.platform.swing.review.report.interfaces.ILocatorConfigurationEventListener;
import ua.com.fielden.platform.swing.review.report.interfaces.ILocatorEventListener;
import ua.com.fielden.platform.utils.EntityUtils;
import ua.com.fielden.platform.utils.ResourceLoader;

public class EntityLocatorDialog<VT extends AbstractEntity<?>, RT extends AbstractEntity<?>> extends JDialog {

    private static final long serialVersionUID = 7943323636459934782L;

    private final LocatorConfigurationView<VT, RT> locatorConfigurationView;

    /**
     * The focus listener for binded autocompleter.
     */
    private final FocusListener autocompleterFocusListener;

    /**
     * The autocompleter to which this locator is binded.
     */
    private AutocompleterTextFieldLayerWithEntityLocator<VT> autocompleter;

    /**
     * Properties those holds the information about the autocompleter state.
     */
    private int startSelectedIndex, endSelectedIndex, previousCaretPosition;

    public EntityLocatorDialog(final LocatorConfigurationModel<VT, RT> locatorConfigurationModel, final boolean isMulti){
	//	this.textFieldLayer = new AutocompleterTextFieldLayerWithEntityLocator<VT>(entity, locatorConfigurationModel.name, //
	//		textComponent, locatorConfigurationModel.entityFactory, createEntityLocatorValueMatcher(), entityMasterFactory,//
	//		expression, cellRenderer, caption, valueSeparator);
	super(null, "", ModalityType.APPLICATION_MODAL);
	//TODO must be changed later, when the locatorManager will allow one to determine the type of locator: default or local.
	//locatorType = defineLocatorType(locatorConfigurationModel);

	//Configuring locator configuration mode. Add save, save as default and load default listeners those change the locator type.


	//Configuring the content view of the locator dialog.
	final BlockingIndefiniteProgressLayer progressLayer = new BlockingIndefiniteProgressLayer(null, "");
	this.locatorConfigurationView = new LocatorConfigurationView<VT, RT>(locatorConfigurationModel, progressLayer, isMulti);
	this.locatorConfigurationView.addLocatorEventListener(createLocatorEventListener());
	this.locatorConfigurationView.addLocatorConfigurationEventListener(createLocatorConfigurationListener());
	this.autocompleterFocusListener = createComponentFocusListener();
	progressLayer.setView(locatorConfigurationView);
	getContentPane().add(progressLayer);

	//Configuring the entity locator's dialog.
	setPreferredSize(new Dimension(800, 600));
	setIconImage(ResourceLoader.getImage("images/tg-icon.png"));
	setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
	addWindowListener(createWindowCloseHandler());
	//setTitle(generateTitle(locatorType, locatorConfigurationModel.entityType, locatorConfigurationModel.name));

    }

    /**
     * Binds this entity locator dialog to the passed autocompleter.
     *
     * @param autocompleter
     */
    @SuppressWarnings("unchecked")
    public void bindToAutocompleter(final AutocompleterTextFieldLayerWithEntityLocator<VT> autocompleter){
	if(this.autocompleter == autocompleter){
	    return;
	}
	if(autocompleter == null){
	    this.autocompleter.getAutocompleter().getTextComponent().removeFocusListener(autocompleterFocusListener);
	    this.autocompleter = null;
	} else {
	    final AutocompleterUiWithEntityLocator<VT> ui = (AutocompleterUiWithEntityLocator<VT>) autocompleter.getUI();
	    if(ui.getEntityLocatorDialog() == this){
		if(this.autocompleter != null){
		    this.autocompleter.getAutocompleter().getTextComponent().removeFocusListener(autocompleterFocusListener);
		}
		this.autocompleter = autocompleter;
		this.autocompleter.getAutocompleter().getTextComponent().addFocusListener(autocompleterFocusListener);
	    } else {
		throw new IllegalArgumentException("The autocompleter is not bind with this entity locaotr dialog!");
	    }
	}

    }

    /**
     * Shows this entity locator dialog.
     */
    public void showDialog(){
	//save the autocompleter's state.
	previousCaretPosition = autocompleter.getView().getCaretPosition();
	startSelectedIndex = autocompleter.getView().getSelectionStart();
	endSelectedIndex = autocompleter.getView().getSelectionEnd();

	//Configuring the entity locator title.
	setTitle(generateTitle());

	//Configuring the content and size of the entity locator and display it.
	setPreferredSize(new Dimension(800, 600));
	pack();
	RefineryUtilities.centerFrameOnScreen(this);
	locatorConfigurationView.open();
	setVisible(true);
    }

    /**
     * Returns the list of selected entities.
     *
     * @return
     */
    private List<VT> getSelectedEntities(){
	if(locatorConfigurationView.getPreviousView() != null){
	    return EntityUtils.makeNotEnhanced(locatorConfigurationView.getPreviousView().getSelectedEntities());
	}
	return new ArrayList<VT>();
    }

    /**
     * Creates the {@link WindowListener} that handles entity locator's close event.
     *
     * @return
     */
    private WindowListener createWindowCloseHandler() {
	return new WindowAdapter() {
	    @Override
	    public void windowClosing(final WindowEvent e) {
		final LocatorConfigurationModel<VT, RT> model = locatorConfigurationView.getModel();
		boolean isChanged = model.isChanged();
		if(!isChanged && model.isInFreezedPhase()){
		    model.discard();
		    isChanged = model.isChanged();
		}
		if (isChanged) {
		    // TODO The logic should be revised after ILocatorManager enhancements!
		    final boolean isFreezed = model.isInFreezedPhase();
		    final Object options[] = { "Save", "Save as default", "No" };
		    final int chosenOption = JOptionPane.showOptionDialog(EntityLocatorDialog.this, "This locator has been changed, would you like to save it?", "Save entity locator configuration", JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, options[0]);
		    switch (chosenOption) {
		    case JOptionPane.YES_OPTION:
			if(isFreezed){
			    model.save();
			}
			model.save();
			break;
		    case JOptionPane.NO_OPTION:
			if(isFreezed){
			    model.save();
			}
			model.saveGlobally();
			model.save();
			break;
		    case JOptionPane.CANCEL_OPTION:
		    case JOptionPane.CLOSED_OPTION:
			if(isFreezed){
			    model.discard();
			}
			model.discard();
			break;
		    }
		} else {
		    model.discard();
		}
		dispose();
		SwingUtilities.windowForComponent(autocompleter.getAutocompleter().getTextComponent()).setVisible(true);
		autocompleter.getAutocompleter().getTextComponent().requestFocusInWindow();
	    }

	};
    }

    /**
     * Returns the {@link ILocatorConfigurationEventListener} that handles the POST_SAVE, POST_SAVE_AS_DEFAULT and LOAD_DEFAULT events and changes the loaded locator type according to the generated event.
     *
     * @return
     */
    private ILocatorConfigurationEventListener createLocatorConfigurationListener() {
	return new ILocatorConfigurationEventListener() {

	    @Override
	    public boolean locatorConfigurationEventPerformed(final LocatorConfigurationEvent event) {
		setTitle(generateTitle());
		return true;
	    }


	};
    }

    /**
     * Creates the window closing event listener.
     *
     * @return
     */
    private ILocatorEventListener createLocatorEventListener() {
	return new ILocatorEventListener() {

	    @Override
	    public void locatorActionPerformed(final LocatorEvent event) {
		final WindowEvent wev = new WindowEvent(EntityLocatorDialog.this, WindowEvent.WINDOW_CLOSING);
		Toolkit.getDefaultToolkit().getSystemEventQueue().postEvent(wev);
	    }
	};
    }

    /**
     * Creates the focus listener for autocompleter's component.
     *
     * @return
     */
    private FocusListener createComponentFocusListener() {
	return new FocusAdapter() {
	    @Override
	    public void focusGained(final FocusEvent e) {
		final List<VT> selectedEntities = getSelectedEntities();
		if (selectedEntities.size() > 0) {
		    final Object selectedString = autocompleter.getAutocompleter().getSelectedHint(selectedEntities, startSelectedIndex, endSelectedIndex, previousCaretPosition);
		    autocompleter.getAutocompleter().acceptHint(selectedString);
		}
	    }
	};
    }

    /**
     * Generates the title for the locator dialog.
     *
     * @return
     */
    private String generateTitle() {
	final LocatorConfigurationModel<VT, RT> locatorConfigurationModel = locatorConfigurationView.getModel();
	return TitlesDescsGetter.getEntityTitleAndDesc(locatorConfigurationModel.getEntityType()).getKey() + " " //
		+ locatorConfigurationModel.getType() + " entity locator";
    }
}
