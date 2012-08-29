package ua.com.fielden.platform.expression.ast.visitor;

import java.lang.reflect.Field;
import java.util.Collection;

import org.apache.commons.lang.StringUtils;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.expression.EgTokenCategory;
import ua.com.fielden.platform.expression.ast.AbstractAstVisitor;
import ua.com.fielden.platform.expression.ast.AstNode;
import ua.com.fielden.platform.expression.exception.semantic.IncompatibleOperandException;
import ua.com.fielden.platform.expression.exception.semantic.SemanticException;
import ua.com.fielden.platform.expression.exception.semantic.TypeCompatibilityException;
import ua.com.fielden.platform.reflection.Finder;
import ua.com.fielden.platform.reflection.Reflector;

/**
 * Identifies and assigns a collectional context for every AST node.
 * Collectional context indicates where in respect to collectional associations a specific AST node resides.
 * This is needed to prevent incorrect situations where the same expression may contain portions that belong to different collectional contexts.
 * Such cases are prohibited from the relational theory perspective. *
 * <p>
 * Each AST node is tagged with appropriate tag.
 * There three distinct possibilities:
 * <ul>
 * <li> THIS -- the tag that points to the fact that the node resolves to the context of the high-order type.
 * <li> ABOVE -- the tag points to the fact that the node resolves to the context above the high-order type (e.g. aggregation on property with tag THIS would result in tag ABOVE).
 * <li> [collection path] -- a tag in the form of a dot notated property, where properties in that path indicate the path to the last collectional property that defines a collectional context for the node.
 * </ul>
 *
 * Throws {@link IncompatibleOperandException} exception in case of operation operands with different owning contexts.
 *
 * @author TG Team
 *
 */
public class CollectionalContextVisitor extends AbstractAstVisitor {

    private static final String PROPERTY_SEPARATOR = ".";

    public static final String SUPER = "super";
    public static final String THIS = "this";

    private final String contextTag;


    /**
     * {@inheritDoc}
     */
    public CollectionalContextVisitor(final Class<? extends AbstractEntity> highOrderType, final String contextProperty) {
	super(highOrderType, contextProperty);
	contextTag = determineTagForContext(getContextProperty());
    }

    /**
     * Convenient constructor where context matches the high-order type.
     *
     * @param highOrderType
     */
    public CollectionalContextVisitor(final Class<? extends AbstractEntity> highOrderType) {
	this(highOrderType, null);
    }

    @Override
    public void postVisit(final AstNode rootNode) throws SemanticException {

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
	case NOW:
	case DATE:
	    node.setTag(null);
	    break;
	case SELF:
	    node.setTag(THIS);
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
	case LT:
	case GT:
	case LE:
	case GE:
	case EQ:
	case NE:
	case AND:
	case OR:
	case CASE:
	case WHEN:
	case DAY_DIFF:
	case DAYS:
	case MONTHS:
	case YEARS:
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
	return determineTag(relative2Absolute(text));
    }

    private String determineTagForContext(final String context) {
	if (StringUtils.isEmpty(context)) {
	    return THIS;
	}
	// first validate the cases where the passed in property is not in dot notation, but is itself collectional
	final Field field = Finder.findFieldByName(getHigherOrderType(), context);
	if (Collection.class.isAssignableFrom(field.getType())) {
	    return context;
	}
	// if the property is in dot notation then need to analyse the full path bit by bit
	return determineTag(context);
    }

    private final String determineTag(final String text) {
	String property = text;
	while (property.contains(PROPERTY_SEPARATOR)) {
	    final int index = property.lastIndexOf(PROPERTY_SEPARATOR);
	    property = index > 0 ? property.substring(0, index) : property;
	    final Field field = Finder.findFieldByName(getHigherOrderType(), property);
	    if (Collection.class.isAssignableFrom(field.getType())) {
		return property;
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
	String tagToReturn = ""; // should be the longest collectional context for all the operands
	for (final AstNode child: node.getChildren()) {
	    // this statement handles the very first assignment of the tag variable based on the first non-null tag value of this operation's operands
	    tag = tag == null && child.getTag() != null ? child.getTag() : tag; // i.e. tag is assigned only if it was not assigned before
	    tagToReturn = deeperContext(tagToReturn, child.getTag());

	    // collectional context permits mixing only of
	    if (child.getTag() != null && tag != null && !tag.equals(child.getTag())) {
		// if (contextTag < level || contextTag < child.getLevel()) {
		if (conflictsWithContextCollectionalTag(child.getTag()) || conflictsWithContextCollectionalTag(tag)) {
		    throw new IncompatibleOperandException("Incompatible operand context for operation '" + node.getToken().text + "': '" + child.getTag() + "' is not compatible with '" + tag + "'.", child.getToken());
		}
	    }
	}
	return StringUtils.isEmpty(tagToReturn) ? null : tagToReturn;
    }

    /**
     * A helper function that calculates the depth of the tags representing property paths, returning the shortest tag as the result.
     * It takes into account special values THIS and SUPER.
     *
     * @param thisTag
     * @param thatTag
     * @return
     */
    private String deeperContext(final String thisTag, final String thatTag) {
	if (StringUtils.isEmpty(thatTag)) {
	    return thisTag;
	}
	if (StringUtils.isEmpty(thisTag)) {
	    return thatTag;
	}
	return depth(thisTag) < depth(thatTag) ? thatTag : thisTag;
    }

    private int depth(final String path) {
	if (StringUtils.isEmpty(path)) {
	    throw new IllegalArgumentException("The depth of the property path cannot be calculated for an empty path.");
	}

	if (THIS.equalsIgnoreCase(path)) {
	    return 1;
	} else if (SUPER.equalsIgnoreCase(path)) {
	    return 0;
	} else {
	    return Reflector.propertyDepth(path);
	}
    }

    private boolean conflictsWithContextCollectionalTag(final String tag) {
	if (THIS.equalsIgnoreCase(tag) || SUPER.equalsIgnoreCase(tag) || contextTag.startsWith(tag) || tag.startsWith(contextTag)) {
	    return false;
	}
	return true;
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
	    return SUPER;
	} else {
	    return determineTagForProperty(absolute2Relative(tag));
	}
    }

}
