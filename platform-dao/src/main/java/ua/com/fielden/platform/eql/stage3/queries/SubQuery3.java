package ua.com.fielden.platform.eql.stage3.queries;

import ua.com.fielden.platform.entity.query.DbVersion;
import ua.com.fielden.platform.eql.exceptions.EqlStage3ProcessingException;
import ua.com.fielden.platform.eql.meta.PropType;
import ua.com.fielden.platform.eql.stage3.QueryComponents3;
import ua.com.fielden.platform.eql.stage3.operands.ISingleOperand3;
import ua.com.fielden.platform.meta.IDomainMetadata;
import ua.com.fielden.platform.utils.CollectionUtil;

import java.util.List;
import java.util.Objects;

import static ua.com.fielden.platform.eql.stage3.sundries.Yield3.NO_EXPECTED_TYPE;

public class SubQuery3 extends AbstractQuery3 implements ISingleOperand3 {

    private final PropType type;
    
    public SubQuery3(final QueryComponents3 queryComponents, final PropType type) {
        super(queryComponents, type.javaType());
        this.type = type;
    }

    @Override
    public String sql(final IDomainMetadata metadata, final DbVersion dbVersion) {
        if (yields.getYields().size() != 1) {
            throw new EqlStage3ProcessingException(
                    "Subquery must yield only 1 value but yields %s: [%s]".formatted(
                            yields.getYields().size(), CollectionUtil.toString(yields.getYields(), ", ")));
        }

        final PropType expectedType = Objects.requireNonNullElse(type, NO_EXPECTED_TYPE);
        return "(" + super.sql(metadata, dbVersion, List.of(expectedType)) + ")";
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
    
}
