package ua.com.fielden.platform.entity.query.fluent;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.ICaseWhenFunctionArgument;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.ICaseWhenFunctionWhen;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IExprOperand0;

abstract class CaseWhenFunctionArgument<T, ET extends AbstractEntity<?>>
		extends AbstractExprOperand<ICaseWhenFunctionWhen<T, ET>, IExprOperand0<ICaseWhenFunctionWhen<T, ET>, ET>, ET>
		implements ICaseWhenFunctionArgument<T, ET> {
	abstract T getParent3();

	@Override
	IExprOperand0<ICaseWhenFunctionWhen<T, ET>, ET> getParent2() {
		return new ExprOperand0<ICaseWhenFunctionWhen<T, ET>, ET>() {

			@Override
			ICaseWhenFunctionWhen<T, ET> getParent3() {
				return new CaseWhenFunctionWhen<T, ET>() {

					@Override
					T getParent() {
						return CaseWhenFunctionArgument.this.getParent3();
					}

				};
			}

		};
	}

	@Override
	ICaseWhenFunctionWhen<T, ET> getParent() {
		return new CaseWhenFunctionWhen<T, ET>() {

			@Override
			T getParent() {
				return CaseWhenFunctionArgument.this.getParent3();
			}

		};
	}
}