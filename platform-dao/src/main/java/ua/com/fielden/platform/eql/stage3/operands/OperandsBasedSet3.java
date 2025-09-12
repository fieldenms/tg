package ua.com.fielden.platform.eql.stage3.operands;

import ua.com.fielden.platform.entity.query.DbVersion;
import ua.com.fielden.platform.meta.IDomainMetadata;
import ua.com.fielden.platform.utils.ToString;

import java.util.List;

import static java.lang.String.format;
import static java.util.stream.Collectors.joining;

public record OperandsBasedSet3 (List<ISingleOperand3> operands) implements ISetOperand3, ToString.IFormattable {

    @Override
    public String sql(final IDomainMetadata metadata, final DbVersion dbVersion) {
        return String.format("(%s)", operands.stream().map(op -> op.sql(metadata, dbVersion)).collect(joining(", ")));
    }

    @Override
    public String toString() {
        return toString(ToString.separateLines());
    }

    @Override
    public String toString(final ToString.IFormat format) {
        return format.toString(this)
                .add("operands", operands)
                .$();
    }

}
