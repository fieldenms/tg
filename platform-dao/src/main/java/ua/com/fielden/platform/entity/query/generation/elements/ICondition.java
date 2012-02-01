package ua.com.fielden.platform.entity.query.generation.elements;


public interface ICondition extends IPropertyCollector {
    boolean ignore();
    String sql();
}
