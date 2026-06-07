package br.com.effies.laboris.backend.infra.security;

import br.com.effies.laboris.backend.domain.repository.UserRepository;
import br.com.effies.laboris.backend.domain.service.TokenService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class SecurityFilter extends OncePerRequestFilter {

    private final TokenService tokenService;
    private final UserRepository repository;

    SecurityFilter(TokenService tokenService, UserRepository userRepository){
        this.tokenService = tokenService;
        this.repository = userRepository;
    }

    @Override
    protected void doFilterInternal(
        HttpServletRequest request,
        HttpServletResponse response,
        FilterChain filterChain
    ) throws ServletException, IOException {

        var token = this.recoverToken(request);

        if(token != null){

            var email = tokenService.validateToken(token);

            if(!email.isEmpty()){

                UserDetails user = repository.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("Usuário não encontrado no filtro de segurança"));

                var authentication = new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());

                SecurityContextHolder.getContext().setAuthentication(authentication);

                if (tokenService.isPasswordResetRequired(token)) {
                    String requestURI = request.getRequestURI();
                    String method = request.getMethod();
                    boolean isPublicRoute = requestURI.startsWith("/api/v1/auth/");
                    boolean isChangePasswordRoute = (requestURI.equals("/api/v1/me/password") || requestURI.equals("/api/v1/me/password/"))
                            && "PUT".equalsIgnoreCase(method);

                    if (!isPublicRoute && !isChangePasswordRoute) {
                        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                        response.setContentType("application/json;charset=UTF-8");
                        response.getWriter().write("{\"error\": \"PASSWORD_RESET_REQUIRED\", \"message\": \"Password reset is required before accessing this resource.\"}");
                        return;
                    }
                }
            }
        }
        filterChain.doFilter(request, response);
    }

    private String recoverToken(HttpServletRequest request){
        var authHeader = request.getHeader("Authorization");
        if(authHeader == null) return null;
        return authHeader.replace("Bearer ","");
    }
}
