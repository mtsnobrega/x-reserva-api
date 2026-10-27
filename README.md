# ⚡ API - Reserva de Vagas

API REST responsável pelo gerenciamento de reservas de vagas de estacionamento. O projeto faz parte de uma arquitetura composta por serviços independentes, na qual cada API possui uma responsabilidade específica dentro do sistema.

A **API de Reservas de Vagas** é responsável por criar, consultar, atualizar, cancelar e excluir reservas, além de controlar operações específicas do ciclo de uma reserva, como **check-in** e **check-out**. Para validar a disponibilidade das vagas e manter o estado delas sincronizado, ela se comunica com a **API de Vagas** por HTTP.

A aplicação foi desenvolvida utilizando **Java com Spring Boot**, seguindo uma arquitetura em camadas para separar responsabilidades e facilitar a manutenção, evolução e organização do sistema.

---

## 🎯 Objetivos

O objetivo desta API é fornecer os recursos necessários para controlar o ciclo de vida de uma reserva de estacionamento. Uma reserva relaciona:

- um usuário;
- um veículo (placa);
- uma vaga;
- um período de utilização;
- um status;
- os horários reais de entrada e saída;
- o valor da reserva;
- o motivo e a data de cancelamento, quando existirem;
- a data de criação.

A API também possui regras para impedir situações inválidas, como:

- duas reservas para a mesma vaga no mesmo período;
- reserva de uma vaga que não esteja `DISPONIVEL` na API de Vagas;
- check-in em uma vaga que já esteja ocupada.

---

## 🏛️ Arquitetura

O projeto utiliza uma arquitetura em camadas, cada uma possuindo uma responsabilidade específica.

```
Cliente
   │
   ▼
Controller
   │
   ▼
Service ──────► VagaClient ──────► API de Vagas (HTTP)
   │
   ▼
Repository
   │
   ▼
H2 Database
```

### 🎛️ Controller

O Controller representa a porta de entrada da API. Ele recebe as requisições HTTP, obtém os dados enviados pelo cliente, chama o Service e devolve a resposta HTTP. Exemplos:

```
POST   /reservas/nova-reserva
GET    /reservas/listar-reservas
PUT    /reservas/atualizar-reservas/{id}
DELETE /reservas/deletar-reservas/{id}
PATCH  /reservas/{id}/checkin
PATCH  /reservas/{id}/checkout
PATCH  /reservas/{id}/cancelar
```

### 🧠 Service

O Service concentra as regras de negócio da aplicação. É nessa camada que são realizadas validações como:

- verificar se as datas da reserva são válidas;
- impedir uma reserva com data inicial no passado;
- impedir uma reserva com data inicial igual ou posterior à data final;
- verificar conflito de horário;
- verificar se a vaga existe e está disponível (consulta à API de Vagas);
- impedir alterações em reservas finalizadas e canceladas;
- controlar o check-in e check-out (e o estado da vaga correspondente);
- permitir o cancelamento apenas de reservas pendentes;
- controlar quando uma reserva pode ser excluída;
- calcular o valor total da reserva.

### 📦 Repository

O Repository é responsável pela comunicação com o banco de dados. O projeto utiliza:

```
JpaRepository<Reserva, Long>
```

Com isso, operações básicas como inserir, consultar, atualizar e excluir registros são fornecidas pelo Spring Data JPA. Além disso também existem consultas específicas para:

```
Buscar reservas por status
Buscar reservas por usuário
Buscar reservas por vaga
Verificar conflitos de horário
```

### 🔌 Client

O `VagaClient` é a classe responsável por conversar com a API de Vagas usando o `RestClient` do Spring. Ele isola os detalhes da comunicação HTTP do restante da aplicação e traduz os erros recebidos (vaga não encontrada, vaga indisponível, serviço fora do ar) em exceções que o Service entende.

```
buscarVaga(id)   →  GET   /vagas/listar-vagas/{id}
ocupar(id)       →  PATCH /vagas/{id}/ocupar
liberar(id)      →  PATCH /vagas/{id}/liberar
```

