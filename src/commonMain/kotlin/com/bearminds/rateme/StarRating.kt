package com.bearminds.rateme

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ripple
import androidx.compose.runtime.mutableIntStateOf
import audiobookify.rateme.generated.resources.Res
import audiobookify.rateme.generated.resources.star_fill
import audiobookify.rateme.generated.resources.star_line
import org.jetbrains.compose.resources.vectorResource
import org.jetbrains.compose.ui.tooling.preview.Preview
import androidx.compose.material3.Icon
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.draw.clip
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

data class StarStyle(
    val color: Color = Color(0xFFE0E0E0),
    val starSize: Dp = 40.dp,
    val borderColor: Color = Color(0xFFE0E0E0),
    val backgroundColor: Color = Color(0xFFE0E0E0),
)

@Composable
fun StarRatingRow(
    modifier: Modifier,
    initialRating: Int = 0,
    maxRating: Int = 5,
    onRatingChange: (Int) -> Unit,
    inactiveStyle: StarStyle,
    activeStyle: StarStyle,
) {
    val shape = RoundedCornerShape(size = 8.dp)

    var rating by remember(initialRating) { mutableIntStateOf(initialRating) }
    val style = when (rating) {
        0 -> inactiveStyle
        else -> activeStyle
    }

    val scope = rememberCoroutineScope()

    StarRating(
        modifier = modifier
            .fillMaxWidth()
            .clip(shape = shape)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        rating = rating,
        maxRating = maxRating,
        style = style,
        onRatingChange = {
            rating = it
            scope.launch {
                delay(rating * 150L)
                onRatingChange(it)
            }
        }
    )
}

@Composable
private fun StarRating(
    modifier: Modifier = Modifier,
    style: StarStyle,
    rating: Int = 0,
    maxRating: Int = 5,
    onRatingChange: (Int) -> Unit,
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        for (i in 1..maxRating) {
            val starAnimationDelay = if (i <= rating) {
                // Filling: animate from left to right
                i * 50L
            } else {
                // Unfilling: animate from right to left
                (maxRating - i + 1) * 50L
            }
            StarIcon(
                modifier = Modifier.size(style.starSize),
                isFilled = i <= rating,
                style = style,
                animationDelay = starAnimationDelay,
                onClick = {
                    onRatingChange(i)
                }
            )
        }
    }
}

@Composable
fun StarIcon(
    modifier: Modifier = Modifier,
    isFilled: Boolean,
    style: StarStyle,
    animationDelay: Long = 0,
    onClick: () -> Unit
) {
    val animatedTint by animateColorAsState(
        targetValue = if (isFilled) style.color else Color.Transparent,
        animationSpec = tween(
            durationMillis = 400,
            delayMillis = animationDelay.toInt(),
            easing = FastOutSlowInEasing
        ),
        label = "tint_animation"
    )

    Box(
        modifier = modifier
            .size(style.starSize)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(false)
            ) { onClick() },
        contentAlignment = Alignment.Center
    ) {
        // Background star (unfilled)
        Icon(
            imageVector = vectorResource(Res.drawable.star_line),
            contentDescription = null,
            modifier = Modifier.size(style.starSize),
            tint = style.borderColor
        )

        // Foreground star (filled)
        Icon(
            imageVector = vectorResource(Res.drawable.star_fill),
            contentDescription = null,
            modifier = Modifier.size(style.starSize),
            tint = animatedTint
        )
    }
}


@Preview
@Composable
private fun StarRatingPreview() {
    val inactiveStyle = StarStyle(
        color = Color(0xFFE0E0E0),
        starSize = 40.dp,
        borderColor = Color(0xFFE0E0E0),
        backgroundColor = Color(0xFFF5F5F5)
    )
    val activeStyle = StarStyle(
        color = Color(0xFFFFD700),
        starSize = 40.dp,
        borderColor = Color(0xFFFFD700),
        backgroundColor = Color(0xFFFFF8E1)
    )

    MaterialTheme {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(10.dp, Alignment.Top),
        ) {
            StarRatingRow(
                initialRating = 3,
                maxRating = 5,
                onRatingChange = {},
                inactiveStyle = inactiveStyle,
                activeStyle = activeStyle,
                modifier = Modifier.fillMaxWidth()
            )

            StarRatingRow(
                initialRating = 0,
                maxRating = 5,
                onRatingChange = {},
                inactiveStyle = inactiveStyle,
                activeStyle = activeStyle,
                modifier = Modifier.fillMaxWidth()
            )

            StarRatingRow(
                initialRating = 1,
                maxRating = 5,
                onRatingChange = {},
                inactiveStyle = inactiveStyle.copy(starSize = 56.dp),
                activeStyle = activeStyle.copy(starSize = 56.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(80.dp)
            )

            StarRatingRow(
                initialRating = 10,
                maxRating = 10,
                onRatingChange = {},
                inactiveStyle = inactiveStyle.copy(starSize = 24.dp),
                activeStyle = activeStyle.copy(starSize = 24.dp),
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
