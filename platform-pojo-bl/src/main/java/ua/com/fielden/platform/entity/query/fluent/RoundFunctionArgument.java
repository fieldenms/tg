package ua.com.fielden.platform.entity.query.fluent;

import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IExprOperand0;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IRoundFunctionArgument;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IRoundFunctionTo;

final class RoundFunctionArgument<T> extends AbstractExprOperand<IRoundFunctionTo<T>, IExprOperand0<IRoundFunctionTo<T>>> implements IRoundFunctionArgument<T> {
    T parent;
    RoundFunctionArgument(final Tokens queryTokens, final T parent) {
	super(queryTokens);
	this.parent = parent;
    }

    @Override
    IExprOperand0<IRoundFunctionTo<T>> getParent2() {
	return new ExprOperand0<IRoundFunctionTo<T>>(getTokens(), new RoundFunctionTo<T>(getTokens(), parent));
    }

    @Override
    IRoundFunctionTo<T> getParent() {
	return new RoundFunctionTo<T>(getTokens(), parent);
    }
}
