package ua.com.fielden.platform.eql.meta;

import ua.com.fielden.platform.domaintree.ICalculatedProperty.CalculatedPropertyCategory;
import ua.com.fielden.platform.entity.query.model.ExpressionModel;

public record CalcPropInfo(ExpressionModel expressionModel, CalculatedPropertyCategory category) {

}
