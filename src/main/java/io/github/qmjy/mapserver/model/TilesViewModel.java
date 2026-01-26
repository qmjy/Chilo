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

package io.github.qmjy.mapserver.model;

import io.github.qmjy.mapserver.spec.TileJSON;
import io.github.qmjy.mapserver.spec.TileJSONV2;
import io.github.qmjy.mapserver.spec.TileJSONV3;
import lombok.Getter;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

@Getter
public class TilesViewModel {
    private final String name;
    private final String type;
    private final String fileSize;
    private TileJSONV3 tileJSONV3;
    private TileJSONV2 tileJSONV2;

    /**
     * 构造方法
     *
     * @param file 瓦片文件
     */
    public TilesViewModel(File file) {
        String fileName = file.getName();
        this.name = fileName;
        this.type = file.isDirectory() ? "" : fileName.substring(fileName.lastIndexOf("."));
        this.fileSize = file.isDirectory() ? "" : formatFileSize(file.length());
    }

    public TilesViewModel(File file, TileJSON tileJSON) {
        this(file);

        if (tileJSON instanceof TileJSONV3 v3) {
            this.tileJSONV3 = v3;
        }

        if (tileJSON instanceof TileJSONV2 v2) {
            this.tileJSONV2 = v2;
        }
    }


    /**
     * 文件大小智能转换
     * 会将文件大小转换为最大满足单位
     *
     * @param size（文件大小，单位为B）
     * @return 文件大小
     */
    public static String formatFileSize(Long size) {
        String sizeName = null;
        if (1024 * 1024 > size && size >= 1024) {
            sizeName = String.format("%.2f", size.doubleValue() / 1024) + "KB";
        } else if (1024 * 1024 * 1024 > size && size >= 1024 * 1024) {
            sizeName = String.format("%.2f", size.doubleValue() / (1024 * 1024)) + "MB";
        } else if (size >= 1024 * 1024 * 1024) {
            sizeName = String.format("%.2f", size.doubleValue() / (1024 * 1024 * 1024)) + "GB";
        } else {
            sizeName = size.toString() + "B";
        }
        return sizeName;
    }
}
