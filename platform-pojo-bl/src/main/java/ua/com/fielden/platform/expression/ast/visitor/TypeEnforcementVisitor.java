package ua.com.fielden.platform.expression.ast.visitor;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Date;

import org.joda.time.DateTime;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.expression.EgTokenCategory;
import ua.com.fielden.platform.expression.ast.AstNode;
import ua.com.fielden.platform.expression.ast.IAstVisitor;
import ua.com.fielden.platform.expression.exception.semantic.CouldNotDetermineTypeException;
import ua.com.fielden.platform.expression.exception.semantic.SemanticException;
import ua.com.fielden.platform.expression.exception.semantic.TypeCompatibilityException;
import ua.com.fielden.platform.expression.exception.semantic.UnexpectedNumberOfOperandsException;
import ua.com.fielden.platform.expression.exception.semantic.UnsupportedTypeException;
import ua.com.fielden.platform.expression.type.AbstractDateLiteral;
import ua.com.fielden.platform.expression.type.DateLiteral;
import ua.com.fielden.platform.expression.type.Day;
import ua.com.fielden.platform.expression.type.Month;
import ua.com.fielden.platform.expression.type.Year;
import ua.com.fielden.platform.reflection.Finder;
import ua.com.fielden.platform.types.Money;

/**
 * A visitor, which enforces type compatibility between AST nodes and identifies the type of the expression represented by the AST.
 *
 * @author TG Team
 *
 */
public class TypeEnforcementVisitor implements IAstVisitor {

    private final Class<? extends AbstractEntity> context;

    /**
     * Context is a top level type for which all referenced by AST nodes properties should be its properties or subproperties.
     *
     * @param context
     */
    public TypeEnforcementVisitor(final Class<? extends AbstractEntity> context) {
	this.context = context;
    }

    @Override
    public final void visit(final AstNode node) throws SemanticException {
	final EgTokenCategory cat = EgTokenCategory.byIndex(node.getToken().category.getIndex());
	switch (cat) {
	// literal types
	case INT:
	    node.setType(Integer.class);
	    node.setValue(Integer.parseInt(node.getToken().text));
	    break;
	case DECIMAL:
	    node.setType(BigDecimal.class);
	    node.setValue(new BigDecimal(node.getToken().text));
	    break;
	case STRING:
	    node.setType(String.class);
	    node.setValue(node.getToken().text);
	    break;
	case DATE_CONST:
	    processDateLiteralToken(node);
	    break;
	// property types
	case NAME:
	    node.setType(identifyPropertyType(node));
	    break;
	// bi-operand operation types
	case PLUS:
	case MINUS:
	case MULT:
	case DIV:
	    processOperation(node);
	    break;
	// uno-operand date type functions
	case DAY:
	case MONTH:
	case YEAR:
	    processDatePartExtractionFunction(node);
	    break;
	// uno-operand string type functions
	case UPPER:
	case LOWER:
	    processStringFunction(node);
	    break;
	// bi-operand date type functions
	case DAY_DIFF:
	    processDateDiffFunction(node);
	    break;
	// uno-operand aggregation functions
	case AVG:
	case SUM:
	    processAvgSum(node);
	    break;
	case MIN:
	case MAX:
	    processMinMax(node);
	    break;
	case COUNT:
	    processCount(node);
	    break;
	default:
	    throw new TypeCompatibilityException("Unexpected token " + node.getToken() + " in AST node.", node.getToken());
	}
    }

    private void processAvgSum(final AstNode node) throws SemanticException {
	final EgTokenCategory cat = EgTokenCategory.byIndex(node.getToken().category.getIndex());
	if (node.getChildren().size() != 1) {
	    throw new UnexpectedNumberOfOperandsException("Operation " + cat + " expects 1 operands, found " + node.getChildren().size(), node.getToken());
	}
	// ensure that type of the operand is determined
	final Class<?> type = node.getChildren().get(0).getType();
	if (type == null) {
	    throw new TypeCompatibilityException("Operand " + node.getChildren().get(0) + " is missing type.", node.getChildren().get(0).getToken());
	}

	// validate operand type
	if (!Money.class.isAssignableFrom(type) && !BigDecimal.class.isAssignableFrom(type) && !Integer.class.isAssignableFrom(type)) {
	    throw new UnsupportedTypeException("Operand type " + type.getName() + " is not supported.", node.getToken());
	}
	// check whether operand is a constant value
	if (node.getChildren().get(0).getValue() != null) {
	    throw new TypeCompatibilityException("Constant value is not applicable to aggregation functions.", node.getChildren().get(0).getToken());
	}
	// determine resulting type
	if (EgTokenCategory.AVG == cat) {
	    if (Money.class.isAssignableFrom(type)) {
		node.setType(Money.class);
	    } else {
		node.setType(BigDecimal.class);
	    }
	} else if (EgTokenCategory.SUM == cat) {
	    if (Money.class.isAssignableFrom(type)) {
		node.setType(Money.class);
	    } else if (BigDecimal.class.isAssignableFrom(type)) {
		node.setType(BigDecimal.class);
	    } else if (Integer.class.isAssignableFrom(type)) {
		node.setType(Integer.class);
	    }
	}

    }

