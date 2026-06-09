package br.com.effies.laboris.backend.presentation.controller;

//import br.com.effies.laboris.backend.domain.service.AuthService;
import br.com.effies.laboris.backend.domain.service.AuthService;
import br.com.effies.laboris.backend.presentation.dto.request.LoginRequestDto;
import br.com.effies.laboris.backend.presentation.dto.response.LoginResponseDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    @Autowired
    private AuthService authService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @PostMapping("/login")
    public ResponseEntity<LoginResponseDto> login(@RequestBody LoginRequestDto loginRequest){

        try{
            LoginResponseDto response = authService.login(loginRequest);
            return ResponseEntity.ok(response);
        } catch (AuthenticationException e) {
            return ResponseEntity.status(401).build();
        }

    }
}
