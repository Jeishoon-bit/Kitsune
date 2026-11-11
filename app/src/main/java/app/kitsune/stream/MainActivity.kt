package app.kitsune.stream

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import app.kitsune.stream.ui.KitsuneApp
import app.kitsune.stream.ui.theme.KitsuneTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            KitsuneTheme {
                KitsuneApp()
            }
        }
    }
}
