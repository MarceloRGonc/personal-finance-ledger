package com.finances.web;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.servlet.ServletException;
import java.io.IOException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

class SameOriginFilterTest {

    private final SameOriginFilter filter = new SameOriginFilter();

    @Test
    void readsAreNeverBlocked() throws Exception {
        var outcome = send(request("GET").header("Sec-Fetch-Site", "cross-site"));

        assertThat(outcome.reachedController()).isTrue();
    }

    @Test
    void writesWithoutBrowserHeadersAreAllowedForCommandLineClients() throws Exception {
        var outcome = send(request("POST"));

        assertThat(outcome.reachedController()).isTrue();
    }

    @ParameterizedTest
    @ValueSource(strings = {"same-origin", "none"})
    void writesFromTheAppItselfAreAllowed(String fetchSite) throws Exception {
        var outcome = send(request("POST").header("Sec-Fetch-Site", fetchSite));

        assertThat(outcome.reachedController()).isTrue();
    }

    @Test
    void writesWithAMatchingOriginAreAllowed() throws Exception {
        var outcome = send(request("DELETE").header("Origin", "http://localhost:8080"));

        assertThat(outcome.reachedController()).isTrue();
    }

    @ParameterizedTest
    @ValueSource(strings = {"POST", "PUT", "DELETE", "PATCH"})
    void writesFromAnotherSiteAreBlocked(String method) throws Exception {
        var outcome = send(request(method).header("Sec-Fetch-Site", "cross-site"));

        assertThat(outcome.reachedController()).isFalse();
        assertThat(outcome.response().getStatus()).isEqualTo(403);
        assertThat(outcome.response().getContentAsString()).isEqualTo("Cross-site request blocked");
    }

    @Test
    void writesFromAnotherPortOnTheSameHostAreBlocked() throws Exception {
        var bySiteHeader = send(request("POST").header("Sec-Fetch-Site", "same-site"));
        var byOrigin = send(request("POST").header("Origin", "http://localhost:3000"));

        assertThat(bySiteHeader.reachedController()).isFalse();
        assertThat(byOrigin.reachedController()).isFalse();
    }

    @ParameterizedTest
    @ValueSource(strings = {"http://evil.example", "null", "not a url"})
    void writesWithAForeignOrUnreadableOriginAreBlocked(String origin) throws Exception {
        var outcome = send(request("POST").header("Origin", origin));

        assertThat(outcome.reachedController()).isFalse();
        assertThat(outcome.response().getStatus()).isEqualTo(403);
    }

    private static FluentRequest request(String method) {
        var request = new FluentRequest(method);
        request.addHeader("Host", "localhost:8080");
        return request;
    }

    private Outcome send(MockHttpServletRequest request) throws ServletException, IOException {
        var response = new MockHttpServletResponse();
        var chain = new MockFilterChain();
        filter.doFilter(request, response, chain);
        return new Outcome(response, chain.getRequest() != null);
    }

    private static final class FluentRequest extends MockHttpServletRequest {

        FluentRequest(String method) {
            super(method, "/backup/restore");
        }

        FluentRequest header(String name, String value) {
            addHeader(name, value);
            return this;
        }
    }

    private record Outcome(MockHttpServletResponse response, boolean reachedController) {
    }
}
