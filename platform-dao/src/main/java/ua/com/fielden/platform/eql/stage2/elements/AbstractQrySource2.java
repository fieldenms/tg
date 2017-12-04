package ua.com.fielden.platform.eql.stage2.elements;

import java.util.ArrayList;
import java.util.List;

import ua.com.fielden.platform.eql.stage2.elements.IQrySource2;

public abstract class AbstractQrySource2 implements IQrySource2 {

    private final List<EntProp2> props = new ArrayList<>();

    @Override
    public String toString() {
        return sourceType().getSimpleName();
    }

    @Override
    public void addProp(final EntProp2 prop) {
        props.add(prop);
    }
}