package ua.com.fielden.platform.reflection.asm.impl;

import static ua.com.fielden.platform.reflection.asm.impl.TypeMaker.GET_ORIG_TYPE_METHOD_NAME;
import static ua.com.fielden.platform.utils.Pair.pair;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;

import net.bytebuddy.dynamic.loading.InjectionClassLoader;
import ua.com.fielden.platform.classloader.TgSystemClassLoader;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.utils.Pair;

/**
 * A class loader for dynamically constructed or modified entity types.
 * <p>
 * All created types should be loaded by the same instance as used for type modification.
 * The role of {@link InjectionClassLoader} as a parent type is to allow this class loader to be used by ByteBuddy for injecting new types.
 * <p>
 * <b><i>Note:</i></b> This class is NOT thread safe!!! Nor should it be!
 *
 * @author TG Team
 */
public class DynamicEntityClassLoader extends InjectionClassLoader {

    private final TgSystemClassLoader parent;
    
    public static DynamicEntityClassLoader getInstance(final ClassLoader parent) {
        if (parent instanceof TgSystemClassLoader) {
            return new DynamicEntityClassLoader(parent);
        }
        
        throw new IllegalArgumentException("The parent class loader can only be of type TgSystemClassLoader.");
    }
    
    private DynamicEntityClassLoader(final ClassLoader parent) {
        super(parent, /*sealed*/ false);
        this.parent = (TgSystemClassLoader) parent;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Map<String, Class<?>> doDefineClasses(final Map<String, byte[]> typeDefinitions) throws ClassNotFoundException {
        return typeDefinitions.entrySet().stream().collect(Collectors.toMap(
                Map.Entry::getKey, 
                entry -> doDefineClass(entry.getKey(), entry.getValue())));
    }
    
    protected Class<?> doDefineClass(final String name, final byte[] bytes) {
        return getTypeByNameFromCache(name).map(Pair::getKey).orElseGet(() -> {
            // the class hasn't been defined, so load it and cache for later reuse
            final Class klass = defineClass(name, bytes, 0, bytes.length);
            registerClass(pair(klass, bytes));
            return klass;
        });
    }
    
    @Override
    public InputStream getResourceAsStream(final String name) {
        // name is a resource name so convert it to binary form
        return getTypeByNameFromCache(toBinaryName(name))
                .map(pair -> (InputStream) new ByteArrayInputStream(pair.getValue()))
                .orElseGet(() -> super.getResourceAsStream(name));
    }
    
    /**
     * Tries to retrieve a type with <code>typeName</code> from cache.
     * @param typeName - fully-qualified name in binary form (i.e. the result of {@link Class#getName()}.
     * @return
     */
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
     * @param origType
     * @return
     * @throws ClassNotFoundException
     */
    public <T> TypeMaker<T> startModification(final Class<T> origType) throws ClassNotFoundException {
        return new TypeMaker<T>(this, origType).startModification();
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
     * Returns an original type for the specified one (the type from which <code>type</code> was generated). 
     * If <code>type</code> is not a generated one - simply returns it.
     *
     * @param type
     * @return
     */
    @SuppressWarnings("unchecked")
    public static <T extends AbstractEntity<?>> Class<T> getOriginalType(final Class<?> type) {
        if (isGenerated(type)) {
            try {
                final Method getOrigType = type.getMethod(GET_ORIG_TYPE_METHOD_NAME);
                return (Class<T>) getOrigType.invoke(null);
            } catch (final RuntimeException ex) {
                throw ex;
            } catch (final Exception ex) {
                throw new IllegalStateException(ex);
            }
        } else {
            return (Class<T>) type;
        }
    }

    public byte[] getCachedByteArray(final String name) {
        return getTypeByNameFromCache(name).map(Pair::getValue).orElse(null);
    }
    
    /**
     * Converts a resource name of a java class to its binary form.
     * @param resourceName
     * @return binary name of a class or null if <code>resourceName</code> doesn't represent a java class.
     */
    private static String toBinaryName(final String resourceName) {
        return resourceName.endsWith(".class") ? StringUtils.substringBeforeLast(resourceName, ".class").replace('/', '.') : null;
    }
}
