package com.najmi.oreamnos.cardgen.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.TextUnitType
import androidx.compose.ui.unit.dp
import com.najmi.oreamnos.cardgen.model.CardData
import com.najmi.oreamnos.ui.components.NeoButton

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DataEditorSheet(
    inputText: String,
    onInputTextChange: (String) -> Unit,
    cardData: CardData?,
    onCardDataChange: ((CardData) -> CardData) -> Unit,
    isExtracting: Boolean,
    onExtractClick: () -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = false)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .padding(bottom = 24.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Source Text Input section
            SectionHeader("ARTICLE SOURCE TEXT")
            OutlinedTextField(
                value = inputText,
                onValueChange = onInputTextChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp),
                placeholder = { Text("Paste a football article here...") },
                maxLines = 6,
                shape = RoundedCornerShape(0.dp)
            )
            Spacer(Modifier.height(12.dp))
            NeoButton(
                text = "Extract Card Data",
                onClick = onExtractClick,
                isLoading = isExtracting,
                enabled = inputText.isNotBlank() && !isExtracting,
                modifier = Modifier.fillMaxWidth()
            )

            // Dynamic Data Editor section
            if (cardData != null) {
                Spacer(Modifier.height(32.dp))
                SectionHeader("EDIT CARD DATA")
                DynamicDataEditor(cardData = cardData, onCardDataChange = onCardDataChange)
            }
        }
    }
}

@Composable
private fun SectionHeader(title: String) {
    Text(
        text = title,
        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
        letterSpacing = TextUnit(2f, TextUnitType.Sp),
        modifier = Modifier.padding(bottom = 8.dp)
    )
}

