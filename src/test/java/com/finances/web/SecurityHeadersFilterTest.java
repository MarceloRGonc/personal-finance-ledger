package com.finances.web;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

class SecurityHeadersFilterTest {

    @Test
    void everyResponseCarriesTheSecurityHeadersAndStillReachesTheController() throws Exception {
        var response = new MockHttpServletResponse();
        var chain = new MockFilterChain();

        new SecurityHeadersFilter().doFilter(new MockHttpServletRequest("GET", "/summary"), response, chain);

        assertThat(chain.getRequest()).isNotNull();
        assertThat(response.getHeader("Content-Security-Policy"))
                .contains("default-src 'self'", "frame-ancestors 'none'", "form-action 'self'", "object-src 'none'")
                .doesNotContain("unsafe-inline", "unsafe-eval");
        assertThat(response.getHeader("X-Content-Type-Options")).isEqualTo("nosniff");
        assertThat(response.getHeader("X-Frame-Options")).isEqualTo("DENY");
        assertThat(response.getHeader("Referrer-Policy")).isEqualTo("same-origin");
    }
}
