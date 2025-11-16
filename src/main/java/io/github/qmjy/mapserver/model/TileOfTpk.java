package io.github.qmjy.mapserver.model;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.geotools.api.geometry.Position;
import org.geotools.tpk.TPKFile;
import org.geotools.tpk.TPKZoomLevel;

import java.io.File;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

@EqualsAndHashCode(callSuper = true)
@Data
public class TileOfTpk extends AbstractTile {
    private TPKFile tpkFile;
    private Map<Long, TPKZoomLevel> zoomLevelMap;

    public TileOfTpk(File tpk) {
        super(tpk.getAbsolutePath());
        this.fileLength = tpk.length();
        this.zoomLevelMap = new HashMap<>();
        if (loadMetadata()) {
            this.valid = true;
            this.isGzip = isCompressed();
        }
    }

    @Override
    public boolean loadMetadata() {
        try {
            tpkFile = new TPKFile(new File(filePath), zoomLevelMap);

            if ("MIXED".equals(tpkFile.getImageFormat())) {
                return false;
            }

            metaDataMap.put("format", tpkFile.getImageFormat().toLowerCase(Locale.getDefault()));
            metaDataMap.put("minzoom", tpkFile.getMinZoomLevel());
            metaDataMap.put("maxzoom", tpkFile.getMaxZoomLevel());

            Position lowerCorner = tpkFile.getBounds().getLowerCorner();
            Position upperCorner = tpkFile.getBounds().getUpperCorner();

            metaDataMap.put("bounds", lowerCorner.getCoordinate()[0] + ","
                    + lowerCorner.getCoordinate()[1] + ","
                    + upperCorner.getCoordinate()[0] + ","
                    + upperCorner.getCoordinate()[1]);
            return true;
        } catch (RuntimeException e) {
            logger.error("Read tpk failed: {}", e.getMessage());
            return false;
        }
    }

    @Override
    public int getTileFileType() {
        return TILE_FILE_TYPE_OF_TPK;
    }

    @Override
    public void countSize() {
        // TODO: 实现 TPK 瓦片计数逻辑
    }

    @Override
    public boolean isCompressed() {
        // TODO: 实现 TPK 压缩检查逻辑
        return false;
    }

    @Override
    public boolean releaseResource() {
        tpkFile = null;
        zoomLevelMap.clear();
        return true;
    }
}
