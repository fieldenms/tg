package ua.com.fielden.platform.expression.ast.visitor;

import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.cond;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.expr;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.ICaseWhenFunctionWhen;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IDateDiffFunctionArgument;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IDateDiffFunctionBetween;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IFunctionLastArgument;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IStandAloneExprOperand;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IStandAloneExprOperationAndClose;
import ua.com.fielden.platform.entity.query.model.ConditionModel;
import ua.com.fielden.platform.entity.query.model.ExpressionModel;
import ua.com.fielden.platform.expression.EgTokenCategory;
import ua.com.fielden.platform.expression.ast.AbstractAstVisitor;
import ua.com.fielden.platform.expression.ast.AstNode;
import ua.com.fielden.platform.expression.exception.semantic.SemanticException;
import ua.com.fielden.platform.expression.exception.semantic.TypeCompatibilityException;
import ua.com.fielden.platform.expression.type.AbstractDateLiteral;

/**
 * A visitor, which generates a computational model for AST.
 *
 * @author TG Team
 *
 */
public class ModelGeneratingVisitor extends AbstractAstVisitor {

    /**
     * {@inheritDoc}
     */
    public ModelGeneratingVisitor(final Class<? extends AbstractEntity<?>> higherOrderType, final String contextProperty) {
	super(higherOrderType, contextProperty);
    }

    @Override
    public final void visit(final AstNode node) throws SemanticException {
	final EgTokenCategory cat = EgTokenCategory.byIndex(node.getToken().category.getIndex());
	switch (cat) {
	// literal types
	case INT:
	case DECIMAL:
	case STRING:
	case DATE_CONST:
	case DATE:
	    node.setModel(createLiteralModel(node));
	    break;
	case NOW:
	    node.setModel(createNowModel(node));
	    break;
	    // property types
	case NAME:
	    node.setModel(createPropertyModel(node));
	    break;
	    // self types
	case SELF:
	    node.setModel(createSelfModel(node));
	    break;
	    // bi-operand operation types
	case PLUS:
	case MINUS:
	case MULT:
	case DIV:
	    node.setModel(createOperationModel(node));
	    break;
	    // uno-operand date type functions
	case DAY:
	case MONTH:
	case YEAR:
	    node.setModel(createDateFunctionModel(node));
	    break;
	    // uno-operand date type functions
	case UPPER:
	case LOWER:
	    node.setModel(createStringFunctionModel(node));
	    break;
	    // bi-operand date type functions
	case DAY_DIFF: // TODO to be removed as deprecated
	    node.setModel(createDayDiffFunctionModel(node));
	    break;
	case DAYS:
	    node.setModel(createDaysFunctionModel(node));
	    break;
	case MONTHS:
	    node.setModel(createMonthsFunctionModel(node));
	    break;
	case YEARS:
	    node.setModel(createYearsFunctionModel(node));
	    break;
	case GT:
	case LT:
	case GE:
	case LE:
	case EQ:
	case NE:
	    node.setConditionModel(createComparisonModel(node));
	    break;
	case AND:
	case OR:
	    node.setConditionModel(createLogicalModel(node));
	    break;
	case WHEN: // no need to process WHEN, ELSE and END nodes for model generation as it will be consumed as part of CASE processing
	case ELSE:
	case END:
	    break;
	case CASE:
	    node.setModel(createCaseModel(node));
	    break;
	    // uno-operand aggregation functions
	case AVG:
	case SUM:
	case MIN:
	case MAX:
	case COUNT:
	    node.setModel(createAggregateFunctionModel(node));
	    break;
	default:
	    throw new TypeCompatibilityException("Unexpected token " + node.getToken() + " in AST node.", node.getToken());
	}
    }

