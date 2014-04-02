package ua.com.fielden.platform.eql.s2.elements;

import java.util.ArrayList;
import java.util.List;

public abstract class AbstractSource2 implements ISource2 {

    public List<EntProp2> props = new ArrayList<>();

    @Override
    public String toString() {
        return sourceType().getSimpleName();
    }

    @Override
    public void addProp(final EntProp2 prop) {
        props.add(prop);
    }

    public List<EntProp2> props() {
        return props;
    }
}