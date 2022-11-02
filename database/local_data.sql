-- Data for testing locally
-- *** DO NOT DEPLOY TO UAT OR OPS ***

INSERT INTO "Users" ("id", "username") VALUES ('fee1dc78-0604-4fa6-adae-0b4b55440e7d', 'test@local.test');

INSERT INTO "RasterDefinitions" (
  "outputGranuleExtentFlag", "outputSamplingGridType", "rasterResolution", "utmZoneAdjust", "mgrsBandAdjust"
) VALUES (
  TRUE, 'UTM', 1000, 1, -1
);

INSERT INTO "RasterDefinitions" (
  "outputGranuleExtentFlag", "outputSamplingGridType", "rasterResolution"
) VALUES (
  TRUE, 'GEO', 3
);
