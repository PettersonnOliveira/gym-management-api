package com.portfolio.Gym.Management.API.Controllers;

import com.portfolio.Gym.Management.API.DTO.AuthResponseDTO;
import com.portfolio.Gym.Management.API.DTO.LoginRequestDTO;
import com.portfolio.Gym.Management.API.DTO.RegisterRequestDTO;
import com.portfolio.Gym.Management.API.Services.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
public class AuthController {
    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ResponseEntity<AuthResponseDTO> register(@RequestBody @Valid RegisterRequestDTO inputData) {
        AuthResponseDTO response = authService.register(inputData);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponseDTO> login(@RequestBody @Valid LoginRequestDTO inputData) {
        return ResponseEntity.ok(authService.login(inputData));
    }
}
