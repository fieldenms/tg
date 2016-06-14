package ua.com.fielden.platform.entity;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

import ua.com.fielden.platform.entity.annotation.IsProperty;
import ua.com.fielden.platform.entity.annotation.KeyType;
import ua.com.fielden.platform.entity.annotation.MapTo;
import ua.com.fielden.platform.entity.annotation.Observable;
import ua.com.fielden.platform.entity.annotation.Title;
import ua.com.fielden.platform.web.centre.CentreConfigUpdater;

/**
 * A base class for functional entities that are intended to modify entity (master entity) collectional association properties. The master entity, whose collection modifies,
 * needs to have persistent nature.
 * <p>
 * Implementors should implement producer that is descendant of {@link AbstractFunctionalEntityProducerForCollectionModification} -- this producer will assign the key for this functional entity and will do other preparation job.
 * <p>
 * Concrete implementors need to be persistent (do not forget to annotate with @MapEntityTo annotation).
 * <p>
 * Note, that there exists {@link CentreConfigUpdater} implementor that deviate from above rules. Its master entity (centre criteria entity) is not persistent, and {@link CentreConfigUpdater} is not persistent too.
 * This is legal, because no versioning is required for centre configuration updating process (no other user is able to change centre configuration of current user). But, the above rules are applicable for application development
 * during implementation of 'collection updater' functional entities.
 *
 * @author TG Team
 *
 * @param <ID_TYPE> -- the type of identifiers for collection items (usually Long.class for persisted entities, otherwise it should be equal to key type).
 */
@KeyType(Long.class)
public abstract class AbstractFunctionalEntityForCollectionModification<ID_TYPE> extends AbstractFunctionalEntityWithCentreContext<Long> {
    private static final long serialVersionUID = 1L;
    
    /**
     * This is to be used for internal validation for versioning -- this is not needed to be send to the client application, that is why it was made as non-property (no {@link IsProperty} annotation).
     */
    private AbstractEntity<?> refetchedMasterEntity;
    
    @IsProperty(value = Long.class) 
    @Title(value = "Chosen ids", desc = "IDs of chosen entities (added and / or remained chosen)")
    private Set<ID_TYPE> chosenIds = new LinkedHashSet<>();
    
    @IsProperty(value = Long.class)
    @Title(value = "Added ids", desc = "IDs of added entities")
    private Set<ID_TYPE> addedIds = new LinkedHashSet<>();
    
    @IsProperty(value = Long.class)
    @Title(value = "Removed ids", desc = "IDs of removed entities")
    private Set<ID_TYPE> removedIds = new LinkedHashSet<>();
    
    @IsProperty
    @MapTo
    @Title(value = "Surrogate Version", desc = "Surrogate Version (used also as the property to mark this entity as dirty for saving purposes)")
    private Long surrogateVersion;

    @Observable
    public AbstractFunctionalEntityForCollectionModification<ID_TYPE> setSurrogateVersion(final Long surrogateVersion) {
        this.surrogateVersion = surrogateVersion;
        return this;
    }

    public Long getSurrogateVersion() {
        return surrogateVersion;
    }

    @Observable
    protected AbstractFunctionalEntityForCollectionModification<ID_TYPE> setAddedIds(final Set<ID_TYPE> addedIds) {
        this.addedIds.clear();
        this.addedIds.addAll(addedIds);
        return this;
    }

    public Set<ID_TYPE> getAddedIds() {
        return Collections.unmodifiableSet(addedIds);
    }

    @Observable
    protected AbstractFunctionalEntityForCollectionModification<ID_TYPE> setRemovedIds(final Set<ID_TYPE> removedIds) {
        this.removedIds.clear();
        this.removedIds.addAll(removedIds);
        return this;
    }

    public Set<ID_TYPE> getRemovedIds() {
        return Collections.unmodifiableSet(removedIds);
    }

    @Observable
    public AbstractFunctionalEntityForCollectionModification<ID_TYPE> setChosenIds(final Set<ID_TYPE> chosenIds) {
        this.chosenIds.clear();
        this.chosenIds.addAll(chosenIds);
        return this;
    }

    public Set<ID_TYPE> getChosenIds() {
        return Collections.unmodifiableSet(chosenIds);
    }
    
    public static boolean isCollectionOfIds(final String propertyName) {
        return "chosenIds".equals(propertyName) || "addedIds".equals(propertyName) || "removedIds".equals(propertyName);
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
        if (!isCollectionOfIds(propertyName)) {
            return super.isLinkPropertyRequiredButMissing(propertyName);
        } else {
            return false;
        }
    }
    
    public void setRefetchedMasterEntity(final AbstractEntity<?> refetchedMasterEntity) {
        // to be initialised early in base producer of functional entity (AbstractFunctionalEntityForCollectionModificationProducer)
        this.refetchedMasterEntity = refetchedMasterEntity;
    }
    
    public AbstractEntity<?> refetchedMasterEntity() {
        return refetchedMasterEntity;
    }
}