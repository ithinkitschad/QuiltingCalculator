package com.chadsprojects.quiltingcalculator

import android.app.Application
import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.NoteAdd
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.chadsprojects.quiltingcalculator.ui.theme.QuiltingCalculatorTheme
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.admanager.AdManagerAdRequest
import com.google.android.gms.ads.admanager.AdManagerAdView
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

val Context.dataStore by preferencesDataStore(name = "journal_notes")

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        MobileAds.initialize(this) {}

        setContent {
            QuiltingCalculatorTheme {
                val navController = rememberNavController()

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    Color(0xFFFF4081),
                                    Color(0xFFFF99C8)
                                )
                            )
                        )
                ) {
                    Box(
                        modifier = Modifier.weight(1f)
                    ) {
                        NavHost(
                            navController = navController,
                            startDestination = "calculator"
                        ) {
                            composable("calculator") { CourtneyCalculatorTabs() }
                            composable("journal") { JournalScreen(navController) }
                        }

                        FloatingActionButton(
                            onClick = { navController.navigate("journal") },
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .padding(16.dp)
                                .shadow(8.dp, shape = CircleShape),
                            containerColor = Color(0xFFD81B60),
                            contentColor = Color.White
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.NoteAdd,
                                contentDescription = "Open Journal"
                            )
                        }
                    }

                    BannerAdView()
                }
            }
        }
    }
}
    @Composable
fun BannerAdView() {
    AndroidView(
        modifier = Modifier
            .fillMaxWidth()
            .height(50.dp),
        factory = { context ->
            AdManagerAdView(context).apply {
                setAdSize(AdSize.BANNER)
                adUnitId = "ca-app-pub-4438014143794059/8766587353"
                loadAd(AdManagerAdRequest.Builder().build())
            }
        }
    )
}

@Composable
fun CourtneyCalculatorTabs() {
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("Binding", "Backing", "Blocks", "Fabric")

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        Image(
            painter = painterResource(id = R.drawable.quilting_calculator_logo),
            contentDescription = "Quilting Calculator Logo",
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
                .wrapContentWidth(Alignment.CenterHorizontally),
            contentScale = ContentScale.Fit
        )

        Card(
            shape = RoundedCornerShape(12.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 6.dp)
        ) {
            TabRow(
                selectedTabIndex = selectedTab,
                modifier = Modifier.height(48.dp),
                containerColor = Color(0xFF885D69),
                contentColor = Color.White,
                indicator = { tabPositions ->
                    Box(
                        modifier = Modifier
                            .tabIndicatorOffset(tabPositions[selectedTab])
                            .height(4.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(
                                brush = Brush.horizontalGradient(
                                    colors = listOf(Color.White, Color(0xFFFF99C8))
                                )
                            )
                    )
                }
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(
                                if (selectedTab == index) Color(0xFFB67C8D) else Color.Transparent
                            )
                            .padding(vertical = 8.dp),
                        text = {
                            Text(
                                title,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = if (selectedTab == index) Color.White else Color.LightGray,
                                modifier = Modifier.align(Alignment.CenterHorizontally)
                            )
                        }
                    )
                }
            }
        }

        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            when (selectedTab) {
                0 -> BindingCalculator()
                1 -> BackingBattingCalculator()
                2 -> BlockYardageCalculator()
                3 -> FabricCalculator()
            }
        }
    }
}

@Composable
fun StyledOutlinedTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    keyboardType: KeyboardType = KeyboardType.Number
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 4.dp)
            .shadow(6.dp, shape = RoundedCornerShape(16.dp))
            .background(Color(0xFFB67C8D), shape = RoundedCornerShape(16.dp))
            .padding(8.dp)
    ) {
        Text(
            text = label,
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color.Black,
            modifier = Modifier.padding(start = 8.dp, bottom = 4.dp)
        )

        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            textStyle = TextStyle(fontSize = 18.sp, color = Color.Black),
            shape = RoundedCornerShape(16.dp),
            colors = TextFieldDefaults.colors(
                focusedTextColor = Color.Black,
                unfocusedTextColor = Color.Black,
                cursorColor = Color.Black,
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White,
                focusedIndicatorColor = Color(0xFFD81B60),
                unfocusedIndicatorColor = Color.LightGray
            ),
            keyboardOptions = KeyboardOptions.Default.copy(keyboardType = keyboardType),
            modifier = Modifier
                .fillMaxWidth()
                .shadow(2.dp, RoundedCornerShape(16.dp))
        )
    }
}

