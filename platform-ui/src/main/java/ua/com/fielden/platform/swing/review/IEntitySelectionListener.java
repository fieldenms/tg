package ua.com.fielden.platform.swing.review;

import ua.com.fielden.platform.entity.AbstractEntity;

public interface IEntitySelectionListener {

    void performSelection(AbstractEntity selectedObject);

    void performDeselect(AbstractEntity selectedObject);

    boolean isSelected(AbstractEntity entityToCheck);

    void clearSelection();

    boolean isMultiselection();

    boolean isSelectionEmpty();
}
