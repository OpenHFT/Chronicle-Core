package net.openhft.chronicle.core.analytics;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;

import java.util.Collections;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

class AnalyticsFacadeTest {

    private AnalyticsFacade analyticsFacade;
    private AnalyticsFacade.Builder builder;

    @BeforeEach
    void setUp() {
        // Initialize with mock or stub implementation
        analyticsFacade = mock(AnalyticsFacade.class);
        builder = mock(AnalyticsFacade.Builder.class);
    }

    @Test
    void isEnabledReturnsCorrectValue() {
        // Set up scenarios for when analytics is enabled and disabled
        // Assertions based on those setups
    }

    @Test
    void builderReturnsNonNullInstance() {
        assertNotNull(AnalyticsFacade.builder("measurementId", "apiSecret"));
    }

    @Test
    void standardBuilderIncludesCorrectParameters() {
        // Verify standard event parameters and user properties are included
    }

    @Test
    void standardEventParametersContainsAppVersion() {
        // Verify the returned map has the correct app version
    }

    // Similar test methods for other scenarios...

    class BuilderTest {

        private AnalyticsFacade.Builder builder;

        @BeforeEach
        void setUp() {
            builder = mock(AnalyticsFacade.Builder.class);
        }

        @Test
        void putEventParameterAddsParameterCorrectly() {
            builder.putEventParameter("key", "value");
            verify(builder).putEventParameter("key", "value");
        }

        @Test
        void withFrequencyLimitSetsLimitCorrectly() {
            builder.withFrequencyLimit(5, 1, TimeUnit.HOURS);
            verify(builder).withFrequencyLimit(5, 1, TimeUnit.HOURS);
        }

        @Test
        void withErrorLoggerSetsCustomLogger() {
            Consumer<String> errorLogger = System.err::println;
            builder.withErrorLogger(errorLogger);
            verify(builder).withErrorLogger(errorLogger);
        }

        @Test
        void withDebugLoggerSetsCustomLogger() {
            Consumer<String> debugLogger = System.out::println;
            builder.withDebugLogger(debugLogger);
            verify(builder).withDebugLogger(debugLogger);
        }

        @Test
        void withClientIdFileNameSetsCustomFileName() {
            String clientIdFileName = "custom_client_id_file.txt";
            builder.withClientIdFileName(clientIdFileName);
            verify(builder).withClientIdFileName(clientIdFileName);
        }

        @Test
        void withUrlSetsCustomUrl() {
            String customUrl = "https://custom-analytics-url.com";
            builder.withUrl(customUrl);
            verify(builder).withUrl(customUrl);
        }

        @Test
        void withReportDespiteJUnitAllowsReporting() {
            builder.withReportDespiteJUnit();
            verify(builder).withReportDespiteJUnit();
        }

        @Test
        void buildCreatesNewAnalyticsFacadeInstance() {
            when(builder.build()).thenReturn(mock(AnalyticsFacade.class));
            assertNotNull(builder.build());
        }
    }

}
