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
    @Title(value = "Generated Hierarchy", desc = "Generated types or properties level of hierarchy")
    private List<AbstractEntity<?>> generatedHierarchy = new ArrayList<>();

    @IsProperty(Long.class)
    @Title(value = "Loaded hiererchy", desc = "The indexes of tree items where loaded hierarchy should be inserted")
    private List<Long> loadedHierarchy = new ArrayList<>();

    @IsProperty
    @Title(value = "Domain Type", desc = "Domain Type which properties should be loaded")
    private String domainTypeName;

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

    public DomainExplorerInsertionPoint () {
        setKey(NoKey.NO_KEY);
    }
}
