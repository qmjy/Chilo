/*******************    💫 Codegeex Inline Diff    *******************/
/*
 * Copyright (c) 2023 QMJY.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *        https://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */

package io.github.qmjy.mapserver.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.io.File;
import java.nio.file.Paths;

/**
 * Web MVC配置
 *
 * @author Shaofeng Liu
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {
    private final AppConfig appConfig;

    public WebMvcConfig(AppConfig appConfig) {
        this.appConfig = appConfig;
    }

    /**
     * 静态资源访问路由
     * 系统自带的静态资源都在static目录下
     * 用户上传的资源放在assets目录下
     *
     * @param registry ResourceHandlerRegistry
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/static/**")
                //版本包自带静态资源
                .addResourceLocations("classpath:/static/")
                //用户上传的静态资源
                .addResourceLocations("file:" + Paths.get(appConfig.getDataPath(), "assets") + File.separator);
    }


    /**
     * 允许跨域访问
     *
     * @param registry CorsRegistry
     */
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedHeaders("Content-Type", "X-Requested-With", "accept,Origin", "Access-Control-Request-Method", "Access-Control-Request-Headers", "token")
                .allowedMethods("*")
                .allowedOriginPatterns("*")
                .allowCredentials(true);
    }
}
