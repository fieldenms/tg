package ua.com.fielden.uds.designer.zui.component.expression;

import java.awt.geom.GeneralPath;
import java.awt.geom.Rectangle2D;

/**
 * This class is needed only for a proper parenthesis representation of the abs operator.
 * 
 * @author 01es
 * 
 */
public class AbsOperatorNode extends OperatorNode {
    private static final long serialVersionUID = -3376954811238474757L;

    public AbsOperatorNode() {
	super(new Abs());
    }

    public AbsOperatorNode(AbsOperatorNode node) {
	super(node);
    }

    protected GeneralPath rightParenthesis(Rectangle2D newBounds) {
	GeneralPath rightParentheses = new GeneralPath(GeneralPath.WIND_EVEN_ODD);
	rightParentheses.moveTo((float) (newBounds.getWidth()), (float) (newBounds.getHeight()));
	rightParentheses.lineTo((float) (newBounds.getWidth()), (float) newBounds.getY());

	return rightParentheses;
    }

    protected GeneralPath leftParenthesis(Rectangle2D newBounds) {
	GeneralPath leftParentheses = new GeneralPath(GeneralPath.WIND_EVEN_ODD);
	leftParentheses.moveTo((float) newBounds.getX(), (float) newBounds.getY());
	leftParentheses.lineTo((float) newBounds.getX(), (float) (newBounds.getHeight()));

	return leftParentheses;
    }

    public Object clone() {
	return new AbsOperatorNode(this);
    }

}
