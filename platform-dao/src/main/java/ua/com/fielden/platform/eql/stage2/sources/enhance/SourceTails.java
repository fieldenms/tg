package ua.com.fielden.platform.eql.stage2.sources.enhance;

import java.util.List;

import ua.com.fielden.platform.eql.stage2.sources.ISource2;

public record SourceTails(
        ISource2<?> source, 
        List<PendingTail> tails
        ) {
}