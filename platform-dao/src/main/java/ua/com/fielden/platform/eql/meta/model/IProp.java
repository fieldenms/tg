package ua.com.fielden.platform.eql.meta.model;

import java.util.Map;

public interface IProp {
    Map<String, IProp> props();
    String name();
}
