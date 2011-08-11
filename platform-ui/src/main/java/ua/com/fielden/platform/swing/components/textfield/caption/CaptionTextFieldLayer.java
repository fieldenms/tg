package ua.com.fielden.platform.swing.components.textfield.caption;

import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.geom.Rectangle2D;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JTextField;

import net.miginfocom.swing.MigLayout;

import org.apache.commons.lang.StringUtils;
import org.jdesktop.jxlayer.JXLayer;
import org.jdesktop.jxlayer.plaf.AbstractLayerUI;

import ua.com.fielden.platform.swing.utils.SimpleLauncher;
import ua.com.fielden.platform.swing.utils.Utils2D;

/**
 * This is a layer, which provides caption for a JTextField instance.
 * 
 * @author 01es
 * 
 */
public class CaptionTextFieldLayer<T> extends JXLayer<JTextField> {
    private static final long serialVersionUID = 1L;

    public CaptionTextFieldLayer(final JTextField textComponent, final String caption) {
	super(textComponent);
	setUI(new CaptionUi(textComponent, caption));
	textComponent.addFocusListener(new FocusListener() {
	    @Override
	    public void focusGained(final FocusEvent e) {
		repaint();
	    }

	    @Override
	    public void focusLost(final FocusEvent e) {
		repaint();
	    }
	});
    }

    /**
     * Provides painting logic.
     * 
     * @author 01es
     * 
     */
    private static class CaptionUi extends AbstractLayerUI<JTextField> {
	private final JTextField component;
	private final String caption;

	public CaptionUi(final JTextField component, final String caption) {
	    this.caption = caption;
	    this.component = component;
	}

	@Override
	protected void paintLayer(final Graphics2D g2, final JXLayer<JTextField> l) {
	    super.paintLayer(g2, l); // this paints layer as is
	    // paint the caption is appropriate
	    if (StringUtils.isEmpty(component.getText()) && !StringUtils.isEmpty(caption) && !component.hasFocus()) {
		g2.setColor(new Color(0f, 0f, 0f, 0.6f));
		g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB);
		final double xPos = component.getInsets().left;

		// define how many characters in the caption can be drawn
		final int w = component.getSize().width - (component.getInsets().left + component.getInsets().right);
		final String textToDisplay = Utils2D.abbreviate(g2, caption, w);
		final FontMetrics fm = g2.getFontMetrics();
		final Rectangle2D textSize = fm.getStringBounds(textToDisplay, g2);
		final double yPos = (component.getSize().height - textSize.getHeight()) / 2. + fm.getAscent();
		g2.drawString(textToDisplay, (float) xPos, (float) yPos);
	    }
	}
    }

    public static void main(final String[] args) {
	final JPanel panel = new JPanel(new MigLayout("fill", "[:250:]"));
	panel.add(new CaptionTextFieldLayer<JTextField>(new JTextField(), "some caption"), "growx, wrap");
	panel.add(new JButton("Dummy"), "align right");
	SimpleLauncher.show("Show off the caption", panel);
    }
}