@Composable
fun CopyableResultText(resultText: String) {
    var showCopied by remember { mutableStateOf(false) }
    val clipboardManager = LocalClipboardManager.current

    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = resultText,
            fontSize = 18.sp,
            color = Color.Black,
            modifier = Modifier
                .padding(8.dp)
                .clickable {
                    clipboardManager.setText(androidx.compose.ui.text.AnnotatedString(resultText))
                    showCopied = true
                }
        )

        if (showCopied) {
            Text(
                text = "Copied!",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF008000),
                modifier = Modifier.padding(top = 4.dp)
            )

            LaunchedEffect(showCopied) {
                delay(1000)
                showCopied = false
            }
        }
    }
}

// BINDING CALCULATOR
@Composable
fun BindingCalculator() {
    var quiltWidth by remember { mutableStateOf("") }
    var quiltHeight by remember { mutableStateOf("") }
    var bindingStripWidth by remember { mutableStateOf("2.5") }
    var fabricWidth by remember { mutableStateOf("43") }
    var overage by remember { mutableStateOf("10") }
    var resultText by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            "Binding Calculator",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF000000)
        )

        Spacer(modifier = Modifier.height(8.dp))

        StyledOutlinedTextField(
            value = quiltWidth,
            onValueChange = { quiltWidth = it },
            label = "Quilt Width (in inches)"
        )

        StyledOutlinedTextField(
            value = quiltHeight,
            onValueChange = { quiltHeight = it },
            label = "Quilt Height (in inches)"
        )

        StyledOutlinedTextField(
            value = bindingStripWidth,
            onValueChange = { bindingStripWidth = it },
            label = "Binding Strip Width (in inches, default 2.5)"
        )

        StyledOutlinedTextField(
            value = fabricWidth,
            onValueChange = { fabricWidth = it },
            label = "Fabric Width (in inches, default 43)"
        )

        StyledOutlinedTextField(
            value = overage,
            onValueChange = { overage = it },
            label = "Extra Binding Length (in inches, default 10)"
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = {
            val width = quiltWidth.toDoubleOrNull() ?: 0.0
            val height = quiltHeight.toDoubleOrNull() ?: 0.0
            val stripWidth = bindingStripWidth.toDoubleOrNull() ?: 2.5
            val fabricW = fabricWidth.toDoubleOrNull() ?: 40.0
            val extraBinding = overage.toDoubleOrNull() ?: 10.0

            resultText = if (width > 0 && height > 0) {
                calculateBinding(width, height, stripWidth, fabricW, extraBinding)
            } else {
                "Please enter valid dimensions."
            }
        }) {
            Text("Calculate")
        }

        Spacer(modifier = Modifier.height(16.dp))

        CopyableResultText(resultText)

        Spacer(modifier = Modifier.height(24.dp))
    }
}

fun calculateBinding(
    quiltWidth: Double,
    quiltHeight: Double,
    bindingStripWidth: Double,
    fabricWidth: Double,
    overage: Double
): String {
    if (quiltWidth == 0.0 || quiltHeight == 0.0 || bindingStripWidth == 0.0 || fabricWidth == 0.0) {
        return "Please enter valid values."
    }

    val totalBindingLength = 2 * (quiltWidth + quiltHeight) + overage

    val numberOfStrips = kotlin.math.ceil(totalBindingLength / fabricWidth)

    val totalFabricRequired = (numberOfStrips * bindingStripWidth) / 36.0

    return """
        Total Binding Length: %.2f inches
        Number of Strips: %.0f
        Total Fabric Required: %.2f yards
    """.trimIndent().format(totalBindingLength, numberOfStrips, totalFabricRequired)
}

// BACKING & BATTING CALCULATOR
@Composable
fun BackingBattingCalculator() {
    var fabricWidth by remember { mutableStateOf("43") }
    var quiltWidth by remember { mutableStateOf("") }
    var quiltLength by remember { mutableStateOf("") }
    var overage by remember { mutableStateOf("8") }
    var resultText by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Backing & Batting Calculator",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF000000)
        )

        Spacer(modifier = Modifier.height(8.dp))

        InputField(value = fabricWidth, label = "Fabric Width (in inches, default 43)") { fabricWidth = it }
        InputField(value = quiltWidth, label = "Quilt Width (in inches)") { quiltWidth = it }
        InputField(value = quiltLength, label = "Quilt Length (in inches)") { quiltLength = it }
        InputField(value = overage, label = "Overage (extra inches, default 8)") { overage = it }

        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = {
            val fWidth = fabricWidth.toDoubleOrNull() ?: 43.0
            val qWidth = quiltWidth.toDoubleOrNull() ?: 0.0
            val qLength = quiltLength.toDoubleOrNull() ?: 0.0
            val overageVal = overage.toDoubleOrNull() ?: 8.0

            resultText = if (qWidth > 0 && qLength > 0) {
                calculateBackingBatting(fWidth, qWidth, qLength, overageVal)
            } else "Please enter valid dimensions."
        }) {
            Text("Calculate")
        }

        Spacer(modifier = Modifier.height(16.dp))

        CopyableResultText(resultText)
    }
}

