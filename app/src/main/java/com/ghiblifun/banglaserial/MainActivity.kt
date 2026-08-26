package com.ghiblifun.banglaserial

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.ui.PlayerView
import androidx.compose.ui.viewinterop.AndroidView

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            BanglaSerialApp()
        }
    }
}

@Composable
fun BanglaSerialApp() {
    val navController = rememberNavController()
    MaterialTheme {
        NavHost(navController = navController, startDestination = "welcome") {
            composable("welcome") { WelcomeScreen(navController) }
            composable("channels") { ChannelsScreen(navController) }
            composable("serial/{serialId}") { backStackEntry ->
                val id = backStackEntry.arguments?.getString("serialId") ?: "1"
                SerialScreen(serialId = id, navController = navController)
            }
            composable("player/{videoUrl}") { backStackEntry ->
                val url = backStackEntry.arguments?.getString("videoUrl") ?: ""
                PlayerScreen(videoUrl = url)
            }
        }
    }
}

@Composable
fun WelcomeScreen(navController: NavHostController) {
    Box(modifier = Modifier.fillMaxSize().background(
        brush = Brush.verticalGradient(listOf(Color(0xFF2196F3), Color(0xFFFF9800)))
    )) {
        Column(modifier = Modifier.align(Alignment.Center), horizontalAlignment = Alignment.CenterHorizontally) {
            // Placeholder for logo
            Box(modifier = Modifier.size(120.dp).background(Color.White), contentAlignment = Alignment.Center) {
                Text(text = "Bangla\nSerial", fontSize = 20.sp, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(24.dp))
            Button(onClick = { navController.navigate("channels") }) {
                Text(text = "শুরু করুন")
            }
        }
    }
}

data class Channel(val id: String, val name: String)

data class Serial(val id: String, val channelId: String, val name: String)

data class Episode(val id: String, val serialId: String, val title: String, val videoUrl: String)

@Composable
fun ChannelsScreen(navController: NavHostController) {
    val channels = remember { mutableStateListOf(Channel("1","নাটক চ্যানেল"), Channel("2","কমেডি চ্যানেল")) }
    Scaffold(topBar = { TopAppBar(title = { Text("Bangla Serial") }) }) { padding ->
        LazyColumn(modifier = Modifier.padding(padding)) {
            items(channels) { channel ->
                Row(modifier = Modifier.fillMaxWidth().clickable { navController.navigate("serial/${channel.id}") }.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(56.dp).background(Color.LightGray))
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(text = channel.name, fontSize = 18.sp)
                }
                Divider()
            }
        }
    }
}

@Composable
fun SerialScreen(serialId: String, navController: NavHostController) {
    // sample data
    val serial = Serial(id = serialId, channelId = "1", name = "সিরিয়াল নমুনা")
    val episodes = remember {
        mutableStateListOf(
            Episode("1", serialId, "আজকের এপিসোড", "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4"),
            Episode("2", serialId, "পুরনো এপিসোড ১", "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ElephantsDream.mp4")
        )
    }

    Scaffold(topBar = { TopAppBar(title = { Text(serial.name) }) }) { padding ->
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = "আজকের এপিসোড", fontWeight = FontWeight.Bold, fontSize = 20.sp)
            Spacer(modifier = Modifier.height(8.dp))
            val today = episodes.firstOrNull()
            if (today != null) {
                Card(modifier = Modifier.fillMaxWidth().clickable { navController.navigate("player/${today.videoUrl}") }) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(text = today.title, fontSize = 18.sp)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(text = "ট্যাপ করে প্লে করুন", fontSize = 14.sp, color = Color.Gray)
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text(text = "পুরোনো এপিসোড", fontWeight = FontWeight.Bold, fontSize = 18.sp)
            Spacer(modifier = Modifier.height(8.dp))
            LazyColumn {
                items(episodes.drop(1)) { ep ->
                    Row(modifier = Modifier.fillMaxWidth().clickable { navController.navigate("player/${ep.videoUrl}") }.padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.size(56.dp).background(Color.LightGray))
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(text = ep.title)
                            Text(text = "তারিখ: --", fontSize = 12.sp, color = Color.Gray)
                        }
                    }
                    Divider()
                }
            }
        }
    }
}

@Composable
fun PlayerScreen(videoUrl: String) {
    Scaffold(topBar = { TopAppBar(title = { Text("প্লেয়ার") }) }) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            if (videoUrl.isNotEmpty()) {
                AndroidView(factory = { context ->
                    val player = SimpleExoPlayer.Builder(context).build()
                    val playerView = PlayerView(context)
                    playerView.player = player
                    val mediaItem = MediaItem.fromUri(videoUrl)
                    player.setMediaItem(mediaItem)
                    player.prepare()
                    player.playWhenReady = true
                    playerView
                }, modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp))
            } else {
                Box(modifier = Modifier.fillMaxWidth().height(220.dp).background(Color.Black), contentAlignment = Alignment.Center) {
                    Text(text = "ভিডিও পাওয়া যায়নি", color = Color.White)
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(text = "ভিডিও বিবরণ", modifier = Modifier.padding(12.dp))
        }
    }
}
