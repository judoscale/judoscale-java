package com.judoscale.core;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AdapterTest {

    @Test
    void createsAdapterWithAllFields() {
        Adapter adapter = new Adapter("judoscale-spring-boot", "1.0.0", "3.2.2");

        assertThat(adapter.name()).isEqualTo("judoscale-spring-boot");
        assertThat(adapter.version()).isEqualTo("1.0.0");
        assertThat(adapter.runtimeVersion()).isEqualTo("3.2.2");
    }

    @Test
    void allowsNullRuntimeVersion() {
        Adapter adapter = new Adapter("judoscale-spring-boot", "1.0.0", null);

        assertThat(adapter.runtimeVersion()).isNull();
    }

    @Test
    void throwsExceptionWhenNameIsNull() {
        assertThatThrownBy(() -> new Adapter(null, "1.0.0", "3.2.2"))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Adapter name must not be null");
    }

    @Test
    void throwsExceptionWhenVersionIsNull() {
        assertThatThrownBy(() -> new Adapter("judoscale-spring-boot", null, "3.2.2"))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Adapter version must not be null");
    }

    @Test
    void equalsAndHashCodeWorkCorrectly() {
        Adapter adapter1 = new Adapter("judoscale-spring-boot", "1.0.0", "3.2.2");
        Adapter adapter2 = new Adapter("judoscale-spring-boot", "1.0.0", "3.2.2");
        Adapter adapter3 = new Adapter("judoscale-spring-boot-2", "1.0.0", "3.2.2");
        Adapter adapter4 = new Adapter("judoscale-spring-boot", "1.0.0", "2.7.0");
        Adapter adapter5 = new Adapter("judoscale-spring-boot", "1.0.0", null);

        assertThat(adapter1).isEqualTo(adapter2);
        assertThat(adapter1.hashCode()).isEqualTo(adapter2.hashCode());
        assertThat(adapter1).isNotEqualTo(adapter3);
        assertThat(adapter1).isNotEqualTo(adapter4);
        assertThat(adapter1).isNotEqualTo(adapter5);
    }
}
