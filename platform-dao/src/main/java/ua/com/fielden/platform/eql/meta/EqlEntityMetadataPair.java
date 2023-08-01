package ua.com.fielden.platform.eql.meta;

import ua.com.fielden.platform.entity.AbstractEntity;

/**
 * Represents parameterised pair of values -- entity type and its eql metadata.
 * 
 * 
 * @param <ET> -- entity type parameter
 * 
 * @author TG Team
 */
public record EqlEntityMetadataPair<ET extends AbstractEntity<?>> (Class<ET> entityType, EqlEntityMetadata<ET> eqlEntityMetadata) {}
