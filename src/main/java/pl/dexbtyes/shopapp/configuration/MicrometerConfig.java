package pl.dexbtyes.shopapp.configuration;

import io.micrometer.core.instrument.Meter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.config.MeterFilter;
import io.micrometer.core.instrument.config.NamingConvention;
import io.micrometer.core.instrument.distribution.DistributionStatisticConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.autoconfigure.metrics.MeterRegistryCustomizer;
import org.springframework.boot.info.BuildProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;
import java.util.Arrays;
import java.util.Objects;
import java.util.stream.Collectors;

@Configuration
@Slf4j
public class MicrometerConfig {
    private final BuildProperties buildProperties;

    @Autowired
    public MicrometerConfig(BuildProperties buildProperties) {
        this.buildProperties = buildProperties;
    }

    @Bean
    MeterRegistryCustomizer<MeterRegistry> metrics() {
        return registry -> registry
                .config()
                .meterFilter(new MicrometerFilter())
                .namingConvention(new PrometheusMetricsNamingConvention(buildProperties));
    }

    protected static class MicrometerFilter implements MeterFilter {
        @Override
        public DistributionStatisticConfig configure(Meter.Id id, DistributionStatisticConfig config) {
            if (id.getName().equals("http.server.requests")) {
                return DistributionStatisticConfig.builder()
                        .percentilesHistogram(false)
                        .percentiles(0.5, 0.75, 0.9, 0.95, 0.99)
                        .serviceLevelObjectives(
                                (double) Duration.ofMillis(1).toNanos(),
                                (double) Duration.ofMillis(10).toNanos(),
                                (double) Duration.ofMillis(50).toNanos(),
                                (double) Duration.ofMillis(100).toNanos(),
                                (double) Duration.ofMillis(300).toNanos(),
                                (double) Duration.ofMillis(500).toNanos(),
                                (double) Duration.ofSeconds(1).toNanos(),
                                (double) Duration.ofSeconds(3).toNanos(),
                                (double) Duration.ofSeconds(5).toNanos()
                        )
                        .minimumExpectedValue((double) Duration.ofMillis(1).toNanos())
                        .maximumExpectedValue((double) Duration.ofSeconds(5).toNanos())
                        .build()
                        .merge(config);
            }
            return MeterFilter.super.configure(id, config);
        }
    }

    protected static class PrometheusMetricsNamingConvention implements NamingConvention {

        private static final String REGEX = "([^a-zA-Z0-9])";
        final BuildProperties buildProperties;

        public PrometheusMetricsNamingConvention(BuildProperties buildProperties) {
            this.buildProperties = buildProperties;
        }

        @Override
        public String tagKey(String key) {
            return NamingConvention.super.tagKey(
                    Arrays.stream(key.split(REGEX))
                            .filter(Objects::nonNull)
                            .collect(Collectors.joining("_"))
            );
        }

        @Override
        public String tagValue(String value) {
            return NamingConvention.super.tagValue(
                    Arrays.stream(value.split(REGEX))
                            .filter(Objects::nonNull)
                            .collect(Collectors.joining("_"))
            );
        }

        @Override
        public String name(String name, Meter.Type type, String baseUnit) {
            return buildProperties.getArtifact() + "_" + Arrays.stream(name.split(REGEX))
                    .filter(Objects::nonNull)
                    .collect(Collectors.joining("_"));
        }
    }
}
