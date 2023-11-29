package ua.com.fielden.platform.eql.meta;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import ua.com.fielden.platform.entity.AbstractEntity;

/**
 * Represents entity metadata for every property defined, except so called "pure", which affectively have no data source behind them.
 *
 * @param <ET>
 */
public class EqlEntityMetadata<ET extends AbstractEntity<?>> implements Comparable<EqlEntityMetadata<ET>> {
    public final EntityTypeInfo<? super ET> typeInfo;
    public final Class<ET> entityType;
    private final List<EqlPropertyMetadata> props = new ArrayList<>();

    public EqlEntityMetadata(final Class<ET> entityType, EntityTypeInfo<? super ET> typeInfo, final List<EqlPropertyMetadata> props) {
        this.entityType = entityType;
        this.typeInfo = typeInfo;
        this.props.addAll(props);
    }

    public List<EqlPropertyMetadata> props() {
        return Collections.unmodifiableList(props);
    }

    @Override
    public int compareTo(final EqlEntityMetadata<ET> that) {
        return this.entityType.getSimpleName().compareTo(that.entityType.getSimpleName());
    }

}
