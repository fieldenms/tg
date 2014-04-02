package ua.com.fielden.platform.entity.query.generation.elements;

import java.util.Set;

public interface ICondition extends IPropertyCollector {
    boolean ignore();

    String sql();

    Set<ISource> getInvolvedSources();
}
