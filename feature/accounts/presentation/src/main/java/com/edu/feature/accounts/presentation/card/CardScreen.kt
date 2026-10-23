package com.edu.feature.accounts.presentation.card

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CreditCard
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.edu.core.presentation.designsystem.ClearSpendTheme
import com.edu.core.presentation.designsystem.Error
import com.edu.core.presentation.designsystem.OnNavy
import com.edu.core.presentation.designsystem.OnSurface
import com.edu.core.presentation.designsystem.OnSurfaceVariant
import com.edu.core.presentation.designsystem.Outline
import com.edu.core.presentation.designsystem.RobotoMono
import com.edu.core.presentation.designsystem.Success
import com.edu.core.presentation.designsystem.Surface
import com.edu.core.presentation.designsystem.SurfaceVariant
import com.edu.core.presentation.ui.EmptyStateScreen
import com.edu.core.presentation.ui.ErrorScreen
import com.edu.core.presentation.ui.ObserveAsEvent
import com.edu.core.presentation.ui.OfflineBanner
import com.edu.core.presentation.ui.SkeletonBlock
import com.edu.core.presentation.ui.model.CardSummaryUi
import com.edu.feature.accounts.presentation.R
import com.edu.feature.accounts.presentation.components.BackHeader
import com.edu.feature.accounts.presentation.components.UtilisationBar
import kotlinx.coroutines.delay
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import org.koin.compose.viewmodel.koinViewModel
import kotlin.math.roundToLong

@Composable
fun CardScreen(
    onNavigateBack: () -> Unit,
    onNavigateToConnectBank: () -> Unit,
    viewModel: CardViewModel = koinViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    ObserveAsEvent(viewModel.effect) { effect ->
        when (effect) {
            CardEffect.NavigateBack -> onNavigateBack()
            CardEffect.NavigateToConnectBank -> onNavigateToConnectBank()
        }
    }
    CardContent(state = state, onAction = viewModel::onAction)
}

@Composable
fun CardContent(
    state: CardUiState,
    onAction: (CardAction) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Surface)
            .statusBarsPadding(),
    ) {
        BackHeader(
            title = stringResource(R.string.card_title),
            onBack = { onAction(CardAction.BackClicked) },
            modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 16.dp, bottom = 20.dp),
        )
        val card = state.card
        when {
            state.isLoading && card == null -> CardSkeleton()
            state.hasError -> ErrorScreen(onRetry = { onAction(CardAction.RetryClicked) })
            state.hasNoCard -> EmptyStateScreen(
                title = stringResource(R.string.card_empty_title),
                subtitle = stringResource(R.string.card_empty_body),
                icon = Icons.Outlined.CreditCard,
            )
            card != null -> CardBody(card, state.offlineSyncTime)
            else -> CardSkeleton()
        }
    }
}

