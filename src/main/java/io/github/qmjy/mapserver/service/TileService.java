package io.github.qmjy.mapserver.service;

import io.github.qmjy.mapserver.model.TileInfo;

public interface TileService {

    /**
     * 初始化方法调用
     */
    void initialize();

    /**
     * 获取瓦片信息
     *
     * @param zoom 地图缩放层级
     * @param x    行
     * @param y    列
     * @return 瓦片信息
     */
    TileInfo getTile(int zoom, int x, int y);

    /**
     * 获取瓦片二进制数据
     *
     * @param zoom 地图缩放层级
     * @param x    行
     * @param y    列
     * @return 瓦片数据流
     */
    byte[] getTileByte(int zoom, int x, int y);

    /**
     * 释放资源
     */
    void cleanup();
}
