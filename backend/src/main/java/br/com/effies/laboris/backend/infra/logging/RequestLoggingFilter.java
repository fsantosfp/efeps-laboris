package br.com.effies.laboris.backend.infra.logging;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@Slf4j
public class RequestLoggingFilter extends OncePerRequestFilter {
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        long startTime = System.currentTimeMillis();

        log.info("Iniciando processamento para {} {}", request.getMethod(), request.getRequestURI());

        try{
            filterChain.doFilter(request, response);
        }finally {
            long duration = System.currentTimeMillis() - startTime;

            log.info("Finalizando processamento para {} {} em {}ms - Status: {}",
                request.getMethod(),
                request.getRequestURI(),
                duration,
                response.getStatus());
        }
    }
}
