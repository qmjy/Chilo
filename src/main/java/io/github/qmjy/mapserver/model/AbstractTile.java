package io.github.qmjy.mapserver.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.github.qmjy.mapserver.spec.TileJSON;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;


@Data
@AllArgsConstructor
@NoArgsConstructor
public abstract class AbstractTile {
    @JsonIgnore
    protected final Logger logger = LoggerFactory.getLogger(getClass());

    // 瓦片数据包文件格式类型
    public static final int TILE_FILE_TYPE_OF_MBTILES = 1;
    public static final int TILE_FILE_TYPE_OF_TPK = 2;
    public static final int TILE_FILE_TYPE_OF_VTPK = 3;
    public static final int TILE_FILE_TYPE_OF_DIRECTORY = 4;

    protected TileJSON tileJSON;

    protected String name;
    protected long tilesCount = -1;
    protected long fileLength = 0L;
    //maptiler的数据是gzip压缩；bbbike的未被压缩；
    protected boolean isGzip = false;

    @JsonIgnore
    protected String filePath = "";

    //文件是否有效：文件能够解析且解析成功则会置为true
    @JsonIgnore
    protected boolean valid = false;

    protected AbstractTile(String filePath) {
        this.filePath = filePath;
        this.name = new File(filePath).getName();
    }

    public abstract boolean loadMetadata();

    public abstract int getTileFileType();

    /**
     * 根据 Zoom/X/Y 查询瓦片数据
     *
     * @param zoom 层级
     * @param x    列
     * @param y    行
     * @return 原始 PBF 字节流，如果不存在则返回 null
     */
    public abstract byte[] getTile(int zoom, int x, int y);

    public abstract void countSize();

    public abstract boolean isCompressed();

    public abstract boolean releaseResource();
}
