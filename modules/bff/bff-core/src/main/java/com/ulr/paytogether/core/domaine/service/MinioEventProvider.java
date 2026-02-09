package com.ulr.paytogether.core.domaine.service;

import com.ulr.paytogether.core.modele.MinioEvent;

public interface MinioEventProvider {

        void handleMinioEvent(MinioEvent event);
}
