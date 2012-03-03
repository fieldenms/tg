package ua.com.fielden.platform.swing.taskpane;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;

import javax.swing.ImageIcon;
import javax.swing.JPanel;

public class ImagePanel extends JPanel {

    private static final long serialVersionUID = -597212092905771735L;

    private Image icon;

    public ImagePanel() {
	icon = null;
    }

    public ImagePanel(final ImageIcon icon){
	setIcon(icon.getImage());
    }

    @Override
    protected void paintComponent(final Graphics g) {
	// super.paintComponent(g);
	final Graphics2D g2D = (Graphics2D) g;
	g2D.drawImage(icon, 0, 0, null);
    }

    public void setIcon(final Image icon) {
	this.icon = icon;
	revalidate();
    }

    public Image getIcon() {
	return icon;
    }

}
