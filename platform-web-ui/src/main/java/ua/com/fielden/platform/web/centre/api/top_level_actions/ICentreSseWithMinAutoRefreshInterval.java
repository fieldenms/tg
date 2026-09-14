package ua.com.fielden.platform.web.centre.api.top_level_actions;

import ua.com.fielden.platform.entity.AbstractEntity;

/// A contract for Entity Centre Web UI DSL to limit the frequency of automatic refreshes for Entity Centres, associated with an Event Source.
///
/// If none of the APIs defined in this contract are used when configuring an Entity Centre, associated with an Event Source, then every SSE event may lead to a refresh, subject only to the delays supplied in the event payload and the refresh prompt configuration.
/// One behaviour applies regardless of this configuration: a data load (Run, paging, refresh) cancels a pending automatic refresh as redundant — the load delivers data at least as fresh as the events that instigated that refresh.
///
public interface ICentreSseWithMinAutoRefreshInterval<T extends AbstractEntity<?>> extends ICentreSseWithPromptRefresh<T> {

    /// Specifies the minimum interval between automatic refreshes, driven by SSE events — a "refresh governor".
    ///
    /// If an SSE event arrives sooner than `seconds` after the last completed data load (Run, paging, a prior refresh) or a skipped refresh prompt, the refresh is not discarded — it is deferred until the interval expires.
    /// Any completed data load restarts the interval, which governs only refreshes instigated by subsequent SSE events.
    /// A pending automatic refresh is not deferred to the new interval edge — a data load cancels it as redundant (with or without this configuration), and only the next SSE event can instigate an automatic refresh again.
    /// This bounds both the frequency of automatic refreshes and the staleness of the displayed data, and users actively working with a centre are not interrupted by automatic refreshes at all — each load cancels the pending refresh and pushes the interval out.
    ///
    /// If `seconds` is less than or equal to zero, then a misconfiguration exception is thrown.
    ///
    /// @param seconds  the minimum number of seconds between automatic refreshes
    ///
    ICentreSseWithPromptRefresh<T> withMinAutoRefreshInterval(final int seconds);

}
