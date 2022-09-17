-- Create tables
CREATE TABLE "Users" (
    "id" uuid DEFAULT gen_random_uuid() PRIMARY KEY
);

CREATE TABLE "RasterDefinitions" (
    "id" uuid DEFAULT gen_random_uuid() PRIMARY KEY
);

CREATE TABLE "L2RasterProducts" (
    "id" uuid DEFAULT gen_random_uuid() PRIMARY KEY,
    "definitionID" uuid NOT NULL,
    FOREIGN KEY ("definitionID") REFERENCES "RasterDefinitions" ("id") ON DELETE CASCADE
);

CREATE TABLE "ProductHistory" (
    "requestedByID" uuid,
    "rasterProductID" uuid,
    "timestamp" timestamp with time zone NOT NULL DEFAULT current_timestamp,
    PRIMARY KEY ("requestedByID", "rasterProductID"),
    FOREIGN KEY ("requestedByID") REFERENCES "Users" ("id"),
    FOREIGN KEY ("rasterProductID") REFERENCES "L2RasterProducts" ("id") ON DELETE CASCADE
);

CREATE TABLE "Status" (
    "id" uuid DEFAULT gen_random_uuid() PRIMARY KEY,
    "productID" uuid NOT NULL,
    "timestamp" timestamp with time zone NOT NULL DEFAULT current_timestamp,
    "state" varchar NOT NULL,
    "reason" text,
    FOREIGN KEY ("productID") REFERENCES "L2RasterProducts" ("id") ON DELETE CASCADE
);
