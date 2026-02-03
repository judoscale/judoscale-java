package com.judoscale.spring;

import com.judoscale.core.ConfigBase;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuration properties for Judoscale.
 * Can be set via application.properties/yml or environment variables.
 * 
 * <p>The API URL can be configured in several ways (in order of precedence):
 * <ol>
 *   <li>{@code judoscale.api-base-url} property</li>
 *   <li>{@code judoscale.url} property</li>
 *   <li>{@code JUDOSCALE_URL} environment variable (via Spring's relaxed binding)</li>
 * </ol>
 */
@ConfigurationProperties(prefix = "judoscale")
public class JudoscaleConfig extends ConfigBase {
}
