document.addEventListener("DOMContentLoaded", function() {
    console.log("DOM Content Loaded - Initializing publish editor");
    
    // --- DOM Element References ---
    const editorElement = document.getElementById('toastUiEditor');
    const mainNavbar = document.querySelector('nav.fixed-top');
    const topActionBar = document.querySelector('.top-action-bar.fixed-top');
    const publishPageWrapper = document.querySelector('.publish-page-wrapper');
    const bottomStatusBar = document.querySelector('.bottom-status-bar.fixed-bottom');
    const articleTitleInput = document.getElementById('articleTitleInput');
    const formArticleContent = document.getElementById('formArticleContent');
    const wordCountEl = document.getElementById('wordCount');
    const charCountEl = document.getElementById('charCount');

    // Modal related elements
    const topPublishBtnModalTrigger = document.querySelector('.top-action-bar .publish-btn');
    const publishSettingsModalElement = document.getElementById('publishSettingsModal');
    const confirmPublishBtn = document.getElementById('confirmPublishBtn');
    const articleSettingsForm = document.getElementById('articleSettingsForm');
    const articleCoverUpload = document.getElementById('articleCoverUpload');
    const coverPreview = document.getElementById('coverPreview');
    const customFileUploadArea = document.querySelector('#publishSettingsModal .custom-file-upload');
    const articleSummaryTextarea = document.getElementById('articleSummary');
    const summaryCharCount = document.getElementById('summaryCharCount');
    const initialContentDataElement = document.getElementById('initial-content-data');
    const generateAISummaryBtn = document.getElementById('generateAISummaryBtn');
    const articleTagsInput = document.getElementById('articleTags');
    const articlePortfolioInput = document.getElementById('articlePortfolio');
    
    // Debug element references
    console.log("Element references check:");
    console.log("- topPublishBtnModalTrigger:", topPublishBtnModalTrigger);
    console.log("- publishSettingsModalElement:", publishSettingsModalElement);
    console.log("- confirmPublishBtn:", confirmPublishBtn);
    console.log("- articleTagsInput:", articleTagsInput);
    console.log("- articlePortfolioInput:", articlePortfolioInput);
    console.log("- articleSummaryTextarea:", articleSummaryTextarea);

    let editorInstance = null;
    let bsModalInstance = null; // For Bootstrap 5 Native JS Modal Instance

    // --- Helper Functions ---
    function getElementHeight(element) {
        return element ? element.offsetHeight : 0;
    }

    function getElementPadding(element, side) {
        if (!element) return 0;
        const style = getComputedStyle(element);
        return parseFloat(style[side]) || 0;
    }

    //--- Dynamic Layout Adjustments ---
    function adjustLayout() {
        const currentMainNavbarHeight = getElementHeight(mainNavbar);

        if (topActionBar) {
            topActionBar.style.top = currentMainNavbarHeight + 'px';
        } else {
            console.warn("Top action bar not found for layout adjustment.");
        }
        return { mainNavbarHeight: currentMainNavbarHeight};
    }

    const { mainNavbarHeight, topActionBarHeight } = adjustLayout();
    window.addEventListener('resize', adjustLayout);


    // --- Initialize Toast UI Editor ---
    if (!editorElement) {
        console.error("CRITICAL: Toast UI Editor mount point (#toastUiEditor) not found!");
    } else {
        try {
            let editorHeight = '82vh'; // Default height
            if (mainNavbarHeight > 0 && topActionBarHeight > 0) {
                const bottomBarH = 35;
                const editorContentContainer = document.querySelector('.editor-content-container');
                const containerPaddingY = getElementPadding(editorContentContainer, 'paddingTop') + getElementPadding(editorContentContainer, 'paddingBottom');
                const availableHeight = window.innerHeight - (mainNavbarHeight + topActionBarHeight + bottomBarH + containerPaddingY);
                editorHeight = Math.max(250, availableHeight) + 'px'; // Min height 300px
                console.log(`Calculated editor height: ${editorHeight}`);
                console.log(`Calculated TUI_Editor height: ${editorHeight}, WindowH: ${window.innerHeight}, NavH: ${mainNavbarHeight}, ActionBarH: ${topActionBarHeight}, BottomBarH: ${bottomBarH}, ContainerPaddingY: ${containerPaddingY}`);
            } else {
                console.warn("Could not calculate dynamic editor height accurately, using default '70vh'. Used values - mainNavbarH:", mainNavbarHeight, "topActionBarH:", topActionBarHeight);
            }

            // console.log("Available plugins in toastui.Editor.plugin:", toastui.Editor.plugin);
            let editorPlugins = [];
            if (toastui.Editor.plugin && (toastui.Editor.plugin['code-syntax-highlight'] || toastui.Editor.plugin.codeSyntaxHighlight) && typeof hljs !== 'undefined') {
                editorPlugins.push(toastui.Editor.plugin['code-syntax-highlight'] || toastui.Editor.plugin.codeSyntaxHighlight);
                console.log("Code syntax highlight plugin added.");
            } else {
                console.warn("Code syntax highlight plugin or hljs not available.");
            }

            let resolvedInitialContent = '';
            if (initialContentDataElement) {
                resolvedInitialContent = initialContentDataElement.textContent || '';
            }

            editorInstance = new toastui.Editor({
                el: editorElement,
                height: editorHeight,
                initialEditType: 'markdown',
                previewStyle: 'vertical',
                initialValue: resolvedInitialContent || '',
                placeholder: '请输入文章内容...',
                usageStatistics: false,
                plugins: editorPlugins,
                hooks: {
                    addImageBlobHook: async (blob, callback) => {
                        console.log("addImageBlobHook triggered, blob:", blob);
                        // Placeholder for actual upload; using base64 for now.
                        // Replace with your actual upload logic to your server.
                        const reader = new FileReader();
                        reader.onload = (event) => {
                            console.log("File read as base64, calling callback.");
                            callback(event.target.result, blob.name);
                        };
                        reader.onerror = (error) => {
                            console.error("Error reading file for base64 preview:", error);
                            callback(null, "Error reading file"); // Or handle error appropriately
                        };
                        reader.readAsDataURL(blob);
                        return false; // Important to return false if you handle the upload
                    }
                }
            });
            console.log("Toast UI Editor initialized.", editorInstance);
        } catch (error) {
            console.error("Error initializing Toast UI Editor:", error);
        }
    }

    // --- Initialize Bootstrap Modal ---
    if (publishSettingsModalElement) {
        console.log("Attempting to initialize modal with publishSettingsModalElement");
        if (typeof bootstrap !== 'undefined' && bootstrap.Modal) {
            console.log("Using Bootstrap 5 Modal API");
            bsModalInstance = new bootstrap.Modal(publishSettingsModalElement);
            console.log("Bootstrap 5 Modal instance created:", bsModalInstance);
        } else if (typeof $ !== 'undefined' && typeof $(publishSettingsModalElement).modal === 'function') {
            console.log("Using jQuery Bootstrap 4 Modal API");
        } else {
            console.warn("Bootstrap Modal JS (neither v5 nor v4 via jQuery) not found, or modal element #publishSettingsModal missing.");
            console.log("bootstrap object:", typeof bootstrap, bootstrap);
            console.log("jQuery object:", typeof $, $);
        }
    }


    // --- Stats Update Function ---
    function updateStats() {
        if (!wordCountEl || !charCountEl || !editorInstance) return;
        try {
            const markdownText = editorInstance.getMarkdown();
            const plainText = markdownText.replace(/<[^>]*>?/gm, '').trim();
            charCountEl.textContent = plainText.length;
            wordCountEl.textContent = plainText ? plainText.split(/\s+/).filter(Boolean).length : 0;
        } catch (e) {
            console.error("Error updating stats:", e);
        }
    }

    if (editorInstance) {
        editorInstance.on('change', updateStats);
        updateStats(); // Initial stats
    } else {
        console.error("Cannot setup stats update: Editor instance is not available at initial setup.");
    }

    // --- Modal Interactions ---

    // Cover image preview
    if (customFileUploadArea && articleCoverUpload && coverPreview) {
        customFileUploadArea.addEventListener('click', () => articleCoverUpload.click());
        articleCoverUpload.addEventListener('change', function(event) {
            const file = event.target.files[0];
            if (file && file.type.startsWith('image/')) {
                const reader = new FileReader();
                reader.onload = (e) => {
                    coverPreview.src = e.target.result;
                    coverPreview.style.display = 'block';
                };
                reader.readAsDataURL(file);
            } else {
                coverPreview.src = '#';
                coverPreview.style.display = 'none';
                if (file) alert("请选择图片文件！");
            }
        });
    } else {
        console.warn("Elements for cover image preview not fully found.");
    }

    // Summary character count
    function updateSummaryCharCount() {
        if (articleSummaryTextarea && summaryCharCount) {
            const currentLength = articleSummaryTextarea.value.length;
            summaryCharCount.textContent = currentLength;
            const maxLength = parseInt(articleSummaryTextarea.getAttribute('maxlength')) || 200;
            articleSummaryTextarea.classList.toggle('is-invalid', currentLength > maxLength);
            summaryCharCount.style.color = currentLength > maxLength ? 'red' : '';
        }
    }
    if (articleSummaryTextarea) {
        articleSummaryTextarea.addEventListener('input', updateSummaryCharCount);
        updateSummaryCharCount(); // Initial call to set count if textarea has initial value
    } else {
        console.warn("Article summary textarea or char count element not found.");
    }

    // --- AI Summary Generation Function ---
    async function fetchAISummary(textContent, maxLength = 200) {
        if (!textContent.trim()) {
            console.warn("文章内容为空，无法生成摘要。");
            return null;
        }
        const originalPlaceholder = articleSummaryTextarea ? articleSummaryTextarea.placeholder : "";
        if (articleSummaryTextarea) articleSummaryTextarea.placeholder = "AI 正在生成摘要，请稍候...";
        // --- BEGIN CSRF Token Logic ---
        const csrfTokenMeta = document.querySelector('meta[name="_csrf"]');
        const csrfHeaderMeta = document.querySelector('meta[name="_csrf_header"]');

        const csrfToken = csrfTokenMeta ? csrfTokenMeta.getAttribute('content') : null;
        const csrfHeaderName = csrfHeaderMeta ? csrfHeaderMeta.getAttribute('content') : null;

        const requestHeaders = {
            'Content-Type': 'application/json'
            // CSRF token会在这里添加 (如果存在)
        };

        if (csrfToken && csrfHeaderName) {
            requestHeaders[csrfHeaderName] = csrfToken;
            console.log("CSRF Header added:", csrfHeaderName, csrfToken); // 用于调试
        } else {
            console.warn("CSRF token or header name not found in meta tags OR their content is null. POST request might be blocked by CSRF protection.");
            // 更详细的调试信息
            if (!csrfTokenMeta) console.warn("Meta tag with name '_csrf' NOT found.");
            if (!csrfHeaderMeta) console.warn("Meta tag with name '_csrf_header' NOT found.");
            if (csrfTokenMeta && !csrfToken) console.warn("Meta tag '_csrf' found, but its 'content' attribute is null or empty.");
            if (csrfHeaderMeta && !csrfHeaderName) console.warn("Meta tag '_csrf_header' found, but its 'content' attribute is null or empty.");
        }
        // --- END CSRF Token Logic ---
        try {
            const response = await fetch('/api/ai/generate-summary-deepseek', { // Ensure this URL is correct
                method: 'POST',
                headers: requestHeaders,
                body: JSON.stringify({ textContent: textContent, maxLength: maxLength })
            });
            if (!response.ok) {
                let errorMsg = `AI服务暂时不可用 (HTTP ${response.status})。请稍后重试或手动输入。`;
                try {
                    const errorData = await response.json();
                    if (errorData && errorData.summary) {
                        errorMsg = errorData.summary;
                    } else if (errorData && errorData.message) {
                        errorMsg = errorData.message;
                    } else if (response.status === 403) { // 更具体地处理403
                        errorMsg = "请求被拒绝 (403 Forbidden)。这可能是由于安全限制（如CSRF Token问题）。请确保页面已正确加载或尝试刷新。";
                    }
                    console.error(`AI Summary HTTP Error ${response.status}:`, errorData || "未能从响应中解析出错误JSON");
                } catch (e) {
                    console.error(`AI Summary HTTP Error ${response.status} (无法解析错误响应JSON):`, e);
                    if (response.status === 403) {
                        errorMsg = "请求被拒绝 (403 Forbidden)。安全策略可能阻止了此操作。";
                    }
                }
                return { summary: null, error: errorMsg };
            }
            const data = await response.json();
            return data.summary;
        } catch (error) {
            console.error("Error fetching AI summary:", error);
            return null;
        } finally {
            if (articleSummaryTextarea) articleSummaryTextarea.placeholder = originalPlaceholder || "请输入文章摘要...";
        }
    }


    // --- Top Publish Button (Trigger for Modal) ---
    if (topPublishBtnModalTrigger && articleTitleInput) {
        console.log("Setting up topPublishBtnModalTrigger click handler");
        topPublishBtnModalTrigger.addEventListener('click', function () {
            console.log("Top publish button (modal trigger) CLICKED!");
            if (!editorInstance) {
                showToast('编辑器正在加载，请稍候再试', 'error');
                console.error("Editor instance not available for modal trigger.");
                return;
            }

            const title = articleTitleInput.value;
            const contentMarkdown = editorInstance.getMarkdown();

            if (!title.trim()) {
                showToast('请先输入文章标题', 'warning');
                articleTitleInput.focus();
                return;
            }
            if (!contentMarkdown.trim()) {
                showToast('请先输入文章内容', 'warning');
                editorInstance.focus();
                return;
            }

            if (articleSummaryTextarea) {
                articleSummaryTextarea.value = ''; // 清空现有摘要，或根据需求保留
                articleSummaryTextarea.placeholder = "AI 正在准备摘要..."; // 初始提示
                updateSummaryCharCount();
            }

            // 1. 显示 Modal
            console.log("Attempting to show modal...");
            let modalShown = false;
            
            // 确保 bootstrap 对象存在
            if (typeof bootstrap === 'undefined' && typeof $ !== 'undefined') {
                console.log("Bootstrap object not found, trying jQuery modal");
                // 尝试使用 jQuery 显示模态框
                try {
                    $(publishSettingsModalElement).modal('show');
                    modalShown = true;
                    console.log("jQuery Modal shown successfully");
                } catch (e) {
                    console.error("Error showing jQuery modal:", e);
                }
            } else if (bsModalInstance) {
                try {
                    bsModalInstance.show();
                    modalShown = true;
                    console.log("Bootstrap Modal shown successfully");
                } catch (e) {
                    console.error("Error showing Bootstrap modal:", e);
                }
            } else if (typeof bootstrap !== 'undefined' && bootstrap.Modal) {
                try {
                    // 重新创建一个模态框实例
                    const newModal = new bootstrap.Modal(publishSettingsModalElement);
                    newModal.show();
                    bsModalInstance = newModal;
                    modalShown = true;
                    console.log("New Bootstrap Modal created and shown");
                } catch (e) {
                    console.error("Error creating and showing new Bootstrap modal:", e);
                }
            } else {
                console.error("Cannot show modal: No valid Bootstrap JS or jQuery modal instance/element available for #publishSettingsModal.");
                showToast("无法打开发布设置，请刷新页面重试", 'error');
            }

            // 2. 如果 Modal 成功（或尝试）显示，则异步获取 AI 摘要
            if (modalShown && articleSummaryTextarea && editorInstance) {
                const plainTextForAI = contentMarkdown
                    .replace(/<\/?[^>]+(>|$)/g, "")
                    .replace(/(\r\n|\n|\r)/gm, " ")
                    .replace(/\[(.*?)\]\(.*?\)/g, '$1')
                    .replace(/[`*#~_>+\-|=]/g, "")
                    .replace(/\s\s+/g, ' ').trim();

                if (plainTextForAI) {
                    const summaryMaxLength = parseInt(articleSummaryTextarea.getAttribute('maxlength')) || 100;
                    console.log("Fetching AI summary after modal is shown...");

                    // 使用 .then() 处理异步，避免阻塞 Modal 显示
                    fetchAISummary(plainTextForAI, summaryMaxLength)
                        .then(aiSummary => {
                            if (aiSummary !== null && articleSummaryTextarea) { // 检查 articleSummaryTextarea 仍然存在
                                articleSummaryTextarea.value = aiSummary;
                                console.log("AI summary auto-populated after modal shown:", aiSummary);
                            } else if (articleSummaryTextarea) {
                                articleSummaryTextarea.placeholder = "AI摘要生成失败，请手动输入。";
                                console.warn("AI summary generation failed or content was empty for AI (after modal shown).");
                            }
                            if (articleSummaryTextarea) updateSummaryCharCount(); // 再次更新字数
                        })
                        .catch(error => {
                            // fetchAISummary 内部已经处理了 alert 和 console.error
                            if (articleSummaryTextarea) articleSummaryTextarea.placeholder = "AI摘要生成出错，请手动输入。";
                            if (articleSummaryTextarea) updateSummaryCharCount();
                        });
                } else {
                    if (articleSummaryTextarea) articleSummaryTextarea.placeholder = "文章内容为空，无法自动生成摘要。";
                    if (articleSummaryTextarea) updateSummaryCharCount();
                }
            }
        });
        console.log("Event listener for modal trigger attached to .top-action-bar .publish-btn.");
    } else {
        console.error("Top publish button ('.top-action-bar .publish-btn') or articleTitleInput NOT FOUND for modal trigger setup.");
    }

    // --- Confirm Publish Button (Inside Modal) ---
    if (confirmPublishBtn && articleTitleInput && articleSettingsForm && formArticleContent) {
        console.log("Setting up confirmPublishBtn click handler");
        confirmPublishBtn.addEventListener('click', async function() {
            console.log("Confirm publish button clicked (RESTful mode).");

            try {
                // 数据收集
                // 从页面和模态框的各个输入框中收集最新数据
                const title = articleTitleInput.value;
                const content = editorInstance ? editorInstance.getMarkdown() : '';
                
                console.log("Checking required inputs:");
                console.log("- articleTagsInput:", articleTagsInput);
                console.log("- articlePortfolioInput:", articlePortfolioInput);
                console.log("- articleSummaryTextarea:", articleSummaryTextarea);
                
                if (!articleTagsInput) {
                    console.error("articleTagsInput element not found!");
                    showToast("页面加载异常，请刷新后重试", 'error');
                    return;
                }
                
                if (!articleSummaryTextarea) {
                    console.error("articleSummaryTextarea element not found!");
                    showToast("页面加载异常，请刷新后重试", 'error');
                    return;
                }
                
                const tagsValue = articleTagsInput.value;
                const portfolioName = articlePortfolioInput ? articlePortfolioInput.value : '';
                const excerpt = articleSummaryTextarea.value;
                
                console.log("Collected form data:", {
                    title,
                    contentLength: content.length,
                    tagsValue,
                    portfolioName,
                    excerpt
                });

                // 客户端数据校验
                if (!title.trim()) { showToast('请输入文章标题', 'warning'); articleTitleInput.focus(); return; }
                if (!content.trim()) { showToast('请输入文章内容', 'warning'); editorInstance.focus(); return; }
                if (!tagsValue.trim()) { showToast('请添加至少一个标签', 'warning'); articleTagsInput.focus(); return; }
                if (!excerpt.trim()) { showToast('请输入文章摘要', 'warning'); articleSummaryTextarea.focus(); return; }

                // 构建与后端 DTO 结构完全一致的 JSON 对象
                // 将标签字符串处理成字符串数组
                const tagsNameArray = tagsValue.split(',').map(tag => tag.trim()).filter(Boolean);

                const requestData = {
                    title: title,
                    content: content,
                    excerpt: excerpt,
                    tagsName: tagsNameArray,
                    portfolioName: portfolioName.trim() // portfolioName 是可选的
                };

                console.log("Final data to publish via AJAX:", requestData);

                // 防止重复点击
                confirmPublishBtn.disabled = true;
                confirmPublishBtn.innerHTML = '<i class="fas fa-spinner fa-spin mr-1"></i>发布中...';

                // 获取CSRF令牌
                const csrfToken = document.querySelector('meta[name="_csrf"]');
                const csrfHeader = document.querySelector('meta[name="_csrf_header"]');
                
                if (!csrfToken || !csrfHeader) {
                    console.warn("CSRF token or header meta tag not found");
                }
                
                const headers = {
                    'Content-Type': 'application/json'
                };
                
                if (csrfToken && csrfHeader) {
                    headers[csrfHeader.getAttribute('content')] = csrfToken.getAttribute('content');
                }
                
                console.log("Request headers:", headers);

                try {
                    // 使用 fetch API 发起异步的 AJAX POST 请求
                    const response = await fetch('/api/article/publish', { // 指向正确的 RESTful API 端点
                        method: 'POST',
                        headers: headers,
                        body: JSON.stringify(requestData) // 将 JS 对象转换为 JSON 字符串
                    });

                    console.log("Server response status:", response.status);
                    
                    // 处理服务器的响应
                    if (response.ok) { // HTTP 状态码为 2xx
                        const newArticle = await response.json();
                        console.log("Publish successful, article data:", newArticle);
                        
                        // 关闭模态框
                        if (bsModalInstance) {
                            bsModalInstance.hide();
                        } else if (typeof $ !== 'undefined' && typeof $(publishSettingsModalElement).modal === 'function') {
                            $(publishSettingsModalElement).modal('hide');
                        }
                        
                        // 直接跳转到文章页面
                        window.location.href = `/article/${newArticle.slug}`;
                    } else {
                        // 处理服务器返回的错误信息
                        try {
                            const errorData = await response.json();
                            console.error("Server error response:", errorData);
                            showToast(`发布失败: ${errorData.message || '服务器处理请求时出错'}`, 'error');
                        } catch (jsonError) {
                            console.error("Failed to parse error response:", jsonError);
                            showToast(`发布失败，服务器返回错误码: ${response.status}`, 'error');
                        }
                    }
                } catch (networkError) {
                    console.error('发布请求时遇到网络错误:', networkError);
                    showToast('网络连接异常，请检查网络后重试', 'error');
                } finally {
                    // 无论成功失败，都恢复按钮的可用状态
                    confirmPublishBtn.disabled = false;
                    confirmPublishBtn.innerHTML = '<i class="fas fa-check mr-1"></i>确定并发布';
                }
            } catch (error) {
                console.error("Unexpected error in publish button handler:", error);
                showToast("发布过程出现异常，请刷新页面重试", 'error');
                confirmPublishBtn.disabled = false;
                confirmPublishBtn.innerHTML = '<i class="fas fa-check mr-1"></i>确定并发布';
            }
        });
        console.log("RESTful event listener attached to #confirmPublishBtn.");
    } else {
        console.error("Confirm publish button or other critical elements for final publish (articleTitleInput, articleSettingsForm, formArticleContent) NOT FOUND.");
    }

    // Toast notification function
    function showToast(message, type = 'success') {
        // 检查是否已有toast容器
        let toastContainer = document.querySelector('.toast-container');
        
        if (!toastContainer) {
            toastContainer = document.createElement('div');
            toastContainer.className = 'toast-container position-fixed top-0 end-0 p-3';
            document.body.appendChild(toastContainer);
        }
        
        // 获取图标
        const icon = getToastIcon(type);
        
        // 创建toast元素
        const toastId = 'toast-' + Date.now();
        const toastHTML = `
            <div id="${toastId}" class="toast" role="alert" aria-live="assertive" aria-atomic="true">
                <div class="toast-header bg-${getToastBgClass(type)} text-white">
                    <i class="${icon} me-2"></i>
                    <strong class="me-auto">${getToastTitle(type)}</strong>
                    <button type="button" class="btn-close btn-close-white" data-bs-dismiss="toast" aria-label="关闭"></button>
                </div>
                <div class="toast-body">
                    ${message}
                </div>
            </div>
        `;
        
        toastContainer.insertAdjacentHTML('beforeend', toastHTML);
        
        const toastElement = document.getElementById(toastId);
        
        // 添加自定义样式
        toastElement.style.minWidth = '300px';
        toastElement.style.boxShadow = '0 0.5rem 1rem rgba(0, 0, 0, 0.15)';
        
        // 尝试使用Bootstrap 5的方式创建Toast
        if (typeof bootstrap !== 'undefined' && bootstrap.Toast) {
            const toast = new bootstrap.Toast(toastElement, {
                delay: 2000, // 缩短到2秒
                animation: true,
                autohide: true
            });
            toast.show();
        } 
        // 回退到Bootstrap 4 (jQuery)
        else if (typeof $ !== 'undefined' && typeof $(toastElement).toast === 'function') {
            $(toastElement).toast({
                delay: 2000, // 缩短到2秒
                animation: true,
                autohide: true
            });
            $(toastElement).toast('show');
        }
        // 最后的回退方案：简单的计时器
        else {
            toastElement.style.display = 'block';
            setTimeout(() => {
                toastElement.style.opacity = '0';
                setTimeout(() => toastElement.remove(), 300);
            }, 2000); // 缩短到2秒
        }
        
        // 自动移除toast元素
        toastElement.addEventListener('hidden.bs.toast', function() {
            this.remove();
        });
    }
    
    /**
     * 获取Toast的背景类名
     */
    function getToastBgClass(type) {
        switch (type) {
            case 'success': return 'success';
            case 'error': return 'danger';
            case 'warning': return 'warning';
            default: return 'info';
        }
    }
    
    /**
     * 获取Toast的图标
     */
    function getToastIcon(type) {
        switch (type) {
            case 'success': return 'fas fa-check-circle';
            case 'error': return 'fas fa-exclamation-circle';
            case 'warning': return 'fas fa-exclamation-triangle';
            default: return 'fas fa-info-circle';
        }
    }
    
    /**
     * 获取Toast的标题
     */
    function getToastTitle(type) {
        switch (type) {
            case 'success': return '成功';
            case 'error': return '错误';
            case 'warning': return '警告';
            default: return '提示';
        }
    }
});