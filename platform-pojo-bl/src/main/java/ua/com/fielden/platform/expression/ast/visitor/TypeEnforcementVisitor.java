package ua.com.fielden.platform.expression.ast.visitor;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Collection;
import java.util.Date;

import org.joda.time.DateTime;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.expression.EgTokenCategory;
import ua.com.fielden.platform.expression.ast.AbstractAstVisitor;
import ua.com.fielden.platform.expression.ast.AstNode;
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
public class TypeEnforcementVisitor extends AbstractAstVisitor {

    public TypeEnforcementVisitor(final Class<? extends AbstractEntity> higherOrderType, final String contextProperty) {
	super(higherOrderType, contextProperty);
    }

    public TypeEnforcementVisitor(final Class<? extends AbstractEntity> higherOrderType) {
	super(higherOrderType, null);
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
	case SELF:
	    node.setType(getContextPropertyType());
	    break;
	case NOW:
	    node.setType(Date.class);
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
	case DAY_DIFF: // TODO deprecated function
	case DAYS:
	case MONTHS:
	case YEARS:
	    processDateDiffFunctions(node);
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
	case LT:
	case GT:
	case LE:
	case GE:
	case EQ:
	case NE:
	    processComparisonOperator(node);
	    break;
	case AND:
	case OR:
	    processLogicalOperator(node);
	    break;
	case CASE:
	    processCaseOperator(node);
	    break;
	case WHEN:
	    processWhenOperator(node);
	    break;
	default:
	    throw new TypeCompatibilityException("Unexpected token " + node.getToken() + " in AST node.", node.getToken());
	}
    }

    private void processComparisonOperator(final AstNode node) throws SemanticException {
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
	if ((String.class.isAssignableFrom(leftOperandType) && leftOperandType.isAssignableFrom(rightOperandType)) || // strings are comparable
	    (Money.class.isAssignableFrom(leftOperandType) && (leftOperandType.isAssignableFrom(rightOperandType) || Number.class.isAssignableFrom(rightOperandType)))	 || // money are comparable with each other and numbers
	    (Number.class.isAssignableFrom(leftOperandType) && (Number.class.isAssignableFrom(rightOperandType) || Money.class.isAssignableFrom(rightOperandType))) || // the same, but in reverse order
	    ((Date.class.isAssignableFrom(leftOperandType) || DateTime.class.isAssignableFrom(leftOperandType)) && (Date.class.isAssignableFrom(rightOperandType) || DateTime.class.isAssignableFrom(rightOperandType))) || // dates are comparable
	    (Day.class.isAssignableFrom(leftOperandType) && rightOperand.getToken().category == EgTokenCategory.DAYS) || // day literal is comparable only with DAYS function
	    (leftOperand.getToken().category == EgTokenCategory.DAYS && Day.class.isAssignableFrom(rightOperandType) ) || // the same, but in reverse
	    (Month.class.isAssignableFrom(leftOperandType) && rightOperand.getToken().category == EgTokenCategory.MONTHS) || // month literal is comparable only with MONTHS function
	    (leftOperand.getToken().category == EgTokenCategory.MONTHS && Month.class.isAssignableFrom(rightOperandType)) || // the same, but in reverse
	    (Year.class.isAssignableFrom(leftOperandType) && rightOperand.getToken().category == EgTokenCategory.YEARS) || // year literal is comparable only with YEARS function
	    (leftOperand.getToken().category == EgTokenCategory.YEARS && Year.class.isAssignableFrom(rightOperandType))) { // the same, but in reverse
	    // the type of the operation should be the lease restrictive type of its operands
	    node.setType(boolean.class);
	} else {
	    throw new UnsupportedTypeException("Operands for operation " + cat + " should have compatible types.", leftOperandType, node.getToken());
	}

    }

    private void processLogicalOperator(final AstNode node) throws SemanticException {
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
	if (!boolean.class.isAssignableFrom(leftOperandType) || !boolean.class.isAssignableFrom(rightOperandType)) {
	    throw new UnsupportedTypeException("Operands for operation " + cat + " should both be of boolean type.", leftOperandType, node.getToken());
	}
	node.setType(boolean.class);
    }

    private void processCaseOperator(final AstNode node) throws SemanticException {
	// simply ensure that all operands have type assigned
	for (final AstNode child : node.getChildren()) {
	    if (child.getType() == null) {
		throw new TypeCompatibilityException("Operand " + child + " is missing type.", child.getToken());
	    }
	}
	node.setType(String.class);
    }

    private void processWhenOperator(final AstNode node) throws SemanticException {
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
	if (!boolean.class.isAssignableFrom(leftOperandType)) {
	    throw new UnsupportedTypeException("First operand for operation " + cat + " should be of boolen type.", leftOperandType, node.getToken());
	}
	if (!String.class.isAssignableFrom(rightOperandType)) {
	    throw new UnsupportedTypeException("Second operand for operation " + cat + " should be of string type.", leftOperandType, node.getToken());
	}
	node.setType(String.class);
    }

    private void processAvgSum(final AstNode node) throws SemanticException {
	final EgTokenCategory cat = EgTokenCategory.byIndex(node.getToken().category.getIndex());
	if (node.getChildren().size() != 1) {
	    throw new UnexpectedNumberOfOperandsException("Operation " + cat + " expects 1 operand, found " + node.getChildren().size(), node.getToken());
	}
	// ensure that type of the operand is determined
	final Class<?> operandType = node.getChildren().get(0).getType();
	if (operandType == null) {
	    throw new TypeCompatibilityException("Operand " + node.getChildren().get(0) + " is missing type.", node.getChildren().get(0).getToken());
	}

	// validate operand type
	if (!Money.class.isAssignableFrom(operandType) && !BigDecimal.class.isAssignableFrom(operandType) && !Integer.class.isAssignableFrom(operandType)) {
	    throw new UnsupportedTypeException(operandType, node.getToken());
	}
	// check whether operand is a constant value
	if (node.getChildren().get(0).getValue() != null) {
	    throw new TypeCompatibilityException("Constant value is not applicable to aggregation functions.", node.getChildren().get(0).getToken());
	}
	// determine resulting type
	if (EgTokenCategory.AVG == cat) {
	    if (Money.class.isAssignableFrom(operandType)) {
		node.setType(Money.class);
	    } else {
		node.setType(BigDecimal.class);
	    }
	} else if (EgTokenCategory.SUM == cat) {
	    if (Money.class.isAssignableFrom(operandType)) {
		node.setType(Money.class);
	    } else if (BigDecimal.class.isAssignableFrom(operandType)) {
		node.setType(BigDecimal.class);
	    } else if (Integer.class.isAssignableFrom(operandType)) {
		node.setType(Integer.class);
	    }
	}

    }

    /**
     * Validates correctness of the operand for the MIN, MAX aggregation functions and determines the type of the node.
     *
     * @param node
     * @throws SemanticException
     */
    private void processMinMax(final AstNode node) throws SemanticException {
	final EgTokenCategory cat = EgTokenCategory.byIndex(node.getToken().category.getIndex());
	if (node.getChildren().size() != 1) {
	    throw new UnexpectedNumberOfOperandsException("Operation " + cat + " expects 1 operand, found " + node.getChildren().size(), node.getToken());
	}
	// ensure that type of the operand is determined
	final Class<?> operandType = node.getChildren().get(0).getType();
	if (operandType == null) {
	    throw new TypeCompatibilityException("Operand " + node.getChildren().get(0) + " is missing type.", node.getChildren().get(0).getToken());
	}

	// validate operand type
	if (!Money.class.isAssignableFrom(operandType) && !BigDecimal.class.isAssignableFrom(operandType) && !Integer.class.isAssignableFrom(operandType)
		&& !Date.class.isAssignableFrom(operandType) && !DateTime.class.isAssignableFrom(operandType) && !String.class.isAssignableFrom(operandType)) {
	    throw new UnsupportedTypeException(operandType, node.getToken());
	}
	// check whether operand is a constant value
	if (node.getChildren().get(0).getValue() != null) {
	    throw new TypeCompatibilityException("Constant value is not applicable to aggregation functions.", node.getChildren().get(0).getToken());
	}
	// resulting type should be compatible or match the operand type
	if (Money.class.isAssignableFrom(operandType)) {
	    node.setType(Money.class);
	} else if (BigDecimal.class.isAssignableFrom(operandType)) {
	    node.setType(BigDecimal.class);
	} else if (Integer.class.isAssignableFrom(operandType)) {
	    node.setType(Integer.class);
	} else if (Date.class.isAssignableFrom(operandType)) {
	    node.setType(Date.class);
	} else if (DateTime.class.isAssignableFrom(operandType)) {
	    node.setType(DateTime.class);
	} else {
	    node.setType(operandType);
	}
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
	final Class<?> operandType = node.getChildren().get(0).getType();
	if (operandType == null) {
	    throw new TypeCompatibilityException("Operand " + node.getChildren().get(0) + " is missing type.", node.getChildren().get(0).getToken());
	}
	// check whether operand is a constant value
	if (node.getChildren().get(0).getValue() != null) {
	    throw new TypeCompatibilityException("Constant value is not applicable to aggregation functions.", node.getChildren().get(0).getToken());
	}
	// set the node type
	node.setType(Integer.class);
    }

    /**
     * Validates correctness of operands for functions DAYS, MONTHS, YEARS and determines the type of the node.
     *
     * @param node
     * @throws SemanticException
     */
    private void processDateDiffFunctions(final AstNode node) throws SemanticException {
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
	if (!Date.class.isAssignableFrom(firstOperandType) && !DateTime.class.isAssignableFrom(firstOperandType)) {
	    throw new UnsupportedTypeException(firstOperandType, firstOperand.getToken());
	}
	if (!Date.class.isAssignableFrom(secondOperandType) && !DateTime.class.isAssignableFrom(secondOperandType)) {
	    throw new UnsupportedTypeException(secondOperandType, secondOperand.getToken());
	}

	// set the node type
	node.setType(Integer.class);
    }

    /**
     * Validates correctness of the operand for the string functions UPPER, LOWER and determines the type of the node.
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
	final Class<?> operandType = node.getChildren().get(0).getType();
	if (operandType == null) {
	    throw new TypeCompatibilityException("Operand " + node.getChildren().get(0) + " is missing type.", node.getChildren().get(0).getToken());
	}
	// validate operand type
	if (!String.class.isAssignableFrom(operandType)) {
	    throw new UnsupportedTypeException(operandType, node.getChildren().get(0).getToken());
	}

	// set the node type
	node.setType(String.class);
    }

    /**
     * Validates correctness of the operand for the date part extraction functions such as DAY, MONTH, YEAR and determines the type of the node.
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
	final Class<?> operandType = node.getChildren().get(0).getType();
	if (operandType == null) {
	    throw new TypeCompatibilityException("Operand " + node.getChildren().get(0) + " is missing type.", node.getChildren().get(0).getToken());
	}
	// validate operand type
	if (!Date.class.isAssignableFrom(operandType) && !DateTime.class.isAssignableFrom(operandType)) {
	    throw new UnsupportedTypeException(operandType, node.getChildren().get(0).getToken());
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
	final Field field = Finder.findFieldByName(getHigherOrderType(), relative2Absolute(node.getToken().text));
	final Class<?> type = field.getType();
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
	} else if (boolean.class.isAssignableFrom(type) || Boolean.class.isAssignableFrom(type)) {
	    return type;
	} else if (Collection.class.isAssignableFrom(type)) {
	    return type;
	} else if (AbstractEntity.class.isAssignableFrom(type)) {
	    return type;
	}

	throw new UnsupportedTypeException(type, node.getToken());
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

    /**
     * Provides the rules for identifying types of operation operands and the resultant operation type.
     *
     * @param node
     * @param leftOperandType
     * @param rightOperandType
     * @param cat
     * @return
     * @throws SemanticException
     */
    private Class<?> operationType(final AstNode node, final Class<?> leftOperandType, final Class<?> rightOperandType, final EgTokenCategory cat) throws SemanticException {
	// Date is not supported
	if (isDate(leftOperandType)) {
	    throw new UnsupportedTypeException("Left operand of date type is not applicable to operation " + cat, leftOperandType, node.getToken());
	}
	if (isDate(rightOperandType)) {
	    throw new UnsupportedTypeException("Right operand of date type are not applicable to operation " + cat, rightOperandType, node.getToken());
	}

	// collectional and entity types are not supported
	if (Collection.class.isAssignableFrom(leftOperandType)) {
	    throw new UnsupportedTypeException("Left operand of collectional type is not applicable to operation " + cat, leftOperandType, node.getToken());
	}
	if (Collection.class.isAssignableFrom(rightOperandType)) {
	    throw new UnsupportedTypeException("Right operand of collectional type is not  applicable to operation " + cat, leftOperandType, node.getToken());
	}
	if (AbstractEntity.class.isAssignableFrom(leftOperandType)) {
	    throw new UnsupportedTypeException("Left operand of entity type is not applicable to operation " + cat, leftOperandType, node.getToken());
	}
	if (AbstractEntity.class.isAssignableFrom(rightOperandType)) {
	    throw new UnsupportedTypeException("Right operand of entity type is not applicable to operation " + cat, leftOperandType, node.getToken());
	}

	switch (cat) {
	case PLUS:
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
	    break;
	case MINUS:
	    if (String.class.isAssignableFrom(leftOperandType) || String.class.isAssignableFrom(rightOperandType)) {
		throw new UnsupportedTypeException("Operands of string type are not applicable to operation " + cat, String.class, node.getToken());
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
	    if (String.class.isAssignableFrom(leftOperandType) || String.class.isAssignableFrom(rightOperandType)) {
		throw new UnsupportedTypeException("Operands of string type are not applicable to operation " + cat, String.class, node.getToken());
	    }
	    if (AbstractDateLiteral.class.isAssignableFrom(leftOperandType) || AbstractDateLiteral.class.isAssignableFrom(rightOperandType)) {
		throw new UnsupportedTypeException("Operands of date literal type are not applicable to operation " + cat, AbstractDateLiteral.class, node.getToken());
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
	    if (String.class.isAssignableFrom(leftOperandType) || String.class.isAssignableFrom(rightOperandType)) {
		throw new UnsupportedTypeException("Operands of string type are not applicable to operation " + cat, String.class, node.getToken());
	    }
	    if (AbstractDateLiteral.class.isAssignableFrom(leftOperandType) || AbstractDateLiteral.class.isAssignableFrom(rightOperandType)) {
		throw new UnsupportedTypeException("Operands of date literal type are not applicable to operation " + cat, AbstractDateLiteral.class, node.getToken());
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
