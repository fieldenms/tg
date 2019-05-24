package ua.com.fielden.platform.eql.stage2.elements;

public class CountAll2 extends ZeroOperandFunction2 {

    public CountAll2() {
        super("COUNT(*)");
    }

    @Override
    public String type() {
        return Long.class.getName();
    }
}