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
 * Allocates expression level to each visited AST node. Some nodes such as literal representing nodes are level agnostic and thus have value of level equal <code>null</code>.
 * Here're the rules:
 * <ul>
 * <li>If node represents tokens INT, DECIMAL, STRING, DATE_CONST then this is a level agnostic node -- value <code>null</code> should be assigned.
 * <li>If node represents token NAME (i.e. property name in dot notation) then its level is the number of collectional properties in the value plus 1.
 * For example, if WorkOrder is the context then properties <code>totalCost</code>, <code>vehicle.techDetauls.doorsQty</code> have the level value of 1;
 * at the same time properties <code>vehicle.fuelUsages.qtyPurchased</code> have the level value of 2 due to one collectional property <code>fuelUsages</code> in the path.
 * <li>If node represents PLUS, MINUS, MULT, DIV, DAY, MONTH, YEAR, UPPER, LOWER, DAY_DIFF then its level is the same as for its operands;
 * if the operands have different levels then an exception is thrown;
 * it is possible to have a mix of level agnostic operand and an operand with some level assigned; this way the level of the node should be assigned to the level of the operand with the level value defined;
 * if both operands are level agnostic then the node becomes also level agnostic.
 * <li> If node represents aggregation functions such as AVG, SUM, MIN, MAX or COUNT then its level value is the level of its operand - 1 or <code>null</code> if the operand is level agnostic.
 * </ul>
 * <p>
 * A special care it taken for proper handling of parent annotated paths that contain symbol "←" to indicate the level of the property relative to the level of context property.
 * For example, if there is an entity <code>WorkOrder</code>, which properties <code>cost: Money</code> and <code>veh: Vehicle</code>,
 * and there is a need to use property <code>cost</code> as part of a calculated property in the context of property <code>veh</code>, then it needs to be prefixed with <code>←.</code>.
 * In this case, <code>WorkOrder</code> is considered as a higher-order type, and its property <code>veh</code> -- a context property.
 *
 * @author TG Team
 *
 */
public class LevelAllocatingVisitor extends AbstractAstVisitor {

    private final Integer contextLevel;

    /**
     * {@inheritDoc}
     */
    public LevelAllocatingVisitor(final Class<? extends AbstractEntity> higherOrderType, final String contextProperty) {
	super(higherOrderType, contextProperty);
	contextLevel = StringUtils.isEmpty(getContextProperty()) ? 1 : determineContextLevel(getContextProperty());
    }

    /**
     * This constructor can be used as a mere convenience in cases context and the higher-order type match.
     *
     * @param higherOrderType
     */
    public LevelAllocatingVisitor(final Class<? extends AbstractEntity> higherOrderType) {
	this(higherOrderType,  null);
    }

    @Override
    public void postVisit(final AstNode rootNode) throws SemanticException {
        if (rootNode.getLevel() != null && contextLevel < rootNode.getLevel()) {
            throw new IncompatibleOperandException("Resultant expression level is incompatible with the context.", rootNode.getToken());
        }
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
	    node.setLevel(null);
	    break;
	// collectional property dependent level
	case SELF:
	    node.setLevel(getBaseLevel());
	    break;
	case NAME:
	    node.setLevel(determineLevelForProperty(node.getToken().text));
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
	    node.setLevel(determineLevelBasedOnOperands(node));
	    break;
	// operand dependent and decreasing level operations
	case AVG:
	case SUM:
	case MIN:
	case MAX:
	case COUNT:
	    node.setLevel(determineLevelForAggregationOperations(node));
	    break;
	default:
	    throw new TypeCompatibilityException("Unexpected token " + node.getToken() + " in AST node.", node.getToken());
	}
    }

    /**
     * Iteratively analyses property name in dot notation to determine, which ones are collections in order to calculate property level.
     *
     * @param text
     * @return
     */
    public final Integer determineLevelForProperty(final String text) {
	return determineLevel(relative2AbsoluteInverted(text).split(Reflector.DOT_SPLITTER), getContextPropertyType());
    }

    private final Integer determineContextLevel(final String text) {
	return determineLevel(text.split(Reflector.DOT_SPLITTER), getHigherOrderType());
    }

    private int determineLevel(final String[] parts, final Class<?> type) {
	int level = getBaseLevel();
	String property = "";
	for (int index = 0; index < parts.length; index++) {
	    property += parts[index];
	    final Field field = Finder.findFieldByName(type, property);
	    if (Collection.class.isAssignableFrom(field.getType())) {
		level++;
	    }
	    property += ".";
	}
	return level;
    }

    /**
     * Returns the base level for the whole expression. This is the level with respect to which all other levels are calcualted.
     *
     * @return
     */
    private int getBaseLevel() {
	return getContextLevel() == null ? 1 : getContextLevel();
    }

    /**
     * Iterates over node's children in order to find the first one with level value assigned.
     * Compares the found value with levels of the rest of children to ensure that they're equal or level agnostic.
     *
     * @param node
     * @return -- the level to be assigned to the passed in node.
     * @throws SemanticException -- indicates level incompatibility between node's children.
     */
    public final Integer determineLevelBasedOnOperands(final AstNode node) throws SemanticException {
	Integer level = null;
	for (final AstNode child: node.getChildren()) {
	    // this statement handles the very first assignment of the level variable based on the first non-null level value of this operation's operands
	    level = child.getLevel() != null && level == null ? child.getLevel() : level; // i.e. level is assigned only if it was not assigned before

	    if (child.getLevel() != null && level != null && level != child.getLevel()) {
		//if (contextLevel < level || contextLevel < child.getLevel()) {
		    throw new IncompatibleOperandException("Incompatible operand nesting level for operands of operation '" + node.getToken().text+ "'.", node.getToken());
		//}
	    }
	}
	return level;
    }

    /**
     * Determines node's level based on its children by reusing method {@link #determineLevelBasedOnOperands(AstNode)} and decreases the calculated value by 1 if it's not null.
     *
     * @param node
     * @return
     * @throws SemanticException
     */
    public final Integer determineLevelForAggregationOperations(final AstNode node) throws SemanticException {
	final Integer level = determineLevelBasedOnOperands(node);
	return level != null ? level - 1 : null;
    }

    public Integer getContextLevel() {
        return contextLevel;
    }

}
