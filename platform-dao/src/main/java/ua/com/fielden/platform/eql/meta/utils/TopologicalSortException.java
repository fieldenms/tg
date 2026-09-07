package ua.com.fielden.platform.eql.meta.utils;

import ua.com.fielden.platform.exceptions.AbstractPlatformCheckedException;

import java.util.List;

public final class TopologicalSortException extends AbstractPlatformCheckedException {

    @java.io.Serial
    private static final long serialVersionUID = 1L;

    /// Nodes forming a detected cycle, in edge order (e.g. `[a, b, c, a]`); empty if not captured.
    ///
    private final List<?> cycle;

    public TopologicalSortException(final String message) {
        this(message, List.of());
    }

    public TopologicalSortException(final String message, final List<?> cycle) {
        super(message);
        this.cycle = List.copyOf(cycle);
    }

    public TopologicalSortException(final String message, final Throwable cause) {
        super(message, cause);
        this.cycle = List.of();
    }

    /// Nodes forming a detected cycle, in edge order; empty if the cycle was not captured.
    ///
    public List<?> cycle() {
        return cycle;
    }

}
