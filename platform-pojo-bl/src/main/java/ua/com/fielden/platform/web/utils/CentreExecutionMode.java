package ua.com.fielden.platform.web.utils;

/// Distinguishes the two execution modes of `CriteriaResource#executeEntityCentreConfiguration`.
///
/// Sealed deliberately so that any future Entity Centre evolution that introduces a new mode forces
/// reconciliation with the central execution method (the compiler will flag a non-exhaustive switch).
/// This protects programmatic callers (e.g., `DefaultEntityCentreProcessor`) from breaking when the
/// UI flow changes.
///
/// Permitted implementations:
/// - [HeadlessCentreExecution] -- programmatic API; data only, no UI augmentation.
/// - [UiCentreExecution] -- UI-driven; rendering hints, action indices, dynamic-column metadata,
///   and (when running) the criteria-changed indication are produced.
///
public sealed interface CentreExecutionMode permits HeadlessCentreExecution, UiCentreExecution {}