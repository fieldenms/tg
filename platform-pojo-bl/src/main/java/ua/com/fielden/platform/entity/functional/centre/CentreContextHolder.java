package ua.com.fielden.platform.entity.functional.centre;

import static java.util.Collections.unmodifiableList;
import static java.util.Collections.unmodifiableMap;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.annotation.CompanionObject;
import ua.com.fielden.platform.entity.annotation.IsProperty;
import ua.com.fielden.platform.entity.annotation.KeyTitle;
import ua.com.fielden.platform.entity.annotation.KeyType;
import ua.com.fielden.platform.entity.annotation.Observable;
import ua.com.fielden.platform.entity.annotation.Title;

/**
 * The entity holder for centre context and criteria entity's modified props.
 *
 * @author TG Team
 *
 */
@KeyType(String.class)
@KeyTitle(value = "Key", desc = "Some key description")
@CompanionObject(ICentreContextHolder.class)
public class CentreContextHolder extends AbstractEntity<String> {

    @IsProperty(Object.class)
    @Title(value = "Custom object", desc = "Custom object")
    private final Map<String, Object> customObject = new HashMap<>();

    @IsProperty(Object.class)
    @Title(value = "Modified properties holder", desc = "Modified properties holder")
    private final Map<String, Object> modifHolder = new HashMap<>();

    @IsProperty
    @Title(value = "Originally Produced Entity", desc = "The entity (new only) that was produced during master's contextual retrieval and then reused during validation, saving and autocompletion processes as a validation prototype")
    private AbstractEntity<?> originallyProducedEntity;

    @IsProperty(AbstractEntity.class)
    @Title(value = "Selected entities", desc = "Selected entities")
    private final ArrayList<AbstractEntity<?>> selectedEntities = new ArrayList<>();

    @IsProperty
    @Title(value = "Master entity", desc = "Master entity")
    private AbstractEntity<?> masterEntity;

    @IsProperty
    @Title(value = "Chosen Property", desc = "The property that was clicked during activation result-set functional action")
    private String chosenProperty;

    @IsProperty(CentreContextHolder.class)
    @Title(value = "Related contexts", desc = "Contexts relate to this one")
    private Map<String, CentreContextHolder> relatedContexts = new HashMap<>();

    @Observable
    protected CentreContextHolder setRelatedContexts(final Map<String, CentreContextHolder> relatedContexts) {
        this.relatedContexts.clear();
        this.relatedContexts.putAll(relatedContexts);
        return this;
    }

    public Map<String, CentreContextHolder> getRelatedContexts() {
        return unmodifiableMap(relatedContexts);
    }

    @Observable
    public CentreContextHolder setChosenProperty(final String chosenProperty) {
        this.chosenProperty = chosenProperty;
        return this;
    }

    public String getChosenProperty() {
        return chosenProperty;
    }

    @Observable
    public CentreContextHolder setMasterEntity(final AbstractEntity<?> masterEntity) {
        this.masterEntity = masterEntity;
        return this;
    }

    public AbstractEntity<?> getMasterEntity() {
        return masterEntity;
    }

    @Observable
    protected CentreContextHolder setSelectedEntities(final ArrayList<AbstractEntity<?>> selectedEntities) {
        this.selectedEntities.clear();
        this.selectedEntities.addAll(selectedEntities);
        return this;
    }

    public List<AbstractEntity<?>> getSelectedEntities() {
        return unmodifiableList(selectedEntities);
    }

    @Observable
    public CentreContextHolder setOriginallyProducedEntity(final AbstractEntity<?> originallyProducedEntity) {
        this.originallyProducedEntity = originallyProducedEntity;
        return this;
    }

    public AbstractEntity<?> getOriginallyProducedEntity() {
        return originallyProducedEntity;
    }

    @Observable
    protected CentreContextHolder setModifHolder(final Map<String, Object> modifHolder) {
        this.modifHolder.clear();
        this.modifHolder.putAll(modifHolder);
        return this;
    }

    public Map<String, Object> getModifHolder() {
        return unmodifiableMap(modifHolder);
    }

    @Observable
    protected CentreContextHolder setCustomObject(final Map<String, Object> customObject) {
        this.customObject.clear();
        this.customObject.putAll(customObject);
        return this;
    }

    public Map<String, Object> getCustomObject() {
        return unmodifiableMap(customObject);
    }
}