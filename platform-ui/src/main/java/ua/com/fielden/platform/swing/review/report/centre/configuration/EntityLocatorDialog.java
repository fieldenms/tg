package ua.com.fielden.platform.swing.review.report.centre.configuration;

import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import org.jfree.ui.RefineryUtilities;

import ua.com.fielden.platform.domaintree.ILocatorManager;
import ua.com.fielden.platform.domaintree.centre.ILocatorDomainTreeManager;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.reflection.TitlesDescsGetter;
import ua.com.fielden.platform.swing.components.blocking.BlockingIndefiniteProgressLayer;
import ua.com.fielden.platform.swing.components.smart.autocompleter.development.AutocompleterTextFieldLayerWithEntityLocator;
import ua.com.fielden.platform.swing.review.report.events.LocatorConfigurationEvent;
import ua.com.fielden.platform.swing.review.report.events.LocatorEvent;
import ua.com.fielden.platform.swing.review.report.interfaces.ILocatorConfigurationEventListener;
import ua.com.fielden.platform.swing.review.report.interfaces.ILocatorEventListener;
import ua.com.fielden.platform.utils.ResourceLoader;

public class EntityLocatorDialog<VT extends AbstractEntity, RT extends AbstractEntity> extends JDialog {

    private static final long serialVersionUID = 7943323636459934782L;

    private final LocatorConfigurationView<VT, RT> locatorConfigurationView;

    private AutocompleterTextFieldLayerWithEntityLocator<VT> autocompleter;

    private LoadedLocatorType locatorType = LoadedLocatorType.LOCAL;

    public EntityLocatorDialog(final LocatorConfigurationModel<VT, RT> locatorConfigurationModel, final boolean isMulti){
	//	this.textFieldLayer = new AutocompleterTextFieldLayerWithEntityLocator<VT>(entity, locatorConfigurationModel.name, //
	//		textComponent, locatorConfigurationModel.entityFactory, createEntityLocatorValueMatcher(), entityMasterFactory,//
	//		expression, cellRenderer, caption, valueSeparator);
	super(null, "", ModalityType.APPLICATION_MODAL);
	//TODO must be changed later, when the locatorManager will allow one to determine the type of locator: default or local.
	locatorType = defineLocatorType(locatorConfigurationModel);

	//Configuring locator configuration mode. Add save, save as default and load default listeners those change the locator type.
	locatorConfigurationModel.addLocatorConfigurationEventListener(createLocatorConfigurationListener());

	//Configuring the content view of the locator dialog.
	final BlockingIndefiniteProgressLayer progressLayer = new BlockingIndefiniteProgressLayer(null, "");
	this.locatorConfigurationView = new LocatorConfigurationView<VT, RT>(locatorConfigurationModel, progressLayer, isMulti);
	this.locatorConfigurationView.addLocatorEventListener(createLocatorEventListener());
	progressLayer.setView(locatorConfigurationView);
	getContentPane().add(progressLayer);

	//Configuring the entity locator's dialog.
	setPreferredSize(new Dimension(800, 600));
	setIconImage(ResourceLoader.getImage("images/tg-icon.png"));
	setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
	addWindowListener(createWindowCloseHandler());
	setTitle(generateTitle(locatorType, locatorConfigurationModel.entityType, locatorConfigurationModel.name));

    }

    public void bindToAutocompleter(final AutocompleterTextFieldLayerWithEntityLocator<VT> autocompleter){
	//TODO must also provide condition that determines whether specified autocompleter has this entity locator dialog.
	this.autocompleter = autocompleter;
    }

    /**
     * Shows this entity locator dialog.
     */
    public void showDialog(){
	//Configuring the entity locator title.
	final LocatorConfigurationModel<VT, RT> locatorConfigurationModel = locatorConfigurationView.getModel();
	final Class<VT> entityType = locatorConfigurationModel.entityType;
	final String name = locatorConfigurationModel.name;
	locatorType = defineLocatorType(locatorConfigurationView.getModel());
	setTitle(generateTitle(locatorType, entityType, name));

	//Configuring the content and size of the entity locator and display it.
	locatorConfigurationView.open();
	setPreferredSize(new Dimension(800, 600));
	pack();
	RefineryUtilities.centerFrameOnScreen(this);
	setVisible(true);
    }

