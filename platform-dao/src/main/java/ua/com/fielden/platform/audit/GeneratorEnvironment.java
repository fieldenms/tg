package ua.com.fielden.platform.audit;

/// Provides various facilities for the generation of audit-entities.
///
final class GeneratorEnvironment {

    private final JavaPoet javaPoet;

    public GeneratorEnvironment() {
        this.javaPoet = new JavaPoet();
    }

    public JavaPoet javaPoet() {
        return javaPoet;
    }

}
