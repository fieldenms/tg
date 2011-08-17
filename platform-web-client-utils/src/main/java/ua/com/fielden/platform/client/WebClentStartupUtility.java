package ua.com.fielden.platform.client;

import java.awt.Dimension;
import java.awt.Image;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Properties;

import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.ToolTipManager;

import org.apache.log4j.Logger;
import org.jfree.ui.RefineryUtilities;
import org.restlet.data.Protocol;

import ua.com.fielden.platform.application.update.ApplicationUpdateFeedback;
import ua.com.fielden.platform.branding.SplashController;
import ua.com.fielden.platform.client.config.IMainMenuBinder;
import ua.com.fielden.platform.client.ui.DefaultApplicationMainPanel;
import ua.com.fielden.platform.client.ui.menu.TreeMenuFactory;
import ua.com.fielden.platform.rao.RestClientUtil;
import ua.com.fielden.platform.security.user.IUserProvider;
import ua.com.fielden.platform.swing.components.blocking.BlockingIndefiniteProgressPane;
import ua.com.fielden.platform.swing.dialogs.DialogWithDetails;
import ua.com.fielden.platform.swing.login.StyledLoginScreen;
import ua.com.fielden.platform.swing.menu.TreeMenuItem;
import ua.com.fielden.platform.swing.menu.UndockableTreeMenuWithTabs;
import ua.com.fielden.platform.swing.menu.api.ITreeMenuFactory;
import ua.com.fielden.platform.swing.menu.filter.WordFilter;
import ua.com.fielden.platform.swing.review.EntityMasterManager;
import ua.com.fielden.platform.swing.utils.SwingUtilitiesEx;
import ua.com.fielden.platform.swing.view.BaseFrame;
import ua.com.fielden.platform.ui.config.MainMenuItem;
import ua.com.fielden.platform.update.IClientApplicationRestarter;
import ua.com.fielden.platform.update.ReferenceDependancyController;
import ua.com.fielden.platform.update.Updater;

import com.google.inject.Injector;
import com.jidesoft.plaf.LookAndFeelFactory;

/**
 * A utility class containing all the necessary methods for constructing a web client in a more convenient than manual way.
 *
 * @author TG Team
 *
 */
public class WebClentStartupUtility {


    /**
     * Constructs and displays the main application window with prior validation of user credentials.
     *
     * @param splash
     * @param loginScreen
     * @param restUtil
     * @param injector
     * @param emm -- entity master manager, which provides instantiation and caching of entity master review/edit windows.
     * @param mmBinder -- main menu binder, which is used for instantiation and configuration of the application main menu.
     * @param caption -- default application caption displayed in the title of the main window.
     * @param dim -- default main window dimension
     * @param icon -- application icon, which appears in the title of every application window.
     * @param logger
     */
    public static void showMainWindow(//
	    final SplashController splash, //
	    final StyledLoginScreen loginScreen, //
	    final RestClientUtil restUtil, //
	    final Injector injector, //
	    final EntityMasterManager emm, //
	    final IMainMenuBinder mmBinder,
	    final String caption,//
	    final Dimension dim,//
	    final Image icon,//
	    final Logger logger) {
	logger.info("Showing main window...");
	SwingUtilitiesEx.invokeLater(new Runnable() {
	    @Override
	    public void run() {
		message("Loading application modules...", splash, loginScreen);
		JFrame.setDefaultLookAndFeelDecorated(true);
		JDialog.setDefaultLookAndFeelDecorated(true);
		SwingUtilitiesEx.installNimbusLnFifPossible();

		if (restUtil.isUserConfigured()) {
		    createAndRunMainApplicationFrame(splash, loginScreen, injector, emm, mmBinder, caption, dim, icon);
		}
	    }// run
	});
    }

    /**
     * Updates UI by drawing the specified message either on the splash or a blocking pane of the login screen.
     */
    private static void message(final String msg, final SplashController splash, final StyledLoginScreen loginScreen) {
	SwingUtilitiesEx.invokeLater(new Runnable() {
	    @Override
	    public void run() {
		if (loginScreen != null) {
		    loginScreen.getBlockingPane().setText(msg);
		} else {
		    splash.drawSplashProgress(msg);
		}
	    }
	});
    }