### 👤 Model

A classe `Reserva` representa a entidade persistida no banco de dados. Ela é mapeada para a tabela:

```
reservas
```

Principais atributos:

```
id
usuarioId
placaVeiculo
vagaId
nomeVaga
dataInicio
dataFim
status
checkinAt
checkoutAt
precoVaga
precoReserva
motivoCancelamento
canceladaAt
createdAt
```

---

## 📁 Estrutura do projeto

A estrutura atual segue a separação por responsabilidades:

```
src/
└── main/
    ├── java/
    │   └── com/
    │       └── riobranco/
    │           └── x/
    │               └── reserva_api/
    │                   ├── client/
    │                   │   ├── VagaClient.java
    │                   │   └── VagaResponse.java
    │                   │
    │                   ├── config/
    │                   │   └── RestClientConfig.java
    │                   │
    │                   ├── controller/
    │                   │   └── ReservaController.java
    │                   │
    │                   ├── model/
    │                   │   └── Reserva.java
    │                   │
    │                   ├── repository/
    │                   │   └── ReservaRepository.java
    │                   │
    │                   └── service/
    │                       └── ReservaService.java
    │
    └── resources/
        └── application.properties
```

Essa organização permite que cada parte da aplicação tenha uma responsabilidade bem definida.

---

## ⚙️ Tecnologias utilizadas

- Java 17+
- Spring Boot
- Spring Web (REST e RestClient)
- Spring Data JPA
- Hibernate
- H2 Database
- Maven
- REST API
- JSON
- Postman para testes

---

## 🔧 Configuração

Arquivo `application.properties`:

```properties
# Endereço da API de Vagas
vaga-api.url=http://localhost:8081
```

Em ambiente publicado (Azure App Service, por exemplo), o endereço pode ser definido pela variável de ambiente `VAGA_API_URL`, que sobrescreve o valor do arquivo.

O `RestClient` é configurado com tempo limite de **3 segundos para conexão** e **10 segundos para leitura**, evitando que uma API de Vagas travada deixe as requisições penduradas.

---

## 🗄️ Banco de dados

Durante o desenvolvimento, a aplicação utiliza o **H2 Database**. O H2 é utilizado como banco de dados para facilitar o desenvolvimento e os testes da API. A entidade `Reserva` é persistida na tabela `reservas`, o acesso ao banco é realizado através do `Spring Data JPA` e do `ReservaRepository`.

> O H2 está configurado em memória: os dados são perdidos sempre que a aplicação é reiniciada.

---

## 📁 Modelo de Reserva

A entidade possui a seguinte estrutura conceitual:

```
Reserva
│
├── id
├── usuarioId
├── placaVeiculo
├── vagaId
├── nomeVaga
├── dataInicio
├── dataFim
├── precoVaga
├── precoReserva
├── status
├── checkinAt
├── checkoutAt
├── motivoCancelamento
├── canceladaAt
└── createdAt
```

### `id`

Identificador único da reserva. É gerado automaticamente pelo banco de dados.

### `usuarioId`

Identifica o usuário responsável pela reserva. Neste momento, o usuário é representado pelo seu ID porque o gerenciamento de usuários pertence a outra API.

### `placaVeiculo`

Placa do veículo que utilizará a vaga.

### `vagaId`

Identifica a vaga que será utilizada. A administração das vagas pertence à **API de Vagas**, que é consultada a cada nova reserva.

### `nomeVaga`

Nome da vaga (por exemplo `A10`). **Não é enviado pelo cliente**: o valor é obtido da API de Vagas no momento da criação da reserva.

### `dataInicio` e `dataFim`

Representam o período reservado. A API verifica se `dataInicio < dataFim` e se `dataInicio` não está no passado. Uma reserva com período inválido é rejeitada.

### `precoVaga`

