# FURIA Fan App

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

O aplicativo foi desenvolvido utilizando tecnologias modernas do ecossistema Android, como Jetpack Compose para a interface do usuário e Firebase para backend, proporcionando uma experiência fluida e responsiva para os usuários.

## 🎯 Objetivo

O objetivo principal do FURIA Fan App é proporcionar uma plataforma interativa e engajante para os fãs da FURIA, fortalecendo a comunidade ao redor do time e oferecendo conteúdo exclusivo e funcionalidades que aumentam a conexão entre os fãs e o time.

Os principais objetivos incluem:

- Aumentar o engajamento dos fãs com o time FURIA
- Proporcionar uma experiência gamificada com sistema de pontos e recompensas
- Criar uma comunidade ativa de fãs dentro do aplicativo
- Oferecer formas inovadoras de interação com o time e outros fãs
- Disponibilizar informações em tempo real sobre partidas e jogadores

## 🛠️ Tecnologias Utilizadas

### Linguagem e Framework
- **Kotlin**: Linguagem principal de desenvolvimento, oferecendo recursos modernos como null-safety, funções de extensão e coroutines
- **Jetpack Compose**: Framework declarativo para construção de interfaces de usuário, permitindo uma experiência de desenvolvimento mais produtiva e código mais conciso

### Arquitetura e Padrões
- **MVVM (Model-View-ViewModel)**: Padrão de arquitetura principal que separa a lógica de negócios da interface do usuário
- **Clean Architecture**: Organização do código em camadas bem definidas para melhor manutenibilidade e testabilidade
- **Repository Pattern**: Para abstrair e centralizar o acesso a dados
- **Dependency Injection**: Implementada com Hilt para gerenciar dependências de forma eficiente

### Backend e Armazenamento
- **Firebase Authentication**: Sistema completo de autenticação com suporte a email/senha e provedores sociais
- **Firebase Firestore**: Banco de dados NoSQL escalável para armazenamento de dados do usuário, apostas e informações de partidas
- **Firebase Realtime Database**: Para funcionalidades em tempo real como chat e atualizações de partidas
- **Firebase Storage**: Armazenamento de imagens e outros arquivos de mídia
- **Firebase Cloud Messaging**: Sistema de notificações push para alertas de partidas e eventos

### Bibliotecas e Componentes
- **Kotlin Coroutines**: Para gerenciamento de operações assíncronas de forma concisa e eficiente
- **Kotlin Flow**: Para streams de dados reativos e transformações
- **Navigation Component**: Gerenciamento de navegação entre telas com suporte a argumentos tipados
- **Coil**: Biblioteca eficiente para carregamento e cache de imagens
- **Material Design 3**: Implementação dos mais recentes componentes e diretrizes de design do Google
- **Retrofit**: Cliente HTTP para comunicação com APIs externas
- **Room**: Camada de abstração sobre SQLite para cache local de dados
- **WorkManager**: Para execução confiável de tarefas em background
- **Accompanist**: Conjunto de bibliotecas auxiliares para Jetpack Compose
- **DataStore**: Para armazenamento de preferências do usuário de forma tipada

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

### Princípios Arquiteturais

1. **Separação de Responsabilidades**: Cada componente tem uma responsabilidade única e bem definida
2. **Dependências Unidirecionais**: As dependências fluem de fora para dentro (UI → Domain → Data)
3. **Abstração**: As camadas superiores não conhecem os detalhes de implementação das camadas inferiores
4. **Testabilidade**: A arquitetura facilita a escrita de testes unitários, de integração e de UI
5. **Modularidade**: Componentes podem ser desenvolvidos, testados e mantidos de forma independente

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
- **Histórico de Atividades**: Registro de todas as ações do usuário no aplicativo

### 2. Mini-Jogos
- **Quiz FURIA**: Perguntas sobre o time, jogadores e história
- **Jogo da Memória**: Encontre pares de cards com jogadores da FURIA
- **Palavras Embaralhadas**: Descubra palavras relacionadas à FURIA
- **Desafios Diários**: Tarefas para ganhar pontos extras
- **Leaderboard**: Ranking dos melhores jogadores em cada mini-jogo
- **Sistema de Recompensas**: Prêmios virtuais para desempenho nos jogos

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
- **Histórico de Apostas**: Registro detalhado de apostas anteriores
- **Leaderboard**: Ranking dos melhores apostadores
- **Análise de Desempenho**: Estatísticas de sucesso nas apostas
- **Sistema de Odds**: Cálculo dinâmico de odds baseado em estatísticas reais

### 4. Acompanhamento de Partidas
- **Próximas Partidas**: Calendário completo de jogos futuros
- **Partidas ao Vivo**: Acompanhamento em tempo real com estatísticas
- **Histórico de Partidas**: Resultados e highlights de jogos anteriores
- **Notificações**: Alertas personalizáveis para início de partidas
- **Estatísticas Detalhadas**: Análise profunda de desempenho por jogador e equipe
- **Transmissões**: Links diretos para assistir às partidas em plataformas de streaming

### 5. Comunidade
- **Chat**: Sistema de conversas entre fãs com moderação
- **Chatbot**: Assistente virtual para informações sobre o time
- **Compartilhamento**: Integração com redes sociais para compartilhar conquistas
- **Fóruns de Discussão**: Espaços temáticos para discussões sobre jogos e estratégias
- **Eventos**: Calendário de eventos oficiais da FURIA
- **Conteúdo Exclusivo**: Acesso a conteúdos exclusivos do time

