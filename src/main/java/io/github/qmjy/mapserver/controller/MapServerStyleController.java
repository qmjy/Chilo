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

package io.github.qmjy.mapserver.controller;

import io.github.qmjy.mapserver.config.AppConfig;
import io.github.qmjy.mapserver.util.FileUtils;
import io.github.qmjy.mapserver.util.SystemUtils;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.FileCopyUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Optional;

@RestController
@RequestMapping("/api/styles")
@Tag(name = "Maplibre 样式服务管理", description = "Maplibre 离线服务接口能力")
public class MapServerStyleController {
    private final AppConfig appConfig;

    public MapServerStyleController(AppConfig appConfig) {
        this.appConfig = appConfig;
    }


    /**
     * 加载样式
     *
     * @return 样式内容
     */
    @ResponseBody
    @GetMapping(value = "/{styleName}", produces = "application/json")
    public ResponseEntity<String> loadStyle(@PathVariable String styleName) {
        if (SystemUtils.checkTilesetName(styleName)) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        if (StringUtils.hasLength(appConfig.getDataPath())) {
            Optional<File> safeFileOfStyle = FileUtils.getInstance(appConfig).getSafeFileOfStyle(styleName);
            if (safeFileOfStyle.isPresent()) {
                File file = safeFileOfStyle.get();
                try {
                    String styleJson = FileCopyUtils.copyToString(new FileReader(file));
                    HttpHeaders headers = new HttpHeaders();
                    headers.setContentType(MediaType.APPLICATION_JSON);
                    return ResponseEntity.ok().headers(headers).contentLength(styleJson.getBytes().length).body(styleJson);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }
}