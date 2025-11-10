package cow.management.cowmanagementservice.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import cow.management.cowmanagementservice.model.*
import cow.management.cowmanagementservice.ui.theme.LightGreen
import cow.management.cowmanagementservice.ui.viewmodel.CowListViewModel
import cow.management.cowmanagementservice.ui.viewmodel.ViewModelFactory
import java.time.LocalDate
import java.time.format.DateTimeFormatter

// Main Screen Composable
@Composable
fun CowListScreen(modifier: Modifier = Modifier, factory: ViewModelFactory) {
    val viewModel: CowListViewModel = viewModel(factory = factory)
    val cows by viewModel.cows.collectAsStateWithLifecycle()
    val error by viewModel.errorState.collectAsStateWithLifecycle()
    val success by viewModel.operationSuccess.collectAsStateWithLifecycle()

    var showAddCowDialog by remember { mutableStateOf(false) }
    var selectedCow by remember { mutableStateOf<CowWithDetails?>(null) }
    var showUpdateCowDialog by remember { mutableStateOf(false) }

    LaunchedEffect(success) {
        if (success) {
            showAddCowDialog = false
            showUpdateCowDialog = false
            viewModel.onOperationCompleted()
        }
    }

    error?.let { ErrorDialog(errorMessage = it, onDismiss = { viewModel.onErrorShown() }) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = LightGreen, // Apply the light green background color
        floatingActionButton = { FloatingActionButton(onClick = { showAddCowDialog = true }) { Icon(Icons.Default.Add, contentDescription = "Add Cow") } }
    ) { innerPadding ->
        Column(modifier = Modifier.padding(innerPadding).fillMaxSize()) {
            Text(
                text = "The cows in your plantation are:",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                textAlign = TextAlign.Center
            )
            LazyColumn {
                items(cows) { cowWithDetails ->
                    CowListItem(cowWithDetails = cowWithDetails, onClick = { selectedCow = it })
                }
            }
        }
    }

    if (showAddCowDialog) {
        AddCowDialog(
            onDismiss = { showAddCowDialog = false },
            onSave = { earTag, breed, birthDate, entryDate, exitDate, sex, category ->
                viewModel.addCow(earTag, breed, birthDate, entryDate, exitDate, sex, category)
            }
        )
    }

    selectedCow?.let {
        if (showUpdateCowDialog) {
            UpdateCowDialog(
                cow = it.cow,
                onDismiss = { showUpdateCowDialog = false },
                onSave = { originalCow, earTag, breed, birthDate, entryDate, exitDate, sex, category ->
                    viewModel.updateCow(originalCow, earTag, breed, birthDate, entryDate, exitDate, sex, category)
                }
            )
        } else {
            CowDetailsDialog(
                cowWithDetails = it,
                onDismiss = { selectedCow = null },
                onUpdateClick = { showUpdateCowDialog = true },
                viewModel = viewModel
            )
        }
    }
}

//region Main Screen Components
@Composable
fun CowListItem(cowWithDetails: CowWithDetails, onClick: (CowWithDetails) -> Unit, modifier: Modifier = Modifier) {
    ListItem(
        modifier = modifier.clickable { onClick(cowWithDetails) },
        headlineContent = { Text(cowWithDetails.cow.earTag, fontWeight = FontWeight.Bold) },
        supportingContent = { Text("${cowWithDetails.cow.breed.name.formatEnum()} | ${cowWithDetails.cow.sex.name.formatEnum()}") },
        leadingContent = {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Text(text = cowWithDetails.cow.earTag.take(1).uppercase(), color = MaterialTheme.colorScheme.onPrimaryContainer)
            }
        }
    )
    Divider()
}

@Composable
fun SectionTitle(title: String, onClickAdd: (() -> Unit)?) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 16.dp, bottom = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        if (onClickAdd != null) {
            IconButton(onClick = onClickAdd, modifier = Modifier.size(24.dp)) {
                Icon(Icons.Default.Add, contentDescription = "Add $title")
            }
        }
    }
}
//endregion

