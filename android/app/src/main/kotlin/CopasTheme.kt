package copas.app

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

@Composable
fun CopasTheme(content: @Composable () -> Unit) {
  val darkTheme = isSystemInDarkTheme()
  val brandColor = Color(0xFF7E7EFF)

  MaterialTheme(
          colorScheme =
                  if (darkTheme) {
                    darkColorScheme(
                            primary = brandColor,
                            background = Color(0xFF121212),
                            surface = Color(0xFF1E1E1E),
                            error = Color(0xFFF44336),
                            onPrimary = Color.White,
                            onBackground = Color.White,
                            onSurface = Color.White,
                            onError = Color.White
                    )
                  } else {
                    lightColorScheme(
                            primary = brandColor,
                            background = Color(0xFFF5F5F5),
                            surface = Color.White,
                            error = Color(0xFFD32F2F),
                            onPrimary = Color.Black,
                            onBackground = Color.Black,
                            onSurface = Color.Black,
                            onError = Color.White
                    )
                  },
          typography =
                  Typography(
                          bodyLarge = TextStyle(fontSize = 16.sp),
                          titleLarge = TextStyle(fontSize = 22.sp, fontWeight = FontWeight.Bold),
                          labelMedium = TextStyle(fontSize = 14.sp)
                  ),
          content = content
  )
}
