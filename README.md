# SWODLR - SWOT On-Demand Level 2 Raster Generation

SWODLR (swaa·dler) is an open-source software system developed to generate custom Level 2 raster data products for the [SWOT mission](https://swot.jpl.nasa.gov/). It provides an Application Programming Interface (API) and Graphical User Interface (GUI) that allows end-users to provide custom configurations to generate on-demand raster data products from underlying standard data products (PIXC, PIXCVec).

## Related Repositories

SWODLR is a collection of various services deployed to an Amazon Web Services (AWS) environment. Development on the individual components of the system is handled within each respective repository. Planning and documentation for the system as a whole is concentrated in this repository.

Deployable Components:
- https://github.com/podaac/swodlr-api
- https://github.com/podaac/swodlr-async-update
- https://github.com/podaac/swodlr-ingest-to-sds
- https://github.com/podaac/swodlr-raster-create
- https://github.com/podaac/swodlr-ui
- https://github.com/podaac/swodlr-user-notify

Libraries:
- https://github.com/podaac/swodlr-common-py
- https://github.com/podaac/swodlr-bootstrap
- https://github.com/podaac/swodlr-common-js
- https://github.com/podaac/swodlr-db-models
- https://github.com/podaac/swodlr-schemas

Other:
- https://github.com/podaac/swodlr-functional-tests
- https://github.com/podaac/swodlr-data-nao (potentially obsolete)
  
## Background

The SWOT Algorithm Development Team (ADT) developed the capability to generate raster products on-demand from underlying standard data products (PIXC, PIXCVec). In order to maintain a consistent end product, a second copy of the SWOT Science Data System (SDS) hosts the on-demand product generation capability. This system interfaces with the PO.DAAC-hosted swodlr system and end-users to provide a seamless experience.

The L2_HR_Raster Standard Data Products (SDPs) are available for all HR data collected by KaRIn. The SDPs are generated as NetCDF files with the following characteristics:

- Universal Transverse Mercator (UTM) projection grid
- 128 km x 128 km non-overlapping granule sizes
- Provided at each of 100 m and 250 m resolutions

## Inputs

The following inputs can be provided by the user:

- Granule extent flag: A flag indicating whether the raster should be produced as a non-overlapping or overlapping granule. `0` for a non-overlapping, 128 km x 128 km granule extent. `1` for an overlapping, 256 km x 128 km granule extent.

- Sampling grid type: Specifies the type of the raster sampling grid. It can be either a Universal Transverse Mercator (UTM) grid or a geodetic latitude-longitude grid.

- Raster resolution: Resolution of the raster sampling grid in units of integer meters for UTM grids and integer arc-seconds for latitude-longitude grids.

- UTM zone adjust: This parameter allows the UTM grid to use a zone within +/-1 zone of the closest zone to the center of the raster scene in order to allow nearby L2_HR_Raster outputs to be sampled on a common grid. This parameter has no effect if the output grid is not UTM.

- MGRS band adjust: This parameter allows the UTM grid to use a Military Grid Reference System (MGRS) latitude band within +/-1 band of the closest band to the center of the raster scene in order to allow nearby L2_HR_Raster outputs to be sampled on a common grid. This parameter has no effect if the output grid is not UTM.

## License

SWODLR is licensed under the [Apache License, Version 2.0](https://www.apache.org/licenses/LICENSE-2.0).