    /**
     * Validates correctness of the operand for the COUNT aggregation function and determines the type of the node.
     *
     * @param node
     * @throws SemanticException
     */
    private void processMinMax(final AstNode node) throws SemanticException {
	final EgTokenCategory cat = EgTokenCategory.byIndex(node.getToken().category.getIndex());
	if (node.getChildren().size() != 1) {
	    throw new UnexpectedNumberOfOperandsException("Operation " + cat + " expects 1 operands, found " + node.getChildren().size(), node.getToken());
	}
	// ensure that type of the operand is determined
	final Class<?> type = node.getChildren().get(0).getType();
	if (type == null) {
	    throw new TypeCompatibilityException("Operand " + node.getChildren().get(0) + " is missing type.", node.getChildren().get(0).getToken());
	}
	// set the node type
	node.setType(Integer.class);
    }

    /**
     * Validates correctness of the operand for the COUNT aggregation function and determines the type of the node.
     *
     * @param node
     * @throws SemanticException
     */
    private void processCount(final AstNode node) throws SemanticException {
	final EgTokenCategory cat = EgTokenCategory.byIndex(node.getToken().category.getIndex());
	if (node.getChildren().size() != 1) {
	    throw new UnexpectedNumberOfOperandsException("Operation " + cat + " expects 1 operands, found " + node.getChildren().size(), node.getToken());
	}
	// ensure that type of the operand is determined
	final Class<?> type = node.getChildren().get(0).getType();
	if (type == null) {
	    throw new TypeCompatibilityException("Operand " + node.getChildren().get(0) + " is missing type.", node.getChildren().get(0).getToken());
	}
	// set the node type
	node.setType(type);
    }

    /**
     * Validates correctness of the operand for the date diff function and determines the type of the node.
     *
     * @param node
     * @throws SemanticException
     */
    private void processDateDiffFunction(final AstNode node) throws SemanticException {
	final EgTokenCategory cat = EgTokenCategory.byIndex(node.getToken().category.getIndex());
	if (node.getChildren().size() != 2) {
	    throw new UnexpectedNumberOfOperandsException("Operation " + cat + " expects 2 operands, found " + node.getChildren().size(), node.getToken());
	}
	// ensure that types of the operands are determined
	final AstNode firstOperand = node.getChildren().get(0);
	final Class<?> firstOperandType = firstOperand.getType();
	if (firstOperandType == null) {
	    throw new TypeCompatibilityException("Operand " + firstOperand + " is missing type.", firstOperand.getToken());
	}
	final AstNode secondOperand = node.getChildren().get(1);
	final Class<?> secondOperandType = secondOperand.getType();
	if (secondOperandType == null) {
	    throw new TypeCompatibilityException("Operand " + secondOperand + " is missing type.", secondOperand.getToken());
	}

	// validate operand types
	if (Date.class.isAssignableFrom(firstOperandType) && DateTime.class.isAssignableFrom(firstOperandType)) {
	    throw new UnsupportedTypeException("First operand type " + firstOperandType.getName() + " is not supported.", firstOperand.getToken());
	}
	if (Date.class.isAssignableFrom(secondOperandType) && DateTime.class.isAssignableFrom(secondOperandType)) {
	    throw new UnsupportedTypeException("Secod operand type " + secondOperandType.getName() + " is not supported.", secondOperand.getToken());
	}

	// set the node type
	node.setType(Integer.class);
    }

