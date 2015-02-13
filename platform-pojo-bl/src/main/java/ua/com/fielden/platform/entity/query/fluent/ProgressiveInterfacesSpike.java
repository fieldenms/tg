package ua.com.fielden.platform.entity.query.fluent;


public interface ProgressiveInterfacesSpike {

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
    
    public static void main(final String[] args) {
        final OpenParenthesis a = null;
        
        a.begin().prop().add().prop().add().prop().add().prop().end();
        
        a.begin().prop().add().prop().end();
        
        a.begin().prop().gt().prop().end();
        
        a.begin().prop().gt().prop().and().prop().gt().prop().end();
    }
}
