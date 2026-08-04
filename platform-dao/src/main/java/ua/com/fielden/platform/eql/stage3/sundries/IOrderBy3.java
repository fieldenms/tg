package ua.com.fielden.platform.eql.stage3.sundries;

import ua.com.fielden.platform.entity.query.DbVersion;
import ua.com.fielden.platform.eql.stage3.INode3;
import ua.com.fielden.platform.eql.stage3.operands.ISingleOperand3;
import ua.com.fielden.platform.meta.IDomainMetadata;
import ua.com.fielden.platform.utils.ToString;

public sealed interface IOrderBy3 extends ToString.IFormattable, INode3 {

    String DESC = " DESC", ASC = " ASC";

    String sql(IDomainMetadata metadata, DbVersion dbVersion);

    boolean isDesc();

    record Operand (ISingleOperand3 operand, boolean isDesc) implements IOrderBy3 {

        public String sql(final IDomainMetadata metadata, final DbVersion dbVersion) {
            return (operand.sql(metadata, dbVersion)) + (isDesc ? DESC : ASC);
        }

        public Operand setOperand(final ISingleOperand3 operand) {
            return operand == this.operand ? this : new Operand(operand, isDesc);
        }

        @Override
        public String toString() {
            return toString(ToString.separateLines());
        }

        @Override
        public String toString(final ToString.IFormat format) {
            return format.toString(this)
                    .add("isDesc", isDesc)
                    .add("operand", operand)
                    .$();
        }
    }

    // TODO Remove yieldColumn once the `sql` operation can access all yields of the enclosing query.
    // yieldColumn is accidental complexity, yieldName is the only essential information about a yield.
    // If the `sql` operation had access to all yields of the enclosing query, yieldName alone would suffice to obtain the corresponding yield's column.
    record Yield (String name, String column, boolean isDesc) implements IOrderBy3 {

        public String sql(final IDomainMetadata metadata, final DbVersion dbVersion) {
            return column + (isDesc ? DESC : ASC);
        }

        @Override
        public String toString() {
            return toString(ToString.separateLines());
        }

        @Override
        public String toString(final ToString.IFormat format) {
            return format.toString(this)
                    .add("yield", name)
                    .add("isDesc", isDesc)
                    .$();
        }
    }

}
