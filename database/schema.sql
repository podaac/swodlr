-- Create tables
CREATE TABLE "L2RasterProducts" (
    "id" uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    "definition" uuid NOT NULL,
    "currentStatus" uuid NOT NULL
);

CREATE TABLE "ProductHistory" (
    "requestedBy" uuid,
    "rasterProduct" uuid,
    "timestamp" timestamp with time zone NOT NULL DEFAULT current_timestamp,
    PRIMARY KEY ("requestedBy", "rasterProduct")
);

CREATE TABLE "RasterDefinitions" (
    "id" uuid PRIMARY KEY DEFAULT gen_random_uuid()
);

CREATE TABLE "Status" (
    "id" uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    "productID" uuid NOT NULL,
    "previousStatus" uuid,
    "timestamp" timestamp with time zone NOT NULL DEFAULT current_timestamp,
    "status" text NOT NULL
);

CREATE TABLE "Users" (
    "id" uuid PRIMARY KEY DEFAULT gen_random_uuid()
);

-- Create references
ALTER TABLE "L2RasterProducts"
    ADD FOREIGN KEY ("definition") REFERENCES "RasterDefinitions" (id),
    ADD FOREIGN KEY ("currentStatus") REFERENCES "Status" (id) DEFERRABLE INITIALLY DEFERRED;

ALTER TABLE "ProductHistory"
    ADD FOREIGN KEY ("requestedBy") REFERENCES "Users" (id),
    ADD FOREIGN KEY ("rasterProduct") REFERENCES "L2RasterProducts" (id);

ALTER TABLE "Status"
    ADD FOREIGN KEY ("productID") REFERENCES "L2RasterProducts" (id) DEFERRABLE INITIALLY DEFERRED,
    ADD FOREIGN KEY ("previousStatus") REFERENCES "Status" (id);
