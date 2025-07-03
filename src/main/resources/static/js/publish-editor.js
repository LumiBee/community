/**
 * =================================================================================
 * publish-editor.js (v4 - 独立 Toast 实现)
 *
 * 变更日志:
 * - v4: 完全重写 showToast 函数，使其不依赖任何 Bootstrap JS 插件，从而根除
 * "toast is not a function" 的错误。该函数现在是自包含的。
 * - v3: 修正了 Bootstrap 4 模态框的调用方法。
 * =================================================================================
 */
document.addEventListener("DOMContentLoaded", function () {
    console.log("DOM Content Loaded - Initializing Publish Editor v4 (Independent Toast)");

    // =================================================================
    // 1. 全局配置与状态管理
    // =================================================================

    const csrfTokenEl = document.querySelector('meta[name="_csrf"]');
    const csrfHeaderEl = document.querySelector('meta[name="_csrf_header"]');
    const csrfToken = csrfTokenEl ? csrfTokenEl.content : null;
    const csrfHeader = csrfHeaderEl ? csrfHeaderEl.content : null;

    if (!csrfToken || !csrfHeader) {
        console.error("CRITICAL: CSRF meta tags not found!");
        alert('页面安全凭证加载失败，请刷新重试');
    }

    let autoSaveTimer = null;
    let isContentDirty = false;
    let isSaving = false;
    const AUTO_SAVE_INTERVAL = 2000;
    let currentDraftId = null;

    const urlParams = new URLSearchParams(window.location.search);
    const draftIdFromUrl = urlParams.get('draftId');
    const articleIdInput = document.getElementById('articleId');
    const initialArticleId = articleIdInput ? articleIdInput.value : null;

    if (draftIdFromUrl) {
        currentDraftId = parseInt(draftIdFromUrl, 10);
        console.log(`草稿ID从URL中加载: ${currentDraftId}`);
    } else if (initialArticleId) {
        currentDraftId = parseInt(initialArticleId, 10);
        console.log(`文章/草稿ID从页面加载: ${currentDraftId}`);
    }

    const isEditMode = !!initialArticleId;

    const dom = {
        editorElement: document.getElementById('toastUiEditor'),
        articleTitleInput: document.getElementById('articleTitleInput'),
        wordCountEl: document.getElementById('wordCount'),
        charCountEl: document.getElementById('charCount'),
        autosaveStatusEl: document.querySelector('.auto-save-indicator span'),
        saveDraftBtn: document.getElementById('saveDraftBtn'),
        topPublishBtn: document.querySelector('.publish-btn'),
        publishModalEl: document.getElementById('publishSettingsModal'),
        confirmPublishBtn: document.getElementById('confirmPublishBtn'),
        articleTagsInput: document.getElementById('articleTags'),
        articlePortfolioInput: document.getElementById('articlePortfolio'),
        articleSummaryTextarea: document.getElementById('articleSummary'),
        summaryCharCount: document.getElementById('summaryCharCount'),
        generateAISummaryBtn: document.getElementById('generateAISummaryBtn'),
        initialContentData: document.getElementById('initial-content-data'),
    };

    let editorInstance = null;

    // =================================================================
    // 2. 核心功能函数定义
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
        editorInstance.on('change', updateStats);
    }

    function bindEvents() {
        dom.articleTitleInput?.addEventListener('input', onContentChange);
        editorInstance?.on('change', onContentChange);
        dom.saveDraftBtn?.addEventListener('click', handleManualSaveDraft);
        dom.topPublishBtn?.addEventListener('click', handleOpenPublishModal);
        dom.confirmPublishBtn?.addEventListener('click', handleConfirmSubmit);
        dom.generateAISummaryBtn?.addEventListener('click', handleGenerateSummary);
        dom.articleSummaryTextarea?.addEventListener('input', updateSummaryCharCount);
    }

    function onContentChange() {
        isContentDirty = true;
        updateAutoSaveStatus('内容已修改...', false);
        if (autoSaveTimer) clearTimeout(autoSaveTimer);
        autoSaveTimer = setTimeout(autoSaveDraft, AUTO_SAVE_INTERVAL);
    }

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
                // [修改] 如果是第一次保存（currentDraftId为空），则从后端返回的数据中获取新的ID
                if (!currentDraftId && result.articleId) {
                    currentDraftId = result.articleId;
                    console.log(`新草稿创建成功，ID: ${currentDraftId}`);
                    // 使用 history.replaceState 更新URL，不会创建新的历史记录
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

    function handleManualSaveDraft() {
        if (autoSaveTimer) clearTimeout(autoSaveTimer);
        autoSaveDraft();
    }

    function handleOpenPublishModal() {
        if (!editorInstance) {
            showToast('编辑器尚未准备就绪，请稍后重试。', 'error');
            return;
        }
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

    async function handleConfirmSubmit() {
        const requestData = {
            articleId: currentDraftId, // 传递草稿ID
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
        const apiUrl = '/api/article/publish';
        const method = 'POST';
        try {
            const response = await apiRequest(apiUrl, method, requestData);
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

    async function handleGenerateSummary() {
        const btn = dom.generateAISummaryBtn;
        const originalText = btn.innerHTML;
        btn.disabled = true;
        btn.innerHTML = `<i class="fas fa-spinner fa-spin"></i> AI生成中...`;
        dom.articleSummaryTextarea.value = '';
        dom.articleSummaryTextarea.placeholder = "正在请求AI服务，请稍候...";
        const plainTextContent = editorInstance.getMarkdown().replace(/<\/?[^>]+(>|$)/g, "").trim();
        if (!plainTextContent) {
            dom.articleSummaryTextarea.placeholder = "文章内容为空，无法生成摘要。";
            btn.disabled = false;
            btn.innerHTML = originalText;
            return;
        }
        try {
            const response = await apiRequest('/api/ai/generate-summary-deepseek', 'POST', { textContent: plainTextContent });
            if (response.ok) {
                const data = await response.json();
                dom.articleSummaryTextarea.value = data.summary;
                dom.articleSummaryTextarea.placeholder = "摘要已生成，可手动修改。";
            } else {
                const errorData = await response.json();
                showToast(errorData.summary || `AI服务出错 (HTTP ${response.status})`, 'error');
                dom.articleSummaryTextarea.placeholder = "AI摘要生成失败，请手动输入。";
            }
        } catch (error) {
            showToast('生成摘要时发生网络错误。', 'error');
            dom.articleSummaryTextarea.placeholder = "生成摘要出错，请手动输入。";
        } finally {
            btn.disabled = false;
            btn.innerHTML = originalText;
            updateSummaryCharCount();
        }
    }

    // =================================================================
    // 4. UI 辅助函数
    // =================================================================

    function apiRequest(endpoint, method, body) {
        if (!csrfToken || !csrfHeader) {
            showToast('安全凭证丢失，无法发送请求。', 'error');
            return Promise.reject(new Error("CSRF token is missing."));
        }
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
        updateSummaryCharCount();
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

    /**
     * 显示一个全局的 Toast 通知
     * 这个函数不依赖任何外部库 (如 Bootstrap JS), 纯原生 JavaScript 实现。
     * @param {string} message - 消息内容
     * @param {string} type - 'success', 'error', 'warning', 'info'
     */
    function showToast(message, type = 'info') {
        let container = document.getElementById('toast-container-main');
        if (!container) {
            container = document.createElement('div');
            container.id = 'toast-container-main';
            Object.assign(container.style, {
                position: 'fixed',
                top: '80px',
                right: '20px',
                zIndex: '2000',
                display: 'flex',
                flexDirection: 'column',
                gap: '10px'
            });
            document.body.appendChild(container);
        }

        const toastElement = document.createElement('div');
        const toastId = 'toast-' + Date.now();
        toastElement.id = toastId;

        const colors = {
            success: '#28a745',
            error: '#dc3545',
            warning: '#ffc107',
            info: '#17a2b8'
        };
        const icons = {
            success: 'fa-check-circle',
            error: 'fa-times-circle',
            warning: 'fa-exclamation-triangle',
            info: 'fa-info-circle'
        };

        const bgColor = colors[type] || colors.info;
        const iconClass = icons[type] || icons.info;

        Object.assign(toastElement.style, {
            backgroundColor: bgColor,
            color: 'white',
            padding: '15px 20px',
            borderRadius: '5px',
            boxShadow: '0 4px 10px rgba(0,0,0,0.1)',
            display: 'flex',
            alignItems: 'center',
            gap: '10px',
            opacity: '0',
            transform: 'translateX(100%)',
            transition: 'opacity 0.3s ease, transform 0.3s ease'
        });

        toastElement.innerHTML = `
            <i class="fas ${iconClass}" style="font-size: 1.2em;"></i>
            <span>${message}</span>
            <button style="background:none; border:none; color:white; font-size:1.2em; cursor:pointer; margin-left:auto; padding:0 5px;">&times;</button>
        `;

        container.appendChild(toastElement);

        // Animate in
        setTimeout(() => {
            toastElement.style.opacity = '1';
            toastElement.style.transform = 'translateX(0)';
        }, 10);

        // Close button logic
        toastElement.querySelector('button').addEventListener('click', () => {
            closeToast(toastElement);
        });

        // Auto-close logic
        const autoCloseTimeout = setTimeout(() => {
            closeToast(toastElement);
        }, 5000);

        toastElement.dataset.timeoutId = autoCloseTimeout.toString();

        function closeToast(el) {
            const timeoutId = parseInt(el.dataset.timeoutId, 10);
            clearTimeout(timeoutId);
            el.style.opacity = '0';
            el.style.transform = 'translateX(100%)';
            el.addEventListener('transitionend', () => {
                el.remove();
                if (container.children.length === 0) {
                    container.remove();
                }
            }, { once: true });
        }
    }


    // =================================================================
    // 5. 启动应用
    // =================================================================
    initEditor();
    bindEvents();
    populateFormForEditMode();
    updateStats();
    updateSummaryCharCount();
});