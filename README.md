# 🏋️ Gym Management API

API REST desenvolvida para gerenciar uma academia: cadastro de alunos, cadastro de treinos e controle de matrículas. Permite matricular alunos em treinos, cancelar matrículas e consultar o histórico.

Este é o quarto projeto do meu portfólio de estudos em Java e Spring Boot. Diferente dos anteriores, o foco aqui não foi aprender conceitos novos, e sim ganhar velocidade e autonomia aplicando a mesma arquitetura (Controller → Service → Repository → DTO) de forma independente, em um contexto diferente.

💡 Problema que resolve

Academias que controlam matrículas em treinos de forma manual enfrentam problemas como:

- Permitir que um aluno se matricule duas vezes no mesmo treino
- Perder o controle de quem está matriculado em qual treino
- Falta de um cadastro centralizado de alunos e treinos

A Gym Management API resolve isso centralizando o cadastro de alunos, treinos e o controle de matrículas, com regras de negócio que impedem operações inválidas.

🛠️ Tecnologias utilizadas

- Java 21
- Spring Boot
- Spring Web (REST API)
- Spring Data JPA
- Spring Security
- JWT (JJWT)
- PostgreSQL
- Bean Validation (validação de dados de entrada)
- Maven
- Docker (Render)
- Frontend estático (Cloudflare Pages)

🏗️ Arquitetura

Controller → Service → Repository → Banco de Dados

Controller: recebe requisições HTTP e devolve respostas
Service: contém as regras de negócio, incluindo validação de duplicidade
Repository: comunicação com o banco de dados, incluindo consultas derivadas (existsBy)
DTOs: protegem a API, controlando exatamente o que entra e sai — inclusive em relacionamentos, através de DTOs aninhados
Exceptions: exceções customizadas capturadas globalmente, garantindo respostas de erro consistentes

📦 Modelo de dados

Member
Campo	Tipo
id	Long
name	String
email	String
phone	String

Workout
Campo	Tipo
id	Long
name	String
description	String
duration	Integer (minutos)

Enrollment
Campo	Tipo
id	Long
enrollmentDate	LocalDateTime
member	Member (relacionamento @ManyToOne)
workout	Workout (relacionamento @ManyToOne)

🧠 Relacionamentos

Um Member pode se matricular em vários Workout, através de várias Enrollment
Um Workout pode ter vários Member matriculados, através de várias Enrollment
Enrollment é a entidade intermediária que resolve esse relacionamento N:N, guardando a data da matrícula

📌 Endpoints disponíveis

Health
Ação	Método	Rota
Verificar disponibilidade	GET	/health ou /api/health

Auth
Ação	Método	Rota
Registrar usuário	POST	/auth/register
Login	POST	/auth/login

Members
Ação	Método	Rota
Cadastrar aluno	POST	/members
Listar alunos	GET	/members
Buscar aluno por ID	GET	/members/{id}
Atualizar aluno	PUT	/members/{id}
Deletar aluno	DELETE	/members/{id}

Workouts
Ação	Método	Rota
Cadastrar treino	POST	/workouts
Listar treinos	GET	/workouts
Buscar treino por ID	GET	/workouts/{id}
Atualizar treino	PUT	/workouts/{id}
Deletar treino	DELETE	/workouts/{id}

Enrollments
Ação	Método	Rota
Matricular aluno em treino	POST	/enrollments
Listar matrículas	GET	/enrollments
Cancelar matrícula	DELETE	/enrollments/{id}

Permissões

- `/health` e `/api/health` são públicos e retornam `200 OK`
- `POST /auth/register` cria usuários com papel `USER`
- `GET` em members, workouts e enrollments exige token JWT de `ADMIN` ou `USER`
- `POST`, `PUT` e `DELETE` exigem token JWT de `ADMIN`

Exemplo — Matricular aluno em treino

POST /enrollments

```json
{
  "memberId": 1,
  "workoutId": 1
}
```

Resposta (201 Created)

```json
{
  "id": 1,
  "enrollmentDate": "2026-07-22T17:58:12.345678",
  "member": {
    "id": 1,
    "name": "Lucas Ferreira",
    "email": "lucas.ferreira@email.com",
    "phone": "11999998888"
  },
  "workout": {
    "id": 1,
    "name": "Treino de Força - Full Body",
    "description": "Treino focado em força com pesos livres",
    "duration": 60
  }
}
```

🔥 Regras de negócio

Regra	Comportamento
Aluno já matriculado no mesmo treino não pode se matricular de novo	400 Bad Request
Aluno inexistente	404 Not Found
Treino inexistente	404 Not Found

⚠️ Tratamento de erros

A API utiliza exceções customizadas, capturadas globalmente através de @RestControllerAdvice:

ResourceNotFoundException → devolve 404 Not Found (recurso não encontrado pelo ID)
BusinessRuleException → devolve 400 Bad Request (regra de negócio violada, como matrícula duplicada)
InvalidCredentialsException → devolve 401 Unauthorized (login com credenciais inválidas)

✅ Validações

Os DTOs de entrada possuem validação via Bean Validation:

name, email → @NotBlank
memberId, workoutId → @NotNull

Requisições que violam essas regras retornam 400 Bad Request automaticamente.

🖥️ Frontend estático

O frontend em HTML, CSS e JavaScript puro fica em `src/main/resources/static` (`index.html`, `css/styles.css`, `js/app.js`) e é servido automaticamente pelo Spring Boot em `http://localhost:8080`.

