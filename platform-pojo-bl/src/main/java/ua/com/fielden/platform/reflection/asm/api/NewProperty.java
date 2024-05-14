package ua.com.fielden.platform.reflection.asm.api;

import static java.util.stream.Collectors.joining;
import static ua.com.fielden.platform.reflection.Reflector.newParameterizedType;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.StringJoiner;
import java.util.function.Supplier;

import org.apache.commons.lang3.StringUtils;

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
 * @param <T> The raw type of this property. This type parameter provides a compile-time safe way to initialize property's value using
 * {@link #setValueSupplier(Supplier)}.
 * <p>
 * If the type is unknown and {@code NewProperty<?>} is being used, then
 * {@link #setValueSupplierOrThrow(Supplier<?>)} can be used to initialize its value.
 * <p>
 * The context of using multiple instances of {@code NewProperty} is expected to be the process of enhancement of the same entity.
 * Hence, the identify for instances of {@code NewProperty} is determined by field {@code name}, which stands for the property name.
 *
 * @author TG Team
 */
public final class NewProperty<T> {
    public static final IsProperty DEFAULT_IS_PROPERTY_ANNOTATION = new IsPropertyAnnotation().newInstance();

    private final String name;
    private final Class<T> type;
    private final List<Type> typeArguments = new ArrayList<Type>();
    private final String title;
    private final String desc;

    /**
     * Stores all annotations that are directly present on this property except for {@link IsProperty},
     * which is stored in a separate field {@link #atIsProperty}.
     */
    private final List<Annotation> annotations = new ArrayList<Annotation>();

    /**
     * Stores the instance of {@link IsProperty} annotation, since it requires frequent access.
     * Should never be null after initialization.
     */
    private IsProperty atIsProperty;
    private Supplier<T> valueSupplier; // a supplier for property's initialisation value
    private boolean initialised;

    /**
     * Constructs a property from {@code field}.
     * To initialize the value explicitly use {@link #setValueSupplierOrThrow(Supplier<Object>)}.
     * @param field
     * @return
     */
    public static NewProperty<?> fromField(final Field field) {
        final Annotation[] annotations = field.getDeclaredAnnotations();

        final Type genericType = field.getGenericType();
        // explicit check is needed to be able to store the information about type arguments
        if (ParameterizedType.class.isInstance(genericType)) {
            final ParameterizedType parType = (ParameterizedType) genericType;
            return new NewProperty<>(field.getName(), PropertyTypeDeterminator.classFrom(parType.getRawType()),
                    Arrays.asList(parType.getActualTypeArguments()), null, null, annotations);
        }
        // only raw type information is available
        return new NewProperty<>(field.getName(), PropertyTypeDeterminator.classFrom(genericType), null, null, annotations);
    }

    /**
     * Constructs a property from a field with <code>name</code> found in <code>type</code>'s hierarchy.
     * To initialize the value explicitly use {@link #setValueSupplierOrThrow(Supplier<Object>)}.
     *
     * @param type
     * @param name
     * @return
     */
    public static NewProperty<?> fromField(final Class<?> type, final String name) {
        return fromField(Finder.getFieldByName(type, name));
    }

    /**
     * A convenient factory method for creating a new property representation with a raw type and type arguments.
     * <p>
     * Refer to {@link #NewProperty(String, Class, List, String, String, Annotation...)} for details.
     * 
     * @param name simple name of the property
     * @param rawType rawType of the property
     * @param typeArguments actual typeArguments if any, otherwise pass <code>null</code> or use another constructor
     * @param title property title as in {@link Title} annotation
     * @param desc property description as in {@link Title} annotation
     * @param annotations annotations directly present on this property (see note)
     */
    public static <T> NewProperty<T> create(final String name, final Class<T> rawType, final List<Type> typeArguments, final String title,
            final String desc, final Annotation... annotations) 
    {
        return new NewProperty<T>(name, rawType, typeArguments, title, desc, annotations);
    }
    
    /**
     * Creates a new property representation with a raw type and type arguments.
     * <p>
     * If <code>annotations</code> do not contain {@link IsProperty}, then it's added implicitly. However, for collectional properties
     * and {@link PropertyDescriptor}s it is required to explicitly supply an instance of {@link IsProperty} with 
     * its {@code value()} set accordingly.
     * <p>
     * Generally, {@link Title} annotation is constructed using <code>title</code> and <code>desc</code>, so having
     * <code>annotations</code> contain {@link Title} has no effect.
     * If, however, both <code>title</code> and <code>desc</code> are <code>null</code>, then {@link Title} from 
     * <code>annotations</code> is considered (if found).
     * 
     * @param name simple name of the property
     * @param rawType rawType of the property
     * @param typeArguments actual typeArguments if any, otherwise pass <code>null</code> or use another constructor
     * @param title property title as in {@link Title} annotation
     * @param desc property description as in {@link Title} annotation
     * @param annotations annotations directly present on this property (see note)
     */
    public NewProperty(final String name, final Class<T> rawType, final List<Type> typeArguments, final String title, final String desc, final Annotation... annotations) {
        if (StringUtils.isBlank(name)) {
            throw new NewPropertyException("New propety name cannot be blank.");
        }
        if (rawType == null) {
            throw new NewPropertyException("New propety type cannot be null.");
        }

        this.name = name;
        this.type = rawType;
        this.typeArguments.addAll(typeArguments);
        this.title = title;
        this.desc = desc;
        
        if (title != null || desc != null) {
            this.annotations.add(titleAnnotation());
        }

        // is @IsProperty provided?
        final Optional<Annotation> maybeIsProperty = Arrays.stream(annotations)
                .filter(annot -> annot.annotationType() == IsProperty.class)
                .findAny();
        if (maybeIsProperty.isPresent()) {
            this.atIsProperty = (IsProperty) maybeIsProperty.get();
            // this::addAnnotation is used to avoid the possibility of old code breaking, since it could be providing duplicate annotations
            // and also to avoid the possibility of adding @Title twice
            Arrays.stream(annotations)
                .filter(annot -> annot.annotationType() != IsProperty.class)
                .forEach(this::addAnnotation);
        }
        else {
            this.atIsProperty = new IsPropertyAnnotation().newInstance();
            // this::addAnnotation is used to avoid the possibility of old code breaking, since it could be providing duplicate annotations
            // and also to avoid the possibility of adding @Title twice
            Arrays.stream(annotations).forEach(this::addAnnotation);
        }
    }

    /**
     * A convenient factory method for creating a new property representation with a raw type.
     * <p>
     * Refer to {@link #NewProperty(String, Class, String, String, Annotation...)} for details.
     * 
     * @param name simple name of the property
     * @param rawType raw type of the property
     * @param title property title as in {@link Title} annotation
     * @param desc property description as in {@link Title} annotation
     * @param annotations annotations directly present on this property (see note)
     */
    public static <T> NewProperty<T> create(final String name, final Class<T> rawType, final String title, final String desc, final Annotation... annotations) {
        return new NewProperty<T>(name, rawType, List.of(), title, desc, annotations);
    }

    /**
     * Creates a new property representation with a raw type.
     * <p>
     * If <code>annotations</code> do not contain {@link IsProperty}, then it's added implicitly.
     * <p>
     * Generally, {@link Title} annotation is constructed using <code>title</code> and <code>desc</code>, so having
     * <code>annotations</code> contain {@link Title} has no effect.
     * If, however, both <code>title</code> and <code>desc</code> are <code>null</code>, then {@link Title} from 
     * <code>annotations</code> is considered (if found).
     * 
     * @param name simple name of the property
     * @param rawType raw type of the property
     * @param title property title as in {@link Title} annotation
     * @param desc property description as in {@link Title} annotation
     * @param annotations annotations directly present on this property (see note)
     */
    public NewProperty(final String name, final Class<T> rawType, final String title, final String desc, final Annotation... annotations) {
        this(name, rawType, List.of(), title, desc, annotations);
    }

    /**
     * A convenient factory method for creating a new property representation with a parameterized type.
     * <p>
     * Refer to {@link #NewProperty(String name, Class, List, String, String, Annotation...)} for details.
     *
     * @param name simple name of the property
     * @param type parameterized type of the property
     * @param title property title as in {@link Title} annotation
     * @param desc property description as in {@link Title} annotation
     * @param annotations annotations directly present on this property (see note)
     */
    public static NewProperty<?> create(final String name, final ParameterizedType type, final String title, final String desc,
            final Annotation... annotations) 
    {
        return new NewProperty<>(name, PropertyTypeDeterminator.classFrom(type.getRawType()), Arrays.asList(type.getActualTypeArguments()),
                title, desc, annotations);
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
    @SuppressWarnings("unchecked") // these casts are safe, since they are preceeded by manual type checking
    public <A extends Annotation> A getAnnotationByType(final Class<A> annotationType) {
        if (annotationType == IsProperty.class) return atIsProperty == null ? null : (A) atIsProperty;
        else {
            return getAnnotations().stream().filter(annot -> annot.annotationType() == annotationType)
                    .findAny().map(annot -> (A) annot).orElse(null);
        }
    }

    /**
     * Add the specified annotation to the list of property annotations only if it hasn't been added yet.
     *
     * @param annotation
     * @return
     */
    public NewProperty<T> addAnnotation(final Annotation annotation) {
        final Class<? extends Annotation> type = annotation.annotationType();

        if (type == IsProperty.class && atIsProperty == null) {
            atIsProperty = (IsProperty) annotation;
        }
        else if (!containsAnnotationDescriptorFor(type)) {
            annotations.add(annotation);
        }

        return this;
    }
    
    private Title titleAnnotation() {
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
     * The same rules apply to {@link PropertyDescriptor} which also accepts a single type argument.
     * <p>
     * Refer to {@link #genericTypeAsDeclared()} to get a precise representation (which would simply return a raw {@link List}
     * in the example above).
     * <p>
     * <i>Note:</i> This method tries to determine the correct generic type on every call, so use it sparingly.
     * 
     * @return the generic type of this property (either {@link Class} or {@link ParameterizedType})
     */
    public Type genericType() {
        if (type == null) return null;

        final Type[] typeArgs;
        // prioritize the actual type arguments
        if (hasTypeArguments()) {
            typeArgs = typeArguments.toArray(Type[]::new);
        }
        // special case? look at the value of @IsProperty
        else if (isCollectional() || isPropertyDescriptor()) {
            final Class<?> value = atIsProperty.value();
            typeArgs = (value == Void.class) ? null : new Type[] {value};
        }
        else typeArgs = null;

        // found any type arguments?
        return typeArgs == null ? type : newParameterizedType(type, typeArgs);
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

    public Class<T> getRawType() {
        return type;
    }

    public List<Type> getTypeArguments() {
        return typeArguments;
    }

    /**
     * Sets type arguments of this property's type.
     */
    public NewProperty<T> setTypeArguments(final List<Type> typeArguments) {
        this.typeArguments.clear();
        this.typeArguments.addAll(typeArguments);
        return this;
    }
    
    /**
     * Sets type arguments of this property's type.
     */
    public NewProperty<T> setTypeArguments(final Type... typeArguments) {
        return setTypeArguments(Arrays.asList(typeArguments));
    }
    
    public NewProperty<T> addTypeArguments(final List<Type> typeArguments) {
        for (final Type typeArg: typeArguments) {
            this.typeArguments.add(typeArg);
        }
        return this;
    }

    public NewProperty<T> addTypeArguments(final Type... typeArguments) {
        return addTypeArguments(Arrays.asList(typeArguments));
    }
    
    /**
     * Returns {@link IsProperty} annotation for this property.
     */
    public IsProperty getIsProperty() {
        return atIsProperty;
    }
    
    /**
     * Changes the {@code value()} of {@link IsProperty} annotation for this property.
     * <p>
     * It should be emphasized that this method <b>changes</b>, rather than <b>sets</b> the new value.
     * Old reference to {@link IsProperty} instance is discarded by creating a copy with the new {@code value}.
     */
    public NewProperty<T> changeIsPropertyValue(final Class<?> value) {
        atIsProperty = IsPropertyAnnotation.from(atIsProperty).value(value).newInstance();
        return this;
    }

    /**
     * Returns all annotations present on this property. The returned list is unmodifiable.
     */
    public List<Annotation> getAnnotations() {
        final List<Annotation> all = new ArrayList<>(annotations);
        if (atIsProperty != null) {
            all.add(atIsProperty);
        }

        return Collections.unmodifiableList(all);
    }

    public NewProperty<T> setAnnotations(final List<Annotation> annotations) {
        this.annotations.clear();
        annotations.forEach(this::addAnnotation);
        return this;
    }

    public NewProperty<T> setAnnotations(final Annotation... annotations) {
        return setAnnotations(Arrays.asList(annotations));
    }
    
    public boolean isInitialised() {
        return this.initialised;
    }
    
    /**
     * Returns the initialized value of this property.
     * @return
     */
    public Supplier<T> getValueSupplier() {
        return valueSupplier;
    }
    
    /**
     * Sets a new initialising value supplier.
     * @param valueSupplier
     * @return this modified instance
     */
    public NewProperty<T> setValueSupplier(final Supplier<T> valueSupplier) {
        this.valueSupplier = valueSupplier;
        this.initialised = true;
        return this;
    }

    /**
     * Sets the initialization value of this property to <code>value</code>.
     * <p>
     * If <code>value</code> is not assignment-compatible with this property's type, then an exception is thrown.
     * @param valueSupplier
     * @return this modified instance
     * @throws NewPropertyException
     */
    @SuppressWarnings("unchecked")
    public NewProperty<T> setValueSupplierOrThrow(final Supplier<?> valueSupplier) {
        final Object value = valueSupplier.get();
        // if supplier returns null we assume that this is a valid initialisation value
        if (value != null && !type.isInstance(value)) {
            throw new NewPropertyException("Couldn't initialise property value due to incompatible types. Property type: %s. Value type: %s.".formatted(type, value.getClass()));
        }

        return setValueSupplier((Supplier<T>) valueSupplier); // this cast is safe
    }

    /**
     * Returns a newly created copy of this instance. However, the annotation instances are not copied, which means that you shouldn't modify them.
     * @return
     */
    public NewProperty<T> copy() {
        return new NewProperty<T>(name, type, List.copyOf(typeArguments), title, desc, getAnnotations().toArray(Annotation[]::new));
    }
    
    /**
     * Returns a fresh copy of this instance with its raw type set to <code>rawType</code>.
     * <p>
     * <i>Note:</i> individual annotation instances are not copied, but referenced instead. 
     * @param <C> new raw type
     * @param rawType
     * @return
     */
    public <C> NewProperty<C> changeType(final Class<C> rawType) {
        return NewProperty.create(name, rawType, List.copyOf(typeArguments), title, desc,
                getAnnotations().toArray(Annotation[]::new));
    }
    
    /**
     * Returns a fresh copy of this instance with its type arguments updated with {@code typeArguments}.
     * <p>
     * <i>Note:</i> individual annotation instances are not copied, but referenced instead. 
     * @param typeArguments
     * @return
     */
    public NewProperty<T> changeTypeArguments(final List<Type> typeArguments) {
        return NewProperty.create(name, type, typeArguments, title, desc, getAnnotations().toArray(Annotation[]::new));
    }

    /**
     * Returns a fresh copy of this instance with its type arguments updated with {@code typeArguments}.
     * <p>
     * <i>Note:</i> individual annotation instances are not copied, but referenced instead. 
     * @param typeArguments
     * @return
     */
    public NewProperty<T> changeTypeArguments(final Type... typeArguments) {
        return changeTypeArguments(Arrays.asList(typeArguments));
    }
    
    @Override
    public int hashCode() {
        return 31 * name.hashCode();
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }

        if (!(obj instanceof NewProperty)) {
            return false;
        }

        final NewProperty<?> that = (NewProperty<?>) obj;
        return this.name.equals(that.name);
    }

    /**
     * Returns a string representation of this property in its fullest form (with all annotations included).
     * For shorter representations see {@link #toString(boolean)}.
     */
    @Override
    public String toString() {
        return toString(false);
    }

    /**
     * Returns a string representation of this property with or without its annotations.
     *
     * @param withoutAnnotations - controls whether to omit annotations for the string representation
     * @return
     */
    public String toString(final boolean withoutAnnotations) {
        final StringBuilder strBuilder = new StringBuilder();
        strBuilder.append(String.format("%s: %s", name, genericTypeAsDeclared().getTypeName()));
        if (!withoutAnnotations) {
            strBuilder.append(", annotations: " + getAnnotations().stream().map(a -> "@" + a.annotationType().getSimpleName()).collect(joining(", ")));
        }
        return strBuilder.toString();
    }

}
