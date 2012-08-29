package ua.com.fielden.platform.expression.ast;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import ua.com.fielden.platform.entity.query.model.ConditionModel;
import ua.com.fielden.platform.entity.query.model.ExpressionModel;
import ua.com.fielden.platform.expression.Token;
import ua.com.fielden.platform.expression.exception.MisplacedTokenException;
import ua.com.fielden.platform.expression.exception.RecognitionException;

/**
 * A base class for the expression grammar AST node.
 *
 * @author TG Team
 *
 */
public class AstNode {
    private final Token token;
    private final List<AstNode> children = new ArrayList<AstNode>();
    private Class<?> type;
    private Object value;
    private Integer level;
    private String tag;
    private ExpressionModel model;
    private ConditionModel conditionModel;

    public AstNode(final Token token) {
	if (token == null) {
	    throw new IllegalArgumentException("Token value is required");
	}
	this.token = token;
    }

    public Token getToken() {
	return token;
    }

    public AstNode addChild(final AstNode node) throws RecognitionException {
	final StringBuilder reason = new StringBuilder();
	if (!node.canBeAddedTo(this, reason)) {
	    throw new MisplacedTokenException(reason.toString(), node.token);
	}
	children.add(node);
	return this;
    }

    /**
     * This method is introduced specifically to facilitate cases where an AST node being added to the parent knows that it whould not be added there.
     * <p>
     * For example, an AST node associated with token SELF can only be added to a node for token COUNT.
     *
     * @param intendedParentNode
     * @param reason
     * @return
     */
    protected boolean canBeAddedTo(final AstNode intendedParentNode, final StringBuilder reason) {
	return true;
    }

    @Override
    public String toString() {
	return token != null ? token.text : "nil";
    }

    public String treeToString() {
	if (children.size() == 0) {
	    return this.toString();
	}
	final StringBuilder buf = new StringBuilder();
	if (children.size() > 0) {
	    buf.append("(");
	    buf.append(this.toString());
	    buf.append(' ');
	}
	for (int index = 0; index < children.size(); index++) {
	    final AstNode t = children.get(index);
	    if (index > 0) {
		buf.append(' ');
	    }
	    buf.append(t.treeToString());
	}
	if (children.size() > 0) {
	    buf.append(")");
	}
	return buf.toString();

    }

    /**
     * Returns the type as computed by the semantic analysis during AST tree walking.
     *
     * @return
     */
    public Class<?> getType() {
	return type;
    }

    public void setType(final Class<?> type) {
	this.type = type;
    }

    /**
     * Should return an actual <code>value</code> of the <code>type</code> represented by this node instance.
     * May return <code>null</code> if value was not yet determined or not applicable.
     *
     * @return
     */
    public Object getValue() {
	return value;
    }

    public void setValue(final Object value) {
	this.value = value;
    }

    public List<AstNode> getChildren() {
	return Collections.unmodifiableList(children);
    }

    public Integer getLevel() {
	return level;
    }

    public void setLevel(final Integer level) {
	this.level = level;
    }

    public String getTag() {
	return tag;
    }

    public void setTag(final String tag) {
	this.tag = tag;
    }

    public boolean hasValue() {
	return value != null;
    }

    public ExpressionModel getModel() {
	return model;
    }

    public AstNode setModel(final ExpressionModel model) {
	this.model = model;
	return this;
    }

    public ConditionModel getConditionModel() {
	return conditionModel;
    }

    public void setConditionModel(final ConditionModel conditionModel) {
	this.conditionModel = conditionModel;
    }
}
