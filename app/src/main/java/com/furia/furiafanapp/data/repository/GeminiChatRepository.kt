package com.furia.furiafanapp.data.repository

import android.content.Context
import android.util.Log
import com.furia.furiafanapp.data.model.ChatMessage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.random.Random

@Singleton
class GeminiChatRepository @Inject constructor(
    private val context: Context
) {
    private val TAG = "FuriaChatRepository"
    
    private val messageHistory = mutableListOf<Pair<String, String>>()

    val welcomeMessage = ChatMessage(
        user = "assistant",
        text = "Olá! Sou o assistente oficial da FURIA. Como posso ajudar você hoje?"
    )

    private val times = mapOf(
        "cs2" to "O time atual de CS2 da FURIA é composto por: art (capitão), yuurih, kscerato, chelo, FalleN, com saffee como reserva. O técnico é guerri.",
        "valorant" to "O time atual de Valorant da FURIA é composto por: Quick, Khalil, Mazin, Nozwerr e dgzin. O técnico é bzkA.",
        "lol" to "O time atual de League of Legends da FURIA é composto por: Ranger, Goot, Envy, Brance e RedBert. O técnico é Maestro.",
        "rainbow" to "O time atual de Rainbow Six Siege da FURIA é composto por: Fntzy, Stk, Pino, Miracle e Soulz1. O técnico é Twister.",
        "pubg" to "O time atual de PUBG da FURIA é composto por: rustyzera, killdemo, raspu e ITZZ. O técnico é Raspu.",
        "rocket" to "O time atual de Rocket League da FURIA é composto por: yanxnz, drufinho e CaioTG1. O técnico é Caard.",
        "kings league" to "A FURIA participa da Kings League com um time misto de jogadores brasileiros e ex-atletas de futebol.",
        "free fire" to "O time de Free Fire da FURIA é composto por: Federal, Trem, Gato, Cerol e Japa. O técnico é Faustão.",
        "fifa" to "A FURIA tem PHzin e Crepaldi como atletas oficiais de FIFA.",
        "fortnite" to "A FURIA tem k1nG, Sunz e Vortex como atletas de Fortnite."
    )
    
    private val jogadoresCS2 = mapOf(
        "art" to "Gabriel \"art\" Oliveira é o capitão do time de CS2 da FURIA. Nascido em 1997, ele é conhecido por seu estilo agressivo e liderança dentro do servidor. Está na FURIA desde 2018 e é considerado um dos melhores AWPers do Brasil.",
        "yuurih" to "Yuri \"yuurih\" Santos é um dos jogadores mais consistentes do time de CS2 da FURIA. Nascido em 1999, é conhecido por seu rifle preciso e capacidade de clutch. Está na FURIA desde 2017, sendo um dos membros mais antigos da equipe.",
        "kscerato" to "Kaike \"kscerato\" Cerato é considerado um dos melhores jogadores do Brasil no CS2. Nascido em 1999, tem um estilo de jogo muito técnico e é conhecido por sua precisão com rifles. Está na FURIA desde 2019.",
        "chelo" to "Rafael \"chelo\" Klemz é um rifler experiente que se juntou à FURIA em 2023. Nascido em 1997, passou por equipes como MIBR e GODSENT antes de chegar à FURIA.",
        "fallen" to "Gabriel \"FalleN\" Toledo é uma lenda do CS brasileiro, que se juntou à FURIA em 2024. Nascido em 1991, é bicampeão mundial pelo time da Luminosity/SK Gaming e trouxe sua vasta experiência como AWPer e líder para a FURIA.",
        "saffee" to "Rafael \"saffee\" Costa é o AWPer reserva do time de CS2 da FURIA. Nascido em 1994, se destacou na PAIN Gaming antes de se juntar à FURIA em 2022.",
        "guerri" to "Nicholas \"guerri\" Nogueira é o técnico do time de CS2 da FURIA desde 2018. Ex-jogador profissional, ele é considerado um dos melhores técnicos do Brasil e foi fundamental para o desenvolvimento da equipe."
    )
    
    private val jogadoresValorant = mapOf(
        "quick" to "Agustin \"Quick\" Gonzalez é o IGL do time de Valorant da FURIA. De origem argentina, se destacou no cenário sul-americano antes de se juntar à FURIA em 2023.",
        "khalil" to "Khalil \"Khalil\" Schmidt é um dos principais fraggers do time de Valorant da FURIA. Brasileiro, entrou para a equipe em 2024 após se destacar em torneios regionais.",
        "mazin" to "Matheus \"Mazin\" Araújo é o controlador do time de Valorant da FURIA. Brasileiro, está na equipe desde 2023 e é conhecido por seu domínio de agentes como Astra e Omen.",
        "nozwerr" to "Gabriel \"Nozwerr\" Alves é o sentinela do time de Valorant da FURIA. Brasileiro, entrou para a equipe em 2024 e se destaca com agentes como Killjoy e Cypher.",
        "dgzin" to "Douglas \"dgzin\" Silva é o duelista do time de Valorant da FURIA. Brasileiro, entrou para a equipe em 2024 após se destacar na LOUD Academy.",
        "bzka" to "Bruno \"bzkA\" Zanatta é o técnico do time de Valorant da FURIA desde 2023. Ex-jogador de CS:GO, trouxe sua experiência tática para o Valorant."
    )
    
    private val conquistas = mapOf(
        "cs2" to "As principais conquistas da FURIA no CS2/CSGO incluem:\n- Campeão da IEM Katowice 2023\n- Campeão da ESL Pro League Season 16 (2022)\n- Campeão da BLAST Premier Spring Finals 2023\n- Vice-campeão da ESL One Cologne 2024\n- Top 4 no Major de Paris 2023\n- Top 4 no Major de Copenhague 2024",
        "valorant" to "No Valorant, a FURIA conquistou:\n- Campeão do VCT Americas 2024\n- 3º lugar no Valorant Champions 2024\n- Campeão do VCT Brazil Stage 1 2023\n- Vice-campeão do VCT LOCK//IN São Paulo 2023\n- Campeão da Valorant Copa Elite Split 2 2024",
        "rainbow" to "No Rainbow Six Siege, a FURIA conquistou:\n- Campeão do Six Invitational 2023\n- Campeão do Brasileirão 2023 - Stage 2\n- Vice-campeão do Six Major Berlim 2024\n- Top 4 no Six Invitational 2025",
        "lol" to "No League of Legends, a FURIA conquistou:\n- Campeão do CBLOL 2024 - Split 2\n- Vice-campeão do CBLOL 2025 - Split 1\n- Participação no MSI 2025\n- Top 8 no Worlds 2024",
        "rocket" to "No Rocket League, a FURIA conquistou:\n- Campeão da RLCS South American Regional 2024\n- Top 6 no RLCS World Championship 2024\n- Bicampeão do Campeonato Brasileiro de Rocket League (2023-2024)"
    )
    
    private val organizacao = mapOf(
        "fundacao" to "A FURIA Esports foi fundada em 2017 por Jaime \"raizen\" Pádua e André Akkari. Inicialmente focada em CS:GO, a organização rapidamente expandiu para outras modalidades de esports.",
        "fundadores" to "Os fundadores da FURIA são Jaime \"raizen\" Pádua (ex-jogador profissional de poker) e André Akkari (campeão mundial de poker). Eles fundaram a organização em 2017 com a visão de criar uma equipe de esports de classe mundial representando o Brasil.",
        "sede" to "A sede principal da FURIA fica em São Paulo, Brasil. Em 2022, a organização também inaugurou um centro de treinamento em Miami, Estados Unidos, para facilitar a participação em competições internacionais.",
        "cores" to "As cores oficiais da FURIA são preto e amarelo/dourado. Essas cores são representadas em todos os uniformes e na identidade visual da organização.",
        "mascote" to "O mascote da FURIA é uma pantera negra, simbolizando agilidade, força e precisão. A pantera é frequentemente representada nos uniformes e materiais promocionais da equipe.",
        "slogan" to "O slogan da FURIA é \"#DIADEFURIA\" (Dia de Fúria), que representa a intensidade e paixão que a equipe traz para cada competição.",
        "valores" to "Os valores fundamentais da FURIA incluem excelência competitiva, desenvolvimento de talentos brasileiros, inovação no esports e conexão com a comunidade de fãs."
    )
    
    private val redesSociais = mapOf(
        "instagram" to "O Instagram oficial da FURIA é @furiagg, com mais de 2 milhões de seguidores em maio de 2025.",
        "twitter" to "O Twitter oficial da FURIA é @FURIA, com mais de 1,5 milhão de seguidores em maio de 2025.",
        "youtube" to "O canal oficial da FURIA no YouTube é FURIA, com mais de 1 milhão de inscritos em maio de 2025. Lá são publicados conteúdos exclusivos, bastidores e highlights das partidas.",
        "twitch" to "O canal oficial da FURIA na Twitch é furiatv, onde são transmitidos treinos, interações com jogadores e conteúdos exclusivos.",
        "tiktok" to "O TikTok oficial da FURIA é @furiagg, com mais de 1,2 milhão de seguidores em maio de 2025.",
        "discord" to "A FURIA possui um servidor oficial no Discord para a comunidade de fãs, onde são compartilhadas notícias e realizados eventos exclusivos.",
        "site" to "O site oficial da FURIA é furia.gg, onde você encontra todas as informações sobre times, jogadores, loja oficial e calendário de competições."
    )
    
    private val patrocinadores = mapOf(
        "principais" to "Os patrocinadores master da FURIA em 2025 são: Lenovo Legion, Red Bull, Betway e HyperX.",
        "tecnologia" to "A FURIA tem parcerias com empresas de tecnologia como Lenovo Legion (computadores), HyperX (periféricos) e AOC (monitores).",
        "bebidas" to "A Red Bull é a parceira oficial de bebidas energéticas da FURIA desde 2019.",
        "apostas" to "A Betway é a parceira oficial de apostas esportivas da FURIA desde 2020.",
        "roupas" to "A FURIA possui uma linha própria de roupas e acessórios, disponível em sua loja oficial (shop.furia.gg).",
        "nike" to "Em 2023, a FURIA fechou uma parceria com a Nike para o desenvolvimento de uniformes exclusivos para suas equipes."
    )
    
    private val curiosidades = listOf(
        "A FURIA foi a primeira organização brasileira de esports a abrir capital na Bolsa de Valores dos EUA, em 2023.",
        "O nome FURIA foi escolhido para representar a intensidade e a paixão que os jogadores brasileiros trazem para as competições.",
        "Em 2022, a FURIA lançou um token próprio (FURIA Token) para engajamento com fãs, oferecendo benefícios exclusivos.",
        "A FURIA possui um programa de desenvolvimento de talentos chamado FURIA Academy, que já revelou diversos jogadores para as equipes principais.",
        "Em 2024, a FURIA inaugurou a FURIA Arena em São Paulo, um espaço para competições e eventos com capacidade para 500 pessoas.",
        "A FURIA foi a primeira organização brasileira a ter uma gaming house nos Estados Unidos, facilitando a participação em competições internacionais.",
        "O jogador art, capitão de CS2 da FURIA, é conhecido por seu estilo agressivo e foi apelidado de 'Tubarão' pela comunidade.",
        "A FURIA possui um documentário chamado 'FURIAGG: A Ascensão', lançado em 2023, que conta a história da organização.",
        "Em 2024, a FURIA lançou um programa de responsabilidade social chamado 'FURIA do Bem', focado em inclusão digital em comunidades carentes.",
        "A FURIA foi a primeira organização brasileira a ter times competitivos em mais de 10 modalidades diferentes de esports."
    )
    
    private val eventos = mapOf(
        "proximos" to "Próximas competições da FURIA (maio 2025):\n- CS2: ESL Pro League Season 21 (15-30 de maio)\n- Valorant: VCT Americas Stage 2 (10-25 de maio)\n- League of Legends: CBLOL Split 2 (início em 8 de junho)\n- Rainbow Six: Six Major Madri (22-28 de maio)",
        "calendario" to "O calendário completo de competições da FURIA está disponível no site oficial (furia.gg/calendario) e é atualizado regularmente com novas datas e torneios.",
        "transmissoes" to "As partidas da FURIA são transmitidas oficialmente nos canais dos organizadores dos torneios (ESL, Riot Games, etc.) e também são comentadas no canal oficial da FURIA na Twitch."
    )
    
    private val generico = listOf(
        "A FURIA está sempre buscando evoluir em todas as modalidades de esports em que compete. Nosso objetivo é representar o Brasil no mais alto nível!",
        "Como fã da FURIA, você faz parte da nossa família! Continuamos trabalhando para trazer os melhores resultados em todas as competições.",
        "A FURIA valoriza muito seus fãs e está sempre buscando inovar, tanto dentro quanto fora dos jogos.",
        "A comunidade da FURIA é uma das mais apaixonadas do esports! É esse apoio que nos motiva a buscar sempre mais conquistas.",
        "A FURIA tem como missão não apenas vencer campeonatos, mas também desenvolver o cenário de esports no Brasil e inspirar novos talentos.",
        "O #DIADEFURIA é todo dia! Estamos sempre trabalhando para elevar o nome do Brasil no cenário internacional de esports.",
        "A FURIA acredita no potencial dos jogadores brasileiros e investe continuamente no desenvolvimento de novos talentos.",
        "Desde 2017, a FURIA vem construindo uma história de sucesso no esports mundial, representando o Brasil com garra e determinação.",
        "A FURIA é mais que uma organização de esports, é um movimento que une milhões de fãs apaixonados em todo o Brasil e no mundo.",
        "O sucesso da FURIA é resultado de muito trabalho, dedicação e uma visão clara de como desenvolver o esports brasileiro."
    )
    
    private val redirecionamento = listOf(
        "Como assistente da FURIA, posso falar sobre nossos times, jogadores, conquistas e história. O que você gostaria de saber sobre a FURIA?",
        "Estou aqui para conversar sobre a FURIA Esports! Posso te contar sobre nossos times ou competições recentes.",
        "Sou especializado em informações sobre a FURIA Esports. Que tal conversarmos sobre nossos times ou conquistas recentes?",
        "Como assistente da FURIA, meu foco é falar sobre nossa organização. Posso te contar sobre nossos jogadores, times ou história!",
        "Que tal falarmos sobre a FURIA? Posso te contar sobre nossos times, jogadores ou próximas competições.",
        "Estou aqui para compartilhar informações sobre a FURIA Esports. Em que posso ajudar você hoje?",
        "Como assistente oficial da FURIA, posso te fornecer informações atualizadas sobre nossa organização. O que gostaria de saber?"
    )

    suspend fun sendMessage(text: String): ChatMessage {
        return withContext(Dispatchers.IO) {
            try {
                messageHistory.add(Pair("user", text))
                
                delay(Random.nextLong(300, 800))
                
                val resposta = gerarResposta(text.lowercase())
                
                messageHistory.add(Pair("assistant", resposta))
                
                if (messageHistory.size > 10) {
                    messageHistory.subList(0, messageHistory.size - 10).clear()
                }
                
                Log.d(TAG, "Pergunta: $text")
                Log.d(TAG, "Resposta: $resposta")
                
                ChatMessage(user = "assistant", text = resposta)
            } catch (e: Exception) {
                Log.e(TAG, "Error generating response: ${e.message}", e)
                ChatMessage(
                    user = "assistant", 
                    text = "Desculpe, tive um problema ao processar sua mensagem. Como posso ajudar com informações sobre a FURIA?"
                )
            }
        }
    }
    
    private fun gerarResposta(pergunta: String): String {
        if (!isSobreFuria(pergunta)) {
            return redirecionamento.random()
        }
        
        for ((chave, resposta) in times) {
            if (pergunta.contains("time $chave") || pergunta.contains("elenco $chave") || 
                pergunta.contains("jogadores $chave") || pergunta.contains("lineup $chave") ||
                (pergunta.contains("time") && pergunta.contains(chave)) ||
                (pergunta.contains("quem joga") && pergunta.contains(chave))) {
                return resposta
            }
        }
        
        for ((nome, resposta) in jogadoresCS2) {
            if (pergunta.contains(nome)) {
                return resposta
            }
        }
        
        for ((nome, resposta) in jogadoresValorant) {
            if (pergunta.contains(nome)) {
                return resposta
            }
        }
        
        for ((modalidade, resposta) in conquistas) {
            if ((pergunta.contains("conquista") || pergunta.contains("título") || 
                 pergunta.contains("campeonato") || pergunta.contains("ganhou") || 
                 pergunta.contains("venceu")) && pergunta.contains(modalidade)) {
                return resposta
            }
        }
        
        if (pergunta.contains("conquista") || pergunta.contains("título") || 
            pergunta.contains("campeonato") || pergunta.contains("ganhou") || 
            pergunta.contains("venceu")) {
            return "A FURIA tem conquistas importantes em diversas modalidades:\n" +
                   "- CS2/CSGO: Campeão da IEM Katowice 2023, ESL Pro League Season 16, BLAST Premier Spring Finals 2023\n" +
                   "- Valorant: Campeão do VCT Americas 2024, 3º lugar no Valorant Champions 2024\n" +
                   "- Rainbow Six: Campeão do Six Invitational 2023\n" +
                   "- League of Legends: Campeão do CBLOL 2024 - Split 2\n" +
                   "- Rocket League: Campeão da RLCS South American Regional 2024"
        }
        
        for ((chave, resposta) in organizacao) {
            if (pergunta.contains(chave)) {
                return resposta
            }
        }
        
        for ((rede, resposta) in redesSociais) {
            if (pergunta.contains(rede)) {
                return resposta
            }
        }
        
        for ((tipo, resposta) in patrocinadores) {
            if ((pergunta.contains("patrocinador") || pergunta.contains("parceiro") || 
                 pergunta.contains("parceria") || pergunta.contains("sponsor")) && 
                (pergunta.contains(tipo) || tipo == "principais")) {
                return resposta
            }
        }
        
        for ((tipo, resposta) in eventos) {
            if ((pergunta.contains("evento") || pergunta.contains("competição") || 
                 pergunta.contains("torneio") || pergunta.contains("campeonato") ||
                 pergunta.contains("próximo") || pergunta.contains("quando") ||
                 pergunta.contains("agenda") || pergunta.contains("calendário")) && 
                (pergunta.contains(tipo) || tipo == "proximos")) {
                return resposta
            }
        }
        
        if (pergunta.contains("curiosidade") || pergunta.contains("fato") || 
            pergunta.contains("interessante") || pergunta.contains("sabia")) {
            return "Curiosidade sobre a FURIA: ${curiosidades.random()}"
        }
        
        if (pergunta.contains("história") || pergunta.contains("sobre") || 
            pergunta.contains("fundação") || pergunta.contains("quem é") || 
            pergunta.contains("o que é") || pergunta.contains("fundador")) {
            return organizacao["fundacao"] ?: generico.random()
        }
        
        return generico.random()
    }
    
    private fun isSobreFuria(pergunta: String): Boolean {
        val palavrasChaveFuria = listOf(
            "furia", "pantera", "panteras", "cs2", "csgo", "valorant", "lol", "league of legends", 
            "rainbow six", "r6", "pubg", "rocket league", "esports", "esport", "time", "jogador", 
            "jogadores", "campeonato", "torneio", "brasil", "brasileiro", "art", "yuurih", "kscerato", 
            "fallen", "chelo", "quick", "akkari", "raizen", "diadefuria", "dia de furia"
        )
        
        return palavrasChaveFuria.any { pergunta.contains(it) }
    }
    
    fun clearConversation() {
        messageHistory.clear()
    }
}