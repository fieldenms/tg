package ua.com.fielden.platform.reflection.asm.impl;

import java.lang.annotation.Annotation;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;

import net.bytebuddy.ByteBuddy;
import net.bytebuddy.description.modifier.Ownership;
import net.bytebuddy.description.modifier.Visibility;
import net.bytebuddy.dynamic.DynamicType;
import net.bytebuddy.implementation.FixedValue;
import ua.com.fielden.platform.reflection.asm.api.NewProperty;

/**
 * This class provides an API for modifying types at runtime by means of bytecode manipulation.
 * <p>
 * To use this API start with {@link #startModification()}, then perform any other modifications, and end with {@link #endModification()}
 * which loads the modified type and returns a corresponding {@link Class}.
 * <p>
 * <i>Notes on specific parts of the API</i>:
 * <p>
 * If {@link #modifyTypeName(String)} is not called, then {@link #endModification()} will most likely fail due to a name conflict 
 * with the original type.
 * <p>
 * {@link #modifyTypeName(String)} should be called, if needed, <b>only after all other modifications</b>, in order to guarantee
 * correct renaming of all occurences of the previous type name.
 * 
 * @param <T> The original type, on which the modified type is based on.
 * 
 * @author TG Team
 *
 */
public class TypeMaker<T> {

    private static final String NEW_SUPERTYPE_NAME_IS_NULL_OR_EMPTY = "New supertype name is 'null' or empty.";
    private static final String CURRENT_BUILDER_IS_NOT_SPECIFIED = "Current builder is not specified.";
    public static final String GET_ORIG_TYPE_METHOD_NAME = "_GET_ORIG_TYPE_METHOD_";
    private final DynamicEntityClassLoader cl = null; // TODO remove
    private byte[] currentType; // TODO remove
    private String currentName; // TODO remove
    private final Class<T> origType;
    private DynamicType.Builder<T> builder;

    // TODO remove
    public TypeMaker(final DynamicEntityClassLoader loader, final Class<T> origType) {
        this(origType);
    }

    public TypeMaker(final Class<T> origType) {
        this.origType = origType;
    }
    
    /**
     * Initiates adaptation of the specified by name type. This could be either dynamic or static type (created manually by developer).
     *
     * @param typeName
     * @return
     * @throws ClassNotFoundException
     */
    public TypeMaker<T> startModification() throws ClassNotFoundException {
        if (skipAdaptation(origType.getName())) {
            throw new IllegalArgumentException("Java system classes should not be enhanced.");
        }
        // no need for looking up the specified type in cache,
        // which was useful before, since ASM operates on byte[] directly

        // we want to redefine instead of subclass because:
        // 1) it makes type renaming simpler
        // 2) it is intuitive - we base the new type on the original one (make a bytecode-equivalent copy)
        // to preserve polymorphism with the origType use modifySupertypeName(String)
        builder = new ByteBuddy().redefine(origType);
        
        return this;
    }

    /**
     * Adds the specified properties to the type. The provided properties are checked for conflicts with the type being modified -- only non-conflicting ones are added. Also,
     * duplicate properties are eliminated.
     *
     * @param properties
     * @return
     */
    public TypeMaker<T> addProperties(final NewProperty... properties) {
        if (builder == null) {
            throw new IllegalStateException(CURRENT_BUILDER_IS_NOT_SPECIFIED);
        }

        if (properties == null || properties.length == 0) {
            return this;
        }

        final Map<String, NewProperty> propertiesToAdd = new LinkedHashMap<>();
        for (final NewProperty prop : properties) {
            propertiesToAdd.put(prop.name, prop);
        }

        try {
            final ClassReader cr = new ClassReader(currentType);
            final ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
            final AdvancedAddPropertyAdapter cv = new AdvancedAddPropertyAdapter(cw, propertiesToAdd);
            cr.accept(cv, ClassReader.SKIP_FRAMES);
            currentType = cw.toByteArray();
            currentName = cv.getEnhancedName().replace('/', '.');
        } catch (final Exception e) {
            throw new IllegalStateException(e);
        }

        return this;
    }
    
