package ua.com.fielden.platform.entity.functional.centre;

import static java.util.Collections.unmodifiableList;
import static java.util.Collections.unmodifiableMap;
import static ua.com.fielden.platform.entity.NoKey.NO_KEY;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.IContinuationData;
import ua.com.fielden.platform.entity.NoKey;
import ua.com.fielden.platform.entity.annotation.CompanionObject;
import ua.com.fielden.platform.entity.annotation.IsProperty;
import ua.com.fielden.platform.entity.annotation.KeyType;
import ua.com.fielden.platform.entity.annotation.Observable;
import ua.com.fielden.platform.entity.annotation.Title;

/**
 * The entity holder for saving information: entity's modified props + centre context, if any.
 *
 * @author TG Team
 *
 */
@KeyType(NoKey.class)
@CompanionObject(ISavingInfoHolder.class)
public class SavingInfoHolder extends AbstractEntity<NoKey> {
    
    @IsProperty(Object.class)
    @Title(value = "Modified properties holder", desc = "Modified properties holder")
    private final Map<String, Object> modifHolder = new HashMap<>();
    
    @IsProperty
    @Title(value = "Previously Applied Entity", desc = "The entity that was produced / fetched on retrieval phase + all client-side modifications applied during retrieve / validate / save requests. This is to be used during validation, saving and autocompletion processes as a validation prototype, against which one latest modification (or few) is to be applied.")
    private AbstractEntity<?> previouslyAppliedEntity;
    
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
    public SavingInfoHolder setPreviouslyAppliedEntity(final AbstractEntity<?> previouslyAppliedEntity) {
        this.previouslyAppliedEntity = previouslyAppliedEntity;
        return this;
    }

    public AbstractEntity<?> getPreviouslyAppliedEntity() {
        return previouslyAppliedEntity;
    }

    @Observable
    protected SavingInfoHolder setModifHolder(final Map<String, Object> modifHolder) {
        this.modifHolder.clear();
        this.modifHolder.putAll(modifHolder);
        return this;
    }

    public Map<String, Object> getModifHolder() {
        return unmodifiableMap(modifHolder);
    }
}