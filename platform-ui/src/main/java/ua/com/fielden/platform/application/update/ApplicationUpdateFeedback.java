package ua.com.fielden.platform.application.update;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.util.Arrays;
import java.util.Map;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.border.EmptyBorder;

import net.miginfocom.swing.MigLayout;

import org.jfree.ui.RefineryUtilities;

import ua.com.fielden.platform.branding.SplashController;
import ua.com.fielden.platform.swing.dialogs.DialogWithDetails;
import ua.com.fielden.platform.swing.login.StyledLoginScreen;
import ua.com.fielden.platform.swing.progress.DualProgressPane;
import ua.com.fielden.platform.swing.utils.SwingUtilitiesEx;
import ua.com.fielden.platform.swing.view.BaseFrame;
import ua.com.fielden.platform.update.DependencyAction;
import ua.com.fielden.platform.update.DependencyActionResult;
import ua.com.fielden.platform.update.IClientApplicationRestarter;
import ua.com.fielden.platform.update.IUpdateActionFeedback;
import ua.com.fielden.platform.utils.ResourceLoader;

/**
 * This is a convenient client application update feedback facility, which outputs the relevant feedback information either onto the splash screen and/or a specially designed frame with two progress bars to indicate the overall and individual jar file download progress.
 *
 * @author TG Team
 *
 */
public class ApplicationUpdateFeedback implements IUpdateActionFeedback {

    private final SplashController splash;
    private StyledLoginScreen loginScreen;

    private DualProgressPane pane;

    private final IClientApplicationRestarter restarter;

    private boolean updateCompleted = false;

    private BaseFrame progressFrame;
    private final Action exitAction = new AbstractAction("Abort") {

	@Override
	public void actionPerformed(final ActionEvent e) {
	    if (updateCompleted) {
		System.exit(0);
	    } else {
		if (JOptionPane.showConfirmDialog(progressFrame, "The update is in progress.\n Would you like to abort it?", "Update warning", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE) == JOptionPane.YES_OPTION) {
		    System.exit(0);
		}
	    }
	}
    };
    {
	exitAction.setEnabled(true);
	exitAction.putValue(Action.MNEMONIC_KEY, KeyEvent.VK_A);
    }

    public ApplicationUpdateFeedback(final SplashController splash, final StyledLoginScreen loginScreen, final IClientApplicationRestarter restarter) {
	this.splash = splash;
	this.loginScreen = loginScreen;
	this.restarter = restarter;
    }

    @Override
    public void checkDependency() {
	message("Checking for update...");
    }

    @Override
    public void dependencyActions(final Map<String, DependencyAction> map) {
	if (map.size() == 0) {
	    message("Application is up to date");
	} else {
	    message("Updating " + map.size() + " dependencies");

	    try {
		SwingUtilitiesEx.invokeAndWaitIfPossible(new Runnable() {
		    @Override
		    public void run() {
			progressFrame = new BaseFrame("Application update");

			for (final WindowListener listener : Arrays.asList(progressFrame.getWindowListeners())) {
			    progressFrame.removeWindowListener(listener);
			}

			progressFrame.addWindowListener(new WindowAdapter() {

			    @Override
			    public void windowClosing(final WindowEvent e) {
				exitAction.actionPerformed(null);
			    }

			});

			progressFrame.setIconImage(ResourceLoader.getImage("images/tg-icon.png"));
			progressFrame.setLayout(new MigLayout("fill", "[fill, grow, 200:400:]", "[fill, grow, 50:80:][]"));
			final JScrollPane scroll = new JScrollPane(pane = new DualProgressPane(map.size()));
			scroll.setBorder(new EmptyBorder(0, 0, 0, 0));
			progressFrame.add(scroll, "wrap");

			final JPanel buttonPanel = new JPanel(new MigLayout("fill, insets 0", "[fill, grow][fill, :80:]", "[c]"));
			final JButton closeButton = new JButton(exitAction);
			progressFrame.getRootPane().setDefaultButton(closeButton);
			buttonPanel.add(closeButton, "skip 1");
			progressFrame.add(buttonPanel);
			progressFrame.pack();
			RefineryUtilities.centerFrameOnScreen(progressFrame);

			progressFrame.setVisible(true);

			if (loginScreen != null) {
			    loginScreen.setVisible(false);
			    loginScreen.dispose();
			    loginScreen = null;
			}
		    }
		});
	    } catch (final Exception e) {
		e.printStackTrace();
		message("Could not start the update");
	    }
	}
    }

    @Override
    public void backuping() {
	message("Backuping current dependencies...");
    }

    @Override
    public void restoring() {
	message("Restoring dependencies...");
    }

