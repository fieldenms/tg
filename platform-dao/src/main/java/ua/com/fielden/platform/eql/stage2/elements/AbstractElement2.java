package ua.com.fielden.platform.eql.stage2.elements;

public abstract class AbstractElement2 {
    public final int contextId;
    
    protected AbstractElement2(final int contextId) {
        this.contextId = contextId;
    }
    
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + contextId;
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        if (!(obj instanceof AbstractElement2)) {
            return false;
        }
        
        final AbstractElement2 other = (AbstractElement2) obj;
        
        return contextId == other.contextId;
   }
}