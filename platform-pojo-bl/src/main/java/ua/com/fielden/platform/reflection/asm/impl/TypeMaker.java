package ua.com.fielden.platform.reflection.asm.impl;

import java.io.InputStream;
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
import org.kohsuke.asm5.ClassReader;
import org.kohsuke.asm5.ClassWriter;

import ua.com.fielden.platform.reflection.asm.api.NewProperty;
import ua.com.fielden.platform.utils.Pair;

/**
 * A class that is responsible for creation for a single new class based on the provided API.
 * 
 * @author TG Team
 *
 */
public class TypeMaker {

    private static final String NEW_SUPERTYPE_NAME_IS_NULL_OR_EMPTY = "New supertype name is 'null' or empty.";
    private static final String CURRENT_TYPE_OR_NAME_ARE_NOT_SPECIFIED = "Current type or name are not specified.";
    public static final String GET_ORIG_TYPE_METHOD_NAME = "_GET_ORIG_TYPE_METHOD_";
    private final DynamicEntityClassLoader cl;
    private byte[] currentType;
    private String currentName;
    private final Class<?> origType;

    public TypeMaker(final DynamicEntityClassLoader loader, final Class<?> origType) {
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
    public TypeMaker startModification() throws ClassNotFoundException {
        final String typeName = origType.getName();
        if (skipAdaptation(typeName)) {
            throw new IllegalArgumentException("Java system classes should not be enhanced.");
        }
        // try loading the specified type by either actually loading from disk or finding it in cache
        if (cl.getTypeByNameFromCache(typeName).isPresent()) {
            currentType = cl.getCachedByteArray(typeName);
            currentName = typeName;
        } else {
            final String resource = typeName.replace('.', '/') + ".class";
            try (final InputStream is = cl.getResourceAsStream(resource)) {
                final ClassReader cr = new ClassReader(is);
                final ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
                final DoNothingAdapter cv = new DoNothingAdapter(cw);
                cr.accept(cv, ClassReader.SKIP_FRAMES);
                currentType = cw.toByteArray();
                currentName = typeName;
            } catch (final Exception e) {
                throw new ClassNotFoundException(typeName, e);
            }
        }
        
        return this;
    }

    /**
     * Adds the specified properties to the type. The provided properties are checked for conflicts with the type being modified -- only non-conflicting ones are added. Also,
     * duplicate properties are eliminated.
     *
     * @param properties
     * @return
     */
    public TypeMaker addProperties(final NewProperty... properties) {
        if (currentType == null || currentName == null) {
            throw new IllegalStateException(CURRENT_TYPE_OR_NAME_ARE_NOT_SPECIFIED);
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
   public TypeMaker addClassAnnotations(final Annotation... annotations) {
       if (currentType == null || currentName == null) {
           throw new IllegalStateException(CURRENT_TYPE_OR_NAME_ARE_NOT_SPECIFIED);
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
    * Modifies type's name with the specified <code>newTypeName</code>. Note that, if type name is needed to be changed, it should be made after all other modifications
    * (properties adding / adapting etc.).
    *
    * @param newTypeName
    * @return
    */
   public TypeMaker modifyTypeName(final String newTypeName) {
       if (StringUtils.isEmpty(newTypeName)) {
           throw new IllegalStateException("New type name is 'null' or empty.");
       }
       if (currentType == null || currentName == null) {
           throw new IllegalStateException(CURRENT_TYPE_OR_NAME_ARE_NOT_SPECIFIED);
       }
       try {
           final ClassReader cr = new ClassReader(currentType);
           final ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES); //Opcodes..ASM5
           final AdvancedChangeNameAdapter cv = new AdvancedChangeNameAdapter(cw, currentName.replace('.', '/'), newTypeName.replace('.', '/')); //
           cr.accept(cv, ClassReader.SKIP_FRAMES); //  EXPAND_FRAMES
           currentType = cw.toByteArray();
           currentName = cv.getNewTypeName().replace('/', '.');
       } catch (final Exception e) {
           throw new IllegalStateException(e);
       }
       return this;
   }
    
    
   public TypeMaker modifySupertypeName(final String newSupertypeName) {
       if (StringUtils.isEmpty(newSupertypeName)) {
           throw new IllegalStateException(NEW_SUPERTYPE_NAME_IS_NULL_OR_EMPTY);
       }
       if (currentType == null || currentName == null) {
           throw new IllegalStateException(CURRENT_TYPE_OR_NAME_ARE_NOT_SPECIFIED);
       }
       try {
           final ClassReader cr = new ClassReader(currentType);
           final ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
           final AdvancedChangeSupertypeAdapter cv = new AdvancedChangeSupertypeAdapter(newSupertypeName.replace('.', '/'), cw); //
           cr.accept(cv, ClassReader.SKIP_FRAMES); //ClassReader.EXPAND_FRAMES
           currentType = cw.toByteArray();
       } catch (final Exception e) {
           throw new IllegalStateException(e);
       }
       return this;
   }

   /**
    * Modifies type's properties with the specified information.
    *
    * @param propertyReplacements
    * @return
    */
   public TypeMaker modifyProperties(final NewProperty... propertyReplacements) {
       if (currentType == null || currentName == null) {
           throw new IllegalStateException(CURRENT_TYPE_OR_NAME_ARE_NOT_SPECIFIED);
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
       try {
           final ClassReader cr = new ClassReader(currentType);
           final ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
           final AdvancedRecordOriginalTypeAdapter cv = new AdvancedRecordOriginalTypeAdapter(cw, origType);
           cr.accept(cv, ClassReader.SKIP_FRAMES);
           currentType = cw.toByteArray();
       } catch (final Exception e) {
           throw new IllegalStateException(e);
       }

   }
   
   public Class<?> endModification() {
       if (!DynamicEntityClassLoader.isGenerated(origType)) {
           recordOrigType();
       }
       
       final Class<?> klass = cl.defineType(currentName, currentType, 0, currentType.length);
       cl.registerClass(new Pair<Class<?>, byte[]>(klass, currentType));

       currentType = null;
       currentName = null;
       return klass;
   }
   
    private boolean skipAdaptation(final String name) {
        return name.startsWith("java.");
    }

}
