package ua.com.fielden.uds.designer.zui.interfaces;

public interface IValue<T> extends Cloneable {
    T getValue();

    void setValue(T value);

    Object clone();

    void registerUpdater(IUpdater<T> updater);

    void removeUpdater(IUpdater<T> updater);

    String getDefaultValue();

    void setDefaultValue(String defaultValue);

    boolean isEmptyPermitted();

    void setEmptyPermitted(boolean emptyPermitted);
}
