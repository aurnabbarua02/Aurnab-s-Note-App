package com.example.notepad2

import android.content.Context
import android.icu.text.ListFormatter.Width
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.Paragraph
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
//import coil.compose.AsyncImage
import com.example.notepad2.ui.theme.NotePad2Theme
import org.w3c.dom.Document
import java.io.File
import java.io.FileOutputStream
import java.io.FileWriter

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            NotePad2Theme {
//                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
//                    Greeting(
//                        name = "Android",
//                        modifier = Modifier.padding(innerPadding)
//                    )
                    Surface(
                        modifier =Modifier.fillMaxSize(),
                        color = MaterialTheme.colorScheme.background
                    ) {
//                        SimpleTextEditor()
                        NoteApp()
                    }
                }
            }
        }
    }



fun loadTextFromFile(context: Context, fileName: String): String {
    return try {
        val file = File(context.filesDir, fileName)
        file.readText()
    } catch (e: Exception) {
        ""
    }
}
fun loadTextFromFile2(context: Context, fileName: String): String {
    return try {
        val file = File(context.filesDir, fileName)
        val text = file.readText()
        val words = text.split(" ") // Split into words
        val firstFiveWords = words.take(5).joinToString(" ") // Take first 5 words and join them
        firstFiveWords
    }catch (e: Exception) {
        ""
    }
}
@Composable
fun MyTextEditor(fileName: String, onBackClick: () -> Unit) {
    var text by remember { mutableStateOf("") }
    var showMessage by remember { mutableStateOf(false) }
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        text = loadTextFromFile(context, fileName)
    }
    Box(
        modifier = Modifier.fillMaxSize(),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 70.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally
        )
        {

            Row(
                modifier = Modifier.fillMaxWidth(0.9f),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {

                Button(onClick = onBackClick) {
                    Text(text = "Back")

                }

                if (showMessage) {
                    Toast.makeText(context, "Text saved successfully!", Toast.LENGTH_SHORT).show()
                    showMessage = false
                } else {
                    Button(
                        onClick = {
                            val file = File(context.filesDir, fileName)
                            file.writeText(text)
                            showMessage = true
                        }
                    ) {
                        Text(text = "Save")
                    }
                }


            }


            Spacer(modifier = Modifier.height(10.dp))
            TextField(
                value = text,
                onValueChange = { text = it },
                label = { Text("Write your note here") },
                singleLine = false,
                maxLines = Int.MAX_VALUE,
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .height(300.dp)
                    .clip(RoundedCornerShape(16.dp)),
                shape = RoundedCornerShape(16.dp)
            )
        }
    }
}
@Composable
fun NoteItem(
    fileName: String,
    onNoteClick: (String) -> Unit,
    isSelected: Boolean,
    onSelectChange: (String, Boolean) -> Unit,
    selectionMode: Boolean) {
    var text by remember { mutableStateOf("") }
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        text = loadTextFromFile2(context, fileName)}

    Button(
        modifier = Modifier
            .background(color = if (isSelected) Color.LightGray else MaterialTheme.colorScheme.background)
            .fillMaxWidth(0.9f),

        onClick = {
            if (selectionMode) {
                onSelectChange(fileName, !isSelected) // Toggle selection in selection mode
            } else {
                onNoteClick(fileName)
            }
        }
    ) {
        Text(text = text, modifier = Modifier.padding(16.dp), color = Color.Black)
    }
}

