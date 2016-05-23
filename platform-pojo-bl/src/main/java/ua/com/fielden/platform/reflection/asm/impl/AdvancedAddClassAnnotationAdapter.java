package ua.com.fielden.platform.reflection.asm.impl;

import java.lang.annotation.Annotation;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.kohsuke.asm5.AnnotationVisitor;
import org.kohsuke.asm5.ClassVisitor;
import org.kohsuke.asm5.MethodVisitor;
import org.kohsuke.asm5.Opcodes;
import org.kohsuke.asm5.Type;
import org.kohsuke.asm5.commons.RemappingMethodAdapter;
import org.kohsuke.asm5.commons.SimpleRemapper;

import ua.com.fielden.platform.reflection.Reflector;
import ua.com.fielden.platform.utils.Pair;

/**
 * A class adapter designed for adding annotations to a class.
 *
 * @author TG Team
 *
 */
public class AdvancedAddClassAnnotationAdapter extends ClassVisitor implements Opcodes {

    /**
     * Map between annotation type descriptor and the actual annotation instance.
     */
    private final Map<String, Annotation> annotations = new HashMap<>();

    /**
     * Type name that is being put through the adapter.
     */
    private String owner;
    private String enhancedName;

    private final DynamicTypeNamingService namingService;

    public AdvancedAddClassAnnotationAdapter(final ClassVisitor cv, final DynamicTypeNamingService namingService, final Annotation... annotations) {
        super(Opcodes.ASM5, cv);
        this.namingService = namingService;

        for (final Annotation annot : annotations) {
            this.annotations.put(Type.getDescriptor(annot.annotationType()), annot);
        }
    }

    /**
     * {@inheritDoc}
     *
     * Additionally, modifies and records the name of the class currently being traversed.
     */
    @Override
    public synchronized void visit(final int version, final int access, final String name, final String signature, final String superName, final String[] interfaces) {
        owner = name;
        enhancedName = namingService.nextTypeName(name);
        super.visit(version, access, enhancedName, signature, superName, interfaces);
    }

    /**
     * Replaces references to the owner in methods with the enhancedName.
     */
    @Override
    public MethodVisitor visitMethod(final int access, final String name, final String desc, final String signature, final String[] exceptions) {
        final MethodVisitor mv = cv.visitMethod(access, name, fix(desc), fix(signature), exceptions);
        // check if the method is not abstract
        if (mv != null && (access & ACC_ABSTRACT) == 0) {
            return new RemappingMethodAdapter(access, desc, mv, new SimpleRemapper(owner, enhancedName));
        } else {
            return mv;
        }
    }

    /**
     * Ensures that existing in the original class annotations are not replaced with the specified ones.
     */
    @Override
    public AnnotationVisitor visitAnnotation(final String typeDesc, final boolean visibleAtRuntime) {
        if (annotations.containsKey(typeDesc)) {
            annotations.remove(typeDesc);
        }

        return super.visitAnnotation(typeDesc, visibleAtRuntime);
    }

    /**
     * This is where new class annotations are added.
     */
    @Override
    public void visitEnd() {
        for (final Entry<String, Annotation> entry : annotations.entrySet()) {
            addAnnotation(entry.getKey(), entry.getValue(), cv);
        }
        super.visitEnd();
    }

    /**
     * Adds the specified annotation to the class being generated.
     *
     * @param annotationTypeDesc
     * @param annotation
     * @param cv
     */
    private void addAnnotation(final String annotationTypeDesc, final Annotation annotation, final ClassVisitor cv) {
        final AnnotationVisitor av = cv.visitAnnotation(annotationTypeDesc, true);
        processAnnotationParam(av, annotation);
        av.visitEnd();
    }

    private void processAnnotationParam(final AnnotationVisitor av, final Annotation annotation) {
        final List<String> params = Reflector.annotataionParams(annotation.getClass()); // should this not be annotation.annotationType()?

        for (final String name : params) {
            if (!name.equals("equals") && !name.equals("toString") && !name.equals("hashCode")) {
                final Pair<Class<?>, Object> pair = Reflector.getAnnotationParamValue(annotation, name);
                final Class<?> type = pair.getKey();
                final Object value = pair.getValue();
                processValueForAnnotation(av, name, type, value);
            }
        }

    }

    private void processValueForAnnotation(final AnnotationVisitor av, final String name, final Class<?> originalType, final Object value) {
        final Class<?> type = Annotation.class.isAssignableFrom(originalType) && originalType.getName().contains("$") ? originalType.getInterfaces()[0] : originalType;

        if (Enum.class.isAssignableFrom(type)) {
            av.visitEnum(name, Type.getDescriptor(type), value.toString());
        } else if (value instanceof Class) { // if the parameter value is a class then need to use its description
            av.visit(name, Type.getType((Class<?>) value));
        } else if (value instanceof Annotation) {
            final AnnotationVisitor avAnnotation = av.visitAnnotation(name, Type.getDescriptor(type));
            processAnnotationParam(avAnnotation, (Annotation) value);
            avAnnotation.visitEnd();
        } else if (type.isArray()) {
            final Class<?> arrayType = type.getComponentType();
            final Object[] array = (Object[]) value;
            final AnnotationVisitor avArray = av.visitArray(name);
            for (final Object arVal : array) {
                processValueForAnnotation(avArray, null, arrayType, arVal);
            }
            avArray.visitEnd();
        } else if (value != null) {
            av.visit(name, value);
        }
    }

    public String getOwner() {
        return owner;
    }

    public String getEnhancedName() {
        return enhancedName;
    }

    /**
     * Changes all the occurrences of <code>owner<code> with <code>enhancedName</code>.
     */
    private String fix(String signature) {

        if (signature != null) {
            if (signature.indexOf(owner + ";") != -1) {
                signature = signature.replaceAll(Pattern.quote(owner + ";"), Matcher.quoteReplacement(enhancedName + ";"));
            }
        }

        return signature;
    }

}
