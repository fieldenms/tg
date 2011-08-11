package ua.com.fileden.platform.example.swing.treetable;

import org.jdesktop.swingx.treetable.AbstractMutableTreeTableNode;

public class ExampleTreeTableNode extends AbstractMutableTreeTableNode {

    private final String name;

    public ExampleTreeTableNode(final String name) {
	super(name);
	this.name = name;
    }

    @Override
    public int getColumnCount() {
	return 3;
    }

    @Override
    public Object getValueAt(final int arg0) {
	return name + " column " + arg0;
    }

}