    @Override
    public void start(final String dependencyFileName, final Long fileSize, final DependencyAction action) {
	try {
	    switch (action) {
	    case UPDATE:
		pane.initNewStep(fileSize.intValue(), "Downloading " + dependencyFileName);
		break;
	    case DELETE:
		pane.initNewStep(2, "Deleting " + dependencyFileName);
		pane.updateCurrStep(1);
		break;
	    default:
		message("Unknown action for " + dependencyFileName + "...");
		break;
	    }
	} catch (final Exception ex) {
	    ex.printStackTrace();
	    message(ex.getMessage());
	}
    }

    @Override
    public void update(final long totalNumOfBytesRead) {
	try {
	    pane.updateCurrStep(new Long(totalNumOfBytesRead).intValue());
	} catch (final Exception ex) {
	    ex.printStackTrace();
	    message(ex.getMessage());
	}
    }

    @Override
    public void finish(final String dependencyFileName, final DependencyAction action, final DependencyActionResult result) {
	try {
	    switch (action) {
	    case UPDATE:
		if (DependencyActionResult.FAILURE == result) {
		    message("Could not update dependency " + dependencyFileName);
		    pane.message("<html><font color=#AA0000>Could not update dependency " + dependencyFileName + "</font></html>");
		} else {
		    message("Completed update for dependency " + dependencyFileName);
		    pane.finishCurrStep("Completed update for dependency " + dependencyFileName);
		}
		break;
	    case DELETE:
		if (DependencyActionResult.FAILURE == result) {
		    message("Could not delete dependency " + dependencyFileName);
		    pane.message("<html><font color=#AA0000>Could not delete dependency " + dependencyFileName + "</font></html>");
		} else {
		    message("Completed update for dependency " + dependencyFileName);
		    pane.finishCurrStep("Completed update for dependency " + dependencyFileName);
		}
		break;
	    default:
		message("Unknown action for " + dependencyFileName + "...");
		break;
	    }
	} catch (final Exception ex) {
	    ex.printStackTrace();
	    message(ex.getMessage());
	}

    }

    @Override
    public void updateCompleted(final String msg) {
	try {
	    updateCompleted = true;
	    pane.completeAll("<html><b><font color=#00AA00>" + msg + "</font></b></html>");
	    SwingUtilitiesEx.invokeAndWaitIfPossible(new Runnable() {
		@Override
		public void run() {
		    exitAction.setEnabled(true);
		    JOptionPane.showMessageDialog(progressFrame, "Please click Ok and start the application again to complete the update.");
		    System.exit(0);
		}
	    });
	} catch (final Exception e) {
	    e.printStackTrace();
	}
	message(msg);

    }

    @Override
    public void updateFailed(final Exception ex) {
	try {
	    SwingUtilitiesEx.invokeAndWaitIfPossible(new Runnable() {
		@Override
		public void run() {
		    final DialogWithDetails dialog = new DialogWithDetails(null, "Exception during update", ex) {
			@Override
			protected Action createCloseAction() {
			    final Action action = new AbstractAction("Exit") {
				private static final long serialVersionUID = 1L;

				public void actionPerformed(final ActionEvent e) {
				    System.exit(1);
				}
			    };
			    action.putValue(Action.ACCELERATOR_KEY, KeyEvent.VK_E);
			    return action;
			}

			private Action createRestartAction() {
			    final Action action = new AbstractAction("Restart") {
				private static final long serialVersionUID = 1L;

				public void actionPerformed(final ActionEvent e) {
				    restarter.restart();
				}
			    };
			    action.putValue(Action.ACCELERATOR_KEY, KeyEvent.VK_R);
			    return action;
			}

			@Override
			protected JPanel buttonPanel() {
			    final JPanel panel = new JPanel(new MigLayout("fill", "[]push[fill, 80:80:][fill, 80:80:][fill, 80:80:]", "[c]"));

			    final JButton closeButton = new JButton(getCloseAction());
			    panel.add(closeButton, "skip 1");
			    panel.add(new JButton(createRestartAction()));
			    panel.add(new JButton(createDetailsAction(closeButton)));

			    getRootPane().setDefaultButton(closeButton);
			    return panel;
			}
		    };
		    dialog.pack();
		    dialog.setVisible(true);
		}

	    });
	} catch (final Exception e) {
	    e.printStackTrace();
	}
    }

    /**
     * Updates UI by drawing the specified message either on the splash or a blocking pane of the login screen.
     *
     * @param msg
     */
    private void message(final String msg) {
	SwingUtilitiesEx.invokeLater(new Runnable() {
	    @Override
	    public void run() {
		if (loginScreen != null) {
		    loginScreen.getBlockingPane().setText(msg);
		} else if (splash != null) {
		    splash.drawSplashProgress(msg);
		}
	    }
	});
    }
}
