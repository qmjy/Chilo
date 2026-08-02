/*
 * Copyright (c) 2023 QMJY.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *        https://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */

package io.github.qmjy.mapserver;

import com.graphhopper.GraphHopper;
import io.github.qmjy.mapserver.config.AppConfig;
import io.github.qmjy.mapserver.model.*;
import io.github.qmjy.mapserver.spec.TileJSON;
import lombok.Getter;
import lombok.Setter;
import org.geotools.api.data.FileDataStore;
import org.geotools.api.data.FileDataStoreFinder;
import org.geotools.api.feature.simple.SimpleFeature;
import org.geotools.data.geojson.GeoJSONReader;
import org.geotools.data.simple.SimpleFeatureIterator;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.MultiPolygon;
import org.locationtech.jts.geom.Polygon;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * 地图数据库服务工具
 */
@Component
public class MapServerDataCenter {

    private static final Logger logger = LoggerFactory.getLogger(MapServerDataCenter.class);


    /**
     * 瓦片数据库文件模型
     */
    @Getter
    private final Map<String, AbstractTile> tilesMap = new HashMap<>();
    /**
     * 不会再被加载的文件列表
     */
    private final Set<String> removedTiles = new HashSet<>();


    private final Map<String, FileDataStore> shpDataStores = new HashMap<>();

    /**
     * 字体文件模型
     */
    @Getter
    private final Map<String, FontsFileModel> fontsMap = new HashMap<>();

    /**
     * 行政区划数据。key:行政级别、value:区划对象列表
     */
    @Getter
    private final Map<Integer, List<SimpleFeature>> administrativeDivisionLevel = new HashMap<>();

    /**
     * 行政区划数据。key:区划ID、value:区划对象
     */
    @Getter
    private final Map<Integer, SimpleFeature> administrativeDivision = new HashMap<>();
    /**
     * 行政区划层级树
     */
    @Getter
    private AdministrativeDivisionNode simpleAdminDivision;

    @Getter
    private final Map<String, GraphHopper> hopperMap = new HashMap<>();

    @Getter
    @Setter
    private boolean mapnikReady = false;

    /**
     * 初始化完成后再启动扫描
     */
    @Getter
    @Setter
    private boolean initialized = false;


    /**
     * 初始化数据源
     *
     * @param mbtiles 待链接的数据库文件
     */
    public void initJdbcTemplate(File mbtiles) {
        if (!tilesMap.containsKey(mbtiles.getName())) {
            logger.info("Try to load tile of mbtiles: {}", mbtiles.getName());
            TileOfMbtiles dbFileModel = new TileOfMbtiles(mbtiles);
            if (dbFileModel.isValid()) {
                tilesMap.put(mbtiles.getName(), dbFileModel);
            }
        }
    }

    public void initTilesOfDir(File file) {
        if (!tilesMap.containsKey(file.getName())) {
            logger.info("Try to load tile from folder: {}", file.getName());
            TileOfDir dbFileModel = new TileOfDir(file);
            if (dbFileModel.isValid()) {
                tilesMap.put(file.getName(), dbFileModel);
            }
        }
    }


    public void indexArcgisTpk(File tilesetsFolder) {
        File[] files = tilesetsFolder.listFiles(pathname ->
                pathname.getName().endsWith(AppConfig.FILE_EXTENSION_NAME_TPK)
                        || pathname.getName().endsWith(AppConfig.FILE_EXTENSION_NAME_VTPK)
                        || pathname.getName().endsWith(AppConfig.FILE_EXTENSION_NAME_TPKX));
        if (files != null) {
            for (File tpk : files) {
                indexTpk(tpk);
            }
        }
    }

