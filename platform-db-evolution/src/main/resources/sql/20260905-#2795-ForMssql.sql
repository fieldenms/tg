-- Issue #2795: Remove device-based separation of Entity Centre configurations.
-- https://github.com/fieldenms/tg/issues/2795
--
-- Migrates MOBILE Entity Centre configurations into the joint namespace formerly owned by DESKTOP.
-- Apply it together with the `CentreUpdater` change that turns `deviceSpecific` into the identity function.
-- Apply it while the application is stopped, because it does not bump `_VERSION`.
--
-- A persisted title has the shape `MOBILE<surrogate>[<save-as name>]__________DIFFERENCES`.
-- The `MOBILE` prefix and the `[<save-as name>]` part are both optional.
-- A surrogate is one of `__________FRESH`, `__________SAVED` and `__________PREVIOUSLY_RUN`.
--
-- A single configuration is a group of rows sharing an owner, a menu item and a save-as name.
-- `CentreUpdater.editCentreTitleAndDesc` renames FRESH, SAVED and PREVIOUSLY_RUN as one unit.
-- It also fails outright when FRESH or SAVED is missing.
-- Therefore this script migrates a configuration group as a whole, or leaves all of its rows alone.
--
-- Steps:
--     1. Delete MOBILE link configurations.
--     2. Make every remaining MOBILE configuration un-preferred.
--     3. Rename each MOBILE configuration group into a named configuration carrying a ` (mobile)` suffix.
--
-- A group whose new title is already taken is skipped and keeps its `MOBILE` prefix.
-- Such a group becomes a harmless orphan, unreachable once `deviceSpecific` stops producing that prefix.
--
-- The script is idempotent.
-- Re-running it migrates nothing further and skips exactly the same orphans.
--
-- Run it in autocommit mode, so that a failed group is rolled back on its own.
-- Do not wrap it in an explicit transaction.

SET NOCOUNT OFF;
GO

-- `XACT_ABORT` must stay off, so that a caught error does not doom the surrounding transaction.
SET XACT_ABORT OFF;
GO

-- Step 1 of 3.
-- Delete MOBILE link configurations.
--
-- A link configuration uses the reserved save-as name `_______________________link`.
-- The `Load` dialog hides it by an exact match on that name, which a migrated title would no longer satisfy.
-- Deleting these rows also makes their `configUuid` resolve to nothing once the device filter is gone.
DELETE FROM ENTITY_CENTRE_CONFIG
 WHERE LEFT(TITLE, 6) = 'MOBILE'
   AND RIGHT(TITLE, 50) = '[_______________________link]__________DIFFERENCES';
GO

PRINT 'Step 1: deleted MOBILE link configurations.';
GO

-- Step 2 of 3.
-- Make every remaining MOBILE configuration un-preferred.
--
-- In practice `preferred` is only ever set on FRESH named rows, because `getAllPreferredConfigs` looks only at those.
-- The predicate below still covers every MOBILE row, so that no stale flag survives the migration.
UPDATE ENTITY_CENTRE_CONFIG
   SET PREFERRED_ = 'N'
 WHERE LEFT(TITLE, 6) = 'MOBILE'
   AND (PREFERRED_ IS NULL OR PREFERRED_ <> 'N');
GO

PRINT 'Step 2: made MOBILE configurations un-preferred.';
GO

-- Step 3 of 3.
-- Rename MOBILE configurations into the joint namespace.

IF OBJECT_ID('tempdb..#MobileConfig') IS NOT NULL DROP TABLE #MobileConfig;
GO

