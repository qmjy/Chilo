package io.github.qmjy.mapserver.spec;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 协议：<a href="https://github.com/mapbox/tilejson-spec/tree/master/3.0.0">TileJSON V3.0.0</a>
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class TileJSONV3 extends TileJSON {

    /**
     * REQUIRED. String.
     * <p>
     * A semver.org style version number as a string. Describes the version of the TileJSON spec that is implemented by this JSON object.
     */
    private String tilejson = "3.0.0";


    /**
     * OPTIONAL. Array. Default: [].
     * An array of data files in GeoJSON format. {z}, {x} and {y}, if present, are replaced with the corresponding integers.
     * If multiple endpoints are specified, clients may use any combination of endpoints.
     * All endpoints MUST return the same content for the same URL. If the array doesn't contain any entries, then no data is present in the map.
     * This field is for overlaying GeoJSON data on tiled raster maps and is generally no longer used for GL-based maps.
     */
    private String data;


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
}
