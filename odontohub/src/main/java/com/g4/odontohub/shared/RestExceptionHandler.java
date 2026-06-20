package com.g4.odontohub.shared;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Tratamento global de exceções de validação de domínio.
 *
 * <p>Converte erros de regra de negócio em respostas HTTP amigáveis com corpo
 * {@code {"erro": "..."}} (mesmo formato usado pelo contexto de Clínica e
 * esperado pelo frontend), em vez de devolver 500 cru.</p>
 *
 * <p>Handlers locais (declarados dentro de um {@code *RestController}) têm
 * precedência sobre este advice global — então controllers que já tratam essas
 * exceções de forma específica (ex.: Prontuário → 404/409) continuam iguais.</p>
 */
@RestControllerAdvice
public class RestExceptionHandler {

    /** Validação de entrada / regra violada → 400 Bad Request. */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErroResponse> tratarValidacao(IllegalArgumentException e) {
        return ResponseEntity.badRequest().body(new ErroResponse(e.getMessage()));
    }

    /** Conflito de estado (ex.: operação inválida para o estado atual) → 409 Conflict. */
    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ErroResponse> tratarConflito(IllegalStateException e) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(new ErroResponse(e.getMessage()));
    }

    public record ErroResponse(String erro) {}
}