    /**
     * Returns the list of selected entities.
     * 
     * @return
     */
    public List<VT> getSelectedEntities(){
	if(locatorConfigurationView.getPreviousView() != null){
	    return locatorConfigurationView.getPreviousView().getSelectedEntities();
	}
	return new ArrayList<VT>();
    }

    /**
     * Determines the type of loaded locator. It might be Default or Local.
     * TODO This might be removed after the ILocatorManager will allow one to determine the locator type.
     * 
     * @param locatorConfigurationModel
     * @return
     */
    private LoadedLocatorType defineLocatorType(final LocatorConfigurationModel<VT, RT> locatorConfigurationModel) {
	final ILocatorManager locatorManager = locatorConfigurationModel.locatorManager;
	final Class<RT> entityType = locatorConfigurationModel.rootType;
	final String propertyName = locatorConfigurationModel.name;
	final ILocatorDomainTreeManager ldtm = locatorManager.getLocatorManager(entityType, propertyName);
	if(ldtm == null){
	    locatorManager.initLocatorManagerByDefault(entityType, propertyName);
	    return LoadedLocatorType.DEFAULT;
	}
	return LoadedLocatorType.LOCAL;
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
		final ILocatorManager locatorManager = locatorConfigurationView.getModel().locatorManager;
		final Class<RT> entityType = locatorConfigurationView.getModel().rootType;
		final String propertyName = locatorConfigurationView.getModel().name;
		//		if(locatorConfigurationView.getPreviousView() == null){
		//		    return;
		//		}
		if (locatorManager.isChangedLocatorManager(entityType, propertyName)) {
		    final Object options[] = { "Save", "Save as default", "No" };
		    final int chosenOption = JOptionPane.showOptionDialog(EntityLocatorDialog.this, "This locator has been changed, would you like to save it?", "Save entity locator configuration", JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, options[0]);
		    switch (chosenOption) {
		    case JOptionPane.YES_OPTION:
			locatorConfigurationView.getModel().save();
			break;
		    case JOptionPane.NO_OPTION:
			locatorConfigurationView.getModel().saveAsDefault();
			break;
		    }
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
		final Class<VT> entityType = locatorConfigurationView.getModel().entityType;
		final String name = locatorConfigurationView.getModel().name;
		switch(event.getEventAction()){
		case POST_SAVE:
		    locatorType = LoadedLocatorType.LOCAL;
		    break;
		case POST_SAVE_AS_DEFAULT:
		case LOAD_DEFAULT:
		    locatorType = LoadedLocatorType.DEFAULT;
		    break;
		default:
		    return true;
		}
		setTitle(generateTitle(locatorType, entityType, name));
		return true;
	    }


	};
    }

    private ILocatorEventListener createLocatorEventListener() {
	return new ILocatorEventListener() {

	    @Override
	    public void locatorActionPerformed(final LocatorEvent event) {
		final WindowEvent wev = new WindowEvent(EntityLocatorDialog.this, WindowEvent.WINDOW_CLOSING);
		Toolkit.getDefaultToolkit().getSystemEventQueue().postEvent(wev);
	    }
	};
    }

    private static <VT extends AbstractEntity> String generateTitle(final LoadedLocatorType locatorType, final Class<VT> entityType, final String name) {
	return TitlesDescsGetter.getEntityTitleAndDesc(entityType).getKey()
		+ locatorType + " entity locator";
    }

    /**
     * Determines the locator's loaded configuration: Default or Local.
     * 
     * @author TG Team
     *
     */
    private enum LoadedLocatorType{
	DEFAULT("Default"),
	LOCAL("");

	private final String name;

	private LoadedLocatorType(final String name){
	    this.name = name;
	}

	@Override
	public String toString() {
	    return name;
	}
    }
}
