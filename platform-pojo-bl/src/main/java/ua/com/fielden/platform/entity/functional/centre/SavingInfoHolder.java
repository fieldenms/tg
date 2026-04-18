package ua.com.fielden.platform.entity.functional.centre;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.IContinuationData;
import ua.com.fielden.platform.entity.NoKey;
import ua.com.fielden.platform.entity.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.Collections.unmodifiableList;
import static java.util.Collections.unmodifiableMap;
import static ua.com.fielden.platform.entity.NoKey.NO_KEY;

/// The entity holder for saving information: entity's modified props + centre context, if any.
///
@KeyType(NoKey.class)
@CompanionObject(ISavingInfoHolder.class)
// GraphQL Web API schema may not find `CentreContextHolder` type in case if it is used as property in allowed for introspection types
// (e.g. those that are persistent / synthetic and have no @DenyIntrospection).
// That's why it is explicitly denied for introspection and such properties become `AbstractDomainTreeRepresentation.isExcluded`.
@DenyIntrospection
public class SavingInfoHolder extends AbstractEntity<NoKey> {
    
    @IsProperty(Object.class)
    @Title(value = "Modified properties holder", desc = "Modified properties holder")
    private final Map<String, Object> modifHolder = new HashMap<>();
    
    @IsProperty
    @Title(value = "Originally Produced Entity", desc = "The entity (new only) that was produced during master's contextual retrieval and then reused during validation, saving and autocompletion processes as a validation prototype")
    private AbstractEntity<?> originallyProducedEntity;
    
    @IsProperty
    @Title(value = "Centre context holder", desc = "Centre context holder")
    private CentreContextHolder centreContextHolder;
    
    @IsProperty(IContinuationData.class)
    @Title("Continuations")
    private final ArrayList<IContinuationData> continuations = new ArrayList<>();
    
    @IsProperty(String.class)
    @Title("Continuation Properties")
    private final ArrayList<String> continuationProperties = new ArrayList<>();
    
    public SavingInfoHolder() {
        setKey(NO_KEY);
    }

    @Observable
    protected SavingInfoHolder setContinuationProperties(final ArrayList<String> continuationProperties) {
        this.continuationProperties.clear();
        this.continuationProperties.addAll(continuationProperties);
        return this;
    }

    public List<String> getContinuationProperties() {
        return unmodifiableList(continuationProperties);
    }
    
    @Observable
    protected SavingInfoHolder setContinuations(final ArrayList<IContinuationData> continuations) {
        this.continuations.clear();
        this.continuations.addAll(continuations);
        return this;
    }

    public List<IContinuationData> getContinuations() {
        return unmodifiableList(continuations);
    }

    @Observable
    public SavingInfoHolder setCentreContextHolder(final CentreContextHolder centreContextHolder) {
        this.centreContextHolder = centreContextHolder;
        return this;
    }

    public CentreContextHolder getCentreContextHolder() {
        return centreContextHolder;
    }
    
    @Observable
    public SavingInfoHolder setOriginallyProducedEntity(final AbstractEntity<?> originallyProducedEntity) {
        this.originallyProducedEntity = originallyProducedEntity;
        return this;
    }

    public AbstractEntity<?> getOriginallyProducedEntity() {
        return originallyProducedEntity;
    }

    @Observable
    public SavingInfoHolder setModifHolder(final Map<String, Object> modifHolder) {
        this.modifHolder.clear();
        this.modifHolder.putAll(modifHolder);
        return this;
    }

    public Map<String, Object> getModifHolder() {
        return unmodifiableMap(modifHolder);
    }
}
