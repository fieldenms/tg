package ua.com.fielden.platform.serialisation.api;

import com.google.inject.ImplementedBy;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.serialisation.api.impl.SerialisationTypeEncoder;
import ua.com.fielden.platform.serialisation.api.impl.TgJackson;

/// This custom contract determines entity type from its full name.
/// Implementation of this contract is used mainly for generated types, which should be dependent on the centre context, from which they were generated.
///
/// This contract is used for:
/// - Ad hoc registration of generated types in the server's [TgJackson] serialisation engine.
/// - Serialising type information along with actual entity instances, and more.
///
@ImplementedBy(SerialisationTypeEncoder.class)
public interface ISerialisationTypeEncoder {
    
    /// Decodes a [String] entity type name into an entity type that is used during [TgJackson] deserialisation of entity instances.
    ///
    <T extends AbstractEntity<?>> Class<T> decode(final String entityTypeName);
    
    ISerialisationTypeEncoder setTgJackson(final ISerialiserEngine tgJackson);

}
