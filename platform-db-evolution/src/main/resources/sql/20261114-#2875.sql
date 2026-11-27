-- Issue #2875: Entity Centre: the last run of a configuration is shared between browser tabs and devices.
-- https://github.com/fieldenms/tg/issues/2875
--
-- Deletes the PREVIOUSLY_RUN surrogate centres of Entity Centre configurations.
-- Each browser tab or device now keeps the last run of a configuration itself.
-- The application therefore neither reads nor writes these rows.
-- Apply it together with the platform release that includes issue #2875, or at any time after it.
-- The application may keep running while it is applied.
--
-- A PREVIOUSLY_RUN title has the shape `__________PREVIOUSLY_RUN[<save-as name>]__________DIFFERENCES`.
-- The `[<save-as name>]` part is optional.
-- Rows left with the `MOBILE` prefix by the migration of issue #2795 are deleted too, as nothing uses them either.
-- `LEFT` is used rather than `LIKE`, where `_` would match any character.
--
-- The script serves both SQL Server and PostgreSQL, and it is idempotent.

DELETE FROM ENTITY_CENTRE_CONFIG
 WHERE LEFT(TITLE, 24) = '__________PREVIOUSLY_RUN'
    OR LEFT(TITLE, 30) = 'MOBILE__________PREVIOUSLY_RUN';