@Composable
fun InputField(value: String, label: String, onValueChange: (String) -> Unit) {
    StyledOutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = label
    )
}


//BACKING & BATTING CALCULATION FUNCTION
fun calculateBackingBatting(fabricWidth: Double, quiltWidth: Double, quiltLength: Double, overage: Double): String {
    val adjustedWidth = quiltWidth + (overage * 2)
    val adjustedLength = quiltLength + (overage * 2)

    // **Width-wise calculation (fewer seams, longer cuts)**
    val panelsWidthWise = kotlin.math.ceil(adjustedWidth / fabricWidth).toInt()
    val totalYardageWidthWise = (panelsWidthWise * adjustedLength) / 36.0

    // **Length-wise calculation (more seams, shorter cuts)**
    val panelsLengthWise = kotlin.math.ceil(adjustedLength / fabricWidth).toInt()
    val totalYardageLengthWise = (panelsLengthWise * adjustedWidth) / 36.0

    return """
        **Cutting Width-Wise:**  
        Yardage = ${convertToFraction(totalYardageWidthWise)} yard(s)  
        (${(totalYardageWidthWise * 36).toInt()} inches)  
        - Final Backing Size: ${adjustedWidth.toInt()} x ${adjustedLength.toInt()} inches  
        - Seams Needed: ${if (panelsWidthWise > 1) panelsWidthWise - 1 else 0}  

        **Cutting Length-Wise:**  
        Yardage = ${convertToFraction(totalYardageLengthWise)} yard(s)  
        (${(totalYardageLengthWise * 36).toInt()} inches)  
        - Final Backing Size: ${adjustedLength.toInt()} x ${adjustedWidth.toInt()} inches  
        - Seams Needed: ${if (panelsLengthWise > 1) panelsLengthWise - 1 else 0}  
    """.trimIndent()
}

//FRACTION CONVERSION FUNCTION
fun convertToFraction(value: Double): String {
    val fractions = mapOf(
        0.125 to "1/8", 0.25 to "1/4", 0.375 to "3/8", 0.5 to "1/2",
        0.625 to "5/8", 0.75 to "3/4", 0.875 to "7/8"
    )

    val wholeNumber = value.toInt()
    val decimalPart = value - wholeNumber

    val fractionString = fractions.entries.minByOrNull { kotlin.math.abs(it.key - decimalPart) }?.value

    return if (wholeNumber == 0 && fractionString != null) {
        fractionString // Just fraction if no whole number
    } else if (fractionString != null) {
        "$wholeNumber $fractionString" // Whole number + fraction
    } else {
        wholeNumber.toString() // Just whole number
    }
}

//BLOCK YARDAGE CALCULATOR - Calculates total fabric yardage needed
@Composable
fun BlockYardageCalculator() {
    var blockWidth by remember { mutableStateOf("") }
    var blockHeight by remember { mutableStateOf("") }
    var numBlocks by remember { mutableStateOf("") }
    var fabricWidth by remember { mutableStateOf("43") }
    var resultText by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Block Yardage Calculator",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF000000)
        )

        Spacer(modifier = Modifier.height(8.dp))

        InputField(value = blockWidth, label = "Block Width (in inches)") { blockWidth = it }
        InputField(value = blockHeight, label = "Block Height (in inches)") { blockHeight = it }
        InputField(value = numBlocks, label = "Number of Blocks", keyboardType = KeyboardType.Number) { numBlocks = it }
        InputField(value = fabricWidth, label = "Fabric Width (in inches, default 43)", keyboardType = KeyboardType.Number) { fabricWidth = it }

        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = {
            val bWidth = blockWidth.toDoubleOrNull() ?: 0.0
            val bHeight = blockHeight.toDoubleOrNull() ?: 0.0
            val blocks = numBlocks.toIntOrNull() ?: 0
            val fabricW = fabricWidth.toDoubleOrNull() ?: 42.0

            resultText = if (bWidth > 0 && bHeight > 0 && blocks > 0) {
                calculateBlockYardage(bWidth, bHeight, blocks, fabricW)
            } else "Please enter valid dimensions."
        }) {
            Text("Calculate")
        }

        Spacer(modifier = Modifier.height(16.dp))
        CopyableResultText(resultText)

    }
}

