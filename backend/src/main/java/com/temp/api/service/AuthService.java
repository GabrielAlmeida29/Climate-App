package com.temp.api.service;

import com.temp.api.dto.AuthResponseDTO;
import com.temp.api.dto.LoginRequestDTO;
import com.temp.api.dto.RegisterRequestDTO;
import com.temp.api.dto.UserResponseDTO;
import com.temp.api.exceptions.EmailJaCadastradoException;
import com.temp.api.exceptions.UsuarioNaoEncontradoException;
import com.temp.api.model.Role;
import com.temp.api.model.User;
import com.temp.api.repository.UserRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Locale;

@Service
public class AuthService {

    // O BCrypt só considera os primeiros 72 bytes da senha.
    private static final int BCRYPT_MAX_BYTES = 72;

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenService tokenService;
    private final PasswordValidatorService passwordValidatorService;

    // Usado quando o e-mail não existe, para o login levar o mesmo tempo
    // com e sem usuário (evita descobrir e-mails cadastrados pelo tempo de resposta).
    private final String hashFicticio;

    public AuthService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       JwtTokenService tokenService, PasswordValidatorService passwordValidator) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.tokenService = tokenService;
        this.hashFicticio = passwordEncoder.encode("senha-ficticia");
        this.passwordValidatorService = passwordValidator;
    }

    public UserResponseDTO cadastrar(RegisterRequestDTO dto) {
        passwordValidatorService.validar(dto.senha());

        String email = normalizar(dto.email());

        if (userRepository.existsByEmail(email)) {
            throw new EmailJaCadastradoException();
        }

        User user = new User(
                dto.nome().trim(),
                email,
                passwordEncoder.encode(dto.senha()),
                Role.USER
        );

        try {
            user = userRepository.save(user);
        } catch (DataIntegrityViolationException e) {
            // Corrida: outro cadastro com o mesmo e-mail passou entre o exists e o save.
            throw new EmailJaCadastradoException();
        }

        return UserResponseDTO.from(user);
    }

    public AuthResponseDTO autenticar(LoginRequestDTO dto) {
        String email = normalizar(dto.email());

        User user = userRepository.findByEmail(email).orElse(null);
        String hash = (user != null) ? user.getSenhaHash() : hashFicticio;

        boolean senhaConfere = passwordEncoder.matches(dto.senha(), hash);

        // Mesma resposta para e-mail inexistente, senha errada e conta inativa.
        if (user == null || !senhaConfere || !user.isAtivo()) {
            throw new CredenciaisInvalidasException();
        }

        return new AuthResponseDTO(
                tokenService.gerarToken(user),
                "Bearer",
                tokenService.getValidadeEmSegundos(),
                UserResponseDTO.from(user)
        );
    }

    public UserResponseDTO buscarPerfil(Long userId) {
        User user = userRepository.findById(userId)
                .filter(User::isAtivo)
                .orElseThrow(UsuarioNaoEncontradoException::new);

        return UserResponseDTO.from(user);
    }

    public void desativarUsuario(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(UsuarioNaoEncontradoException::new);

        user.desativar();
        userRepository.save(user);
    }

    public void ativarUsuario(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(UsuarioNaoEncontradoException::new);

        user.ativar();
        userRepository.save(user);
    }

    private String normalizar(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }
}