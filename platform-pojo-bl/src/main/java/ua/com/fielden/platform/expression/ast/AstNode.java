package ua.com.fielden.platform.expression.ast;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import ua.com.fielden.platform.entity.query.model.ExpressionModel;
import ua.com.fielden.platform.expression.Token;

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

    public AstNode(final Token token) {
	if (token == null) {
	    throw new IllegalArgumentException("Token value is required");
	}
	this.token = token;
    }

    public Token getToken() {
	return token;
    }

    public AstNode addChild(final AstNode node) {
	children.add(node);
	return this;
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

    public void setModel(final ExpressionModel model) {
        this.model = model;
    }
}
