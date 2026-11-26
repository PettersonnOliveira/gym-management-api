package com.portfolio.Gym.Management.API.Services;

import com.portfolio.Gym.Management.API.DTO.AuthResponseDTO;
import com.portfolio.Gym.Management.API.DTO.LoginRequestDTO;
import com.portfolio.Gym.Management.API.DTO.RegisterRequestDTO;
import com.portfolio.Gym.Management.API.Entities.User;
import com.portfolio.Gym.Management.API.Exceptions.BusinessRuleException;
import com.portfolio.Gym.Management.API.Exceptions.InvalidCredentialsException;
import com.portfolio.Gym.Management.API.Repositories.UserRepository;
import com.portfolio.Gym.Management.API.Security.JwtService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Locale;

@Service
public class AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtService jwtService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    public AuthResponseDTO register(RegisterRequestDTO inputData) {
        String email = normalizeEmail(inputData.email());
        if (userRepository.existsByEmail(email)) {
            throw new BusinessRuleException("Email já cadastrado");
        }

        User user = new User(
                email,
                passwordEncoder.encode(inputData.password()),
                User.Role.MEMBER
        );
        User savedUser = userRepository.save(user);
        return toResponse(savedUser);
    }

    public AuthResponseDTO login(LoginRequestDTO inputData) {
        String email = normalizeEmail(inputData.email());
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new InvalidCredentialsException("Credenciais inválidas"));

        if (!passwordEncoder.matches(inputData.password(), user.getPassword())) {
            throw new InvalidCredentialsException("Credenciais inválidas");
        }

        return toResponse(user);
    }

    private AuthResponseDTO toResponse(User user) {
        return new AuthResponseDTO(
                jwtService.generateToken(user),
                user.getEmail(),
                user.getRole().name()
        );
    }

    private String normalizeEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }
}
