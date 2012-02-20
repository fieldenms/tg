package ua.com.fielden.platform.expression.ast.visitor;

import java.lang.reflect.Field;
import java.util.Collection;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.expression.EgTokenCategory;
import ua.com.fielden.platform.expression.ast.AbstractAstVisitor;
import ua.com.fielden.platform.expression.ast.AstNode;
import ua.com.fielden.platform.expression.exception.semantic.IncompatibleOperandException;
import ua.com.fielden.platform.expression.exception.semantic.SemanticException;
import ua.com.fielden.platform.expression.exception.semantic.TypeCompatibilityException;
import ua.com.fielden.platform.reflection.Finder;

/**
 * Assigns AST node tag to indicate the owning context of the node.
 * Throws {@link IncompatibleOperandException} exception in case of operation operands with different owning contexts.
 *
 * @author TG Team
 *
 */
public class TaggingVisitor extends AbstractAstVisitor {

    public static final String ABOVE = "above";
    private static final String EMPTY = "";
    private static final String PROPERTY_SEPARATOR = ".";
    public static final String THIS = "this";
    public static final String THIS_COLLECTIONAL = "this-collectional";
    private final Class<? extends AbstractEntity> context;

    /**
     * Context is a top level type for which all referenced by AST nodes properties should be its properties or subproperties.
     *
     * @param context
     */
    public TaggingVisitor(final Class<? extends AbstractEntity> context) {
	this.context = context;
    }

    @Override
    public final void visit(final AstNode node) throws SemanticException {
	final EgTokenCategory cat = EgTokenCategory.byIndex(node.getToken().category.getIndex());
	switch (cat) {
	// level agnostic tokens
	case INT:
	case DECIMAL:
	case STRING:
	case DATE_CONST:
	    node.setTag(null);
	    break;
	// collectional property dependent level
	case NAME:
	    node.setTag(determineTagForProperty(node.getToken().text));
	    break;
	// operand dependent level operations
	case PLUS:
	case MINUS:
	case MULT:
	case DIV:
	case DAY:
	case MONTH:
	case YEAR:
	case UPPER:
	case LOWER:
	case DAY_DIFF:
	    node.setTag(determineTagBasedOnOperands(node));
	    break;
	// operand dependent and decreasing level operations
	case AVG:
	case SUM:
	case MIN:
	case MAX:
	case COUNT:
	    node.setTag(determineTagForAggregationOperations(node));
	    break;
	default:
	    throw new TypeCompatibilityException("Unexpected token " + node.getToken() + " in AST node.", node.getToken());
	}
    }

    /**
     * Iteratively analyses property name in dot notation to determine, which ones are collections in order to calculate property owning context.
     *
     * @param text
     * @return
     */
    public final String determineTagForProperty(final String text) {
	String property = text;
	while (property.contains(PROPERTY_SEPARATOR)) {
	    final int index = property.lastIndexOf(PROPERTY_SEPARATOR);
	    property = index > 0 ? property.substring(0, index) : property;
	    final Field field = Finder.findFieldByName(context, property);
	    if (Collection.class.isAssignableFrom(field.getType())) {
		return property;
		//index = property.lastIndexOf(PROPERTY_SEPARATOR);
		//property = index > 0 ? property.substring(0, index) : property;
		//return EMPTY.equals(property) ? THIS : property;
	    }
	}
	return THIS;
    }

    /**
     * Iterates over node's children in order to find the first one with tag assigned.
     * Compares the found value with tags of the rest of children to ensure that they're equal or tag agnostic.
     *
     * @param node
     * @return -- the tag to be assigned to the passed in node.
     * @throws SemanticException -- indicates tag incompatibility between node's children.
     */
    public final String determineTagBasedOnOperands(final AstNode node) throws SemanticException {
	String tag = null;
	for (final AstNode child: node.getChildren()) {
	    if (child.getTag() != null && tag == null) {
		tag = child.getTag();
	    }

	    if (child.getTag() != null && tag != null && !tag.equals(child.getTag())) {
		throw new IncompatibleOperandException("Incompatible operand context for operation '" + node.getToken().text + "'", child.getToken());
	    }
	}
	return tag;
    }

    /**
     * Determines node's tag based on its children by reusing method {@link #determineTagBasedOnOperands(AstNode)}.
     *
     * @param node
     * @return
     * @throws SemanticException
     */
    public final String determineTagForAggregationOperations(final AstNode node) throws SemanticException {
	final String tag = determineTagBasedOnOperands(node);
	if (tag == null){
	    return null;
	} else if (THIS.equals(tag)) {
	    return ABOVE;
	} else {
	    return determineTagForProperty(tag);
	}
    }

}
