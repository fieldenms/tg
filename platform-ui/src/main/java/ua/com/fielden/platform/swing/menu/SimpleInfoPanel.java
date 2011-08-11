package ua.com.fielden.platform.swing.menu;

import javax.swing.JLabel;
import javax.swing.JPanel;

import net.miginfocom.swing.MigLayout;

/**
 * This is a convenient class representing an info panel with textual information that could be used either as a default tree menu with tabs info panel, or as any tree menu item
 * info panel.
 * <p>
 * It is best of the information text passed into the constructor is HTML, which supports rich formatting capabilities and wrapping.
 * 
 * @author 01es
 * 
 */
public class SimpleInfoPanel extends JPanel {
    private static final long serialVersionUID = 1L;

    /**
     * Principle constructor. It is best if the <code>info</code> parameter is a HTML text.
     * 
     * @param info
     */
    public SimpleInfoPanel(final String info) {
	super(new MigLayout("fill", "[fill, grow, c]", "[top]"));
	add(new JLabel(info));
    }

}
