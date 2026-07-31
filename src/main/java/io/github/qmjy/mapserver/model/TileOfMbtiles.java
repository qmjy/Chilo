package io.github.qmjy.mapserver.model;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zaxxer.hikari.HikariDataSource;
import io.github.qmjy.mapserver.spec.TileJSONV3;
import io.github.qmjy.mapserver.spec.VectorLayer;
import io.github.qmjy.mapserver.util.IOUtils;
import io.github.qmjy.mapserver.util.JdbcUtils;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.ArrayUtils;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;
import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
public class TileOfMbtiles extends AbstractTile {

    private JdbcTemplate jdbcTemplate;

    public TileOfMbtiles(File file, String className) {
        super(file.getAbsolutePath());
        this.fileLength = file.length();
        initJdbc(className, file);
        if (loadMetadata()) {
            countSize();
            this.isGzip = isCompressed();
            this.valid = true;
        }
    }

    @Override
    public boolean loadMetadata() {
        this.tileJSON = new TileJSONV3();

        try {
            List<Map<String, Object>> mapList = jdbcTemplate.queryForList("SELECT * FROM metadata");
            for (Map<String, Object> map : mapList) {
                String key = map.get("name").toString();
                String value = map.get("value").toString();
                switch (key) {
                    case "name":
                        tileJSON.setName(value);
                        break;
                    case "description":
                        tileJSON.setDescription(value);
                        break;
                    case "version":
                        tileJSON.setVersion(value);
                        break;
                    case "center":
                        Float[] array = Arrays.stream(value.split(",")).map(Float::valueOf).toArray(Float[]::new);
                        tileJSON.setCenter(ArrayUtils.toPrimitive(array));
                        break;
                    case "attribution":
                        tileJSON.setAttribution(value);
                        break;
                    case "scheme":
                        tileJSON.setScheme(value);
                        break;
                    case "minzoom":
                        tileJSON.setMinzoom(Integer.parseInt(value));
                        break;
                    case "maxzoom":
                        tileJSON.setMaxzoom(Integer.parseInt(value));
                        break;
                    case "json":
                        tileJSON.setVector_layers(convertVec(value));
                        break;
                    case "bounds":
                        Float[] floatObjArray = Arrays.stream(value.split(","))
                                .map(Float::valueOf).toArray(Float[]::new);
                        tileJSON.setBounds(ArrayUtils.toPrimitive(floatObjArray));
                        break;
                    case "format":
                        tileJSON.setFormat(value);
                        break;
                }
            }
            return true;
        } catch (DataAccessException e) {
            logger.error("Load map meta data failed: {}", filePath);
            return false;
        }
    }

    private List<VectorLayer> convertVec(String value) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            Map<String, Object> rootMap = objectMapper.readValue(value, new TypeReference<Map<String, Object>>() {
            });
            Object layersObj = rootMap.get("vector_layers");
            // 将 vector_layers 数组转换为 List<VectorLayer>
            return objectMapper.convertValue(layersObj, new TypeReference<List<VectorLayer>>() {
                    }
            );
        } catch (Exception e) {
            logger.error("Parse vector_layers failed: {}", filePath);
            return null;
        }
    }

    @Override
    public int getTileFileType() {
        return TILE_FILE_TYPE_OF_MBTILES;
    }

    @Override
    public byte[] getTile(int zoom, int x, int y) {
        return new byte[0];
    }

    @Override
    public void countSize() {
        String sql = "SELECT COUNT(*) AS count FROM tiles";
        Map<String, Object> result = jdbcTemplate.queryForMap(sql);
        tilesCount = (int) result.get("count");
    }

    @Override
    public boolean isCompressed() {
        String sql = "SELECT tile_data FROM tiles limit 1";
        byte[] data = jdbcTemplate.queryForObject(sql, (rs, rowNum) -> rs.getBytes(1));
        if (data == null || data.length < 2) {
            return false;
        }
        return IOUtils.isGZIP(data);
    }

    @Override
    public boolean releaseResource() {
        //执行检查点操作，将所有WAL内容写入主数据库文件
        jdbcTemplate.execute("PRAGMA wal_checkpoint(FULL)");
        DataSource dataSource = jdbcTemplate.getDataSource();
        if (dataSource instanceof HikariDataSource hikariDataSource) {
            hikariDataSource.close();
        }
        return true;
    }

    private void initJdbc(String className, File file) {
        this.jdbcTemplate = JdbcUtils.getInstance().getJdbcTemplate(className, file.getAbsolutePath());
    }
}
