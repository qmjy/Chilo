package io.github.qmjy.mapserver.service.impl;

import io.github.qmjy.mapserver.config.AppConfig;
import io.github.qmjy.mapserver.model.TileInfo;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.Locale;

@Service
public class MbtilesTileService extends BaseTileService {
    private static final Logger LOGGER = LoggerFactory.getLogger(MbtilesTileService.class);

    private final String FOLDER_OF_TILESET = "tilesets";
    private final String FILE_EXTENSION_OF_MBTILES = ".mbtiles";

    private final AppConfig appConfig;

    public MbtilesTileService(AppConfig appConfig) {
        this.appConfig = appConfig;
    }

    @PostConstruct
    @Override
    public void initialize() {
           File dataFolder = new File(appConfig.getDataPath());                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                       String[] mbtiles = new File(dataFolder, FOLDER_OF_TILESET).list((dir, name) -> name.toLowerCase(Locale.getDefault()).endsWith(FILE_EXTENSION_OF_MBTILES));
    }

    @Override
    public TileInfo getTile(int zoom, int x, int y) {
        return null;
    }

    @Override
    public byte[] getTileByte(int zoom, int x, int y) {
        return new byte[0];
    }

    @Override
    public void cleanup() {

    }
}
