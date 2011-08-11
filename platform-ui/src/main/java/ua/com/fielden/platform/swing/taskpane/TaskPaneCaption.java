package ua.com.fielden.platform.swing.taskpane;

import java.awt.Cursor;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.MediaTracker;
import java.awt.Toolkit;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;

import net.miginfocom.swing.MigLayout;

import org.jdesktop.swingx.JXCollapsiblePane;

public class TaskPaneCaption extends JPanel {

    private static final long serialVersionUID = -1763479717570040581L;

    private static final Image downIcon = Toolkit.getDefaultToolkit().getImage(Thread.currentThread().getContextClassLoader().getResource("images/downarrow1.png"));
    private static final Image upIcon = Toolkit.getDefaultToolkit().getImage(Thread.currentThread().getContextClassLoader().getResource("images/uparrow1.png"));

    private final ImagePanel panel = new ImagePanel();
    private final JXCollapsiblePane collapsiblePane;
    private final JLabel caption;

    private final MouseListener mouseListener;

    private final JTableHeader tableHeader;

    private String title;

    public TaskPaneCaption(final JXCollapsiblePane collapsiblePane) {
	this.title = " ";
	this.collapsiblePane = collapsiblePane;
	final MediaTracker tracker = new MediaTracker(this);
	tracker.addImage(downIcon, 1);
	tracker.addImage(upIcon, 2);
	caption = new JLabel(title);
	try {
	    tracker.waitForAll();
	} catch (final InterruptedException e) {
	    e.printStackTrace();
	}
	if (collapsiblePane.isCollapsed()) {
	    panel.setIcon(downIcon);
	} else {
	    panel.setIcon(upIcon);
	}
	mouseListener = createMouseListener();
	setLayout(new MigLayout("fill, insets 3", "[]push[]", "[c]"));
	add(caption, "growx");
	add(panel, "width 16:16:16, height 16:16:16, gapright 10");
	panel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
	caption.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
	setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
	addMouseListener(mouseListener);
	panel.addMouseListener(mouseListener);
	caption.addMouseListener(mouseListener);
	panel.setBackground(null);
	final JTable table = new JTable(new DefaultTableModel(null, new String[] { " " }));
	tableHeader = table.getTableHeader();
    }

    private MouseListener createMouseListener() {
	return new MouseAdapter() {
	    @Override
	    public void mouseClicked(final MouseEvent e) {
		if (collapsiblePane.isCollapsed()) {
		    panel.setIcon(upIcon);
		    panel.setToolTipText("Hides the " + title);
		} else {
		    panel.setIcon(downIcon);
		    panel.setToolTipText("Shows the " + title);
		}
		collapsiblePane.setCollapsed(!collapsiblePane.isCollapsed());
	    }

	    @Override
	    public void mouseEntered(final MouseEvent e) {
		// caption.setForeground(Color.LIGHT_GRAY);
	    }

	    @Override
	    public void mouseExited(final MouseEvent e) {
		// caption.setForeground(Color.BLACK);
	    }
	};
    }

    public String getTitle() {
	return title;
    }

    public void setTitle(final String title) {
	this.title = title;
	caption.setText(title);
    }

    @Override
    protected void paintComponent(final Graphics g) {
	super.paintComponent(g);
	tableHeader.setSize(this.getSize());
	tableHeader.getColumnModel().getColumn(0).setWidth(this.getWidth());
	tableHeader.getUI().paint(g, tableHeader);

    }

}
