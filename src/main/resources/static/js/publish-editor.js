/**
 * =================================================================================
 * publish-editor.js
 *
 * 功能说明:
 * - 统一管理文章发布页面的所有交互功能。
 * - 包含编辑器初始化、手动/自动保存草稿、发布流程、AI摘要、字数统计等。
 * - 解决了之前遇到的所有CSRF令牌和变量作用域问题。
 * - 这份代码旨在可直接替换使用。
 * =================================================================================
 */
document.addEventListener("DOMContentLoaded", function () {
    console.log("DOM Content Loaded - Initializing Full-Featured Publish Editor");

    // =================================================================
    // 1. 全局配置与状态管理
    // =================================================================

    // --- CSRF 安全令牌 ---
    const csrfTokenEl = document.querySelector('meta[name="_csrf"]');
    const csrfHeaderEl = document.querySelector('meta[name="_csrf_header"]');
    const csrfToken = csrfTokenEl ? csrfTokenEl.content : null;
    const csrfHeader = csrfHeaderEl ? csrfHeaderEl.content : null;

    if (!csrfToken || !csrfHeader) {
        console.error("CRITICAL: CSRF meta tags not found! Requests will likely fail.");
        showToast('页面安全凭证加载失败，请刷新重试', 'error');
    }

    // --- 自动保存状态变量 ---
    let autoSaveTimer = null;
    let isContentDirty = false;
    let currentDraftId = null; // 用于存储当前草稿的ID
    let isSaving = false;
    const AUTO_SAVE_INTERVAL = 8000; // 自动保存间隔，设置为8秒

    // --- DOM 元素缓存 ---
    const dom = {
        editorElement: document.getElementById('toastUiEditor'),
        articleTitleInput: document.getElementById('articleTitleInput'),
        wordCountEl: document.getElementById('wordCount'),
        charCountEl: document.getElementById('charCount'),
        autosaveStatusEl: document.querySelector('.auto-save-indicator span'),

        // 按钮
        saveDraftBtn: document.querySelector('.btn-outline-secondary'), // “存为草稿”或“草稿箱”按钮
        topPublishBtn: document.querySelector('.top-action-bar .publish-btn'),

        // 模态框相关
        publishModalEl: document.getElementById('publishSettingsModal'),
        confirmPublishBtn: document.getElementById('confirmPublishBtn'),
        articleTagsInput: document.getElementById('articleTags'),
        articlePortfolioInput: document.getElementById('articlePortfolio'),
        articleSummaryTextarea: document.getElementById('articleSummary'),
        summaryCharCount: document.getElementById('summaryCharCount'),
        generateAISummaryBtn: document.getElementById('generateAISummaryBtn'),

        // 用于加载草稿
        initialContentData: document.getElementById('initial-content-data'),
    };

    // --- 实例存储 ---
    let editorInstance = null;
    let publishModalInstance = null;


    // =================================================================
    // 2. 核心功能函数定义
    // =================================================================

    /**
     * 初始化编辑器
     */
    function initEditor() {
        if (!dom.editorElement) {
            console.error("严重错误: 未找到编辑器挂载点 (#toastUiEditor)!");
            return;
        }

        const editorPlugins = [];
        if (toastui.Editor.plugin?.['code-syntax-highlight'] && typeof hljs !== 'undefined') {
            editorPlugins.push([toastui.Editor.plugin['code-syntax-highlight'], { hljs }]);
        }

        const initialContent = dom.initialContentData ? dom.initialContentData.textContent : '';

        editorInstance = new toastui.Editor({
            el: dom.editorElement,
            height: 'calc(100vh - 180px)', // 动态计算高度
            initialEditType: 'markdown',
            previewStyle: 'vertical',
            initialValue: initialContent,
            placeholder: '在这里开始你的创作吧...',
            usageStatistics: false,
            plugins: editorPlugins,
        });
    }

    /**
     * 绑定所有事件监听器
     */
    function bindEvents() {
        // 自动保存触发
        dom.articleTitleInput?.addEventListener('input', onContentChange);
        editorInstance?.on('change', onContentChange);

        // 手动保存草稿
        dom.saveDraftBtn?.addEventListener('click', handleManualSaveDraft);

        // 打开“发布”模态框
        dom.topPublishBtn?.addEventListener('click', handleOpenPublishModal);

        // 确认发布
        dom.confirmPublishBtn?.addEventListener('click', handleConfirmPublish);

        // AI 生成摘要
        dom.generateAISummaryBtn?.addEventListener('click', handleGenerateSummary);

        // 摘要字数统计
        dom.articleSummaryTextarea?.addEventListener('input', updateSummaryCharCount);
    }

    /**
     * 内容变化处理器，用于触发“防抖”的自动保存
     */
    function onContentChange() {
        isContentDirty = true;
        updateAutoSaveStatus('内容已修改...', false);

        if (autoSaveTimer) {
            clearTimeout(autoSaveTimer);
        }

        autoSaveTimer = setTimeout(autoSaveDraft, AUTO_SAVE_INTERVAL);
    }

    /**
     * 异步API请求的封装
     * @param {string} endpoint - API的URL路径
     * @param {object} body - 请求体数据
     * @returns {Promise<Response>} - 返回 fetch 的 Promise 对象
     */
    function apiPost(endpoint, body) {
        if (!csrfToken || !csrfHeader) {
            showToast('安全凭证丢失，无法发送请求。', 'error');
            return Promise.reject(new Error("CSRF token is missing."));
        }

        return fetch(endpoint, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                [csrfHeader]: csrfToken
            },
            body: JSON.stringify(body)
        });
    }

    /**
     * 自动保存草稿的核心逻辑
     */
    async function autoSaveDraft() {
        if (!isContentDirty || isSaving) {
            return;
        }

        isSaving = true;
        updateAutoSaveStatus('正在自动保存...', true);

        const title = dom.articleTitleInput.value.trim();
        const content = editorInstance.getMarkdown();

        if (!title && !content) {
            isSaving = false;
            updateAutoSaveStatus('', false); // 如果都为空，不显示状态
            return;
        }

        const requestData = {
            articleId: currentDraftId,
            title: title || "无标题草稿",
            content: content,
        };

        try {
            const response = await apiPost('/api/article/save-draft', requestData); // 使用封装的apiPost

            if (response.ok) {
                const result = await response.json();
                isContentDirty = false;

                if (!currentDraftId && result.articleId) {
                    currentDraftId = result.articleId;
                    // 使用 history.replaceState 更新URL，不会创建新的历史记录
                    const newUrl = `${window.location.pathname}?draftId=${currentDraftId}`;
                    history.replaceState({ path: newUrl }, '', newUrl);
                }

                const now = new Date();
                updateAutoSaveStatus(`已于 ${now.getHours()}:${now.getMinutes().toString().padStart(2, '0')} 保存`, false);
            } else {
                updateAutoSaveStatus('自动保存失败', false);
            }
        } catch (error) {
            console.error("自动保存失败:", error);
            updateAutoSaveStatus('保存异常', false);
        } finally {
            isSaving = false;
        }
    }

    /**
     * 手动保存草稿的处理器
     */
    async function handleManualSaveDraft() {
        const btn = dom.saveDraftBtn;
        btn.disabled = true;
        btn.innerHTML = `<i class="fas fa-spinner fa-spin"></i> 保存中...`;

        // 强制执行一次自动保存的逻辑
        await autoSaveDraft();

        // 无论成功失败，恢复按钮状态
        btn.disabled = false;
        btn.innerHTML = `<i class="fas fa-folder"></i> 存为草稿`;
    }

    /**
     * 打开发布设置模态框的处理器
     */
    function handleOpenPublishModal() {
        const title = dom.articleTitleInput.value.trim();
        const content = editorInstance.getMarkdown().trim();

        if (!title || !content) {
            showToast('发布前，请先填写标题和内容。', 'warning');
            return;
        }

        if (publishModalInstance) {
            publishModalInstance.show();
            // 异步生成摘要，不阻塞UI
            handleGenerateSummary();
        } else {
            showToast('无法打开发布设置，请刷新页面。', 'error');
        }
    }

    /**
     * 最终确认发布的处理器
     */
    async function handleConfirmPublish() {
        dom.confirmPublishBtn.disabled = true;
        dom.confirmPublishBtn.innerHTML = `<i class="fas fa-spinner fa-spin"></i> 发布中...`;

        const requestData = {
            title: dom.articleTitleInput.value.trim(),
            content: editorInstance.getMarkdown().trim(),
            excerpt: dom.articleSummaryTextarea.value.trim(),
            tagsName: dom.articleTagsInput.value.split(',').map(tag => tag.trim()).filter(Boolean),
            portfolioName: dom.articlePortfolioInput.value.trim()
        };

        // 发布前的数据校验
        if (!requestData.title || !requestData.content || requestData.tagsName.length === 0 || !requestData.excerpt) {
            showToast('标题、内容、标签和摘要均为必填项。', 'warning');
            dom.confirmPublishBtn.disabled = false;
            dom.confirmPublishBtn.innerHTML = `<i class="fas fa-check"></i> 确定并发布`;
            return;
        }

        try {
            const response = await apiPost('/api/article/publish', requestData);
            if (response.ok) {
                const newArticle = await response.json();
                showToast('文章发布成功！正在跳转...', 'success');
                window.location.href = `/article/${newArticle.slug}`;
            } else {
                const errorData = await response.json();
                showToast(`发布失败: ${errorData.message || '服务器错误'}`, 'error');
            }
        } catch (error) {
            console.error("发布文章时出错:", error);
            showToast('发布时发生网络错误。', 'error');
        } finally {
            dom.confirmPublishBtn.disabled = false;
            dom.confirmPublishBtn.innerHTML = `<i class="fas fa-check"></i> 确定并发布`;
        }
    }

    /**
     * 调用AI服务生成摘要并填充
     */
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
            const response = await apiPost('/api/ai/generate-summary-deepseek', { textContent: plainTextContent });
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
            console.error("请求AI摘要时出错:", error);
            showToast('生成摘要时发生网络错误。', 'error');
            dom.articleSummaryTextarea.placeholder = "生成摘要出错，请手动输入。";
        } finally {
            btn.disabled = false;
            btn.innerHTML = originalText;
            updateSummaryCharCount();
        }
    }

    // =================================================================
    // 3. UI 辅助函数
    // =================================================================

    /**
     * 更新字数和字符数统计
     */
    function updateStats() {
        if (!dom.wordCountEl || !dom.charCountEl || !editorInstance) return;
        const markdownText = editorInstance.getMarkdown();
        const plainText = markdownText.replace(/<[^>]*>?/gm, '').trim();
        dom.charCountEl.textContent = plainText.length;
        dom.wordCountEl.textContent = plainText ? plainText.split(/\s+/).filter(Boolean).length : 0;
    }

    /**
     * 更新摘要输入框的字数统计
     */
    function updateSummaryCharCount() {
        if (!dom.articleSummaryTextarea || !dom.summaryCharCount) return;
        const maxLength = parseInt(dom.articleSummaryTextarea.getAttribute('maxlength')) || 200;
        const currentLength = dom.articleSummaryTextarea.value.length;
        dom.summaryCharCount.textContent = `${currentLength}/${maxLength}`;
        dom.summaryCharCount.style.color = currentLength > maxLength ? 'red' : '';
    }

    /**
     * 更新自动保存状态的UI提示
     * @param {string} message - 要显示的消息
     * @param {boolean} isSaving - 是否显示“正在保存”的脉冲动画
     */
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
     * @param {string} message - 消息内容
     * @param {string} type - 'success', 'error', 'warning', 'info'
     */
    function showToast(message, type = 'info') {
        let toastContainer = document.querySelector('.toast-container-wrapper');
        if (!toastContainer) {
            toastContainer = document.createElement('div');
            toastContainer.className = 'toast-container-wrapper';
            document.body.appendChild(toastContainer);
        }

        const config = {
            success: { title: '成功', icon: 'check-circle', bg: 'success' },
            error: { title: '错误', icon: 'times-circle', bg: 'danger' },
            warning: { title: '警告', icon: 'exclamation-triangle', bg: 'warning' },
            info: { title: '提示', icon: 'info-circle', bg: 'info' }
        };
        const toastConfig = config[type] || config.info;

        const toastId = 'toast-' + Date.now();
        const toastHTML = `
            <div id="${toastId}" class="toast align-items-center text-white bg-${toastConfig.bg} border-0" role="alert" aria-live="assertive" aria-atomic="true">
              <div class="d-flex">
                <div class="toast-body">
                  <i class="fas fa-${toastConfig.icon} me-2"></i>
                  ${message}
                </div>
                <button type="button" class="btn-close btn-close-white me-2 m-auto" data-bs-dismiss="toast" aria-label="Close"></button>
              </div>
            </div>
        `;
        toastContainer.insertAdjacentHTML('beforeend', toastHTML);

        const toastElement = document.getElementById(toastId);
        const toast = new bootstrap.Toast(toastElement, { delay: 3000 });
        toast.show();
        toastElement.addEventListener('hidden.bs.toast', () => toastElement.remove());
    }


    // =================================================================
    // 4. 启动应用
    // =================================================================

    // --- 初始化编辑器和模态框 ---
    initEditor();
    if (dom.publishModalEl && typeof bootstrap !== 'undefined') {
        publishModalInstance = new bootstrap.Modal(dom.publishModalEl);
    }

    // --- 绑定事件 ---
    bindEvents();

    // --- 页面加载时，检查URL是否有 draftId，如果有，则加载该草稿 ---
    const urlParams = new URLSearchParams(window.location.search);
    const draftIdFromUrl = urlParams.get('draftId');
    if (draftIdFromUrl) {
        currentDraftId = parseInt(draftIdFromUrl, 10);
        // 这里可以添加一个函数，用于根据ID获取草稿内容并填充到编辑器中
        // loadDraftContent(currentDraftId);
    }

    // --- 初始化UI ---
    updateStats();
    updateSummaryCharCount();
});