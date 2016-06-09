package ua.com.fielden.platform.domaintree;

public interface IProperty {
    Class<?> getRoot();

    String getContextPath();

    String getTitle();

    String getDesc();

    String name();

    Class<?> resultType();

    String path();
}