    /**
     * Validates correctness of the operand for the string functions and determines the type of the node.
     *
     * @param node
     * @throws SemanticException
     */
    private void processStringFunction(final AstNode node) throws SemanticException {
	final EgTokenCategory cat = EgTokenCategory.byIndex(node.getToken().category.getIndex());
	if (node.getChildren().size() != 1) {
	    throw new UnexpectedNumberOfOperandsException("Operation " + cat + " expects 1 operand, found " + node.getChildren().size(), node.getToken());
	}
	// ensure that type of the operand is determined
	final Class<?> type = node.getChildren().get(0).getType();
	if (type == null) {
	    throw new TypeCompatibilityException("Operand " + node.getChildren().get(0) + " is missing type.", node.getChildren().get(0).getToken());
	}
	// validate operand type
	if (String.class.isAssignableFrom(type)) {
	    throw new UnsupportedTypeException("Operand type " + type.getName() + " is not supported.", node.getChildren().get(0).getToken());
	}

	// set the node type
	node.setType(String.class);
    }

    /**
     * Validates correctness of the operand for the date part extraction functions and determines the type of the node.
     *
     * @param node
     * @throws SemanticException
     */
    private void processDatePartExtractionFunction(final AstNode node) throws SemanticException {
	final EgTokenCategory cat = EgTokenCategory.byIndex(node.getToken().category.getIndex());
	if (node.getChildren().size() != 1) {
	    throw new UnexpectedNumberOfOperandsException("Operation " + cat + " expects 1 operand, found " + node.getChildren().size(), node.getToken());
	}
	// ensure that type of the operand is determined
	final Class<?> type = node.getChildren().get(0).getType();
	if (type == null) {
	    throw new TypeCompatibilityException("Operand " + node.getChildren().get(0) + " is missing type.", node.getChildren().get(0).getToken());
	}
	// validate operand type
	if (Date.class.isAssignableFrom(type) && DateTime.class.isAssignableFrom(type)) {
	    throw new UnsupportedTypeException("Operand type " + type.getName() + " is not supported.", node.getChildren().get(0).getToken());
	}

	// set the node type
	node.setType(Integer.class);
    }

    /**
     * Identifies AST node type based the property type.
     *
     * @param node
     * @return
     * @throws UnsupportedTypeException
     */
    private Class<?> identifyPropertyType(final AstNode node) throws UnsupportedTypeException {
	final Class<?> type = Finder.findFieldByName(context, node.getToken().text).getType();
	if (Money.class.isAssignableFrom(type)) {
	    return Money.class;
	} else if (BigDecimal.class.isAssignableFrom(type) || double.class.isAssignableFrom(type)) {
	    return BigDecimal.class;
	} else if (Integer.class.isAssignableFrom(type) || int.class.isAssignableFrom(type)) {
	    return Integer.class;
	} else if (String.class.isAssignableFrom(type)) {
	    return String.class;
	} else if (DateTime.class.isAssignableFrom(type)) {
	    return DateTime.class;
	} else if (Date.class.isAssignableFrom(type)) {
	    return Date.class;
	}
	throw new UnsupportedTypeException("Property of type " + type.getName() + " is not supported.", node.getToken());
    }

    /**
     * Determines the type of the operation and calculates the value if possible.
     *
     * Value calculation is possible if the operands are reducible to constants (i.e. have value).
     *
     * @param node
     * @param cat
     * @throws SemanticException
     */
    private void processOperation(final AstNode node) throws SemanticException {
	final EgTokenCategory cat = EgTokenCategory.byIndex(node.getToken().category.getIndex());
	if (node.getChildren().size() != 2) {
	    throw new UnexpectedNumberOfOperandsException("Operation " + cat + " expects 2 operands, found " + node.getChildren().size(), node.getToken());
	}
	// check if the operands have type
	final AstNode leftOperand = node.getChildren().get(0);
	final Class<?> leftOperandType = leftOperand.getType();
	if (leftOperandType == null) {
	    throw new TypeCompatibilityException("Operand " + leftOperand + " is missing type.", leftOperand.getToken());
	}
	final AstNode rightOperand = node.getChildren().get(1);
	final Class<?> rightOperandType = rightOperand.getType();
	if (rightOperandType == null) {
	    throw new TypeCompatibilityException("Operand " + rightOperand + " is missing type.", rightOperand.getToken());
	}

	// check compatibility of operand types
	// the type of the operation should be the lease restrictive type of its operands
	node.setType(operationType(node, leftOperandType, rightOperandType, cat));
	if (node.getType() == null) {
	    throw new CouldNotDetermineTypeException("Could not determine type for operation " + node, node.getToken());
	}

	// evaluate the operation
	node.setValue(evaluateOperation(node));
    }

