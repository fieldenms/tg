package ua.com.fielden.platform.eql.stage3.operands.functions;

import ua.com.fielden.platform.entity.query.DbVersion;
import ua.com.fielden.platform.meta.IDomainMetadata;

import static ua.com.fielden.platform.eql.meta.PropType.INTEGER_PROP_TYPE;

public class CountAll3 extends AbstractFunction3 {
    
    public static final CountAll3 INSTANCE = new CountAll3();
    
    private CountAll3() {
        super(INTEGER_PROP_TYPE);
    }

    private static final String COUNT_ALL_SQL = "COUNT(*)";
    
    @Override
    public String sql(final IDomainMetadata metadata, final DbVersion dbVersion) {
        return COUNT_ALL_SQL;
    }
}
