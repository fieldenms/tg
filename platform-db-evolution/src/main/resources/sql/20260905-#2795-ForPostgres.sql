-- Issue #2795: Remove device-based separation of Entity Centre configurations.
-- https://github.com/fieldenms/tg/issues/2795
--
-- Migrates MOBILE Entity Centre configurations into the joint namespace formerly owned by DESKTOP.
-- Apply it together with the platform release that shares Entity Centre configurations across devices.
-- That release also introduces `EntityCentreConfig.preferredOnMobile`, whose column step 1 adds on every database.
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
--     1. Add the `PREFERREDONMOBILE_` column.
--     2. Delete MOBILE link configurations.
--     3. Move the preferredness of MOBILE configurations from `PREFERRED_` to `PREFERREDONMOBILE_`.
--     4. Rename each MOBILE configuration group, except defaults of base users, into one carrying a ` (mobile)` suffix.
--     5. Give every migrated default configuration a `configUuid`, as step 4 turns it into a named one.
--     6. Make the SAVED row of every migrated default configuration a copy of its FRESH row, adding it where missing.
--     7. Make a migrated `Default (mobile)` preferred on mobile, where no other configuration is.
--
-- Preferredness stays per device profile.
-- `PREFERRED_` applies to devices other than mobile, and `PREFERREDONMOBILE_` applies to mobile.
-- After steps 3 and 7, a mobile user opens the same configuration as before, unless its group is left behind.
--
-- A group whose new title is already taken is skipped and keeps its `MOBILE` prefix.
-- Such a group becomes a harmless orphan, unreachable because the application no longer produces that prefix.
--
-- The default configuration groups of base users are left behind in the same way.
-- A migrated default is named `Default (mobile)` for every user, and inheritance from base is decided by name.
-- A derived user's own `Default (mobile)` would be shown as inherited from base, and overwritten by Load or Discard.
-- A derived user without one would be offered the base user's personal default, and could not delete it.
-- On a phone, a base user whose default was left behind opens the default shared with other devices instead.
-- Those base users are reported right after step 4, one message per base user.
--
-- Known and accepted, not addressed here, because base configurations are rare in the MOBILE namespace.
-- A configuration inherited from base or shared stays correctly linked only because both sides are renamed by the
-- same rule; a group skipped on one side but migrated on the other reproduces the defect described in issue #1626.
--
-- The script is idempotent.
-- Re-running it migrates nothing further and skips exactly the same orphans.
--
-- Steps 4 and 5 run inside a `DO` block, where each group is wrapped in its own `BEGIN ... EXCEPTION` sub-transaction.
-- A group that fails is therefore rolled back on its own, and the remaining groups still migrate.
--
-- Step 5 uses `gen_random_uuid()`, which is built in from PostgreSQL 13.
-- On an older server, either enable the `pgcrypto` extension or replace the call with
-- `md5(random()::text || clock_timestamp()::text)::uuid`.

-- Step 1.
-- Add the column of `EntityCentreConfig.preferredOnMobile`.
-- It is required by the application on every database, including one without any MOBILE configurations.
--
-- A NOT NULL column can only be added to a table with rows together with a default, which fills those rows.
-- The default is dropped right after, so that the column is the same as in a newly generated database.
-- Nothing relies on it afterwards: the application always writes the column, and so does the insert of step 6.
ALTER TABLE ENTITY_CENTRE_CONFIG ADD COLUMN IF NOT EXISTS PREFERREDONMOBILE_ CHAR(1) NOT NULL DEFAULT 'N';
ALTER TABLE ENTITY_CENTRE_CONFIG ALTER COLUMN PREFERREDONMOBILE_ DROP DEFAULT;

-- Step 2.
-- Delete MOBILE link configurations.
--
-- A link configuration uses the reserved save-as name `_______________________link`.
-- The `Load` dialog hides it by an exact match on that name, which a migrated title would no longer satisfy.
-- Deleting these rows also makes their `configUuid` resolve to nothing, as uuids are now looked up across devices.
DELETE FROM ENTITY_CENTRE_CONFIG
 WHERE left(TITLE, 6) = 'MOBILE'
   AND right(TITLE, 50) = '[_______________________link]__________DIFFERENCES';