//region Cow Dialogs
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddCowDialog(
    onDismiss: () -> Unit,
    onSave: (String, Breed?, String, String, String, Sex?, Category?) -> Unit,
    modifier: Modifier = Modifier
) { 
    val scrollState = rememberScrollState()
    var earTag by remember { mutableStateOf("") }
    var birthDate by remember { mutableStateOf("") }
    var entryDate by remember { mutableStateOf("") }
    var exitDate by remember { mutableStateOf("") }
    var selectedSex by remember { mutableStateOf<Sex?>(null) }
    var selectedCategory by remember { mutableStateOf<Category?>(null) }
    var selectedBreed by remember { mutableStateOf<Breed?>(null) }

    AlertDialog(
        modifier = modifier,
        onDismissRequest = onDismiss,
        title = { Text(text = "Add a new cow") },
        text = {
            Column(
                modifier = Modifier.verticalScroll(scrollState),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                TextField(value = earTag, onValueChange = { earTag = it }, label = { Text("Ear Tag") }, modifier = Modifier.fillMaxWidth())
                EnumDropDown(options = Breed.values(), selected = selectedBreed, onSelected = { selectedBreed = it }, label = "Breed")
                TextField(value = birthDate, onValueChange = { birthDate = it }, label = { Text("Birth Date (DD.MM.YYYY)") }, modifier = Modifier.fillMaxWidth())
                TextField(value = entryDate, onValueChange = { entryDate = it }, label = { Text("Entry Date (DD.MM.YYYY)") }, modifier = Modifier.fillMaxWidth())
                TextField(value = exitDate, onValueChange = { exitDate = it }, label = { Text("Exit Date (DD.MM.YYYY) (Optional)") }, modifier = Modifier.fillMaxWidth())
                EnumDropDown(options = Sex.values(), selected = selectedSex, onSelected = { selectedSex = it }, label = "Sex")
                EnumDropDown(options = Category.values(), selected = selectedCategory, onSelected = { selectedCategory = it }, label = "Category")
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onSave(earTag, selectedBreed, birthDate, entryDate, exitDate, selectedSex, selectedCategory) }
            ) {
                Text("Save")
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UpdateCowDialog(
    cow: Cow,
    onDismiss: () -> Unit,
    onSave: (Cow, String, Breed?, String, String, String, Sex?, Category?) -> Unit,
    modifier: Modifier = Modifier
) {
    val dateFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy")
    val scrollState = rememberScrollState()
    var earTag by remember { mutableStateOf(cow.earTag) }
    var birthDate by remember { mutableStateOf(cow.birthDate.format(dateFormatter)) }
    var entryDate by remember { mutableStateOf(cow.entryDate.format(dateFormatter)) }
    var exitDate by remember { mutableStateOf(cow.exitDate?.format(dateFormatter) ?: "") }
    var selectedSex by remember { mutableStateOf<Sex?>(cow.sex) }
    var selectedCategory by remember { mutableStateOf<Category?>(cow.category) }
    var selectedBreed by remember { mutableStateOf<Breed?>(cow.breed) }

    AlertDialog(
        modifier = modifier,
        onDismissRequest = onDismiss,
        title = { Text(text = "Update a cow") },
        text = {
            Column(
                modifier = Modifier.verticalScroll(scrollState),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                TextField(value = earTag, onValueChange = { earTag = it }, label = { Text("Ear Tag") }, modifier = Modifier.fillMaxWidth())
                EnumDropDown(options = Breed.values(), selected = selectedBreed, onSelected = { selectedBreed = it }, label = "Breed")
                TextField(value = birthDate, onValueChange = { birthDate = it }, label = { Text("Birth Date (DD.MM.YYYY)") }, modifier = Modifier.fillMaxWidth())
                TextField(value = entryDate, onValueChange = { entryDate = it }, label = { Text("Entry Date (DD.MM.YYYY)") }, modifier = Modifier.fillMaxWidth())
                TextField(value = exitDate, onValueChange = { exitDate = it }, label = { Text("Exit Date (DD.MM.YYYY) (Optional)") }, modifier = Modifier.fillMaxWidth())
                EnumDropDown(options = Sex.values(), selected = selectedSex, onSelected = { selectedSex = it }, label = "Sex")
                EnumDropDown(options = Category.values(), selected = selectedCategory, onSelected = { selectedCategory = it }, label = "Category")
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onSave(cow, earTag, selectedBreed, birthDate, entryDate, exitDate, selectedSex, selectedCategory) }
            ) {
                Text("Save")
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

@Composable
fun CowDetailsDialog(
    cowWithDetails: CowWithDetails,
    onDismiss: () -> Unit,
    onUpdateClick: () -> Unit,
    viewModel: CowListViewModel,
    modifier: Modifier = Modifier
) {
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }
    var showAddInseminationDialog by remember { mutableStateOf(false) }
    var showAddBirthDialog by remember { mutableStateOf(false) }
    var editingBirth by remember { mutableStateOf<BirthWithCalves?>(null) }
    var editingInsemination by remember { mutableStateOf<ArtificialInsemination?>(null) }
    val dateFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy")

    when (cowWithDetails.cow.sex) {
        Sex.FEMALE -> LaunchedEffect(cowWithDetails.cow.id) { viewModel.loadBirthsForCow(cowWithDetails.cow.id) }
        Sex.MALE -> LaunchedEffect(cowWithDetails.cow.id) { viewModel.loadInseminationsBySire(cowWithDetails.cow.id) }
    }
    val inseminationsBySire by viewModel.inseminationsBySire.collectAsStateWithLifecycle()
    val birthsWithCalves by viewModel.birthsWithCalves.collectAsStateWithLifecycle()

    AlertDialog(
        modifier = modifier,
        onDismissRequest = onDismiss,
        title = { Text(text = "Details for ${cowWithDetails.cow.earTag}") },
        text = {
            LazyColumn {
                item { DetailRow(label = "Sex", value = cowWithDetails.cow.sex.name.formatEnum()) }
                item { DetailRow(label = "Breed", value = cowWithDetails.cow.breed.name.formatEnum()) }
                item { DetailRow(label = "Category", value = cowWithDetails.cow.category.name.formatEnum()) }
                item { DetailRow(label = "Birth Date", value = cowWithDetails.cow.birthDate.format(dateFormatter)) }
                item { cowWithDetails.cow.exitDate?.let { DetailRow(label = "Exit Date", value = it.format(dateFormatter)) } }

                when (cowWithDetails.cow.sex) {
                    Sex.FEMALE -> {
                        item { SectionTitle(title = "Inseminations", onClickAdd = { showAddInseminationDialog = true }) }
                        if (cowWithDetails.inseminations.isEmpty()) item { Text("  None") } else items(cowWithDetails.inseminations) { insemination ->
                            Text("  - ${insemination.date.format(dateFormatter)} (Sire ID: ${insemination.sireId ?: "N/A"})", modifier = Modifier.clickable { editingInsemination = insemination })
                        }

                        item { SectionTitle(title = "Births", onClickAdd = { showAddBirthDialog = true }) }
                        if (birthsWithCalves.isEmpty()) item { Text("  None") } else items(birthsWithCalves) { birth ->
                            Text("  - ${birth.birth.date.format(dateFormatter)}", modifier = Modifier.clickable { editingBirth = birth })
                        }
                    }
                    Sex.MALE -> {
                        item { SectionTitle(title = "Inseminations Performed", onClickAdd = null) }
                        if (inseminationsBySire.isEmpty()) item { Text("  None") } else items(inseminationsBySire) { insemination ->
                            Text("  - ${insemination.insemination.date.format(dateFormatter)} with ${insemination.cow.earTag}", modifier = Modifier.clickable { /* Future Edit Action */ })
                        }
                    }
                }
            }
        },
        confirmButton = {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                TextButton(onClick = { showDeleteConfirmDialog = true }) { Text("Delete") }
                Spacer(modifier = Modifier.width(8.dp))
                TextButton(onClick = onUpdateClick) { Text("Update") }
                Spacer(modifier = Modifier.width(8.dp))
                TextButton(onClick = onDismiss) { Text("Close") }
            }
        },
        dismissButton = {}
    )

    editingBirth?.let { 
        EditBirthDialog(birthWithCalves = it, onDismiss = { editingBirth = null }, viewModel = viewModel)
    }
    editingInsemination?.let { 
        EditInseminationDialog(insemination = it, onDismiss = { editingInsemination = null }, viewModel = viewModel) 
    }
    
    if (showDeleteConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmDialog = false },
            title = { Text("Confirm Deletion") },
            text = { Text("Are you sure you want to delete cow ${cowWithDetails.cow.earTag}?") },
            confirmButton = { TextButton(onClick = { viewModel.deleteCow(cowWithDetails.cow); showDeleteConfirmDialog = false; onDismiss() }) { Text("Yes") } },
            dismissButton = { TextButton(onClick = { showDeleteConfirmDialog = false }) { Text("No") } }
        )
    }

    if (showAddInseminationDialog) {
        AddInseminationDialog(
            onDismiss = { showAddInseminationDialog = false },
            onSave = { date, sireId ->
                viewModel.addInsemination(ArtificialInsemination(date = date, cowId = cowWithDetails.cow.id, sireId = sireId))
            }
        )
    }

    if (showAddBirthDialog) {
        AddBirthDialog(
            onDismiss = { showAddBirthDialog = false },
            onSave = { date ->
                viewModel.addBirth(Birth(date = date, motherId = cowWithDetails.cow.id))
            }
        )
    }
}
//endregion

//region Event Dialogs
@Composable
fun EditInseminationDialog(
    insemination: ArtificialInsemination,
    onDismiss: () -> Unit,
    viewModel: CowListViewModel
) {
    val dateFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy")
    var date by remember { mutableStateOf(insemination.date.format(dateFormatter)) }
    var sireId by remember { mutableStateOf(insemination.sireId?.toString() ?: "") }
    var showDeleteConfirm by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Insemination") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                TextField(value = date, onValueChange = { date = it }, label = { Text("Insemination Date (DD.MM.YYYY)") })
                TextField(value = sireId, onValueChange = { sireId = it }, label = { Text("Sire ID (Optional)") })
            }
        },
        confirmButton = {
            Row(horizontalArrangement = Arrangement.End, modifier = Modifier.fillMaxWidth()) {
                TextButton(onClick = { showDeleteConfirm = true }) { Text("Delete") }
                TextButton(onClick = onDismiss) { Text("Cancel") }
                TextButton(
                    onClick = {
                        val updatedInsemination = insemination.copy(
                            date = LocalDate.parse(date, dateFormatter),
                            sireId = sireId.toLongOrNull()
                        )
                        viewModel.updateInsemination(updatedInsemination)
                        onDismiss()
                    },
                    enabled = date.isNotBlank()
                ) { Text("Save") }
            }
        }
    )

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("Confirm Delete") },
            text = { Text("Are you sure you want to delete this insemination record?") },
            confirmButton = { TextButton(onClick = { viewModel.deleteInsemination(insemination); showDeleteConfirm = false; onDismiss() }) { Text("Yes") } },
            dismissButton = { TextButton(onClick = { showDeleteConfirm = false }) { Text("No") } }
        )
    }
}

