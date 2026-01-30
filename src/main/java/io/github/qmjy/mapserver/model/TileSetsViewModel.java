package io.github.qmjy.mapserver.model;

import io.github.qmjy.mapserver.spec.TileJSON;
import lombok.Data;

@Data
public class TileSetsViewModel {
    private final String name;
    private final long tilesCount;
    private final long fileLength;

    private final TileJSON tileJSON;

    public TileSetsViewModel(AbstractTile tilesFileModel) {
        this.name = tilesFileModel.getName();
        this.tilesCount = tilesFileModel.getTilesCount();
        this.fileLength = tilesFileModel.getFileLength();
        this.tileJSON = tilesFileModel.getTileJSON();
    }
}
