package ua.com.fielden.platform.sample.domain;

public class Continuation extends RuntimeException {
    private static final long serialVersionUID = 1L;
    
    public final String initiatingEntityTypeStr;

    public Continuation(final Class<?> initiatingEntityType) {
        super("Continuation for [" + initiatingEntityType.getSimpleName() + "] entity.");
        this.initiatingEntityTypeStr = initiatingEntityType.getSimpleName();
    }
}
