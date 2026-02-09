package com.ulr.paytogether.api.mapper;

import com.ulr.paytogether.api.dto.MinioEventDto;
import com.ulr.paytogether.core.modele.MinioEvent;
import org.springframework.stereotype.Component;

@Component
public class MinioEventMapper {

   public MinioEvent fromDto(MinioEventDto dto) {
        return MinioEvent.builder()
                .records(dto.records().stream()
                        .map(recordDto -> new MinioEvent.Record(
                                new MinioEvent.S3(
                                        new MinioEvent.Object(recordDto.s3().object().key())
                                )
                        ))
                        .toList())
                .build();
    }
}
