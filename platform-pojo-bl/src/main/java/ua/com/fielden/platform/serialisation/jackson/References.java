package ua.com.fielden.platform.serialisation.jackson;

import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Set;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.reflection.asm.impl.DynamicEntityClassLoader;

/**
 * Class representing references to instances already serialised or deserialised.
 *
 * It is used mainly as a cache to resolve circular references during serialisation and provide better performance during deserialisation.
 *
 * @author TG Team
 *
 */
public class References {
    private final IdentityHashMap<AbstractEntity<?>, String> entityToReference = new IdentityHashMap<>();
    private final Map<String, AbstractEntity<?>> referenceToEntity = new HashMap<>();
    private final Map<String, Long> typeToRefCount = new HashMap<>();

    public void reset() {
        entityToReference.clear();
        referenceToEntity.clear();
        typeToRefCount.clear();
    }

    /**
     * Generates and returns the next reference number for the entity of the specified <code>type</code>.
     *
     * @param type
     * @return
     */
    public Long addNewId(final Class<? extends AbstractEntity<?>> type) {
        final Long count = typeToRefCount.get(type.getName());
        if (count == null) {
            typeToRefCount.put(type.getName(), 1L); // starting from 1
        } else {
            typeToRefCount.put(type.getName(), count + 1); // takes the next integer
        }
        return typeToRefCount.get(type.getName());
    }

    public AbstractEntity<?> getEntity(final String reference) {
        final AbstractEntity<?> entity = referenceToEntity.get(reference);
        if (entity == null) {
            throw new IllegalStateException("EntityJsonDeserialiser has tried to get entity with serialisationId [" + reference + "] from the cache, but that entity does not exist.");
        }
        return entity;
    }

    public References putEntity(final String reference, final AbstractEntity<?> entity) {
        if (referenceToEntity.containsKey(reference)) {
            throw new IllegalStateException("EntityJsonDeserialiser has tried to put entity of type [" + entity.getType().getName() + "] with existing serialisationId [" + reference + "] to the cache.");
        }
        referenceToEntity.put(reference, entity);
        return this;
    }

    public String getReference(final AbstractEntity<?> entity) {
        return entityToReference.get(entity);
    }

    public String putReference(final AbstractEntity<?> entity, final String reference) {
        if (entityToReference.get(entity) != null) {
            throw new IllegalStateException("EntityJsonSerialiser has tried to put reference for existing entity [" + entity + "] of type [" + entity.getType().getName() + "] to the cache.");
        }
        return entityToReference.put(entity, reference);
    }

    public Set<AbstractEntity<?>> getNotEnhancedEntities() {
        //        for (int index = 2; index < 2 + references.referenceCount; index++) {
        //            final Object obj = references.referenceToObject.get(index);
        //
        //            // let's try to identify whether we are loading generated types here
        //            if (obj != null && DynamicEntityClassLoader.isEnhanced(obj.getClass())) {
        //                return;
        //            }
        //
        //            // interested only in instances of the enhanced AbstractEntity.
        //            if (obj instanceof AbstractEntity) {
        //                refs.add((AbstractEntity<?>) obj);
        //            }
        //        }

        final Set<AbstractEntity<?>> refs = new HashSet<>();
        for (final AbstractEntity<?> entity : referenceToEntity.values()) {
            if (entity != null && !DynamicEntityClassLoader.isGenerated(entity.getClass())) {
                refs.add(entity);
            }
        }

        return refs;
    }
}
