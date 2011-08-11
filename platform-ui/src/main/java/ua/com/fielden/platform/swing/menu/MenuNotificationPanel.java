package ua.com.fielden.platform.swing.menu;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;
import javax.swing.border.EmptyBorder;

import net.miginfocom.swing.MigLayout;
import ua.com.fielden.platform.swing.components.NotificationLayer;
import ua.com.fielden.platform.swing.components.NotificationLayer.MessageType;
import ua.com.fielden.platform.swing.utils.SimpleLauncher;

import com.jidesoft.swing.StyledLabelBuilder;

/**
 * This is a panel with bevel border and a label at the left of the panel containing the specified text, which supports message notification mechanism based on
 * {@link NotificationLayer}.
 * 
 * @author TG Team
 */
public class MenuNotificationPanel extends JPanel {
    private static final long serialVersionUID = 1L;

    private final String caption;
    private final NotificationLayer<JPanel> notifLayer;

    public MenuNotificationPanel(final String caption) {
	super(new MigLayout("fill, insets 0", "[fill, :200:]", "[]-1[top]"));
	this.caption = caption;
	final JPanel panel = new JPanel(new MigLayout("fill, insets 0 5 0 5", "[]", "[grow,fill,c,30:30:30]"));
	panel.setBorder(new EmptyBorder(0, 0, 0, 0));
	final JLabel headerLable = new StyledLabelBuilder().add(caption, "bold,f:darkgray").createLabel();
	panel.add(headerLable);
	add(notifLayer = new NotificationLayer<JPanel>(panel), "wrap");
	add(new JSeparator());
    }

    public void setMessage(final String msg, final MessageType msgType) {
	notifLayer.setMessage(msg, msgType);
    }

    public static void main(final String[] args) {
	for (final LookAndFeelInfo laf : UIManager.getInstalledLookAndFeels()) {
	    if ("Nimbus".equals(laf.getName())) {
		try {
		    UIManager.setLookAndFeel(laf.getClassName());
		} catch (final Exception e) {
		    e.printStackTrace();
		}
	    }
	}

	final MenuNotificationPanel panel = new MenuNotificationPanel("Work Order Master");

	final JButton buttonYellow = new JButton(new AbstractAction("Yellow") {
	    private static final long serialVersionUID = 1L;

	    @Override
	    public void actionPerformed(final ActionEvent e) {
		panel.setMessage("Yellow message sssssssssssssssssssssssssssssssssssssssss", MessageType.WARNING);
	    }
	});
	final JButton buttonRed = new JButton(new AbstractAction("Red") {
	    private static final long serialVersionUID = 1L;

	    @Override
	    public void actionPerformed(final ActionEvent e) {
		panel.setMessage("Red message sssssssssssssssssssssssssssssssssssssssss", MessageType.ERROR);
	    }
	});
	final JButton buttonGreen = new JButton(new AbstractAction("Green") {
	    private static final long serialVersionUID = 1L;

	    @Override
	    public void actionPerformed(final ActionEvent e) {
		panel.setMessage("Green message sssssssssssssssssssssssssssssssssssssssss", MessageType.INFO);
	    }
	});

	final JButton buttonNone = new JButton(new AbstractAction("None") {
	    private static final long serialVersionUID = 1L;

	    @Override
	    public void actionPerformed(final ActionEvent e) {
		panel.setMessage("", MessageType.NONE);
	    }
	});

	final JPanel actionPanel = new JPanel(new MigLayout("", "[fill, :100:][fill, :100:]"));
	actionPanel.add(buttonYellow);
	actionPanel.add(buttonRed);
	actionPanel.add(buttonGreen);
	actionPanel.add(buttonNone);
	final JPanel holder = new JPanel(new MigLayout("fill", "[grow,fill,:600:]", "[grow,fill][]"));
	holder.add(panel, "wrap");
	holder.add(actionPanel);
	SimpleLauncher.show("Show off the fading notification layer", holder);
    }

    public String getCaption() {
	return caption;
    }

}