Indica o valor cobrado por hora da vaga. **Não é enviado pelo cliente**: é uma cópia do `precoHora` da vaga no momento da reserva, de modo que alterações futuras de preço na API de Vagas não afetam reservas já feitas.

### `precoReserva`

Indica o valor total cobrado pela reserva. É sempre calculado pelo backend com base no `precoVaga` e na duração do período.
Cada hora iniciada é cobrada integralmente. Por isso, se a reserva durar 1 hora e 30 minutos, são cobradas 2 horas.

### `status`

Representa o estado atual da reserva. Estados utilizados no fluxo atual:

```
PENDENTE
EM_USO
FINALIZADA
CANCELADA
```

O fluxo principal é:

```
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

Uma reserva `PENDENTE` também pode ser cancelada:

```
PENDENTE
    │
    │ cancelar
    ▼
CANCELADA
```

### `checkinAt` e `checkoutAt`

Horários reais de entrada e saída. São gerados pelo servidor no momento do check-in e do check-out.

### `motivoCancelamento` e `canceladaAt`

Preenchidos apenas quando a reserva é cancelada: o motivo informado e o horário do cancelamento.

---

## 🔗 Integração com a API de Vagas

A API de Reservas consulta e altera o estado das vagas nos seguintes momentos:

| Operação | Chamada à API de Vagas | Efeito |
| -------- | ---------------------- | ------ |
| Criar reserva | `GET /vagas/listar-vagas/{id}` | Verifica se a vaga existe e está `DISPONIVEL`; copia `nomeVaga` e `precoHora` |
| Atualizar reserva (trocando a vaga) | `GET /vagas/listar-vagas/{id}` | Mesma verificação para a nova vaga. Não é permitido trocar a vaga de uma reserva `EM_USO` |
| Check-in | `PATCH /vagas/{id}/ocupar` | A vaga passa para `OCUPADA` |
| Check-out | `PATCH /vagas/{id}/liberar` | A vaga volta para `DISPONIVEL` |
| Cancelar | nenhuma | Só reservas `PENDENTE` podem ser canceladas, e a vaga ainda está disponível nesse momento |

Nas operações de check-in e check-out, a API de Vagas é chamada **antes** de gravar a mudança na reserva. Se ela recusar ou estiver fora do ar, nada é alterado na reserva.

---

## 📌 Endpoints

| Método | Endpoint                            | Função             |
| ------ | ----------------------------------- | ------------------ |
| POST   | `/reservas/nova-reserva`            | Criar reserva      |
| GET    | `/reservas/listar-reservas`         | Listar reservas    |
| GET    | `/reservas/listar-reservas/{id}`    | Buscar reserva     |
| PUT    | `/reservas/atualizar-reservas/{id}` | Atualizar reserva  |
| DELETE | `/reservas/deletar-reservas/{id}`   | Excluir reserva    |
| PATCH  | `/reservas/{id}/checkin`            | Realizar check-in  |
| PATCH  | `/reservas/{id}/checkout`           | Realizar check-out |
| PATCH  | `/reservas/{id}/cancelar`           | Cancelar reserva   |
| GET    | `/reservas/status/{status}`         | Buscar por status  |
| GET    | `/reservas/usuario/{usuarioId}`     | Buscar por usuário |
| GET    | `/reservas/vaga/{vagaId}`           | Buscar por vaga    |

---

## 🏦 Arquitetura do sistema

A API de Reservas não será responsável por todo o sistema. A arquitetura possui serviços independentes:

```
            ┌──────────────────┐
            │    Aplicação     │
            │  Web / Mobile    │
            └────────┬─────────┘
                     │
      ┌──────────────┼──────────────┐
      │              │              │
      ▼              ▼              ▼
┌───────────┐  ┌─────────────┐  ┌───────────┐
│ User API  │  │ Reserva API │  │ Vaga API  │
│ (futura)  │  │             │  │           │
│           │  │ Reservas    │  │ Cadastro  │
│ Usuários  │  │ Check-in    │  │ Estado    │
│ Login     │  │ Check-out   │  │ Resumo    │
│ Roles     │  │ Cancelamento│  │           │
└───────────┘  └──────┬──────┘  └─────▲─────┘
                      │               │
                      └───────────────┘
                     HTTP (RestClient)
