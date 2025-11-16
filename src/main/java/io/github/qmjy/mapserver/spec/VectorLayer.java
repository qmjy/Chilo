package io.github.qmjy.mapserver.spec;

import java.util.Map;

import lombok.Data;

@Data
public class VectorLayer {
    private String id;
    private String description;
    private String minzoom;
    private String maxzoom;
    private Map<String, Object> fields;
    private String[] geometry;
}