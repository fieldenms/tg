package ua.com.fielden.platform.entity.query.fluent;


public interface ProgressiveInterfacesWithBranchingSpike {

    interface OpenParenthesis {
        Operand begin();
    }
    
    interface Operand {
        AddOperation prop();
    }
    
    interface IfTrue {
    	I1 add(); 
    	I1 sub(); 
    }
    
    interface I1 {
    	I2 otherwise();
    }
    
    interface I2 {
    	ArithmeticalOperationSecondArgument add();
    	ArithmeticalOperationSecondArgument sub();
    }
    
    interface AddOperation {
        IfTrue ifTrue(boolean status);

    	ArithmeticalOperationSecondArgument add();
    }
    
    interface ArithmeticalOperatorOrEnd {
        IfTrue ifTrue(boolean status);
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

    interface ComparisonOperationSecondArgument {
        LogicalOperatorOrEnd prop();
    }
    
    
    public static void main(String[] args) {
    	OpenParenthesis a = null;
    	a.begin().prop().ifTrue(true).sub().otherwise().add().prop().add().prop().end();
	}
}
