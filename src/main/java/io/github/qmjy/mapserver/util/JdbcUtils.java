/*
 * Copyright (c) 2024 QMJY.
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

package io.github.qmjy.mapserver.util;

import com.zaxxer.hikari.HikariDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.SingleConnectionDataSource;

import javax.sql.DataSource;
import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class JdbcUtils {

    private static final Logger logger = LoggerFactory.getLogger(JdbcUtils.class);
    private static final JdbcUtils INSTANCE = new JdbcUtils();

    private final Map<String, JdbcTemplate> jdbcTemplateMap = new ConcurrentHashMap<>();

    private JdbcUtils() {
    }

    public static JdbcUtils getInstance() {
        return INSTANCE;
    }

    /**
     * 创建外部JDBC链接
     *
     * @param filePath JDBC文件路径
     * @return JdbcTemplate
     */
    public JdbcTemplate getJdbcTemplate(String filePath) {
        String name = new File(filePath).getName();
        if (jdbcTemplateMap.containsKey(name)) {
            return jdbcTemplateMap.get(name);
        }

        try {
            String url = "jdbc:sqlite:file:" + filePath + "?readonly=true";
            Connection connection = DriverManager.getConnection(url);

            // SingleConnectionDataSource无需设置driver class name
            JdbcTemplate jdbc = new JdbcTemplate(new SingleConnectionDataSource(connection, true));
            jdbcTemplateMap.put(name, jdbc);
            return jdbc;
        } catch (SQLException e) {
            logger.error("创建SQLite连接失败: {}", filePath, e);
            return null;
        }
    }

    /**
     * 释放JDBC链接
     *
     * @param jdbcTemplate 待释放的JdbcTemplate
     * @param fileName     sqlite文件名称
     */
    public void releaseJdbcTemplate(JdbcTemplate jdbcTemplate, String fileName) {
        DataSource dataSource = jdbcTemplate.getDataSource();
        if (dataSource instanceof HikariDataSource ds) {
            ds.close();
            jdbcTemplateMap.remove(fileName);
        } else if (dataSource instanceof SingleConnectionDataSource scds) {
            scds.destroy();
            jdbcTemplateMap.remove(fileName);
        }
    }
}
