-- Create types
CREATE TYPE state AS ENUM (
    'NEW', 'UNAVAILABLE', 'GENERATING',
    'ERROR', 'READY', 'AVAILABLE'
);

-- Create tables
CREATE TABLE "L2RasterProducts" (
    "id" uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    "definitionID" uuid NOT NULL
);

CREATE TABLE "ProductHistory" (
    "requestedByID" uuid,
    "rasterProductID" uuid,
    "timestamp" timestamp with time zone NOT NULL DEFAULT current_timestamp,
    PRIMARY KEY ("requestedByID", "rasterProductID")
);

CREATE TABLE "RasterDefinitions" (
    "id" uuid PRIMARY KEY DEFAULT gen_random_uuid()
);

CREATE TABLE "Status" (
    "id" uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    "productID" uuid NOT NULL,
    "timestamp" timestamp with time zone NOT NULL DEFAULT current_timestamp,
    "state" state NOT NULL,
    "reason" text
);

CREATE TABLE "Users" (
    "id" uuid PRIMARY KEY DEFAULT gen_random_uuid()
);

-- Create references
ALTER TABLE "L2RasterProducts"
    ADD FOREIGN KEY ("definitionID") REFERENCES "RasterDefinitions" (id);

ALTER TABLE "ProductHistory"
    ADD FOREIGN KEY ("requestedByID") REFERENCES "Users" (id),
    ADD FOREIGN KEY ("rasterProductID") REFERENCES "L2RasterProducts" (id);

ALTER TABLE "Status"
    ADD FOREIGN KEY ("productID") REFERENCES "L2RasterProducts" (id) DEFERRABLE INITIALLY DEFERRED;
