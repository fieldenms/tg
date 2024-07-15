package ua.com.fielden.platform.eql.stage1;

import static java.util.Collections.unmodifiableList;

import java.util.List;
import java.util.Objects;

import ua.com.fielden.platform.eql.meta.query.AbstractQuerySourceItem;
import ua.com.fielden.platform.eql.stage2.sources.ISource2;
import ua.com.fielden.platform.eql.stage3.sources.ISource3;

public class PropResolution {
    public final ISource2<? extends ISource3> source;
    private final List<AbstractQuerySourceItem<?>> path;

    public PropResolution(final ISource2<? extends ISource3> source, final List<AbstractQuerySourceItem<?>> path) {
        this.source = source;
        this.path = path;
    }
    
    public List<AbstractQuerySourceItem<?>> getPath() {
        return unmodifiableList(path);
    }
    
    public AbstractQuerySourceItem<?> lastPart() {
        return path.get(path.size() - 1);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + source.id().hashCode();
        result = prime * result + path.hashCode();
        return result;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }

        if (!(obj instanceof PropResolution)) {
            return false;
        }
        
        final PropResolution other = (PropResolution) obj;
        
        return Objects.equals(source.id(), other.source.id()) && (path == other.path);
    }
}
