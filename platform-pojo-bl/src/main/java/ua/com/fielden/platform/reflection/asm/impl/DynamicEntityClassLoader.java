package ua.com.fielden.platform.reflection.asm.impl;

import static ua.com.fielden.platform.utils.Pair.pair;

import java.util.Optional;

import org.kohsuke.asm5.ClassReader;

import ua.com.fielden.platform.classloader.TgSystemClassLoader;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.utils.Pair;

/**
 * A class loader for dynamically constructed or modified entity types.
 * <p>
 * All created types should be loaded by the same instance as used for type modification.
 *
 * This class is NOT thread safe!!! Nor should it be!
 *
 * @author TG Team
 */
public class DynamicEntityClassLoader extends ClassLoader {

    private final TgSystemClassLoader parent;
    
    public static DynamicEntityClassLoader getInstance(final ClassLoader parent) {
        if (parent instanceof TgSystemClassLoader) {
            return new DynamicEntityClassLoader(parent);
        }
        
        throw new IllegalArgumentException("The parent class loader can only be of type TgSystemClassLoader.");
    }
    
    private DynamicEntityClassLoader(final ClassLoader parent) {
        super(parent);
        this.parent = (TgSystemClassLoader) parent;
    }
    
    public Optional<Pair<Class<?>, byte[]>> getTypeByNameFromCache(final String typeName) {
        return parent.classByName(typeName);
    }
    
    public DynamicEntityClassLoader registerClass(final Pair<Class<?>, byte[]> typePair) {
        parent.cacheClassDefinition(typePair.getKey(), typePair.getValue());
        return this;
    }
    
    /**
     * Initiates adaptation of the specified by name type. This could be either dynamic or static type (created manually by developer).
     *
     * @param typeName
     * @return
     * @throws ClassNotFoundException
     */
    public TypeMaker startModification(final String typeName) throws ClassNotFoundException {
        return new TypeMaker(this).startModification(typeName);
    }

    protected final Class<?> defineType(final String name, final byte[] b, final int off, final int len) {
        return super.defineClass(name, b, off, len);
    }
    
    public Class<?> defineClass(final byte[] currentType) {
        // let's find out whether currentType has already been loaded
        // if it is then simply return the previously cached class
        final String typeName = new ClassReader(currentType).getClassName().replace("/", ".");
        
        return getTypeByNameFromCache(typeName).map(Pair::getKey).orElseGet(() -> {
            // the class was not yet loaded, so it needs to be loaded and cached to later reuse
            final Class klass = defineClass(null, currentType, 0, currentType.length);
            registerClass(pair(klass, currentType));
            return klass;
        });
    }
    
    /**
     * Returns <code>true</code> in case when the type is generated using ASM in TG platform from other (statically defined) entity type, <code>false</code> otherwise.
     * <p>
     * Most likely, generated types have one or more calculated (or custom) property. But there are also edge-cases
     * (for example, in tests like SerialisationTestResource) when generated type have no calculated properties, but just have changed the type name.
     * 
     * @param type
     * @return
     */
    public static boolean isGenerated(final Class<?> type) {
        return type.getName().contains(DynamicTypeNamingService.APPENDIX);
    }

    @Override
    public Class<?> findClass(final String name) throws ClassNotFoundException {
        return parent.findClass(name);
    }

    /**
     * Returns an original type for the specified one (the type from which <code>type</code> was generated). If <code>type</code> is not enhanced -- returns the same
     * <code>type</code>.
     *
     * @param type
     * @return
     */
    @SuppressWarnings("unchecked")
    public static <T extends AbstractEntity<?>> Class<T> getOriginalType(final Class<?> type) {
        final String typeName = type.getName();
        if (isGenerated(type)) {
            final String originalTypeName = typeName.substring(0, typeName.indexOf(DynamicTypeNamingService.APPENDIX));
            try {
                return (Class<T>) ClassLoader.getSystemClassLoader().loadClass(originalTypeName);
            } catch (final ClassNotFoundException e) {
                throw new IllegalStateException(e);
            }
        } else {
            return (Class<T>) type;
        }
    }

    public byte[] getCachedByteArray(final String name) {
        return getTypeByNameFromCache(name).map(Pair::getValue).orElse(null);
    }
}
