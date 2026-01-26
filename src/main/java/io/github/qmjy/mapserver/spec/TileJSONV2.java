package io.github.qmjy.mapserver.spec;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 协议：<a href="https://github.com/mapbox/tilejson-spec/tree/master/2.2.0">TileJSON V2.2.0</a>
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class TileJSONV2 extends TileJSON{
    /**
     * REQUIRED.
     * A semver.org style version number. Describes the version of the TileJSON spec that is implemented by this JSON object.
     */
    private String tilejson = "2.2.0";
}
