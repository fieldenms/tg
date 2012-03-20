package ua.com.fielden.platform.expression.ast.visitor;

import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.expr;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IDateDiffFunctionArgument;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IDateDiffFunctionBetween;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IFunctionLastArgument;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IStandAloneExprOperand;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IStandAloneExprOperationAndClose;
import ua.com.fielden.platform.entity.query.model.ExpressionModel;
import ua.com.fielden.platform.expression.EgTokenCategory;
import ua.com.fielden.platform.expression.ast.AbstractAstVisitor;
import ua.com.fielden.platform.expression.ast.AstNode;
import ua.com.fielden.platform.expression.exception.semantic.SemanticException;
import ua.com.fielden.platform.expression.exception.semantic.TypeCompatibilityException;

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
    public ModelGeneratingVisitor(final Class<? extends AbstractEntity> higherOrderType, final String contextProperty) {
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
	    break;
	// property types
	case NAME:
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
	// uno-operand string type functions
	case UPPER:
	case LOWER:
	    node.setModel(createStringFunctionModel(node));
	    break;
	// bi-operand date type functions
	case DAY_DIFF:
	    node.setModel(createDayDiffFunctionModel(node));
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
	final IStandAloneExprOperationAndClose exprWithRight;
	exprWithRight = operandOperationModel(expr, rightOperand);
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
	final IStandAloneExprOperationAndClose exprWithOperand;
	if (operand.getModel() == null) {
	    final EgTokenCategory cat = EgTokenCategory.byIndex(operand.getToken().category.getIndex());
	    exprWithOperand = cat == EgTokenCategory.NAME ? expr.prop(relative2Absolute(operand.getToken().text)) : expr.val(operand.getToken().text);
	} else {
	    exprWithOperand = expr.expr(operand.getModel());
	}
	return exprWithOperand;
    }

    /**
     * Composes operand computational model for uno-argument functions by identifying whether it represents an expression, literal or property.
     *
     * @param expr
     * @param operand
     * @return
     */
    private IStandAloneExprOperationAndClose unoOperandModel(final IFunctionLastArgument<IStandAloneExprOperationAndClose> expr, final AstNode operand) {
	final IStandAloneExprOperationAndClose exprWithOperand;
	if (operand.getModel() == null) {
	    final EgTokenCategory cat = EgTokenCategory.byIndex(operand.getToken().category.getIndex());
	    exprWithOperand = cat == EgTokenCategory.NAME ? expr.prop(relative2Absolute(operand.getToken().text)) : expr.val(operand.getToken().text);
	} else {
	    exprWithOperand = expr.expr(operand.getModel());
	}
	return exprWithOperand;
    }

    private ExpressionModel createDateFunctionModel(final AstNode node) throws TypeCompatibilityException {
	final EgTokenCategory cat = EgTokenCategory.byIndex(node.getToken().category.getIndex());
	final IFunctionLastArgument<IStandAloneExprOperationAndClose> expr;
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
	final IFunctionLastArgument<IStandAloneExprOperationAndClose> expr = cat == EgTokenCategory.UPPER ? expr().upperCase() : expr().lowerCase();
	return unoOperandModel(expr, node.getChildren().get(0)).model();
    }

    private ExpressionModel createDayDiffFunctionModel(final AstNode node) throws TypeCompatibilityException {
	final EgTokenCategory cat = EgTokenCategory.byIndex(node.getToken().category.getIndex());
	if (cat != EgTokenCategory.DAY_DIFF) {
	    throw new TypeCompatibilityException("Unexpected token " + node.getToken() + " in AST node.", node.getToken());
	}

	final IDateDiffFunctionArgument<IStandAloneExprOperationAndClose> expr = expr().countDays().between();

	// identify left operand expression model
	final AstNode leftOperand = node.getChildren().get(0);
	final IDateDiffFunctionBetween<IStandAloneExprOperationAndClose> exprWithOperand;
	if (leftOperand.getModel() == null) {
	    final EgTokenCategory cat1 = EgTokenCategory.byIndex(leftOperand.getToken().category.getIndex());
	    exprWithOperand = cat1 == EgTokenCategory.NAME ? expr.prop(relative2Absolute(leftOperand.getToken().text)) : expr.val(leftOperand.getToken().text);
	} else {
	    exprWithOperand = expr.expr(leftOperand.getModel());
	}


	final IFunctionLastArgument<IStandAloneExprOperationAndClose> andExpr = exprWithOperand.and();

	// identify right operand expression model
	final AstNode rightOperand = node.getChildren().get(1);
	final IStandAloneExprOperationAndClose exprWithOperand1;
	if (rightOperand.getModel() == null) {
	    final EgTokenCategory cat1 = EgTokenCategory.byIndex(rightOperand.getToken().category.getIndex());
	    exprWithOperand1 = cat1 == EgTokenCategory.NAME ? andExpr.prop(rightOperand.getToken().text) : andExpr.val(rightOperand.getToken().text);
	} else {
	    exprWithOperand1 = andExpr.expr(rightOperand.getModel());
	}


	return exprWithOperand1.model();
    }

    private ExpressionModel createAggregateFunctionModel(final AstNode node) throws TypeCompatibilityException {
	final EgTokenCategory cat = EgTokenCategory.byIndex(node.getToken().category.getIndex());
	final IFunctionLastArgument<IStandAloneExprOperationAndClose> expr;

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
	    expr = expr().countOf();
	    break;
	default:
	    throw new TypeCompatibilityException("Unexpected token " + node.getToken() + " in AST node.", node.getToken());
	}
	return unoOperandModel(expr, node.getChildren().get(0)).model();
    }

}
