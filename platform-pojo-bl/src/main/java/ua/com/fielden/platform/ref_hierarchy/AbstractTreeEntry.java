package ua.com.fielden.platform.ref_hierarchy;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.annotation.IsProperty;
import ua.com.fielden.platform.entity.annotation.Observable;
import ua.com.fielden.platform.entity.annotation.Title;

/**
 * The entity that should be used as a base entity for entities displayed in tree component.
 *
 * @author TG Team
 *
 * @param <K>
 */
public abstract class AbstractTreeEntry<K extends Comparable<?>> extends AbstractEntity<K> {

    @IsProperty
    @Title("Has Children?")
    private boolean hasChildren = false;

    @IsProperty(AbstractTreeEntry.class)
    @Title("Children")
    private List<AbstractTreeEntry<?>> children = new ArrayList<>();

    @IsProperty
    @Title("Parent hierrarchy entry")
    private AbstractTreeEntry<?> parent;

    @Observable
    public AbstractTreeEntry<K> setHasChildren(final boolean hasChildren) {
        this.hasChildren = hasChildren;
        return this;
    }

    public boolean getHasChildren() {
        return hasChildren;
    }

    @Observable
    public AbstractTreeEntry<?> setChildren(final List<? extends AbstractTreeEntry<?>> children) {
        this.children.clear();
        this.children.addAll(children);
        for (final AbstractTreeEntry<?> entry : this.children) {
            entry.setParent(this);
        }
        this.setHasChildren(!this.children.isEmpty());
        return this;
    }

    public List<? extends AbstractTreeEntry<?>> getChildren() {
        return Collections.unmodifiableList(children);
    }

    public AbstractTreeEntry<?> getParent() {
        return parent;
    }

    @Observable
    private void setParent(final AbstractTreeEntry<?> parent) {
        this.parent = parent;
    }
}