@Composable
fun InputField(value: String, label: String, keyboardType: KeyboardType = KeyboardType.Number, onValueChange: (String) -> Unit) {
    StyledOutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = label,
        keyboardType = keyboardType
    )
}

//BLOCK YARDAGE CALCULATION FUNCTION
fun calculateBlockYardage(
    blockWidth: Double,
    blockHeight: Double,
    numBlocks: Int,
    fabricWidth: Double
): String {
    if (blockWidth == 0.0 || blockHeight == 0.0 || numBlocks == 0 || fabricWidth == 0.0) {
        return "Please enter valid values."
    }

    // **Step 1:** Determine how many blocks fit per strip across the fabric width
    val blocksPerRow = (fabricWidth / blockWidth).toInt()
    if (blocksPerRow == 0) return "Block width is too large for the fabric width."

    // **Step 2:** Calculate the number of full strips required
    val numRows = kotlin.math.ceil(numBlocks.toDouble() / blocksPerRow).toInt()

    // **Step 3:** Compute the total fabric length needed in inches and convert to yards
    val totalFabricLength = numRows * blockHeight
    val totalYardage = totalFabricLength / 36.0

    return """
        Total Blocks: $numBlocks
        Blocks Per Strip: $blocksPerRow
        Number of Strips Needed: $numRows
        Total Fabric Length: ${totalFabricLength.toInt()} inches
        Fabric Needed: ${convertToFraction(totalYardage)} yard(s)
    """.trimIndent()
}

//FABRIC CALCULATOR - Determines how many pieces can be cut from a fabric section
@Composable
fun FabricCalculator() {
    var fabricWidth by remember { mutableStateOf("") }
    var fabricLength by remember { mutableStateOf("") }
    var pieceWidth by remember { mutableStateOf("") }
    var pieceHeight by remember { mutableStateOf("") }
    var resultText by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Fabric Calculator",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF000000)
        )

        Spacer(modifier = Modifier.height(8.dp))

        InputField(value = fabricWidth, label = "Fabric Width (in inches)") { fabricWidth = it }
        InputField(value = fabricLength, label = "Fabric Length (in inches)") { fabricLength = it }
        InputField(value = pieceWidth, label = "Piece Width (in inches)") { pieceWidth = it }
        InputField(value = pieceHeight, label = "Piece Height (in inches)") { pieceHeight = it }

        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = {
            val fWidth = fabricWidth.toDoubleOrNull() ?: 0.0
            val fLength = fabricLength.toDoubleOrNull() ?: 0.0
            val pWidth = pieceWidth.toDoubleOrNull() ?: 1.0
            val pHeight = pieceHeight.toDoubleOrNull() ?: 1.0

            resultText = if (fWidth > 0 && fLength > 0 && pWidth > 0 && pHeight > 0) {
                calculateFabricUsage(fWidth, fLength, pWidth, pHeight)
            } else "Please enter valid dimensions."
        }) {
            Text("Calculate")
        }

        Spacer(modifier = Modifier.height(16.dp))
        CopyableResultText(resultText)

    }
}

//FABRIC USAGE CALCULATION FUNCTION
fun calculateFabricUsage(
    fabricWidth: Double,
    fabricLength: Double,
    pieceWidth: Double,
    pieceHeight: Double
): String {
    if (fabricWidth == 0.0 || fabricLength == 0.0 || pieceWidth == 0.0 || pieceHeight == 0.0) {
        return "Please enter valid values."
    }

    // **Step 1:** Calculate how many pieces fit per row
    val piecesPerRow = (fabricWidth / pieceWidth).toInt()
    if (piecesPerRow == 0) return "Piece width is too large for the fabric width."

    // **Step 2:** Calculate how many full rows fit in the fabric length
    val numRows = (fabricLength / pieceHeight).toInt()
    if (numRows == 0) return "Piece height is too large for the fabric length."

    // **Step 3:** Compute the total number of pieces that can be cut
    val totalPieces = piecesPerRow * numRows

    return """
        Fabric Width: ${fabricWidth.toInt()} inches
        Fabric Length: ${fabricLength.toInt()} inches
        Piece Size: ${pieceWidth.toInt()}" x ${pieceHeight.toInt()}"
        Pieces Per Row: $piecesPerRow
        Number of Rows: $numRows
        Total Pieces: $totalPieces
    """.trimIndent()
}

