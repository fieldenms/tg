package ua.com.fielden.platform.example.components.dnd;

import static ua.com.fielden.platform.swing.utils.SimpleLauncher.show;
import static ua.com.fielden.platform.swing.utils.SwingUtilitiesEx.installNimbusLnFifPossible;

import java.util.Date;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import net.miginfocom.swing.MigLayout;

import org.jdesktop.swingx.JXDatePicker;

import ua.com.fielden.platform.application.AbstractUiApplication;
import ua.com.fielden.platform.branding.SplashController;
import ua.com.fielden.platform.swing.dnd.DndPanel;

/**
 * Example of label dragging on panel.
 *
 * @author yura
 *
 */
public class ComponentsDndExample extends AbstractUiApplication {

    @Override
    protected void beforeUiExposure(final String[] args, final SplashController splashController) throws Throwable {
	super.beforeUiExposure(args, splashController);
	installNimbusLnFifPossible();
	//        setLookAndFeel(getSystemLookAndFeelClassName());
    }

    @Override
    protected void exposeUi(final String[] args, final SplashController splashController) throws Throwable {
	final DndPanel panel = new DndPanel(new MigLayout("fill", "[200][200]", "[30][30]"));

	panel.addDraggable(new JLabel("left-top"), "cell 0 0, grow");
	panel.addDraggable(new JXDatePicker(new Date()), "cell 1 0, grow");
	panel.addDraggable(new JTextField("left-bottom"), "cell 0 1, grow");
	panel.addDraggable(new JCheckBox("right-bottom"), "cell 1 1, grow");

	final JPanel topPanel = new JPanel(new MigLayout("fill"));
	topPanel.add(panel, "grow, wrap");
	topPanel.add(new JButton(panel.getChangeLayoutAction()), "growx, split 2");
	topPanel.add(new JButton(panel.getBackToNormalAction()), "push, growx");

	show("Components drag-n-drop example", topPanel);
    }

    public static void main(final String[] args) {
	new ComponentsDndExample().launch(args);
    }

}
