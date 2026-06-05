package ua.com.fielden.platform.web.utils;

/// Headless execution invoked from `DefaultEntityCentreProcessor` for the programmatic API.
/// UI-only output (rendering hints, primary/secondary/property action indices, dynamic-column metadata,
/// criteria-changed indication, leading custom-object entry in the result list) is suppressed.
/// The result list contains only entities.
///
public record HeadlessCentreExecution() implements CentreExecutionMode {}