-- `CORE` drops the `MOBILE` prefix and the `__________DIFFERENCES` suffix, leaving `<surrogate>[<save-as name>]`.
-- `SAVE_AS_PART` is `[<save-as name>]` for a named configuration, and an empty string for a default one.
-- Those two forms can never be equal, so `SAVE_AS_PART` identifies a configuration within an owner and a menu item.
-- A default configuration becomes named `Default (mobile)`, and a named one gains a ` (mobile)` suffix.
--
-- The first operand of the concatenation is cast to `VARCHAR(8000)`, so that a long new title is not truncated.
-- An over-long title is then detected and skipped rather than silently cut down to `TITLE VARCHAR(255)`.
--
-- The shortest migratable title is `MOBILE__________FRESH__________DIFFERENCES`, which is 42 characters long.
-- Both `SUBSTRING` length arguments are guarded by a `CASE`, because `CROSS APPLY` is part of the `FROM` clause.
-- A shorter title would otherwise reach `SUBSTRING` with a negative length before the `WHERE` clause discards it.
SELECT ecc._ID                                  AS ID,
       ecc.ID_CRAFT                             AS OWNER_ID,
       ecc.ID_MAIN_MENU                         AS MENU_ID,
       part.SAVE_AS_PART                        AS SAVE_AS_PART,
       newTitle.NEW_TITLE                       AS NEW_TITLE
  INTO #MobileConfig
  FROM ENTITY_CENTRE_CONFIG ecc
 CROSS APPLY (VALUES (SUBSTRING(ecc.TITLE, 7, CASE WHEN LEN(ecc.TITLE) >= 42
                                                   THEN LEN(ecc.TITLE) - 27
                                                   ELSE 0
                                              END))) AS core(CORE)
 CROSS APPLY (VALUES (CASE WHEN LEFT(core.CORE, 15) IN ('__________FRESH', '__________SAVED') THEN 15
                           WHEN LEFT(core.CORE, 24) = '__________PREVIOUSLY_RUN' THEN 24
                      END)) AS surrogate(SURROGATE_LEN)
 CROSS APPLY (VALUES (SUBSTRING(core.CORE, surrogate.SURROGATE_LEN + 1, LEN(core.CORE)))) AS part(SAVE_AS_PART)
 CROSS APPLY (VALUES (CASE WHEN LEN(part.SAVE_AS_PART) >= 2
                           THEN SUBSTRING(part.SAVE_AS_PART, 2, LEN(part.SAVE_AS_PART) - 2)
                           ELSE 'Default'
                      END)) AS saveAs(SAVE_AS_NAME)
 CROSS APPLY (VALUES (CAST(LEFT(core.CORE, surrogate.SURROGATE_LEN) AS VARCHAR(8000))
                      + '[' + saveAs.SAVE_AS_NAME + ' (mobile)]'
                      + '__________DIFFERENCES')) AS newTitle(NEW_TITLE)
 WHERE LEFT(ecc.TITLE, 6) = 'MOBILE'
   AND RIGHT(ecc.TITLE, 21) = '__________DIFFERENCES'
   AND LEN(ecc.TITLE) >= 42
   AND surrogate.SURROGATE_LEN IS NOT NULL;
GO

CREATE INDEX I_MOBILECONFIG__GROUP ON #MobileConfig(OWNER_ID, MENU_ID, SAVE_AS_PART);
GO

DECLARE @ownerId BIGINT, @menuId BIGINT, @saveAsPart VARCHAR(8000);
DECLARE @migrated INT = 0, @skippedConflict INT = 0, @skippedError INT = 0;

-- Groups are processed in order of their lowest `_ID`, so that the outcome does not depend on the query plan.
-- Two MOBILE groups can compete for one new title, for example a default one and a named one called `Default`.
-- Ordering by `MIN(ID)` lets the older of the two win, and the other is skipped as a conflict.
DECLARE configGroup CURSOR LOCAL FAST_FORWARD FOR
    SELECT OWNER_ID, MENU_ID, SAVE_AS_PART
      FROM #MobileConfig
     GROUP BY OWNER_ID, MENU_ID, SAVE_AS_PART
     ORDER BY MIN(ID);

OPEN configGroup;
FETCH NEXT FROM configGroup INTO @ownerId, @menuId, @saveAsPart;

WHILE @@FETCH_STATUS = 0
BEGIN
    BEGIN TRY
        -- A group is migrated only when every one of its new titles is free and fits into `TITLE VARCHAR(255)`.
        -- Rows renamed by earlier iterations are already visible here, so MOBILE-to-MOBILE clashes are caught too.
        IF EXISTS (SELECT 1
                     FROM #MobileConfig m
                    WHERE m.OWNER_ID = @ownerId
                      AND m.MENU_ID = @menuId
                      AND m.SAVE_AS_PART = @saveAsPart
                      AND (LEN(m.NEW_TITLE) > 255
                           OR EXISTS (SELECT 1
                                        FROM ENTITY_CENTRE_CONFIG e
                                       WHERE e.ID_CRAFT = m.OWNER_ID
                                         AND e.ID_MAIN_MENU = m.MENU_ID
                                         AND e.TITLE = m.NEW_TITLE)))
        BEGIN
            SET @skippedConflict = @skippedConflict + 1;
        END
        ELSE
        BEGIN
            UPDATE e
               SET e.TITLE = m.NEW_TITLE
              FROM ENTITY_CENTRE_CONFIG e
             INNER JOIN #MobileConfig m ON m.ID = e._ID
             WHERE m.OWNER_ID = @ownerId
               AND m.MENU_ID = @menuId
               AND m.SAVE_AS_PART = @saveAsPart;
            SET @migrated = @migrated + 1;
        END
    END TRY
    BEGIN CATCH
        -- An unforeseen failure skips this configuration only, and leaves it behind as a MOBILE orphan.
        SET @skippedError = @skippedError + 1;
        PRINT 'Skipped owner [' + CAST(@ownerId AS VARCHAR(20))
            + '], menu item [' + CAST(@menuId AS VARCHAR(20))
            + '], save-as part [' + @saveAsPart
            + '] due to: ' + ERROR_MESSAGE();
    END CATCH

    FETCH NEXT FROM configGroup INTO @ownerId, @menuId, @saveAsPart;
END

CLOSE configGroup;
DEALLOCATE configGroup;

PRINT 'Step 3: migrated ' + CAST(@migrated AS VARCHAR(20)) + ' configuration(s), skipped '
    + CAST(@skippedConflict AS VARCHAR(20)) + ' on conflict and '
    + CAST(@skippedError AS VARCHAR(20)) + ' on error.';
GO

DROP TABLE #MobileConfig;
GO
