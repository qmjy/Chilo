package io.github.qmjy.mapserver.model;

import lombok.Data;

@Data
public class TileInfo {
    private int zoom;
    private int x;
    private int y;
    private byte[] data;
    private String format;
}
