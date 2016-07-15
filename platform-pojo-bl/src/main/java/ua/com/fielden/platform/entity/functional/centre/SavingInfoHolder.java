package ua.com.fielden.platform.entity.functional.centre;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.annotation.CompanionObject;
import ua.com.fielden.platform.entity.annotation.IsProperty;
import ua.com.fielden.platform.entity.annotation.KeyTitle;
import ua.com.fielden.platform.entity.annotation.KeyType;
import ua.com.fielden.platform.entity.annotation.Observable;
import ua.com.fielden.platform.entity.annotation.Title;

/**
 * The entity holder for saving information: entity's modified props + centre context, if any.
 *
 * @author TG Team
 *
 */
@KeyType(String.class)
@KeyTitle(value = "Key", desc = "Some key description")
@CompanionObject(ISavingInfoHolder.class)
public class SavingInfoHolder extends AbstractEntity<String> {
    private static final long serialVersionUID = -1062648037823353306L;

    @IsProperty(Object.class)
    @Title(value = "Modified properties holder", desc = "Modified properties holder")
    private final Map<String, Object> modifHolder = new HashMap<String, Object>();

    @IsProperty
    @Title(value = "Centre context holder", desc = "Centre context holder")
    private CentreContextHolder centreContextHolder;
    
    @IsProperty(AbstractEntity.class)
    @Title("Continuations")
    private final ArrayList<AbstractEntity<?>> continuations = new ArrayList<AbstractEntity<?>>();
    
    @IsProperty(String.class)
    @Title("Continuation Properties")
    private final ArrayList<String> continuationProperties = new ArrayList<>();
    
    @Observable
    protected SavingInfoHolder setContinuationProperties(final ArrayList<String> continuationProperties) {
        this.continuationProperties.clear();
        this.continuationProperties.addAll(continuationProperties);
        return this;
    }

    public ArrayList<String> getContinuationProperties() {
        return /* Collections.unmodifiableList( */ continuationProperties /* ) */;
    }
    
    @Observable
    protected SavingInfoHolder setContinuations(final ArrayList<AbstractEntity<?>> continuations) {
        this.continuations.clear();
        this.continuations.addAll(continuations);
        return this;
    }

    public ArrayList<AbstractEntity<?>> getContinuations() {
        return /* Collections.unmodifiableList( */ continuations /* ) */;
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
    protected SavingInfoHolder setModifHolder(final Map<String, Object> modifHolder) {
        this.modifHolder.clear();
        this.modifHolder.putAll(modifHolder);
        return this;
    }

    public Map<String, Object> getModifHolder() {
        return Collections.unmodifiableMap(modifHolder);
    }
}