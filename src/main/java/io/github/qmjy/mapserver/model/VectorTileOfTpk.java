package io.github.qmjy.mapserver.model;

import com.esri.arcgisruntime.ArcGISRuntimeException;
import com.esri.arcgisruntime.arcgisservices.VectorTileSourceInfo;
import com.esri.arcgisruntime.data.VectorTileCache;
import com.esri.arcgisruntime.geometry.Envelope;
import com.esri.arcgisruntime.loadable.LoadStatus;
import io.github.qmjy.mapserver.spec.TileJSONV3;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

@EqualsAndHashCode(callSuper = true)
@Data
public class VectorTileOfTpk extends AbstractTile {
    private VectorTileCache tileCache;
    private ZipFile zipFile;
    private boolean loaded = false;

    public VectorTileOfTpk(File tpk) {
        super(tpk.getAbsolutePath());
        this.fileLength = tpk.length();

        this.tileCache = new VectorTileCache(tpk.getAbsolutePath());
        this.tileCache.loadAsync();
        this.tileCache.addDoneLoadingListener(() -> {
            if (tileCache.getLoadStatus() == LoadStatus.LOADED) {
                logger.info("The vtpk file is loaded: {}", tpk.getName());
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
                logger.error("Load vtpk failed: {}", tpk.getAbsolutePath());
            }
            this.loaded = true;
        });

        try {
            this.zipFile = new ZipFile(tpk);
        } catch (Exception e) {
            throw new RuntimeException("Failed to open VTPK as Zip", e);
        }
    }

    @Override
    public boolean loadMetadata() {
        this.tileJSON = new TileJSONV3();
        try {
            VectorTileSourceInfo sourceInfo = tileCache.getSourceInfo();

            sourceInfo.getDefaultStyle();
            sourceInfo.getOrigin();
            sourceInfo.getName();
            sourceInfo.getDefaultStyleUri();

            tileJSON.setMinzoom(sourceInfo.getLevelsOfDetail().getFirst().getLevel());
            tileJSON.setMaxzoom(sourceInfo.getLevelsOfDetail().getLast().getLevel());

            Envelope fullExtent = tileCache.getSourceInfo().getFullExtent();
            tileJSON.setBounds(new float[]{(float) fullExtent.getXMin(), (float) fullExtent.getYMin(), (float) fullExtent.getXMax(), (float) fullExtent.getYMax()});
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
    public byte[] getTile(int zoom, int x, int y) {
        // VTPK 内部结构通常为: p12/tiles/L{z}/R{y}/C{x}.pbf
        // 注意：有些 VTPK 路径可能是 v1/tiles/...
        // 这里的路径格式取决于 ArcGIS Pro 打包时的版本

        // 尝试标准路径格式 (ArcGIS Online / Pro 默认)
        //String tilePath = String.format("p12/tiles/L%02d/R%08x/C%08x.pbf", zoom, y, x);
        String tilePath = String.format("p12/tile/L%02d/R%08x/C%08x.pbf", zoom, y, x);

        // 如果是较旧或扁平化的结构，可能是:
        // String tilePath = "tile/" + z + "/" + y + "/" + x + ".pbf";

        try {
            ZipEntry entry = zipFile.getEntry(tilePath);
            if (entry == null) {
                // 某些 vtpk 内部路径不带 p12 前缀，进行模糊匹配搜索
                // 或者在构造时读取 root.json 确定路径模板
                return null;
            }

            try (InputStream is = zipFile.getInputStream(entry)) {
                return is.readAllBytes();
            }
        } catch (Exception e) {
            System.err.println("Error reading tile " + zoom + "/" + x + "/" + y + ": " + e.getMessage());
            return null;
        }
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
        if (zipFile != null) {
            try {
                zipFile.close();
            } catch (IOException e) {
                return false;
            }
        }
        return true;
    }
}
