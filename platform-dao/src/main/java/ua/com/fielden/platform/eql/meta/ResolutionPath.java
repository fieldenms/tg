package ua.com.fielden.platform.eql.meta;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class ResolutionPath {

    private final List<AbstractPropInfo> elements = new ArrayList<>();

    public void add(AbstractPropInfo element) {
        elements.add(element);
    }

    public void add(ResolutionPath resolutionPath) {
        elements.addAll(resolutionPath.elements);
    }

    public boolean isEmpty() {
        return elements.isEmpty();
    }

    public AbstractPropInfo getFinalMember() {
        return isEmpty() ? null : elements.get(elements.size() - 1);
    }

    public Class javaType() {
        return getFinalMember().javaType();
    }

    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer();
        for (Iterator<AbstractPropInfo> iterator = elements.iterator(); iterator.hasNext();) {
            sb.append(String.format("\n%-60s%-10s", iterator.next(), (iterator.hasNext() ? "->" : "")));
        }
        return sb.toString();
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((elements == null) ? 0 : elements.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        ResolutionPath other = (ResolutionPath) obj;
        if (elements == null) {
            if (other.elements != null)
                return false;
        } else if (!elements.equals(other.elements))
            return false;
        return true;
    }
}