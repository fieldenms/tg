package ua.com.fielden.platform.eql.stage2.sources.enhance;

import java.util.List;

// there are 2 types: 1) tail corresponds to link, 2) tail is shorter (as left side being converted into nodes)
public record PendingTail(Prop2Lite link, List<PropChunk> tail) {
    public PendingTail {
        tail = List.copyOf(tail);
    }
}