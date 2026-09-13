# ⚡ API - Reserva de Vagas 

API REST responsável pelo gerenciamento de reservas de vagas de estacionamento. O projeto faz parte de uma arquitetura composta por serviços independentes, na qual cada API possui uma responsabilidade específica dentro do sistema.

A **API de Reservas de Vagas** é responsável por criar, consultar, atualizar e excluir reservas, além de controlar operações específicas do ciclo de uma reserva, como **check-in** e **check-out**.

A aplicação foi desenvolvida utilizando **Java com Spring Boot**, seguindo uma arquitetura em camadas para separar responsabilidades e facilitar a manutenção, evolução e organização do sistema.

---

## 🎯 Objetivos

O objetivo desta API é fornecer os recursos necessários para controlar o ciclo de vida de uma reserva de estacionamento. Uma reserva relaciona:
* um usuário;
* uma vaga;
* um período de utilização;
* um status;
* os horários reais de entrada e saída;
* o valor da reserva;
* a data de criação.

A API também possui regras para impedir situações inválidas, como duas reservas para a mesma vaga no mesmo período.

---

## 🏛️ Arquitetura

O projeto utiliza uma arquitetura em camadas, cada uma possuindo uma responsabilidade específica.

```text
Cliente
   │
   ▼
Controller
   │
   ▼
Service
   │
   ▼
Repository
   │
   ▼
H2 Database
```

###  🎛️ Controller

O Controller representa a porta de entrada da API. Ele recebe as requisições HTTP, obtém os dados enviados pelo cliente, chama o Service e devolve a resposta HTTP. Exemplos:
```text
POST   /reservas/nova-reserva
GET    /reservas/listar-reservas
PUT    /reservas/atualizar-reservas/{id}
DELETE /reservas/deletar-reservas/{id}
PATCH  /reservas/{id}/checkin
PATCH  /reservas/{id}/checkout
```

### 🧠 Service

O Service concentra as regras de negócio da aplicação. É nessa camada que são realizadas validações como:
* verificar se as datas da reserva são válidas;
* impedir uma reserva com data inicial igual ou posterior à data final;
* verificar conflito de horário;
* impedir alterações em reservas finalizadas e canceladas;
* controlar o check-in e check-out;
* controlar quando uma reserva pode ser excluída.

### 📦 Repository

O Repository é responsável pela comunicação com o banco de dados. O projeto utiliza:

```java
JpaRepository<Reserva, Long>
```

Com isso, operações básicas como inserir, consultar, atualizar e excluir registros são fornecidas pelo Spring Data JPA. Além disso também existem consultas específicas para:

```text
Buscar reservas por status
Buscar reservas por usuário
Buscar reservas por vaga
Verificar conflitos de horário
```

### 👤  Model

A classe `Reserva` representa a entidade persistida no banco de dados. Ela é mapeada para a tabela:

```text
reservas
```

Principais atributos:

```text
id
usuarioId
vagaId
dataInicio
dataFim
status
checkinAt
checkoutAt
precoReserva
createdAt
```

---

## 📁 Estrutura do projeto

A estrutura atual segue a separação por responsabilidades:

```text
src/
└── main/
    └── java/
        └── com/
            └── riobranco/
                └── x/
                    └── reserva_api/
                        ├── controller/
                        │   └── ReservaController.java
                        │
                        ├── model/
                        │   └── Reserva.java
                        │
                        ├── repository/
                        │   └── ReservaRepository.java
                        │
                        └── service/
                            └── ReservaService.java
```

Essa organização permite que cada parte da aplicação tenha uma responsabilidade bem definida.

---

## ⚙️ Tecnologias utilizadas

* Java 17+
* Spring Boot
* Spring Web
* Spring Data JPA
* Hibernate
* H2 Database
* Maven
* REST API
* JSON
* Postman para testes

---

## Banco de dados

Durante o desenvolvimento, a aplicação utiliza o **H2 Database**. O H2 é utilizado como banco de dados para facilitar o desenvolvimento e os testes da API. A entidade `Reserva` é persistida na tabela: `reservas`, o acesso ao banco é realizado através do `Spring Data JPA` e do `ReservaRepository`

---

## 📁 Modelo de Reserva

A entidade possui a seguinte estrutura conceitual:

```text
Reserva
│
├── id
├── usuarioId
├── vagaId
├── dataInicio
├── dataFim
├── status
├── checkinAt
├── checkoutAt
├── precoReserva
└── createdAt
```

### `id`

Identificador único da reserva. É gerado automaticamente pelo banco de dados.

### `usuarioId`

Identifica o usuário responsável pela reserva. Neste momento, o usuário é representado pelo seu ID porque o gerenciamento de usuários pertence a outra API.

### `vagaId`

Identifica a vaga que será utilizada. A administração das vagas pertence à futura **API de Vagas**.

### `dataInicio` e `dataFim`

Representam o período reservado. A API verifica se: `dataInicio < dataFim`, uma reserva com período inválido é rejeitada.

### `precoReserva`
Indica o valor cobrado pela vaga, pelo tempo de uso, sendo calculada: 
```text
precoReserva = preco_vaga * (data_fim - data_fim)
```


### `status`

Representa o estado atual da reserva. Estados utilizados no fluxo atual:
```text
PENDENTE
EM_USO
FINALIZADA
CANCELADA
```

O fluxo principal é:

```text
PENDENTE
    │
    │ check-in
    ▼
EM_USO
    │
    │ check-out
    ▼
FINALIZADA
```
---

## 📌Endpoints

