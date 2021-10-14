package com.orbsec.backendfileuploaderapi.dto;

import lombok.Data;

@Data
public class ResponseFile {
    private String name;
    private String url;
    private String type;

    public ResponseFile(String name, String url, String type) {
        this.name = name;
        this.url = url;
        this.type = type;
    }
}
