package com.g4.odontohub.clinica.domain.service;

/**
 * Porta de domínio para codificação/verificação de senhas.
 * Mantém o algoritmo (BCrypt, etc.) fora do domínio — a implementação concreta
 * fica na camada de infraestrutura.
 */
public interface CodificadorSenha {

    /** Devolve o hash seguro da senha em texto puro. */
    String codificar(String senhaPura);

    /** Verifica se a senha em texto puro corresponde ao hash armazenado. */
    boolean corresponde(String senhaPura, String hashArmazenado);
}
