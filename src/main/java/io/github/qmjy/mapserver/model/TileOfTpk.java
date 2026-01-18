package io.github.qmjy.mapserver.model;

import com.esri.arcgisruntime.ArcGISRuntimeException;
import com.esri.arcgisruntime.arcgisservices.TileInfo;
import com.esri.arcgisruntime.data.TileCache;
import com.esri.arcgisruntime.geometry.Envelope;
import com.esri.arcgisruntime.loadable.LoadStatus;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.File;

@EqualsAndHashCode(callSuper = true)
@Data
public class TileOfTpk extends AbstractTile {
    private TileCache tileCache;
    private boolean loaded = false;

    public TileOfTpk(File tpk) {
        super(tpk.getAbsolutePath());
        this.fileLength = tpk.length();

        this.tileCache = new TileCache(tpk.getAbsolutePath());
        this.tileCache.loadAsync();
        this.tileCache.addDoneLoadingListener(() -> {
            if (tileCache.getLoadStatus() == LoadStatus.LOADED) {
                if (loadMetadata()) {
                    this.valid = true;
                }
            } else if (tileCache.getLoadStatus() == LoadStatus.FAILED_TO_LOAD) {
                ArcGISRuntimeException error = tileCache.getLoadError();
                if (error != null) {
                    logger.error("{} 加载失败原因: {}", tpk.getName(), error.getMessage());
                } else {
                    logger.error("加载失败，但未捕获到具体的错误对象。");
                }
            } else {
                logger.error("Load tpk failed: {}", tpk.getAbsolutePath());
            }
            this.loaded = true;
        });
    }

    @Override
    public boolean loadMetadata() {
        try {
            TileInfo tileInfo = tileCache.getTileInfo();
            metaDataMap.put("format", convertTileFormat(tileInfo.getFormat()));
            metaDataMap.put("minzoom", tileInfo.getLevelsOfDetail().getFirst().getLevel());
            metaDataMap.put("maxzoom", tileInfo.getLevelsOfDetail().getLast().getLevel());

            Envelope fullExtent = tileCache.getFullExtent();
            metaDataMap.put("bounds", fullExtent.getXMin() + "," + fullExtent.getYMin() + "," + fullExtent.getXMax() + "," + fullExtent.getYMax());
            return true;
        } catch (RuntimeException e) {
            logger.error("Read tpk failed: {}", e.getMessage());
            return false;
        }
    }

    private Object convertTileFormat(TileInfo.ImageFormat format) {
        return switch (format) {
            case TileInfo.ImageFormat.PNG, TileInfo.ImageFormat.PNG8, TileInfo.ImageFormat.PNG24, TileInfo.ImageFormat.PNG32 -> "png";
            case TileInfo.ImageFormat.JPG -> "jpg";
            // 指切片中可能包含不同格式的数据类型（例如某些区域为有损压缩，某些为无损压缩）。可能部分区域为 JPEG（基础底图），部分为 PNG（带透明度的叠加图层）。例如影像地图
            case TileInfo.ImageFormat.MIXED -> "jpg";
            //LERC（Limited Error Raster Compression）：是一种高效的压缩编码，特别适用于浮点型栅格数据（如高程、遥感影像等），它能在控制最大误差的同时实现高压缩比。
            case TileInfo.ImageFormat.LERC -> "tif";
            default -> "unknown";
        };
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
        return true;
    }
}
