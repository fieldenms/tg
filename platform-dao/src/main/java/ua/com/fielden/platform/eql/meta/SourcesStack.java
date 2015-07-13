package ua.com.fielden.platform.eql.meta;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ua.com.fielden.platform.eql.s1.elements.ISource1;
import ua.com.fielden.platform.eql.s2.elements.ISource2;

public class SourcesStack {
    private List<Map<ISource1<? extends ISource2>, SourceInfo>> sourcesList = new ArrayList<>();

    public SourcesStack() {
        sourcesList.add(new HashMap<ISource1<? extends ISource2>, SourceInfo>());
    }

    public List<Map<ISource1<? extends ISource2>, SourceInfo>> getSourcesList() {
        return sourcesList;
    }

    public void add(Map<ISource1<? extends ISource2>, SourceInfo> sources) {
        sourcesList.add(sources);
    }

    public void add(SourcesStack sourcesStack) {
        sourcesList.addAll(sourcesStack.sourcesList);
    }

    public void accumulateTransformedSource(final ISource1<? extends ISource2> originalSource, SourceInfo sourceInfo) {
        sourcesList.get(0).put(originalSource, sourceInfo);
    }

    public ISource2 getTransformedSource(ISource1<? extends ISource2> originalSource) {
        return sourcesList.get(0).get(originalSource).getSource();
    }
}