@Composable
fun JournalScreen(
    navController: NavController,
    journalViewModel: JournalViewModel = viewModel()
) {
    val notes by journalViewModel.notes.collectAsState()
    var newNote by remember { mutableStateOf("") }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFFFF4081),
                        Color(0xFFFF99C8)
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Journal",
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF000000),
                modifier = Modifier.padding(16.dp)
            )

            LazyColumn(modifier = Modifier.weight(1f)) {
                items(notes) { note ->
                    var isEditing by remember { mutableStateOf(false) }
                    var noteText by remember { mutableStateOf(note) }

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0x74746D77))
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            if (isEditing) {
                                OutlinedTextField(
                                    value = noteText,
                                    onValueChange = { noteText = it },
                                    label = { Text("Write your note...", color = Color.Black) },
                                    textStyle = TextStyle(fontSize = 18.sp, color = Color.Black),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = TextFieldDefaults.colors(
                                        focusedTextColor = Color.Black,
                                        unfocusedTextColor = Color.Black,
                                        cursorColor = Color(0xFFD81B60),
                                        focusedContainerColor = Color.White,
                                        unfocusedContainerColor = Color.White,
                                        focusedIndicatorColor = Color(0xFFD81B60),
                                        unfocusedIndicatorColor = Color(0xFFBDBDBD)
                                    ),
                                    singleLine = false,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 8.dp, horizontal = 16.dp)
                                        .shadow(4.dp, RoundedCornerShape(12.dp))
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
                                    Button(onClick = {
                                        journalViewModel.updateNote(note, noteText)
                                        isEditing = false
                                    }) {
                                        Text("Save")
                                    }
                                } else {
                                    IconButton(onClick = { isEditing = true }) {
                                        Icon(
                                            imageVector = Icons.AutoMirrored.Filled.NoteAdd,
                                            contentDescription = "Edit",
                                            tint = Color.Black
                                        )
                                    }
                                }

                                IconButton(onClick = { journalViewModel.deleteNote(note) }) {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = "Delete",
                                        tint = Color.Red
                                    )
                                }
                            }
                        }
                    }
                }
            }

            OutlinedTextField(
                value = newNote,
                onValueChange = { newNote = it },
                label = { Text("Write your note...", color = Color.Black) },
                textStyle = TextStyle(fontSize = 18.sp, color = Color.Black),
                shape = RoundedCornerShape(12.dp),
                colors = TextFieldDefaults.colors(
                    focusedTextColor = Color.Black,
                    unfocusedTextColor = Color.Black,
                    cursorColor = Color(0xFFD81B60),
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White,
                    focusedIndicatorColor = Color(0xFFD81B60),
                    unfocusedIndicatorColor = Color(0xFFBDBDBD)
                ),
                keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Text),
                singleLine = false,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .shadow(6.dp, RoundedCornerShape(12.dp))
            )

            Button(
                onClick = {
                    if (newNote.isNotBlank()) {
                        journalViewModel.addNote(newNote)
                        newNote = ""
                    }
                },
                modifier = Modifier.padding(16.dp)
            ) {
                Text("Save Note")
            }

            Button(
                onClick = { navController.popBackStack() },
                modifier = Modifier.padding(16.dp)
            ) {
                Text("Back to Calculator")
            }
        }
    }
}


class JournalViewModel(application: Application) : AndroidViewModel(application) {
        private val dataStore = application.dataStore
        private val notesKey = stringSetPreferencesKey("notes")

        val notes: StateFlow<List<String>> = dataStore.data
            .map { prefs -> prefs[notesKey]?.toList() ?: emptyList() }
            .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

        fun addNote(note: String) {
            viewModelScope.launch {
                dataStore.edit { prefs ->
                    val updatedNotes = (prefs[notesKey] ?: emptySet()).toMutableSet()
                    updatedNotes.add(note)
                    prefs[notesKey] = updatedNotes
                }
            }
        }

        fun updateNote(oldNote: String, newNote: String) {
            viewModelScope.launch {
                dataStore.edit { prefs ->
                    val updatedNotes = (prefs[notesKey] ?: emptySet()).toMutableSet()
                    updatedNotes.remove(oldNote)
                    updatedNotes.add(newNote)
                    prefs[notesKey] = updatedNotes
                }
            }
        }

        fun deleteNote(note: String) {
            viewModelScope.launch {
                dataStore.edit { prefs ->
                    val updatedNotes = (prefs[notesKey] ?: emptySet()).toMutableSet()
                    updatedNotes.remove(note)
                    prefs[notesKey] = updatedNotes
                }
            }
        }
    }