    private ExpressionModel createCaseModel(final AstNode node) {
	final EgTokenCategory cat = EgTokenCategory.byIndex(node.getToken().category.getIndex());
	if (cat != EgTokenCategory.CASE) {
	    throw new IllegalArgumentException("CASE node is expected.");
	}

	// the case node should have n children where (n-1) represent WHEN clause, and the last n-th child is the ELSE result
	// there should be at least one WHERE clause
	final AstNode firstWhere = node.getChildren().get(0);
	ICaseWhenFunctionWhen<IStandAloneExprOperationAndClose, AbstractEntity<?>> expr = expr().caseWhen().condition(firstWhere.getChildren().get(0).getConditionModel()).then().val(firstWhere.getChildren().get(1).getValue());
	for (int index = 1 /* exclude the first node */; index < node.getChildren().size() - 1 /* exclude the last node */; index++) {
	    final AstNode where = node.getChildren().get(index);
	    expr = expr.when().condition(where.getChildren().get(0).getConditionModel()).then().val(where.getChildren().get(1).getValue());
	}
	final AstNode elseNode = node.getChildren().get(node.getChildren().size() - 1);
	final ExpressionModel model = expr.otherwise().val(elseNode.getValue()).end().model();
	return model;
    }

    private ConditionModel createLogicalModel(final AstNode node) throws TypeCompatibilityException {
	final EgTokenCategory cat = EgTokenCategory.byIndex(node.getToken().category.getIndex());

	final AstNode left = node.getChildren().get(0);
	final AstNode right = node.getChildren().get(1);

	switch (cat) {
	case AND:
	    return cond().condition(left.getConditionModel()).and().condition(right.getConditionModel()).model();
	case OR:
	    return cond().condition(left.getConditionModel()).or().condition(right.getConditionModel()).model();
	default:
	    throw new TypeCompatibilityException("Unexpected token " + node.getToken() + " in AST node.", node.getToken());
	}
    }

    private ConditionModel createComparisonModel(final AstNode node) throws TypeCompatibilityException {
	final EgTokenCategory cat = EgTokenCategory.byIndex(node.getToken().category.getIndex());

	final AstNode left = node.getChildren().get(0);
	final AstNode right = node.getChildren().get(1);

	switch (cat) {
	case GT:
	    if (left.getModel() != null && !left.getModel().containsSingleValueToken()) {
		if (right.getModel() != null && !right.getModel().containsSingleValueToken()) {
		    return cond().expr(left.getModel()).gt().expr(right.getModel()).model();
		} else {
		    return cond().expr(left.getModel()).gt().val(right.getValue()).model();
		}
	    } else {
		if (right.getModel() != null && !right.getModel().containsSingleValueToken()) {
		    return cond().val(left.getValue()).gt().expr(right.getModel()).model();
		} else {
		    return cond().val(left.getValue()).gt().val(right.getValue()).model();
		}
	    }
	case LT:
	    if (left.getModel() != null && !left.getModel().containsSingleValueToken()) {
		if (right.getModel() != null && !right.getModel().containsSingleValueToken()) {
		    return cond().expr(left.getModel()).lt().expr(right.getModel()).model();
		} else {
		    return cond().expr(left.getModel()).lt().val(right.getValue()).model();
		}
	    } else {
		if (right.getModel() != null && !right.getModel().containsSingleValueToken()) {
		    return cond().val(left.getValue()).lt().expr(right.getModel()).model();
		} else {
		    return cond().val(left.getValue()).lt().val(right.getValue()).model();
		}
	    }
	case GE:
	    if (left.getModel() != null && !left.getModel().containsSingleValueToken()) {
		if (right.getModel() != null && !right.getModel().containsSingleValueToken()) {
		    return cond().expr(left.getModel()).ge().expr(right.getModel()).model();
		} else {
		    return cond().expr(left.getModel()).ge().val(right.getValue()).model();
		}
	    } else {
		if (right.getModel() != null && !right.getModel().containsSingleValueToken()) {
		    return cond().val(left.getValue()).ge().expr(right.getModel()).model();
		} else {
		    return cond().val(left.getValue()).ge().val(right.getValue()).model();
		}
	    }
	case LE:
	    if (left.getModel() != null && !left.getModel().containsSingleValueToken()) {
		if (right.getModel() != null && !right.getModel().containsSingleValueToken()) {
		    return cond().expr(left.getModel()).le().expr(right.getModel()).model();
		} else {
		    return cond().expr(left.getModel()).le().val(right.getValue()).model();
		}
	    } else {
		if (right.getModel() != null && !right.getModel().containsSingleValueToken()) {
		    return cond().val(left.getValue()).le().expr(right.getModel()).model();
		} else {
		    return cond().val(left.getValue()).le().val(right.getValue()).model();
		}
	    }
	case EQ:
	    if (left.getModel() != null && !left.getModel().containsSingleValueToken()) {
		if (right.getModel() != null && !right.getModel().containsSingleValueToken()) {
		    return cond().expr(left.getModel()).eq().expr(right.getModel()).model();
		} else {
		    return cond().expr(left.getModel()).eq().val(right.getValue()).model();
		}
	    } else {
		if (right.getModel() != null && !right.getModel().containsSingleValueToken()) {
		    return cond().val(left.getValue()).eq().expr(right.getModel()).model();
		} else {
		    return cond().val(left.getValue()).eq().val(right.getValue()).model();
		}
	    }
	case NE:
	    if (left.getModel() != null && !left.getModel().containsSingleValueToken()) {
		if (right.getModel() != null && !right.getModel().containsSingleValueToken()) {
		    return cond().expr(left.getModel()).ne().expr(right.getModel()).model();
		} else {
		    return cond().expr(left.getModel()).ne().val(right.getValue()).model();
		}
	    } else {
		if (right.getModel() != null && !right.getModel().containsSingleValueToken()) {
		    return cond().val(left.getValue()).ne().expr(right.getModel()).model();
		} else {
		    return cond().val(left.getValue()).ne().val(right.getValue()).model();
		}
	    }
	default:
	    throw new TypeCompatibilityException("Unexpected token " + node.getToken() + " in AST node.", node.getToken());
	}
    }




