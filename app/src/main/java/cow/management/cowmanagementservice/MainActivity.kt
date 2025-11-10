package cow.management.cowmanagementservice

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import cow.management.cowmanagementservice.ui.screen.CowListScreen
import cow.management.cowmanagementservice.ui.theme.CowManagementServiceTheme
import cow.management.cowmanagementservice.ui.viewmodel.ViewModelFactory

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val repository = (application as CowManagementApplication).repository
        val factory = ViewModelFactory(repository)

        setContent {
            CowManagementServiceTheme {
                CowListScreen(factory = factory)
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    CowManagementServiceTheme {
        Text(text = "Cow List Preview")
    }
}