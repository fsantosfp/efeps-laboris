package br.com.effies.laboris.backend.domain.service;


import br.com.effies.laboris.backend.domain.entity.User;
import br.com.effies.laboris.backend.presentation.dto.request.LoginRequestDto;
import br.com.effies.laboris.backend.presentation.dto.response.LoginResponseDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    @Autowired
    private AuthenticationManager authenticationManager;
    @Autowired
    private TokenService tokenService;

    public LoginResponseDto login(LoginRequestDto loginRequest){
        var usernamePassword = new UsernamePasswordAuthenticationToken(
            loginRequest.getEmail(),
            loginRequest.getPassword());

        var auth = this.authenticationManager.authenticate(usernamePassword);

        User authenticatedUser = (User) auth.getPrincipal();

        String token = tokenService.generateToken(authenticatedUser);
        return new LoginResponseDto(token, authenticatedUser.isPasswordResetRequired());
    }
}