@Composable
private fun CardBody(card: CardSummaryUi, offlineSyncTime: String?) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(start = 20.dp, end = 20.dp, bottom = 24.dp)
            .testTag(CardTestTags.CONTENT),
    ) {
        offlineSyncTime?.let { OfflineBanner(it, Modifier.padding(bottom = 12.dp)) }
        ShutterCard(card)
        Spacer(Modifier.height(20.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            BalanceTile(stringResource(R.string.card_available), card.formattedAvailable ?: "—", Success, Modifier.weight(1f))
            BalanceTile(stringResource(R.string.card_current), card.formattedCurrent ?: "—", OnSurface, Modifier.weight(1f))
        }
        val utilisationLabel = card.utilisationLabel
        if (utilisationLabel != null) {
            Spacer(Modifier.height(20.dp))
            Column(
                Modifier
                    .fillMaxWidth()
                    .background(SurfaceVariant, RoundedCornerShape(18.dp))
                    .padding(18.dp),
            ) {
                Row {
                    Text(stringResource(R.string.card_utilisation), color = OnSurface, fontSize = 13.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                    Text(utilisationLabel, color = Color(card.utilisationColorArgb), fontSize = 13.sp, fontWeight = FontWeight.Bold)
                }
                Spacer(Modifier.height(10.dp))
                UtilisationBar(card.utilisationFraction, Color(card.utilisationColorArgb), height = 8)
                Spacer(Modifier.height(8.dp))
                Text(
                    stringResource(R.string.card_of_limit, card.formattedCurrent.orEmpty(), card.formattedCreditLimit.orEmpty()),
                    color = OnSurfaceVariant,
                    fontSize = 12.sp,
                )
            }
        }
        if (card.hasPaymentDue) {
            Spacer(Modifier.height(16.dp))
            Row(
                Modifier
                    .fillMaxWidth()
                    .background(Error.copy(alpha = 0.07f), RoundedCornerShape(18.dp))
                    .border(1.dp, Error.copy(alpha = 0.3f), RoundedCornerShape(18.dp))
                    .padding(18.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(Icons.Outlined.ErrorOutline, contentDescription = null, tint = Error, modifier = Modifier.size(22.dp))
                Spacer(Modifier.width(14.dp))
                Column {
                    Text(
                        stringResource(R.string.card_payment_due, card.formattedPaymentDue!!, card.formattedPaymentDueDate!!),
                        color = Error,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                    )
                    card.formattedLastStatementBalance?.let {
                        Text(stringResource(R.string.card_last_statement, it), color = OnSurfaceVariant, fontSize = 11.5.sp)
                    }
                }
            }
        }
    }
}

@Composable
private fun BalanceTile(label: String, value: String, valueColor: Color, modifier: Modifier) {
    Column(modifier.background(SurfaceVariant, RoundedCornerShape(16.dp)).padding(14.dp)) {
        Text(label, color = OnSurfaceVariant, fontSize = 11.sp)
        Spacer(Modifier.height(4.dp))
        Text(value, color = valueColor, fontSize = 17.sp, fontWeight = FontWeight.Bold)
    }
}

// --- Card visual + shutter animation ------------------------------------------------------

/** ClearSpend.dc.html `CARD_THEMES`: the card swaps between these on each tap. */
private val CardThemes = listOf(
    Color(0xFF1B2A47) to Color(0xFF0F1D38),
    Color(0xFF0E3A34) to Color(0xFF05140F),
)

private const val STRIP_COUNT = 8
private const val STRIP_ANIM_MS = 650
private const val STRIP_STAGGER_MS = 35
private const val TOTAL_MS = STRIP_ANIM_MS + (STRIP_COUNT - 1) * STRIP_STAGGER_MS

/**
 * `cs-blind-flip` keyframes: 0 % → 0°, 48 % → 90°, 52 % → 90°, 100 % → 0°,
 * eased within each segment like the CSS `ease-in-out`.
 */
private fun blindFlipRotation(progress: Float): Float {
    val ease = FastOutSlowInEasing
    return when {
        progress <= 0.48f -> 90f * ease.transform(progress / 0.48f)
        progress <= 0.52f -> 90f
        else -> 90f * (1f - ease.transform((progress - 0.52f) / 0.48f))
    }
}

/**
 * The card face is drawn as 8 vertical strips. On tap each strip flips about
 * its top (even) or bottom (odd) edge with `graphicsLayer { rotationX }`,
 * staggered 35 ms; the colour theme swaps at 46 % of the whole run, while
 * the strips are edge-on, so the change is hidden (ClearSpend.dc.html `tapCard`).
 * Purely visual, so this state lives in the Composable, not the UiState.
 */
@Composable
private fun ShutterCard(card: CardSummaryUi) {
    var themeIndex by rememberSaveable { mutableIntStateOf(0) }
    var animating by remember { mutableStateOf(false) }
    val strips = remember { List(STRIP_COUNT) { Animatable(0f) } }
    val scope = rememberCoroutineScope()
    val (startColor, endColor) = CardThemes[themeIndex]
    val tapLabel = stringResource(R.string.card_tap_hint)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .border(1.dp, Outline, RoundedCornerShape(20.dp))
            .clickable(role = Role.Button, onClickLabel = tapLabel) {
                if (animating) return@clickable
                animating = true
                scope.launch {
                    val jobs = strips.mapIndexed { index, strip ->
                        launch {
                            strip.snapTo(0f)
                            strip.animateTo(1f, tween(STRIP_ANIM_MS, delayMillis = index * STRIP_STAGGER_MS, easing = LinearEasing))
                            strip.snapTo(0f)
                        }
                    }
                    delay((TOTAL_MS * 0.46f).roundToLong())
                    themeIndex = 1 - themeIndex
                    jobs.joinAll()
                    animating = false
                }
            }
            .testTag(CardTestTags.CARD_FACE),
    ) {
        Row(Modifier.matchParentSize()) {
            strips.forEachIndexed { index, strip ->
                Box(
                    Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .graphicsLayer {
                            rotationX = blindFlipRotation(strip.value)
                            transformOrigin = TransformOrigin(0.5f, if (index % 2 == 0) 0f else 1f)
                            cameraDistance = 12f * density
                        }
                        .drawBehind {
                            // Each strip paints its slice of one card-wide 135° gradient.
                            val cardWidth = size.width * STRIP_COUNT
                            val x = -index * size.width
                            drawRect(
                                Brush.linearGradient(
                                    listOf(startColor, endColor),
                                    start = Offset(x, 0f),
                                    end = Offset(x + cardWidth, size.height),
                                ),
                            )
                        },
                )
            }
        }
        Column(Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.Top) {
                Box(
                    Modifier
                        .size(width = 38.dp, height = 28.dp)
                        .background(Brush.linearGradient(listOf(Color(0xFFF3D477), Color(0xFFD6AE4B))), RoundedCornerShape(6.dp)),
                )
                Spacer(Modifier.weight(1f))
                Text(card.network, color = OnNavy.copy(alpha = 0.85f), fontSize = 13.sp, fontWeight = FontWeight.Bold, letterSpacing = 0.5.sp)
            }
            Spacer(Modifier.height(36.dp))
            Text(card.cardFaceNumber, color = OnNavy, fontSize = 18.sp, fontWeight = FontWeight.SemiBold, fontFamily = RobotoMono, letterSpacing = 2.sp)
            Spacer(Modifier.height(14.dp))
            Row(verticalAlignment = Alignment.Bottom) {
                Column(Modifier.weight(1f)) {
                    Text(stringResource(R.string.card_cardholder), color = OnNavy.copy(alpha = 0.5f), fontSize = 10.sp)
                    Spacer(Modifier.height(2.dp))
                    Text(card.nameOnCard, color = OnNavy, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                }
                Text(card.displayName, color = OnNavy.copy(alpha = 0.65f), fontSize = 12.sp)
            }
        }
    }
}

@Composable
private fun CardSkeleton() {
    Column(Modifier.padding(horizontal = 20.dp).testTag(CardTestTags.SKELETON), verticalArrangement = Arrangement.spacedBy(20.dp)) {
        SkeletonBlock(Modifier.fillMaxWidth().height(190.dp), RoundedCornerShape(20.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            SkeletonBlock(Modifier.weight(1f).height(70.dp), RoundedCornerShape(16.dp))
            SkeletonBlock(Modifier.weight(1f).height(70.dp), RoundedCornerShape(16.dp))
        }
        SkeletonBlock(Modifier.fillMaxWidth().height(100.dp), RoundedCornerShape(18.dp))
    }
}

object CardTestTags {
    const val SKELETON = "card_skeleton"
    const val CONTENT = "card_content"
    const val CARD_FACE = "card_face"
}

private val previewCard = CardSummaryUi(
    "c", "Credit card", "•••• 9999", "••••  ••••  ••••  9999", "VISA", "A N Other",
    "£900.00", "£100.00", "£1,000.00", "£120.00", "£25.00", "1 Feb 2029", "10%", 0.1f, 0xFF00C853.toInt(),
)

@Preview(widthDp = 412, heightDp = 892)
@Composable
private fun CardPreview() {
    ClearSpendTheme(darkTheme = false) { CardContent(CardUiState(card = previewCard), onAction = {}) }
}

@Preview(widthDp = 412, heightDp = 892)
@Composable
private fun CardEmptyPreview() {
    ClearSpendTheme(darkTheme = false) { CardContent(CardUiState(hasNoCard = true), onAction = {}) }
}