@Composable
fun EditBirthDialog(
    birthWithCalves: BirthWithCalves,
    onDismiss: () -> Unit,
    viewModel: CowListViewModel
) {
    val potentialCalves by viewModel.potentialCalves.collectAsStateWithLifecycle()
    val dateFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy")
    var date by remember { mutableStateOf(birthWithCalves.birth.date.format(dateFormatter)) }
    var showDeleteConfirm by remember { mutableStateOf(false) }

    val calvesInBirth = remember { mutableStateListOf(*birthWithCalves.calves.toTypedArray()) }
    val otherCalves = remember { mutableStateListOf<Cow>() }

    LaunchedEffect(date) {
        try {
            val parsedDate = LocalDate.parse(date, dateFormatter)
            viewModel.loadPotentialCalves(parsedDate)
        } catch (e: Exception) { /* Do nothing if date is not valid yet */ }
    }

    LaunchedEffect(potentialCalves) {
        otherCalves.clear()
        otherCalves.addAll(potentialCalves.filter { it !in calvesInBirth })
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Birth") },
        text = {
            Column {
                TextField(value = date, onValueChange = { date = it }, label = { Text("Birth Date (DD.MM.YYYY)") })

                Text("Calves in this Birth", fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 8.dp))
                calvesInBirth.forEach { calf ->
                    Text("- ${calf.earTag}", modifier = Modifier.clickable { calvesInBirth.remove(calf); otherCalves.add(calf) })
                }

                Text("Other Calves Born on this Day", fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 8.dp))
                otherCalves.forEach { calf ->
                    Text("+ ${calf.earTag}", modifier = Modifier.clickable { otherCalves.remove(calf); calvesInBirth.add(calf) })
                }
            }
        },
        confirmButton = {
             Row(horizontalArrangement = Arrangement.End, modifier = Modifier.fillMaxWidth()) {
                TextButton(onClick = { showDeleteConfirm = true }) { Text("Delete") }
                TextButton(onClick = onDismiss) { Text("Cancel") }
                TextButton(
                    onClick = {
                        val added = calvesInBirth.filter { it !in birthWithCalves.calves }
                        val removed = birthWithCalves.calves.filter { it !in calvesInBirth }
                        val updatedBirth = birthWithCalves.birth.copy(date = LocalDate.parse(date, dateFormatter))
                        viewModel.updateBirth(updatedBirth, added, removed)
                        onDismiss()
                    }
                ) { Text("Save") }
            }
        }
    )

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("Confirm Delete") },
            text = { Text("Are you sure you want to delete this birth record?") },
            confirmButton = { TextButton(onClick = { viewModel.deleteBirth(birthWithCalves.birth); showDeleteConfirm = false; onDismiss() }) { Text("Yes") } },
            dismissButton = { TextButton(onClick = { showDeleteConfirm = false }) { Text("No") } }
        )
    }
}

