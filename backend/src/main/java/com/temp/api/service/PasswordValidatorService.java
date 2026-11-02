package com.temp.api.service;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class PasswordValidatorService {

    public void validar(String senha) {
        int minCaracteres = 8;
        int maxCaracteres = 72;

        if (senha.length() < minCaracteres) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "A senha deve ter no mínimo " + minCaracteres + " caracteres."
            );
        }

        if(senha.length() > maxCaracteres) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "A senha deve ter no máximo " + maxCaracteres + " caracteres."
            );
        }

        String regex = "^(?=.*[A-Z])(?=.*[!@#$%^&*(),.?\":{}|<>_-])(?=.*\\d.*\\d.*\\d).*$";

        if (!senha.matches(regex)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "A senha deve conter pelo menos 1 letra maiúscula, 1 caractere especial e mais de 2 números."
            );
        }
    }
}