package ua.com.fielden.platform.eql.stage3.operands;

import ua.com.fielden.platform.entity.query.DbVersion;
import ua.com.fielden.platform.eql.meta.PropType;
import ua.com.fielden.platform.eql.stage3.sources.ISource3;
import ua.com.fielden.platform.meta.IDomainMetadata;
import ua.com.fielden.platform.utils.ToString;

import java.util.Objects;

public class Prop3 extends AbstractSingleOperand3 {

    /**
     * In most cases a simple property name.
     * In case of a property declared in a union entity type -- path to a subproperty of the union type (e.g., {@code location.workshop}).
     * In case of a component-typed property -- path to a component subproperty (e.g., {@code richText.coreText}).
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
        return this == obj
               || obj instanceof Prop3 that
                  && Objects.equals(name, that.name)
                  && Objects.equals(source, that.source)
                  && super.equals(that);
    }

    @Override
    protected ToString addToString(final ToString toString) {
        return super.addToString(toString)
                .add("name", name)
                .add("source", source);
    }

}
