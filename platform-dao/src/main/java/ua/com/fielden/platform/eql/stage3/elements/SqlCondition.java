package ua.com.fielden.platform.eql.stage3.elements;

public class SqlCondition {
    public final PropColumn pc1;
    public final PropColumn pc2;
    
    
    public SqlCondition(final PropColumn pc1, final PropColumn pc2) {
        this.pc1 = pc1;
        this.pc2 = pc2;
    }
    
    public String sql() {
        return pc1.sql() + " = " + pc2.sql();
    }
    

}