    private ExpressionModel createNowModel(final AstNode node) {
	final EgTokenCategory cat = EgTokenCategory.byIndex(node.getToken().category.getIndex());
	if (cat != EgTokenCategory.NOW) {
	    throw new IllegalArgumentException("Only NOW nodes are permitted.");
	}
	return expr().now().model();
    }

    private ExpressionModel createPropertyModel(final AstNode node) {
	final EgTokenCategory cat = EgTokenCategory.byIndex(node.getToken().category.getIndex());
	if (cat != EgTokenCategory.NAME) {
	    throw new IllegalArgumentException("Only property nodes are permitted.");
	}
	return expr().prop(relative2AbsoluteInverted(node.getToken().text)).model();
    }

    /**
     * Creates value model for literals. Has special processing for STRING to remove leading and trailing double quote.
     *
     * @param node
     * @return
     */
    private ExpressionModel createLiteralModel(final AstNode node) {
	if (node.getToken().category == EgTokenCategory.DATE_CONST) {
	    return expr().val(((AbstractDateLiteral) node.getValue()).getValue()).model();
	}
	return expr().val(node.getValue()).model();
    }


    /**
     * The model for keyword SELF is not really needed as it can be used only in the context of function COUNT that
     * is provided with <code>countAll()</code> model (no arguments that would depend on the model for SELF).
     * @param node
     * @return
     */
    private ExpressionModel createSelfModel(final AstNode node) {
	final EgTokenCategory cat = EgTokenCategory.byIndex(node.getToken().category.getIndex());
	if (cat != EgTokenCategory.SELF) {
	    throw new IllegalArgumentException("Only SELF nodes are permitted.");
	}
	return expr().prop(AbstractEntity.ID).model();
    }

    private ExpressionModel createOperationModel(final AstNode node) {
	final EgTokenCategory cat = EgTokenCategory.byIndex(node.getToken().category.getIndex());

	IStandAloneExprOperand expr = expr();
	final IStandAloneExprOperationAndClose exprWithLeft;

	// identify left operand expression model
	final AstNode leftOperand = node.getChildren().get(0);
	exprWithLeft = operandOperationModel(expr, leftOperand);

	// identify the operation expression model
	switch (cat) {
	case PLUS:
	    expr = exprWithLeft.add();
	    break;
	case MINUS:
	    expr = exprWithLeft.sub();
	    break;
	case MULT:
	    expr = exprWithLeft.mult();
	    break;
	case DIV:
	    expr = exprWithLeft.div();
	    break;
	}

	// identify right operand expression model
	final AstNode rightOperand = node.getChildren().get(1);
	final IStandAloneExprOperationAndClose exprWithRight = operandOperationModel(expr, rightOperand);
	return exprWithRight.model();
    }

    /**
     * Composes operand computational model for operations by identifying whether it represents an expression, literal or property.
     *
     * @param expr
     * @param operand
     * @return
     */
    private IStandAloneExprOperationAndClose operandOperationModel(final IStandAloneExprOperand expr, final AstNode operand) {
	final EgTokenCategory cat = EgTokenCategory.byIndex(operand.getToken().category.getIndex());
	return determineModel(expr, operand, cat);
    }