```

Cada API possui uma responsabilidade específica e o seu próprio banco de dados.

- A **User API** será responsável por usuários, autenticação e identidade.
- A **Reserva API** é responsável pelo ciclo de vida das reservas.
- A **Vaga API** é responsável pelo gerenciamento e disponibilidade das vagas.

---

## ⭐ Princípios adotados

O projeto busca aplicar alguns princípios importantes de desenvolvimento de software:

**Separação de responsabilidades:** cada camada possui uma função específica.

**Baixo acoplamento:** Controller, Service e Repository possuem responsabilidades separadas, e a comunicação com outros serviços fica isolada no `VagaClient`.

**Reutilização:** o Spring Data JPA fornece operações comuns de persistência, evitando a implementação manual de consultas básicas.

**Regra de negócio centralizada:** as decisões relacionadas às reservas ficam no Service.

**Arquitetura orientada a serviços:** usuários, reservas e vagas são organizados em APIs independentes, cada uma com seu banco.

**Fonte única da verdade:** dados que pertencem a outro serviço (nome e preço da vaga) são obtidos dele, e não informados pelo cliente.

**Evolução incremental:** funcionalidades mais complexas, como segurança, serão adicionadas gradualmente.

---

## 🚀 Executando o projeto

Pré-requisitos: JDK 17+ e Maven (ou o wrapper `mvnw` do projeto).

```
./mvnw spring-boot:run          (Linux / macOS)
mvnw.cmd spring-boot:run        (Windows)
```

A API sobe em `http://localhost:8080`. Para testar as operações que dependem de vagas, a **API de Vagas** precisa estar rodando em `http://localhost:8081`.

---

## 🧪 Testando com Postman

A API pode ser testada utilizando o Postman. Com a aplicação executando localmente: `http://localhost:8080`

> Antes de criar uma reserva, cadastre uma vaga na API de Vagas (`POST http://localhost:8081/vagas/nova-vaga`). Nos exemplos abaixo, considere uma vaga `A10`, com `id` 1 e `precoHora` de 10.00.

### POST - Criar uma reserva

O endpoint permite que um usuário crie uma reserva. Os campos `nomeVaga` e `precoVaga` **não devem ser enviados**: são obtidos da API de Vagas.

```
http://localhost:8080/reservas/nova-reserva
```

Body:

```
{
    "usuarioId": 1,
    "placaVeiculo": "ABC-1234",
    "vagaId": 1,
    "dataInicio": "2026-10-28T15:00:00",
    "dataFim": "2026-10-28T17:30:00"
}
```

Exemplo de resposta retornado pela API (2h30 de duração = 3 horas cobradas):

```
{
    "canceladaAt": null,
    "checkinAt": null,
    "checkoutAt": null,
    "createdAt": "2026-10-27T16:49:12.5719669",
    "dataFim": "2026-10-28T17:30:00",
    "dataInicio": "2026-10-28T15:00:00",
    "id": 1,
    "motivoCancelamento": null,
    "nomeVaga": "A10",
    "placaVeiculo": "ABC-1234",
    "precoReserva": 30.00,
    "precoVaga": 10.00,
    "status": "PENDENTE",
    "usuarioId": 1,
    "vagaId": 1
}
```

Se a vaga não estiver disponível, a API responde com **400** e a mensagem:

```
A vaga A10 não está disponível para reserva (status: OCUPADA).
```

### GET - Buscar reservas por status

É um endpoint específico do administrador, lista todas as reservas com base no status.

Permite listar quais reservas estão pendentes para fazer o check-in

```
http://localhost:8080/reservas/status/PENDENTE
```

Permite listar quais reservas estão em uso e pendentes de check-out

