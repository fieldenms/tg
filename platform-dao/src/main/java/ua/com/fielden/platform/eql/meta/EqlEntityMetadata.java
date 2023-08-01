package ua.com.fielden.platform.eql.meta;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.metadata.EntityTypeInfo;

public class EqlEntityMetadata<ET extends AbstractEntity<?>> {
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
}