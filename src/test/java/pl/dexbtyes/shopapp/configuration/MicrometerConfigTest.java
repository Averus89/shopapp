package pl.dexbtyes.shopapp.configuration;

import io.micrometer.core.instrument.Meter;
import io.micrometer.core.instrument.distribution.DistributionStatisticConfig;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Duration;

import static com.google.common.truth.Truth.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MicrometerConfigTest {
    @Mock
    Meter.Id meterId;
    @Mock
    DistributionStatisticConfig config;
    double[] percentiles = {0.5, 0.75, 0.9, 0.95, 0.99};
    double[] slas = {1000000.0, 1.0E7, 5.0E7, 1.0E8, 3.0E8, 5.0E8, 1.0E9, 3.0E9, 5.0E9};

    @Test
    void shouldMergeMicrometerDistributionConfig() {
        when(meterId.getName()).thenReturn("http.server.requests");

        MicrometerConfig.MicrometerFilter filter = new MicrometerConfig.MicrometerFilter();
        DistributionStatisticConfig newConfig = filter.configure(meterId, config);

        assertThat(newConfig).isNotNull();
        assertThat(newConfig.getPercentiles()).isEqualTo(percentiles);
        assertThat(newConfig.getMinimumExpectedValueAsDouble()).isEqualTo((double) Duration.ofMillis(1).toNanos());
        assertThat(newConfig.getMaximumExpectedValueAsDouble()).isEqualTo((double) Duration.ofSeconds(5).toNanos());
        assertThat(newConfig.getServiceLevelObjectiveBoundaries()).isEqualTo(slas);
        assertThat(newConfig.isPercentileHistogram()).isFalse();
    }
}