package com.temp.api.exceptions;

public class UsuarioNaoEncontradoException extends RuntimeException{
    public UsuarioNaoEncontradoException() {
        super("Usuário não encontrado!");
    }
}
