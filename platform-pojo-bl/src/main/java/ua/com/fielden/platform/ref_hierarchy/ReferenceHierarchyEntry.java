package ua.com.fielden.platform.ref_hierarchy;

import ua.com.fielden.platform.entity.annotation.*;

import java.util.ArrayList;
import java.util.List;

import static java.util.Collections.unmodifiableList;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Stream.of;

/**
 * The base entity for all reference hierarchy tree entries.
 *
 * @author TG Team
 *
 */
@KeyType(String.class)
@DescTitle("# of references")
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
        setActions(of(actions).map(action -> action.name()).collect(toList()));
        return this;
    }

    public List<String> getActions() {
        return unmodifiableList(actions);
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

    @Override
    @Observable
    public ReferenceHierarchyEntry setDesc(String desc) {
        return super.setDesc(desc);
    }

}
