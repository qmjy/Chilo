package io.github.qmjy.mapserver.model;

import lombok.Data;

@Data
public class TileSetsViewModel {
    private final String name;
    private final long tilesCount;
    private final long fileLength;
    private final String format;

    public TileSetsViewModel(AbstractTile tilesFileModel) {
        this.name = tilesFileModel.getName();
        this.tilesCount = tilesFileModel.getTilesCount();
        this.fileLength = tilesFileModel.getFileLength();
        this.format = (String) tilesFileModel.getMetaDataMap().get("format");
    }
}
