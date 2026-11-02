package com.temp.api.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PasswordValidatorServiceTest {

    private static final String MSG_MINIMO = "A senha deve ter no mínimo 8 caracteres.";
    private static final String MSG_MAXIMO = "A senha deve ter no máximo 20 caracteres.";
    private static final String MSG_REGRA =
            "A senha deve conter pelo menos 1 letra maiúscula, 1 caractere especial e mais de 2 números.";

    private final PasswordValidatorService validator = new PasswordValidatorService();

    // ------------------------------------------------------------- senhas aceitas

    @ParameterizedTest
    @ValueSource(strings = {
            "Senha@123",
            "SENHA@123",
            "Abcdef!123",
            "S1e2n3@ha",       // números separados
            "senhA_9x8y7",
            "Senha-1234"
    })
    void validar_senhaQueAtendeATodasAsRegras_naoLancaExcecao(String senha) {
        assertThatCode(() -> validator.validar(senha)).doesNotThrowAnyException();
    }

    @Test
    void validar_senhaComExatamente8Caracteres_eAceita() {
        assertThatCode(() -> validator.validar("Ab@1c2d3")).doesNotThrowAnyException();
    }

    @Test
    void validar_senhaComExatamente20Caracteres_eAceita() {
        String senha = "Aa@123" + "x".repeat(14);
        assertThat(senha).hasSize(20);

        assertThatCode(() -> validator.validar(senha)).doesNotThrowAnyException();
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "!", "@", "#", "$", "%", "^", "&", "*", "(", ")", ",", ".", "?",
            "\"", ":", "{", "}", "|", "<", ">", "_", "-"
    })
    void validar_cadaCaractereEspecialPermitido_eAceito(String especial) {
        assertThatCode(() -> validator.validar("Senha123" + especial))
                .doesNotThrowAnyException();
    }

    // ----------------------------------------------------------------- tamanho

    @ParameterizedTest
    @ValueSource(strings = {"", "A@1", "Ab@123x"})
    void validar_senhaComMenosDe8Caracteres_lancaBadRequestComMensagemDeMinimo(String senha) {
        assertSenhaRejeitada(senha, MSG_MINIMO);
    }

    @Test
    void validar_senhaComMaisDe20Caracteres_lancaBadRequestComMensagemDeMaximo() {
        String senha = "Aa@123" + "x".repeat(15);
        assertThat(senha).hasSize(21);

        assertSenhaRejeitada(senha, MSG_MAXIMO);
    }

    @Test
    void validar_tamanhoTemPrioridadeSobreARegraDeComposicao() {
        // Curta e sem maiúscula, especial ou números: deve reclamar do tamanho primeiro.
        assertSenhaRejeitada("abc", MSG_MINIMO);
    }

    @Test
    void validar_senhaNoLimiteComCaracteresDe3Bytes_nuncaExcedeOLimiteDoBcrypt() {
        // "日" ocupa 1 unidade UTF-16 e 3 bytes em UTF-8: o pior caso por unidade.
        String senha = "Aa@123" + "\u65E5".repeat(14);
        assertThat(senha).hasSize(20);

        assertThatCode(() -> validator.validar(senha)).doesNotThrowAnyException();
        // O BCrypt só aceita até 72 bytes; com no máximo 20 unidades o teto é 60.
        assertThat(senha.getBytes(StandardCharsets.UTF_8).length).isLessThanOrEqualTo(72);
    }

    // ------------------------------------------------------------- composição

    @Test
    void validar_semLetraMaiuscula_eRejeitada() {
        assertSenhaRejeitada("senha@123", MSG_REGRA);
    }

    @Test
    void validar_semCaractereEspecial_eRejeitada() {
        assertSenhaRejeitada("Senha1234", MSG_REGRA);
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "Senha@ab",   // nenhum número
            "Senha@1a",   // 1 número
            "Senha@12"    // 2 números
    })
    void validar_comMenosDeTresNumeros_eRejeitada(String senha) {
        assertSenhaRejeitada(senha, MSG_REGRA);
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "Senha123+",   // '+' não está na lista de especiais aceitos
            "Senha123~",
            "Senha123/",
            "Senha123;",
            "Senha123 "    // espaço
    })
    void validar_caractereForaDaListaDePermitidos_naoContaComoEspecial(String senha) {
        assertSenhaRejeitada(senha, MSG_REGRA);
    }

    // ----------------------------------------------------------------- apoio

    private void assertSenhaRejeitada(String senha, String mensagemEsperada) {
        assertThatThrownBy(() -> validator.validar(senha))
                .isInstanceOfSatisfying(ResponseStatusException.class, e -> {
                    assertThat(e.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
                    assertThat(e.getReason()).isEqualTo(mensagemEsperada);
                });
    }
}