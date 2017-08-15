package ua.com.fielden.platform.entity.query.fluent;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.ICaseWhenFunctionEnd;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.ICaseWhenFunctionLastArgument;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.ICaseWhenFunctionWhen;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IExprOperand0;

abstract class CaseWhenFunctionLastArgument<T, ET extends AbstractEntity<?>>
		extends AbstractExprOperand<ICaseWhenFunctionEnd<T>, IExprOperand0<ICaseWhenFunctionEnd<T>, ET>, ET>
		implements ICaseWhenFunctionLastArgument<T, ET> {

	abstract T getParent3();

	@Override
	IExprOperand0<ICaseWhenFunctionEnd<T>, ET> getParent2() {
		return new ExprOperand0<ICaseWhenFunctionEnd<T>, ET>() {
			@Override
			ICaseWhenFunctionEnd<T> getParent3() {
				return new CaseWhenFunctionEnd<T>() {

					@Override
					T getParent() {
						return CaseWhenFunctionLastArgument.this.getParent3();
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
				return CaseWhenFunctionLastArgument.this.getParent3();
			}

		};
	}
}