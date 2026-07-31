package io.github.qmjy.mapserver.service.impl;

import io.github.qmjy.mapserver.config.AppConfig;
import io.github.qmjy.mapserver.model.TileInfo;
import jakarta.annotation.PostConstruct;
import org.geotools.geopkg.GeoPackage;
import org.geotools.geopkg.TileEntry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Service
public class GeopackageTileService extends BaseTileService {
    private static final Logger LOGGER = LoggerFactory.getLogger(GeopackageTileService.class);

    private final String FOLDER_OF_GPKG = "gpkg";
    private final String FILE_EXTENSION_OF_GPKG = ".gpkg";

    private final AppConfig appConfig;

    private Map<String, GeoPackage> geoPackageMap = new HashMap<>();
    private Map<String, Map<String, TileEntry>> gpkgEntryMap = new HashMap<>();

    public GeopackageTileService(AppConfig appConfig) {
        this.appConfig = appConfig;
    }

    @PostConstruct
    @Override
    public void initialize() {
        File dataFolder = new File(appConfig.getDataPath());
        String[] gpkgs = new File(dataFolder, FOLDER_OF_GPKG).list((dir, name) -> name.toLowerCase(Locale.getDefault()).endsWith(FILE_EXTENSION_OF_GPKG));

        if (gpkgs != null) {
            for (String gpkg : gpkgs) {
                try {
                    GeoPackage geoPackage = new GeoPackage(new File(gpkg));
                    geoPackageMap.put(gpkg, geoPackage);

                    Map<String, TileEntry> tileEntries = new HashMap<>();
                    List<TileEntry> entries = geoPackage.tiles();
                    for (TileEntry entry : entries) {
                        String tableName = entry.getTableName();
                        tileEntries.put(tableName, entry);
                    }
                    gpkgEntryMap.put(gpkg, tileEntries);
                } catch (IOException e) {
                    LOGGER.error("解析geopackage失败：{}", gpkg);
                }
            }
        }
    }

    @Override
    public TileInfo getTile(int zoom, int x, int y) {
        return null;
    }

    @Override
    public byte[] getTileByte(int zoom, int x, int y) {
        return new byte[0];
    }

    private byte[] getTileByte(String layer, int zoom, int x, int y) {
        return new byte[0];
    }


    @Override
    public void cleanup() {
        geoPackageMap.values().forEach(GeoPackage::close);
    }
}