    private static void createAndRunMainApplicationFrame(//
	    final SplashController splash,//
	    final StyledLoginScreen loginScreen,//
	    final Injector injector,//
	    final EntityMasterManager emm,//
	    final IMainMenuBinder mmBuilder,//
	    final String caption,//
	    final Dimension dim,//
	    final Image icon) {
	message("Loading user configurations...", splash, loginScreen);
	// Define all menu items for the main application frame menu
	// compose all menu items into one vector
	@SuppressWarnings("rawtypes")
	final TreeMenuItem<?> menuItems = new TreeMenuItem("root", "root panel");
	final BaseFrame mainApplicationFrame = new BaseFrame(caption, emm.getEntityMasterCache());
	@SuppressWarnings({ "rawtypes", "unchecked" })
	final UndockableTreeMenuWithTabs<?> menu = new UndockableTreeMenuWithTabs(menuItems, new WordFilter(), injector.getInstance(IUserProvider.class), new BlockingIndefiniteProgressPane(mainApplicationFrame));

	final Thread thread = new Thread(new Runnable() {
	    @Override
	    public void run() {
		try {
		    buildMainMenu(splash, loginScreen, injector, mmBuilder, menuItems, menu);

		    SwingUtilitiesEx.invokeLater(new Runnable() {
			@Override
			public void run() {
			    // create main application frame and menu menu based on the above menu items
			    message("Instantiating application main window...", splash, loginScreen);

			    mainApplicationFrame.setIconImage(icon);
			    mainApplicationFrame.setPreferredSize(dim);

			    mainApplicationFrame.add(new DefaultApplicationMainPanel(menu));
			    mainApplicationFrame.pack();

			    RefineryUtilities.centerFrameOnScreen(mainApplicationFrame);
			    mainApplicationFrame.setVisible(true);

			    if (loginScreen != null) {
				loginScreen.getBlockingPane().unlock();
				loginScreen.dispose();
			    }

			}
		    });
		} catch (final Exception ex) {
		    ex.printStackTrace();
		    new DialogWithDetails(null, "Could not launch application", ex).setVisible(true);
		    System.exit(1);
		}
	    }
	});
	thread.setDaemon(false);
	thread.start();
    }

    /**
     * Builds menu items.
     *
     * @param splash
     * @param loginScreen
     * @param injector
     * @param mmBuilder
     * @param menuItems
     * @param menu
     */
    private static void buildMainMenu(//
	    final SplashController splash,//
	    final StyledLoginScreen loginScreen,//
	    final Injector injector, //
	    final IMainMenuBinder mmBuilder,//
	    final TreeMenuItem<?> menuItems, //
	    final UndockableTreeMenuWithTabs<?> menu) {
	final ITreeMenuFactory menuFactory = new TreeMenuFactory(menuItems, menu, injector);
	mmBuilder.bindMainMenuItemFactories(menuFactory);
	final List<MainMenuItem> itemsFromCloud = mmBuilder.getMainMenuItemController().loadMenuSkeletonStructure();

	message("Building user configurations...", splash, loginScreen);

	menuFactory.build(itemsFromCloud);
	menu.getModel().getOriginModel().reload();
    }



    /**
     * Make a HTTP request to the application server in order to check its availability.
     *
     * @param splash
     * @param props
     * @param VERSION
     * @return
     */
    public static RestClientUtil checkApplicationServerAvailability(final SplashController splash, final Properties props, final String VERSION) {
	splash.drawSplashProgress("Checking application server availability...");
	final RestClientUtil restUtil = new RestClientUtil(Protocol.HTTP, props.getProperty("host"), Integer.parseInt(props.getProperty("port")), VERSION, props.getProperty("username"));
	restUtil.warmUp();
	return restUtil;
    }

    /**
     * Reads application setting from the specified as args property file. The args should have exactly one element. Otherwise, file "aplication.properties" is used. The array args
     * is used as a convenience for calling method from main without additional processing such as checking args lengs etc.
     * <p>
     * The splash instance is used for providing user feedback.
     * <p>
     * An illegal argument exception is thrown of the application property file could not be found or read.
     *
     * @param args
     * @param splash
     * @return -- application settings
     */
    public static Properties readProperties(final String[] args, final SplashController splash) {
	splash.drawSplashProgress("Reading configuration settings...");

	final Properties props = new Properties();
	final String fileName = args.length == 1 ? args[0] : "application.properties";
	InputStream st = null;
	try {
	    st = new FileInputStream(fileName);
	    props.load(st);
	    return props;
	} catch (final Exception ex) {
	    ex.printStackTrace();
	    JOptionPane.showMessageDialog(null, String.format("Application property file %s could not be located or its values are not recognised.", fileName), "Application properties", JOptionPane.ERROR_MESSAGE);
	    throw new IllegalArgumentException(ex);
	} finally {
	    try {
		st.close();
	    } catch (final Exception e) {
		e.printStackTrace(); // can be ignored
	    }
	}
    }

    /**
     * Sets up UI preferences such as LaF tool tip manager dismiss delay etc.
     */
    public static void setupUiPreferences() {
	SwingUtilitiesEx.invokeLater(new Runnable() {
	    @Override
	    public void run() {
		SwingUtilitiesEx.installNimbusLnFifPossible();
		com.jidesoft.utils.Lm.verifyLicense("Fielden Management Services", "Rollingstock Management System", "xBMpKdqs3vWTvP9gxUR4jfXKGNz9uq52");
		LookAndFeelFactory.installJideExtension();

		ToolTipManager.sharedInstance().setDismissDelay(1000 * 300);
	    }
	});
    }

    /**
     * Instantiates a new updater for the client application.
     */
    public static Updater getUpdater(final SplashController splash, final StyledLoginScreen loginScreen, final RestClientUtil restUtil, final IClientApplicationRestarter restarter)
	    throws IOException {
	return new Updater(System.getProperty("user.dir"), new ReferenceDependancyController(restUtil), new ApplicationUpdateFeedback(splash, loginScreen, restarter));
    }

}
