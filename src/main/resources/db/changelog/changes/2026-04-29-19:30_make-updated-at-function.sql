--liquibase formatted sql
--changeset waazeved:make-updated-at-function context:new splitStatements:true endDelimiter:;

CREATE FUNCTION update_updated_at_column() RETURNS trigger AS
    '
    BEGIN
        NEW.updated_at
            = now();
        RETURN NEW;
    END;
'
    language plpgsql;


--rollback DROP FUNCTION IF EXISTS update_updated_at_column();