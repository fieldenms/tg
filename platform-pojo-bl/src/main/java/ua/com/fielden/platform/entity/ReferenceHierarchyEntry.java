package ua.com.fielden.platform.entity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import ua.com.fielden.platform.entity.annotation.CompanionObject;
import ua.com.fielden.platform.entity.annotation.IsProperty;
import ua.com.fielden.platform.entity.annotation.KeyType;
import ua.com.fielden.platform.entity.annotation.Observable;
import ua.com.fielden.platform.entity.annotation.Title;

/**
 * The base entity for all reference hierarchy tree entries.
 *
 * @author TG Team
 *
 */
@KeyType(String.class)
@CompanionObject(IReferenceHierarchyEntry.class)
public class ReferenceHierarchyEntry extends AbstractTreeEntry<String> {

    @IsProperty
    @Title(value = "Reference Hierarchy Level", desc = "One of two available reference hierarchy levels: TYPE or INSTANCE")
    private String level;

    @IsProperty(String.class)
    @Title(value = "Actions", desc = "Action list")
    private List<String> actions = new ArrayList<String>();

    @Observable
    protected ReferenceHierarchyEntry setActions(final List<String> actions) {
        this.actions.clear();
        this.actions.addAll(actions);
        return this;
    }

    public ReferenceHierarchyEntry setHierarchyActions(final ReferenceHierarchyActions... actions) {
        setActions(Arrays.asList(actions).stream().map(action -> action.name()).collect(Collectors.toList()));
        return this;
    }

    public List<String> getActions() {
        return Collections.unmodifiableList(actions);
    }

    @Observable
    public ReferenceHierarchyEntry setLevel(final String level) {
        this.level = level;
        return this;
    }

    public String getLevel() {
        return level;
    }

    public ReferenceHierarchyEntry setHierarchyLevel(final ReferenceHierarchyLevel level) {
        setLevel(level.name());
        return this;
    }

    public ReferenceHierarchyLevel getHierarchyLevel() {
        return ReferenceHierarchyLevel.valueOf(this.level);
    }

}
