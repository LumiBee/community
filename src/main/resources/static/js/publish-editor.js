import { showToast } from "./common-utils.js";

document.addEventListener("DOMContentLoaded", function () {

    // =================================================================
    // 1. 全局配置与状态管理
    // =================================================================

    // CSRF 安全凭证
    const csrfTokenEl = document.querySelector('meta[name="_csrf"]');
    const csrfHeaderEl = document.querySelector('meta[name="_csrf_header"]');
    const csrfToken = csrfTokenEl ? csrfTokenEl.content : null;
    const csrfHeader = csrfHeaderEl ? csrfHeaderEl.content : null;

    if (!csrfToken || !csrfHeader) {
        console.error("CRITICAL: CSRF meta tags not found!");
        alert('页面安全凭证加载失败，请刷新重试');
        return; // 终止脚本执行
    }

    // 自动保存相关状态
    let autoSaveTimer = null;
    let isContentDirty = false;
    let isSaving = false;
    const AUTO_SAVE_INTERVAL = 1000;

    // 文章/草稿 ID 管理
    let currentDraftId = null;
    const urlParams = new URLSearchParams(window.location.search);
    const draftIdFromUrl = urlParams.get('draftId');
    const articleIdInput = document.getElementById('articleId');
    const initialArticleId = articleIdInput ? articleIdInput.value : null;

    if (draftIdFromUrl) {
        currentDraftId = parseInt(draftIdFromUrl, 10);
    } else if (initialArticleId) {
        currentDraftId = parseInt(initialArticleId, 10);
    }

    const isEditMode = !!initialArticleId;

    // DOM 元素缓存 (保持不变)
    const dom = {
        editorElement: document.getElementById('toastUiEditor'),
        articleTitleInput: document.getElementById('articleTitleInput'),
        wordCountEl: document.getElementById('wordCount'),
        charCountEl: document.getElementById('charCount'),
        autosaveStatusEl: document.querySelector('.auto-save-indicator span'),
        topPublishBtn: document.querySelector('.publish-btn'),
        publishModalEl: document.getElementById('publishSettingsModal'),
        confirmPublishBtn: document.getElementById('confirmPublishBtn'),
        articleTagsInput: document.getElementById('articleTags'),
        articlePortfolioInput: document.getElementById('articlePortfolio'),
        articleSummaryTextarea: document.getElementById('articleSummary'),
        summaryCharCount: document.getElementById('summaryCharCount'),
        generateAISummaryBtn: document.getElementById('generateAISummaryBtn'),
        initialContentData: document.getElementById('initial-content-data'),
        topActionBar: document.querySelector('.top-action-bar'), // 为事件委托新增
    };

    let editorInstance = null;

    // =================================================================
    // 2. 核心功能函数定义 (保持不变)
    // =================================================================

    function initEditor() {
        if (!dom.editorElement) return;
        const editorPlugins = [];
        if (toastui.Editor.plugin?.['code-syntax-highlight'] && typeof hljs !== 'undefined') {
            editorPlugins.push([toastui.Editor.plugin['code-syntax-highlight'], { hljs }]);
        }
        const initialContent = dom.initialContentData ? dom.initialContentData.textContent : '';
        editorInstance = new toastui.Editor({
            el: dom.editorElement,
            height: 'calc(100vh - 200px)',
            initialEditType: 'markdown',
            previewStyle: 'vertical',
            initialValue: initialContent,
            placeholder: '在这里开始你的创作吧...',
            usageStatistics: false,
            plugins: editorPlugins,
        });
        // 注意：编辑器库的自定义事件无法通过DOM事件委托，必须直接绑定
        editorInstance.on('change', onContentChange);
        editorInstance.on('change', updateStats); // 编辑器内容改变也应更新字数统计
    }

    // 内容变更处理函数
    function onContentChange() {
        isContentDirty = true;
        updateAutoSaveStatus('内容已修改...', false);
        if (autoSaveTimer) clearTimeout(autoSaveTimer);
        autoSaveTimer = setTimeout(autoSaveDraft, AUTO_SAVE_INTERVAL);
    }

    // 自动保存草稿
    async function autoSaveDraft() {
        if (!isContentDirty || isSaving) return;
        isSaving = true;
        updateAutoSaveStatus('正在自动保存...', true);
        const title = dom.articleTitleInput.value.trim();
        const content = editorInstance.getMarkdown();
        if (!title && !content) {
            isSaving = false;
            updateAutoSaveStatus('草稿为空', false);
            return;
        }
        const requestData = {
            articleId: currentDraftId,
            title: title || "无标题草稿",
            content: content,
        };
        try {
            const response = await apiRequest('/api/article/save-draft', 'POST', requestData);
            if (response.ok) {
                const result = await response.json();
                isContentDirty = false;
                if (!currentDraftId && result.articleId) {
                    currentDraftId = result.articleId;
                    const newUrl = `${window.location.pathname}?draftId=${currentDraftId}`;
                    history.replaceState({ path: newUrl }, '', newUrl);
                }
                const now = new Date();
                updateAutoSaveStatus(`已于 ${now.getHours()}:${String(now.getMinutes()).padStart(2, '0')} 保存`, false);
            } else {
                const errorData = await response.json().catch(() => ({ message: '保存失败，请检查网络连接' }));
                showToast(errorData.message, 'error');
                updateAutoSaveStatus('自动保存失败', false);
            }
        } catch (error) {
            updateAutoSaveStatus('保存异常', false);
        } finally {
            isSaving = false;
        }
    }

    // 打开发布模态框
    function handleOpenPublishModal() {
        const title = dom.articleTitleInput.value.trim();
        const content = editorInstance.getMarkdown().trim();
        if (!title || !content) {
            showToast('发布前，请先填写标题和内容。', 'warning');
            return;
        }
        if (dom.publishModalEl && typeof $ !== 'undefined') {
            $(dom.publishModalEl).modal('show');
            if(!dom.articleSummaryTextarea.value){
                handleGenerateSummary();
            }
        } else {
            showToast('发布窗口组件初始化失败。', 'error');
        }
    }

    // 确认并提交发布
    async function handleConfirmSubmit() {
        const requestData = {
            articleId: currentDraftId,
            title: dom.articleTitleInput.value.trim(),
            content: editorInstance.getMarkdown().trim(),
            excerpt: dom.articleSummaryTextarea.value.trim(),
            tagsName: dom.articleTagsInput.value.split(',').map(tag => tag.trim()).filter(Boolean),
            portfolioName: dom.articlePortfolioInput.value.trim()
        };
        if (!requestData.title || !requestData.content || !requestData.excerpt === 0) {
            showToast('标题、内容、摘要和标签均为必填项', 'warning');
            return;
        }
        dom.confirmPublishBtn.disabled = true;
        dom.confirmPublishBtn.innerHTML = `<i class="fas fa-spinner fa-spin"></i> 处理中...`;
        try {
            const response = await apiRequest('/api/article/publish', 'POST', requestData);
            if (response.ok) {
                const result = await response.json();
                showToast(isEditMode ? '文章更新成功！' : '文章发布成功！', 'success');
                window.location.href = `/article/${result.slug}`;
            } else {
                const errorData = await response.json();
                throw new Error(errorData.message || '服务器返回了错误');
            }
        } catch (error) {
            showToast(`提交失败: ${error.message}`, 'error');
            dom.confirmPublishBtn.disabled = false;
            dom.confirmPublishBtn.innerHTML = isEditMode ? '确定并更新' : '确定并发布';
        }
    }

    // AI 生成摘要
    async function handleGenerateSummary() {
        const btn = dom.generateAISummaryBtn;
        btn.disabled = true;
        btn.innerHTML = `<i class="fas fa-spinner fa-spin"></i> AI生成中...`;
        const plainTextContent = editorInstance.getMarkdown().replace(/<\/?[^>]+(>|$)/g, "").trim();
        if (!plainTextContent) {
            showToast("文章内容为空，无法生成摘要。", "warning");
            btn.disabled = false;
            btn.innerHTML = `<i class="fas fa-magic mr-1"></i>AI生成摘要`;
            return;
        }
        try {
            const response = await apiRequest('/api/ai/generate-summary-deepseek', 'POST', { textContent: plainTextContent });
            if (response.ok) {
                const data = await response.json();
                dom.articleSummaryTextarea.value = data.summary;
                updateSummaryCharCount();
            } else {
                const errorData = await response.json();
                showToast(errorData.summary || `AI服务出错 (HTTP ${response.status})`, 'error');
            }
        } catch (error) {
            showToast('生成摘要时发生网络错误。', 'error');
        } finally {
            btn.disabled = false;
            btn.innerHTML = `<i class="fas fa-magic mr-1"></i>AI生成摘要`;
        }
    }

    // =================================================================
    // 3. 事件委托绑定
    // =================================================================

    function bindDelegatedEvents() {

        // --- 委托到顶部操作栏 ---
        if (dom.topActionBar) {
            // 监听标题输入
            dom.topActionBar.addEventListener('input', function(event) {
                if (event.target === dom.articleTitleInput) {
                    onContentChange();
                }
            });

            // 监听点击事件
            dom.topActionBar.addEventListener('click', function(event) {
                // 使用 closest 查找被点击的按钮，即使点击的是图标也能正确响应
                if (event.target.closest('.publish-btn')) {
                    handleOpenPublishModal();
                }
            });
        }

        // --- 委托到发布设置模态框 ---
        if (dom.publishModalEl) {
            // 监听模态框内的点击事件
            dom.publishModalEl.addEventListener('click', function(event) {
                const button = event.target.closest('button');
                if (!button) return; // 如果点击的不是按钮，则忽略

                switch(button.id) {
                    case 'confirmPublishBtn':
                        handleConfirmSubmit();
                        break;
                    case 'generateAISummaryBtn':
                        handleGenerateSummary();
                        break;
                }
            });

            // 监听模态框内的输入事件
            dom.publishModalEl.addEventListener('input', function(event) {
                if (event.target === dom.articleSummaryTextarea) {
                    updateSummaryCharCount();
                }
            });
        }
    }


    // =================================================================
    // 4. UI 辅助函数 (保持不变)
    // =================================================================

    function apiRequest(endpoint, method, body) {
        return fetch(endpoint, {
            method: method.toUpperCase(),
            headers: { 'Content-Type': 'application/json', [csrfHeader]: csrfToken },
            body: JSON.stringify(body)
        });
    }

    function populateFormForEditMode() {
        if (!isEditMode) return;
        if (dom.topPublishBtn) dom.topPublishBtn.innerHTML = '<i class="fas fa-paper-plane"></i> 更新文章';
        if (dom.confirmPublishBtn) dom.confirmPublishBtn.innerHTML = '<i class="fas fa-check"></i> 确定并更新';
    }

    function updateStats() {
        if (!dom.wordCountEl || !dom.charCountEl || !editorInstance) return;
        const markdownText = editorInstance.getMarkdown();
        const plainText = markdownText.replace(/<[^>]*>?/gm, '').trim();
        dom.charCountEl.textContent = plainText.length;
        dom.wordCountEl.textContent = plainText ? plainText.split(/\s+/).filter(Boolean).length : 0;
    }

    function updateSummaryCharCount() {
        if (!dom.articleSummaryTextarea || !dom.summaryCharCount) return;
        const maxLength = parseInt(dom.articleSummaryTextarea.getAttribute('maxlength')) || 200;
        const currentLength = dom.articleSummaryTextarea.value.length;
        dom.summaryCharCount.textContent = `${currentLength}/${maxLength}`;
        dom.summaryCharCount.style.color = currentLength > maxLength ? 'red' : '';
    }

    function updateAutoSaveStatus(message, isSaving) {
        if (!dom.autosaveStatusEl) return;
        dom.autosaveStatusEl.textContent = message;
        const indicator = dom.autosaveStatusEl.previousElementSibling;
        if (indicator) {
            indicator.style.animation = isSaving ? 'pulse 2s infinite' : 'none';
        }
    }


    // =================================================================
    // 5. 启动应用
    // =================================================================
    initEditor();
    bindDelegatedEvents(); // <--- 调用新的事件委托绑定函数
    populateFormForEditMode();
    updateStats();
    updateSummaryCharCount();
});