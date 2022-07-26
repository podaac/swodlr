-- Create tables
CREATE TABLE "Users" (
    "id" uuid DEFAULT gen_random_uuid() PRIMARY KEY,
    "username" varchar UNIQUE NOT NULL
);

CREATE TABLE "RasterDefinitions" (
    "id" uuid DEFAULT gen_random_uuid() PRIMARY KEY,
    "outputGranuleExtentFlag" boolean NOT NULL,
    "outputSamplingGridType" varchar NOT NULL,
    "rasterResolution" int NOT NULL,
    "utmZoneAdjust" int,
    "mgrsBandAdjust" int
);

CREATE TABLE "L2RasterProducts" (
    "id" uuid DEFAULT gen_random_uuid() PRIMARY KEY,
    "definitionID" uuid NOT NULL,
    "cycle" int NOT NULL,
    "pass" int NOT NULL,
    "scene" int NOT NULL,
    FOREIGN KEY ("definitionID") REFERENCES "RasterDefinitions" ("id")
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
