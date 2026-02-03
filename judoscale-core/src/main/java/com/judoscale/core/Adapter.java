package com.judoscale.core;

import java.util.Objects;

/**
 * Represents an adapter that integrates with Judoscale.
 * Each adapter (e.g., Spring Boot, job queue libraries) provides its name and version
 * for identification in the metrics report.
 */
public final class Adapter {

    private final String name;
    private final String version;

    /**
     * Creates an Adapter with the specified name and version.
     *
     * @param name the adapter name (e.g., "judoscale-spring-boot", "judoscale-spring-boot-2"), must not be null
     * @param version the adapter version, must not be null
     */
    public Adapter(String name, String version) {
        if (name == null) {
            throw new IllegalArgumentException("Adapter name must not be null");
        }
        if (version == null) {
            throw new IllegalArgumentException("Adapter version must not be null");
        }
        this.name = name;
        this.version = version;
    }

    /**
     * Returns the adapter name.
     *
     * @return the adapter name
     */
    public String name() {
        return name;
    }

    /**
     * Returns the adapter version.
     *
     * @return the adapter version
     */
    public String version() {
        return version;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Adapter adapter = (Adapter) o;
        return Objects.equals(name, adapter.name) && Objects.equals(version, adapter.version);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, version);
    }

    @Override
    public String toString() {
        return "Adapter{name='" + name + "', version='" + version + "'}";
    }
}
