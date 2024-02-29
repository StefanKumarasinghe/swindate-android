package com.example.swindate


import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*

import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.swindate.ui.theme.SwindateTheme

class MainActivity : ComponentActivity() {
    object PreferenceUtils {
        private const val PREF_NAME = "com.example.swindate.PREFERENCES"
        private const val IS_FIRST_LAUNCH = "IS_FIRST_LAUNCH"

        fun isFirstLaunch(context: Context): Boolean {
            val sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            return sharedPreferences.getBoolean(IS_FIRST_LAUNCH, true)
        }

        fun setFirstLaunchDone(context: Context) {
            val sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            val editor = sharedPreferences.edit()
            editor.putBoolean(IS_FIRST_LAUNCH, false)
            editor.apply()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        updateUI(resources.configuration.orientation)
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        updateUI(newConfig.orientation)
    }

    private fun updateUI(orientation: Int) {
        if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
            // Display the splash screen when in landscape mode
            setContent {
                SwindateTheme {
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = MaterialTheme.colors.background
                    ) {
                        // Replace this with your splash screen's Composable function
                        SplashScreen()
                    }
                }
            }
        } else {
            setContent {
                SwindateTheme {
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = MaterialTheme.colors.background
                    ) {
                        SwindateApp()
                    }
                }
            }
        }
    }
}

val Comfortaa = FontFamily(Font(R.font.comfortaa))
fun showWebView(url: String, context: Context) {
    val intent = Intent(context, WebViewActivity::class.java)
    intent.putExtra("url", url)
    context.startActivity(intent)
}
@Composable
fun SwindateApp() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val scrollState = rememberScrollState()

    // Variable Font Sizes
    val headerFontSize = 14.sp
    val announcementFontSize = 14.sp
    val titleFontSize = 24.sp
    val bodyFontSize = 16.sp
    val buttonTextSize = 16.sp
    val footerFontSize = 12.sp
    val showDialog = remember { mutableStateOf(MainActivity.PreferenceUtils.isFirstLaunch(context)) }
Column() {


    if (showDialog.value) {
        PrivacyPolicyDialog(
            onDismiss = {
                showDialog.value = false
            },
            onAgree = {
                MainActivity.PreferenceUtils.setFirstLaunchDone(context)
                showDialog.value = false
            }
        )
    }
}

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .verticalScroll(scrollState)
    ) {
        // Content
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White)
                .padding(12.dp)
        ) {
            Text(
                text = "Swindate",
                fontFamily = Comfortaa,
                color = Color.Red,
                fontSize = headerFontSize
            )
        }

        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.Yellow)
                .padding(12.dp)
        ) {
            Text(
                text = "You are using the Swindate's lite mode where everything is simplified...",
                fontFamily = Comfortaa,
                color = Color.Black,
                textAlign = TextAlign.Center,
                fontSize = announcementFontSize
            )
        }

        Spacer(modifier = Modifier.height(70.dp))

        Image(
            painter = painterResource(id = R.drawable.swincopy),
            contentDescription = "Swindate halloween theme cover image",
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(20.dp))

        Column(
            modifier = Modifier
                .weight(1f)  // This will make sure the content occupies all the available space
                .padding(16.dp)
        ) {
            Text(
                text = "Swindate's Dating Algorithm",
                fontFamily = Comfortaa,
                fontSize = titleFontSize
            )
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = "Created by two Swinburne Comp Sci students for University Students in Australia",
                fontFamily = Comfortaa,
                fontSize = bodyFontSize
            )

            Spacer(modifier = Modifier.height(20.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = {
                        scope.launch {
                            showWebView("https://swindate.com/dashboard", context)
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        backgroundColor = Color.Black,
                        contentColor = Color.White
                    )
                ) {
                    Text(text = "Start Matching", fontFamily = Comfortaa, fontSize = buttonTextSize)
                }
                Button(
                    onClick = {
                        scope.launch {
                            showWebView("https://swindate.com/documentation/algorithm", context)
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        backgroundColor = Color.Red,
                        contentColor = Color.White
                    )
                ) {
                    Text(text = "Algo", fontFamily = Comfortaa, fontSize = buttonTextSize)
                }


            }
        }

        // Footer
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.Black)
                .padding(12.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "© 2023 Swindate. All Rights Reserved. Created by 2 Swinburne Students",
                fontFamily = Comfortaa,
                color = Color.White,
                fontSize = footerFontSize,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun PrivacyPolicyDialog(onDismiss: () -> Unit, onAgree: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = "Welcome to Swindate Lite", fontFamily = Comfortaa) },
        text = {
            // The actual content of your privacy policy or a summary can be placed here.
            Text(text = "Swindate is a free app for anyone to use. While using Swindate, make sure you follow all the guidelines and policies. By using our apps, you are agreeing to the terms and conditions and privacy policy", fontFamily = Comfortaa)
        },
        confirmButton = {
            Button(onClick = onAgree, colors = ButtonDefaults.buttonColors(
                backgroundColor = Color.Black,
                contentColor = Color.White,
            ) ) {
                Text("Got it", fontFamily = Comfortaa)
            }
        }
    )
}


@Composable
fun SwindateTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colors = MaterialTheme.colors,
        shapes = MaterialTheme.shapes,
        content = content
    )
}
@Composable
fun SplashScreen() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(
                painter = painterResource(id = R.mipmap.image),
                contentDescription = "App Icon",
                modifier = Modifier.size(100.dp)
            )
            Spacer(modifier = Modifier.height(20.dp))
            Text(
                text = "Only available in portrait mode",
                fontFamily = Comfortaa,
                fontSize = 16.sp
            )
            Text(
                text = "to enhance experience",
                fontFamily = Comfortaa,
                fontSize = 16.sp
            )
        }
    }
}


@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    SwindateTheme {
        SwindateApp()
    }
}
