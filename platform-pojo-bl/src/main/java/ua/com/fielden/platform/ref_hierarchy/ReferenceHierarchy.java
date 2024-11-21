package ua.com.fielden.platform.ref_hierarchy;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.AbstractFunctionalEntityWithCentreContext;
import ua.com.fielden.platform.entity.NoKey;
import ua.com.fielden.platform.entity.annotation.*;
import ua.com.fielden.platform.entity.annotation.mutator.BeforeChange;
import ua.com.fielden.platform.entity.annotation.mutator.Handler;
import ua.com.fielden.platform.entity.validation.ICanBuildReferenceHierarchyForEntityValidator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static java.util.Optional.empty;
import static java.util.Optional.of;
import static ua.com.fielden.platform.entity.NoKey.NO_KEY;

/**
 * Functional entity for representing a reference hierarchy master component, used as transport between reference hierarchy master and server.
 * The server receives the data used to determine the next sub-hierarchy to load and returns it to the client.
 *
 * @author TG Team
 *
 */
@EntityTitle("Reference Hierarchy")
@KeyType(NoKey.class)
@CompanionObject(IReferenceHierarchy.class)
public class ReferenceHierarchy extends AbstractFunctionalEntityWithCentreContext<NoKey> {

    @IsProperty
    @Title(value = "Referenced Entity ID", desc = "Referenced Entity Id for which type level of hierarchy should be build")
    private Long refEntityId;

    @IsProperty
    @Title(value = "Referenced Entity Type", desc = "The type of Referenced Entity ID")
    @BeforeChange(@Handler(ICanBuildReferenceHierarchyForEntityValidator.class))
    private String refEntityType;

    @IsProperty
    @Title(value = "Entity Type", desc = "The type of entity that references the Referenced Entity ID'")
    @BeforeChange(@Handler(ICanBuildReferenceHierarchyForEntityValidator.class))
    private String entityType;

    @IsProperty
    @Title(value = "Reference Hierarchy Filter", desc = "Text to match entity types or entity instances")
    private String referenceHierarchyFilter;

    @IsProperty
    @Title(value = "Show active only?", desc = "If true, only active and non-activatable references are displayed in the “Referenced By” sections.")
    private boolean activeOnly;

    @IsProperty(AbstractEntity.class)
    @Title(value = "Generated Hierarchy", desc = "Generated type or instance level of hierarchy")
    private final List<AbstractEntity<?>> generatedHierarchy = new ArrayList<>();

    @IsProperty(Long.class)
    @Title(value = "Loaded hiererchy", desc = "The indexes of tree items on each level where returned hieararchy should be inserted")
    private final List<Long> loadedHierarchy = new ArrayList<>();

    @IsProperty
    @Title(value = "Page Size", desc = "Page size of inctances to load")
    private Integer pageSize;

    @IsProperty
    @Title(value = "Page Number", desc = "Page number of instances to load")
    private Integer pageNumber;

    @IsProperty
    @Title(value = "Page Count", desc = "The data page count")
    private Integer pageCount;

    @IsProperty
    @Title("Title")
    private String title;

    @IsProperty
    @Title(value = "Reset Filter?", desc = "Indicates whether filter should be reset or not")
    private boolean resetFilter = true;

    @IsProperty
    @Title(value = "Loaded Reference Hierarchy Level", desc = "The refernce hierarchy level that was loaded on previous call")
    private String loadedLevel;

    public String getLoadedLevel() {
        return loadedLevel;
    }

    @Observable
    public ReferenceHierarchy setLoadedLevel(final String loadedLevel) {
        this.loadedLevel = loadedLevel;
        return this;
    }

    public ReferenceHierarchy setLoadedHierarchyLevel(final ReferenceHierarchyLevel level) {
        return setLoadedLevel(level.name());
    }

    public ReferenceHierarchyLevel getLoadedHierarchyLevel() {
        return ReferenceHierarchyLevel.valueOf(this.loadedLevel);
    }

