# Trinity

Sistema web de gestão de igrejas: cadastro de membros, avisos, eventos, escalas de louvor, patrimônio, financeiro (com contribuições por PIX), relatórios e Bíblia, tudo com controle de acesso por permissões.

Projeto de Trabalho de Conclusão de Curso de **Sistemas para Internet**, UniALFA Umuarama/PR.
Autores: **André Lucas Camargo** e **Leonardo Mirandola**.

## Funcionalidades

| Módulo | O que faz |
|---|---|
| **Painel inicial** | Totais de membros, saldo em caixa, resumo financeiro dos últimos meses, aniversariantes do mês, avisos recentes e próximos eventos |
| **Membros** | Cadastro com CPF validado, telefone e endereço (preenchido pelo CEP), busca, ativar, desativar e excluir |
| **Acessos e permissões** | Conta de acesso por membro, com permissões por módulo e senha provisória |
| **Avisos** | Publicação, ativação, exclusão e confirmação de leitura, com placar de quem leu |
| **Eventos** | Agenda da igreja e resposta de presença (vou / não vou) |
| **Escalas** | Escalas de louvor com integrantes e funções musicais, sem repetir membro nem função |
| **Patrimônio** | Itens da igreja com categoria, quantidade, estado de conservação e localização |
| **Financeiro** | Lançamentos de entrada e saída por categoria, saldo mensal e geral |
| **Contribuições** | O membro gera um PIX (QR Code e "copia e cola") e a tesouraria confirma; a confirmação vira lançamento |
| **Relatórios** | Financeiro, membros e patrimônio, com filtros e impressão ou PDF |
| **Bíblia** | Leitura por livro e capítulo e busca por palavra |

## Tecnologias

- Java 17 e Spring Boot 4 (Web MVC, Data JPA, Security, Validation)
- Thymeleaf, Bootstrap 5.3, Bootstrap Icons e Chart.js
- SQLite (arquivo local) com Hibernate
- Autenticação com JWT em cookies HttpOnly e refresh tokens (jjwt)
- Maven (o projeto inclui o Maven Wrapper, não é preciso instalar o Maven)

## Como rodar

### Requisitos

- **JDK 17** ou superior
- Navegador atualizado e internet (o front usa bibliotecas por CDN e o CEP é consultado no ViaCEP)

### 1. Baixar o projeto

```bash
git clone https://github.com/Andreluc23/trinity.git
cd trinity
```

### 2. Definir as variáveis de ambiente

| Variável | Obrigatória | Para que serve |
|---|---|---|
| `TRINITY_JWT_SEGREDO` | **Sim** | Chave que assina os tokens de login, em Base64 (mínimo de 32 bytes) |
| `TRINITY_ADMIN_EMAIL` | Recomendada | E-mail do administrador inicial |
| `TRINITY_ADMIN_SENHA` | Recomendada | Senha do administrador inicial |
| `TRINITY_DEMO` | Não | Com `true`, carrega dados fictícios de demonstração |
| `TRINITY_PIX_CHAVE` | Não | Chave PIX usada nas contribuições |
| `TRINITY_PIX_NOME` | Não | Nome do recebedor do PIX (padrão: Igreja Trinity) |
| `TRINITY_PIX_CIDADE` | Não | Cidade do recebedor do PIX (padrão: Umuarama) |

O administrador inicial só é criado se ainda não existir nenhum administrador e as duas variáveis (`TRINITY_ADMIN_EMAIL` e `TRINITY_ADMIN_SENHA`) estiverem definidas.

**Gerar o segredo do JWT**

No Windows (PowerShell):

```powershell
$b = New-Object byte[] 32; [Security.Cryptography.RandomNumberGenerator]::Create().GetBytes($b); [Convert]::ToBase64String($b)
```

No Linux ou macOS:

```bash
openssl rand -base64 32
```

### 3. Iniciar a aplicação

**Windows (PowerShell):**

```powershell
$env:TRINITY_JWT_SEGREDO = "cole-o-segredo-gerado"
$env:TRINITY_ADMIN_EMAIL = "admin@exemplo.com"
$env:TRINITY_ADMIN_SENHA = "UmaSenhaForte123"
.\mvnw spring-boot:run
```

**Linux ou macOS:**

```bash
export TRINITY_JWT_SEGREDO="cole-o-segredo-gerado"
export TRINITY_ADMIN_EMAIL="admin@exemplo.com"
export TRINITY_ADMIN_SENHA="UmaSenhaForte123"
./mvnw spring-boot:run
```

**IntelliJ IDEA:** abra o projeto, vá em *Run → Edit Configurations*, adicione as variáveis em *Environment variables* (separadas por `;`) e execute `TrinityApplication`.

