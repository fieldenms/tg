package ua.com.fielden.platform.expression.ast.visitor;

import java.lang.reflect.Field;
import java.util.Collection;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.expression.EgTokenCategory;
import ua.com.fielden.platform.expression.ast.AstNode;
import ua.com.fielden.platform.expression.ast.IAstVisitor;
import ua.com.fielden.platform.expression.exception.semantic.IncompatibleOperandException;
import ua.com.fielden.platform.expression.exception.semantic.SemanticException;
import ua.com.fielden.platform.expression.exception.semantic.TypeCompatibilityException;
import ua.com.fielden.platform.reflection.Finder;

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
public class LevelAllocatingVisitor implements IAstVisitor {

    private final Class<? extends AbstractEntity> higherOrderType;
    private final String contextProperty;

    /**
     * The <code>higherOrderType</code> argument should represent a top level type for which all referenced by the AST nodes properties should be its properties or subproperties.
     * The <code>contextProperty</code> argument specifies the relative location in the type tree against which all other AST nodes' levels should be checked for compatibility.
     * The value of the <code>contextProperty</code> argument should be null if the higher-order type represents a context.
     *
     * @param higherOrderType
     * @param contextProperty
     */
    public LevelAllocatingVisitor(final Class<? extends AbstractEntity> higherOrderType, final String contextProperty) {
	this.higherOrderType = higherOrderType;
	this.contextProperty = contextProperty;
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
    public final void visit(final AstNode node) throws SemanticException {
	final EgTokenCategory cat = EgTokenCategory.byIndex(node.getToken().category.getIndex());
	switch (cat) {
	// level agnostic tokens
	case INT:
	case DECIMAL:
	case STRING:
	case DATE_CONST:
	    node.setLevel(null);
	    break;
	// collectional property dependent level
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
	case DAY_DIFF:
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
	final String[] parts = text.split("\\.");
	int level = 1;
	String property = "";
	for (int index = 0; index < parts.length; index++) {
	    property += parts[index];
	    final Field field = Finder.findFieldByName(higherOrderType, property);
	    if (Collection.class.isAssignableFrom(field.getType())) {
		level++;
	    }
	    property += ".";
	}
	return level;
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
	    if (child.getLevel() != null && level == null) {
		level = child.getLevel();
	    }

	    if (child.getLevel() != null && level != null && !level.equals(child.getLevel())) {
		throw new IncompatibleOperandException("Incompatible operand nesting level.", node.getToken());
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


    public static void main(final String[] args) {
	System.out.println('\u2190');
    }
}