-- Step 3.
-- Move the preferredness of MOBILE configurations from `PREFERRED_` to `PREFERREDONMOBILE_`.
--
-- In practice `preferred` is only ever set on FRESH named rows, because `getAllPreferredConfigs` looks only at those.
-- The predicate below still covers every MOBILE row, so that no stale desktop flag survives the migration.
-- Both assignments read the values from before the update, so the flag moves rather than being lost.
UPDATE ENTITY_CENTRE_CONFIG
   SET PREFERREDONMOBILE_ = CASE WHEN PREFERRED_ = 'Y' THEN 'Y' ELSE PREFERREDONMOBILE_ END,
       PREFERRED_ = 'N'
 WHERE left(TITLE, 6) = 'MOBILE'
   AND (PREFERRED_ IS NULL OR PREFERRED_ <> 'N');

-- Steps 4 and 5.
-- Rename MOBILE configurations into the joint namespace, and give migrated defaults a `configUuid`.

-- Qualified, so that a permanent table of that name is never dropped when no temp table exists.
DROP TABLE IF EXISTS pg_temp.MOBILE_CONFIG;

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
       left(p.CORE, p.SURROGATE_LEN)              AS SURROGATE,
       left(p.CORE, p.SURROGATE_LEN)
           || '['
           || CASE WHEN length(p.SAVE_AS_PART) >= 2
                   THEN substr(p.SAVE_AS_PART, 2, length(p.SAVE_AS_PART) - 2)
                   ELSE 'Default'
              END
           || ' (mobile)]'
           || '__________DIFFERENCES'             AS NEW_TITLE
  FROM PARTS p
 -- The default configuration groups of base users are left behind, see the header.
 WHERE NOT (p.SAVE_AS_PART = '' AND EXISTS (SELECT 1 FROM USER_ u WHERE u._ID = p.OWNER_ID AND u.BASE_ = 'Y'));

CREATE INDEX I_MOBILE_CONFIG__GROUP ON MOBILE_CONFIG(OWNER_ID, MENU_ID, SAVE_AS_PART);