    /**
     * 预加载 tpk
     *
     * @param tpk tpk 文件
     */
    public void indexTpk(File tpk) {
        if (!tilesMap.containsKey(tpk.getName()) && !removedTiles.contains(tpk.getName())) {
            logger.info("Try to load tile of tpk: {}", tpk.getName());

            AbstractTile tileModel = null;

            if (tpk.getName().endsWith(".vtpk")) {
                VectorTileOfTpk dbFileModel = new VectorTileOfTpk(tpk);
                tileModel = dbFileModel;
                while (!dbFileModel.isLoaded()) {
                    try {
                        TimeUnit.MILLISECONDS.sleep(100);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }
            } else {
                RasterTileOfTpk dbFileModel = new RasterTileOfTpk(tpk);
                tileModel = dbFileModel;
                while (!dbFileModel.isLoaded()) {
                    try {
                        TimeUnit.MILLISECONDS.sleep(100);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }
            }

            if (tileModel.isValid()) {
                tilesMap.put(tpk.getName(), tileModel);
            } else {
                removedTiles.add(tpk.getName());
            }
        }
    }

    public void initShapefile(File shapefile) {
        FileDataStore dataStore = null;
        try {
            dataStore = FileDataStoreFinder.getDataStore(shapefile);
        } catch (IOException e) {
            logger.error("FileDataStoreFinder.getDataStore() failed: {}", shapefile.getAbsolutePath());
        }
        shpDataStores.put(shapefile.getName(), dataStore);
    }

    public FileDataStore getShpDataStores(String shapefile) {
        return shpDataStores.get(shapefile);
    }

    public void initHopper(String fileName, GraphHopper hopper) {
        hopperMap.put(fileName, hopper);
    }

    public void initMapnik(boolean ready) {
        setMapnikReady(ready);
    }


    /**
     * 初始化字体库文件
     *
     * @param fontFolder 字体文件目录
     */
    public void initFontsFile(File fontFolder) {
        fontsMap.put(fontFolder.getName(), new FontsFileModel(fontFolder));
    }

    /**
     * geojson格式的加载行政区划边界数据。
     *
     * @param boundary 行政区划边界
     */
    public void initBoundaryFile(File boundary) {
        try {
            GeoJSONReader reader = new GeoJSONReader(new FileInputStream(boundary));
            SimpleFeatureIterator features = reader.getFeatures().features();
            while (features.hasNext()) {
                SimpleFeature feature = features.next();

                administrativeDivision.put((int) feature.getAttribute("osm_id"), feature);

                int adminLevel = feature.getAttribute("admin_level") == null ? -1 : (int) feature.getAttribute("admin_level");
                if (administrativeDivisionLevel.containsKey(adminLevel)) {
                    List<SimpleFeature> simpleFeatures = administrativeDivisionLevel.get(adminLevel);
                    simpleFeatures.add(feature);
                } else {
                    ArrayList<SimpleFeature> value = new ArrayList<>();
                    value.add(feature);
                    administrativeDivisionLevel.put(adminLevel, value);
                }
            }
            features.close();
            packageModel();
        } catch (IOException e) {
            logger.error("Read OSM file failed：{}", boundary.getAbsolutePath());
        }
    }

    private void packageModel() {
        if (isOldVersion()) {
            administrativeDivision.values().forEach(feature -> {
                if (simpleAdminDivision == null) {
                    simpleAdminDivision = initRootNode(feature);
                } else {
                    Object parentsObj = feature.getAttribute("parents");
                    if (parentsObj != null) {
                        packageAdminTreeByParents(feature, parentsObj);
                    }
                }
            });
        } else {
            List<Integer> levelList = administrativeDivisionLevel.keySet().stream().sorted().toList();
            for (int i = 0; i < levelList.size(); i++) {
                Integer currentLevel = levelList.get(i);
                List<SimpleFeature> currentLevelFeatures = administrativeDivisionLevel.get(currentLevel);
                if (i == 0) {
                    //TODO 假设根节点只有一个
                    SimpleFeature first = currentLevelFeatures.getFirst();
                    simpleAdminDivision = new AdministrativeDivisionNode(first, -1);
                } else {
                    Integer upperLevel = levelList.get(i - 1);
                    List<SimpleFeature> upperLevelFeatures = administrativeDivisionLevel.get(upperLevel);

                    for (SimpleFeature currentLevelFeature : currentLevelFeatures) {
                        int osmId = getParentId(currentLevelFeature, upperLevelFeatures);
                        if (osmId != -1) {
                            appendToAdminNode(simpleAdminDivision, osmId, currentLevelFeature);
                        }
                    }
                }
            }
        }
    }

    private void appendToAdminNode(AdministrativeDivisionNode simpleAdminDivision, int osmId, SimpleFeature currentLevelFeature) {
        List<AdministrativeDivisionNode> children = simpleAdminDivision.getChildren();
        if (simpleAdminDivision.getId() == osmId) {
            children.add(new AdministrativeDivisionNode(currentLevelFeature, osmId));
            return;
        }
        for (AdministrativeDivisionNode child : children) {
            appendToAdminNode(child, osmId, currentLevelFeature);
        }
    }

    private int getParentId(SimpleFeature currentLevelFeature, List<SimpleFeature> upperLevelFeatures) {
        for (SimpleFeature upperLevelFeature : upperLevelFeatures) {
            Object geometry = upperLevelFeature.getAttribute("geometry");
            if (geometry instanceof Polygon || geometry instanceof MultiPolygon) {
                Geometry g = (Geometry) geometry;
                if (g.covers((Geometry) currentLevelFeature.getAttribute("geometry"))) {
                    return (int) upperLevelFeature.getAttribute("osm_id");
                }
            }
        }
        return -1;
    }


    /**
     * 抽取一个判断数据是否为老版本，老版本数据才有parents属性
     *
     * @return 是否为老版本数据
     */
    private boolean isOldVersion() {
        Map.Entry<Integer, SimpleFeature> next = administrativeDivision.entrySet().iterator().next();
        return next.getValue().getAttribute("parents") != null;
    }

    private void packageAdminTreeByParents(SimpleFeature feature, Object parentsObj) {
        String[] parents = parentsObj.toString().split(",");

        AdministrativeDivisionNode tempNode = new AdministrativeDivisionNode(feature, Integer.parseInt(parents[0]));

        for (int i = 0; i < parents.length; i++) {
            int parentId = Integer.parseInt(parents[i]);
            Optional<AdministrativeDivisionNode> nodeOpt = findNode(simpleAdminDivision, parentId);
            if (nodeOpt.isPresent()) {
                AdministrativeDivisionNode child = nodeOpt.get();
                //如果父节点已经在早期全路径时构造过了，则不需要再追加此单节点。
                if (!contains(child, (int) feature.getAttribute("osm_id"))) {
                    child.getChildren().add(tempNode);
                }
                break;
            } else {
                AdministrativeDivisionNode tmp = new AdministrativeDivisionNode(administrativeDivision.get(parentId), Integer.parseInt(parents[i + 1]));
                tmp.getChildren().add(tempNode);
                tempNode = tmp;
            }
        }
    }

    private boolean contains(AdministrativeDivisionNode child, int parentId) {
        for (AdministrativeDivisionNode item : child.getChildren()) {
            if (item.getId() == parentId) {
                return true;
            }
        }
        return false;
    }


    private AdministrativeDivisionNode initRootNode(SimpleFeature feature) {
        Object parents = feature.getAttribute("parents");
        if (parents == null) {
            return new AdministrativeDivisionNode(feature, -1);
        } else {
            String[] split = parents.toString().split(",");
            List<AdministrativeDivisionNode> children = new ArrayList<>();
            AdministrativeDivisionNode tmp = null;
            for (int i = 0; i < split.length; i++) {
                int osmId = Integer.parseInt(split[i]);
                if (i + 1 > split.length - 1) {
                    tmp = new AdministrativeDivisionNode(administrativeDivision.get(osmId), -1);
                    tmp.setChildren(children);
                } else {
                    tmp = new AdministrativeDivisionNode(administrativeDivision.get(osmId), Integer.parseInt(split[i + 1]));
                    tmp.setChildren(children);
                    children = new ArrayList<>();
                    children.add(tmp);
                }
            }
            return tmp;
        }
    }

    private Optional<AdministrativeDivisionNode> findNode(AdministrativeDivisionNode tmp, int parentId) {
        if (tmp.getId() == parentId) {
            return Optional.of(tmp);
        }
        List<AdministrativeDivisionNode> children = tmp.getChildren();
        for (AdministrativeDivisionNode item : children) {
            if (item.getId() == parentId) {
                return Optional.of(item);
            } else {
                Optional<AdministrativeDivisionNode> childOpt = findNode(item, parentId);
                if (childOpt.isPresent()) {
                    return childOpt;
                }
            }
        }
        return Optional.empty();
    }


    /**
     * 通过文件名获取数据源
     *
     * @param fileName 数据库文件名称
     * @return 数据库数据源
     */
    public Optional<JdbcTemplate> getDataSource(String fileName) {
        if (StringUtils.hasLength(fileName)) {
            AbstractTile model = tilesMap.get(fileName);
            if (model instanceof TileOfMbtiles m) {
                return Optional.of(m.getJdbcTemplate());
            }
        }
        return Optional.empty();
    }

    public void releaseDataSource(String fileName) {
        if (fileName.endsWith(AppConfig.FILE_EXTENSION_NAME_MBTILES)) {
            if (StringUtils.hasLength(fileName) && tilesMap.containsKey(fileName)) {
                AbstractTile tileSource = tilesMap.remove(fileName);
                tileSource.releaseResource();
            }
        }

        if (fileName.endsWith(AppConfig.FILE_EXTENSION_NAME_TPK) || fileName.endsWith(AppConfig.FILE_EXTENSION_NAME_TPKX) || fileName.endsWith(AppConfig.FILE_EXTENSION_NAME_VTPK)) {
            if (StringUtils.hasLength(fileName) && tilesMap.containsKey(fileName)) {
                AbstractTile tileSource = tilesMap.remove(fileName);
                tileSource.releaseResource();
            }
        }
    }

    /**
     * 返回瓦片文件对象
     *
     * @param fileName 瓦片集文件名称
     * @return 瓦片集文件对象
     */
    public AbstractTile getTilesFileModel(String fileName) {
        return tilesMap.get(fileName);
    }


    /**
     * 返回瓦片数据库文件的元数据
     *
     * @param fileName 瓦片数据库文件名
     * @return 瓦片元数据
     */
    public TileJSON getTileMetaData(String fileName) {
        if (StringUtils.hasLength(fileName)) {
            AbstractTile model = tilesMap.get(fileName);
            if (model != null) {
                return model.getTileJSON();
            }
        }
        return null;
    }


    /**
     * 获取字符文件目录
     *
     * @param fontName 字体文件名
     * @return 字体文件目录
     */
    public Optional<FontsFileModel> getFontFolder(String fontName) {
        if (StringUtils.hasLength(fontName)) {
            FontsFileModel fontsFileModel = fontsMap.get(fontName);
            if (fontsFileModel != null) {
                return Optional.of(fontsFileModel);
            }
        }
        return Optional.empty();
    }
}