- **Local:** a URL da API usa `window.location.origin` quando o host é `localhost` ou `127.0.0.1`.
- **Produção (Cloudflare Pages):** edite a constante `RENDER_API_URL` em `js/app.js` com a URL do serviço no Render (sem barra no final, ex.: `https://gym-management-api.onrender.com`).
- No Cloudflare Pages, publique a pasta `src/main/resources/static` como saída (não há etapa de build).
- Adicione o domínio do Pages em `CORS_ALLOWED_ORIGINS` no Render (ex.: `https://gym.pages.dev`).
- Contas criadas pelo formulário recebem o papel `USER` (somente leitura). Para acesso administrativo, configure `ADMIN_EMAIL` e `ADMIN_PASSWORD` no Render; o usuário `ADMIN` é criado no primeiro startup.

🐳 Docker e deploy (Render)

O `Dockerfile` na raiz do projeto é um build multi-stage:
- Stage 1: `maven:3.9.9-eclipse-temurin-21-alpine` compila o projeto com `./mvnw clean package -DskipTests -B`
- Stage 2: `eclipse-temurin:21-jre-alpine` executa o JAR com usuário não-root (`appuser`) e heap limitado a 384 MB

Construir e executar localmente:

```bash
docker build -t gym-management-api .
docker run -p 8080:8080 --env-file .env gym-management-api
```

No Render (serviço Docker):
- Caminho do Dockerfile: raiz do repositório
- Build command: `docker build -t gym-management-api .`
- Porta: `8080`
- Health check: `GET /health`
- Variáveis de ambiente: `DB_URL`, `DB_USER`, `DB_PASSWORD`, `JWT_SECRET`, `ADMIN_EMAIL`, `ADMIN_PASSWORD`, `CORS_ALLOWED_ORIGINS`, `PORT`

O frontend estático em `src/main/resources/static` é servido pelo Spring Boot e publicado separadamente no Cloudflare Pages.

⚙️ Como rodar o projeto localmente

Pré-requisitos
- Java 21 instalado
- PostgreSQL rodando localmente
- Maven (ou usar o mvnw incluso no projeto)

Passos
Clone o repositório

```bash
git clone <url-do-repositorio>
```

Copie o arquivo de exemplo e preencha as credenciais locais (o `.env` é ignorado pelo git e carregado automaticamente):

```bash
cp .env.example .env
```

```bash
DB_URL=jdbc:postgresql://localhost:5432/gym_management
DB_USER=postgres
DB_PASSWORD=SUA_SENHA_LOCAL
JWT_SECRET=SEGREDO_BASE64_COM_PELO_MENOS_32_BYTES
ADMIN_EMAIL=admin@gym.com
ADMIN_PASSWORD=SENHA_DO_ADMIN
CORS_ALLOWED_ORIGINS=http://localhost:5173
```

Para produção no Render com PostgreSQL do Aiven, configure no painel do Render:

- `DB_URL`: URL JDBC do Aiven, normalmente no formato `jdbc:postgresql://HOST:PORT/DB?sslmode=require`
- `DB_USER` e `DB_PASSWORD`: credenciais do banco
- `JWT_SECRET`: chave Base64 com pelo menos 32 bytes
- `ADMIN_EMAIL` e `ADMIN_PASSWORD`: usuário admin inicial
- `CORS_ALLOWED_ORIGINS`: origens do frontend separadas por vírgula (ex.: `https://seu-frontend.onrender.com`)
- `PORT`: o Render define automaticamente; a aplicação usa `${PORT:8080}`

O `JWT_SECRET` deve ser uma chave Base64 com pelo menos 32 caracteres e decodificar para pelo menos 32 bytes. O usuário definido por `ADMIN_EMAIL`/`ADMIN_PASSWORD` é criado automaticamente na primeira execução com papel `ADMIN`.

Execute a aplicação:

```bash
./mvnw spring-boot:run
```

A API estará disponível em http://localhost:8080

Testando

Recomenda-se o uso do Postman ou Insomnia para testar os endpoints.

🧪 Testes automatizados

```bash
./mvnw test
```

A suíte atual cobre:
- Contexto da aplicação (`GymManagementApiApplicationTests`)
- Validação do `JwtService` (`Security/JwtServiceTest`)
- Integração do frontend estático com a segurança da API (`StaticFrontendIntegrationTest`)

Todos os testes utilizam banco H2 em memória, garantindo que nada tente acessar o banco de produção.🗺️ Próximos passos (roadmap)

 - Horários e disponibilidade de treinos
 - Histórico de frequência (check-in) do aluno

📝 O que aprendi construindo esse projeto

Esse projeto foi um exercício de repetição deliberada: aplicar, sem apoio, a mesma arquitetura já usada nos três projetos anteriores, em um domínio novo. Isso trouxe ganhos de:

- Velocidade para estruturar Entity, Repository, DTOs, Service e Controller de forma independente
- Reconhecimento mais rápido de erros mecânicos (rota mal configurada, tipo incompatível, anotação esquecida) sem precisar reaprender o conceito por trás
- Confiança para replicar o padrão de entidade intermediária (Enrollment) para resolver um relacionamento N:N, já na segunda vez sem tanta orientação
- Reforço do tratamento de exceções customizadas (ResourceNotFoundException, BusinessRuleException) e do critério para decidir qual usar em cada situação

Desenvolvido por Petterson Oliveira como parte do meu portfólio de estudos em Java e Spring Boot.

