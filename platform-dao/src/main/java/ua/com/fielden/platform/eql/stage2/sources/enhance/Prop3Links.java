package ua.com.fielden.platform.eql.stage2.sources.enhance;

import java.util.List;

public record Prop3Links(Integer sourceId, String propName, List<Prop2Lite> links) {
    public Prop3Links {
        links = List.copyOf(links);
    }
}