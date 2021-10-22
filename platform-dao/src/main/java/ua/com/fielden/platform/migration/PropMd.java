package ua.com.fielden.platform.migration;

import static java.util.Collections.unmodifiableList;

import java.util.List;

public class PropMd {
    public final String name;
    public final Class<?> type;
    public final String column;
    public final boolean required;
    public final boolean utcType;
    public final List<String> leafProps; // contains prop name for retrieving value for primitive props and key prop name(s) for retrieving value(s) for entity props

    public PropMd(final String name, final Class<?> type, final String column, final boolean required, final boolean utcType, final List<String> leafProps) {
        this.name = name;
        this.type = type;
        this.column = column;
        this.required = required;
        this.utcType = utcType;
        this.leafProps = unmodifiableList(leafProps);
    }
}