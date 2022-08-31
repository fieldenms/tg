package ua.com.fielden.platform.reflection.asm.api;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import ua.com.fielden.platform.entity.annotation.IsProperty;
import ua.com.fielden.platform.entity.annotation.Title;
import ua.com.fielden.platform.entity.annotation.factory.IsPropertyAnnotation;

/**
 * A convenient abstraction for representing data needed for dynamic construction of properties.
 * 
 * @author TG Team
 * 
 */
public final class NewProperty {
    public static final IsProperty DEFAULT_IS_PROPERTY_ANNOTATION = new IsPropertyAnnotation().newInstance();

    public final String name;
    public final Class<?> type;
    public final boolean changeSignature;
    public final String title;
    public final String desc;
    public final List<Annotation> annotations = new ArrayList<Annotation>();
    private Type genericType;
    
    public static NewProperty fromField(final Field field) {
        final Title titleAnnot = field.getDeclaredAnnotation(Title.class);
        // exclude Title annotation from the list of declared annotations
        // instead pass its value() and desc() attributes
        final Annotation[] restAnnotations = titleAnnot == null ? 
                field.getDeclaredAnnotations() : 
                Arrays.stream(field.getDeclaredAnnotations()).filter(annot -> !annot.equals(titleAnnot)).toArray(Annotation[]::new);
        return new NewProperty(field.getName(), field.getType(), false,
                titleAnnot == null ? null : titleAnnot.value(),
                titleAnnot == null ? null : titleAnnot.desc(),
                restAnnotations);
    }

    public static NewProperty changeType(final String name, final Class<?> type) {
        return new NewProperty(name, type, false, null, null, new Annotation[0]);
    }

    public static NewProperty changeTypeSignature(final String name, final Class<?> type) {
        return new NewProperty(name, type, true, null, null, new Annotation[0]);
    }

    public NewProperty(final String name, final Class<?> type, final boolean changeSignature, final String title, final String desc, 
            final Annotation... annotations) 
    {
        this.name = name;
        this.type = type;
        this.changeSignature = changeSignature;
        this.title = title;
        this.desc = desc;
        this.annotations.addAll(Arrays.asList(annotations));
        addAnnotation(DEFAULT_IS_PROPERTY_ANNOTATION); // add in case it wasn't provided
    }

    /**
     * Tests whether an annotation is present for a specific annotation type.
     * 
     * @param annotationType
     * @return
     */
    public boolean containsAnnotationDescriptorFor(final Class<? extends Annotation> annotationType) {
        return getAnnotationByType(annotationType) != null;
    }

    /**
     * Returns an annotation for the specified annotation type. Returns <code>null</code> if such annotation is not in the list.
     * 
     * @param annotationType
     * @return
     */
    public Annotation getAnnotationByType(final Class<? extends Annotation> annotationType) {
        return annotations.stream().filter(annot -> annot.annotationType() == annotationType).findAny().orElse(null);
    }

    /**
     * Add the specified annotation to the list of property annotations.
     * 
     * @param annotation
     * @return
     */
    public NewProperty addAnnotation(final Annotation annotation) {
        if (!containsAnnotationDescriptorFor(annotation.annotationType())) {
            annotations.add(annotation);
        }
        return this;
    }
    
    public Title titleAnnotation() {
        return new Title() {
            @Override
            public Class<Title> annotationType() {
                return Title.class;
            }

            @Override
            public String value() {
                return title == null ? "" : title;
            }

            @Override
            public String desc() {
                return desc == null ? "" : desc;
            }
        };
    }
    
    /**
     * Similar to {@link Field#getGenericType()}.
     * <p>
     * {@link NewProperty} representing a collectional property may have a raw type, such as {@link List}, so it's necessary to look at the value of {@link IsProperty} annotation to try to determine the type argument. Otherwise, current property type is returned as is.
     * @return
     */
    public Type getGenericType() {
        if (genericType == null) {
            final IsProperty adIsProperty = (IsProperty) getAnnotationByType(IsProperty.class);
            final Class<?> typeArg = adIsProperty != null ? adIsProperty.value() : null;
            genericType = (typeArg == null || typeArg.equals(Void.class)) ? type : new ParameterizedType() {
                @Override public Type getRawType() { return type; }
                @Override public Type getOwnerType() { return null; } // top-level type
                @Override public Type[] getActualTypeArguments() { return new Type[] {typeArg}; }
            };
        }
        return genericType;
    }
}
