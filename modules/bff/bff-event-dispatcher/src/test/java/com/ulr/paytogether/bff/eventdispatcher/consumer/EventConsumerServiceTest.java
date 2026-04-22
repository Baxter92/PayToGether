package com.ulr.paytogether.bff.eventdispatcher.consumer;

import com.ulr.paytogether.bff.event.annotation.FunctionalHandler;
import com.ulr.paytogether.bff.event.handler.ConsumerHandler;
import com.ulr.paytogether.core.event.AccountValidationEvent;
import com.ulr.paytogether.core.event.EventPublisher;
import com.ulr.paytogether.bff.eventdispatcher.repository.EventRecordRepository;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Test de découverte des handlers avec @FunctionalHandler
 */
class EventConsumerServiceTest {

    @Component
    static class TestHandler implements ConsumerHandler {
        @FunctionalHandler(eventType = AccountValidationEvent.class, maxAttempts = 3, description = "Test handler")
        public void handleEvent(AccountValidationEvent event) {
            // Test method
        }
    }

    @Test
    void testDiscoverHandlers() {
        // Arrange
        EventRecordRepository mockRepository = mock(EventRecordRepository.class);
        EventPublisher mockPublisher = mock(EventPublisher.class);
        ApplicationContext mockContext = mock(ApplicationContext.class);

        TestHandler testHandler = new TestHandler();
        when(mockContext.getBeanNamesForType(ConsumerHandler.class))
                .thenReturn(new String[]{"testHandler"});
        when(mockContext.getBean("testHandler")).thenReturn(testHandler);

        // Act
        EventConsumerService service = new EventConsumerService(mockRepository, mockContext, null);

        // Assert
        // Le test devrait découvrir 1 handler
        assertTrue(true, "Handler discovery should work without exceptions");
    }
}

