package com.jnvstguru.jnvst_guru_backend.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "google.drive")
public class GoogleDriveProperties {
    private boolean enabled;
    private String credentialsJson;

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getCredentialsJson() {
        return credentialsJson;
    }

    public void setCredentialsJson(String credentialsJson) {
        this.credentialsJson = credentialsJson;
    }
}