@Composable
private fun DynamicDataEditor(
    cardData: CardData,
    onCardDataChange: ((CardData) -> CardData) -> Unit
) {
    when (cardData) {
        is CardData.HeadlineQuote -> {
            EditorField("Headline", cardData.headline) { v -> onCardDataChange { (it as CardData.HeadlineQuote).copy(headline = v) } }
            EditorField("Subtext", cardData.subtext) { v -> onCardDataChange { (it as CardData.HeadlineQuote).copy(subtext = v) } }
            EditorField("Quote Author", cardData.quoteAuthor) { v -> onCardDataChange { (it as CardData.HeadlineQuote).copy(quoteAuthor = v) } }
        }
        is CardData.PlayerSpotlight -> {
            EditorField("Player Name", cardData.playerName) { v -> onCardDataChange { (it as CardData.PlayerSpotlight).copy(playerName = v) } }
            EditorField("Club", cardData.club) { v -> onCardDataChange { (it as CardData.PlayerSpotlight).copy(club = v) } }
            EditorField("Position", cardData.position) { v -> onCardDataChange { (it as CardData.PlayerSpotlight).copy(position = v) } }
            EditorField("Rating", cardData.rating.toString(), isNumber = true) { v -> onCardDataChange { (it as CardData.PlayerSpotlight).copy(rating = v.toFloatOrNull() ?: 0f) } }
            EditorField("Goals", cardData.goals.toString(), isNumber = true) { v -> onCardDataChange { (it as CardData.PlayerSpotlight).copy(goals = v.toIntOrNull() ?: 0) } }
            EditorField("Assists", cardData.assists.toString(), isNumber = true) { v -> onCardDataChange { (it as CardData.PlayerSpotlight).copy(assists = v.toIntOrNull() ?: 0) } }
            EditorField("Minutes", cardData.minutesPlayed.toString(), isNumber = true) { v -> onCardDataChange { (it as CardData.PlayerSpotlight).copy(minutesPlayed = v.toIntOrNull() ?: 0) } }
            EditorField("Key Action", cardData.keyAction) { v -> onCardDataChange { (it as CardData.PlayerSpotlight).copy(keyAction = v) } }
            EditorField("Key Quote", cardData.keyQuote) { v -> onCardDataChange { (it as CardData.PlayerSpotlight).copy(keyQuote = v) } }
        }
        is CardData.DetailedScoreboard -> {
            EditorField("Home Team", cardData.homeTeam) { v -> onCardDataChange { (it as CardData.DetailedScoreboard).copy(homeTeam = v) } }
            EditorField("Away Team", cardData.awayTeam) { v -> onCardDataChange { (it as CardData.DetailedScoreboard).copy(awayTeam = v) } }
            EditorField("Home Score", cardData.homeScore.toString(), isNumber = true) { v -> onCardDataChange { (it as CardData.DetailedScoreboard).copy(homeScore = v.toIntOrNull() ?: 0) } }
            EditorField("Away Score", cardData.awayScore.toString(), isNumber = true) { v -> onCardDataChange { (it as CardData.DetailedScoreboard).copy(awayScore = v.toIntOrNull() ?: 0) } }
            EditorField("Home Scorers", cardData.homeScorers) { v -> onCardDataChange { (it as CardData.DetailedScoreboard).copy(homeScorers = v) } }
            EditorField("Away Scorers", cardData.awayScorers) { v -> onCardDataChange { (it as CardData.DetailedScoreboard).copy(awayScorers = v) } }
            EditorField("Possession", cardData.possession) { v -> onCardDataChange { (it as CardData.DetailedScoreboard).copy(possession = v) } }
            EditorField("Shots On Target", cardData.shotsOnTarget) { v -> onCardDataChange { (it as CardData.DetailedScoreboard).copy(shotsOnTarget = v) } }
            EditorField("Competition", cardData.competition) { v -> onCardDataChange { (it as CardData.DetailedScoreboard).copy(competition = v) } }
            EditorField("Match Status", cardData.matchStatus) { v -> onCardDataChange { (it as CardData.DetailedScoreboard).copy(matchStatus = v) } }
        }
        is CardData.TransferNews -> {
            EditorField("Player Name", cardData.playerName) { v -> onCardDataChange { (it as CardData.TransferNews).copy(playerName = v) } }
            EditorField("Action (Label)", cardData.action) { v -> onCardDataChange { (it as CardData.TransferNews).copy(action = v) } }
            EditorField("From Team", cardData.fromTeam) { v -> onCardDataChange { (it as CardData.TransferNews).copy(fromTeam = v) } }
            EditorField("To Team", cardData.toTeam) { v -> onCardDataChange { (it as CardData.TransferNews).copy(toTeam = v) } }
            EditorField("Fee", cardData.fee) { v -> onCardDataChange { (it as CardData.TransferNews).copy(fee = v) } }
            EditorField("Contract Length", cardData.contractLength) { v -> onCardDataChange { (it as CardData.TransferNews).copy(contractLength = v) } }
            EditorField("Transfer Type", cardData.transferType) { v -> onCardDataChange { (it as CardData.TransferNews).copy(transferType = v) } }
            EditorField("Quote", cardData.quote) { v -> onCardDataChange { (it as CardData.TransferNews).copy(quote = v) } }
        }
        is CardData.BreakingNews -> {
            EditorField("Label", cardData.label) { v -> onCardDataChange { (it as CardData.BreakingNews).copy(label = v) } }
            EditorField("Headline", cardData.headline) { v -> onCardDataChange { (it as CardData.BreakingNews).copy(headline = v) } }
            EditorField("Subtext", cardData.subtext) { v -> onCardDataChange { (it as CardData.BreakingNews).copy(subtext = v) } }
            EditorField("Impact (1-5)", cardData.impactRating.toString(), isNumber = true) { v -> onCardDataChange { (it as CardData.BreakingNews).copy(impactRating = v.toIntOrNull() ?: 1) } }
            EditorField("Related Teams", cardData.relatedTeams) { v -> onCardDataChange { (it as CardData.BreakingNews).copy(relatedTeams = v) } }
        }
        is CardData.MatchPreview -> {
            EditorField("Competition", cardData.competition) { v -> onCardDataChange { (it as CardData.MatchPreview).copy(competition = v) } }
            EditorField("Home Team", cardData.homeTeam) { v -> onCardDataChange { (it as CardData.MatchPreview).copy(homeTeam = v) } }
            EditorField("Away Team", cardData.awayTeam) { v -> onCardDataChange { (it as CardData.MatchPreview).copy(awayTeam = v) } }
            EditorField("Home Form", cardData.homeForm) { v -> onCardDataChange { (it as CardData.MatchPreview).copy(homeForm = v) } }
            EditorField("Away Form", cardData.awayForm) { v -> onCardDataChange { (it as CardData.MatchPreview).copy(awayForm = v) } }
            EditorField("Time", cardData.matchTime) { v -> onCardDataChange { (it as CardData.MatchPreview).copy(matchTime = v) } }
            EditorField("Stadium", cardData.stadium) { v -> onCardDataChange { (it as CardData.MatchPreview).copy(stadium = v) } }
        }
        is CardData.TopStats -> {
            EditorField("Match Context", cardData.matchContext) { v -> onCardDataChange { (it as CardData.TopStats).copy(matchContext = v) } }
            cardData.stats.forEachIndexed { index, stat ->
                EditorField("Stat ${index+1} Label", stat.label) { v ->
                    onCardDataChange {
                        val current = it as CardData.TopStats
                        val newStats = current.stats.toMutableList().apply { this[index] = stat.copy(label = v) }
                        current.copy(stats = newStats)
                    }
                }
                EditorField("Stat ${index+1} Value", stat.value) { v ->
                    onCardDataChange {
                        val current = it as CardData.TopStats
                        val newStats = current.stats.toMutableList().apply { this[index] = stat.copy(value = v) }
                        current.copy(stats = newStats)
                    }
                }
            }
        }
        is CardData.OnThisDay -> {
            EditorField("Date Label", cardData.dateLabel) { v -> onCardDataChange { (it as CardData.OnThisDay).copy(dateLabel = v) } }
            EditorField("Years Ago", cardData.yearsAgo.toString(), isNumber = true) { v -> onCardDataChange { (it as CardData.OnThisDay).copy(yearsAgo = v.toIntOrNull() ?: 0) } }
            EditorField("Competition", cardData.competition) { v -> onCardDataChange { (it as CardData.OnThisDay).copy(competition = v) } }
            EditorField("Headline", cardData.headline) { v -> onCardDataChange { (it as CardData.OnThisDay).copy(headline = v) } }
        }
        is CardData.StartingXI -> {
            EditorField("Team Name", cardData.teamName) { v -> onCardDataChange { (it as CardData.StartingXI).copy(teamName = v) } }
            EditorField("Formation", cardData.formation) { v -> onCardDataChange { (it as CardData.StartingXI).copy(formation = v) } }
            EditorField("Manager", cardData.manager) { v -> onCardDataChange { (it as CardData.StartingXI).copy(manager = v) } }
            EditorField("Absent/Injured", cardData.keyAbsences) { v -> onCardDataChange { (it as CardData.StartingXI).copy(keyAbsences = v) } }
        }
    }
}

@Composable
private fun EditorField(
    label: String,
    value: String,
    isNumber: Boolean = false,
    onValueChange: (String) -> Unit
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label, style = MaterialTheme.typography.labelSmall) },
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp),
        singleLine = !isNumber, // Allow multi-line for text fields commonly used for lists/quotes
        maxLines = 3,
        shape = RoundedCornerShape(4.dp)
    )
}
