package gov.nasa.podaac.swodlr.rasterdefinition;

import java.util.UUID;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import javax.persistence.Table;
import org.hibernate.validator.constraints.Range;

@Entity
@Table(name = "RasterDefinitions")
public class RasterDefinition {
  @Id
  public UUID id;

  /*
   * Flag indicating whether the SAS should produce a non-
   * overlapping or overlapping granule
   * 
   * false - a non-overlapping, 128 km x 128 km granule extent
   * true - an overlapping, 256 km x 128 km granule extent
   */
  @Column(nullable = false)
  public Boolean outputGranuleExtentFlag;

  /*
   * Type of the raster sampling grid
   */
  @Column(nullable = false)
  @Enumerated(EnumType.STRING)
  public GridType outputSamplingGridType;

  /*
   * Resolution of the raster sampling grid in units of integer
   * meters for UTM grids and integer arc-seconds for
   * latitude-longitude grids
   * 
   * - For UTM grids: (100, 125, 200, 250, 500, 1000, 2500, 5000,
   * 10000 meters
   * - For latitude-longitude grids: [3, 4, 5, 6, 8, 15, 30, 60,
   * 180, 300] arc-seconds
   * 
   * TODO: Validate based on value of outputSamplingGridType + valid values?
   */
  @Column(nullable = false)
  @Range(min = 3, max = 10000)
  public Integer rasterResolution;

  /*
   * This parameter allows the UTM grid to use a zone within
   * +/-1 zone of the closest zone to the center of the raster
   * scene in order to allow nearby L2 HR Raster outputs to
   * be sampled on a common grid. This parameter has no
   * effect if the output grid is not UTM.
   * 
   * TODO: Check if not null only on UTM grids?
   */
  @Column
  @Range(min = -1, max = 1)
  public Integer utmZoneAdjust;

  /*
   * This parameter allows the UTM grid to use an MGRS
   * latitude band within +/-1 band of the closest band to the
   * center of the raster scene in order to allow nearby
   * L2_HR_Raster outputs to be sampled on a common grid.
   * This parameter has no effect if the output grid is not
   * UTM.
   * 
   * TODO: Check if not null only on UTM grids?
   */
  @Column
  @Range(min = -1, max = 1)
  public Integer mgrsBandAdjust;

  public RasterDefinition() {
    id = UUID.randomUUID();
  }

  public UUID getId() {
    return id;
  }

  public String toString() {
    StringBuilder sb = new StringBuilder();

    sb.append("RasterDefinition (");
    sb.append("id = %s, ".formatted(this.id));
    sb.append("outputGranuleExtentFlag = %s, ".formatted(this.outputGranuleExtentFlag));
    sb.append("outputSamplingGridType = %s, ".formatted(this.outputSamplingGridType));
    sb.append("rasterResolution = %s, ".formatted(this.rasterResolution));
    sb.append("utmZoneAdjust = %s, ".formatted(this.utmZoneAdjust));
    sb.append("mgrsBandAdjust = %s".formatted(this.mgrsBandAdjust));
    sb.append(")");

    return sb.toString();
  }
}
