-- SESSION 14 ROLLBACK
-- Run this against Supabase if you need to revert all Session 14 DB changes.
-- Safe to run multiple times (IF EXISTS guards).

DROP TABLE IF EXISTS missing_information CASCADE;
DROP TABLE IF EXISTS ae_case CASCADE;
DROP TABLE IF EXISTS copay_case CASCADE;
DROP TABLE IF EXISTS pap_case CASCADE;
DROP TABLE IF EXISTS bi_case CASCADE;

-- Remove Flyway history entry so the migration can be re-run if needed
DELETE FROM flyway_schema_history
WHERE script LIKE '%add_child_case_stubs%';
