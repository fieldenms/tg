package ua.com.fielden.platform.swing.checkboxlist;

import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;

import javax.swing.BorderFactory;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JToggleButton;

public class CheckboxListCellRenderer<T> extends JPanel implements CheckingListCellRenderer<T> {

    private static final long serialVersionUID = 7668995176743184529L;

    protected final JToggleButton toggleButton;
    protected final DefaultListCellRenderer defaultRenderer = new DefaultListCellRenderer();

    public CheckboxListCellRenderer(final JToggleButton toggleButton) {
	super(new FlowLayout(FlowLayout.LEFT, 0, 0));
	this.toggleButton = toggleButton;
	toggleButton.setText("");
	add(toggleButton);
	add(defaultRenderer);
    }

    @SuppressWarnings("unchecked")
    @Override
    public Component getListCellRendererComponent(final JList list, final Object value, final int index, final boolean isSelected, final boolean cellHasFocus) {
	if (list instanceof CheckboxList) {
	    final CheckboxList<T> checkboxList = (CheckboxList<T>) list;
	    defaultRenderer.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
	    setBackground(new Color(defaultRenderer.getBackground().getRGB()));
	    setBorder(defaultRenderer.getBorder());
	    defaultRenderer.setOpaque(false);
	    defaultRenderer.setBorder(BorderFactory.createEmptyBorder());
	    if (checkboxList.isValueChecked((T) value)) {
		toggleButton.setSelected(true);
	    } else {
		toggleButton.setSelected(false);
	    }
	    return this;
	}
	return defaultRenderer.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
    }

    @Override
    public boolean isOnHotSpot(final int x, final int y) {
	return toggleButton.getBounds().contains(x, y);
    }

}
