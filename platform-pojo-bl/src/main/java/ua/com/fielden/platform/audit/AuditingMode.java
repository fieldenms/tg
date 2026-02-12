package ua.com.fielden.platform.audit;

/// Modes of auditing that an application can use.
///
/// The standard mode is [#ENABLED].
///
/// Auditing facilities should document their behaviour that depends on the active mode.
///
public enum AuditingMode {

    /// The standard modus operandi.
    /// All audited entity types must have corresponding audit types.
    ENABLED,

    /// This mode supports generation of audit sources.
    /// It permits absence of audit types at runtime, which would otherwise be required.
    GENERATION,

    /// Disables auditing entirely.
    DISABLED,

}
