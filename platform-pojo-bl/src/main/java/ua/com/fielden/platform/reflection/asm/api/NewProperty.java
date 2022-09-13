package ua.com.fielden.platform.reflection.asm.api;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.StringJoiner;
import java.util.stream.Collectors;

import ua.com.fielden.platform.entity.annotation.IsProperty;
import ua.com.fielden.platform.entity.annotation.Title;
import ua.com.fielden.platform.entity.annotation.factory.IsPropertyAnnotation;
import ua.com.fielden.platform.entity.meta.PropertyDescriptor;
import ua.com.fielden.platform.reflection.Finder;
import ua.com.fielden.platform.reflection.PropertyTypeDeterminator;
import ua.com.fielden.platform.reflection.asm.exceptions.NewPropertyException;

/**
 * A convenient abstraction for representing data needed for dynamic construction of properties.
 * 
 * @author TG Team
 * 
 */
public final class NewProperty {
    public static final IsProperty DEFAULT_IS_PROPERTY_ANNOTATION = new IsPropertyAnnotation().newInstance();

    // TODO make fields private
    public String name;
    public Class<?> type; // TODO rename to rawType
    private List<Type> typeArguments = new ArrayList<Type>();
    @Deprecated public final boolean changeSignature; // TODO remove
    public String title;
    public String desc;
    // TODO use ordered set
    public final List<Annotation> annotations = new ArrayList<Annotation>();
    private Type genericType; // lazy access
    public final boolean deprecated;
    
    /**
     * Constructs a property from the given <code>field</code>.
     * @param field
     * @return
     */
    public static NewProperty fromField(final Field field) {
        final Title titleAnnot = field.getDeclaredAnnotation(Title.class);
        final String title = titleAnnot == null ? null : titleAnnot.value();
        final String desc = titleAnnot == null ? null : titleAnnot.desc();
        // exclude Title annotation from the list of declared annotations
        // instead we will pass its value() and desc() attributes
        final Annotation[] restAnnotations = titleAnnot == null ? 
                field.getDeclaredAnnotations() : 
                Arrays.stream(field.getDeclaredAnnotations()).filter(annot -> !annot.equals(titleAnnot)).toArray(Annotation[]::new);

        final Type genericType = field.getGenericType();
        // explicit check is needed to be able to store the information about type arguments
        if (ParameterizedType.class.isInstance(genericType)) {
            return new NewProperty(field.getName(), (ParameterizedType) genericType, title, desc, restAnnotations);
        }
        // only raw type information is available
        return new NewProperty(field.getName(), PropertyTypeDeterminator.classFrom(genericType), title, desc, restAnnotations);
    }

    /**
     * Constructs a property from a field with <code>name</code> found in <code>type</code>'s hierarchy.
     * @param type
     * @param name
     * @return
     */
    public static NewProperty fromField(final Class<?> type, final String name) {
        return NewProperty.fromField(Finder.getFieldByName(type, name));
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
        if (typeArguments.length > 0) this.typeArguments.addAll(Arrays.asList(typeArguments));
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
        return typeArguments != null && !typeArguments.isEmpty();
    }
    
    public boolean isCollectional() {
        return Collection.class.isAssignableFrom(type);
    }
    
