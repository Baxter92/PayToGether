package com.ulr.paytogether.api.resource;

import com.ulr.paytogether.api.apiadapter.MinioEventApiAdapter;
import com.ulr.paytogether.api.dto.MinioEventDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/minio/webhook")
@RequiredArgsConstructor
public class MinioWebhookResource {

    private final MinioEventApiAdapter minioEventApiAdapter;
    @PostMapping("/events")
    public ResponseEntity<Void> handleEvent(@RequestBody MinioEventDto event) {

        minioEventApiAdapter.handleMinioEvent(event);
        return ResponseEntity.ok().build();

    }
}
