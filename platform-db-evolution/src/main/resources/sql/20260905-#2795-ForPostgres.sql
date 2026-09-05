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
-- Step 3 runs inside a `DO` block, where each group is wrapped in its own `BEGIN ... EXCEPTION` sub-transaction.
-- A group that fails is therefore rolled back on its own, and the remaining groups still migrate.

-- Step 1 of 3.
-- Delete MOBILE link configurations.
--
-- A link configuration uses the reserved save-as name `_______________________link`.
-- The `Load` dialog hides it by an exact match on that name, which a migrated title would no longer satisfy.
-- Deleting these rows also makes their `configUuid` resolve to nothing once the device filter is gone.
DELETE FROM ENTITY_CENTRE_CONFIG
 WHERE left(TITLE, 6) = 'MOBILE'
   AND right(TITLE, 50) = '[_______________________link]__________DIFFERENCES';

-- Step 2 of 3.
-- Make every remaining MOBILE configuration un-preferred.
--
-- In practice `preferred` is only ever set on FRESH named rows, because `getAllPreferredConfigs` looks only at those.
-- The predicate below still covers every MOBILE row, so that no stale flag survives the migration.
UPDATE ENTITY_CENTRE_CONFIG
   SET PREFERRED_ = 'N'
 WHERE left(TITLE, 6) = 'MOBILE'
   AND (PREFERRED_ IS NULL OR PREFERRED_ <> 'N');

-- Step 3 of 3.
-- Rename MOBILE configurations into the joint namespace.

DROP TABLE IF EXISTS MOBILE_CONFIG;

-- `CORE` drops the `MOBILE` prefix and the `__________DIFFERENCES` suffix, leaving `<surrogate>[<save-as name>]`.
-- `SAVE_AS_PART` is `[<save-as name>]` for a named configuration, and an empty string for a default one.
-- Those two forms can never be equal, so `SAVE_AS_PART` identifies a configuration within an owner and a menu item.
-- A default configuration becomes named `Default (mobile)`, and a named one gains a ` (mobile)` suffix.
--
-- The shortest migratable title is `MOBILE__________FRESH__________DIFFERENCES`, which is 42 characters long.
-- Requiring that length keeps the `length(TITLE) - 27` argument of `substr` positive for every candidate row.
CREATE TEMP TABLE MOBILE_CONFIG AS
WITH MOBILE AS (
    SELECT _ID           AS ID,
           ID_CRAFT      AS OWNER_ID,
           ID_MAIN_MENU  AS MENU_ID,
           TITLE         AS TITLE
      FROM ENTITY_CENTRE_CONFIG
     WHERE left(TITLE, 6) = 'MOBILE'
       AND right(TITLE, 21) = '__________DIFFERENCES'
       AND length(TITLE) >= 42
), CORES AS (
    SELECT m.ID,
           m.OWNER_ID,
           m.MENU_ID,
           substr(m.TITLE, 7, greatest(length(m.TITLE) - 27, 0)) AS CORE
      FROM MOBILE m
), SURROGATES AS (
    SELECT c.ID,
           c.OWNER_ID,
           c.MENU_ID,
           c.CORE,
           CASE WHEN left(c.CORE, 15) IN ('__________FRESH', '__________SAVED') THEN 15
                WHEN left(c.CORE, 24) = '__________PREVIOUSLY_RUN' THEN 24
           END AS SURROGATE_LEN
      FROM CORES c
), PARTS AS (
    SELECT s.ID,
           s.OWNER_ID,
           s.MENU_ID,
           s.CORE,
           s.SURROGATE_LEN,
           substr(s.CORE, s.SURROGATE_LEN + 1) AS SAVE_AS_PART
      FROM SURROGATES s
     WHERE s.SURROGATE_LEN IS NOT NULL
)
SELECT p.ID                                       AS ID,
       p.OWNER_ID                                 AS OWNER_ID,
       p.MENU_ID                                  AS MENU_ID,
       p.SAVE_AS_PART                             AS SAVE_AS_PART,
       left(p.CORE, p.SURROGATE_LEN)
           || '['
           || CASE WHEN length(p.SAVE_AS_PART) >= 2
                   THEN substr(p.SAVE_AS_PART, 2, length(p.SAVE_AS_PART) - 2)
                   ELSE 'Default'
              END
           || ' (mobile)]'
           || '__________DIFFERENCES'             AS NEW_TITLE
  FROM PARTS p;

CREATE INDEX I_MOBILE_CONFIG__GROUP ON MOBILE_CONFIG(OWNER_ID, MENU_ID, SAVE_AS_PART);

DO $$
DECLARE
    grp              RECORD;
    migrated         INT := 0;
    skipped_conflict INT := 0;
    skipped_error    INT := 0;
BEGIN
    -- Groups are processed in order of their lowest `ID`, so that the outcome does not depend on the query plan.
    -- Two MOBILE groups can compete for one new title, for example a default one and a named one called `Default`.
    -- Ordering by `min(ID)` lets the older of the two win, and the other is skipped as a conflict.
    FOR grp IN
        SELECT OWNER_ID, MENU_ID, SAVE_AS_PART
          FROM MOBILE_CONFIG
         GROUP BY OWNER_ID, MENU_ID, SAVE_AS_PART
         ORDER BY min(ID)
    LOOP
        BEGIN
            -- A group is migrated only when every one of its new titles is free and fits into `TITLE VARCHAR(255)`.
            -- Rows renamed by earlier iterations are already visible here, so MOBILE-to-MOBILE clashes are caught too.
            IF EXISTS (SELECT 1
                         FROM MOBILE_CONFIG m
                        WHERE m.OWNER_ID = grp.OWNER_ID
                          AND m.MENU_ID = grp.MENU_ID
                          AND m.SAVE_AS_PART = grp.SAVE_AS_PART
                          AND (length(m.NEW_TITLE) > 255
                               OR EXISTS (SELECT 1
                                            FROM ENTITY_CENTRE_CONFIG e
                                           WHERE e.ID_CRAFT = m.OWNER_ID
                                             AND e.ID_MAIN_MENU = m.MENU_ID
                                             AND e.TITLE = m.NEW_TITLE)))
            THEN
                skipped_conflict := skipped_conflict + 1;
            ELSE
                UPDATE ENTITY_CENTRE_CONFIG e
                   SET TITLE = m.NEW_TITLE
                  FROM MOBILE_CONFIG m
                 WHERE m.ID = e._ID
                   AND m.OWNER_ID = grp.OWNER_ID
                   AND m.MENU_ID = grp.MENU_ID
                   AND m.SAVE_AS_PART = grp.SAVE_AS_PART;
                migrated := migrated + 1;
            END IF;
        EXCEPTION WHEN OTHERS THEN
            -- An unforeseen failure skips this configuration only, and leaves it behind as a MOBILE orphan.
            skipped_error := skipped_error + 1;
            RAISE NOTICE 'Skipped owner [%], menu item [%], save-as part [%] due to: %',
                grp.OWNER_ID, grp.MENU_ID, grp.SAVE_AS_PART, SQLERRM;
        END;
    END LOOP;

    RAISE NOTICE 'Step 3: migrated % configuration(s), skipped % on conflict and % on error.',
        migrated, skipped_conflict, skipped_error;
END $$;

DROP TABLE MOBILE_CONFIG;
