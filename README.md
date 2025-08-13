
# README.md

# ForumHub Backend (Spring Boot) - Challenge Alura

API REST do fórum com autenticação JWT, controle de acesso por perfis (ADMIN/USER), CRUD de tópicos, paginação/filtro, e migrações com Flyway.

## Stack

* **Java 17**, **Spring Boot 4.0.0-SNAPSHOT**
* **Spring Security**, **JPA/Hibernate**, **Validation**
* **MySQL**, **Flyway**
* **JJWT 0.11.5** (HS256)
* **Lombok**

## Arquitetura (visão rápida)

* **Domínios**: `Topico`, `Usuario` (implementa `UserDetails`), `UserSession`
* **Auth**:

    * `JwtService` (gera/valida token com `sessionId`)
    * `JwtAuthFilter` (lê `Authorization: Bearer`, valida e popula `SecurityContext`)
    * **Token único**: filtro confere se o token = `ultimoToken` em `user_session`
* **Security**:

    * `SecurityConfig` (stateless, CORS liberado, rotas públicas e protegidas)
    * `CustomAuthEntryPoint` (401), `CustomAccessDeniedHandler` (403)
    * **Method Security**: `@PreAuthorize("hasRole('ADMIN')")` no DELETE total
* **Exceções globais**:

    * 400 (`IllegalArgumentException` etc.), 401 (`UnauthorizedException`), 403 (`Forbidden/AuthorizationDenied`), 404/409 (duplicidades), etc.
* **Tópicos**:

    * CRUD + busca paginada por `curso` e/ou `ano`
    * Unicidade (`titulo`+`mensagem`) via coluna hash (MySQL `GENERATED ALWAYS`)

## Requisitos

* JDK 17 + Maven
* MySQL ativo

## Configuração

Arquivo `application.properties` (já incluso):

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/forumhub?createDatabaseIfNotExist=true&useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC
spring.datasource.username=root
spring.datasource.password=${MYSQL_PASSWORD:251216}

spring.jpa.hibernate.ddl-auto=validate
spring.flyway.enabled=true

# JWT
security.jwt.secret=BASE64_COM_PELO_MENOS_32_BYTES
security.jwt.expiration-seconds=900
```

### Gerar `security.jwt.secret` (32B base64)

* **OpenSSL (Linux/macOS/WSL):**

  ```bash
  openssl rand -base64 32
  ```
* **PowerShell (Windows):**

  ```powershell
  $bytes = New-Object 'System.Byte[]' 32; (New-Object System.Security.Cryptography.RNGCryptoServiceProvider).GetBytes($bytes); [Convert]::ToBase64String($bytes)
  ```

## Banco & Migrações

* Flyway habilitado. Tabelas: `topico`, `usuarios`, `user_session` (+ seed de admin).
* **Usuário seed (DEV):** `admin@forum.com` / **123456** (bcrypt no script). Troque em produção.

## Executando

```bash
mvn spring-boot:run
```

---

## Autenticação & Regras de Perfil

### Login

`POST /auth/login`

```json
{ "login": "admin@forum.com", "senha": "123456" }
```

**200** → `{ "token": "...", "type": "Bearer" }`

> O token carrega `sub` (login) e `sessionId`. O último token emitido é salvo em `user_session` e **apenas ele** é aceito nas próximas requisições.

### Criar usuário

`POST /auth/criar-usuario`

| role enviada            | JWT                   | Resultado                                     |
| ----------------------- | --------------------- | --------------------------------------------- |
| `""`/`null`             | nenhum                | cria **USER** (200)                           |
| `"USER"`                | nenhum                | cria **USER** (200)                           |
| `"ADMIN"`               | **ADMIN JWT**         | cria **ADMIN** (200)                          |
| `"ADMIN"`               | sem/usuário não admin | **401** (sem auth) ou **403** (não admin)     |
| inválida (ex.: `"GOD"`) | qualquer              | **400** `"Role inválida. Use ADMIN ou USER."` |

Exemplo (criar ADMIN):

```bash
curl -X POST http://localhost:8080/auth/criar-usuario \
 -H "Authorization: Bearer <ADMIN_TOKEN>" \
 -H "Content-Type: application/json" \
 -d '{ "login":"novo.admin@forum.com", "senha":"123456", "role":"ADMIN" }'
```

---

## Tópicos

### Criar

`POST /topicos` (público)

```json
{
  "titulo":"Título",
  "mensagem":"Mensagem",
  "autor":"Autor",
  "curso":"Curso"
}
```

* **409** se já existir `titulo+mensagem`.

### Listar (público)

`GET /topicos?curso=java&ano=2024&top10=true`

* `top10=true` → 10 primeiros por `dataCriacao` ASC
* Padrão com paginação (`Pageable`)

### Detalhar (público)

`GET /topicos/{id}`

### Atualizar

`PUT /topicos/{id}` (público no código atual)

### Excluir

`DELETE /topicos/{id}` (público no código atual)

### Excluir **todos** (somente ADMIN)

`DELETE /topicos/todos`

* **403** (JSON): `"Apenas usuários ADMIN podem apagar todos os tópicos."`
* **401** se sem JWT válido
* **204** (No Content) se ADMIN

Exemplo:

```bash
curl -X DELETE http://localhost:8080/topicos/todos \
 -H "Authorization: Bearer <ADMIN_TOKEN>"
```

---

## Padrão de Erros (exemplos)

* **400** `{"status":400,"error":"Bad Request","message":"Role inválida. Use ADMIN ou USER."}`
* **401** `{"status":401,"error":"Unauthorized","message":"Você precisa estar autenticado para acessar este recurso."}`
* **403** `{"status":403,"error":"Forbidden","message":"Apenas usuários ADMIN podem apagar todos os tópicos."}`
* **404** `{"status":404,"error":"Not Found","message":"..."}`
* **409** `{"status":409,"error":"Conflict","message":"Já existe um tópico com este título e mensagem."}`

---

## Notas de Segurança

* **Stateless** (sem sessão HTTP)
* **CORS** liberado (`*`); restrinja em produção
* **Token único**: apenas o último token emitido vale
* Senhas com **BCrypt**
* Permissões:

    * Público: `POST /auth/login`, `POST /auth/criar-usuario`, `GET /topicos/**`
    * Protegido: demais rotas
    * Method security: `@PreAuthorize("hasRole('ADMIN')")` em `/topicos/todos`

---

## Testes rápidos via cURL

**Login**

```bash
TOKEN=$(curl -s -X POST http://localhost:8080/auth/login \
 -H "Content-Type: application/json" \
 -d '{"login":"admin@forum.com","senha":"123456"}' | jq -r .token)
```

**Criar USER sem JWT**

```bash
curl -X POST http://localhost:8080/auth/criar-usuario \
 -H "Content-Type: application/json" \
 -d '{"login":"user1@forum.com","senha":"123456","role":"USER"}'
```

**Criar ADMIN com JWT de ADMIN**

```bash
curl -X POST http://localhost:8080/auth/criar-usuario \
 -H "Authorization: Bearer '"$TOKEN"'" \
 -H "Content-Type: application/json" \
 -d '{"login":"admin2@forum.com","senha":"123456","role":"ADMIN"}'
```

**Tentar apagar todos como USER → 403**

```bash
curl -X DELETE http://localhost:8080/topicos/todos \
 -H "Authorization: Bearer <USER_TOKEN>"
```

---

## Convenções de commit

* Sugestão: Conventional Commits (`feat:`, `fix:`, `docs:`, `chore:` …)

---

## Licença

Uso acadêmico/demonstrativo. Adapte conforme sua necessidade.

---
