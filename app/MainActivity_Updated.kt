package com.chadsprojects.quiltingcalculator

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.NoteAdd
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.*

val Context.dataStore by preferencesDataStore(name = "journal_notes")

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val navController = rememberNavController()
            QuiltingCalculatorTheme {
                NavHost(navController, startDestination = "calculator_tabs") {
                    composable("calculator_tabs") { CourtneyCalculatorTabs(navController) }
                    composable("journal") { JournalScreen(navController) }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CourtneyCalculatorTabs(navController: NavHostController) {
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("Binding", "Backing & Batting", "Block Yardage", "Fabric Calc")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFF99C8)),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Courtney's Calculator",
            fontSize = 26.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFFAD1457),
            modifier = Modifier.padding(16.dp)
        )

        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = Color(0xFFD81B60),
            contentColor = Color.White
        ) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    text = { Text(title) }
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        AnimatedContent(selectedTab) { tabIndex ->
            when (tabIndex) {
                0 -> BindingCalculator()
                1 -> BackingBattingCalculator()
                2 -> BlockYardageCalculator()
                3 -> FabricCalculator()
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        Button(
            onClick = { navController.navigate("journal") },
            modifier = Modifier.padding(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD81B60))
        ) {
            Icon(imageVector = Icons.Default.NoteAdd, contentDescription = "Journal")
            Spacer(modifier = Modifier.width(8.dp))
            Text("Open Journal", color = Color.White)
        }
    }
}

@Composable
fun JournalScreen(navController: NavController, journalViewModel: JournalViewModel = viewModel()) {
    val notes by journalViewModel.notes.collectAsState()
    var newNote by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFF99C8)), // Match background
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Journal",
            fontSize = 26.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFFAD1457),
            modifier = Modifier.padding(16.dp)
        )

        LazyColumn(modifier = Modifier.weight(1f)) {
            items(notes) { note ->
                var isEditing by remember { mutableStateOf(false) }
                var noteText by remember { mutableStateOf(note) }

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp)
                        .shadow(4.dp, RoundedCornerShape(8.dp)),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFFCCBC))
                ) {
                    Column(
                        modifier = Modifier
                            .padding(16.dp)
                            .animateContentSize()
                    ) {
                        if (isEditing) {
                            OutlinedTextField(
                                value = noteText,
                                onValueChange = { noteText = it },
                                textStyle = TextStyle(fontSize = 18.sp, color = Color.Black),
                                modifier = Modifier.fillMaxWidth()
                            )
                        } else {
                            Text(note, fontSize = 18.sp, color = Color.Black)
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Spacer(modifier = Modifier.weight(1f))

                            if (isEditing) {
                                Button(
                                    onClick = {
                                        journalViewModel.updateNote(note, noteText)
                                        isEditing = false
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD81B60))
                                ) {
                                    Text("Save", color = Color.White)
                                }
                            } else {
                                IconButton(onClick = { isEditing = true }) {
                                    Icon(imageVector = Icons.Default.NoteAdd, contentDescription = "Edit", tint = Color.Black)
                                }
                            }

                            IconButton(onClick = { journalViewModel.deleteNote(note) }) {
                                Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete", tint = Color.Red)
                            }
                        }
                    }
                }
            }
        }

        OutlinedTextField(
            value = newNote,
            onValueChange = { newNote = it },
            textStyle = TextStyle(fontSize = 18.sp, color = Color.Black),
            keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Text),
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .shadow(2.dp, RoundedCornerShape(8.dp))
        )

        Button(
            onClick = {
                if (newNote.isNotBlank()) {
                    journalViewModel.addNote(newNote)
                    newNote = ""
                }
            },
            modifier = Modifier.padding(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD81B60))
        ) {
            Text("Save Note", color = Color.White)
        }

        Button(
            onClick = { navController.popBackStack() },
            modifier = Modifier.padding(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD81B60))
        ) {
            Text("Back to Calculator", color = Color.White)
        }
    }
}
