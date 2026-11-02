package com.temp.api.service;

import com.temp.api.dto.AuthResponseDTO;
import com.temp.api.dto.LoginRequestDTO;
import com.temp.api.dto.RegisterRequestDTO;
import com.temp.api.dto.UserResponseDTO;
import com.temp.api.exceptions.CredenciaisInvalidasException;
import com.temp.api.exceptions.EmailJaCadastradoException;
import com.temp.api.exceptions.UsuarioNaoEncontradoException;
import com.temp.api.model.Role;
import com.temp.api.model.User;
import com.temp.api.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    private static final String EMAIL = "ana@email.com";
    private static final String SENHA = "Senha@123";
    private static final Long USER_ID = 1L;

    @Mock
    private UserRepository userRepository;

    @Mock
    private JwtTokenService tokenService;

    @Mock
    private PasswordValidatorService passwordValidatorService;

    // Encoder real, com custo baixo só para os testes ficarem rápidos.
    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder(4);

    private AuthService authService;

    @BeforeEach
    void setUp() {
        authService = new AuthService(
                userRepository, passwordEncoder, tokenService, passwordValidatorService);
    }

    // ---------------------------------------------------------------- cadastro

    @Test
    void cadastrar_validaASenhaAntesDeConsultarOBanco() {
        when(userRepository.existsByEmail(EMAIL)).thenReturn(false);
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        authService.cadastrar(new RegisterRequestDTO("Ana", EMAIL, SENHA));

        InOrder ordem = inOrder(passwordValidatorService, userRepository);
        ordem.verify(passwordValidatorService).validar(SENHA);
        ordem.verify(userRepository).existsByEmail(EMAIL);
        ordem.verify(userRepository).save(any(User.class));
    }

    @Test
    void cadastrar_normalizaEmailEGravaHashDaSenha() {
        when(userRepository.existsByEmail(EMAIL)).thenReturn(false);
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        UserResponseDTO resposta =
                authService.cadastrar(new RegisterRequestDTO(" Ana ", "  Ana@Email.COM ", SENHA));

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        User salvo = captor.getValue();

        assertThat(salvo.getEmail()).isEqualTo(EMAIL);
        assertThat(salvo.getNome()).isEqualTo("Ana");
        assertThat(salvo.getRole()).isEqualTo(Role.USER);
        assertThat(salvo.getSenhaHash()).isNotEqualTo(SENHA);
        assertThat(passwordEncoder.matches(SENHA, salvo.getSenhaHash())).isTrue();
        assertThat(resposta.email()).isEqualTo(EMAIL);
    }

    @Test
    void cadastrar_senhaInvalida_propagaExcecaoENaoTocaNoBanco() {
        doThrow(new ResponseStatusException(HttpStatus.BAD_REQUEST, "Senha fraca"))
                .when(passwordValidatorService).validar("123");

        assertThatThrownBy(() ->
                authService.cadastrar(new RegisterRequestDTO("Ana", EMAIL, "123")))
                .isInstanceOfSatisfying(ResponseStatusException.class,
                        e -> assertThat(e.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST));

        verifyNoInteractions(userRepository);
    }

    @Test
    void cadastrar_emailJaCadastrado_lancaEmailJaCadastrado() {
        when(userRepository.existsByEmail(EMAIL)).thenReturn(true);

        assertThatThrownBy(() ->
                authService.cadastrar(new RegisterRequestDTO("Ana", EMAIL, SENHA)))
                .isInstanceOf(EmailJaCadastradoException.class);

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void cadastrar_corridaNoBanco_converteParaEmailJaCadastrado() {
        when(userRepository.existsByEmail(EMAIL)).thenReturn(false);
        when(userRepository.save(any(User.class)))
                .thenThrow(new DataIntegrityViolationException("uk_users_email"));

        assertThatThrownBy(() ->
                authService.cadastrar(new RegisterRequestDTO("Ana", EMAIL, SENHA)))
                .isInstanceOf(EmailJaCadastradoException.class);
    }

    // ------------------------------------------------------------------- login

    @Test
    void autenticar_credenciaisValidas_retornaTokenSemRevalidarForcaDaSenha() {
        User user = usuario(true);
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(user));
        when(tokenService.gerarToken(user)).thenReturn("jwt-de-teste");
        when(tokenService.getValidadeEmSegundos()).thenReturn(3600L);

        AuthResponseDTO resposta =
                authService.autenticar(new LoginRequestDTO("  ANA@email.com ", SENHA));

        assertThat(resposta.accessToken()).isEqualTo("jwt-de-teste");
        assertThat(resposta.tokenType()).isEqualTo("Bearer");
        assertThat(resposta.expiresInSeconds()).isEqualTo(3600L);
        // A regra de força vale só para senhas novas, nunca para o login.
        verifyNoInteractions(passwordValidatorService);
    }

    @Test
    void autenticar_senhaErrada_lancaCredenciaisInvalidas() {
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(usuario(true)));

        assertThatThrownBy(() ->
                authService.autenticar(new LoginRequestDTO(EMAIL, "Senha@999")))
                .isInstanceOf(CredenciaisInvalidasException.class);
    }

    @Test
    void autenticar_emailInexistente_lancaCredenciaisInvalidas() {
        when(userRepository.findByEmail("ninguem@email.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                authService.autenticar(new LoginRequestDTO("ninguem@email.com", SENHA)))
                .isInstanceOf(CredenciaisInvalidasException.class);
    }

    @Test
    void autenticar_usuarioInativo_lancaCredenciaisInvalidas() {
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(usuario(false)));

        assertThatThrownBy(() ->
                authService.autenticar(new LoginRequestDTO(EMAIL, SENHA)))
                .isInstanceOf(CredenciaisInvalidasException.class);
    }

    // ------------------------------------------------------------------ perfil

    @Test
    void buscarPerfil_usuarioAtivo_retornaDados() {
        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(usuario(true)));

        UserResponseDTO perfil = authService.buscarPerfil(USER_ID);

        assertThat(perfil.email()).isEqualTo(EMAIL);
        assertThat(perfil.nome()).isEqualTo("Ana");
        assertThat(perfil.role()).isEqualTo("USER");
    }

    @Test
    void buscarPerfil_usuarioInexistente_lancaUsuarioNaoEncontrado() {
        when(userRepository.findById(USER_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.buscarPerfil(USER_ID))
                .isInstanceOf(UsuarioNaoEncontradoException.class);
    }

    @Test
    void buscarPerfil_usuarioInativo_lancaUsuarioNaoEncontrado() {
        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(usuario(false)));

        assertThatThrownBy(() -> authService.buscarPerfil(USER_ID))
                .isInstanceOf(UsuarioNaoEncontradoException.class);
    }

    // --------------------------------------------------- ativar / desativar

    @Test
    void desativarUsuario_marcaComoInativoEGrava() {
        User user = usuario(true);
        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));

        authService.desativarUsuario(USER_ID);

        assertThat(user.isAtivo()).isFalse();
        verify(userRepository).save(user);
    }

    @Test
    void desativarUsuario_usuarioInexistente_lancaUsuarioNaoEncontrado() {
        when(userRepository.findById(USER_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.desativarUsuario(USER_ID))
                .isInstanceOf(UsuarioNaoEncontradoException.class);

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void ativarUsuario_marcaComoAtivoEGrava() {
        User user = usuario(false);
        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));

        authService.ativarUsuario(USER_ID);

        assertThat(user.isAtivo()).isTrue();
        verify(userRepository).save(user);
    }

    @Test
    void ativarUsuario_usuarioInexistente_lancaUsuarioNaoEncontrado() {
        when(userRepository.findById(USER_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.ativarUsuario(USER_ID))
                .isInstanceOf(UsuarioNaoEncontradoException.class);

        verify(userRepository, never()).save(any(User.class));
    }

    // ----------------------------------------------------------------- apoio

    private User usuario(boolean ativo) {
        User user = new User("Ana", EMAIL, passwordEncoder.encode(SENHA), Role.USER);
        if (!ativo) {
            user.desativar();
        }
        return user;
    }
}