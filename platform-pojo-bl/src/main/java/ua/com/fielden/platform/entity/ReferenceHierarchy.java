package ua.com.fielden.platform.entity;

import static ua.com.fielden.platform.entity.NoKey.NO_KEY;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import ua.com.fielden.platform.entity.annotation.CompanionObject;
import ua.com.fielden.platform.entity.annotation.EntityTitle;
import ua.com.fielden.platform.entity.annotation.IsProperty;
import ua.com.fielden.platform.entity.annotation.KeyType;
import ua.com.fielden.platform.entity.annotation.Observable;
import ua.com.fielden.platform.entity.annotation.Title;

@EntityTitle("Reference Hierarchy")
@KeyType(NoKey.class)
@CompanionObject(IReferenceHierarchy.class)
public class ReferenceHierarchy extends AbstractFunctionalEntityWithCentreContext<NoKey> {

    @IsProperty
    @Title(value = "Referenced Entity ID", desc = "Referenced Entity Id for which type level of hierarchy should be build")
    private Long refEntityId;

    @IsProperty
    @Title(value = "Referenced Entity Type", desc = "The type of Referenced Entity ID")
    private String refEntityType;

    @IsProperty
    @Title(value = "Entity Type", desc = "The type of entity that references the Referenced Entity ID'")
    private String entityType;

    @IsProperty
    @Title(value = "Reference Hierarchy Filter", desc = "Text to match entity types or entity instances")
    private String referenceHierarchyFilter;

    @IsProperty(AbstractEntity.class)
    @Title(value = "Generated Hierarchy", desc = "Generated type or instance level of hierarchy")
    private List<AbstractEntity<?>> generatedHierarchy = new ArrayList<>();

    @IsProperty(Integer.class)
    @Title(value = "Loaded hiererchy", desc = "The indexes of tree items on each level where returned hieararchy should be inserted")
    private List<Integer> loadedHierarchy = new ArrayList<>();

    @Observable
    protected ReferenceHierarchy setLoadedHierarchy(final List<Integer> loadedHierarchy) {
        this.loadedHierarchy.clear();
        this.loadedHierarchy.addAll(loadedHierarchy);
        return this;
    }

    public List<Integer> getLoadedHierarchy() {
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

    public Class<? extends AbstractEntity<?>> getRefEntityClass() throws ClassNotFoundException {
        return (Class<? extends AbstractEntity<?>>) Class.forName(refEntityType);
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