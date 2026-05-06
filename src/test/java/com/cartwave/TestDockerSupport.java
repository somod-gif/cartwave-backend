package com.cartwave;

import org.testcontainers.DockerClientFactory;

public final class TestDockerSupport {

    private TestDockerSupport() {
    }

    public static boolean isDockerAvailable() {
        try {
            return DockerClientFactory.instance().isDockerAvailable();
        } catch (Throwable ignored) {
            return false;
        }
    }
}
