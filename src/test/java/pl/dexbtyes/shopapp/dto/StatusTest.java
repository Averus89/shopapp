package pl.dexbtyes.shopapp.dto;

import org.junit.jupiter.api.Test;

import static com.google.common.truth.Truth.assertThat;

class StatusTest {
    @Test
    void shouldCreateProperObject() {
        Status status = new Status(404, "Not found");

        assertThat(status).isNotNull();
        assertThat(status.status()).isEqualTo("Not found");
        assertThat(status.code()).isEqualTo(404);
    }
}