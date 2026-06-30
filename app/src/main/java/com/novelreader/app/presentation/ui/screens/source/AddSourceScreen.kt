package com.novelreader.app.presentation.ui.screens.source

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddSourceScreen(
    onNavigateBack: () -> Unit,
    viewModel: AddSourceViewModel = hiltViewModel()
) {
    val formState by viewModel.formState.collectAsStateWithLifecycle()

    LaunchedEffect(formState.savedSuccess) {
        if (formState.savedSuccess) {
            onNavigateBack()
        }
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
                placeholder = { Text(".book-title") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            OutlinedTextField(
                value = formState.bookAuthorRule,
                onValueChange = { viewModel.updateBookAuthorRule(it) },
                label = { Text("作者选择器") },
                placeholder = { Text(".author") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            OutlinedTextField(
                value = formState.coverUrlRule,
                onValueChange = { viewModel.updateCoverUrlRule(it) },
                label = { Text("封面URL选择器") },
                placeholder = { Text("img.cover") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            OutlinedTextField(
                value = formState.introRule,
                onValueChange = { viewModel.updateIntroRule(it) },
                label = { Text("简介选择器") },
                placeholder = { Text(".intro") },
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
