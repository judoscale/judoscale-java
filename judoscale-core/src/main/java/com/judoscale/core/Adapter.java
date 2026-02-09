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
    private final String runtimeVersion;

    /**
     * Creates an Adapter with the specified name, version, and runtime version.
     *
     * @param name the adapter name (e.g., "judoscale-spring-boot", "judoscale-spring-boot-2"), must not be null
     * @param version the adapter version, must not be null
     * @param runtimeVersion the runtime version (e.g., Spring Boot version), may be null
     */
    public Adapter(String name, String version, String runtimeVersion) {
        if (name == null) {
            throw new IllegalArgumentException("Adapter name must not be null");
        }
        if (version == null) {
            throw new IllegalArgumentException("Adapter version must not be null");
        }
        this.name = name;
        this.version = version;
        this.runtimeVersion = runtimeVersion;
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

    /**
     * Returns the runtime version (e.g., Spring Boot version).
     *
     * @return the runtime version, or null if not set
     */
    public String runtimeVersion() {
        return runtimeVersion;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Adapter adapter = (Adapter) o;
        return Objects.equals(name, adapter.name) && Objects.equals(version, adapter.version)
            && Objects.equals(runtimeVersion, adapter.runtimeVersion);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, version, runtimeVersion);
    }

    @Override
    public String toString() {
        return "Adapter{name='" + name + "', version='" + version + "', runtimeVersion='" + runtimeVersion + "'}";
    }
}
