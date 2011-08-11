package ua.com.fielden.platform.swing.pivot.analysis.treetable;

import java.util.Comparator;

import org.jdesktop.swingx.treetable.MutableTreeTableNode;

public class DefaultSorter implements Comparator<MutableTreeTableNode> {

    @Override
    public int compare(final MutableTreeTableNode o1, final MutableTreeTableNode o2) {
	return o1.getUserObject().toString().compareTo(o2.getUserObject().toString());
    }

}