    public boolean isPropertyDescriptor() {
        return PropertyDescriptor.class.isAssignableFrom(type);
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
     * Similar to {@link Field#getGenericType()} determines the generic type of this property taking into account its raw type and type arguments.
     * <p>
     * There is a special case when the raw type is either {@link Collection} or {@link PropertyDescriptor}, 
     * which allows the property type to be a raw one, while the type argument information is represented by the value of 
     * {@link IsProperty} annotation. For example:
     * <pre>
     * &#64;IsProperty(Vehicle.class)
     * private List vehicles;
     * </pre>
     * In such case the value of {@link IsProperty} is recorded in the form of type arguments. However, if a type argument is present,
     * then it's prioritized over the value of  {@link IsProperty}.
     * Refer to {@link #getGenericTypeAsDeclared()} to get a precise representation (which would simply return a raw {@link List}).
     * The same rules apply to {@link PropertyDescriptor} which also accepts a single type argument.
     * <p>
     * <i>Note:</i> This method tries to determine the correct generic type on every call, so use it sparingly.
     * 
     * @return the generic type of this property (either {@link Class} or {@link ParameterizedType})
     */
    public Type genericType() {
        if (type == null) return null;

        final List<Type> typeArgs;
        // prioritize the actual type arguments
        if (hasTypeArguments()) {
            typeArgs = typeArguments;
        }
        // special case? look at the value of @IsProperty
        else if (isCollectional() || isPropertyDescriptor()) {
            final IsProperty adIsProperty = (IsProperty) getAnnotationByType(IsProperty.class);
            final Class<?> value = adIsProperty == null ? null : adIsProperty.value();
            typeArgs = (value == null || value.equals(Void.class)) ? null : List.of(adIsProperty.value());
        }
        else {
            typeArgs = null;
        }
        // found any type arguments?
        genericType = typeArgs == null ? type : newParameterizedType(type, typeArgs.toArray(Type[]::new));

        return genericType;
    }
    
    /**
     * Returns the generic type of this property exactly like it's declared. As opposed to {@link #genericType()}, 
     * this method does not take the value of {@link IsProperty} into account.
     * <p>
     * Should be used when a precise generic type is needed.
     * 
     * @return declared generic type of this property (either {@link Class} or {@link ParameterizedType})
     */
    public Type genericTypeAsDeclared() {
        if (type == null) return null;

        return !hasTypeArguments() ? type : newParameterizedType(type, typeArguments.toArray(Type[]::new));
    }

    public String getName() {
        return name;
    }
    
    public NewProperty setName(final String name) {
        this.name = name;
        return this;
    }

    public Class<?> getRawType() {
        return type;
    }

    public NewProperty setRawType(final Class<?> rawType) {
        this.type = rawType;
        return this;
    }

    public List<Type> getTypeArguments() {
        return typeArguments;
    }

    /**
     * Updates type arguments of this instance.
     * <p>
     * If the raw type represents {@link PropertyDescriptor} or {@link Collection}, then {@link IsProperty#value()} is updated by
     * the first type argument. If no type arguments are present, then for {@link Collection} {@link Object} is chosen, but for
     * {@link PropertyDescriptor} a runtime exception is thrown.
     * <p>
     * For other types, {@link IsProperty#value()} is unchanged.
     */
    public NewProperty setTypeArguments(final List<Type> typeArguments) {
        this.typeArguments.clear();
        this.typeArguments.addAll(typeArguments);
        updateIsProperty();
        return this;
    }
    
    /**
     * Updates type arguments of this instance.
     * <p>
     * Might also update {@link IsProperty} - see {@link #setTypeArguments(List)}.
     */
    public NewProperty setTypeArguments(final Type... typeArguments) {
        return setTypeArguments(Arrays.asList(typeArguments));
    }

    public List<Annotation> getAnnotations() {
        return annotations;
    }

    public NewProperty setAnnotations(final List<Annotation> annotations) {
        this.annotations.clear();
        annotations.forEach(this::addAnnotation);
        return this;
    }

    public NewProperty setAnnotations(final Annotation... annotations) {
        return setAnnotations(Arrays.asList(annotations));
    }

    /**
     * Modifies this instance by changing its raw type and type arguments.
     * <p>
     * Might also update {@link IsProperty} - see {@link #setType(ParameterizedType)}.
     * @param rawType
     * @param typeArguments
     * @return
     */
    public NewProperty setType(final Class<?> rawType, final Type[] typeArguments) {
        return setType(newParameterizedType(rawType, typeArguments));
    }

    /**
     * Modifies this instance by changing its raw type and type arguments that are derived from <code>type</code>.
     * <p>
     * If the raw type represents {@link PropertyDescriptor} or {@link Collection}, then {@link IsProperty#value()} is updated by
     * the first type argument. If no type arguments are present, then for {@link Collection} {@link Object} is chosen, but for
     * {@link PropertyDescriptor} a runtime exception is thrown.
     * <p>
     * For other types, {@link IsProperty#value()} is unchanged.
     * @param type
     * @return
     */
    public NewProperty setType(final ParameterizedType type) {
        typeArguments = Arrays.asList(type.getActualTypeArguments());
        // determine new raw type
        this.type = PropertyTypeDeterminator.classFrom(type.getRawType());
        // update @IsProperty
        updateIsProperty();

        return this;
    }
    
    /**
     * Returns a newly created copy of this instance. However, the annotation instances are not copied, which means that you shouldn't modify them.
     * @return
     */
    public NewProperty copy() {
        return new NewProperty(name, type, typeArguments.toArray(Type[]::new), title, desc, annotations.toArray(Annotation[]::new));
    }
    
    /**
     * Returns a string representation of this property in its fullest form (with all annotations included).
     * For shorter representations see {@link #toString(boolean)} and {@link #toString(Class...)}.
     */
    @Override
    public String toString() {
        return toString(true);
    }
    
    /**
     * Returns a string representation of this property without any annotations.
     * @param ommitAnnotations - controls whether to omit annotations
     * @return
     */
    public String toString(final boolean ommitAnnotations) {
        final StringBuilder strBuilder = new StringBuilder();
        if (!ommitAnnotations) {
            strBuilder.append(annotations.stream().map(Annotation::toString).collect(Collectors.joining(" ")));
            strBuilder.append(' ');
        }
        return strBuilder.append(String.format("%s %s", genericTypeAsDeclared().getTypeName(), name)).toString();
    }
    
    /**
     * Returns a string representation of this property including the provided annotations only.
     * @param withAnnotations - annotation types to include
     * @return
     */
    public String toString(final Class<?>... withAnnotations) {
        final Set<Class<?>> withAnnotationsSet = new HashSet<>(Arrays.asList(withAnnotations));

        final StringBuilder strBuilder = new StringBuilder();
        strBuilder.append(annotations.stream()
                .filter(annot -> withAnnotationsSet.contains(annot.annotationType()))
                .map(Annotation::toString)
                .collect(Collectors.joining(" ")));
        if (!strBuilder.isEmpty()) strBuilder.append(' ');
        return strBuilder.append(String.format("%s %s", genericTypeAsDeclared().getTypeName(), name)).toString();
    }

    private void updateIsProperty() {
        final Class<?> newValue = determineIsPropertyValue();
        if (newValue == null) return;

        // copy IsProperty and replace its value(), retain all other annotations
        // TODO create a separate accessor for @IsProperty for better performance
        setAnnotations(annotations.stream().map(annot -> {
            if (annot.annotationType().equals(IsProperty.class)) {
                return IsPropertyAnnotation.from((IsProperty) annot).value(newValue).newInstance();
            }
            return annot;
        }).toList());
    }

    private Class<?> determineIsPropertyValue() {
        if (isCollectional()) {
            // collectional property and empty type arguments -> set IsProperty.value() to Object
            if (typeArguments == null || typeArguments.isEmpty()) {
                return Object.class;
            }
            else {
                return PropertyTypeDeterminator.classFrom(typeArguments.get(0));
            }
        }
        else if (isPropertyDescriptor()) {
            // property descriptor MUST BE parameterized
            if (typeArguments == null || typeArguments.isEmpty()) {
                throw new NewPropertyException("PropertyDescriptor must be parameterized, got empty type arguments instead.");
            }
            else {
                return PropertyTypeDeterminator.classFrom(typeArguments.get(0));
            }
        }
        else {
            final Annotation isProperty = getAnnotationByType(IsProperty.class);
            if (isProperty == null) return null;

            return ((IsProperty) isProperty).value();
        }
    }
    
    /**
     * Creates a new anonymous class implementing {@link ParameterizedType}.
     * 
     * @param rawType
     * @param typeArguments
     * @return
     */
    private static ParameterizedType newParameterizedType(final Class<?> rawType, final Type... typeArguments) {
        final Type owner = rawType.getDeclaringClass();

        return new ParameterizedType() {
            @Override public Class<?> getRawType() { return rawType; }
            @Override public Type getOwnerType() { return owner; }
            @Override public Type[] getActualTypeArguments() { return typeArguments; }
            @Override
            public String toString() {
                final StringBuilder sb = new StringBuilder();
                if (owner != null) {
                    sb.append(owner.getTypeName());
                    sb.append('$');
                    sb.append(rawType.getSimpleName());
                }
                else {
                    sb.append(rawType.getName());
                }

                final StringJoiner sj = new StringJoiner(", ", "<", ">").setEmptyValue("");
                for (final Type arg : typeArguments) {
                    sj.add(arg.getTypeName());
                }

                return sb.append(sj.toString()).toString();
            }
        };
    }
}
