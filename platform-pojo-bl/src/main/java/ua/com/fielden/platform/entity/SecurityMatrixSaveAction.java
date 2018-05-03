package ua.com.fielden.platform.entity;
import static ua.com.fielden.platform.entity.NoKey.NO_KEY;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ua.com.fielden.platform.entity.annotation.CompanionObject;
import ua.com.fielden.platform.entity.annotation.IsProperty;
import ua.com.fielden.platform.entity.annotation.KeyType;
import ua.com.fielden.platform.entity.annotation.Observable;
import ua.com.fielden.platform.entity.annotation.Title;

/**
 * Represents the security matrix save action.
 *
 * @author TG Team
 *
 */
@KeyType(NoKey.class)
@CompanionObject(ISecurityMatrixSaveAction.class)
public class SecurityMatrixSaveAction  extends AbstractFunctionalEntityWithCentreContext<NoKey> {

    @IsProperty(Object.class)
    @Title("Token Role Associations To Save")
    private Map<String, List<Long>> associationsToSave = new HashMap<>();

    @IsProperty(Object.class)
    @Title("Token Role Associations To Remove")
    private Map<String, List<Long>> associationsToRemove = new HashMap<>();

    protected SecurityMatrixSaveAction() {
        setKey(NO_KEY);
    }

    @Observable
    protected SecurityMatrixSaveAction setAssociationsToRemove(final Map<String, List<Long>> associationsToRemove) {
        this.associationsToRemove.clear();
        this.associationsToRemove.putAll(associationsToRemove);
        return this;
    }

    public Map<String, List<Long>> getAssociationsToRemove() {
        return Collections.unmodifiableMap(associationsToRemove);
    }

    @Observable
    protected SecurityMatrixSaveAction setAssociationsToSave(final Map<String, List<Long>> aasociationsToSave) {
        this.associationsToSave.clear();
        this.associationsToSave.putAll(aasociationsToSave);
        return this;
    }

    public Map<String, List<Long>> getAssociationsToSave() {
        return Collections.unmodifiableMap(associationsToSave);
    }
}
