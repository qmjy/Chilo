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
import lombok.AllArgsConstructor;
import org.apache.tomcat.util.http.fileupload.IOUtils;
import org.springframework.core.io.ByteArrayResource;
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
import java.nio.file.Files;
import java.util.Optional;

@RestController
@RequestMapping("/api/sprites")
@AllArgsConstructor
@Tag(name = "Maplibre 雪碧图服务管理", description = "Maplibre 离线服务接口能力")
public class MapServerSpritesController {

    private AppConfig appConfig;


    /**
     * 加载sprite的json 内容
     *
     * @return sprite的json 内容
     */
    @ResponseBody
    @GetMapping(value = "/{spriteName}/{fileName}.json", produces = "application/json")
    public ResponseEntity<String> loadStyle(@PathVariable String spriteName, @PathVariable String fileName) {
        if (SystemUtils.checkTilesetName(fileName)) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        String configDataPath = appConfig.getDataPath();
        if (StringUtils.hasLength(configDataPath)) {
            Optional<File> safeFileOfSprites = FileUtils.getInstance(appConfig).getSafeFileOfSprites(spriteName, fileName, AppConfig.FILE_EXTENSION_NAME_JSON);
            if (safeFileOfSprites.isPresent()) {
                try {
                    String styleJson = FileCopyUtils.copyToString(new FileReader(safeFileOfSprites.get()));
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

    /**
     * 加载sprite 的图片内容
     *
     * @return sprite 的图片内容
     */
    @ResponseBody
    @GetMapping(value = "/{spriteName}/{fileName}.png")
    public ResponseEntity<ByteArrayResource> loadSpritePng(@PathVariable String spriteName, @PathVariable String fileName) {
        if (SystemUtils.checkTilesetName(fileName)) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        if (StringUtils.hasLength(appConfig.getDataPath())) {
            Optional<File> safeFileOfSprites = FileUtils.getInstance(appConfig).getSafeFileOfSprites(spriteName, fileName, AppConfig.FILE_EXTENSION_NAME_PNG);
            if (safeFileOfSprites.isPresent()) {
                try {
                    File file = safeFileOfSprites.get();
                    byte[] buffer = FileCopyUtils.copyToByteArray(file);
                    IOUtils.readFully(Files.newInputStream(file.toPath()), buffer);
                    HttpHeaders headers = new HttpHeaders();
                    headers.setContentType(MediaType.IMAGE_PNG);
                    ByteArrayResource resource = new ByteArrayResource(buffer);
                    return ResponseEntity.ok().headers(headers).contentLength(buffer.length).body(resource);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }
}
