package ua.com.fielden.platform.domain.metadata;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.AbstractFunctionalEntityWithCentreContext;
import ua.com.fielden.platform.entity.NoKey;
import ua.com.fielden.platform.entity.annotation.CompanionObject;
import ua.com.fielden.platform.entity.annotation.IsProperty;
import ua.com.fielden.platform.entity.annotation.KeyType;
import ua.com.fielden.platform.entity.annotation.Observable;
import ua.com.fielden.platform.entity.annotation.Title;

/**
 * Entity that represents the insertion point in Domain Explorer Centre.
 *
 * @author TG Team
 *
 */
@KeyType(NoKey.class)
@CompanionObject(DomainExplorerInsertionPointCo.class)
public class DomainExplorerInsertionPoint extends AbstractFunctionalEntityWithCentreContext<NoKey> {

    @IsProperty
    @Title("Domain Filter")
    private String domainFilter;

    @IsProperty(AbstractEntity.class)
    @Title(value = "Generated Part Of Domain Hierarchy", desc = "Generated types or properties of domain hierarchy.")
    private List<AbstractEntity<?>> generatedHierarchy = new ArrayList<>();

    @IsProperty(Long.class)
    @Title(value = "Loaded Domain Hierarchy", desc = "The indices of tree items where loaded domain hierarchy should be inserted.")
    private List<Long> loadedHierarchy = new ArrayList<>();

    @IsProperty
    @Title(value = "Domain Type", desc = "Domain type whose properties should be loaded.")
    private String domainTypeName;

    @IsProperty
    @Title("Domain Type Holder Id")
    private Long domainTypeHolderId;

    @IsProperty
    @Title("Domain Property Holder Id")
    private Long domainPropertyHolderId;

    public DomainExplorerInsertionPoint () {
        setKey(NoKey.NO_KEY);
    }

    @Observable
    public DomainExplorerInsertionPoint setDomainPropertyHolderId(final Long domainPropertyHolderId) {
        this.domainPropertyHolderId = domainPropertyHolderId;
        return this;
    }

    public Long getDomainPropertyHolderId() {
        return domainPropertyHolderId;
    }

    @Observable
    public DomainExplorerInsertionPoint setDomainTypeHolderId(final Long domainTypeHolderId) {
        this.domainTypeHolderId = domainTypeHolderId;
        return this;
    }

    public Long getDomainTypeHolderId() {
        return domainTypeHolderId;
    }

    @Observable
    public DomainExplorerInsertionPoint setDomainTypeName(final String domainTypeName) {
        this.domainTypeName = domainTypeName;
        return this;
    }

    public String getDomainTypeName() {
        return domainTypeName;
    }

    @Observable
    public DomainExplorerInsertionPoint setDomainFilter(final String domainFilter) {
        this.domainFilter = domainFilter;
        return this;
    }

    public String getDomainFilter() {
        return domainFilter;
    }

    @Observable
    protected DomainExplorerInsertionPoint setLoadedHierarchy(final List<Long> loadedHierarchy) {
        this.loadedHierarchy.clear();
        this.loadedHierarchy.addAll(loadedHierarchy);
        return this;
    }

    public List<Long> getLoadedHierarchy() {
        return Collections.unmodifiableList(loadedHierarchy);
    }

    @Observable
    protected DomainExplorerInsertionPoint setGeneratedHierarchy(final List<? extends AbstractEntity<?>> generatedHierarchy) {
        this.generatedHierarchy.clear();
        this.generatedHierarchy.addAll(generatedHierarchy);
        return this;
    }

    public List<AbstractEntity<?>> getGeneratedHierarchy() {
        return Collections.unmodifiableList(generatedHierarchy);
    }

}