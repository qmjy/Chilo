package io.github.qmjy.mapserver.model;

import com.zaxxer.hikari.HikariDataSource;
import io.github.qmjy.mapserver.util.IOUtils;
import io.github.qmjy.mapserver.util.JdbcUtils;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;
import java.io.File;
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
        try {
            List<Map<String, Object>> mapList = jdbcTemplate.queryForList("SELECT * FROM metadata");
            for (Map<String, Object> map : mapList) {
                metaDataMap.put(String.valueOf(map.get("name")), map.get("value"));
            }
            return true;
        } catch (DataAccessException e) {
            logger.error("Load map meta data failed: {}", filePath);
            return false;
        }
    }

    @Override
    public int getTileFileType() {
        return TILE_FILE_TYPE_OF_MBTILES;
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