### 6. Loja Virtual
- **Itens Virtuais**: Compra de itens com pontos acumulados
- **Recompensas Exclusivas**: Conteúdo exclusivo para fãs
- **Histórico de Compras**: Registro detalhado de transações
- **Catálogo Dinâmico**: Itens atualizados regularmente
- **Sistema de Raridade**: Itens com diferentes níveis de raridade
- **Promoções**: Ofertas especiais em datas comemorativas

## 📱 Telas Principais

### Tela Inicial
- Dashboard com acesso a todas as funcionalidades
- Feed de notícias sobre a FURIA
- Próximas partidas em destaque
- Resumo de pontos e conquistas do usuário
- Acesso rápido às funcionalidades mais utilizadas

### Mini-Jogos
- Seleção de jogos disponíveis
- Interface interativa para cada mini-jogo
- Telas de conclusão com feedback e pontuação
- Instruções detalhadas para cada jogo
- Histórico de partidas jogadas

### Arena de Apostas
- Visualização de partidas disponíveis para apostas
- Formulário para seleção de tipo de aposta e valor
- Histórico e estatísticas de apostas
- Detalhes das odds e potenciais ganhos
- Análise de tendências de apostas

### Perfil e Configurações
- Informações detalhadas do usuário
- Estatísticas de uso e desempenho
- Preferências e configurações personalizáveis
- Gerenciamento de notificações
- Opções de privacidade e segurança

## 🔄 Fluxo de Dados

### 1. Autenticação
- O usuário se autentica via Firebase Authentication
- Os dados do perfil são armazenados no Firestore
- As preferências do usuário são salvas localmente com DataStore
- O token de autenticação é gerenciado para requisições seguras

### 2. Mini-Jogos
- Os dados dos jogos são carregados do Firestore
- Os resultados e pontuações são sincronizados com o backend
- As estatísticas de desempenho são analisadas e armazenadas
- O leaderboard é atualizado em tempo real

### 3. Arena de Apostas
- As informações das partidas são obtidas via API
- As apostas são registradas no Firestore
- Os resultados são processados e os pontos distribuídos automaticamente
- As estatísticas de apostas são analisadas para ajustar as odds

### 4. Loja Virtual
- Os itens disponíveis são carregados do Firestore
- As transações são processadas e registradas no backend
- O inventário do usuário é atualizado em tempo real
- As notificações de novas aquisições são enviadas ao usuário

## 🚀 Instalação e Configuração

### Pré-requisitos
- Android Studio Arctic Fox ou superior
- JDK 11 ou superior
- Dispositivo/emulador com Android 6.0 (API 23) ou superior
- Conta no Firebase para configuração do backend

### Configuração do Ambiente de Desenvolvimento
1. Clone o repositório:
   ```
   git clone https://github.com/litorx/Furia_App.git
   ```

2. Abra o projeto no Android Studio

3. Configure o Firebase:
   - Crie um projeto no Firebase Console
   - Adicione um aplicativo Android com o pacote `com.furia.furiafanapp`
   - Baixe o arquivo `google-services.json` e coloque-o na pasta `app/`
   - Ative os serviços necessários: Authentication, Firestore, Storage, Cloud Messaging

4. Configure as variáveis de ambiente:
   - Crie um arquivo `local.properties` na raiz do projeto (se não existir)
   - Adicione as chaves de API necessárias seguindo o formato do arquivo `local.properties.example`

5. Execute o aplicativo em um emulador ou dispositivo físico

### Estrutura do Projeto
- **app**: Módulo principal do aplicativo
- **buildSrc**: Definições de dependências e versões
- **gradle**: Configurações do Gradle

## 📊 Monitoramento e Analytics

O aplicativo utiliza Firebase Analytics para monitorar:
- Engajamento dos usuários
- Uso de funcionalidades
- Retenção e conversão
- Desempenho e crashes
- Funis de conversão
- Segmentação de usuários

### Métricas Principais
- **DAU/MAU**: Usuários ativos diários e mensais
- **Retenção**: Taxa de retorno dos usuários
- **Tempo de Sessão**: Duração média das sessões
- **Engajamento**: Interações por sessão
- **Conversão**: Taxa de conclusão de objetivos

## 🔒 Segurança

- **Autenticação**: Implementação segura via Firebase Authentication
- **Validação de Dados**: Verificação de entradas do usuário em todos os níveis
- **Regras de Segurança**: Configurações robustas no Firestore e Storage
- **Criptografia**: Dados sensíveis são criptografados
- **Proteção contra Ataques**: Implementação de medidas contra ataques comuns
- **Atualizações Regulares**: Bibliotecas mantidas atualizadas para corrigir vulnerabilidades

## 🧪 Testes

O projeto inclui uma suíte completa de testes:

### Testes Unitários
- Testes para ViewModels, Repositories e UseCases
- Mocking de dependências com Mockito
- Testes de fluxos de dados com Turbine

### Testes de Integração
- Testes de integração entre componentes
- Testes de repositórios com dados reais
- Testes de APIs e serviços externos

### Testes de UI
- Testes de componentes Compose com ComposeTestRule
- Testes de navegação e fluxos de usuário
- Testes de acessibilidade

### Automação de Testes
- Execução de testes em CI/CD
- Relatórios de cobertura de código
- Análise estática de código

---

<p align="center">
  <i>FURIA Fan App - Conectando fãs ao time que amam</i>
</p>
