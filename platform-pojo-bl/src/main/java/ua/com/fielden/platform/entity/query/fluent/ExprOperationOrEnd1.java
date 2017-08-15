package ua.com.fielden.platform.entity.query.fluent;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IExprOperand1;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IExprOperationOrEnd0;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IExprOperationOrEnd1;

abstract class ExprOperationOrEnd1<T, ET extends AbstractEntity<?>>
		extends AbstractExprOperationOrEnd<IExprOperand1<T, ET>, IExprOperationOrEnd0<T, ET>, ET>
		implements IExprOperationOrEnd1<T, ET> {

	abstract T getParent3();


	@Override
	IExprOperationOrEnd0<T, ET> getParent2() {
		return new ExprOperationOrEnd0<T, ET>() {

			@Override
			T getParent3() {
				return ExprOperationOrEnd1.this.getParent3();
			}

		};
	}

	@Override
	IExprOperand1<T, ET> getParent() {
		return new ExprOperand1<T, ET>() {

			@Override
			T getParent3() {
				return ExprOperationOrEnd1.this.getParent3();
			}
		};
	}
}