    /**
    *
    * Adds the specified class level annotation to the class.
    * <p>
    * It is important that these annotation have their target specified as <code>TYPE</code> and retention as <code>RUNTIME</code>. Otherwise, method throws an illegal argument
    * exception.
    *
    * @param annotations
    * @return
    */
   public TypeMaker<T> addClassAnnotations(final Annotation... annotations) {
       if (builder == null) {
           throw new IllegalStateException(CURRENT_BUILDER_IS_NOT_SPECIFIED);
       }

       if (annotations == null || annotations.length == 0) {
           return this;
       }

       // let's validate provided annotations
       for (final Annotation annot : annotations) {
           // check retention policy
           final Retention retention = annot.annotationType().getAnnotation(Retention.class);
           if (retention == null || retention.value() != RetentionPolicy.RUNTIME) {
               throw new IllegalArgumentException(String.format("The provided annotation %s should have runtime retention policy.", annot.annotationType().getSimpleName()));
           }

           // check target
           final Target target = annot.annotationType().getAnnotation(Target.class);
           if (target == null || Arrays.stream(target.value()).filter(t -> t == ElementType.TYPE).count() == 0) {
               throw new IllegalArgumentException(String.format("The provided annotation %s should have 'type' target.", annot.annotationType().getSimpleName()));
           }
       }

       // proceed with type construction
       try {
           final ClassReader cr = new ClassReader(currentType);
           final ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
           final AdvancedAddClassAnnotationAdapter cv = new AdvancedAddClassAnnotationAdapter(cw, annotations);
           cr.accept(cv, ClassReader.SKIP_FRAMES);
           currentType = cw.toByteArray();
           currentName = cv.getEnhancedName().replace('/', '.');
       } catch (final Exception e) {
           throw new IllegalStateException(e);
       }

       return this;
   }

   /**
    * Modifies type's name with the specified <code>newTypeName</code>. 
    * <p>
    * <i><b>NOTE</b></i>: this method should be called, if needed, only after all other modifications, in order to guarantee
    * correct renaming of all occurences.
    * 
    * @param newTypeName - must be fully-qualified in a binary format 
    * (e.g. <code>foo.Bar</code> )
    * @return
    */
   public TypeMaker<T> modifyTypeName(final String newTypeName) {
       if (StringUtils.isEmpty(newTypeName)) {
           throw new IllegalStateException("New type name is 'null' or empty.");
       }
       if (builder == null) {
           throw new IllegalStateException(CURRENT_BUILDER_IS_NOT_SPECIFIED);
       }
       builder = builder.name(newTypeName);
       
       return this;
   }

   /**
    * Modifies the supertype's name with the specified <code>newSupertypeName</code>. 
    * <p>
    * 
    * @param newSupertypeName - must be fully-qualified in a binary format 
    * (e.g. <code>foo.Bar</code> )
    * @return
    */
   public TypeMaker<T> modifySupertypeName(final String newSupertypeName) {
       if (StringUtils.isEmpty(newSupertypeName)) {
           throw new IllegalStateException(NEW_SUPERTYPE_NAME_IS_NULL_OR_EMPTY);
       }
       if (builder == null) {
           throw new IllegalStateException(CURRENT_BUILDER_IS_NOT_SPECIFIED);
       }
       // DynamicType.Builder does not provide supertype modification capabilities
       // so we have to use an ASM wrapper
       builder = builder.visit(AdvancedChangeSupertypeAdapter.asAsmVisitorWrapper(newSupertypeName));
       
       return this;
   }

   /**
    * Modifies type's properties with the specified information.
    *
    * @param propertyReplacements
    * @return
    */
   public TypeMaker<T> modifyProperties(final NewProperty... propertyReplacements) {
       if (builder == null) {
           throw new IllegalStateException(CURRENT_BUILDER_IS_NOT_SPECIFIED);
       }

       if (propertyReplacements == null || propertyReplacements.length == 0) {
           return this;
       }

       final Map<String, NewProperty> propertiesToAdapt = new HashMap<>();
       for (final NewProperty prop : propertyReplacements) {
           propertiesToAdapt.put(prop.name, prop);
       }

       try {
           final ClassReader cr = new ClassReader(currentType);
           final ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
           final AdvancedModifyPropertyAdapter cv = new AdvancedModifyPropertyAdapter(cw, propertiesToAdapt);
           cr.accept(cv, ClassReader.SKIP_FRAMES);
           currentType = cw.toByteArray();
           currentName = cv.getEnhancedName().replace('/', '.');
       } catch (final Exception e) {
           throw new IllegalStateException(e);
       }

       return this;
   }

   /**
    * Generates code to capture the original type.
    */
   private void recordOrigType() {
       builder = builder.defineMethod(GET_ORIG_TYPE_METHOD_NAME, origType.getClass(), Visibility.PUBLIC, Ownership.STATIC)
               .intercept(FixedValue.value(origType));
   }
   
   public Class<?> endModification() {
       if (!DynamicEntityClassLoader.isGenerated(origType)) {
           recordOrigType();
       }
       
       return builder.make().load(origType.getClassLoader()).getLoaded();
//        more like cache class (in the parent class loader)
//       cl.registerClass(new Pair<Class<?>, byte[]>(klass, currentType));
   }
   
    private boolean skipAdaptation(final String name) {
        return name.startsWith("java.");
    }

}