```
http://localhost:8080/reservas/status/EM_USO
```

Permite listar quais reservas foram finalizadas

```
http://localhost:8080/reservas/status/FINALIZADA
```

Permite listar quais reservas foram canceladas

```
http://localhost:8080/reservas/status/CANCELADA
```

### PATCH - Check-in

O endpoint é utilizado pelo administrador para realizar o check-in
de uma reserva que esteja com status `PENDENTE`. Ao realizar o check-in, a API de Vagas é chamada para marcar a vaga como `OCUPADA`.

Para identificar a reserva, o administrador primeiro consulta as
reservas pendentes através do endpoint de **GET - Buscar reservas por status**.

Após identificar a reserva desejada, o `id` da reserva é utilizado
na URL do endpoint de check-in.

```
PATCH http://localhost:8080/reservas/1/checkin
```

Body:

```
Não precisa enviar JSON. O Body fica vazio e o horário do checkinAt será gerado pelo servidor
```

Exemplo de resposta retornado pela API:

```
{
    "canceladaAt": null,
    "checkinAt": "2026-10-28T15:02:41.1084626",
    "checkoutAt": null,
    "createdAt": "2026-10-27T16:49:12.571967",
    "dataFim": "2026-10-28T17:30:00",
    "dataInicio": "2026-10-28T15:00:00",
    "id": 1,
    "motivoCancelamento": null,
    "nomeVaga": "A10",
    "placaVeiculo": "ABC-1234",
    "precoReserva": 30.00,
    "precoVaga": 10.00,
    "status": "EM_USO",
    "usuarioId": 1,
    "vagaId": 1
}
```

Se a vaga já estiver ocupada (ou foi desativada), o check-in é recusado com **400** e a mensagem:

```
A vaga não está disponível: ela já está ocupada ou foi desativada.
```

### PATCH - Check-out

O endpoint é utilizado pelo administrador para realizar o check-out
de uma reserva que esteja com status `EM_USO`. Ao realizar o check-out, a API de Vagas é chamada para devolver a vaga ao estado `DISPONIVEL`.
Segue a mesma regra do id aplicada sobre o check-in

```
PATCH http://localhost:8080/reservas/1/checkout
```

Body:

```
Não precisa enviar JSON. O Body fica vazio e o horário do checkoutAt será gerado pelo servidor
```

Exemplo de resposta retornado pela API:

```
{
    "canceladaAt": null,
    "checkinAt": "2026-10-28T15:02:41.1084626",
    "checkoutAt": "2026-10-28T17:25:10.334125",
    "createdAt": "2026-10-27T16:49:12.571967",
    "dataFim": "2026-10-28T17:30:00",
    "dataInicio": "2026-10-28T15:00:00",
    "id": 1,
    "motivoCancelamento": null,
    "nomeVaga": "A10",
    "placaVeiculo": "ABC-1234",
    "precoReserva": 30.00,
    "precoVaga": 10.00,
    "status": "FINALIZADA",
    "usuarioId": 1,
    "vagaId": 1
}
```

### PATCH - Cancelar uma reserva

Permite cancelar uma reserva, informando o motivo. Só é possível cancelar reservas com status `PENDENTE`. Como a vaga só é ocupada no check-in, o cancelamento não altera o estado da vaga.

```
PATCH http://localhost:8080/reservas/1/cancelar
```

Body:

```
{
    "motivo": "Mudança de planos"
}
```

Exemplo de resposta retornado pela API:

```
{
    "canceladaAt": "2026-10-27T17:10:33.4120031",
    "checkinAt": null,
    "checkoutAt": null,
    "createdAt": "2026-10-27T16:49:12.571967",
    "dataFim": "2026-10-28T17:30:00",
    "dataInicio": "2026-10-28T15:00:00",
    "id": 1,
    "motivoCancelamento": "Mudança de planos",
    "nomeVaga": "A10",
    "placaVeiculo": "ABC-1234",
    "precoReserva": 30.00,
    "precoVaga": 10.00,
    "status": "CANCELADA",
    "usuarioId": 1,
    "vagaId": 1
}
```

