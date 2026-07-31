package io.github.qmjy.mapserver.service.impl;

import io.github.qmjy.mapserver.service.TileService;

public abstract class BaseTileService implements TileService {
    /**
     * 检测图片格式
     */
    private String detectImageFormat(byte[] data) {
        if (data.length > 4) {
            // PNG magic number: 137 80 78 71
            if (data[0] == (byte) 0x89 && data[1] == (byte) 0x50 &&
                    data[2] == (byte) 0x4E && data[3] == (byte) 0x47) {
                return "image/png";
            }
            // JPEG magic number: 255 216
            if ((data[0] & 0xFF) == 0xFF && (data[1] & 0xFF) == 0xD8) {
                return "image/jpeg";
            }
            // WebP magic number
            if (data[0] == (byte) 0x52 && data[1] == (byte) 0x49 &&
                    data[2] == (byte) 0x46 && data[3] == (byte) 0x46) {
                return "image/webp";
            }
        }
        return "image/png"; // 默认返回PNG
    }
}
