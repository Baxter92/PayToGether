package com.ulr.paytogether.core.modele;

import lombok.Builder;

import java.util.List;

@Builder
public record MinioEvent(List<Record> records) {

    public record Record(S3 s3) {}

    public record S3(Object object) {}

    public record Object(String key) {}
}
