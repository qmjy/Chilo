package io.github.qmjy.mapserver.spec;

import lombok.Data;

/**
 * 协议：https://github.com/mapbox/tilejson-spec/tree/master/3.0.0
 */
@Data
public class TileJSONV3 {

    /**
     * REQUIRED. String.
     * <p>
     * A semver.org style version number as a string. Describes the version of the TileJSON spec that is implemented by this JSON object.
     */
    private String tilejson = "3.0.0";

    /**
     * REQUIRED. String.
     * An array of tile endpoints. {z}, {x} and {y}, if present, are replaced with the corresponding integers.
     * If multiple endpoints are specified, clients may use any combination of endpoints. All endpoint urls MUST be absolute.
     * All endpoints MUST return the same content for the same URL. The array MUST contain at least one endpoint. The tile extension is NOT limited to any particular format.
     * Some of the more popular are: mvt, vector.pbf, png, webp, and jpg.
     */
    private String[] tiles;

    private VectorLayer[] vector_layers;

    /**
     * OPTIONAL. String. Default: null.
     * Contains an attribution to be displayed when the map is shown to a user.
     * Implementations MAY decide to treat this as HTML or literal text.
     * For security reasons, make absolutely sure that this content can't be abused as a vector for XSS or beacon tracking.
     */
    private String attribution;

    /**
     * OPTIONAL. Array. Default: [ -180, -85.05112877980659, 180, 85.0511287798066 ] (xyz-compliant tile bounds)
     * The maximum extent of available map tiles.
     * Bounds MUST define an area covered by all zoom levels.
     * The bounds are represented in WGS 84 latitude and longitude values, in the order left, bottom, right, top. Values may be integers or floating point numbers.
     * The minimum/maximum values for longitude and latitude are -180/180 and -90/90 respectively. Bounds MUST NOT "wrap" around the ante-meridian.
     * If bounds are not present, the default value MAY assume the set of tiles is globally distributed.
     */
    private float[] bounds;


    /**
     * OPTIONAL. Array. Default: null.
     * The first value is the longitude, the second is latitude (both in WGS:84 values), the third value is the zoom level as an integer.
     * Longitude and latitude MUST be within the specified bounds. The zoom level MUST be between minzoom and maxzoom.
     * Implementations MAY use this center value to set the default location.
     * If the value is null, implementations MAY use their own algorithm for determining a default location.
     */
    private float[] center;

    /**
     * OPTIONAL. Array. Default: [].
     * An array of data files in GeoJSON format. {z}, {x} and {y}, if present, are replaced with the corresponding integers.
     * If multiple endpoints are specified, clients may use any combination of endpoints.
     * All endpoints MUST return the same content for the same URL. If the array doesn't contain any entries, then no data is present in the map.
     * This field is for overlaying GeoJSON data on tiled raster maps and is generally no longer used for GL-based maps.
     */
    private String data;

    /**
     * OPTIONAL. String. Default: null.
     * A text description of the set of tiles. The description can contain any valid unicode character as described by the JSON specification RFC 8259.
     */
    private String description;


    /**
     * OPTIONAL. Integer. Default: null.
     * An integer specifying the zoom level from which to generate overzoomed tiles.
     * Implementations MAY generate overzoomed tiles from parent tiles if the requested zoom level does not exist.
     * In most cases, overzoomed tiles are generated from the maximum zoom level of the set of tiles. If fillzoom is specified, the overzoomed tile MAY be generated from the fillzoom level.
     * For example, in a set of tiles with maxzoom 10 and no fillzoom specified, a request for a z11 tile will use the z10 parent tiles to generate the new, overzoomed z11 tile.
     * If the same TileJSON object had fillzoom specified at z7, a request for a z11 tile would use the z7 tile instead of z10.
     * While TileJSON may specify rules for overzooming tiles, it is ultimately up to the tile serving client or renderer to implement overzooming.
     */
    private int fillzoom;


    private String[] grids;


    /**
     * OPTIONAL. String. Default: null.
     * Contains a legend to be displayed with the map.
     * Implementations MAY decide to treat this as HTML or literal text.
     * For security reasons, make absolutely sure that this field can't be abused as a vector for XSS or beacon tracking.
     */
    private String legend;

    /**
     * OPTIONAL. Integer. Default: 0.
     * An integer specifying the minimum zoom level. MUST be in range: 0 <= minzoom <= maxzoom <= 30.
     */
    private int minzoom;

    /**
     * OPTIONAL. Integer. Default: 30.
     * An integer specifying the maximum zoom level. MUST be in range: 0 <= minzoom <= maxzoom <= 30.
     * A client or server MAY request tiles outside the zoom range,
     * but the availability of these tiles is dependent on how the tile server or renderer handles the request (such as overzooming tiles).
     */
    private int maxzoom;

    /**
     * OPTIONAL. String. Default: null.
     * A name describing the set of tiles. The name can contain any legal character. Implementations SHOULD NOT interpret the name as HTML.
     */
    private String name;


    /**
     * OPTIONAL. String. Default: "xyz".
     * Either "xyz" or "tms". Influences the y direction of the tile coordinates. The global-mercator (aka Spherical Mercator) profile is assumed.
     */
    private String scheme;


    /**
     * OPTIONAL. String. Default: null.
     * Contains a mustache template to be used to format data from grids for interaction.
     * See https://github.com/mapbox/utfgrid-spec/tree/master/1.2 for the interactivity specification.
     */
    private String template;

    /**
     * OPTIONAL. String. Default: "1.0.0".
     * A semver.org style version number of the tiles.
     * When changes across tiles are introduced the minor version MUST change.
     * This may lead to cut off labels. Therefore, implementors can decide to clean their cache when the minor version changes.
     * Changes to the patch level MUST only have changes to tiles that are contained within one tile.
     * When tiles change significantly, such as updating a vector tile layer name, the major version MUST be increased.
     * Implementations MUST NOT use tiles with different major versions.
     */
    private String version;
}
