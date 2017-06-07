package ua.com.fielden.platform.entity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import ua.com.fielden.platform.entity.annotation.CompanionObject;
import ua.com.fielden.platform.entity.annotation.IsProperty;
import ua.com.fielden.platform.entity.annotation.KeyTitle;
import ua.com.fielden.platform.entity.annotation.KeyType;
import ua.com.fielden.platform.entity.annotation.Observable;
import ua.com.fielden.platform.entity.annotation.Title;

/**
 * Represents the sequential edit action.
 *
 * @author Developers
 *
 */
@KeyType(String.class)
@KeyTitle("Sequentila edit")
@CompanionObject(ISequentialEntityEditAction.class)
public class SequentialEntityEditAction extends AbstractEntityManipulationAction {

    @IsProperty
    @Title("Entity ids' to edit")
    private List<Long> entitiesToEdit = new ArrayList<Long>();

    @Observable
    protected SequentialEntityEditAction setEntitiesToEdit(final List<Long> entitiesToEdit) {
        this.entitiesToEdit.clear();
        this.entitiesToEdit.addAll(entitiesToEdit);
        return this;
    }

    public List<Long> getName() {
        return Collections.unmodifiableList(entitiesToEdit);
    }

}
