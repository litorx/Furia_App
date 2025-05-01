# FURIA Fan App

<p align="center">
  <img src="https://i.imgur.com/placeholder_furia_logo.png" width="200" alt="FURIA Fan App Logo"/>
</p>

<p align="center">
  <a href="#-sobre-o-projeto">Sobre</a> •
  <a href="#-objetivo">Objetivo</a> •
  <a href="#%EF%B8%8F-tecnologias">Tecnologias</a> •
  <a href="#%EF%B8%8F-arquitetura">Arquitetura</a> •
  <a href="#-funcionalidades">Funcionalidades</a> •
  <a href="#-telas-principais">Telas</a> •
  <a href="#-instalação-e-configuração">Instalação</a> •
  <a href="#-testes">Testes</a>
</p>

## 📱 Sobre o Projeto

FURIA Fan App é um aplicativo móvel desenvolvido para os fãs do time de esports FURIA. O aplicativo oferece uma experiência completa para os fãs acompanharem seu time favorito, participarem de mini-jogos, fazerem apostas virtuais em partidas, interagirem com outros fãs e ganharem recompensas exclusivas.

## 🎯 Objetivo

O objetivo principal do FURIA Fan App é proporcionar uma plataforma interativa e engajante para os fãs da FURIA, fortalecendo a comunidade ao redor do time e oferecendo conteúdo exclusivo e funcionalidades que aumentam a conexão entre os fãs e o time.

## 🛠️ Tecnologias Utilizadas

### Linguagem e Framework
- **Kotlin**: Linguagem principal de desenvolvimento
- **Jetpack Compose**: Framework moderno para UI declarativa

### Arquitetura e Padrões
- **MVVM (Model-View-ViewModel)**: Padrão de arquitetura principal
- **Clean Architecture**: Organização do código em camadas
- **Repository Pattern**: Para acesso a dados
- **Dependency Injection**: Com Hilt

### Backend e Armazenamento
- **Firebase Authentication**: Para autenticação de usuários
- **Firebase Firestore**: Banco de dados NoSQL para armazenamento
- **Firebase Realtime Database**: Para funcionalidades em tempo real (chat)
- **Firebase Storage**: Para armazenamento de imagens e mídias
- **Firebase Cloud Messaging**: Para notificações push

### Bibliotecas e Componentes
- **Kotlin Coroutines**: Para operações assíncronas
- **Kotlin Flow**: Para streams de dados reativos
- **Navigation Component**: Para navegação entre telas
- **Coil**: Para carregamento e cache de imagens
- **Material Design 3**: Para componentes de UI
- **Retrofit**: Para requisições HTTP
- **Room**: Para cache local de dados
- **WorkManager**: Para tarefas em background

## 🏗️ Arquitetura

O FURIA Fan App segue os princípios da Clean Architecture combinados com o padrão MVVM, organizando o código em camadas bem definidas:

### Camadas da Arquitetura

```
com.furia.furiafanapp/
├── data/                  # Camada de Dados
│   ├── model/             # Modelos de dados
│   ├── repository/        # Implementações dos repositórios
│   └── source/            # Fontes de dados (local e remoto)
├── domain/                # Camada de Domínio
│   ├── model/             # Entidades de domínio
│   ├── repository/        # Interfaces dos repositórios
│   └── usecase/           # Casos de uso
├── di/                    # Injeção de Dependência
├── ui/                    # Camada de Apresentação
│   ├── components/        # Componentes reutilizáveis
│   ├── screens/           # Telas do aplicativo
│   └── theme/             # Tema e estilos
├── util/                  # Utilitários
└── worker/                # Workers para tarefas em background
```

## 📊 Diagrama de Classes

### Principais Modelos de Dados

```
┌─────────────┐     ┌─────────────┐     ┌─────────────┐
│ UserProfile │     │    Match    │     │    Team     │
├─────────────┤     ├─────────────┤     ├─────────────┤
│ id: String  │     │ id: String  │     │ id: String  │
│ username    │     │ homeTeam    │◄────┤ name        │
│ email       │     │ awayTeam    │     │ logoUrl     │
│ photoUrl    │     │ tournament  │     └─────────────┘
│ points      │     │ startTime   │     
│ badges      │     │ status      │     ┌─────────────┐
└─────────────┘     │ score       │     │ Tournament  │
                    │ streams     │     ├─────────────┤
┌─────────────┐     └─────────────┘     │ id: String  │
│     Bet     │                         │ name        │
├─────────────┤     ┌─────────────┐     │ game        │
│ id: String  │     │ Challenge   │     └─────────────┘
│ userId      │     ├─────────────┤     
│ matchId     │     │ id: String  │     ┌─────────────┐
│ betAmount   │     │ title       │     │  ShopItem   │
│ betType     │     │ description │     ├─────────────┤
│ playerName  │     │ points      │     │ id: String  │
│ statPred.   │     │ actionType  │     │ name        │
│ odds        │     │ actionData  │     │ description │
│ potentialWin│     │ completed   │     │ price       │
│ status      │     └─────────────┘     │ imageUrl    │
└─────────────┘                         │ type        │
                                        └─────────────┘
```

### Arquitetura MVVM

