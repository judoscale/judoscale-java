package com.judoscale.core;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AdapterTest {

    @Test
    void createsAdapterWithNameAndVersion() {
        Adapter adapter = new Adapter("judoscale-spring-boot", "1.0.0");

        assertThat(adapter.name()).isEqualTo("judoscale-spring-boot");
        assertThat(adapter.version()).isEqualTo("1.0.0");
    }

    @Test
    void throwsExceptionWhenNameIsNull() {
        assertThatThrownBy(() -> new Adapter(null, "1.0.0"))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Adapter name must not be null");
    }

    @Test
    void throwsExceptionWhenVersionIsNull() {
        assertThatThrownBy(() -> new Adapter("judoscale-spring-boot", null))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Adapter version must not be null");
    }

    @Test
    void equalsAndHashCodeWorkCorrectly() {
        Adapter adapter1 = new Adapter("judoscale-spring-boot", "1.0.0");
        Adapter adapter2 = new Adapter("judoscale-spring-boot", "1.0.0");
        Adapter adapter3 = new Adapter("judoscale-spring-boot-2", "1.0.0");

        assertThat(adapter1).isEqualTo(adapter2);
        assertThat(adapter1.hashCode()).isEqualTo(adapter2.hashCode());
        assertThat(adapter1).isNotEqualTo(adapter3);
    }
}
