package ua.com.fielden.platform.eql.stage3.operands.functions;

import static ua.com.fielden.platform.eql.meta.PropType.INTEGER_PROP_TYPE;

import ua.com.fielden.platform.eql.meta.EqlDomainMetadata;

public class CountAll3 extends AbstractFunction3 {
    
    public static CountAll3 INSTANCE = new CountAll3();
    
    private CountAll3() {
        super(INTEGER_PROP_TYPE);
    }

    private static final String COUNT_ALL_SQL = "COUNT(*)";
    
    @Override
    public String sql(final EqlDomainMetadata metadata) {
        return COUNT_ALL_SQL;
    }
}
