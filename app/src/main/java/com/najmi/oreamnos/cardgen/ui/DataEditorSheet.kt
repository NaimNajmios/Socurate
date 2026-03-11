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
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.Alignment
import com.najmi.oreamnos.cardgen.model.CardData
import com.najmi.oreamnos.ui.components.NeoButton
import com.najmi.oreamnos.ui.components.NeoInput
import com.najmi.oreamnos.ui.components.NeoOutlinedButton

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DataEditorSheet(
    inputText: String,
    onInputTextChange: (String) -> Unit,
    cardData: CardData?,
    onCardDataChange: ((CardData) -> CardData) -> Unit,
    rewritingFields: Set<String>,
    onRewriteField: (String, String, (String) -> Unit) -> Unit,
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
            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "ARTICLE SOURCE TEXT",
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    letterSpacing = TextUnit(2f, TextUnitType.Sp)
                )
                
                val clipboardManager = LocalClipboardManager.current
                NeoOutlinedButton(
                    text = "PASTE",
                    onClick = {
                        clipboardManager.getText()?.text?.let {
                            onInputTextChange(it)
                        }
                    },
                    modifier = Modifier.height(36.dp)
                )
            }
            NeoInput(
                value = inputText,
                onValueChange = onInputTextChange,
                modifier = Modifier
                    .fillMaxWidth(),
                placeholder = "Paste a football article here...",
                minLines = 6,
                maxLines = 10
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
                DynamicDataEditor(
                    cardData = cardData, 
                    onCardDataChange = onCardDataChange,
                    rewritingFields = rewritingFields,
                    onRewriteField = onRewriteField
                )
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
    onCardDataChange: ((CardData) -> CardData) -> Unit,
    rewritingFields: Set<String>,
    onRewriteField: (String, String, (String) -> Unit) -> Unit
) {
    // Helper to conditionally inject magic button for certain fields
    @Composable
    fun MagicEditorField(label: String, value: String, isNumber: Boolean = false, hasMagic: Boolean = false, onValueChange: (String) -> Unit) {
        val magicClick: (() -> Unit)? = if (hasMagic) {
            { onRewriteField(label, value, onValueChange) }
        } else null
        
        EditorField(
            label = label,
            value = value,
            isNumber = isNumber,
            isRewriting = rewritingFields.contains(label),
            onMagicClick = magicClick,
            onValueChange = onValueChange
        )
    }

    when (cardData) {
        is CardData.HeadlineQuote -> {
            MagicEditorField("Headline", cardData.headline, hasMagic = true) { v -> onCardDataChange { (it as CardData.HeadlineQuote).copy(headline = v) } }
            MagicEditorField("Subtext", cardData.subtext, hasMagic = true) { v -> onCardDataChange { (it as CardData.HeadlineQuote).copy(subtext = v) } }
            MagicEditorField("Quote Author", cardData.quoteAuthor) { v -> onCardDataChange { (it as CardData.HeadlineQuote).copy(quoteAuthor = v) } }
        }
        is CardData.PlayerSpotlight -> {
            MagicEditorField("Player Name", cardData.playerName) { v -> onCardDataChange { (it as CardData.PlayerSpotlight).copy(playerName = v) } }
            MagicEditorField("Club", cardData.club) { v -> onCardDataChange { (it as CardData.PlayerSpotlight).copy(club = v) } }
            MagicEditorField("Position", cardData.position) { v -> onCardDataChange { (it as CardData.PlayerSpotlight).copy(position = v) } }
            MagicEditorField("Rating", cardData.rating.toString(), isNumber = true) { v -> onCardDataChange { (it as CardData.PlayerSpotlight).copy(rating = v.toFloatOrNull() ?: 0f) } }
            MagicEditorField("Goals", cardData.goals.toString(), isNumber = true) { v -> onCardDataChange { (it as CardData.PlayerSpotlight).copy(goals = v.toIntOrNull() ?: 0) } }
            MagicEditorField("Assists", cardData.assists.toString(), isNumber = true) { v -> onCardDataChange { (it as CardData.PlayerSpotlight).copy(assists = v.toIntOrNull() ?: 0) } }
            MagicEditorField("Minutes", cardData.minutesPlayed.toString(), isNumber = true) { v -> onCardDataChange { (it as CardData.PlayerSpotlight).copy(minutesPlayed = v.toIntOrNull() ?: 0) } }
            MagicEditorField("Key Action", cardData.keyAction, hasMagic = true) { v -> onCardDataChange { (it as CardData.PlayerSpotlight).copy(keyAction = v) } }
            MagicEditorField("Key Quote", cardData.keyQuote, hasMagic = true) { v -> onCardDataChange { (it as CardData.PlayerSpotlight).copy(keyQuote = v) } }
        }
        is CardData.DetailedScoreboard -> {
            MagicEditorField("Home Team", cardData.homeTeam) { v -> onCardDataChange { (it as CardData.DetailedScoreboard).copy(homeTeam = v) } }
            MagicEditorField("Away Team", cardData.awayTeam) { v -> onCardDataChange { (it as CardData.DetailedScoreboard).copy(awayTeam = v) } }
            MagicEditorField("Home Score", cardData.homeScore.toString(), isNumber = true) { v -> onCardDataChange { (it as CardData.DetailedScoreboard).copy(homeScore = v.toIntOrNull() ?: 0) } }
            MagicEditorField("Away Score", cardData.awayScore.toString(), isNumber = true) { v -> onCardDataChange { (it as CardData.DetailedScoreboard).copy(awayScore = v.toIntOrNull() ?: 0) } }
            MagicEditorField("Home Scorers", cardData.homeScorers) { v -> onCardDataChange { (it as CardData.DetailedScoreboard).copy(homeScorers = v) } }
            MagicEditorField("Away Scorers", cardData.awayScorers) { v -> onCardDataChange { (it as CardData.DetailedScoreboard).copy(awayScorers = v) } }
            MagicEditorField("Possession", cardData.possession) { v -> onCardDataChange { (it as CardData.DetailedScoreboard).copy(possession = v) } }
            MagicEditorField("Shots On Target", cardData.shotsOnTarget) { v -> onCardDataChange { (it as CardData.DetailedScoreboard).copy(shotsOnTarget = v) } }
            MagicEditorField("Competition", cardData.competition) { v -> onCardDataChange { (it as CardData.DetailedScoreboard).copy(competition = v) } }
            MagicEditorField("Match Status", cardData.matchStatus) { v -> onCardDataChange { (it as CardData.DetailedScoreboard).copy(matchStatus = v) } }
        }
        is CardData.TransferNews -> {
            MagicEditorField("Player Name", cardData.playerName) { v -> onCardDataChange { (it as CardData.TransferNews).copy(playerName = v) } }
            MagicEditorField("Action (Label)", cardData.action) { v -> onCardDataChange { (it as CardData.TransferNews).copy(action = v) } }
            MagicEditorField("From Team", cardData.fromTeam) { v -> onCardDataChange { (it as CardData.TransferNews).copy(fromTeam = v) } }
            MagicEditorField("To Team", cardData.toTeam) { v -> onCardDataChange { (it as CardData.TransferNews).copy(toTeam = v) } }
            MagicEditorField("Fee", cardData.fee) { v -> onCardDataChange { (it as CardData.TransferNews).copy(fee = v) } }
            MagicEditorField("Contract Length", cardData.contractLength) { v -> onCardDataChange { (it as CardData.TransferNews).copy(contractLength = v) } }
            MagicEditorField("Transfer Type", cardData.transferType) { v -> onCardDataChange { (it as CardData.TransferNews).copy(transferType = v) } }
            MagicEditorField("Quote", cardData.quote, hasMagic = true) { v -> onCardDataChange { (it as CardData.TransferNews).copy(quote = v) } }
        }
        is CardData.BreakingNews -> {
            MagicEditorField("Label", cardData.label) { v -> onCardDataChange { (it as CardData.BreakingNews).copy(label = v) } }
            MagicEditorField("Headline", cardData.headline, hasMagic = true) { v -> onCardDataChange { (it as CardData.BreakingNews).copy(headline = v) } }
            MagicEditorField("Subtext", cardData.subtext, hasMagic = true) { v -> onCardDataChange { (it as CardData.BreakingNews).copy(subtext = v) } }
            MagicEditorField("Impact (1-5)", cardData.impactRating.toString(), isNumber = true) { v -> onCardDataChange { (it as CardData.BreakingNews).copy(impactRating = v.toIntOrNull() ?: 1) } }
            MagicEditorField("Related Teams", cardData.relatedTeams) { v -> onCardDataChange { (it as CardData.BreakingNews).copy(relatedTeams = v) } }
        }
        is CardData.MatchPreview -> {
            MagicEditorField("Competition", cardData.competition) { v -> onCardDataChange { (it as CardData.MatchPreview).copy(competition = v) } }
            MagicEditorField("Home Team", cardData.homeTeam) { v -> onCardDataChange { (it as CardData.MatchPreview).copy(homeTeam = v) } }
            MagicEditorField("Away Team", cardData.awayTeam) { v -> onCardDataChange { (it as CardData.MatchPreview).copy(awayTeam = v) } }
            MagicEditorField("Home Form", cardData.homeForm) { v -> onCardDataChange { (it as CardData.MatchPreview).copy(homeForm = v) } }
            MagicEditorField("Away Form", cardData.awayForm) { v -> onCardDataChange { (it as CardData.MatchPreview).copy(awayForm = v) } }
            MagicEditorField("Time", cardData.matchTime) { v -> onCardDataChange { (it as CardData.MatchPreview).copy(matchTime = v) } }
            MagicEditorField("Stadium", cardData.stadium) { v -> onCardDataChange { (it as CardData.MatchPreview).copy(stadium = v) } }
        }
        is CardData.TopStats -> {
            MagicEditorField("Match Context", cardData.matchContext) { v -> onCardDataChange { (it as CardData.TopStats).copy(matchContext = v) } }
            cardData.stats.forEachIndexed { index, stat ->
                MagicEditorField("Stat ${index+1} Label", stat.label) { v ->
                    onCardDataChange {
                        val current = it as CardData.TopStats
                        val newStats = current.stats.toMutableList().apply { this[index] = stat.copy(label = v) }
                        current.copy(stats = newStats)
                    }
                }
                MagicEditorField("Stat ${index+1} Value", stat.value, hasMagic = true) { v ->
                    onCardDataChange {
                        val current = it as CardData.TopStats
                        val newStats = current.stats.toMutableList().apply { this[index] = stat.copy(value = v) }
                        current.copy(stats = newStats)
                    }
                }
            }
        }
        is CardData.OnThisDay -> {
            MagicEditorField("Date Label", cardData.dateLabel) { v -> onCardDataChange { (it as CardData.OnThisDay).copy(dateLabel = v) } }
            MagicEditorField("Years Ago", cardData.yearsAgo.toString(), isNumber = true) { v -> onCardDataChange { (it as CardData.OnThisDay).copy(yearsAgo = v.toIntOrNull() ?: 0) } }
            MagicEditorField("Competition", cardData.competition) { v -> onCardDataChange { (it as CardData.OnThisDay).copy(competition = v) } }
            MagicEditorField("Headline", cardData.headline, hasMagic = true) { v -> onCardDataChange { (it as CardData.OnThisDay).copy(headline = v) } }
        }
        is CardData.StartingXI -> {
            MagicEditorField("Team Name", cardData.teamName) { v -> onCardDataChange { (it as CardData.StartingXI).copy(teamName = v) } }
            MagicEditorField("Formation", cardData.formation) { v -> onCardDataChange { (it as CardData.StartingXI).copy(formation = v) } }
            MagicEditorField("Manager", cardData.manager) { v -> onCardDataChange { (it as CardData.StartingXI).copy(manager = v) } }
            MagicEditorField("Absent/Injured", cardData.keyAbsences) { v -> onCardDataChange { (it as CardData.StartingXI).copy(keyAbsences = v) } }
        }
        is CardData.MatchStatsComparison -> {
            MagicEditorField("Home Team", cardData.homeTeam) { v -> onCardDataChange { (it as CardData.MatchStatsComparison).copy(homeTeam = v) } }
            MagicEditorField("Away Team", cardData.awayTeam) { v -> onCardDataChange { (it as CardData.MatchStatsComparison).copy(awayTeam = v) } }
            cardData.stats.forEachIndexed { index, stat ->
                MagicEditorField("Stat ${index+1} Label", stat.label) { v ->
                    onCardDataChange {
                        val current = it as CardData.MatchStatsComparison
                        val newStats = current.stats.toMutableList().apply { this[index] = stat.copy(label = v) }
                        current.copy(stats = newStats)
                    }
                }
                MagicEditorField("Stat ${index+1} Home Value", stat.homeValue) { v ->
                    onCardDataChange {
                        val current = it as CardData.MatchStatsComparison
                        val newStats = current.stats.toMutableList().apply { this[index] = stat.copy(homeValue = v) }
                        current.copy(stats = newStats)
                    }
                }
                MagicEditorField("Stat ${index+1} Away Value", stat.awayValue) { v ->
                    onCardDataChange {
                        val current = it as CardData.MatchStatsComparison
                        val newStats = current.stats.toMutableList().apply { this[index] = stat.copy(awayValue = v) }
                        current.copy(stats = newStats)
                    }
                }
            }
        }
        is CardData.SocialPost -> {
            MagicEditorField("Handle", cardData.handle) { v -> onCardDataChange { (it as CardData.SocialPost).copy(handle = v) } }
            MagicEditorField("Name", cardData.name) { v -> onCardDataChange { (it as CardData.SocialPost).copy(name = v) } }
            MagicEditorField("Content", cardData.content, hasMagic = true) { v -> onCardDataChange { (it as CardData.SocialPost).copy(content = v) } }
            MagicEditorField("Timestamp", cardData.timestamp) { v -> onCardDataChange { (it as CardData.SocialPost).copy(timestamp = v) } }
            MagicEditorField("Metrics", cardData.metrics) { v -> onCardDataChange { (it as CardData.SocialPost).copy(metrics = v) } }
        }
    }
}

@Composable
private fun EditorField(
    label: String,
    value: String,
    isNumber: Boolean = false,
    isRewriting: Boolean = false,
    onMagicClick: (() -> Unit)? = null,
    onValueChange: (String) -> Unit
) {
    NeoInput(
        value = value,
        onValueChange = onValueChange,
        label = label,
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp),
        minLines = if (!isNumber && (value.length >= 50 || onMagicClick != null)) 2 else 1,
        maxLines = 4,
        trailingIcon = {
            if (isRewriting) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    strokeWidth = 2.dp,
                    color = MaterialTheme.colorScheme.primary
                )
            } else if (onMagicClick != null) {
                // Neo-styled AI Rewrite button (0dp corners, 2dp border via NeoOutlinedButton)
                NeoOutlinedButton(
                    onClick = onMagicClick,
                    text = "✨",
                    modifier = Modifier
                        .size(width = 44.dp, height = 36.dp)
                        .padding(end = 4.dp),
                    contentColor = MaterialTheme.colorScheme.primary
                )
            }
        }
    )
}
