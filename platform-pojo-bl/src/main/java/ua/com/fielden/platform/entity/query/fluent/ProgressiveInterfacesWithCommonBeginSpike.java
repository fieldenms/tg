package ua.com.fielden.platform.entity.query.fluent;


public interface ProgressiveInterfacesWithCommonBeginSpike {

    interface OpenParenthesis {
        Operand begin();
    }
    
    interface Operand {
        UnknownOperation prop();
    }
    
    interface UnknownOperation {
        ArithmeticalOperationSecondArgument add();
        ComparisonOperationSecondArgument gt();
    }
    
    interface ArithmeticalOperatorOrEnd {
        ArithmeticalOperationSecondArgument add();
        void end();
    }

    interface ArithmeticalOperationSecondArgument {
        ArithmeticalOperatorOrEnd prop();
    }

    interface LogicalOperatorOrEnd {
        ComparisonOperationFirstArgument and();
        void end();
    }

    interface ComparisonOperationFirstArgument {
        ComparisonOperator prop();
    }

    interface ComparisonOperator {
        ComparisonOperationSecondArgument gt();
    }

    interface ComparisonOperationSecondArgument {
        LogicalOperatorOrEnd prop();
    }
    
    
    public static void main(String[] args) {
    	OpenParenthesis a = null;
    	a.begin().prop().add().prop().add().prop().end();
	}
}
