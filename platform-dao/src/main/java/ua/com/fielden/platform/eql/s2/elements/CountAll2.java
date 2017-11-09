package ua.com.fielden.platform.eql.s2.elements;

public class CountAll2 extends ZeroOperandFunction2 {

    public CountAll2() {
        super("COUNT(*)");
    }

    @Override
    public Class type() {
        return Long.class;
    }
}