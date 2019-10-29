package ua.com.fielden.platform.sample.domain;

import static ua.com.fielden.platform.entity.NoKey.NO_KEY;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import ua.com.fielden.platform.entity.AbstractFunctionalEntityWithCentreContext;
import ua.com.fielden.platform.entity.NoKey;
import ua.com.fielden.platform.entity.annotation.CompanionObject;
import ua.com.fielden.platform.entity.annotation.IsProperty;
import ua.com.fielden.platform.entity.annotation.KeyType;
import ua.com.fielden.platform.entity.annotation.Observable;
import ua.com.fielden.platform.entity.annotation.Title;

/** 
 * Action for testing how selected entities are carried with context and which order is used.
 * 
 * @author Developers
 *
 */
@KeyType(NoKey.class)
@CompanionObject(ITgSelectedEntitiesExampleAction.class)
public class TgSelectedEntitiesExampleAction extends AbstractFunctionalEntityWithCentreContext<NoKey> {
    
    public TgSelectedEntitiesExampleAction() {
        setKey(NO_KEY);
    }
    
    @IsProperty(Long.class)
    @Title("SelectedEntities Sequence")
    private ArrayList<String> selectedEntitiesSeq = new ArrayList<>();
    
    @IsProperty(Long.class)
    @Title("SelectedEntityIds Sequence")
    private ArrayList<String> selectedEntityIdsSeq = new ArrayList<>();
    
    @Observable
    protected TgSelectedEntitiesExampleAction setSelectedEntityIdsSeq(final ArrayList<String> selectedEntityIdsSeq) {
        this.selectedEntityIdsSeq.clear();
        this.selectedEntityIdsSeq.addAll(selectedEntityIdsSeq);
        return this;
    }
    
    public List<String> getSelectedEntityIdsSeq() {
        return Collections.unmodifiableList(selectedEntityIdsSeq);
    }
    
    @Observable
    protected TgSelectedEntitiesExampleAction setSelectedEntitiesSeq(final ArrayList<String> selectedEntitiesSeq) {
        this.selectedEntitiesSeq.clear();
        this.selectedEntitiesSeq.addAll(selectedEntitiesSeq);
        return this;
    }
    
    public List<String> getSelectedEntitiesSeq() {
        return Collections.unmodifiableList(selectedEntitiesSeq);
    }
    
}