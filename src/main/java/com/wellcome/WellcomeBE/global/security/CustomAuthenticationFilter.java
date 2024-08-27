package com.wellcome.WellcomeBE.global.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.wellcome.WellcomeBE.global.exception.CustomException;
import com.wellcome.WellcomeBE.global.exception.ErrorResponse;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.GenericFilterBean;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

@RequiredArgsConstructor
@Slf4j
public class CustomAuthenticationFilter extends GenericFilterBean {

    private final TokenProvider tokenProvider;

    // 필터 적용 x
    private static final List<String> EXCLUDE_URLS = Arrays.asList(
            "/api/oauth2/kakao/refresh"
    );

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        String path = httpRequest.getRequestURI();

        // 요청된 경로가 필터링 제외 경로에 포함되어 있는지 확인
        if (EXCLUDE_URLS.stream().anyMatch(path::startsWith)) {
            chain.doFilter(request, response);
            return;
        }

        try {
            // 헤더에서 토큰 추출
            String token = tokenProvider.extractToken((HttpServletRequest) request);

            if (token != null) {
                // 토큰이 유효하면 토큰에 포함된 정보를 기반으로 Authentication 객체 생성
                Authentication authentication = tokenProvider.getAuthentication(token);

                // SecurityContext 에 Authentication 객체 저장
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }

            chain.doFilter(request, response);
        } catch (CustomException e) {
            HttpServletResponse httpResponse = (HttpServletResponse) response;

            ErrorResponse errorResponse = ErrorResponse.builder()
                    .errorCode(e.getCustomErrorCode().getCode())
                    .message(e.getMessage())
                    .build();

            httpResponse.setStatus(e.getCustomErrorCode().getHttpStatus().value());
            httpResponse.setContentType("application/json; charset=UTF-8");
            httpResponse.setCharacterEncoding("UTF-8");
            httpResponse.getWriter().write(new ObjectMapper().writeValueAsString(errorResponse));
        }

    }

}