    /**
     * Composes operand computational model for uno-argument functions by identifying whether it represents an expression, literal or property.
     *
     * @param expr
     * @param operand
     * @return
     */
    private IStandAloneExprOperationAndClose unoOperandModel(final IFunctionLastArgument<IStandAloneExprOperationAndClose, AbstractEntity<?>> expr, final AstNode operand) {
	final EgTokenCategory cat = EgTokenCategory.byIndex(operand.getToken().category.getIndex());
	return determineModel(expr, operand, cat);
    }

    /**
     * Helper method to obtain correct model for expression operand nodes.
     *
     * @param expr
     * @param operand
     * @param cat
     * @return
     */
    private IStandAloneExprOperationAndClose determineModel(final IFunctionLastArgument<IStandAloneExprOperationAndClose, AbstractEntity<?>> expr, final AstNode operand, final EgTokenCategory cat) {
	if (cat == EgTokenCategory.NAME) {
	    return expr.prop(relative2AbsoluteInverted(operand.getToken().text));
	} else {
	    return operand.getModel() != null ? expr.expr(operand.getModel()) : expr.val(operand.getValue());
	}
    }

    /**
     * Helper method to obtain correct model for expression operand nodes. Similar to the above, but with different arguments
     *
     * @param expr
     * @param operand
     * @param cat
     * @return
     */
    private IStandAloneExprOperationAndClose determineModel(final IStandAloneExprOperand expr, final AstNode operand, final EgTokenCategory cat) {
	if (cat == EgTokenCategory.NAME) {
	    return expr.prop(relative2AbsoluteInverted(operand.getToken().text));
	} else {
	    return operand.getModel() != null && !operand.getModel().containsSingleValueToken() ? expr.expr(operand.getModel()) : expr.val(operand.getValue());
	}
    }


    private ExpressionModel createDateFunctionModel(final AstNode node) throws TypeCompatibilityException {
	final EgTokenCategory cat = EgTokenCategory.byIndex(node.getToken().category.getIndex());
	final IFunctionLastArgument<IStandAloneExprOperationAndClose, AbstractEntity<?>> expr;
	switch (cat) {
	case DAY:
	    expr = expr().dayOf();
	    break;
	case MONTH:
	    expr = expr().monthOf();
	    break;
	case YEAR:
	    expr = expr().yearOf();
	    break;
	default:
	    throw new TypeCompatibilityException("Unexpected token " + node.getToken() + " in AST node.", node.getToken());
	}
	return unoOperandModel(expr, node.getChildren().get(0)).model();
    }

    private ExpressionModel createStringFunctionModel(final AstNode node) {
	final EgTokenCategory cat = EgTokenCategory.byIndex(node.getToken().category.getIndex());
	final IFunctionLastArgument<IStandAloneExprOperationAndClose, AbstractEntity<?>> expr = cat == EgTokenCategory.UPPER ? expr().upperCase() : expr().lowerCase();
	return unoOperandModel(expr, node.getChildren().get(0)).model();
    }

    private ExpressionModel createDayDiffFunctionModel(final AstNode node) throws TypeCompatibilityException {
	final EgTokenCategory cat = EgTokenCategory.byIndex(node.getToken().category.getIndex());
	if (cat != EgTokenCategory.DAY_DIFF) {
	    throw new TypeCompatibilityException("Unexpected token " + node.getToken() + " in AST node.", node.getToken());
	}

	final IDateDiffFunctionArgument<IStandAloneExprOperationAndClose, AbstractEntity<?>> expr = expr().count().days().between();

	// identify left operand expression model
	final AstNode leftOperand = node.getChildren().get(0);
	final IDateDiffFunctionBetween<IStandAloneExprOperationAndClose, AbstractEntity<?>> exprWithOperand;

	final EgTokenCategory cat1 = EgTokenCategory.byIndex(leftOperand.getToken().category.getIndex());
	if (cat1 == EgTokenCategory.NAME) {
	    exprWithOperand =  expr.prop(relative2AbsoluteInverted(leftOperand.getToken().text));
	} else {
	    exprWithOperand = leftOperand.getModel() != null ? expr.expr(leftOperand.getModel()) : expr.val(leftOperand.getValue());
	}


	final IFunctionLastArgument<IStandAloneExprOperationAndClose, AbstractEntity<?>> andExpr = exprWithOperand.and();

	// identify right operand expression model
	final AstNode rightOperand = node.getChildren().get(1);
	final IStandAloneExprOperationAndClose exprWithOperand1;

	final EgTokenCategory cat2 = EgTokenCategory.byIndex(rightOperand.getToken().category.getIndex());
	if (cat2 == EgTokenCategory.NAME) {
	    exprWithOperand1 =  andExpr.prop(relative2AbsoluteInverted(rightOperand.getToken().text));
	} else {
	    exprWithOperand1 = leftOperand.getModel() != null ? andExpr.expr(rightOperand.getModel()) : andExpr.val(rightOperand.getValue());
	}

	return exprWithOperand1.model();
    }

