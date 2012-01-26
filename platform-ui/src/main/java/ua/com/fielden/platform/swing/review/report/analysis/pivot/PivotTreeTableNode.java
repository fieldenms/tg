package ua.com.fielden.platform.swing.review.report.analysis.pivot;

import org.jdesktop.swingx.treetable.AbstractMutableTreeTableNode;

abstract class PivotTreeTableNode extends AbstractMutableTreeTableNode {

    final static String NULL_USER_OBJECT = "UNKNOWN";

    public PivotTreeTableNode(final Object userObject){
	super(userObject);
    }

    public abstract String getTooltipAt(int column);
}
