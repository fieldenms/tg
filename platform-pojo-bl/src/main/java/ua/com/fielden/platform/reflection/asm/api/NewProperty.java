package ua.com.fielden.platform.reflection.asm.api;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import ua.com.fielden.platform.entity.annotation.IsProperty;
import ua.com.fielden.platform.entity.annotation.Title;
import ua.com.fielden.platform.entity.annotation.factory.IsPropertyAnnotation;
import ua.com.fielden.platform.entity.meta.PropertyDescriptor;
import ua.com.fielden.platform.reflection.PropertyTypeDeterminator;

/**
 * A convenient abstraction for representing data needed for dynamic construction of properties.
 * 
 * @author TG Team
 * 
 */
public final class NewProperty {
    public static final IsProperty DEFAULT_IS_PROPERTY_ANNOTATION = new IsPropertyAnnotation().newInstance();

    // TODO make fields private
    public final String name;
    public final Class<?> type; // TODO rename to rawType
    private final Type[] typeArguments;
    @Deprecated
    public final boolean changeSignature; // TODO remove
    public final String title;
    public final String desc;
    public final List<Annotation> annotations = new ArrayList<Annotation>();
    private Type genericType; // lazy access
    public final boolean deprecated;
    
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

    // TODO remove
    @Deprecated
    public static NewProperty changeType(final String name, final Class<?> type) {
        return new NewProperty(name, type, false, null, null, new Annotation[0]);
    }

    // TODO remove
    @Deprecated
    public static NewProperty changeTypeSignature(final String name, final Class<?> type) {
        return new NewProperty(name, type, true, null, null, new Annotation[0]);
    }

    // TODO obsolete constructor
    @Deprecated
    public NewProperty(final String name, final Class<?> type, final boolean changeSignature, final String title, final String desc, 
            final Annotation... annotations) 
    {
        this.name = name;
        this.type = type;
        this.typeArguments = new Type[0];
        this.changeSignature = changeSignature;
        this.title = title;
        this.desc = desc;
        this.annotations.addAll(Arrays.asList(annotations));
        addAnnotation(DEFAULT_IS_PROPERTY_ANNOTATION); // add in case it wasn't provided
        this.deprecated = true;
    }
    
    /**
     * Creates a new property representation with a raw type and type arguments.
     * <p>
     * <i>Note:</i> <code>annotations</code> parameter - {@link Title} <b>should be</b> omitted and later accessed with {@link #titleAnnotation()};
     *  {@link IsProperty} <b>can be</b> omitted.
     * @param name simple name of the property
     * @param rawType rawType of the property
     * @param typeArguments actual typeArguments if any, otherwise pass <code>null</code> or use another constructor
     * @param title property title as in {@link Title} annotation
     * @param desc property description as in {@link Title} annotation
     * @param annotations annotations directly present on this property (see note)
     */
    public NewProperty(final String name, final Class<?> rawType, final Type[] typeArguments, final String title, final String desc, 
            final Annotation... annotations) 
    {
        this.name = name;
        this.type = rawType;
        this.typeArguments = typeArguments;
        this.changeSignature = false;
        this.title = title;
        this.desc = desc;
        this.annotations.addAll(Arrays.asList(annotations));
        addAnnotation(DEFAULT_IS_PROPERTY_ANNOTATION); // add in case it wasn't provided
        this.deprecated = false;
    }

    /**
     * Creates a new property representation with a raw type.
     * <p>
     * <i>Note:</i> <code>annotations</code> parameter - {@link Title} <b>should be</b> omitted and later accessed with {@link #titleAnnotation()};
     *  {@link IsProperty} <b>can be</b> omitted.
     * @param name simple name of the property
     * @param rawType raw type of the property
     * @param title property title as in {@link Title} annotation
     * @param desc property description as in {@link Title} annotation
     * @param annotations annotations directly present on this property (see note)
     */
    public NewProperty(final String name, final Class<?> rawType, final String title, final String desc, final Annotation... annotations) {
        this(name, rawType, new Type[0], title, desc, annotations);
    }

