package ua.com.fielden.platform.entity;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

import ua.com.fielden.platform.entity.annotation.IsProperty;
import ua.com.fielden.platform.entity.annotation.MapTo;
import ua.com.fielden.platform.entity.annotation.Observable;
import ua.com.fielden.platform.entity.annotation.Title;

/**
 * A base class for functional entities that are intended to modify entity (master entity) collectional association properties. The master entity, whose collection modifies,
 * needs to have persistent nature.
 * <p>
 * The key type for the functional entity should be the same as the master entity type. Implementors should also implement producer that is descendant of 
 * {@link AbstractFunctionalEntityProducerForCollectionModification} -- this producer will assign the key for this functional entity and will do other preparation job.
 * <p>
 * Concrete implementors need to be persistent (do not forget to annotate with @MapEntityTo annotation).
 *
 * @author TG Team
 *
 * @param <K> -- the type of the master entity, whose collection modifies
 */
public abstract class AbstractFunctionalEntityForCollectionModification<K extends AbstractEntity<?>> extends AbstractFunctionalEntityWithCentreContext<K> {
    private static final long serialVersionUID = 1L;
    
    @IsProperty(value = Long.class) 
    @Title(value = "Chosen ids", desc = "IDs of chosen entities (added and / or remained chosen)")
    private Set<Long> chosenIds = new LinkedHashSet<Long>();
    
    @IsProperty(value = Long.class)
    @Title(value = "Added ids", desc = "IDs of added entities")
    private Set<Long> addedIds = new LinkedHashSet<Long>();
    
    @IsProperty(value = Long.class)
    @Title(value = "Removed ids", desc = "IDs of removed entities")
    private Set<Long> removedIds = new LinkedHashSet<Long>();
    
    @IsProperty
    @MapTo
    @Title(value = "Surrogate Version", desc = "Surrogate Version (used also as the property to mark this entity as dirty for saving purposes)")
    private Long surrogateVersion;

    @Observable
    public AbstractFunctionalEntityForCollectionModification<K> setSurrogateVersion(final Long surrogateVersion) {
        this.surrogateVersion = surrogateVersion;
        return this;
    }

    public Long getSurrogateVersion() {
        return surrogateVersion;
    }

    @Observable
    protected AbstractFunctionalEntityForCollectionModification<K> setAddedIds(final Set<Long> addedIds) {
        this.addedIds.clear();
        this.addedIds.addAll(addedIds);
        return this;
    }

    public Set<Long> getAddedIds() {
        return Collections.unmodifiableSet(addedIds);
    }

    @Observable
    protected AbstractFunctionalEntityForCollectionModification<K> setRemovedIds(final Set<Long> removedIds) {
        this.removedIds.clear();
        this.removedIds.addAll(removedIds);
        return this;
    }

    public Set<Long> getRemovedIds() {
        return Collections.unmodifiableSet(removedIds);
    }

    @Observable
    public AbstractFunctionalEntityForCollectionModification<K> setChosenIds(final Set<Long> chosenIds) {
        this.chosenIds.clear();
        this.chosenIds.addAll(chosenIds);
        return this;
    }

    public Set<Long> getChosenIds() {
        return Collections.unmodifiableSet(chosenIds);
    }
    
    /**
     * Override to ignore link property requiredness for collectional properties 
     * <code>chosenIds</code>, <code>addedIds</code>, <code>removedIds</code>.
     *  
     * @param entityType
     * @param propertyName
     * @return
     */
    @Override
    protected boolean isLinkPropertyRequiredButMissing(final String propertyName) {
        if (!"chosenIds".equals(propertyName) && !"addedIds".equals(propertyName) && !"removedIds".equals(propertyName)) {
            return super.isLinkPropertyRequiredButMissing(propertyName);
        } else {
            return false;
        }
    }
}