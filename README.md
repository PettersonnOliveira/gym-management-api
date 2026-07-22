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
- MySQL
- Bean Validation (validação de dados de entrada)
- Maven

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

✅ Validações

Os DTOs de entrada possuem validação via Bean Validation:

name, email → @NotBlank
memberId, workoutId → @NotNull

Requisições que violam essas regras retornam 400 Bad Request automaticamente.

⚙️ Como rodar o projeto localmente

Pré-requisitos
- Java 21 instalado
- MySQL rodando localmente
- Maven (ou usar o mvnw incluso no projeto)

Passos
Clone o repositório

```bash
git clone <url-do-repositorio>
```

Configure o banco de dados no arquivo `src/main/resources/application.properties`:

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/gym_management?createDatabaseIfNotExist=true
spring.datasource.username=SEU_USUARIO
spring.datasource.password=SUA_SENHA
spring.jpa.hibernate.ddl-auto=update
```

⚠️ Este arquivo contém credenciais sensíveis e não deveria ser versionado com dados reais em um projeto de produção.

Execute a aplicação:

```bash
./mvnw spring-boot:run
```

A API estará disponível em http://localhost:8080

Testando

Recomenda-se o uso do Postman ou Insomnia para testar os endpoints.

🗺️ Próximos passos (roadmap)

 - Autenticação e autorização com Spring Security + JWT
 - Horários e disponibilidade de treinos
 - Histórico de frequência (check-in) do aluno

📝 O que aprendi construindo esse projeto

Esse projeto foi um exercício de repetição deliberada: aplicar, sem apoio, a mesma arquitetura já usada nos três projetos anteriores, em um domínio novo. Isso trouxe ganhos de:

- Velocidade para estruturar Entity, Repository, DTOs, Service e Controller de forma independente
- Reconhecimento mais rápido de erros mecânicos (rota mal configurada, tipo incompatível, anotação esquecida) sem precisar reaprender o conceito por trás
- Confiança para replicar o padrão de entidade intermediária (Enrollment) para resolver um relacionamento N:N, já na segunda vez sem tanta orientação
- Reforço do tratamento de exceções customizadas (ResourceNotFoundException, BusinessRuleException) e do critério para decidir qual usar em cada situação

Desenvolvido por Petterson Oliveira como parte do meu portfólio de estudos em Java e Spring Boot.

