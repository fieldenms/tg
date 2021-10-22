package ua.com.fielden.platform.migration;

import static java.util.Collections.unmodifiableList;

import java.util.List;

public class EntityMd {
    public final String tableName;
    public final List<PropMd> props;
    
    public EntityMd(String tableName, List<PropMd> props) {
        this.tableName = tableName;
        this.props = unmodifiableList(props);
    }
}