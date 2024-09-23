package ua.com.fielden.platform.eql.stage3.operands;

import ua.com.fielden.platform.entity.query.DbVersion;
import ua.com.fielden.platform.eql.meta.PropType;
import ua.com.fielden.platform.eql.stage3.sources.ISource3;
import ua.com.fielden.platform.meta.IDomainMetadata;

import java.util.Objects;

public class Prop3 extends AbstractSingleOperand3 {

    /**
     * Ordinarily property name, but in case of union-type property this name contains a subproperty of the union type (e.g., "location.workshop").
     * If component types were to be supported, then it could also include lower level component attribute name (e.g., "cost.amount").
     */
    public final String name;

    /**
     * Either table or query where property {@code name} lives.
     */
    public final ISource3 source;

    public Prop3(final String name, final ISource3 source, final PropType type) {
        super(type);
        this.name = name;
        this.source = source;
    }

    @Override
    public String sql(final IDomainMetadata metadata, final DbVersion dbVersion) {
        return source.column(name);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + name.hashCode();
        result = prime * result + (source == null ? 0 : source.hashCode());
        return result;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }

        if (!super.equals(obj)) {
            return false;
        }

        if (!(obj instanceof Prop3)) {
            return false;
        }

        final Prop3 other = (Prop3) obj;

        return Objects.equals(name, other.name) && Objects.equals(source, other.source);
    }
}