Depois acesse **http://localhost:8080** e entre com o e-mail e a senha do administrador.

O banco é criado sozinho em `./data/trinity.db` na primeira execução.

## Dados de demonstração

Para conhecer o sistema já populado, execute **uma vez** com `TRINITY_DEMO=true` em um **banco vazio** (sem o arquivo `data/trinity.db`):

```powershell
$env:TRINITY_DEMO = "true"
.\mvnw spring-boot:run
```

São criados membros, avisos, eventos, escalas, itens de patrimônio, lançamentos dos últimos 6 meses e contribuições. A carga **só roda quando o banco não tem nenhum membro** e nunca altera dados existentes. Depois da primeira execução, os dados ficam no banco e a variável pode ser removida.

A senha de todas as contas de demonstração é **`Trinity@123`**:

| E-mail | Perfil |
|---|---|
| `tesouraria@trinity.demo` | Financeiro |
| `secretaria@trinity.demo` | Membros, avisos e eventos |
| `louvor@trinity.demo` | Escalas |
| `membro@trinity.demo` | Membro comum |
| `admin@trinity.demo` | Administrador (só quando `TRINITY_ADMIN_EMAIL` não está definido; senão, use o administrador inicial) |

> Use essas contas e essa senha apenas para demonstração.

## Perfis e permissões

Cada usuário tem permissões independentes, e o administrador tem todas:

| Permissão | Libera |
|---|---|
| Administrador | Tudo, incluindo acessos, permissões e excluir ou desativar membros |
| Gerenciar membros | Cadastro de membros e relatório de membros |
| Gerenciar avisos | Criar, editar, ativar e excluir avisos |
| Gerenciar eventos | Criar, editar, cancelar e excluir eventos |
| Visualizar escalas | Ver as escalas |
| Gerenciar escalas | Criar e editar escalas e funções musicais |
| Gerenciar patrimônio | Patrimônio e relatório de patrimônio |
| Gerenciar financeiro | Lançamentos, categorias, confirmação de contribuições e relatório financeiro |

Qualquer usuário logado vê avisos e eventos, responde presença, confirma leitura, contribui por PIX, lê a Bíblia e altera a própria senha.

## Segurança

- Senhas guardadas com **BCrypt**.
- Login com **JWT de 15 minutos** e **refresh token de 7 dias** (de uso único, com rotação), ambos em cookies **HttpOnly** e `SameSite=Lax`.
- Sem sessão no servidor (`STATELESS`); as permissões são lidas do banco a cada requisição, então mudanças valem na hora.
- **Proteção CSRF** em todos os formulários.
- Rotas protegidas por permissão no `SecurityConfig`.
- Segredos e chaves fora do código, por variáveis de ambiente.

## Estrutura do código

O código é organizado em camadas, e cada camada é dividida por módulo:

```
src/main/java/com/trinity/trinity/
├── controller/    recebem as requisições e escolhem a tela
│   ├── avisos  biblia  escalas  eventos  financeiro
│   └── membros  patrimonio  relatorios  usuarios
├── service/       regras de negócio (mesmos módulos)
├── model/         entidades JPA (mesmos módulos)
├── repository/    acesso ao banco com Spring Data (mesmos módulos)
├── security/      JWT, filtros, permissões e rotas
├── exception/     exceções de regra de negócio
├── util/          utilitários (validação e formatação de CPF)
└── config/        carga de dados de demonstração

src/main/resources/
├── templates/     páginas Thymeleaf (forms/ e fragments/)
├── static/        CSS, logo e favicon
└── biblia/        texto da Bíblia
```

## Integrações

- **ViaCEP**: consultado pelo navegador para preencher o endereço a partir do CEP. É opcional, e o endereço continua editável.
- **PIX**: o código "copia e cola" (BR Code, com CRC16) é gerado pelo próprio sistema, sem chamar banco ou API. A confirmação do pagamento é manual, feita pela tesouraria.
- **CDNs**: Bootstrap, Bootstrap Icons, Chart.js e a biblioteca de QR Code são carregados pelo navegador.

## Créditos

Texto bíblico: **Bíblia Livre (BLIVRE)**, de Diego Santos, Mario Sergio e Marco Teles (2018), licença [Creative Commons Atribuição 4.0](https://creativecommons.org/licenses/by/4.0/).

## Observação sobre o PIX

A chave PIX padrão do projeto é a dos autores, usada para testes. Em uso real, defina `TRINITY_PIX_CHAVE`, `TRINITY_PIX_NOME` e `TRINITY_PIX_CIDADE` com os dados da igreja.
