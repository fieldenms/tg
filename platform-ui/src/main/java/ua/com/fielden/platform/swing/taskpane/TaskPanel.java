package ua.com.fielden.platform.swing.taskpane;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.LayoutManager;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.border.Border;

import net.miginfocom.swing.MigLayout;

import org.jdesktop.swingx.JXCollapsiblePane;

public class TaskPanel extends JPanel {

    private static final long serialVersionUID = -6424927990711053422L;

    private final JXCollapsiblePane collapsiblePanel;
    private final TaskPaneCaption taskPaneCaption;

    private final Border taskPaneBorder;

    public TaskPanel() {
	super.setLayout(new MigLayout("fill, insets 0", "[]", "[c]0[c]"));
	collapsiblePanel = new JXCollapsiblePane();
	taskPaneCaption = new TaskPaneCaption(collapsiblePanel);
	// taskPaneCaption.setBorder(BorderFactory.createLineBorder(Color.gray));
	setBorder(BorderFactory.createLineBorder(new Color(146, 151, 161)));
	super.addImpl(taskPaneCaption, "growx, wrap", -1);
	super.addImpl(collapsiblePanel, "grow", -1);
	final JScrollPane scroll = new JScrollPane();
	taskPaneBorder = scroll.getBorder();
	//	collapsiblePanel.addPropertyChangeListener("collapsed", new PropertyChangeListener() {
	//
	//	    @Override
	//	    public void propertyChange(final PropertyChangeEvent evt) {
	//		if (getParent() != null) {
	//		    getParent().invalidate();
	//		    getParent().validate();
	//		    getParent().repaint();
	//		}
	//	    }
	//	});
    }

    public TaskPanel(final LayoutManager manager) {
	this();
	setLayout(manager);
    }

    @Override
    protected void addImpl(final Component comp, final Object constraints, final int index) {
	getCollapsiblePanel().add(comp, constraints, index);
	revalidate();
    }

    @Override
    public void setLayout(final LayoutManager mgr) {
	if (getCollapsiblePanel() != null) {
	    getCollapsiblePanel().setLayout(mgr);
	}
    }

    @Override
    public void remove(final Component comp) {
	getCollapsiblePanel().remove(comp);
    }

    @Override
    public void remove(final int index) {
	getCollapsiblePanel().remove(index);
    }

    @Override
    public void removeAll() {
	getCollapsiblePanel().removeAll();
    }

    public JXCollapsiblePane getCollapsiblePanel() {
	return collapsiblePanel;
    }

    public void setTitle(final String title) {
	taskPaneCaption.setTitle(title);
    }

    public String getTitle() {
	return taskPaneCaption.getTitle();
    }

    public boolean isExpanded() {
	return !getCollapsiblePanel().isCollapsed();
    }

    @Override
    protected void paintComponent(final Graphics g) {
	taskPaneBorder.paintBorder(new JScrollPane(), g, 0, 0, getWidth(), getHeight());
	super.paintComponent(g);
    }

    public void setAnimated(final boolean animated) {
	collapsiblePanel.setAnimated(animated);
    }

    public boolean isAnimated() {
	return collapsiblePanel.isAnimated();
    }

}
