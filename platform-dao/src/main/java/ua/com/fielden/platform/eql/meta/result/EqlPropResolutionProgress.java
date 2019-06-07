package ua.com.fielden.platform.eql.meta.result;

import static java.util.Collections.unmodifiableList;

import java.util.ArrayList;
import java.util.List;

public class EqlPropResolutionProgress {
    private final List<String> pathToResolve = new ArrayList<>();
    private final List<IEqlQueryResultItem<?>> resolved = new ArrayList<>();

    public EqlPropResolutionProgress(final List<String> pathToResolve, final List<IEqlQueryResultItem<?>> resolved) {
        this.pathToResolve.addAll(pathToResolve);
        this.resolved.addAll(resolved);
    }

    public List<String> getPathToResolve() {
        return unmodifiableList(pathToResolve);
    }

    public List<IEqlQueryResultItem<?>> getResolved() {
        return unmodifiableList(resolved);
    }
    
    @Override
    public String toString() {
        return String.format("Path to resolve: %s; --> Resolved: %s", pathToResolve, resolved);
    }
}