DO $$
DECLARE
    grp              RECORD;
    new_uuid         TEXT;
    migrated         INT := 0;
    skipped_conflict INT := 0;
    skipped_error    INT := 0;
    uuids_assigned   INT := 0;
    report           RECORD;
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
            -- Titles are compared case-insensitively, as on SQL Server by default.
            -- This way, both scripts skip the same groups.
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
                                             AND lower(e.TITLE) = lower(m.NEW_TITLE))))
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

                -- Step 5, applied only to a group that has just been migrated, and only to a default one.
                -- A default carries no `configUuid`, since only save-as, link and inherited configurations get one.
                -- Turning it into a named one breaks the invariant that a loadable configuration always has a uuid,
                -- which the `Load` dialog relies on to tell own save-as configurations apart from orphaned ones.
                -- FRESH and SAVED receive one fresh uuid, exactly as a save-as would.
                -- Where SAVED is missing, step 6 creates it with that uuid.
                -- PREVIOUSLY_RUN is left without one, matching `setCentreSaver`, and a named group keeps its own.
                IF grp.SAVE_AS_PART = '' THEN
                    new_uuid := gen_random_uuid()::text;
                    UPDATE ENTITY_CENTRE_CONFIG e
                       SET CONFIGUUID_ = new_uuid
                      FROM MOBILE_CONFIG m
                     WHERE m.ID = e._ID
                       AND m.OWNER_ID = grp.OWNER_ID
                       AND m.MENU_ID = grp.MENU_ID
                       AND m.SAVE_AS_PART = grp.SAVE_AS_PART
                       AND m.SURROGATE IN ('__________FRESH', '__________SAVED');
                END IF;

                -- A failure rolls back the changes of this block, but not these variables.
                -- They are therefore updated last, once nothing can fail any more.
                migrated := migrated + 1;
                IF grp.SAVE_AS_PART = '' THEN
                    uuids_assigned := uuids_assigned + 1;
                END IF;
            END IF;
        EXCEPTION WHEN OTHERS THEN
            -- An unforeseen failure skips this configuration only, and leaves it behind as a MOBILE orphan.
            skipped_error := skipped_error + 1;
            RAISE NOTICE 'Skipped owner [%], menu item [%], save-as part [%] due to: %',
                grp.OWNER_ID, grp.MENU_ID, grp.SAVE_AS_PART, SQLERRM;
        END;
    END LOOP;

    RAISE NOTICE 'Step 4: migrated % configuration(s), skipped % on conflict and % on error.',
        migrated, skipped_conflict, skipped_error;
    RAISE NOTICE 'Step 5: assigned a configUuid to % migrated default configuration(s).', uuids_assigned;

    -- Report the base users whose default configurations were left behind in step 4, one message per base user.
    -- After step 4, every MOBILE default of a base user is left behind, as such groups are never candidates.
    -- A centre counts as embedded in a master when its simple name contains `Master_`.
    -- Any other centre is reported as standalone or embedded, as its name alone cannot tell.
    -- Only FRESH rows are looked at, so that each default configuration is counted once.
    FOR report IN
        SELECT d.BASE_USER AS base_user,
               count(*) FILTER (WHERE NOT d.EMBEDDED) AS standalone_count,
               string_agg(d.CENTRE, ', ' ORDER BY d.CENTRE) FILTER (WHERE NOT d.EMBEDDED) AS standalone_centres,
               count(*) FILTER (WHERE d.EMBEDDED) AS embedded_count,
               string_agg(d.CENTRE, ', ' ORDER BY d.CENTRE) FILTER (WHERE d.EMBEDDED) AS embedded_centres
          FROM (SELECT u.KEY_ AS BASE_USER,
                       substring(mm.KEY_ from '[^.]*$') AS CENTRE,
                       position('Master_' in substring(mm.KEY_ from '[^.]*$')) > 0 AS EMBEDDED
                  FROM ENTITY_CENTRE_CONFIG e
                  JOIN USER_ u ON u._ID = e.ID_CRAFT AND u.BASE_ = 'Y'
                  JOIN MAIN_MENU mm ON mm._ID = e.ID_MAIN_MENU
                 WHERE e.TITLE = 'MOBILE__________FRESH__________DIFFERENCES') d
         GROUP BY d.BASE_USER
         ORDER BY d.BASE_USER
    LOOP
        RAISE NOTICE '%', 'Step 4: left behind defaults of base user [' || report.base_user || ']: '
            || report.standalone_count || ' standalone or embedded (' || coalesce(report.standalone_centres, '') || '), '
            || report.embedded_count || ' embedded in masters (' || coalesce(report.embedded_centres, '') || ').';
    END LOOP;
END $$;

-- Step 6.
-- Make the SAVED row of every migrated default configuration a copy of its FRESH row.
--
-- A default can never be saved in place, as SAVE on a default opens the `Save As` dialog.
-- Its SAVED row therefore only ever holds the empty diff, and did not matter while the configuration stayed a default.
-- As the named `Default (mobile)`, a configuration counts as changed whenever FRESH differs from SAVED.
-- Copying FRESH makes every migrated default start out unchanged, exactly as if the user had saved it with `Save As`.
-- Nothing user-authored is lost, and `Discard` no longer resets the mobile layout to the one the shared default shows.
--
-- The update below aligns an existing SAVED row.
-- NULL bodies, which the application never writes, are left alone.
-- `s.TITLE = m.NEW_TITLE` and `f.TITLE = mf.NEW_TITLE` select only rows whose group was actually migrated.
UPDATE ENTITY_CENTRE_CONFIG s
   SET BODY = f.BODY
  FROM MOBILE_CONFIG m,
       MOBILE_CONFIG mf
 INNER JOIN ENTITY_CENTRE_CONFIG f ON f._ID = mf.ID
 WHERE m.ID = s._ID
   AND f.ID_CRAFT = s.ID_CRAFT
   AND f.ID_MAIN_MENU = s.ID_MAIN_MENU
   AND m.SAVE_AS_PART = ''
   AND m.SURROGATE = '__________SAVED'
   AND s.TITLE = m.NEW_TITLE
   AND mf.SAVE_AS_PART = ''
   AND mf.SURROGATE = '__________FRESH'
   AND f.TITLE = mf.NEW_TITLE
   AND s.BODY <> f.BODY;

