package ua.com.fielden.uds.designer.zui.component.expression;

/**
 * This class represents Parenthesis operator.
 * 
 * @author 01es
 * 
 */
public class ParenthesisOperatorNode extends OperatorNode {
    private static final long serialVersionUID = -3376954811238474757L;

    public ParenthesisOperatorNode() {
        super(new Parenthesis());
        setShowMenu(false);
    }

    protected ParenthesisOperatorNode(ParenthesisOperatorNode parenthesisOperatorNode) {
        super(parenthesisOperatorNode);
        setShowMenu(false);
    }

    public Object clone() {
        return new ParenthesisOperatorNode(this);
    }
}
