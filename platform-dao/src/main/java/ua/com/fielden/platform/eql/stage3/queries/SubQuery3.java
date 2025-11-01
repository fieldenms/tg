package ua.com.fielden.platform.eql.stage3.queries;

import org.apache.logging.log4j.Logger;
import ua.com.fielden.platform.entity.query.DbVersion;
import ua.com.fielden.platform.eql.exceptions.EqlStage3ProcessingException;
import ua.com.fielden.platform.eql.meta.PropType;
import ua.com.fielden.platform.eql.stage3.QueryComponents3;
import ua.com.fielden.platform.eql.stage3.operands.ISingleOperand3;
import ua.com.fielden.platform.meta.IDomainMetadata;
import ua.com.fielden.platform.utils.ToString;

import java.util.List;
import java.util.Objects;

import static org.apache.logging.log4j.LogManager.getLogger;
import static ua.com.fielden.platform.utils.ToString.separateLines;

public class SubQuery3 extends AbstractQuery3 implements ISingleOperand3 {

    private static final Logger LOGGER = getLogger();
    public static final String ERR_SUBQUERY_MUST_YIELD_ONLY_ONE_VALUE = "A scalar subquery must yield only 1 value but yields %s.";

    private final PropType type;
    
    public SubQuery3(final QueryComponents3 queryComponents, final PropType type) {
        super(queryComponents, type.javaType());
        this.type = type;
    }

    @Override
    public String sql(final IDomainMetadata metadata, final DbVersion dbVersion) {
        if (yields.getYields().size() != 1) {
            final var exception = new EqlStage3ProcessingException(ERR_SUBQUERY_MUST_YIELD_ONLY_ONE_VALUE.formatted(yields.getYields().size()));
            LOGGER.error(() -> separateLines().toString(exception.getMessage())
                                 .add("yields", yields)
                                 .$(),
                         exception);
            throw exception;
        }

        return "(" + super.sql(metadata, dbVersion, List.of(type)) + ")";
    }
    
    @Override
    public PropType type() {
        return type;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + type.hashCode();
        return prime * result + SubQuery3.class.getName().hashCode();
    }

    @Override
    public boolean equals(final Object obj) {
        return this == obj || obj instanceof SubQuery3 that && super.equals(that) && Objects.equals(type, that.type);
    }

    @Override
    protected ToString addToString(final ToString toString) {
        return super.addToString(toString).add("type", type);
    }

}
