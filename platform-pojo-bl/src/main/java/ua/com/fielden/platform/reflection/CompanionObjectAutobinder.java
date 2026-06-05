package ua.com.fielden.platform.reflection;

import com.google.inject.Binder;
import com.google.inject.binder.ScopedBindingBuilder;
import ua.com.fielden.platform.dao.IEntityDao;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.annotation.CompanionIsGenerated;
import ua.com.fielden.platform.entity.annotation.CompanionObject;
import ua.com.fielden.platform.entity.exceptions.EntityDefinitionException;

import java.util.Objects;
import java.util.function.BiFunction;
import java.util.stream.Stream;

import static java.lang.String.format;
import static ua.com.fielden.platform.reflection.AnnotationReflector.isAnnotationPresent;

/**
 * A convenience class that assists in automation of binding companion object implementations to its declarations. 
 * It automatically determines the companion object contract from annotation {@link CompanionObject} on the type of an entity object.
 * <p>
 * A possible implementation for a companion object is determined using naming heuristics:
 * <ul>
 * <li> The full name of the companion implementation class matches the full name of a corresponding entity type, but with suffix "Dao". 
 * <li> The full name of the companion implementation class matches the package name of a corresponding entity type and its simple name with prefix "Co".
 * </ul>
 * 
 * If neither are found then a runtime exception of type {@link EntityDefinitionException} is thrown. Otherwise, the first match is used.
 * 
 * @author TG Team
 * 
 */
public class CompanionObjectAutobinder {

    public static final String ERR_MISSING_CO = "Could not find an implementation for companion object [%s].";
    public static final String ERR_MISSING_CO_DECLARATION = "Entity [%s] is missing a companion object declaration.";

    private CompanionObjectAutobinder() {}

    /**
     * Returns companion object type or null if its declaration is missing from the entity object.
     * 
     * @param entityType
     * @return
     */
    public static <T extends IEntityDao<E>, E extends AbstractEntity<?>> Class<T> companionObjectType(final Class<E> entityType) {
        if (entityType.isAnnotationPresent(CompanionObject.class)) {
            return (Class<T>) entityType.getAnnotation(CompanionObject.class).value();
        }
        return null;
    }

    /**
     * Uses the provided binder to bind a companion object implementation to entity's companion object contract.
     * 
     * @param entityType
     * @param binder
     */
    public static <T extends IEntityDao<E>, E extends AbstractEntity<?>> void bindCo(final Class<E> entityType, final Binder binder) {
        bindCo(entityType, (co, type) -> binder.bind(co).to(type));
    }

    /**
     * Uses the specified {@code bindFunction} to bind a companion object for {@code entityType} to its implementation.
     *
     * @param entityType
     * @param bindFunction
     */
    public static <T extends IEntityDao<E>, E extends AbstractEntity<?>> void bindCo(final Class<E> entityType, final BiFunction<Class<T>, Class<T>, ScopedBindingBuilder> bindFunction) {
        // Companion type will be generated only after the IoC configuration is complete.
        if (isAnnotationPresent(entityType, CompanionIsGenerated.class)) {
            return;
        }

        final Class<T> co = companionObjectType(entityType);

        if (co == null) { // check if the companion is declared
            throw new EntityDefinitionException(format(ERR_MISSING_CO_DECLARATION,  entityType.getSimpleName()));
        } else {
            // determine a type implementing the companion for the passed in entity type
            // and bind it if found, otherwise throw an exception
            Stream.of(
                    format("%sDao", entityType.getName()),                                            // the legacy DAO naming strategy
                    format("%s.Co%s", entityType.getPackage().getName(), entityType.getSimpleName())) // the new Co naming strategy
            .map(name -> (Class<T>) fromString(name))
            .filter(Objects::nonNull).findFirst()
            .map(type -> bindFunction.apply(co, type)).orElseThrow(() -> {
                return new EntityDefinitionException(format(ERR_MISSING_CO, co.getSimpleName()));
            }); 
        }
    }
    
    private static <T extends IEntityDao<E>, E extends AbstractEntity<?>> Class<T> fromString(final String fullTypeName) {
        try {
            return (Class<T>) ClassesRetriever.findClass(fullTypeName);
        } catch (final Exception ex) {
            return null;
        }
        
    }

}
