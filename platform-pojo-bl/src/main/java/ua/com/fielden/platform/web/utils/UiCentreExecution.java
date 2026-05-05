package ua.com.fielden.platform.web.utils;

import ua.com.fielden.platform.domaintree.centre.ICentreDomainTreeManager.ICentreDomainTreeManagerAndEnhancer;

/// UI-driven execution from `CriteriaResource`. All UI-specific output is produced.
///
/// `updatedFreshCentre` and `previouslyRunCentre` are consumed only when `isRunning` is `true`,
/// to compute the criteria-changed indication. They are unused when `isRunning` is `false`
/// (sort / navigate / refresh) but must still be supplied for symmetry with the UI request flow.
///
public record UiCentreExecution(
    ICentreDomainTreeManagerAndEnhancer updatedFreshCentre,
    ICentreDomainTreeManagerAndEnhancer previouslyRunCentre
) implements CentreExecutionMode {}