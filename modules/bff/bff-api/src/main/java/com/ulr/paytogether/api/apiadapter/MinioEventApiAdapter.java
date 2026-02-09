package com.ulr.paytogether.api.apiadapter;

import com.ulr.paytogether.api.dto.MinioEventDto;
import com.ulr.paytogether.api.mapper.MinioEventMapper;
import com.ulr.paytogether.core.domaine.service.MinioEventProvider;
import com.ulr.paytogether.core.modele.MinioEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MinioEventApiAdapter  {
    private final MinioEventProvider provider;
    private final MinioEventMapper mapper;


    public void handleMinioEvent(MinioEventDto event) {
        MinioEvent minioEvent = mapper.fromDto(event);
        provider.handleMinioEvent(minioEvent);
    }
}