    /**
     * Creates a new property representation with a parameterized type.
     * <p>
     * <i>Note:</i> <code>type</code> - the raw type ({@link ParameterizedType#getRawType()}) must be an instance of {@link Class}, otherwise a runtime exception is thrown.
     * <p>
     * <i>Note:</i> <code>annotations</code> - {@link Title} <b>should be</b> omitted and later accessed with {@link #titleAnnotation()};
     *  {@link IsProperty} <b>can be</b> omitted.
     * @param name simple name of the property
     * @param type parameterized type of the property
     * @param title property title as in {@link Title} annotation
     * @param desc property description as in {@link Title} annotation
     * @param annotations annotations directly present on this property (see note)
     */
    public NewProperty(final String name, final ParameterizedType type, final String title, final String desc, final Annotation... annotations) {
        this(name, PropertyTypeDeterminator.classFrom(type.getRawType()), type.getActualTypeArguments(), title, desc, annotations);
    }
    
    public boolean hasTypeArguments() {
        return typeArguments != null && typeArguments.length > 0;
    }

    /**
     * Returns a new instance that is similar to this one, but with a changed raw type.
     * @param rawType
     * @return
     */
    public NewProperty changeType(final Class<?> rawType) {
        return new NewProperty(name, rawType, title, desc, annotations.toArray(Annotation[]::new));
    }

    /**
     * Returns a new instance that is similar to this one, but with its raw type and type arguments changed.
     * <p>
     * If <code>rawType</code> represents a property descriptor or a collection, then {@link IsProperty#value()} is replaced by <code>typeArguments[0]</code> if present, otherwise by {@link Void}.
     * @param rawType
     * @param typeArguments
     * @return
     */
    public NewProperty changeType(final Class<?> rawType, final Type[] typeArguments) {
        return changeType(new ParameterizedType() {
            @Override
            public Type getRawType() { return rawType; }
            @Override
            public Type getOwnerType() { return rawType.getDeclaringClass(); }
            @Override
            public Type[] getActualTypeArguments() { return typeArguments; }
        });
    }

    /**
     * Returns a new instance that is similar to this one, but with its raw type and type arguments changed.
     * <p>
     * If the raw type represents a property descriptor or a collection, then {@link IsProperty#value()} is replaced by <code>typeArguments[0]</code> if present, otherwise by {@link Void}.
     * @param type
     * @return
     */
    public NewProperty changeType(final ParameterizedType type) {
        final Type[] newTypeArgs = type.getActualTypeArguments();
        // determine new raw type
        final Class<?> newRawClass = PropertyTypeDeterminator.classFrom(type.getRawType());

        final Class<?> newIsPropertyValue;

        // we have to set value() of @IsProperty to the 1st type argument if type arguments are present AND 
        // the raw type is either a property descriptor or a collection
        if (newTypeArgs != null && newTypeArgs.length > 0 &&
                (PropertyDescriptor.class.isAssignableFrom(newRawClass) || Collection.class.isAssignableFrom(newRawClass))) 
        {
            newIsPropertyValue = PropertyTypeDeterminator.classFrom(newTypeArgs[0]);
        }
        // otherwise replace the previous value() by Void
        else {
            newIsPropertyValue = Void.class;
        }

        // copy IsProperty and replace its value(), retain all other annotations
        // TODO create a separate accessor for @IsProperty for better performance
        final Annotation[] changedAnnotations = annotations.stream().map(annot -> {
            if (annot.annotationType().equals(IsProperty.class)) {
                return IsPropertyAnnotation.from((IsProperty) annot).value(newIsPropertyValue).newInstance();
            }
            return annot;
        }).toArray(Annotation[]::new);

        return new NewProperty(name, newRawClass, newTypeArgs, title, desc, changedAnnotations);
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
    
    public boolean hasTitle() {
        return title != null;
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

    public String getName() {
        return name;
    }

    public Class<?> getRawType() {
        return type;
    }

    public Type[] getTypeArguments() {
        return typeArguments;
    }

    public List<Annotation> getAnnotations() {
        return annotations;
    }
}
