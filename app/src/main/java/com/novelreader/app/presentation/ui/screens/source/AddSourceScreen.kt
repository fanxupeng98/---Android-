package com.novelreader.app.presentation.ui.screens.source

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.novelreader.app.domain.model.BookSource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddSourceScreen(
    onNavigateBack: () -> Unit,
    viewModel: AddSourceViewModel = hiltViewModel()
) {
    val formState by viewModel.formState.collectAsStateWithLifecycle()
    val importResult by viewModel.importResult.collectAsStateWithLifecycle()

    val context = LocalContext.current

    // 文件选择器：支持 .json 和 .txt
    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        uri?.let {
            try {
                val inputStream = context.contentResolver.openInputStream(it)
                val content = inputStream?.bufferedReader()?.use { r -> r.readText() } ?: ""
                inputStream?.close()
                if (content.isNotBlank()) {
                    viewModel.importFromLocalJson(content)
                }
            } catch (e: Exception) {
                // ignore
            }
        }
    }

    LaunchedEffect(formState.savedSuccess) {
        if (formState.savedSuccess) {
            onNavigateBack()
        }
    }

    // 导入弹窗
    if (importResult.showImportDialog) {
        ImportSourceDialog(
            importUrl = importResult.importUrl,
            isLoading = importResult.isLoading,
            error = importResult.error,
            sources = importResult.sources,
            parseSuccess = importResult.parseSuccess,
            onUrlChange = viewModel::updateImportUrl,
            onImportUrl = { viewModel.importFromUrl(importResult.importUrl) },
            onPickFile = {
                filePickerLauncher.launch(arrayOf("application/json", "text/plain", "*/*"))
            },
            onApplySource = viewModel::applySource,
            onSaveSource = viewModel::saveImportedSource,
            onDismiss = viewModel::hideImportDialog
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("添加书源") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.Close, contentDescription = "取消")
                    }
                },
                actions = {
                    // 导入按钮
                    IconButton(onClick = viewModel::showImportDialog) {
                        Icon(Icons.Default.FileDownload, contentDescription = "导入书源")
                    }
                    TextButton(
                        onClick = { viewModel.saveSource() },
                        enabled = !formState.isSaving
                    ) {
                        if (formState.isSaving) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text("保存")
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 导入提示条
            ImportHintBar(onImportClick = viewModel::showImportDialog)

            // 基础信息
            Text(
                "基础信息",
                style = MaterialTheme.typography.titleMedium
            )

            OutlinedTextField(
                value = formState.name,
                onValueChange = { viewModel.updateName(it) },
                label = { Text("书源名称 *") },
                placeholder = { Text("如：笔趣阁") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            OutlinedTextField(
                value = formState.baseUrl,
                onValueChange = { viewModel.updateBaseUrl(it) },
                label = { Text("书源URL *") },
                placeholder = { Text("https://example.com") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            OutlinedTextField(
                value = formState.group,
                onValueChange = { viewModel.updateGroup(it) },
                label = { Text("分组") },
                placeholder = { Text("如：默认、VIP") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            HorizontalDivider()

            // 搜索规则
            Text(
                "搜索规则",
                style = MaterialTheme.typography.titleMedium
            )

            OutlinedTextField(
                value = formState.searchUrl,
                onValueChange = { viewModel.updateSearchUrl(it) },
                label = { Text("搜索URL") },
                placeholder = { Text("/s?kw={keyword}") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            OutlinedTextField(
                value = formState.searchNameRule,
                onValueChange = { viewModel.updateSearchNameRule(it) },
                label = { Text("书名选择器") },
                placeholder = { Text(".book-name, h3 a") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            OutlinedTextField(
                value = formState.searchAuthorRule,
                onValueChange = { viewModel.updateSearchAuthorRule(it) },
                label = { Text("作者选择器") },
                placeholder = { Text(".author") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            OutlinedTextField(
                value = formState.searchDetailUrlRule,
                onValueChange = { viewModel.updateSearchDetailUrlRule(it) },
                label = { Text("详情页链接选择器") },
                placeholder = { Text("a") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            HorizontalDivider()

            // 详情页规则
            Text(
                "详情页规则",
                style = MaterialTheme.typography.titleMedium
            )

            OutlinedTextField(
                value = formState.bookNameRule,
                onValueChange = { viewModel.updateBookNameRule(it) },
                label = { Text("书名选择器") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            OutlinedTextField(
                value = formState.bookAuthorRule,
                onValueChange = { viewModel.updateBookAuthorRule(it) },
                label = { Text("作者选择器") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            OutlinedTextField(
                value = formState.coverUrlRule,
                onValueChange = { viewModel.updateCoverUrlRule(it) },
                label = { Text("封面图片选择器") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            OutlinedTextField(
                value = formState.introRule,
                onValueChange = { viewModel.updateIntroRule(it) },
                label = { Text("简介选择器") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            HorizontalDivider()

            // 章节规则
            Text(
                "章节规则",
                style = MaterialTheme.typography.titleMedium
            )

            OutlinedTextField(
                value = formState.chapterListRule,
                onValueChange = { viewModel.updateChapterListRule(it) },
                label = { Text("章节列表选择器") },
                placeholder = { Text(".chapter-list a") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            OutlinedTextField(
                value = formState.chapterNameRule,
                onValueChange = { viewModel.updateChapterNameRule(it) },
                label = { Text("章节名称选择器") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            OutlinedTextField(
                value = formState.contentRule,
                onValueChange = { viewModel.updateContentRule(it) },
                label = { Text("正文内容选择器") },
                placeholder = { Text("#content") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            // 错误提示
            formState.error?.let { error ->
                Text(
                    text = error,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

/** 导入提示条 */
@Composable
private fun ImportHintBar(onImportClick: () -> Unit) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onImportClick() },
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.FileDownload,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    "导入书源",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Text(
                    "支持从 URL 或本地 JSON 文件导入书源配置",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                )
            }
            Icon(
                Icons.Default.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
        }
    }
}

/** 导入书源弹窗 */
@Composable
private fun ImportSourceDialog(
    importUrl: String,
    isLoading: Boolean,
    error: String?,
    sources: List<BookSource>,
    parseSuccess: Boolean,
    onUrlChange: (String) -> Unit,
    onImportUrl: () -> Unit,
    onPickFile: () -> Unit,
    onApplySource: (BookSource) -> Unit,
    onSaveSource: (BookSource) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("导入书源")
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Tab: URL / 本地文件
                if (!parseSuccess) {
                    // 导入输入区
                    Text(
                        "输入书源 URL 或选择本地 JSON 文件",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    OutlinedTextField(
                        value = importUrl,
                        onValueChange = onUrlChange,
                        label = { Text("书源 URL") },
                        placeholder = { Text("https://example.com/source.json") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        enabled = !isLoading
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = onPickFile,
                            modifier = Modifier.weight(1f),
                            enabled = !isLoading
                        ) {
                            Icon(Icons.Default.FolderOpen, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("本地文件")
                        }

                        Button(
                            onClick = onImportUrl,
                            modifier = Modifier.weight(1f),
                            enabled = !isLoading && importUrl.isNotBlank()
                        ) {
                            if (isLoading) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(16.dp),
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Icon(Icons.Default.CloudDownload, contentDescription = null, modifier = Modifier.size(18.dp))
                            }
                            Spacer(Modifier.width(4.dp))
                            Text("从 URL 导入")
                        }
                    }

                    error?.let {
                        Text(
                            it,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                } else {
                    // 解析结果列表
                    Text(
                        "找到 ${sources.size} 个书源，请选择：",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 300.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(sources) { source ->
                            SourceImportItem(
                                source = source,
                                onApply = { onApplySource(source) },
                                onSaveDirect = { onSaveSource(source) }
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            if (parseSuccess) {
                TextButton(onClick = onDismiss) {
                    Text("关闭")
                }
            }
        },
        dismissButton = {
            if (!parseSuccess) {
                TextButton(onClick = onDismiss) {
                    Text("取消")
                }
            }
        }
    )
}

/** 单个书源导入项 */
@Composable
private fun SourceImportItem(
    source: BookSource,
    onApply: () -> Unit,
    onSaveDirect: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { expanded = !expanded },
        shape = MaterialTheme.shapes.small,
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        source.name.ifBlank { "未命名书源" },
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        source.baseUrl,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1
                    )
                    if (source.group.isNotBlank()) {
                        Text(
                            "分组: ${source.group}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
                Icon(
                    if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = null
                )
            }

            if (expanded) {
                Spacer(Modifier.height(8.dp))
                HorizontalDivider()
                Spacer(Modifier.height(8.dp))

                // 展示关键规则
                if (source.searchUrl.isNotBlank()) {
                    RuleRow("搜索", source.searchUrl)
                }
                if (source.bookNameRule.isNotBlank()) {
                    RuleRow("书名规则", source.bookNameRule)
                }
                if (source.chapterListRule.isNotBlank()) {
                    RuleRow("章节规则", source.chapterListRule)
                }
                if (source.contentRule.isNotBlank()) {
                    RuleRow("正文规则", source.contentRule)
                }

                Spacer(Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onApply,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("编辑")
                    }
                    Button(
                        onClick = onSaveDirect,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("直接添加")
                    }
                }
            }
        }
    }
}

@Composable
private fun RuleRow(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth()) {
        Text(
            "$label: ",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            value,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1
        )
    }
}