    private ExpressionModel createDaysFunctionModel(final AstNode node) throws TypeCompatibilityException {
	final EgTokenCategory cat = EgTokenCategory.byIndex(node.getToken().category.getIndex());
	if (cat != EgTokenCategory.DAYS) {
	    throw new TypeCompatibilityException("Unexpected token " + node.getToken() + " in AST node.", node.getToken());
	}

	final IDateDiffFunctionArgument<IStandAloneExprOperationAndClose, AbstractEntity<?>> expr = expr().count().days().between();

	// identify left operand expression model
	final AstNode leftOperand = node.getChildren().get(0);
	final IDateDiffFunctionBetween<IStandAloneExprOperationAndClose, AbstractEntity<?>> exprWithOperand;

	final EgTokenCategory cat1 = EgTokenCategory.byIndex(leftOperand.getToken().category.getIndex());
	if (cat1 == EgTokenCategory.NAME) {
	    exprWithOperand =  expr.prop(relative2AbsoluteInverted(leftOperand.getToken().text));
	} else {
	    exprWithOperand = leftOperand.getModel() != null && !leftOperand.getModel().containsSingleValueToken() ? expr.expr(leftOperand.getModel()) : expr.val(leftOperand.getValue());
	}

	final IFunctionLastArgument<IStandAloneExprOperationAndClose, AbstractEntity<?>> andExpr = exprWithOperand.and();

	// identify right operand expression model
	final AstNode rightOperand = node.getChildren().get(1);
	final IStandAloneExprOperationAndClose exprWithOperand1;

	final EgTokenCategory cat2 = EgTokenCategory.byIndex(rightOperand.getToken().category.getIndex());
	if (cat2 == EgTokenCategory.NAME) {
	    exprWithOperand1 =  andExpr.prop(relative2AbsoluteInverted(rightOperand.getToken().text));
	} else {
	    exprWithOperand1 = leftOperand.getModel() != null && !leftOperand.getModel().containsSingleValueToken() ? andExpr.expr(rightOperand.getModel()) : andExpr.val(rightOperand.getValue());
	}

	return exprWithOperand1.model();
    }

    private ExpressionModel createMonthsFunctionModel(final AstNode node) throws TypeCompatibilityException {
	final EgTokenCategory cat = EgTokenCategory.byIndex(node.getToken().category.getIndex());
	if (cat != EgTokenCategory.MONTHS) {
	    throw new TypeCompatibilityException("Unexpected token " + node.getToken() + " in AST node.", node.getToken());
	}

	final IDateDiffFunctionArgument<IStandAloneExprOperationAndClose, AbstractEntity<?>> expr = expr().count().months().between();

	// identify left operand expression model
	final AstNode leftOperand = node.getChildren().get(0);
	final IDateDiffFunctionBetween<IStandAloneExprOperationAndClose, AbstractEntity<?>> exprWithOperand;

	final EgTokenCategory cat1 = EgTokenCategory.byIndex(leftOperand.getToken().category.getIndex());
	if (cat1 == EgTokenCategory.NAME) {
	    exprWithOperand =  expr.prop(relative2AbsoluteInverted(leftOperand.getToken().text));
	} else {
	    exprWithOperand = leftOperand.getModel() != null && !leftOperand.getModel().containsSingleValueToken() ? expr.expr(leftOperand.getModel()) : expr.val(leftOperand.getValue());
	}

	final IFunctionLastArgument<IStandAloneExprOperationAndClose, AbstractEntity<?>> andExpr = exprWithOperand.and();

	// identify right operand expression model
	final AstNode rightOperand = node.getChildren().get(1);
	final IStandAloneExprOperationAndClose exprWithOperand1;

	final EgTokenCategory cat2 = EgTokenCategory.byIndex(rightOperand.getToken().category.getIndex());
	if (cat2 == EgTokenCategory.NAME) {
	    exprWithOperand1 =  andExpr.prop(relative2AbsoluteInverted(rightOperand.getToken().text));
	} else {
	    exprWithOperand1 = leftOperand.getModel() != null && !leftOperand.getModel().containsSingleValueToken() ? andExpr.expr(rightOperand.getModel()) : andExpr.val(rightOperand.getValue());
	}

	return exprWithOperand1.model();
    }