    private Class<?> operationType(final AstNode node, final Class<?> leftOperandType, final Class<?> rightOperandType, final EgTokenCategory cat) throws SemanticException {
	switch (cat) {
	case PLUS:
	    if (isDate(leftOperandType) || isDate(rightOperandType)) {
		throw new UnsupportedTypeException("Operands of date type are not applicable to operation " + cat, node.getToken());
	    }
	    // is String?
	    if (String.class.isAssignableFrom(leftOperandType) && leftOperandType.isAssignableFrom(rightOperandType)) {
		return String.class;
	    }
	    // is BigDecimal or Money?
	    if (BigDecimal.class.isAssignableFrom(leftOperandType)) {
		if (leftOperandType.isAssignableFrom(rightOperandType) || Integer.class.isAssignableFrom(rightOperandType)) {
		    return BigDecimal.class;
		} else if (Money.class.isAssignableFrom(rightOperandType)) {
		    return Money.class;
		}
	    }
	    // is Integer or Money?
	    if (Integer.class.isAssignableFrom(leftOperandType)) {
		if (leftOperandType.isAssignableFrom(rightOperandType)) {
		    return Integer.class;
		} else if (BigDecimal.class.isAssignableFrom(rightOperandType)) {
		    return BigDecimal.class;
		} else if (Money.class.isAssignableFrom(rightOperandType)) {
		    return Money.class;
		}
	    }
	    // is Money or Integer or BigDecimal?
	    if (Money.class.isAssignableFrom(leftOperandType)) {
		if (leftOperandType.isAssignableFrom(rightOperandType) || Integer.class.isAssignableFrom(rightOperandType) || BigDecimal.class.isAssignableFrom(rightOperandType)) {
		    return Money.class;
		}
	    }
	    // is Day?
	    if (Day.class.isAssignableFrom(leftOperandType) && leftOperandType.isAssignableFrom(rightOperandType)) {
		return Day.class;
	    }
	    // is Month?
	    if (Month.class.isAssignableFrom(leftOperandType) && leftOperandType.isAssignableFrom(rightOperandType)) {
		return Month.class;
	    }
	    // is Year?
	    if (Year.class.isAssignableFrom(leftOperandType) && leftOperandType.isAssignableFrom(rightOperandType)) {
		return Year.class;
	    }
	case MINUS:
	    if (isDate(leftOperandType) || isDate(rightOperandType)) {
		throw new UnsupportedTypeException("Operands of date type are not applicable to operation " + cat + ".\nPlease consider using " + EgTokenCategory.DAY_DIFF + " function.", node.getToken());
	    }
	    if (String.class.isAssignableFrom(leftOperandType)) {
		throw new UnsupportedTypeException("Operands of string type are not applicable to operation " + cat, node.getToken());
	    }
	    // is BigDecimal or Money?
	    if (BigDecimal.class.isAssignableFrom(leftOperandType)) {
		if (leftOperandType.isAssignableFrom(rightOperandType) || Integer.class.isAssignableFrom(rightOperandType)) {
		    return BigDecimal.class;
		} else if (Money.class.isAssignableFrom(rightOperandType)) {
		    return Money.class;
		}
	    }
	    // is Integer or Money?
	    if (Integer.class.isAssignableFrom(leftOperandType)) {
		if (leftOperandType.isAssignableFrom(rightOperandType)) {
		    return Integer.class;
		} else if (BigDecimal.class.isAssignableFrom(rightOperandType)) {
		    return BigDecimal.class;
		} else if (Money.class.isAssignableFrom(rightOperandType)) {
		    return Money.class;
		}
		return Integer.class;
	    }
	    // is Money or Integer or BigDecimal?
	    if (Money.class.isAssignableFrom(leftOperandType)) {
		if (leftOperandType.isAssignableFrom(rightOperandType) || Integer.class.isAssignableFrom(rightOperandType) || BigDecimal.class.isAssignableFrom(rightOperandType)) {
		    return Money.class;
		}
	    }
	    // is Day?
	    if (Day.class.isAssignableFrom(leftOperandType) && leftOperandType.isAssignableFrom(rightOperandType)) {
		return Day.class;
	    }
	    // is Month?
	    if (Month.class.isAssignableFrom(leftOperandType) && leftOperandType.isAssignableFrom(rightOperandType)) {
		return Month.class;
	    }
	    // is Year?
	    if (Year.class.isAssignableFrom(leftOperandType) && leftOperandType.isAssignableFrom(rightOperandType)) {
		return Year.class;
	    }
	    break;
	case MULT:
	    if (isDate(leftOperandType) || isDate(rightOperandType)) {
		throw new UnsupportedTypeException("Operands of date type are not applicable to operation " + cat, node.getToken());
	    }
	    if (String.class.isAssignableFrom(leftOperandType)) {
		throw new UnsupportedTypeException("Operands of string type are not applicable to operation " + cat, node.getToken());
	    }
	    if (AbstractDateLiteral.class.isAssignableFrom(leftOperandType) || AbstractDateLiteral.class.isAssignableFrom(rightOperandType)) {
		throw new UnsupportedTypeException("Operands of date literal type are not applicable to operation " + cat, node.getToken());
	    }

	    // is BigDecimal or Money?
	    if (BigDecimal.class.isAssignableFrom(leftOperandType)) {
		if (leftOperandType.isAssignableFrom(rightOperandType) || Integer.class.isAssignableFrom(rightOperandType)) {
		    return BigDecimal.class;
		} else if (Money.class.isAssignableFrom(rightOperandType)) {
		    return Money.class;
		}
	    }
	    // is Integer or Money?
	    if (Integer.class.isAssignableFrom(leftOperandType)) {
		if (leftOperandType.isAssignableFrom(rightOperandType)) {
		    return Integer.class;
		} else if (BigDecimal.class.isAssignableFrom(rightOperandType)) {
		    return BigDecimal.class;
		} else if (Money.class.isAssignableFrom(rightOperandType)) {
		    return Money.class;
		}
		return Integer.class;
	    }
	    // is Money or Integer or BigDecimal?
	    if (Money.class.isAssignableFrom(leftOperandType)) {
		if (leftOperandType.isAssignableFrom(rightOperandType)) {
		    throw new TypeCompatibilityException("Operation " + cat + " is cannot be used when both operands are of monetary type.", node.getToken());
		}
		if (Integer.class.isAssignableFrom(rightOperandType) || BigDecimal.class.isAssignableFrom(rightOperandType)) {
		    return Money.class;
		}
	    }
	    break;
	case DIV:
	    if (isDate(leftOperandType) || isDate(rightOperandType)) {
		throw new UnsupportedTypeException("Operands of date type are not applicable to operation " + cat, node.getToken());
	    }
	    if (String.class.isAssignableFrom(leftOperandType)) {
		throw new UnsupportedTypeException("Operands of string type are not applicable to operation " + cat, node.getToken());
	    }
	    if (AbstractDateLiteral.class.isAssignableFrom(leftOperandType) || AbstractDateLiteral.class.isAssignableFrom(rightOperandType)) {
		throw new UnsupportedTypeException("Operands of date literal type are not applicable to operation " + cat, node.getToken());
	    }

	    // is BigDecimal or Money?
	    if (BigDecimal.class.isAssignableFrom(leftOperandType)) {
		if (leftOperandType.isAssignableFrom(rightOperandType) || Integer.class.isAssignableFrom(rightOperandType)) {
		    return BigDecimal.class;
		} else if (Money.class.isAssignableFrom(rightOperandType)) {
		    return Money.class;
		}
	    }
	    // is Integer or Money?
	    if (Integer.class.isAssignableFrom(leftOperandType)) {
		if (leftOperandType.isAssignableFrom(rightOperandType) || BigDecimal.class.isAssignableFrom(rightOperandType)) {
		    return BigDecimal.class;
		} else if (Money.class.isAssignableFrom(rightOperandType)) {
		    return Money.class;
		}
		return Integer.class;
	    }
	    // is Money or Integer or BigDecimal?
	    if (Money.class.isAssignableFrom(leftOperandType)) {
		if (leftOperandType.isAssignableFrom(rightOperandType)) {
		    return BigDecimal.class;
		}
		if (Integer.class.isAssignableFrom(rightOperandType) || BigDecimal.class.isAssignableFrom(rightOperandType)) {
		    return Money.class;
		}
	    }
	    break;

	}

	return null;
    }

