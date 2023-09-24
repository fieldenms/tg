package ua.com.fielden.platform.eql.stage2.sources.enhance;

import java.util.List;

public record FirstChunkGroup(
        PropChunk firstChunk, 
        List<Prop2Lite> origins, // originals props for which `firstChunk` happened to be the last PropChunk in their pending tail
        List<PendingTail> tails // tails that follow `firstChunk`
) {
}