    private ExpressionModel createYearsFunctionModel(final AstNode node) throws TypeCompatibilityException {
	final EgTokenCategory cat = EgTokenCategory.byIndex(node.getToken().category.getIndex());
	if (cat != EgTokenCategory.YEARS) {
	    throw new TypeCompatibilityException("Unexpected token " + node.getToken() + " in AST node.", node.getToken());
	}

	final IDateDiffFunctionArgument<IStandAloneExprOperationAndClose, AbstractEntity<?>> expr = expr().count().years().between();

	// identify left operand expression model
	final AstNode leftOperand = node.getChildren().get(0);
	final IDateDiffFunctionBetween<IStandAloneExprOperationAndClose, AbstractEntity<?>> exprWithOperand;

	final EgTokenCategory cat1 = EgTokenCategory.byIndex(leftOperand.getToken().category.getIndex());
	if (cat1 == EgTokenCategory.NAME) {
	    exprWithOperand =  expr.prop(relative2AbsoluteInverted(leftOperand.getToken().text));
	} else {
	    exprWithOperand = leftOperand.getModel() != null && !leftOperand.getModel().containsSingleValueToken() ? expr.expr(leftOperand.getModel()) : expr.val(leftOperand.getValue());
	}

	final IFunctionLastArgument<IStandAloneExprOperationAndClose, AbstractEntity<?>> andExpr = exprWithOperand.and();

	// identify right operand expression model
	final AstNode rightOperand = node.getChildren().get(1);
	final IStandAloneExprOperationAndClose exprWithOperand1;

	final EgTokenCategory cat2 = EgTokenCategory.byIndex(rightOperand.getToken().category.getIndex());
	if (cat2 == EgTokenCategory.NAME) {
	    exprWithOperand1 =  andExpr.prop(relative2AbsoluteInverted(rightOperand.getToken().text));
	} else {
	    exprWithOperand1 = leftOperand.getModel() != null && !leftOperand.getModel().containsSingleValueToken() ? andExpr.expr(rightOperand.getModel()) : andExpr.val(rightOperand.getValue());
	}

	return exprWithOperand1.model();
    }

    private ExpressionModel createAggregateFunctionModel(final AstNode node) throws TypeCompatibilityException {
	final EgTokenCategory cat = EgTokenCategory.byIndex(node.getToken().category.getIndex());
	final IFunctionLastArgument<IStandAloneExprOperationAndClose, AbstractEntity<?>> expr;

	// first check if the current node represents COUNT with SELF argument
	if (EgTokenCategory.COUNT == cat && EgTokenCategory.SELF == node.getChildren().get(0).getToken().category) {
	    return expr().countAll().model();
	}

	// otherwise proceed is a generic for all functions way
	switch (cat) {
	case SUM:
	    expr = expr().sumOf();
	    break;
	case AVG:
	    expr = expr().avgOf();
	    break;
	case MIN:
	    expr = expr().minOf();
	    break;
	case MAX:
	    expr = expr().maxOf();
	    break;
	case COUNT:
	    expr = expr().countOfDistinct();
	    break;
	default:
	    throw new TypeCompatibilityException("Unexpected token " + node.getToken() + " in AST node.", node.getToken());
	}

	return unoOperandModel(expr, node.getChildren().get(0)).model();
    }

    /**
     * Produces an expression model for the passed in condition model.
     * 
     * @param conditionModel
     * @return
     */
    public ExpressionModel toExpressionModel(final ConditionModel conditionModel) {
	return expr().caseWhen().condition(conditionModel).then().val(true).otherwise().val(false).end().model();
    }

}