    private boolean isDate(final Class<?> type) {
	return Date.class.isAssignableFrom(type) || DateTime.class.isAssignableFrom(type);
    }

    /**
     * Calculates the operation result is possible.
     *
     * @param node
     */
    private Object evaluateOperation(final AstNode node) {
	final EgTokenCategory cat = EgTokenCategory.byIndex(node.getToken().category.getIndex());
	final AstNode leftOperand = node.getChildren().get(0);
	final AstNode rightOperand = node.getChildren().get(1);
	if (leftOperand.hasValue() && rightOperand.hasValue()) {
	    switch (cat) {
	    case PLUS:
		if (BigDecimal.class.isAssignableFrom(node.getType())) {
		    final BigDecimal value = new BigDecimal(leftOperand.getValue().toString()).add(new BigDecimal(rightOperand.getValue().toString()));
		    return value;
		} else if (Integer.class.isAssignableFrom(node.getType())) {
		    final Integer value = new Integer(leftOperand.getValue().toString()) + (new Integer(rightOperand.getValue().toString()));
		    return value;
		} else if (String.class.isAssignableFrom(node.getType())) {
		    final String value = "\"" + leftOperand.getValue().toString().replaceAll("\"", "") + rightOperand.getValue().toString().replaceAll("\"", "") + "\"";
		    return value;
		} else if (Day.class.isAssignableFrom(node.getType())) {
		    final Day value = new Day(((Day) leftOperand.getValue()).getValue() + ((Day) rightOperand.getValue()).getValue());
		    return value;
		} else if (Month.class.isAssignableFrom(node.getType())) {
		    final Month value = new Month(((Month) leftOperand.getValue()).getValue() + ((Month) rightOperand.getValue()).getValue());
		    return value;
		} else if (Year.class.isAssignableFrom(node.getType())) {
		    final Year value = new Year(((Year) leftOperand.getValue()).getValue() + ((Year) rightOperand.getValue()).getValue());
		    return value;
		}
		break;
	    case MINUS:
		if (BigDecimal.class.isAssignableFrom(node.getType())) {
		    final BigDecimal value = new BigDecimal(leftOperand.getValue().toString()).subtract(new BigDecimal(rightOperand.getValue().toString()));
		    return value;
		} else if (Integer.class.isAssignableFrom(node.getType())) {
		    final Integer value = new Integer(leftOperand.getValue().toString()) - (new Integer(rightOperand.getValue().toString()));
		    return value;
		} else if (Day.class.isAssignableFrom(node.getType())) {
		    final Day value = new Day(((Day) leftOperand.getValue()).getValue() - ((Day) rightOperand.getValue()).getValue());
		    return value;
		} else if (Month.class.isAssignableFrom(node.getType())) {
		    final Month value = new Month(((Month) leftOperand.getValue()).getValue() - ((Month) rightOperand.getValue()).getValue());
		    return value;
		} else if (Year.class.isAssignableFrom(node.getType())) {
		    final Year value = new Year(((Year) leftOperand.getValue()).getValue() - ((Year) rightOperand.getValue()).getValue());
		    return value;
		}

		break;
	    case MULT:
		if (BigDecimal.class.isAssignableFrom(node.getType())) {
		    final BigDecimal value = new BigDecimal(leftOperand.getValue().toString()).multiply(new BigDecimal(rightOperand.getValue().toString()));
		    return value;
		} else if (Integer.class.isAssignableFrom(node.getType())) {
		    final Integer value = new Integer(leftOperand.getValue().toString()) * (new Integer(rightOperand.getValue().toString()));
		    return value;
		}
		break;
	    case DIV:
		if (BigDecimal.class.isAssignableFrom(node.getType()) || Integer.class.isAssignableFrom(node.getType())) {
		    final BigDecimal value = new BigDecimal(leftOperand.getValue().toString()).setScale(4, RoundingMode.HALF_EVEN).divide(new BigDecimal(rightOperand.getValue().toString()).setScale(4, RoundingMode.HALF_EVEN), RoundingMode.HALF_EVEN);
		    return value;
		}
		break;
	    }
	}
	return null;
    }

    /**
     * Processes date literal token in order to update the node with correct type and value.
     *
     * @param node
     * @throws TypeCompatibilityException
     */
    private void processDateLiteralToken(final AstNode node) throws TypeCompatibilityException {
	final String text = node.getToken().text;
	final String valueText = text.substring(0, text.length() - 1);
	if (text.endsWith(DateLiteral.DAY.discriminator)) {
	    node.setType(Day.class);
	    node.setValue(new Day(Integer.parseInt(valueText)));
	} else if (text.endsWith(DateLiteral.MONTH.discriminator)) {
	    node.setType(Month.class);
	    node.setValue(new Month(Integer.parseInt(valueText)));
	} else if (text.endsWith(DateLiteral.YEAR.discriminator)) {
	    node.setType(Year.class);
	    node.setValue(new Year(Integer.parseInt(valueText)));
	} else {
	    throw new TypeCompatibilityException("Value " + node.getToken().text + " could not be type casted to any of the date literal types.", node.getToken());
	}
    }

}