@Composable
fun AddInseminationDialog(onDismiss: () -> Unit, onSave: (LocalDate, Long?) -> Unit) {
    var date by remember { mutableStateOf("") }
    var sireId by remember { mutableStateOf("") }
    val dateFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Insemination") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                TextField(value = date, onValueChange = { date = it }, label = { Text("Insemination Date (DD.MM.YYYY)") })
                TextField(value = sireId, onValueChange = { sireId = it }, label = { Text("Sire ID (Optional)") })
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onSave(LocalDate.parse(date, dateFormatter), sireId.toLongOrNull())
                },
                enabled = date.isNotBlank()
            ) { Text("Save") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

@Composable
fun AddBirthDialog(onDismiss: () -> Unit, onSave: (LocalDate) -> Unit) {
    var date by remember { mutableStateOf("") }
    val dateFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Birth") },
        text = {
            TextField(value = date, onValueChange = { date = it }, label = { Text("Birth Date (DD.MM.YYYY)") })
        },
        confirmButton = {
            TextButton(
                onClick = { onSave(LocalDate.parse(date, dateFormatter)) },
                enabled = date.isNotBlank()
            ) { Text("Save") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

@Composable
fun ErrorDialog(errorMessage: String, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Error") },
        text = { Text(errorMessage) },
        confirmButton = { TextButton(onClick = onDismiss) { Text("OK") } }
    )
}
//endregion

//region Helpers
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun <T : Enum<T>> EnumDropDown(
    options: Array<T>,
    selected: T?,
    onSelected: (T) -> Unit,
    label: String
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = !expanded }) {
        TextField(
            modifier = Modifier.menuAnchor(),
            readOnly = true,
            value = selected?.name?.formatEnum() ?: "",
            onValueChange = {},
            label = { Text(label) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            colors = ExposedDropdownMenuDefaults.textFieldColors(),
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            options.forEach { selection ->
                DropdownMenuItem(
                    text = { Text(selection.name.formatEnum()) },
                    onClick = {
                        onSelected(selection)
                        expanded = false
                    }
                )
            }
        }
    }
}

fun String.formatEnum(): String {
    return this.replace("_", " ").lowercase().replaceFirstChar { it.titlecase() }
}

@Composable
fun DetailRow(label: String, value: String) {
    Row(modifier = Modifier.padding(vertical = 4.dp)) {
        Text(text = "$label:", fontWeight = FontWeight.Bold, modifier = Modifier.fillMaxWidth(0.4f))
        Text(text = value)
    }
}
//endregion
