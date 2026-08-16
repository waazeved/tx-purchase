--liquibase formatted sql
--changeset waazeved:make-purchase-table context:new splitStatements:true endDelimiter:;

CREATE TABLE purchase
(
    id              UUID           NOT NULL,
    created_at      TIMESTAMP      NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMP      NOT NULL DEFAULT NOW(),
    description     VARCHAR(50)    NOT NULL,
    date       DATE           NOT NULL,
    amount DECIMAL(19, 2) NOT NULL,
    test_2 TEXT,
    CONSTRAINT purchase_pk PRIMARY KEY (id)
);

CREATE TRIGGER purchase_update_updated_at_column
    BEFORE UPDATE
    ON purchase
    FOR EACH ROW
    EXECUTE PROCEDURE update_updated_at_column();

-- rollback DROP TABLE purchase;