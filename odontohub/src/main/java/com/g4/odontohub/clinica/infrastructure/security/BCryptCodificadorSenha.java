package com.g4.odontohub.clinica.infrastructure.security;

import com.g4.odontohub.clinica.domain.service.CodificadorSenha;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/** Adapter de infraestrutura da porta {@link CodificadorSenha} usando BCrypt. */
public class BCryptCodificadorSenha implements CodificadorSenha {

    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    @Override
    public String codificar(String senhaPura) {
        return encoder.encode(senhaPura);
    }

    @Override
    public boolean corresponde(String senhaPura, String hashArmazenado) {
        if (senhaPura == null || hashArmazenado == null) {
            return false;
        }
        return encoder.matches(senhaPura, hashArmazenado);
    }
}
