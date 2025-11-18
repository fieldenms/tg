package ua.com.fielden.platform.serialisation.api;

import com.google.inject.ImplementedBy;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.serialisation.api.impl.SerialisationTypeEncoder;
import ua.com.fielden.platform.serialisation.api.impl.TgJackson;

/**
 * This custom contract determines entity type from its full name. Implementation of this contract is used mainly for 
 * generated types, which should be dependent on centre context, from which they were generated.
 * <p>
 * This contract is used for: ad-hoc registration of generated types on server's {@link TgJackson} serialisation engine, serialising type information along with 
 * actual entity instances, ...
 * 
 * @author TG Team
 *
 */
@ImplementedBy(SerialisationTypeEncoder.class)
public interface ISerialisationTypeEncoder {
    
    /**
     * Decodes {@link String} entity type name into entity type that is used during {@link TgJackson} deserialisation
     * of entity instances.
     * 
     * @param entityTypeName
     * @return
     */
    <T extends AbstractEntity<?>> Class<T> decode(final String entityTypeName);
    
    ISerialisationTypeEncoder setTgJackson(final ISerialiserEngine tgJackson);
}
