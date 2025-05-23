document.addEventListener("DOMContentLoaded", function() {
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
        if (typeof bootstrap !== 'undefined' && bootstrap.Modal) {
            bsModalInstance = new bootstrap.Modal(publishSettingsModalElement);
            console.log("Bootstrap 5 Modal instance created.");
        } else if (typeof $ !== 'undefined' && typeof $(publishSettingsModalElement).modal === 'function') {
            console.log("jQuery and Bootstrap 4 Modal JS found. Will use jQuery for modal.");
        } else {
            console.warn("Bootstrap Modal JS (neither v5 nor v4 via jQuery) not found, or modal element #publishSettingsModal missing.");
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
        topPublishBtnModalTrigger.addEventListener('click', function () {
            console.log("Top publish button (modal trigger) CLICKED!");
            if (!editorInstance) {
                alert('编辑器尚未准备好，请稍候。');
                console.error("Editor instance not available for modal trigger.");
                return;
            }

            const title = articleTitleInput.value;
            const contentMarkdown = editorInstance.getMarkdown();

            if (!title.trim()) {
                alert('请输入文章标题！');
                articleTitleInput.focus();
                return;
            }
            if (!contentMarkdown.trim()) {
                alert('请输入文章内容！');
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
            if (bsModalInstance) {
                bsModalInstance.show();
                modalShown = true;
                console.log("Bootstrap JS Modal 'show' command sent.");
            } else if (typeof $ !== 'undefined' && typeof $(publishSettingsModalElement).modal === 'function' && $(publishSettingsModalElement).length) {
                $(publishSettingsModalElement).modal('show');
                modalShown = true;
                console.log("jQuery Modal 'show' command sent.");
            } else {
                console.error("Cannot show modal: No valid Bootstrap JS or jQuery modal instance/element available for #publishSettingsModal.");
                alert("无法打开文章设置，Modal 组件似乎未正确初始化或页面元素缺失。");
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
        confirmPublishBtn.addEventListener('click', function() {
            console.log("Confirm publish button in modal clicked.");
            if (!editorInstance) {
                alert("编辑器实例丢失，无法发布。");
                return;
            }

            const title = articleTitleInput.value;
            const contentMarkdown = editorInstance.getMarkdown();
            const settingsData = {};
            const formData = new FormData(articleSettingsForm);
            formData.forEach((value, key) => { settingsData[key] = value; });

            const coverFile = articleCoverUpload ? articleCoverUpload.files[0] : null;
            if (coverFile) settingsData.coverImageFileName = coverFile.name; // Or handle file upload separately

            // Validation for modal fields
            if (!settingsData.tags || !settingsData.tags.trim()) { alert('请输入至少一个标签！'); document.getElementById('articleTags')?.focus(); return; }
            if (!settingsData.summary || !settingsData.summary.trim()) { alert('请输入文章摘要！'); document.getElementById('articleSummary')?.focus(); return; }

            formArticleContent.value = contentMarkdown;

            const publishForm = document.getElementById('publishForm');

            function addHiddenInput(form, name, value) {
                let input = form.querySelector(`input[type="hidden"][name="${name}"]`);
                if (!input) {
                    input = document.createElement('input');
                    input.type = 'hidden';
                    input.name = name;
                    form.appendChild(input);
                }
                input.value = value;
            }

            // 将 title, tags, summary, portfolio 添加到 publishForm 中
            addHiddenInput(publishForm, 'title', title);
            addHiddenInput(publishForm, 'tags', settingsData.tags); // 后端期望参数名为 "tags"
            addHiddenInput(publishForm, 'summary', settingsData.summary); // 后端期望参数名为 "summary" (对应你的 excerpt)
            if (settingsData.portfolio) { // portfolio 是可选的
                addHiddenInput(publishForm, 'portfolio', settingsData.portfolio);
            }

            const finalArticleData = {
                title: title,
                content: contentMarkdown,
                tags: settingsData.tags.split(',').map(tag => tag.trim()).filter(tag => tag.length > 0),
                portfolio: settingsData.portfolio || "", // Ensure portfolio exists
                summary: settingsData.summary
            };
            console.log("Final data to publish:", finalArticleData);

            document.getElementById('publishForm').submit();

            // Close Modal
            if (bsModalInstance) {
                bsModalInstance.hide();
            } else if (typeof $ !== 'undefined' && typeof $(publishSettingsModalElement).modal === 'function') {
                $(publishSettingsModalElement).modal('hide');
            }
        });
        console.log("Event listener attached to #confirmPublishBtn.");
    } else {
        console.error("Confirm publish button or other critical elements for final publish (articleTitleInput, articleSettingsForm, formArticleContent) NOT FOUND.");
    }
});