package com.example.osgi.it;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class HelloControllerIT {

    private static Process karafProcess;
    private static final String HELLO_URL = "http://localhost:8081/hello";
    private static final Duration STARTUP_TIMEOUT = Duration.ofMinutes(3);

    @BeforeAll
    static void startKaraf() throws Exception {
        String assemblyDir = System.getProperty("karaf.assembly.dir");
        if (assemblyDir == null) {
            throw new IllegalStateException("System property karaf.assembly.dir not set — run via Maven Failsafe");
        }

        File karafHome = new File(assemblyDir).getCanonicalFile();
        File karafBin = new File(karafHome, "bin/karaf");
        karafBin.setExecutable(true);

        karafProcess = new ProcessBuilder(karafBin.getAbsolutePath(), "server")
                .directory(karafHome)
                .inheritIO()
                .start();

        waitForEndpoint(HELLO_URL, STARTUP_TIMEOUT);
    }

    @AfterAll
    static void stopKaraf() throws Exception {
        if (karafProcess != null && karafProcess.isAlive()) {
            // bin/karaf is a shell script — kill its spawned JVM children first
            karafProcess.descendants().forEach(ProcessHandle::destroy);
            karafProcess.destroy();
            if (!karafProcess.waitFor(30, TimeUnit.SECONDS)) {
                karafProcess.descendants().forEach(ProcessHandle::destroyForcibly);
                karafProcess.destroyForcibly();
            }
        }
    }

    @Test
    void helloEndpointReturnsHttp200() throws Exception {
        HttpResponse<String> response = get(HELLO_URL);
        assertEquals(200, response.statusCode());
    }

    @Test
    void helloEndpointBodyCombinesBundleGreetings() throws Exception {
        HttpResponse<String> response = get(HELLO_URL);
        // Bundle2.hello() + Bundle3.hello() where Bundle3 delegates to Bundle1
        assertEquals("hello from bundle2hello from bundle1called from bundle 3", response.body());
    }

    private static HttpResponse<String> get(String url) throws Exception {
        return HttpClient.newHttpClient().send(
                HttpRequest.newBuilder().uri(URI.create(url)).GET().build(),
                HttpResponse.BodyHandlers.ofString()
        );
    }

    private static void waitForEndpoint(String url, Duration timeout) throws Exception {
        Instant deadline = Instant.now().plus(timeout);
        while (Instant.now().isBefore(deadline)) {
            try {
                HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
                conn.setConnectTimeout(1000);
                conn.setReadTimeout(2000);
                int code = conn.getResponseCode();
                if (code > 0) {
                    return;
                }
            } catch (Exception ignored) {
                // Karaf/Spring Boot not ready yet
            }
            Thread.sleep(2000);
        }
        throw new IllegalStateException("Spring Boot endpoint did not become available within " + timeout);
    }
}
