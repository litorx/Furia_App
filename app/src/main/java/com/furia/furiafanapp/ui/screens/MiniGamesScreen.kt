package com.furia.furiafanapp.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.Dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.furia.furiafanapp.R
import com.furia.furiafanapp.ui.theme.FuriaBlack
import com.furia.furiafanapp.ui.theme.FuriaWhite
import com.furia.furiafanapp.ui.theme.FuriaYellow
import com.furia.furiafanapp.ui.components.UserProfileHeader
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.time.format.TextStyle
import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.compose.foundation.lazy.grid.items
import com.furia.furiafanapp.data.model.UserProfile
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.material3.TopAppBarState
import androidx.compose.ui.platform.LocalConfiguration

// Definição da classe MiniGame
data class MiniGame(
    val title: String,
    val iconResId: Int,
    val description: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MiniGamesScreen(
    navController: NavHostController,
    viewModel: MiniGamesViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current
    val screenWidth = LocalConfiguration.current.screenWidthDp.dp

    // Função para voltar à tela de seleção de mini-jogos
    val onBackToSelection = {
        viewModel.setCurrentGame(CurrentGame.NONE)
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = when (uiState.currentGame) {
                            CurrentGame.NONE -> "Mini-Jogos"
                            CurrentGame.QUIZ -> "Quiz FURIA"
                            CurrentGame.MEMORY -> "Jogo da Memória"
                            CurrentGame.CHALLENGES -> "Desafios Diários"
                            CurrentGame.WORD_SCRAMBLE -> "Palavras Embaralhadas"
                        },
                        color = FuriaWhite,
                        fontWeight = FontWeight.Bold
                    )
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = FuriaBlack
                ),
                navigationIcon = {
                    if (uiState.currentGame != CurrentGame.NONE) {
                        IconButton(onClick = onBackToSelection) {
                            Icon(
                                imageVector = Icons.Default.ArrowBack,
                                contentDescription = "Voltar",
                                tint = FuriaWhite
                            )
                        }
                    } else {
                        // Botão para voltar à tela inicial quando estiver na tela de seleção de jogos
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(
                                imageVector = Icons.Default.ArrowBack,
                                contentDescription = "Voltar para tela inicial",
                                tint = FuriaWhite
                            )
                        }
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(FuriaBlack)
        ) {
            when (uiState.currentGame) {
                CurrentGame.NONE -> {
                    MiniGamesSelection(
                        uiState = uiState,
                        onGameSelected = { game ->
                            viewModel.setCurrentGame(game)
                        }
                    )
                }
                CurrentGame.QUIZ -> {
                    if (uiState.quizCompleted) {
                        GameCompletionScreen(
                            title = "Quiz Completo!",
                            message = "Você acertou ${uiState.correctAnswers} de ${uiState.quizQuestions.size} perguntas.",
                            pointsEarned = viewModel.calculateQuizPoints(),
                            onPlayAgain = { viewModel.restartQuiz() },
                            onFinish = onBackToSelection,
                            titleFontSize = 24.sp,
                            subtitleFontSize = 18.sp,
                            bodyFontSize = 16.sp,
                            screenWidth = screenWidth,
                            paddingValue = 16.dp
                        )
                    } else {
                        QuizGame(
                            uiState = uiState,
                            onSelectAnswer = { viewModel.selectAnswer(it) },
                            onNextQuestion = { viewModel.nextQuestion() },
                            onFinishQuiz = { viewModel.finishQuiz() },
                            onFinish = onBackToSelection,
                            onRestart = { viewModel.restartQuiz() },
                            screenWidth = screenWidth
                        )
                    }
                }
                CurrentGame.MEMORY -> {
                    if (uiState.memoryGameCompleted) {
                        GameCompletionScreen(
                            title = "Jogo da Memória Completo!",
                            message = "Você completou o jogo em ${uiState.memoryMoves} movimentos.",
                            pointsEarned = viewModel.calculateMemoryPoints(uiState.memoryMoves),
                            onPlayAgain = { viewModel.restartMemoryGame() },
                            onFinish = onBackToSelection,
                            titleFontSize = 24.sp,
                            subtitleFontSize = 18.sp,
                            bodyFontSize = 16.sp,
                            screenWidth = screenWidth,
                            paddingValue = 16.dp
                        )
                    } else {
                        MemoryGame(
                            uiState = uiState,
                            onCardClick = { viewModel.flipCard(it) },
                            onFinish = onBackToSelection,
                            onRestart = { viewModel.restartMemoryGame() }
                        )
                    }
                }
                CurrentGame.CHALLENGES -> {
                    DailyChallenges(
                        uiState = uiState,
                        onChallengeComplete = { viewModel.completeChallenge(it) },
                        onFinish = onBackToSelection,
                        snackbarHostState = snackbarHostState
                    )
                }
                CurrentGame.WORD_SCRAMBLE -> {
                    if (uiState.wordScrambleCompleted) {
                        GameCompletionScreen(
                            title = if (uiState.wordScrambleSuccess) "Palavra Descoberta!" else "Jogo Finalizado",
                            message = if (uiState.wordScrambleSuccess) 
                                "Você descobriu a palavra '${uiState.wordScrambleOriginalWord}' em ${uiState.wordScrambleAttempts} tentativas." 
                            else 
                                "A palavra correta era: ${uiState.wordScrambleOriginalWord}",
                            pointsEarned = if (uiState.wordScrambleSuccess) 50 else 0,
                            onPlayAgain = { viewModel.initializeWordScrambleGame() },
                            onFinish = onBackToSelection,
                            titleFontSize = 24.sp,
                            subtitleFontSize = 18.sp,
                            bodyFontSize = 16.sp,
                            screenWidth = screenWidth,
                            paddingValue = 16.dp
                        )
                    } else {
                        WordScrambleGame(
                            uiState = uiState,
                            onInputChange = { viewModel.updateWordScrambleInput(it) },
                            onSubmit = { viewModel.checkWordScrambleSolution() },
                            onFinish = onBackToSelection,
                            onRestart = { viewModel.initializeWordScrambleGame() }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun GameCard(
    game: MiniGame,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(0.8f)
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = FuriaBlack
        ),
        border = BorderStroke(1.dp, FuriaYellow),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Ícone do jogo
            Icon(
                imageVector = Icons.Default.Gamepad,
                contentDescription = null,
                tint = FuriaYellow,
                modifier = Modifier
                    .size(48.dp)
                    .padding(bottom = 8.dp)
            )
            
            // Nome do jogo
            Text(
                text = game.title,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = FuriaWhite,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(vertical = 8.dp)
            )
            
            // Descrição do jogo
            Text(
                text = game.description,
                fontSize = 12.sp,
                color = FuriaWhite.copy(alpha = 0.7f),
                textAlign = TextAlign.Center,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun MiniGamesSelection(
    uiState: MiniGamesUiState,
    onGameSelected: (CurrentGame) -> Unit
) {
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Exibir o perfil do usuário com o componente padronizado
        uiState.userProfile?.let { profile ->
            UserProfileHeader(
                userProfile = profile,
                isTopOne = uiState.isTopOne
            )
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Text(
            text = "Escolha um mini-jogo",
            style = MaterialTheme.typography.headlineMedium,
            color = FuriaYellow,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        
        // Game selection
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            val miniGames = listOf(
                MiniGame("Quiz Semanal", R.drawable.ic_arena, "Teste seus conhecimentos sobre a FURIA e ganhe até 50 pontos!"),
                MiniGame("Jogo da Memória", R.drawable.ic_arena, "Encontre os pares de cartas com os jogadores da FURIA e ganhe pontos!"),
                MiniGame("Desafios Diários", R.drawable.ic_arena, "Complete desafios diários para ganhar pontos extras!"),
                MiniGame("Palavras Embaralhadas", R.drawable.ic_arena, "Descubra as palavras relacionadas à FURIA!")
            )
            items(miniGames) { game ->
                GameCard(game = game) {
                    when (game.title) {
                        "Quiz Semanal" -> {
                            onGameSelected(CurrentGame.QUIZ)
                        }
                        "Jogo da Memória" -> {
                            onGameSelected(CurrentGame.MEMORY)
                        }
                        "Desafios Diários" -> {
                            onGameSelected(CurrentGame.CHALLENGES)
                        }
                        "Palavras Embaralhadas" -> {
                            onGameSelected(CurrentGame.WORD_SCRAMBLE)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun QuizGame(
    uiState: MiniGamesUiState,
    onSelectAnswer: (Int) -> Unit,
    onNextQuestion: () -> Unit,
    onFinishQuiz: () -> Unit,
    onFinish: () -> Unit,
    onRestart: () -> Unit,
    screenWidth: Dp
) {
    val titleFontSize = if (screenWidth > 600.dp) 24.sp else 20.sp
    val questionFontSize = if (screenWidth > 600.dp) 20.sp else 16.sp
    val optionFontSize = if (screenWidth > 600.dp) 18.sp else 14.sp
    val paddingValue = if (screenWidth > 600.dp) 24.dp else 16.dp
    
    Box(modifier = Modifier.fillMaxSize()) {
        // Mostrar tela de conclusão quando o quiz for completado
        if (uiState.quizCompleted) {
            GameCompletionScreen(
                title = "Quiz Completo!",
                message = "Você acertou ${uiState.correctAnswers} de ${uiState.quizQuestions.size} perguntas!",
                pointsEarned = uiState.earnedPoints,
                onPlayAgain = onRestart,
                onFinish = onFinish,
                titleFontSize = titleFontSize,
                subtitleFontSize = questionFontSize,
                bodyFontSize = optionFontSize,
                screenWidth = screenWidth,
                paddingValue = paddingValue
            )
            return
        }
        
        // Se não tiver perguntas, mostrar mensagem de carregamento
        if (uiState.quizQuestions.isEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValue),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Carregando perguntas...",
                    fontSize = titleFontSize,
                    color = FuriaWhite,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(16.dp))
                CircularProgressIndicator(color = FuriaYellow)
            }
            return
        }
        
        // Conteúdo do quiz
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValue)
        ) {
            // Progresso do quiz
            LinearProgressIndicator(
                progress = (uiState.currentQuestionIndex + 1).toFloat() / uiState.quizQuestions.size,
                modifier = Modifier.fillMaxWidth(),
                color = FuriaYellow,
                trackColor = FuriaWhite.copy(alpha = 0.2f)
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Indicador de progresso textual
            Text(
                text = "Pergunta ${uiState.currentQuestionIndex + 1} de ${uiState.quizQuestions.size}",
                fontSize = optionFontSize,
                color = FuriaWhite.copy(alpha = 0.7f)
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Pergunta atual
            val currentQuestion = uiState.quizQuestions[uiState.currentQuestionIndex]
            Text(
                text = currentQuestion.question,
                fontSize = questionFontSize,
                color = FuriaWhite,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Opções de resposta
            currentQuestion.options.forEachIndexed { index, option ->
                val isSelected = uiState.selectedAnswerIndex == index
                val isCorrect = index == currentQuestion.correctAnswerIndex
                val showResult = uiState.showQuizAnswerResult
                
                val backgroundColor = when {
                    !showResult && isSelected -> FuriaYellow
                    showResult && isSelected && isCorrect -> Color.Green
                    showResult && isSelected && !isCorrect -> Color.Red
                    showResult && isCorrect -> Color.Green.copy(alpha = 0.7f)
                    else -> FuriaWhite.copy(alpha = 0.1f)
                }
                
                val textColor = when {
                    !showResult && isSelected -> FuriaBlack
                    else -> FuriaWhite
                }
                
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                        .clickable(enabled = !uiState.showQuizAnswerResult) {
                            onSelectAnswer(index)
                        },
                    colors = CardDefaults.cardColors(
                        containerColor = backgroundColor
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Text(
                            text = option,
                            fontSize = optionFontSize,
                            color = textColor
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.weight(1f))
            
            // Botão para próxima pergunta ou finalizar
            if (uiState.showQuizAnswerResult) {
                Button(
                    onClick = {
                        if (uiState.currentQuestionIndex >= uiState.quizQuestions.size - 1) {
                            onFinishQuiz()
                        } else {
                            onNextQuestion()
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = FuriaYellow,
                        contentColor = FuriaBlack
                    )
                ) {
                    Text(
                        text = if (uiState.currentQuestionIndex >= uiState.quizQuestions.size - 1) "Finalizar" else "Próxima Pergunta",
                        fontSize = optionFontSize,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(8.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun MemoryGame(
    uiState: MiniGamesUiState,
    onCardClick: (Int) -> Unit,
    onFinish: () -> Unit,
    onRestart: () -> Unit
) {
    BoxWithConstraints(
        modifier = Modifier.fillMaxSize()
    ) {
        val screenHeight = maxHeight
        val screenWidth = maxWidth
        
        // Ajustar padding e tamanhos com base no tamanho da tela
        val paddingValue = (screenHeight.value * 0.02f).coerceAtMost(12f).dp
        val titleFontSize = (screenWidth.value * 0.05f).coerceAtMost(24f).sp
        val subtitleFontSize = (screenWidth.value * 0.035f).coerceAtMost(16f).sp
        val bodyFontSize = (screenWidth.value * 0.03f).coerceAtMost(14f).sp
        
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = paddingValue),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Mostrar tela de conclusão quando o jogo for completado
            if (uiState.memoryGameCompleted) {
                GameCompletionScreen(
                    title = "Jogo da Memória Completo!",
                    message = "Você encontrou todos os pares em ${uiState.memoryMoves} movimentos!",
                    pointsEarned = uiState.earnedPoints,
                    onPlayAgain = onRestart,
                    onFinish = onFinish,
                    titleFontSize = titleFontSize,
                    subtitleFontSize = subtitleFontSize,
                    bodyFontSize = bodyFontSize,
                    screenWidth = screenWidth,
                    paddingValue = paddingValue
                )
                return@Column
            }
            
            // Cabeçalho do jogo
            Text(
                text = "Jogo da Memória",
                fontSize = titleFontSize,
                color = FuriaYellow,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(paddingValue))
            
            // Informações do jogo
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Pares: ${uiState.memoryMatchedPairs}/${uiState.memoryCards.size / 2}",
                    fontSize = subtitleFontSize,
                    color = FuriaWhite
                )
                
                Text(
                    text = "Movimentos: ${uiState.memoryMoves}",
                    fontSize = subtitleFontSize,
                    color = FuriaWhite
                )
            }
            
            Spacer(modifier = Modifier.height(paddingValue))
            
            // Grade de cartas
            val cardSize = (screenWidth / 4) - 8.dp
            
            LazyVerticalGrid(
                columns = GridCells.Fixed(4),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.weight(1f)
            ) {
                items(uiState.memoryCards.size) { index ->
                    val card = uiState.memoryCards[index]
                    MemoryCard(
                        card = card,
                        onClick = { onCardClick(index) },
                        size = cardSize,
                        enabled = !uiState.memoryIsChecking && !card.isMatched && !card.isFlipped
                    )
                }
            }
        }
    }
}

@Composable
fun MemoryCard(
    card: MemoryCard, 
    onClick: () -> Unit,
    size: Dp,
    enabled: Boolean,
    modifier: Modifier = Modifier
) {
    BoxWithConstraints(modifier = modifier) {
        val cardSize = size
        val fontSize = (cardSize.value * 0.3f).coerceAtMost(24f).sp
        
        Card(
            modifier = Modifier
                .size(cardSize)
                .aspectRatio(1f)
                .clickable(enabled = enabled) { onClick() },
            colors = CardDefaults.cardColors(
                containerColor = when {
                    card.isMatched -> Color.Green.copy(alpha = 0.3f)
                    card.isFlipped -> FuriaYellow
                    else -> FuriaBlack
                }
            ),
            border = BorderStroke(2.dp, when {
                card.isMatched -> Color.Green
                card.isFlipped -> FuriaYellow
                else -> FuriaWhite
            })
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                if (card.isFlipped || card.isMatched) {
                    Text(
                        text = card.content.name,
                        fontSize = fontSize,
                        color = FuriaBlack,
                        textAlign = TextAlign.Center,
                        maxLines = 1
                    )
                }
            }
        }
    }
}

@Composable
fun DailyChallenges(
    uiState: MiniGamesUiState,
    onChallengeComplete: (Challenge) -> Unit,
    onFinish: () -> Unit,
    snackbarHostState: SnackbarHostState
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    
    BoxWithConstraints(
        modifier = Modifier.fillMaxSize()
    ) {
        val screenHeight = maxHeight
        val screenWidth = maxWidth
        
        // Ajustar padding e tamanhos com base no tamanho da tela
        val paddingValue = (screenHeight.value * 0.02f).coerceAtMost(12f).dp
        val titleFontSize = (screenWidth.value * 0.05f).coerceAtMost(24f).sp
        val subtitleFontSize = (screenWidth.value * 0.04f).coerceAtMost(18f).sp
        val bodyFontSize = (screenWidth.value * 0.035f).coerceAtMost(16f).sp
        val smallFontSize = (screenWidth.value * 0.03f).coerceAtMost(14f).sp
        
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = paddingValue),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Desafios Diários",
                fontSize = titleFontSize,
                color = FuriaYellow,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height((paddingValue.value / 2).dp))
            
            Text(
                text = "Complete desafios para ganhar pontos extras!",
                fontSize = bodyFontSize,
                color = FuriaWhite,
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(paddingValue))
            
            // Verificar se há desafios para exibir
            if (uiState.dailyChallenges.isEmpty()) {
                // Mostrar uma mensagem em vez de um loader infinito
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp)
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        CircularProgressIndicator(color = FuriaYellow)
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        Text(
                            text = "Carregando desafios...",
                            fontSize = bodyFontSize,
                            color = FuriaWhite,
                            textAlign = TextAlign.Center
                        )
                        
                        // Botão para tentar novamente após 3 segundos
                        LaunchedEffect(Unit) {
                            delay(3000)
                            if (uiState.dailyChallenges.isEmpty()) {
                                // Se ainda estiver vazio após 3 segundos, mostrar botão para voltar
                                scope.launch {
                                    snackbarHostState.showSnackbar(
                                        message = "Não foi possível carregar os desafios",
                                        duration = SnackbarDuration.Short
                                    )
                                }
                            }
                        }
                    }
                }
            } else {
                // Exibir a lista de desafios
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                ) {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        contentPadding = PaddingValues(vertical = 4.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(uiState.dailyChallenges) { challenge ->
                            ChallengeItem(
                                challenge = challenge,
                                onComplete = { if (!challenge.completed) onChallengeComplete(challenge) },
                                fontSize = bodyFontSize,
                                smallFontSize = smallFontSize
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(paddingValue))
                
                Text(
                    text = "Os desafios são atualizados diariamente",
                    fontSize = smallFontSize,
                    color = FuriaWhite.copy(alpha = 0.7f),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
                
                Spacer(modifier = Modifier.height(paddingValue))
                
                Button(
                    onClick = onFinish,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = FuriaYellow,
                        contentColor = FuriaBlack
                    ),
                    modifier = Modifier.width((screenWidth.value * 0.7f).dp)
                ) {
                    Text(
                        "Voltar aos Mini-Jogos",
                        fontSize = bodyFontSize
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WordScrambleGame(
    uiState: MiniGamesUiState,
    onInputChange: (String) -> Unit,
    onSubmit: () -> Unit,
    onFinish: () -> Unit,
    onRestart: () -> Unit
) {
    val word = uiState.wordScrambleScrambledWord
    val userInput = uiState.wordScrambleUserInput
    val attempts = uiState.wordScrambleAttempts
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Cabeçalho do jogo
        Text(
            text = "Descubra a palavra",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = FuriaYellow
        )
        
        // Palavra embaralhada
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            colors = CardDefaults.cardColors(
                containerColor = FuriaBlack.copy(alpha = 0.7f)
            ),
            border = BorderStroke(2.dp, FuriaYellow)
        ) {
            Text(
                text = word,
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = FuriaWhite,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            )
        }
        
        // Contador de tentativas
        Text(
            text = "Tentativa ${attempts + 1} de 5",
            fontSize = 18.sp,
            color = FuriaWhite
        )
        
        // Campo de entrada
        OutlinedTextField(
            value = userInput,
            onValueChange = onInputChange,
            label = { Text("Sua resposta") },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = FuriaYellow,
                unfocusedBorderColor = FuriaYellow.copy(alpha = 0.5f),
                focusedLabelColor = FuriaYellow,
                unfocusedLabelColor = FuriaYellow.copy(alpha = 0.5f),
                cursorColor = FuriaYellow,
                focusedTextColor = FuriaWhite,
                unfocusedTextColor = FuriaWhite
            ),
            modifier = Modifier.fillMaxWidth()
        )
        
        // Botão de enviar
        Button(
            onClick = onSubmit,
            colors = ButtonDefaults.buttonColors(
                containerColor = FuriaYellow,
                contentColor = FuriaBlack
            ),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
        ) {
            Text("Verificar", fontSize = 18.sp)
        }
        
        Spacer(modifier = Modifier.weight(1f))
        
        // Botões de ação
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            TextButton(
                onClick = onFinish,
                colors = ButtonDefaults.textButtonColors(
                    contentColor = FuriaWhite
                )
            ) {
                Text("Voltar")
            }
            
            TextButton(
                onClick = onRestart,
                colors = ButtonDefaults.textButtonColors(
                    contentColor = FuriaYellow
                )
            ) {
                Text("Nova Palavra")
            }
        }
    }
}

// Componente reutilizável para tela de conclusão de jogo
@Composable
fun GameCompletionScreen(
    title: String,
    message: String,
    pointsEarned: Int,
    onPlayAgain: (() -> Unit)? = null,
    onFinish: () -> Unit,
    titleFontSize: TextUnit,
    subtitleFontSize: TextUnit,
    bodyFontSize: TextUnit,
    screenWidth: Dp,
    paddingValue: Dp
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = paddingValue),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Ícone de troféu
        Icon(
            imageVector = Icons.Default.EmojiEvents,
            contentDescription = null,
            tint = FuriaYellow,
            modifier = Modifier.size(64.dp)
        )
        
        Spacer(modifier = Modifier.height(paddingValue))
        
        // Título
        Text(
            text = title,
            fontSize = titleFontSize,
            color = FuriaYellow,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(paddingValue / 2))
        
        // Mensagem
        Text(
            text = message,
            fontSize = subtitleFontSize,
            color = FuriaWhite,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(paddingValue / 2))
        
        // Pontos ganhos
        Text(
            text = "Pontos ganhos: $pointsEarned",
            fontSize = subtitleFontSize,
            color = FuriaYellow,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(paddingValue * 1.5f))
        
        // Botões
        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (onPlayAgain != null) {
                Button(
                    onClick = onPlayAgain,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = FuriaBlack,
                        contentColor = FuriaWhite
                    ),
                    border = BorderStroke(1.dp, FuriaYellow)
                ) {
                    Icon(Icons.Default.Refresh, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "Jogar Novamente",
                        fontSize = bodyFontSize
                    )
                }
            }
            
            Button(
                onClick = onFinish,
                colors = ButtonDefaults.buttonColors(
                    containerColor = FuriaYellow,
                    contentColor = FuriaBlack
                )
            ) {
                Text(
                    "Voltar aos Mini-Jogos",
                    fontSize = bodyFontSize
                )
            }
        }
    }
}

@Composable
fun UserProfileHeader(
    userProfile: UserProfile,
    isTopOne: Boolean
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Avatar do usuário
        Box(
            modifier = Modifier
                .size(60.dp)
                .clip(CircleShape)
                .background(FuriaYellow),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = userProfile.nickname.first().toString().uppercase(),
                color = FuriaBlack,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )
        }
        
        Spacer(modifier = Modifier.width(16.dp))
        
        // Informações do usuário
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = userProfile.nickname,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = FuriaWhite
            )
            
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Star,
                    contentDescription = null,
                    tint = FuriaYellow,
                    modifier = Modifier.size(16.dp)
                )
                
                Spacer(modifier = Modifier.width(4.dp))
                
                Text(
                    text = "${userProfile.points} pontos",
                    fontSize = 14.sp,
                    color = FuriaYellow
                )
                
                if (isTopOne) {
                    Spacer(modifier = Modifier.width(8.dp))
                    
                    Surface(
                        color = FuriaYellow,
                        shape = RoundedCornerShape(4.dp),
                        modifier = Modifier.padding(horizontal = 4.dp)
                    ) {
                        Text(
                            text = "TOP 1",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = FuriaBlack,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ChallengeItem(
    challenge: Challenge,
    onComplete: () -> Unit,
    fontSize: TextUnit,
    smallFontSize: TextUnit
) {
    val context = LocalContext.current
    val intent = remember(challenge.actionData) {
        when (challenge.actionType) {
            ChallengeActionType.EXTERNAL_LINK -> Intent(Intent.ACTION_VIEW, Uri.parse(challenge.actionData))
            ChallengeActionType.INTERNAL_NAVIGATION -> null // Será implementado depois
            ChallengeActionType.INTERNAL_GAME -> null // Será implementado depois
        }
    }
    
    // Estado para mostrar feedback de conclusão
    var showCompletionFeedback by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (challenge.completed) Color.Green.copy(alpha = 0.2f) else Color.Transparent
        ),
        border = BorderStroke(1.dp, if (challenge.completed) Color.Green else FuriaWhite.copy(alpha = 0.3f)),
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = challenge.title,
                    fontSize = fontSize,
                    color = FuriaWhite,
                    fontWeight = FontWeight.Medium
                )
                
                Text(
                    text = challenge.description,
                    fontSize = smallFontSize,
                    color = FuriaWhite.copy(alpha = 0.7f)
                )
                
                // Mostrar feedback de conclusão
                if (showCompletionFeedback) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Desafio completado! +${challenge.points} pontos",
                        fontSize = smallFontSize,
                        color = Color.Green,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            
            Column(
                horizontalAlignment = Alignment.End
            ) {
                Text(
                    text = "+${challenge.points}",
                    fontSize = fontSize,
                    color = FuriaYellow,
                    fontWeight = FontWeight.Bold
                )
                
                if (!challenge.completed) {
                    Button(
                        onClick = {
                            // Primeiro, tenta abrir o link externo se for o caso
                            if (challenge.actionType == ChallengeActionType.EXTERNAL_LINK && intent != null) {
                                try {
                                    context.startActivity(intent)
                                } catch (e: Exception) {
                                    Log.e("ChallengeItem", "Erro ao abrir link: ${e.message}")
                                }
                            }
                            
                            // Mostrar feedback de conclusão
                            showCompletionFeedback = true
                            
                            // Depois, marca o desafio como completo
                            onComplete()
                            
                            // Esconder o feedback após alguns segundos
                            scope.launch {
                                delay(3000)
                                showCompletionFeedback = false
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = FuriaYellow,
                            contentColor = FuriaBlack
                        ),
                        modifier = Modifier.padding(top = 8.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "Completar",
                            fontSize = smallFontSize
                        )
                    }
                } else {
                    Text(
                        text = "Completado",
                        fontSize = smallFontSize,
                        color = Color.Green,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
            }
        }
    }
}