-- A MOBILE default often has no SAVED row at all.
-- As the named `Default (mobile)`, the application would recreate that row lazily without a uuid.
-- For a non-base user it would also copy the row from the base user's configuration of the same name.
-- The `Load` dialog would then find no SAVED row with the FRESH uuid, and report the configuration as orphaned.
-- The insert below therefore adds the missing row, carrying the FRESH uuid and body.
-- It has no description, flags or dashboard settings, as the application keeps those on FRESH only.
-- `e.TITLE = m.NEW_TITLE` selects only a default whose group was actually migrated, never a skipped one.
INSERT INTO ENTITY_CENTRE_CONFIG (
    _ID, _VERSION, ID_CRAFT, ID_MAIN_MENU, TITLE, BODY, CONFIGUUID_,
    IS_PRINCIPAL, PREFERRED_, PREFERREDONMOBILE_, DASHBOARDABLE_, RUNAUTOMATICALLY_
)
SELECT nextval('TG_ENTITY_ID_SEQ'),
       0,
       e.ID_CRAFT,
       e.ID_MAIN_MENU,
       '__________SAVED[Default (mobile)]__________DIFFERENCES',
       e.BODY,
       e.CONFIGUUID_,
       'N', 'N', 'N', 'N', 'N'
  FROM ENTITY_CENTRE_CONFIG e
 INNER JOIN MOBILE_CONFIG m ON m.ID = e._ID
 WHERE m.SAVE_AS_PART = ''
   AND m.SURROGATE = '__________FRESH'
   AND e.TITLE = m.NEW_TITLE
   AND NOT EXISTS (SELECT 1
                     FROM ENTITY_CENTRE_CONFIG s
                    WHERE s.ID_CRAFT = e.ID_CRAFT
                      AND s.ID_MAIN_MENU = e.ID_MAIN_MENU
                      AND s.TITLE = '__________SAVED[Default (mobile)]__________DIFFERENCES');

-- Step 7.
-- Make a migrated `Default (mobile)` preferred on mobile, where no other configuration is.
--
-- A mobile user without a preferred named configuration used to open the MOBILE default.
-- That default is now `Default (mobile)`, so marking it preferred on mobile keeps what such a user opens.
-- It also covers a user whose preferred named configuration was skipped, and is therefore no longer visible.
-- `e.TITLE = m.NEW_TITLE` selects only a default whose group was actually migrated, never a skipped one.
UPDATE ENTITY_CENTRE_CONFIG e
   SET PREFERREDONMOBILE_ = 'Y'
  FROM MOBILE_CONFIG m
 WHERE m.ID = e._ID
   AND m.SAVE_AS_PART = ''
   AND m.SURROGATE = '__________FRESH'
   AND e.TITLE = m.NEW_TITLE
   AND NOT EXISTS (SELECT 1
                     FROM ENTITY_CENTRE_CONFIG p
                    WHERE p.ID_CRAFT = e.ID_CRAFT
                      AND p.ID_MAIN_MENU = e.ID_MAIN_MENU
                      AND p.PREFERREDONMOBILE_ = 'Y'
                      AND left(p.TITLE, 6) <> 'MOBILE');

DROP TABLE pg_temp.MOBILE_CONFIG;
