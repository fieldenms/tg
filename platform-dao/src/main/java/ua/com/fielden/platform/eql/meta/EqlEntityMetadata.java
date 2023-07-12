package ua.com.fielden.platform.eql.meta;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.metadata.EntityTypeInfo;

public class EqlEntityMetadata {
    public final EntityTypeInfo<? extends AbstractEntity<?>> typeInfo;
    public final Class<? extends AbstractEntity<?>> entityType;
    private final List<EqlPropertyMetadata> props = new ArrayList<>();  
    
    public EqlEntityMetadata(final Class<? extends AbstractEntity<?>> entityType, EntityTypeInfo<? extends AbstractEntity<?>> typeInfo, final List<EqlPropertyMetadata> props) {
        this.entityType = entityType;
        this.typeInfo = typeInfo;
        this.props.addAll(props);
    }
    
    public List<EqlPropertyMetadata> props() {
        return Collections.unmodifiableList(props);
    }
}