# Room Reservation API

API RESTful de **Reserva de Salas** desenvolvida em Java 17 com Spring Boot 3.x. O projeto segue arquitetura em camadas, princípios SOLID, Clean Code e inclui validações de negócio, tratamento global de exceções, documentação OpenAPI e testes automatizados.

---

## Sumário

- [Visão Geral](#visão-geral)
- [Stack Tecnológica](#stack-tecnológica)
- [Arquitetura](#arquitetura)
- [Estrutura do Projeto](#estrutura-do-projeto)
- [Pré-requisitos](#pré-requisitos)
- [Instalação e Execução](#instalação-e-execução)
- [Configuração](#configuração)
- [Modelo de Domínio](#modelo-de-domínio)
- [Regras de Negócio](#regras-de-negócio)
- [Documentação da API](#documentação-da-api)
- [Tratamento de Erros](#tratamento-de-erros)
- [Testes Automatizados](#testes-automatizados)
- [Swagger / OpenAPI](#swagger--openapi)
- [Decisões de Design](#decisões-de-design)
- [Exemplos com cURL](#exemplos-com-curl)
- [Roadmap](#roadmap)

---

## Visão Geral

A **Room Reservation API** permite gerenciar salas de reunião e suas reservas. O sistema garante que:

- Apenas salas **ativas** possam receber reservas.
- Não existam **conflitos de horário** entre reservas confirmadas na mesma sala.
- Reservas no **passado** sejam rejeitadas.
- Datas inválidas (`startTime >= endTime`) sejam bloqueadas.

A API expõe endpoints REST versionados em `/api/v1` e retorna respostas padronizadas em JSON, incluindo erros estruturados.

---

## Stack Tecnológica

| Tecnologia | Versão | Finalidade |
|---|---|---|
| Java | 17+ | Linguagem base |
| Spring Boot | 3.3.3 | Framework principal |
| Spring Web | — | REST API |
| Spring Data JPA | — | Persistência |
| Spring Validation | — | Validação de DTOs |
| PostgreSQL | — | Banco de dados relacional |
| Lombok | 1.18.46 | Redução de boilerplate |
| MapStruct | 1.6.0 | Mapeamento Entity ↔ DTO |
| Springdoc OpenAPI | 2.6.0 | Documentação Swagger |
| JUnit 5 + Mockito | — | Testes unitários |
| Spring Boot Test (MockMvc) | — | Testes de integração |

---

## Arquitetura

O projeto adota **arquitetura em camadas** com separação clara de responsabilidades:

```
┌─────────────────────────────────────────────────────────┐
│                    Controller Layer                      │
│         RoomController / ReservationController           │
│              (HTTP, validação de DTO, status)            │
└──────────────────────────┬──────────────────────────────┘
                           │
┌──────────────────────────▼──────────────────────────────┐
│                     Service Layer                        │
│           RoomService / ReservationService               │
│         (regras de negócio, transações, orquestração)    │
└──────────────────────────┬──────────────────────────────┘
                           │
┌──────────────────────────▼──────────────────────────────┐
│                   Repository Layer                       │
│         RoomRepository / ReservationRepository           │
│              (acesso a dados via Spring Data JPA)        │
└──────────────────────────┬──────────────────────────────┘
                           │
┌──────────────────────────▼──────────────────────────────┐
│                   PostgreSQL Database                    │
│                  (rooms / reservations)                  │
└─────────────────────────────────────────────────────────┘

Camadas transversais:
  • DTOs + Mappers (MapStruct) — contratos de entrada/saída
  • Exceptions + GlobalExceptionHandler — erros padronizados
```

### Fluxo de uma requisição de reserva

```
Cliente HTTP
    │
    ▼
ReservationController  ──►  Valida DTO (@Valid)
    │
    ▼
ReservationService     ──►  Valida datas, sala ativa, conflitos
    │
    ▼
ReservationRepository  ──►  Persiste no PostgreSQL
    │
    ▼
ReservationMapper      ──►  Converte Entity → ResponseDTO
    │
    ▼
ResponseEntity<ReservationResponseDTO>
```

---

## Estrutura do Projeto

```
RoomReservation2.0/
├── pom.xml
├── README.md
└── src/
    ├── main/
    │   ├── java/com/roomreservation/
    │   │   ├── RoomReservationApplication.java
    │   │   ├── controller/
    │   │   │   ├── RoomController.java
    │   │   │   └── ReservationController.java
    │   │   ├── service/
    │   │   │   ├── RoomService.java
    │   │   │   └── ReservationService.java
    │   │   ├── repository/
    │   │   │   ├── RoomRepository.java
    │   │   │   └── ReservationRepository.java
    │   │   ├── domain/entity/
    │   │   │   ├── Room.java
    │   │   │   ├── Reservation.java
    │   │   │   └── ReservationStatus.java
    │   │   ├── dto/
    │   │   │   ├── RoomRequestDTO.java
    │   │   │   ├── RoomResponseDTO.java
    │   │   │   ├── ReservationRequestDTO.java
    │   │   │   └── ReservationResponseDTO.java
    │   │   ├── mapper/
    │   │   │   ├── RoomMapper.java
    │   │   │   └── ReservationMapper.java
    │   │   └── exception/
    │   │       ├── GlobalExceptionHandler.java
    │   │       ├── StandardError.java
    │   │       ├── FieldMessage.java
    │   │       ├── RoomNotFoundException.java
    │   │       ├── ReservationNotFoundException.java
    │   │       ├── ReservationConflictException.java
    │   │       ├── InvalidDateException.java
    │   │       ├── InactiveRoomException.java
    │   │       └── BusinessException.java
    │   └── resources/
    │       └── application.yml
    └── test/
        └── java/com/roomreservation/
            ├── controller/
            │   └── ReservationControllerTest.java
            └── service/
                ├── RoomServiceTest.java
                └── ReservationServiceTest.java
```

---

## Pré-requisitos

| Requisito | Versão mínima |
|---|---|
| JDK | 17 (recomendado; evite JDK 26 sem Lombok 1.18.46+) |
| Maven | 3.8+ |
| PostgreSQL | 13+ |
| Git | Qualquer versão recente |

---

## Instalação e Execução

### 1. Clone ou acesse o projeto

```bash
cd D:\Projetos\RoomReservation2.0
```

### 2. Crie o banco de dados PostgreSQL

```sql
CREATE DATABASE room_reservation;
```

### 3. Configure as credenciais

Edite `src/main/resources/application.yml` se necessário:

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/room_reservation
    username: postgres
    password: postgres
```

### 4. Compile e execute os testes

```powershell
# Windows — use JDK 17
$env:JAVA_HOME="C:\Program Files\Java\jdk-17"
mvn clean test
```

### 5. Inicie a aplicação

```powershell
mvn spring-boot:run
```

A API estará disponível em: **http://localhost:8080**

---

## Configuração

| Propriedade | Valor padrão | Descrição |
|---|---|---|
| `server.port` | `8080` | Porta HTTP |
| `spring.datasource.url` | `jdbc:postgresql://localhost:5432/room_reservation` | URL do banco |
| `spring.jpa.hibernate.ddl-auto` | `update` | Cria/atualiza tabelas automaticamente |
| `springdoc.swagger-ui.path` | `/swagger-ui.html` | Interface Swagger |
| `springdoc.api-docs.path` | `/api-docs` | JSON OpenAPI |

---

## Modelo de Domínio

### Entidade: Room (Sala)

| Campo | Tipo | Restrições | Descrição |
|---|---|---|---|
| `id` | UUID | PK, auto-gerado | Identificador único |
| `name` | String | NOT NULL, UNIQUE | Nome da sala |
| `capacity` | Integer | NOT NULL, mínimo 1 | Capacidade de pessoas |
| `active` | Boolean | NOT NULL, default `true` | Indica se a sala está disponível |

### Entidade: Reservation (Reserva)

| Campo | Tipo | Restrições | Descrição |
|---|---|---|---|
| `id` | UUID | PK, auto-gerado | Identificador único |
| `room` | Room | FK, NOT NULL | Sala reservada |
| `reservedBy` | String | NOT NULL | Nome ou ID do solicitante |
| `startTime` | LocalDateTime | NOT NULL | Início da reserva |
| `endTime` | LocalDateTime | NOT NULL | Fim da reserva |
| `status` | ReservationStatus | NOT NULL, default `PENDING` | Status da reserva |

### Enum: ReservationStatus

| Valor | Descrição |
|---|---|
| `PENDING` | Reserva criada, aguardando confirmação |
| `CONFIRMED` | Reserva confirmada; participa da verificação de conflito |
| `CANCELLED` | Reserva cancelada; não ocupa horário |

### Diagrama ER

```
┌─────────────────────┐         ┌──────────────────────────┐
│        rooms        │         │       reservations        │
├─────────────────────┤         ├──────────────────────────┤
│ id        UUID (PK) │◄────────│ room_id      UUID (FK)   │
│ name      VARCHAR   │   1:N   │ id           UUID (PK)   │
│ capacity  INTEGER   │         │ reserved_by  VARCHAR     │
│ active    BOOLEAN   │         │ start_time   TIMESTAMP   │
└─────────────────────┘         │ end_time     TIMESTAMP   │
                                │ status       VARCHAR     │
                                └──────────────────────────┘
```

---

## Regras de Negócio

### Validações no DTO (Bean Validation)

| Campo | Anotação | Regra |
|---|---|---|
| `RoomRequestDTO.name` | `@NotBlank` | Nome obrigatório |
| `RoomRequestDTO.capacity` | `@NotNull`, `@Min(1)` | Capacidade mínima 1 |
| `ReservationRequestDTO.roomId` | `@NotNull` | Sala obrigatória |
| `ReservationRequestDTO.reservedBy` | `@NotBlank` | Solicitante obrigatório |
| `ReservationRequestDTO.startTime` | `@NotNull`, `@FutureOrPresent` | Não pode ser no passado |
| `ReservationRequestDTO.endTime` | `@NotNull`, `@FutureOrPresent` | Não pode ser no passado |

### Validações no Service

| Regra | Exceção | HTTP |
|---|---|---|
| `startTime` deve ser anterior a `endTime` | `InvalidDateException` | 400 |
| Reservas no passado não são permitidas | `InvalidDateException` | 400 |
| Sala deve existir | `RoomNotFoundException` | 404 |
| Sala deve estar ativa | `InactiveRoomException` | 400 |
| Conflito de horário (status CONFIRMED) | `ReservationConflictException` | 409 |
| Reserva não encontrada | `ReservationNotFoundException` | 404 |
| Nome de sala duplicado | `ReservationConflictException` | 409 |
| Confirmar reserva cancelada | `ReservationConflictException` | 409 |

### Regra de Conflito de Horários

Duas reservas **CONFIRMED** conflitam quando seus intervalos se sobrepõem:

```
Conflito:  startA < endB  AND  endA > startB

Exemplo de conflito:
  Reserva A: 14:00 ──────── 16:00
  Reserva B:       15:00 ──────── 17:00  ✗ CONFLITO

Exemplo sem conflito:
  Reserva A: 14:00 ──── 15:00
  Reserva B:                 15:00 ──── 16:00  ✓ OK (adjacentes)
```

Reservas com status `PENDING` ou `CANCELLED` **não** bloqueiam horários.

---

## Documentação da API

Base URL: `http://localhost:8080/api/v1`

### Salas — `/rooms`

#### Listar todas as salas

```
GET /api/v1/rooms
```

**Resposta 200:**
```json
[
  {
    "id": "3fa85f64-5717-4562-b3fc-2c963f66afa6",
    "name": "Sala Alpha",
    "capacity": 12,
    "active": true
  }
]
```

---

#### Buscar sala por ID

```
GET /api/v1/rooms/{id}
```

| Parâmetro | Tipo | Descrição |
|---|---|---|
| `id` | UUID | ID da sala |

**Resposta 200:** objeto `RoomResponseDTO`  
**Resposta 404:** sala não encontrada

---

#### Criar sala

```
POST /api/v1/rooms
Content-Type: application/json
```

**Request Body:**
```json
{
  "name": "Sala Alpha",
  "capacity": 12,
  "active": true
}
```

**Resposta 201:** sala criada  
**Resposta 400:** validação de DTO falhou  
**Resposta 409:** nome de sala já existe

---

#### Atualizar sala

```
PUT /api/v1/rooms/{id}
Content-Type: application/json
```

**Request Body:** mesmo formato de criação.

**Resposta 200:** sala atualizada  
**Resposta 404:** sala não encontrada  
**Resposta 409:** nome duplicado

---

#### Excluir sala

```
DELETE /api/v1/rooms/{id}
```

**Resposta 204:** sala removida  
**Resposta 404:** sala não encontrada

---

### Reservas — `/reservations`

#### Listar todas as reservas

```
GET /api/v1/reservations
```

**Resposta 200:**
```json
[
  {
    "id": "7c9e6679-7425-40de-944b-e07fc1f90ae7",
    "roomId": "3fa85f64-5717-4562-b3fc-2c963f66afa6",
    "roomName": "Sala Alpha",
    "reservedBy": "Maria Silva",
    "startTime": "2026-08-25T14:00:00",
    "endTime": "2026-08-25T16:00:00",
    "status": "PENDING"
  }
]
```

---

#### Buscar reserva por ID

```
GET /api/v1/reservations/{id}
```

**Resposta 200:** objeto `ReservationResponseDTO`  
**Resposta 404:** reserva não encontrada

---

#### Criar reserva

```
POST /api/v1/reservations
Content-Type: application/json
```

**Request Body:**
```json
{
  "roomId": "3fa85f64-5717-4562-b3fc-2c963f66afa6",
  "reservedBy": "Maria Silva",
  "startTime": "2026-08-25T14:00:00",
  "endTime": "2026-08-25T16:00:00",
  "status": "PENDING"
}
```

| Campo | Obrigatório | Descrição |
|---|---|---|
| `roomId` | Sim | UUID da sala |
| `reservedBy` | Sim | Nome do solicitante |
| `startTime` | Sim | ISO-8601 (`yyyy-MM-dd'T'HH:mm:ss`) |
| `endTime` | Sim | ISO-8601 |
| `status` | Não | Default: `PENDING` |

**Resposta 201:** reserva criada  
**Resposta 400:** validação ou regra de negócio violada  
**Resposta 404:** sala não encontrada  
**Resposta 409:** conflito de horário (se status = CONFIRMED)

---

#### Atualizar reserva

```
PUT /api/v1/reservations/{id}
Content-Type: application/json
```

**Request Body:** mesmo formato de criação.

**Resposta 200:** reserva atualizada  
**Resposta 400/404/409:** conforme regras de negócio

---

#### Confirmar reserva

```
POST /api/v1/reservations/{id}/confirm
```

Altera o status para `CONFIRMED` e valida conflitos de horário.

**Resposta 200:** reserva confirmada  
**Resposta 409:** conflito ou reserva cancelada

---

#### Cancelar reserva

```
POST /api/v1/reservations/{id}/cancel
```

Altera o status para `CANCELLED`.

**Resposta 200:** reserva cancelada  
**Resposta 404:** reserva não encontrada

---

#### Excluir reserva

```
DELETE /api/v1/reservations/{id}
```

**Resposta 204:** reserva removida  
**Resposta 404:** reserva não encontrada

---

## Tratamento de Erros

Todos os erros retornam o objeto `StandardError`:

```json
{
  "timestamp": "2026-08-24T10:34:20",
  "status": 400,
  "error": "Validation Error",
  "message": "Invalid request payload",
  "path": "/api/v1/reservations",
  "fieldMessages": [
    {
      "field": "reservedBy",
      "message": "Reserved by is required"
    }
  ]
}
```

### Mapa de exceções → HTTP Status

| Exceção | Status | `error` |
|---|---|---|
| `MethodArgumentNotValidException` | 400 | Validation Error |
| `InvalidDateException` | 400 | Business Rule Violation |
| `InactiveRoomException` | 400 | Business Rule Violation |
| `BusinessException` | 400 | Business Rule Violation |
| `RoomNotFoundException` | 404 | Not Found |
| `ReservationNotFoundException` | 404 | Not Found |
| `ReservationConflictException` | 409 | Conflict |
| `DataIntegrityViolationException` | 409 | Conflict |
| `Exception` (genérica) | 500 | Internal Server Error |

---

## Testes Automatizados

### Executar todos os testes

```powershell
$env:JAVA_HOME="C:\Program Files\Java\jdk-17"
mvn clean test
```

### Cobertura de testes

| Classe de Teste | Tipo | Cenários |
|---|---|---|
| `RoomServiceTest` | Unitário (Mockito) | Criação, nome duplicado, sala inativa, not found |
| `ReservationServiceTest` | Unitário (Mockito) | Criação, conflito, datas inválidas, passado, sala inativa |
| `ReservationControllerTest` | Integração (MockMvc) | POST 201, validação DTO 400, regra de negócio 400 |

### Exemplo de teste unitário — conflito de horários

```java
@Test
void shouldThrowConflictWhenConfirmedReservationOverlaps() {
    when(roomService.getActiveRoomOrThrow(roomId)).thenReturn(room);
    when(reservationRepository.existsOverlappingReservation(...)).thenReturn(true);

    assertThatThrownBy(() -> reservationService.create(requestDTO))
        .isInstanceOf(ReservationConflictException.class);
}
```

---

## Swagger / OpenAPI

Com a aplicação em execução:

| Recurso | URL |
|---|---|
| Swagger UI | http://localhost:8080/swagger-ui.html |
| OpenAPI JSON | http://localhost:8080/api-docs |

A documentação interativa permite testar todos os endpoints diretamente pelo navegador.

---

## Decisões de Design

### SOLID aplicado

| Princípio | Aplicação no projeto |
|---|---|
| **S** — Single Responsibility | Cada camada tem uma responsabilidade: Controller (HTTP), Service (negócio), Repository (dados) |
| **O** — Open/Closed | Novas regras de negócio podem ser adicionadas no Service sem alterar Controllers |
| **L** — Liskov Substitution | Repositories estendem interfaces Spring Data sem quebrar contratos |
| **I** — Interface Segregation | Mappers MapStruct expõem apenas métodos necessários |
| **D** — Dependency Inversion | Services dependem de abstrações (Repositories, Mappers) via injeção de construtor |

### Clean Code

- Injeção de dependências via construtor com `@RequiredArgsConstructor`
- DTOs separados das entidades JPA (evita exposição do modelo de persistência)
- Exceções customizadas com nomes semânticos
- Métodos privados de validação no Service (`validateDateRange`, `validateNoConflict`)
- Nomenclatura expressiva e consistente em inglês no código

### MapStruct

Gera implementações de mapeamento em tempo de compilação, eliminando código manual propenso a erros:

```java
@Mapper(componentModel = "spring")
public interface ReservationMapper {
    @Mapping(source = "room.id", target = "roomId")
    @Mapping(source = "room.name", target = "roomName")
    ReservationResponseDTO toResponseDTO(Reservation reservation);
}
```

---

## Exemplos com cURL

### Criar uma sala

```bash
curl -X POST http://localhost:8080/api/v1/rooms \
  -H "Content-Type: application/json" \
  -d '{"name":"Sala Alpha","capacity":12,"active":true}'
```

### Criar uma reserva

```bash
curl -X POST http://localhost:8080/api/v1/reservations \
  -H "Content-Type: application/json" \
  -d '{
    "roomId": "UUID-DA-SALA",
    "reservedBy": "Maria Silva",
    "startTime": "2026-08-25T14:00:00",
    "endTime": "2026-08-25T16:00:00",
    "status": "PENDING"
  }'
```

### Confirmar uma reserva

```bash
curl -X POST http://localhost:8080/api/v1/reservations/UUID-DA-RESERVA/confirm
```

### Cancelar uma reserva

```bash
curl -X POST http://localhost:8080/api/v1/reservations/UUID-DA-RESERVA/cancel
```

### Listar salas

```bash
curl http://localhost:8080/api/v1/rooms
```

---

## Roadmap

Melhorias sugeridas para evolução do projeto:

- [ ] Autenticação e autorização (Spring Security + JWT)
- [ ] Paginação e filtros nos endpoints de listagem
- [ ] Flyway/Liquibase para migrations versionadas
- [ ] Perfil Docker Compose (app + PostgreSQL)
- [ ] Testes de integração com Testcontainers
- [ ] Cache de salas com Redis
- [ ] Notificações por e-mail ao confirmar/cancelar reserva
- [ ] CI/CD com GitHub Actions

---

## Licença

Projeto de demonstração — uso livre para fins educacionais e comerciais.