| Método | Endpoint                            | Função             |
| ------ | ----------------------------------- | ------------------ |
| POST   | `/reservas/nova-reserva`            | Criar reserva      |
| GET    | `/reservas/listar-reservas`         | Listar reservas    |
| GET    | `/reservas/listar-reservas/{id}`    | Buscar reserva     |
| PUT    | `/reservas/atualizar-reservas/{id}` | Atualizar reserva  |
| DELETE | `/reservas/deletar-reservas/{id}`   | Excluir reserva    |
| PATCH  | `/reservas/{id}/checkin`            | Realizar check-in  |
| PATCH  | `/reservas/{id}/checkout`           | Realizar check-out |
| GET    | `/reservas/status/{status}`         | Buscar por status  |
| GET    | `/reservas/usuario/{usuarioId}`     | Buscar por usuário |
| GET    | `/reservas/vaga/{vagaId}`           | Buscar por vaga    |

---
## 🏦 Arquitetura do sistema

A API de Reservas não será responsável por todo o sistema. A arquitetura planejada possui serviços independentes:

```text
                    ┌──────────────────┐
                    │    Aplicação     │
                    │  Web / Mobile    │
                    └────────┬─────────┘
                             │
                 ┌───────────┴───────────┐
                 │                       │
                 ▼                       ▼
        ┌─────────────────┐     ┌─────────────────┐
        │    User API     │     │  Reserva API    │
        │                 │     │                 │
        │ Usuários        │     │ Reservas        │
        │ Login           │     │ Check-in        │
        │ Autenticação    │     │ Check-out       │
        │ Roles           │     │ Regras negócio  │
        └─────────────────┘     └────────┬────────┘
                                         │
                                         ▼
                                ┌─────────────────┐
                                │     Vaga API    │
                                │                 │
                                │ Cadastro vagas  │
                                │ Disponibilidade │
                                │ Estado da vaga  │
                                └─────────────────┘
```

Cada API possui uma responsabilidade específica.
- A **User API** será responsável por usuários, autenticação e identidade.
- A **Reserva API** será responsável pelo ciclo de vida das reservas.
- A **Vaga API** será responsável pelo gerenciamento e disponibilidade das vagas.

---

## ⭐ Princípios adotados

O projeto busca aplicar alguns princípios importantes de desenvolvimento de software:

**Separação de responsabilidades:** cada camada possui uma função específica.

**Baixo acoplamento:** Controller, Service e Repository possuem responsabilidades separadas.

**Reutilização:** o Spring Data JPA fornece operações comuns de persistência, evitando a implementação manual de consultas básicas.

**Regra de negócio centralizada:** as decisões relacionadas às reservas ficam no Service.

**Arquitetura orientada a serviços:** usuários, reservas e vagas serão organizados em APIs independentes.

**Evolução incremental:** funcionalidades mais complexas, como segurança e comunicação entre APIs, serão adicionadas gradualmente.

---
## 🚀 Testando com Postman

A API pode ser testada utilizando o Postman. Com a aplicação executando localmente: `http://localhost:8080`

### POST - Criar uma reserva

```http
http://localhost:8080/reservas/nova-reserva
```

Body:

```json
{
  "usuarioId": 1,
  "vagaId": 10,
  "dataInicio": "2026-09-16T18:00:00",
  "dataFim": "2026-09-16T21:00:00",
  "precoReserva": 30.00
}
```

### GET - Listar todas as reservas

```http
http://localhost:8080/reservas/listar-reservas
```

### GET - Buscar uma reserva pelo ID

```http
http://localhost:8080/reservas/listar-reservas/1
```

### PUT - Atualizar uma reserva

```http
http://localhost:8080/reservas/atualizar-reservas/1
```

Body:

```json
{
    "vagaId": 7,
    "dataInicio": "2026-09-15T19:00:00",
    "dataFim": "2026-09-15T22:00:00"
}
```

### POST Testar conflito de horário
Consiste em testar criar uma reserva quando uma vaga já esta sendo ocupada. 
```http
http://localhost:8080/reservas/nova-reserva
```

Body Primeira Reserva:

```json
{
  "usuarioId": 1,
  "vagaId": 10,
  "dataInicio": "2026-09-16T18:00:00",
  "dataFim": "2026-09-16T21:00:00",
  "precoReserva": 30.00
}
```
Body Segunda Reserva:
```json
{
    "usuarioId": 2,
    "vagaId": 10,
    "dataInicio": "2026-09-16T19:00:00",
    "dataFim": "2026-09-16T22:00:00",
    "precoReserva": 30.00
}
```

### PATCH - Check-in
Testar após cadastrar uma reserva
```http
PATCH http://localhost:8080/reservas/1/checkin
```

Body:

```json
{
    Não precisa enviar JSON. O Body fica vazio e horário do checkinAt será gerado pelo servidor
}
```

### PATCH - Check-out 
Testar após cadastrar uma reserva
```http
http://localhost:8080/reservas/1/checkout
```

Body:

```json
{
    Não precisa enviar JSON. O Body fica vazio e horário do checkoutAt será gerado pelo servidor
}
```

### GET - Buscar reservas por usuário

```http
http://localhost:8080/reservas/usuario/1
```

### GET - Buscar reservas por status
```http
http://localhost:8080/reservas/status/PENDENTE
```

```http
http://localhost:8080/reservas/status/EM_USO
```

```http
http://localhost:8080/reservas/status/FINALIZADA
```

### DELETE
Nesse caso: 
- PENDENTE   ✖️ não pode deletar
- EM_USO     ✖️ não pode deletar
- FINALIZADA ☑️ pode deletar
- CANCELADA  ☑️ pode deletar

```http
http://localhost:8080/reservas/deletar-reservas/1
```