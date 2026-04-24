# OdontoHub

Sistema de gestão odontológica desenvolvido com arquitetura DDD (Domain-Driven Design) e automação de testes BDD/Cucumber.

## Pré-requisitos

- Java 21+
- Nenhuma instalação extra necessária (Maven Wrapper incluído)

## Rodar os testes

Abra o terminal na pasta `odontohub/` e execute:

**Windows (PowerShell ou CMD):**
```
cd odontohub
.\mvnw.cmd test --no-transfer-progress
```

**Linux/Mac:**
```
cd odontohub
./mvnw test --no-transfer-progress
```

O resultado esperado no final é:

```
19 Scenarios (19 passed)
109 Steps (109 passed)
BUILD SUCCESS
```

## Funcionalidades implementadas (BDD)

| Feature | Descrição | Cenários |
|---------|-----------|----------|
| F01 | Agendamento de Consultas e Retornos | 8 |
| F02 | Gestão da Ficha Clínica | 6 |
| F03 | Registro de Anamnese | 5 |

## Estrutura do projeto

```
odontohub/
├── src/main/java/com/g4/odontohub/
│   ├── agendamento/
│   │   ├── domain/          # Entidades e interfaces (Java puro, sem JPA)
│   │   └── application/     # Serviços de orquestração
│   ├── prontuario/
│   │   ├── domain/          # Prontuario, FichaClinica, Anamnese
│   │   └── application/     # ProntuarioService, AnamneseService
│   └── shared/exception/    # DomainException
└── src/test/
    ├── java/com/g4/odontohub/
    │   ├── steps/           # Step Definitions do Cucumber
    │   └── infra/           # Repositórios em memória para testes
    └── resources/features/  # Arquivos .feature em português
```