Para uma reserva em qualquer outro status, a API responde com **409** e a mensagem:

```
Só é possível cancelar reservas pendentes. Status atual: EM_USO
```

### GET - Buscar uma reserva pelo ID

É um endpoint específico do administrador, permite listar uma reserva por id, para ver detalhes isolados.

```
http://localhost:8080/reservas/listar-reservas/1
```

### GET - Listar todas as reservas

É um endpoint específico do administrador, lista todas as reservas já cadastradas no sistema, como forma de histórico.

```
http://localhost:8080/reservas/listar-reservas
```

### PUT - Atualizar uma reserva

Permite alterar a vaga e o período de uma reserva. Não é possível alterar reservas `FINALIZADA` ou `CANCELADA`.

Se o `vagaId` for alterado, a nova vaga é consultada na API de Vagas (precisa estar `DISPONIVEL`) e `nomeVaga` e `precoVaga` são atualizados. Não é permitido trocar a vaga de uma reserva `EM_USO`. O `precoReserva` é recalculado com base nos novos dados.

```
http://localhost:8080/reservas/atualizar-reservas/1
```

Body:

```
{
    "vagaId": 2,
    "dataInicio": "2026-10-29T19:00:00",
    "dataFim": "2026-10-29T22:00:00"
}
```

### DELETE

A exclusão depende do status da reserva. Nesse caso:

- PENDENTE ✖️ não pode deletar
- EM_USO ✖️ não pode deletar
- FINALIZADA ☑️ pode deletar
- CANCELADA ☑️ pode deletar

```
http://localhost:8080/reservas/deletar-reservas/1
```

### GET - Buscar reservas por usuário

```
http://localhost:8080/reservas/usuario/1
```

### GET - Buscar reservas por vaga

```
http://localhost:8080/reservas/vaga/1
```

---

## 🚦 Respostas de erro

| Código | Quando acontece |
| ------ | --------------- |
| 400 Bad Request | Dados inválidos ou regra de negócio violada: campo obrigatório ausente, datas inválidas, conflito de horário, vaga indisponível, `vagaId` inexistente, tentativa de excluir reserva pendente/em uso |
| 404 Not Found | Reserva não encontrada |
| 409 Conflict | Cancelamento de uma reserva que não está `PENDENTE` |
| 503 Service Unavailable | API de Vagas fora do ar ou sem resposta dentro do tempo limite |

---

## 🧭 Fluxo completo de teste

Com a API de Vagas (8081) e a API de Reservas (8080) rodando:

1. **Vagas:** `POST /vagas/nova-vaga` para criar a vaga `A10`.
2. **Reservas:** `POST /reservas/nova-reserva` sem `nomeVaga` e `precoVaga`. A resposta traz o nome e o preço vindos da API de Vagas.
3. **Reservas:** `PATCH /reservas/1/checkin`.
4. **Vagas:** `GET /vagas/listar-vagas/1`. O status da vaga deve ser `OCUPADA`.
5. **Reservas:** nova reserva para a mesma vaga. Deve ser recusada (400).
6. **Reservas:** `PATCH /reservas/1/checkout`.
7. **Vagas:** `GET /vagas/listar-vagas/1`. O status da vaga volta para `DISPONIVEL`.
8. Com a API de Vagas parada, tente criar uma reserva. Deve retornar **503**.

---

## 🔮 Próximos passos e limitações conhecidas

- **Sem transação entre serviços:** se a API de Vagas confirmar uma mudança e a gravação da reserva falhar em seguida, os dois serviços podem ficar inconsistentes. Retentativas e padrões como saga são evoluções futuras.
- **Autenticação e autorização:** ainda não implementadas; dependem da futura User API.
- **Banco de dados:** o H2 em memória deve ser substituído por um banco persistente (por exemplo, PostgreSQL).