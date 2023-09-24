package ua.com.fielden.platform.eql.stage2.sources.enhance;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class FirstChunkGroup  {
    public final PropChunk firstChunk;
    public final List<PendingTail> tails = new ArrayList<>(); // tails that follow `firstChunk`
    public final List<Prop2Lite> origins = new ArrayList<>(); // originals props for which `firstChunk` happened to be the last PropChunk in their pending tail

    public FirstChunkGroup(final PropChunk firstChunk) {
        this.firstChunk = firstChunk;
    }
    
    public List<Prop2Lite> getOrigins() {
        return Collections.unmodifiableList(origins);
    }
}
