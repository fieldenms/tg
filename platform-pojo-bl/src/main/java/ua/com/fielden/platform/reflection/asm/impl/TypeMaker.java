package ua.com.fielden.platform.reflection.asm.impl;

import java.io.File;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;

import net.bytebuddy.ByteBuddy;
import net.bytebuddy.description.modifier.FieldManifestation;
import net.bytebuddy.description.modifier.Ownership;
import net.bytebuddy.description.modifier.ParameterManifestation;
import net.bytebuddy.description.modifier.Visibility;
import net.bytebuddy.dynamic.DynamicType;
import net.bytebuddy.dynamic.TargetType;
import net.bytebuddy.dynamic.DynamicType.Unloaded;
import net.bytebuddy.implementation.FieldAccessor;
import net.bytebuddy.implementation.FixedValue;
import net.bytebuddy.pool.TypePool;
import ua.com.fielden.platform.entity.annotation.Generated;
import ua.com.fielden.platform.entity.annotation.Observable;
import ua.com.fielden.platform.entity.annotation.factory.ObservableAnnotation;
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

    private static final Generated GENERATED_ANNOTATION = new Generated() {
        @Override
        public Class<Generated> annotationType() {
            return Generated.class;
        }
    };
    private static final String NEW_SUPERTYPE_NAME_IS_NULL_OR_EMPTY = "New supertype name is 'null' or empty.";
    private static final String CURRENT_BUILDER_IS_NOT_SPECIFIED = "Current builder is not specified.";
    public static final String GET_ORIG_TYPE_METHOD_NAME = "_GET_ORIG_TYPE_METHOD_";
    private final DynamicEntityClassLoader cl;
    private byte[] currentType; // TODO remove
    private String currentName; // TODO remove
    private final Class<T> origType;
    private DynamicType.Builder<T> builder;
    private boolean nameModified = false;

    public TypeMaker(final DynamicEntityClassLoader loader, final Class<T> origType) {
        this.cl = loader;
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
     * Adds the specified properties to the type. 
     * Those properties that conflict with the existing ones are discarded (i.e. old properties are not overwritten).
     * Also, duplicate properties are eliminated.
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
        final List<String> existingProperties = Arrays.asList(origType.getDeclaredFields()).stream().map(Field::getName).toList();
        final List<NewProperty> propertiesToAdd = Arrays.asList(properties).stream()
                .filter(prop -> !existingProperties.contains(prop.name))
                .toList();
        propertiesToAdd.forEach(prop -> {
            builder = builder.defineField(prop.name, prop.type, Visibility.PRIVATE)
                    // annotations
                    .annotateField(prop.annotations)
                    .annotateField(prop.titleAnnotation(), GENERATED_ANNOTATION)
                    // getter
                    .defineMethod("get" + StringUtils.capitalize(prop.name), prop.type, Visibility.PUBLIC)
                    .intercept(FieldAccessor.ofField(prop.name))
                    // setter
                    .defineMethod("set" + StringUtils.capitalize(prop.name), TargetType.DESCRIPTION, Visibility.PUBLIC)
                    .withParameter(prop.type, "obj", ParameterManifestation.FINAL)
                    .intercept(FieldAccessor.ofField(prop.name).setsArgumentAt(0).andThen(FixedValue.self()))
                    .annotateMethod(ObservableAnnotation.newInstance());
        });

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
    * @param newTypeName - must be fully-qualified in a binary format (e.g. <code>foo.Bar</code> )
    * @return
    */
   public TypeMaker<T> modifyTypeName(final String newTypeName) {
       if (StringUtils.isEmpty(newTypeName)) {
           throw new IllegalStateException("New type name is 'null' or empty.");
       }
       if (builder == null) {
           throw new IllegalStateException(CURRENT_BUILDER_IS_NOT_SPECIFIED);
       }
       nameModified = true;
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
    * Sets the supertype's name to the name of the original type.
    * A shortcut for {@link #modifySupertypeName(String)} where the argument is the original type's name.
    * @return
    */
   public TypeMaker<T> extendOriginalType() {
      modifySupertypeName(origType.getName());
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
   
   /**
    * Finalizes type modification and loads the resulting class.
    * <p>
    * If the type name wasn't modified prior to this stage, then it is performed here according to {@link DynamicTypeNamingService}.
    * 
    * @return a loaded class representing the modified type
    */
   public Class<?> endModification() {
       if (!DynamicEntityClassLoader.isGenerated(origType)) {
           recordOrigType();
       }
       
       // provide a TypePool that uses the class loader of the original type
       // if origType is a dynamic one, then this will be DynamicEntityClassLoader, which will be able to locate origType
       return builder.make(TypePool.ClassLoading.of(origType.getClassLoader()))
               // provide DynamicEntityClassLoader to be injected with the new dynamic type
               // this allows us to use a single class loader for all dynamically created types,
               // instead of making ByteBuddy create a separate class loader for each
               .load(cl)
               .getLoaded();
       if (!nameModified) {
           modifyTypeName(DynamicTypeNamingService.nextTypeName(origType.getName()));
       }
   }
   
    private boolean skipAdaptation(final String name) {
        return name.startsWith("java.");
    }

}
