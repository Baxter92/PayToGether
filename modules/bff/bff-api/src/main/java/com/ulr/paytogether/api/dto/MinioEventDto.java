package com.ulr.paytogether.api.dto;

import java.util.List;

public record MinioEventDto(List<Record> records) {

    public record Record(S3 s3) {}

    public record S3(Object object) {}

    public record Object(String key) {}
}
