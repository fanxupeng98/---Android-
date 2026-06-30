package com.novelreader.app.presentation.ui.screens.source

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.novelreader.app.domain.model.BookSource
import java.io.InputStreamReader
import java.nio.charset.StandardCharsets

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SourceScreen(
    onAddSource: () -> Unit,
    onEditSource: (Long) -> Unit,
    onNavigateBack: () -> Unit,
    viewModel: SourceViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    
    // 导入对话框状态
    var showImportUrlDialog by remember { mutableStateOf(false) }
    var showImportJsonDialog by remember { mutableStateOf(false) }
    var importUrl by remember { mutableStateOf("") }
    var localJsonContent by remember { mutableStateOf("") }
    var isImporting by remember { mutableStateOf(false) }
    var importError by remember { mutableStateOf<String?>(null) }
    
    // 批量导入结果
    var importSources by remember { mutableStateOf<List<BookSource>>(emptyList()) }
    var showBatchImportDialog by remember { mutableStateOf(false) }
    var selectedSources by remember { mutableStateOf<Set<Int>>(emptySet()) }
    
    // 导出对话框
    var showExportDialog by remember { mutableStateOf(false) }
    
    // 文件选择器
    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            try {
                context.contentResolver.openInputStream(it)?.use { inputStream ->
                    val content = InputStreamReader(inputStream, StandardCharsets.UTF_8).readText()
                    localJsonContent = content
                    showImportJsonDialog = true
                }
            } catch (e: Exception) {
                Toast.makeText(context, "读取文件失败: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
    
    // URL 导入对话框
    if (showImportUrlDialog) {
        AlertDialog(
            onDismissRequest = { 
                showImportUrlDialog = false
                importUrl = ""
                importError = null
            },
            title = { Text("网络导入") },
            text = {
                Column {
                    OutlinedTextField(
                        value = importUrl,
                        onValueChange = { 
                            importUrl = it
                            importError = null
                        },
                        placeholder = { Text("请输入书源 JSON 的 URL 地址") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text(
                        "支持书小页/阅读 Paper 等书源格式",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                    
                    importError?.let { error ->
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(error, color = MaterialTheme.colorScheme.error)
                    }
                    
                    if (isImporting) {
                        Spacer(modifier = Modifier.height(16.dp))
                        LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (importUrl.isBlank()) {
                            importError = "请输入 URL"
                            return@Button
                        }
                        isImporting = true
                        importError = null
                        viewModel.importFromUrl(importUrl) { success, sources, errorMsg ->
                            isImporting = false
                            if (success && sources.isNotEmpty()) {
                                importSources = sources
                                selectedSources = sources.indices.toSet()
                                showBatchImportDialog = true
                                showImportUrlDialog = false
                                importUrl = ""
                            } else {
                                importError = errorMsg ?: "导入失败"
                            }
                        }
                    },
                    enabled = !isImporting
                ) {
                    Text("导入")
                }
            },
            dismissButton = {
                TextButton(onClick = { 
                    showImportUrlDialog = false
                    importUrl = ""
                    importError = null
                }) {
                    Text("取消")
                }
            }
        )
    }
    
    // 本地 JSON 导入对话框
    if (showImportJsonDialog) {
        AlertDialog(
            onDismissRequest = { 
                showImportJsonDialog = false
                localJsonContent = ""
                importError = null
            },
            title = { Text("本地 JSON 导入") },
            text = {
                Column {
                    if (localJsonContent.isNotEmpty()) {
                        Text(
                            "已加载 ${localJsonContent.length} 字符的书源配置",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                    
                    Text(
                        "支持单个书源或批量书源数组（snake_case / camelCase 均兼容）",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                    
                    importError?.let { error ->
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(error, color = MaterialTheme.colorScheme.error)
                    }
                    
                    if (isImporting) {
                        Spacer(modifier = Modifier.height(16.dp))
                        LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        isImporting = true
                        importError = null
                        viewModel.importFromLocalJson(localJsonContent) { success, sources, errorMsg ->
                            isImporting = false
                            if (success && sources.isNotEmpty()) {
                                importSources = sources
                                selectedSources = sources.indices.toSet()
                                showBatchImportDialog = true
                                showImportJsonDialog = false
                                localJsonContent = ""
                            } else {
                                importError = errorMsg ?: "导入失败"
                            }
                        }
                    },
                    enabled = !isImporting && localJsonContent.isNotEmpty()
                ) {
                    Text("解析")
                }
            },
            dismissButton = {
                TextButton(onClick = { 
                    showImportJsonDialog = false
                    localJsonContent = ""
                    importError = null
                }) {
                    Text("取消")
                }
            }
        )
    }
    
    // 批量导入确认对话框
    if (showBatchImportDialog && importSources.isNotEmpty()) {
        AlertDialog(
            onDismissRequest = { 
                showBatchImportDialog = false
                importSources = emptyList()
                selectedSources = emptySet()
            },
            title = { Text("确认导入 (${selectedSources.size}/${importSources.size})") },
            text = {
                LazyColumn(
                    modifier = Modifier.heightIn(max = 400.dp)
                ) {
                    items(importSources.size) { index ->
                        val source = importSources[index]
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = index in selectedSources,
                                onCheckedChange = { checked ->
                                    selectedSources = if (checked) {
                                        selectedSources + index
                                    } else {
                                        selectedSources - index
                                    }
                                }
                            )
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = source.name,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                Text(
                                    text = source.baseUrl,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.outline,
                                    maxLines = 1
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val toImport = importSources.filterIndexed { index, _ -> index in selectedSources }
                        viewModel.batchImportSources(toImport) { count ->
                            Toast.makeText(context, "成功导入 $count 个书源", Toast.LENGTH_SHORT).show()
                            showBatchImportDialog = false
                            importSources = emptyList()
                            selectedSources = emptySet()
                        }
                    },
                    enabled = selectedSources.isNotEmpty()
                ) {
                    Text("导入选中 (${selectedSources.size})")
                }
            },
            dismissButton = {
                TextButton(onClick = { 
                    showBatchImportDialog = false
                    importSources = emptyList()
                    selectedSources = emptySet()
                }) {
                    Text("取消")
                }
            }
        )
    }
    
    // 导出对话框
    if (showExportDialog && uiState.exportText != null) {
        AlertDialog(
            onDismissRequest = {
                showExportDialog = false
                viewModel.clearExportText()
            },
            title = { Text("导出书源") },
            text = {
                Column {
                    OutlinedTextField(
                        value = uiState.exportText!!,
                        onValueChange = { },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(150.dp),
                        maxLines = 10,
                        readOnly = true
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text(
                        "复制后可以分享给他人",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            },
            confirmButton = {
                Button(onClick = {
                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                    val clip = ClipData.newPlainText("书源", uiState.exportText)
                    clipboard.setPrimaryClip(clip)
                    Toast.makeText(context, "已复制到剪贴板", Toast.LENGTH_SHORT).show()
                    showExportDialog = false
                    viewModel.clearExportText()
                }) {
                    Text("复制")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showExportDialog = false
                    viewModel.clearExportText()
                }) {
                    Text("关闭")
                }
            }
        )
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("书源管理") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "返回")
                    }
                },
                actions = {
                    // 导入菜单
                    var showImportMenu by remember { mutableStateOf(false) }
                    IconButton(onClick = { showImportMenu = true }) {
                        Icon(Icons.Default.FileDownload, contentDescription = "导入")
                    }
                    DropdownMenu(
                        expanded = showImportMenu,
                        onDismissRequest = { showImportMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("📡 网络导入") },
                            onClick = {
                                showImportMenu = false
                                showImportUrlDialog = true
                            },
                            leadingIcon = { Icon(Icons.Default.Link, null) }
                        )
                        DropdownMenuItem(
                            text = { Text("📁 本地导入") },
                            onClick = {
                                showImportMenu = false
                                filePickerLauncher.launch("application/json")
                            },
                            leadingIcon = { Icon(Icons.Default.FolderOpen, null) }
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddSource,
                modifier = Modifier.padding(bottom = 80.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "添加书源")
            }
        }
    ) { padding ->
        if (uiState.sources.isEmpty() && !uiState.isLoading) {
            EmptySourceList(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                onAddSource = onAddSource,
                onImportUrl = { showImportUrlDialog = true },
                onImportFile = { filePickerLauncher.launch("application/json") }
            )
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(
                    start = 16.dp,
                    end = 16.dp,
                    top = 8.dp,
                    bottom = 96.dp
                ),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(uiState.sources, key = { it.id }) { source ->
                    SourceItem(
                        source = source,
                        onToggle = { viewModel.toggleSource(source.id, !source.enabled) },
                        onEdit = { onEditSource(source.id) },
                        onDelete = { viewModel.deleteSource(source.id) },
                        onExport = {
                            viewModel.exportSource(source.id)
                            showExportDialog = true
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun SourceItem(
    source: BookSource,
    onToggle: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onExport: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showMenu by remember { mutableStateOf(false) }
    
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Switch(
                checked = source.enabled,
                onCheckedChange = { onToggle() }
            )
            
            Spacer(modifier = Modifier.width(12.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = source.name.ifBlank { "未命名书源" },
                    style = MaterialTheme.typography.titleSmall
                )
                
                if (source.group.isNotBlank()) {
                    Text(
                        text = source.group,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                
                Text(
                    text = source.baseUrl,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.outline,
                    maxLines = 1
                )
            }
            
            Box {
                IconButton(onClick = { showMenu = true }) {
                    Icon(Icons.Default.MoreVert, contentDescription = "更多")
                }
                
                DropdownMenu(
                    expanded = showMenu,
                    onDismissRequest = { showMenu = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("编辑") },
                        onClick = {
                            showMenu = false
                            onEdit()
                        },
                        leadingIcon = { Icon(Icons.Default.Edit, null) }
                    )
                    DropdownMenuItem(
                        text = { Text("导出") },
                        onClick = {
                            showMenu = false
                            onExport()
                        },
                        leadingIcon = { Icon(Icons.Default.Upload, null) }
                    )
                    DropdownMenuItem(
                        text = { Text("删除") },
                        onClick = {
                            showMenu = false
                            onDelete()
                        },
                        leadingIcon = {
                            Icon(
                                Icons.Default.Delete,
                                null,
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun EmptySourceList(
    modifier: Modifier = Modifier,
    onAddSource: () -> Unit,
    onImportUrl: () -> Unit,
    onImportFile: () -> Unit
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            Icons.Default.Source,
            contentDescription = null,
            modifier = Modifier.size(80.dp),
            tint = MaterialTheme.colorScheme.outline
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = "暂无书源",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // 直接显示两个导入按钮
        Button(
            onClick = onAddSource,
            modifier = Modifier.fillMaxWidth(0.7f)
        ) {
            Icon(Icons.Default.Add, null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("手动添加书源")
        }
        
        Spacer(modifier = Modifier.height(12.dp))
        
        Row(
            modifier = Modifier.fillMaxWidth(0.8f),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedButton(
                onClick = onImportUrl,
                modifier = Modifier.weight(1f)
            ) {
                Icon(Icons.Default.Link, null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("网络")
            }
            
            OutlinedButton(
                onClick = onImportFile,
                modifier = Modifier.weight(1f)
            ) {
                Icon(Icons.Default.FolderOpen, null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("本地")
            }
        }
    }
}
