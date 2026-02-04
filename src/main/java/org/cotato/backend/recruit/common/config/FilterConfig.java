package org.cotato.backend.recruit.common.config;

import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.util.ContentCachingRequestWrapper;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import java.io.IOException;

@Configuration
public class FilterConfig {
    // 요청 내용을 여러 번 읽을 수 있게 감싸주는 필터
    @Bean
    public FilterRegistrationBean<Filter> wrappingFilter() {
        FilterRegistrationBean<Filter> registrationBean = new FilterRegistrationBean<>();
        registrationBean.setFilter(new Filter() {
            @Override
            public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
                    throws IOException, ServletException {
                ContentCachingRequestWrapper wrappedRequest = new ContentCachingRequestWrapper(
                        (HttpServletRequest) request, 10 * 1024 * 1024); // 10MB cache limit
                chain.doFilter(wrappedRequest, response);
            }
        });
        registrationBean.addUrlPatterns("/*"); // 모든 요청에 적용
        return registrationBean;
    }
}
