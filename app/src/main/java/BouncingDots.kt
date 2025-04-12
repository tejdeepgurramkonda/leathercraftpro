import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun BouncingDots(
    color: Color = Color(0xFF795548),
    dotSize: Float = 28f,
    animationDelay: Long = 150L,
    bounceHeight: Float = 40f,
    durationMillis: Int = 500
) {
    val dots = List(3) { remember { Animatable(0f) } }
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        scope.launch {
            while (true) {
                dots.forEachIndexed { index, animatable ->
                    scope.launch {
                        delay(animationDelay * index)
                        animatable.animateTo(
                            targetValue = 1f,
                            animationSpec = tween(durationMillis, easing = FastOutSlowInEasing)
                        )
                        animatable.animateTo(
                            targetValue = 0f,
                            animationSpec = tween(durationMillis, easing = BounceEasing) // 🔥 Ground hit effect
                        )
                        delay(200L) // 🔥 Pause at bottom before bouncing again
                    }
                }
                delay(animationDelay * dots.size + durationMillis + 200L) // Prevent app freeze
            }
        }
    }

    Row(
        modifier = Modifier.wrapContentSize(),
        horizontalArrangement = Arrangement.spacedBy((-12).dp) // 🔥 Zero spacing between dots
    ) {
        dots.forEach { animatable ->
            Canvas(
                modifier = Modifier.size(dotSize.dp)
            ) {
                val radius = dotSize / 2f
                drawCircle(
                    color = color,
                    radius = radius,
                    center = Offset(
                        x = center.x,
                        y = center.y - (animatable.value * bounceHeight)
                    )
                )
            }
        }
    }
}

// **Custom Bounce Easing (Slow Stop Effect)**
private val BounceEasing = CubicBezierEasing(0.34f, 1.56f, 0.64f, 1f)