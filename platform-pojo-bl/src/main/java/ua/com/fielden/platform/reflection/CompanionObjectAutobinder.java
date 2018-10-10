package ua.com.fielden.platform.reflection;

import static java.lang.String.format;

import java.util.Objects;
import java.util.stream.Stream;

import com.google.inject.Binder;

import ua.com.fielden.platform.dao.IEntityDao;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.annotation.CompanionObject;
import ua.com.fielden.platform.entity.exceptions.EntityDefinitionException;

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
        final Class<T> co = companionObjectType(entityType);

        if (co == null) { // check if the companion is declared
            throw new EntityDefinitionException(format("Entity of type [%s] is missing a companion object declaration.",  entityType.getSimpleName()));
        } else {
            // determine a type implementing the companion for the passed in entity type
            // and bind it if found, otherwise throw an exception
            Stream.of(
                    format("%sDao", entityType.getName()),                                            // the legacy DAO naming strategy
                    format("%s.Co%s", entityType.getPackage().getName(), entityType.getSimpleName())) // the new Co naming strategy
            .map(name -> (Class<T>) fromString(name))
            .filter(Objects::nonNull).findFirst()
            .map(type -> binder.bind(co).to(type)).orElseThrow(() -> new EntityDefinitionException(format("Could not find a implementation for companion object of type [%s]", co.getSimpleName()))); 
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