```
┌───────────────┐       ┌───────────────┐       ┌───────────────┐
│     View      │       │   ViewModel   │       │  Repository   │
├───────────────┤       ├───────────────┤       ├───────────────┤
│ Compose UI    │◄─────►│ State         │◄─────►│ Interface     │
│ Components    │       │ Events        │       │               │
│ Screens       │       │ Actions       │       │               │
└───────────────┘       └───────────────┘       └───────────────┘
                                                        ▲
                                                        │
                                                        ▼
                                               ┌───────────────┐
                                               │  Data Source  │
                                               ├───────────────┤
                                               │ Remote (API)  │
                                               │ Local (Room)  │
                                               │ Firebase      │
                                               └───────────────┘
```

## 🔍 Funcionalidades

### 1. Autenticação e Perfil
- **Login e Registro**: Autenticação com email/senha ou redes sociais
- **Perfil de Usuário**: Visualização e edição de informações do perfil
- **Sistema de Pontos**: Acúmulo de pontos através de atividades no app
- **Badges e Conquistas**: Recompensas por ações e engajamento

### 2. Mini-Jogos
- **Quiz FURIA**: Perguntas sobre o time, jogadores e história
- **Jogo da Memória**: Encontre pares de cards com jogadores da FURIA
- **Palavras Embaralhadas**: Descubra palavras relacionadas à FURIA
- **Desafios Diários**: Tarefas para ganhar pontos extras

### 3. Arena de Apostas
- **Apostas em Estatísticas**: Previsões sobre desempenho dos jogadores
- **Tipos de Apostas**:
  - MVP da partida
  - Total de kills
  - Porcentagem de headshots
  - Clutches
  - Aces
  - Primeiro abate
  - Bombas plantadas
  - Kills com faca
  - Rounds de pistola
  - Total de kills da equipe
- **Histórico de Apostas**: Registro de apostas anteriores
- **Leaderboard**: Ranking dos melhores apostadores

### 4. Acompanhamento de Partidas
- **Próximas Partidas**: Calendário de jogos futuros
- **Partidas ao Vivo**: Acompanhamento em tempo real
- **Histórico de Partidas**: Resultados de jogos anteriores
- **Notificações**: Alertas para início de partidas

### 5. Comunidade
- **Chat**: Conversas entre fãs
- **Chatbot**: Assistente virtual para informações sobre o time
- **Compartilhamento**: Integração com redes sociais

### 6. Loja Virtual
- **Itens Virtuais**: Compra de itens com pontos acumulados
- **Recompensas Exclusivas**: Conteúdo exclusivo para fãs
- **Histórico de Compras**: Registro de transações

## 📱 Telas Principais

### Tela Inicial
- Dashboard com acesso a todas as funcionalidades
- Feed de notícias sobre a FURIA
- Próximas partidas em destaque

### Mini-Jogos
- Seleção de jogos disponíveis
- Interface interativa para cada mini-jogo
- Telas de conclusão com feedback e pontuação

### Arena de Apostas
- Visualização de partidas disponíveis para apostas
- Formulário para seleção de tipo de aposta e valor
- Histórico e estatísticas de apostas

### Perfil e Configurações
- Informações do usuário
- Estatísticas de uso
- Preferências e configurações

## 🔄 Fluxo de Dados

1. **Autenticação**:
   - O usuário se autentica via Firebase Authentication
   - Os dados do perfil são armazenados no Firestore

2. **Mini-Jogos**:
   - Os dados dos jogos são carregados do Firestore
   - Os resultados e pontuações são sincronizados com o backend

3. **Arena de Apostas**:
   - As informações das partidas são obtidas via API
   - As apostas são registradas no Firestore
   - Os resultados são processados e os pontos distribuídos automaticamente

4. **Loja Virtual**:
   - Os itens disponíveis são carregados do Firestore
   - As transações são processadas e registradas no backend

## 🚀 Instalação e Configuração

### Pré-requisitos
- Android Studio Arctic Fox ou superior
- JDK 11 ou superior
- Dispositivo/emulador com Android 6.0 (API 23) ou superior

### Configuração
1. Clone o repositório:
   ```
   git clone https://github.com/furia/furiafanapp.git
   ```

2. Abra o projeto no Android Studio

3. Configure o Firebase:
   - Crie um projeto no Firebase Console
   - Adicione um aplicativo Android com o pacote `com.furia.furiafanapp`
   - Baixe o arquivo `google-services.json` e coloque-o na pasta `app/`

4. Execute o aplicativo em um emulador ou dispositivo físico

## 📊 Monitoramento e Analytics

O aplicativo utiliza Firebase Analytics para monitorar:
- Engajamento dos usuários
- Uso de funcionalidades
- Retenção e conversão
- Desempenho e crashes

## 🔒 Segurança

- Autenticação segura via Firebase
- Validação de dados de entrada
- Regras de segurança no Firestore
- Proteção contra ataques comuns

## 🧪 Testes

O projeto inclui:
- Testes unitários para lógica de negócios
- Testes de integração para repositórios
- Testes de UI com Compose Testing

---

<p align="center">
  <i>FURIA Fan App - Conectando fãs ao time que amam</i>
</p>