@Composable
fun NoteHomePage(onAddNote: () -> Unit, onNoteClick: (String) -> Unit) {
    val context = LocalContext.current
    var fileNameList by remember { mutableStateOf(loadFileNames(context)) }
    var selectionMode by remember { mutableStateOf(false) }
    val selectedNotes = remember { mutableStateListOf<String>() }

    Column(modifier = Modifier.padding(top = 70.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Row(modifier = Modifier.fillMaxWidth(0.9f), horizontalArrangement = Arrangement.SpaceBetween) {
            Button(onClick = { selectionMode = !selectionMode }) {
                Text(if (selectionMode) "Cancel" else "Select")
            }
            if (selectionMode && selectedNotes.isNotEmpty()) {
                Button(
                    onClick = {
                        deleteNotes(context, selectedNotes)
                        fileNameList = fileNameList.filter { it !in selectedNotes } // Update the list
                        selectedNotes.clear()
                        selectionMode = false
                    }
                ) {
                    Text("Delete")
                }
            }
            else {
                Button(
                    onClick = onAddNote
                ) {
                    Text(text = "Add Notes")
                }
            }
        }


        Spacer(modifier = Modifier.height(16.dp))


        Column(modifier = Modifier
            .verticalScroll(rememberScrollState())
            .align(Alignment.CenterHorizontally)) {
            if (fileNameList.isNotEmpty()) {
                for (fileName in fileNameList) {
                    NoteItem(fileName = fileName,
                        onNoteClick = onNoteClick,
                        isSelected = fileName in selectedNotes,
                        onSelectChange = { fileName, isSelected ->
                            if (isSelected) {
                                selectedNotes.add(fileName)
                            } else {
                                selectedNotes.remove(fileName)
                            }
                        },
                        selectionMode = selectionMode

                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }
}
fun deleteNotes(context: Context, fileNames: List<String>) {
    for (fileName in fileNames) {
        val file = File(context.filesDir, fileName)
        if (file.exists()) {
            file.delete()
        }
    }

    // Update the list file
    val listFile = File(context.filesDir, "All_necessary_Files_names.txt")
    val updatedLines = listFile.readLines().filter { it.trim() !in fileNames }
    listFile.writeText(updatedLines.joinToString("\n"))
}
@Composable
fun NoteApp() {
    var showTextEditor by remember { mutableStateOf(false) }
    var selectedFileName by remember { mutableStateOf("") }
    val context = LocalContext.current

    if (showTextEditor) {
        MyTextEditor(fileName = selectedFileName, onBackClick = { showTextEditor = false })
    } else {
        NoteHomePage(
            onAddNote = {
                createNewNoteFile(context)
                selectedFileName = loadFileNames(context).lastOrNull() ?: ""
                showTextEditor = true
            },
            onNoteClick = { fileName ->
                selectedFileName = fileName
                showTextEditor = true
            }
        )
    }
}
fun loadFileNames(context: Context): List<String> {
    return try {
        context.openFileInput("All_necessary_Files_names.txt")
            .bufferedReader()
            .readLines()
            .map { it.trim() } // Trim whitespace from each line
    } catch (e: Exception) {
        emptyList() // Return an empty list if the file doesn't exist or there's an error
    }
}
fun createNewNoteFile(context: Context) {
    val file = File(context.filesDir, "All_necessary_Files_names.txt")
    if (!file.exists()) {
        file.createNewFile()
    }

    val existingFileNames = loadFileNames(context)
    val lastFileName = existingFileNames.lastOrNull() ?: ""
    val nextNumber = if (lastFileName.isNotEmpty()) {
        val pattern = Regex("\\d+")
        val matchResult = pattern.find(lastFileName)
        (matchResult?.value?.toIntOrNull() ?: 0) + 1
    } else {
        1
    }

    val newFileName = "my_text_$nextNumber.txt"
    FileWriter(file, true).use { writer ->
        writer.write("$newFileName\n")
    }

    // Create the actual note file
    File(context.filesDir, newFileName).createNewFile()
}

@Preview(showBackground = true)
@Composable
fun SimpleTextEditorPreview() {
    NotePad2Theme {
        NoteApp()
    }
}
@Preview(showBackground = true)
@Composable
fun SimpleTextEditorPreview2() {
    NotePad2Theme {
        MyTextEditor(fileName =""){}
            

    }
}