    @Observable
    public ReferenceHierarchy setResetFilter(final boolean resetFilter) {
        this.resetFilter = resetFilter;
        return this;
    }

    public boolean getResetFilter() {
        return resetFilter;
    }

    @Observable
    public ReferenceHierarchy setTitle(final String title) {
        this.title = title;
        return this;
    }

    public String getTitle() {
        return title;
    }

    @Observable
    public ReferenceHierarchy setPageCount(final Integer pageCount) {
        this.pageCount = pageCount;
        return this;
    }

    public Integer getPageCount() {
        return pageCount;
    }

    @Observable
    public ReferenceHierarchy setPageNumber(final Integer pageNumber) {
        this.pageNumber = pageNumber;
        return this;
    }

    public Integer getPageNumber() {
        return pageNumber;
    }

    @Observable
    public ReferenceHierarchy setPageSize(final Integer pageSize) {
        this.pageSize = pageSize;
        return this;
    }

    public Integer getPageSize() {
        return pageSize;
    }

    @Observable
    protected ReferenceHierarchy setLoadedHierarchy(final List<Long> loadedHierarchy) {
        this.loadedHierarchy.clear();
        this.loadedHierarchy.addAll(loadedHierarchy);
        return this;
    }

    public List<Long> getLoadedHierarchy() {
        return Collections.unmodifiableList(loadedHierarchy);
    }

    @Observable
    protected ReferenceHierarchy setGeneratedHierarchy(final List<? extends AbstractEntity<?>> generatedHierarchy) {
        this.generatedHierarchy.clear();
        this.generatedHierarchy.addAll(generatedHierarchy);
        return this;
    }

    public List<AbstractEntity<?>> getGeneratedHierarchy() {
        return Collections.unmodifiableList(generatedHierarchy);
    }

    public ReferenceHierarchy() {
        setKey(NO_KEY);
    }

    public boolean isActiveOnly() {
        return activeOnly;
    }

    @Observable
    public ReferenceHierarchy setActiveOnly(final boolean activeOnly) {
        this.activeOnly = activeOnly;
        return this;
    }

    @Observable
    public ReferenceHierarchy setReferenceHierarchyFilter(final String referenceHierarchyFilter) {
        this.referenceHierarchyFilter = referenceHierarchyFilter;
        return this;
    }

    public String getReferenceHierarchyFilter() {
        return referenceHierarchyFilter;
    }

    @Observable
    public ReferenceHierarchy setRefEntityId(final Long refEntityId) {
        this.refEntityId = refEntityId;
        return this;
    }

    public Long getRefEntityId() {
        return refEntityId;
    }

    @Observable
    public ReferenceHierarchy setRefEntityType(final String refEntityType) {
        this.refEntityType = refEntityType;
        return this;
    }

    public String getRefEntityType() {
        return refEntityType;
    }

    /**
     * Returns an optional value of property {@code refEntityType} represented as a {@code class}.
     * Empty value indicates an error when trying to instantiate a {@code class} from its string representation.
     *
     * @return
     */
    @SuppressWarnings("unchecked")
    public Optional<Class<? extends AbstractEntity<?>>> getRefEntityClass() {
        try {
            return of((Class<? extends AbstractEntity<?>>) Class.forName(refEntityType));
        } catch (final Exception e) {
            return empty();
        }
    }

    /**
     * Returns an optional value of property {@code entityType} represented as a {@code class}.
     * Empty value indicates an error when trying to instantiate a {@code class} from its string representation.
     *
     * @return
     */
    @SuppressWarnings("unchecked")
    public Optional<Class<? extends AbstractEntity<?>>> getEntityClass() {
        try {
            return of((Class<? extends AbstractEntity<?>>) Class.forName(entityType));
        } catch (final Exception e) {
            return empty();
        }
    }

    @Observable
    public ReferenceHierarchy setEntityType(final String entityType) {
        this.entityType = entityType;
        return this;
    }

    public String getEntityType() {
        return entityType;
}
}