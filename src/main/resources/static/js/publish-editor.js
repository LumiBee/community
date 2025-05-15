document.addEventListener("DOMContentLoaded", function() {
    // --- DOM Element References ---
    const editorElement = document.getElementById('toastUiEditor');
    const mainNavbar = document.querySelector('nav.fixed-top'); // Main navigation bar
    const topActionBar = document.querySelector('.top-action-bar.fixed-top'); // Bar with title input
    const publishPageWrapper = document.querySelector('.publish-page-wrapper'); // Main content wrapper
    const bottomStatusBar = document.querySelector('.bottom-status-bar.fixed-bottom'); // Bottom status bar
    const articleTitleInput = document.getElementById('articleTitleInput'); // Title input in top action bar
    const formArticleContent = document.getElementById('formArticleContent'); // Hidden input for editor's markdown
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


    let editorInstance = null;
    let bsModalInstance = null;

    // --- Helper Functions ---
    function getElementHeight(element) {
        return element ? element.offsetHeight : 0;
    }

    function getElementPadding(element, side) {
        return element ? (parseFloat(getComputedStyle(element)[side]) || 0) : 0;
    }

    // --- Dynamic Layout Adjustments ---
    function adjustLayout() {
        const mainNavbarHeight = getElementHeight(mainNavbar);
        let topActionBarHeight = 0;

        if (topActionBar) {
            topActionBar.style.top = mainNavbarHeight + 'px';
            topActionBarHeight = getElementHeight(topActionBar);
        } else {
            console.warn("Top action bar not found for layout adjustment.");
        }

        if (publishPageWrapper) {
            const totalTopOffset = mainNavbarHeight + topActionBarHeight;
            publishPageWrapper.style.paddingTop = totalTopOffset + 'px';

            const bottomStatusBarHeight = getElementHeight(bottomStatusBar);
            publishPageWrapper.style.paddingBottom = bottomStatusBarHeight + 'px';
            console.log(`Layout Adjusted: NavbarH=${mainNavbarHeight}, ActionBarH=${topActionBarHeight}, TopOffset=${totalTopOffset}, BottomBarH=${bottomStatusBarHeight}`);
        } else {
            console.warn("Publish page wrapper not found for layout adjustment.");
        }
        return { mainNavbarHeight, topActionBarHeight }; // Return heights for editor calculation
    }

    const { mainNavbarHeight, topActionBarHeight } = adjustLayout(); // Adjust layout on load
    window.addEventListener('resize', adjustLayout); // Optional: Adjust on resize if heights can change


    // --- Initialize Toast UI Editor ---
    if (!editorElement) {
        console.error("CRITICAL: Toast UI Editor mount point (#toastUiEditor) not found!");
    } else {
        try {
            let editorHeight = '82vh'; // Default height
            if (publishPageWrapper && mainNavbarHeight > 0 && topActionBarHeight > 0) {
                const bottomBarH = getElementHeight(bottomStatusBar);
                const editorContentContainer = document.querySelector('.editor-content-container');
                const containerPaddingY = getElementPadding(editorContentContainer, 'paddingTop') + getElementPadding(editorContentContainer, 'paddingBottom');
                const availableHeight = window.innerHeight - (mainNavbarHeight + topActionBarHeight + bottomBarH + containerPaddingY);
                editorHeight = Math.max(300, availableHeight) + 'px'; // Min height 300px
                console.log(`Calculated editor height: ${editorHeight}`);
            } else {
                console.warn("Could not calculate dynamic editor height accurately, using default.", editorHeight);
            }

            console.log("Available plugins in toastui.Editor.plugin:", toastui.Editor.plugin);
            let editorPlugins = [];
            if (toastui.Editor.plugin && toastui.Editor.plugin['code-syntax-highlight']) {
                editorPlugins.push(toastui.Editor.plugin['code-syntax-highlight']);
            } else if (toastui.Editor.plugin && toastui.Editor.plugin.codeSyntaxHighlight) {
                editorPlugins.push(toastui.Editor.plugin.codeSyntaxHighlight);
            } else {
                console.warn("Code syntax highlight plugin not found.");
            }
            if (editorPlugins.length > 0 && typeof hljs !== 'undefined') {
                console.log("hljs is available globally for code syntax highlight plugin.");
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
                placeholder: '请输入内容...',
                usageStatistics: false,
                plugins: editorPlugins,
                hooks: {
                    addImageBlobHook: async (blob, callback) => {
                        const reader = new FileReader();
                        reader.onload = (event) => {
                            callback(event.target.result, blob.name); // Temporary base64 solution
                        };
                        reader.readAsDataURL(blob);
                        return false;
                    }
                }
            });
            console.log("Toast UI Editor initialized.", editorInstance);
        } catch (error) {
            console.error("Error initializing Toast UI Editor:", error);
        }
    }

    // --- Initialize Bootstrap Modal (if using Bootstrap 5 native JS) ---
    if (publishSettingsModalElement && typeof bootstrap !== 'undefined' && bootstrap.Modal) {
        bsModalInstance = new bootstrap.Modal(publishSettingsModalElement);
        console.log("Bootstrap Modal instance created.");
    } else if (typeof $ !== 'undefined' && publishSettingsModalElement) {
        console.log("jQuery found, will use jQuery for modal if Bootstrap JS modal not available.");
    } else {
        console.warn("Bootstrap Modal JS or jQuery not found, or modal element missing.");
    }


    // --- Stats Update Function ---
    function updateStats() {
        if (!wordCountEl || !charCountEl || !editorInstance) return;
        const markdownText = editorInstance.getMarkdown();
        const plainText = markdownText.replace(/<[^>]*>?/gm, '').trim();
        charCountEl.textContent = plainText.length;
        wordCountEl.textContent = plainText ? plainText.split(/\s+/).filter(Boolean).length : 0;
    }

    if (editorInstance) {
        editorInstance.on('change', updateStats);
        updateStats(); // Initial stats
    } else {
        console.error("Cannot setup stats update: Editor instance is not available.");
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
    }

    // Summary character count
    if (articleSummaryTextarea && summaryCharCount) {
        articleSummaryTextarea.addEventListener('input', function() {
            const currentLength = this.value.length;
            summaryCharCount.textContent = currentLength;
            summaryCharCount.style.color = currentLength > 100 ? 'red' : '';
        });
        // Initial count for summary
        summaryCharCount.textContent = articleSummaryTextarea.value.length;
    }


    // --- Top Publish Button (Trigger for Modal) ---
    if (topPublishBtnModalTrigger) {
        topPublishBtnModalTrigger.addEventListener('click', function() {
            console.log("Top publish button (modal trigger) clicked!");
            if (!editorInstance) {
                alert('编辑器尚未准备好，请稍候。');
                console.error("Editor instance not available for modal trigger.");
                return;
            }
            const title = articleTitleInput ? articleTitleInput.value : '';
            const contentMarkdown = editorInstance.getMarkdown();

            if (!title.trim()) {
                alert('请输入文章标题！');
                if (articleTitleInput) articleTitleInput.focus();
                return;
            }
            if (!contentMarkdown.trim()) {
                alert('请输入文章内容！');
                editorInstance.focus();
                return;
            }

            // Show Modal
            if (bsModalInstance) { // Prefer Bootstrap JS instance
                bsModalInstance.show();
                console.log("Bootstrap JS: Modal 'show' command sent.");
            } else if (typeof $ !== 'undefined' && typeof $(publishSettingsModalElement).modal === 'function') { // Fallback to jQuery
                $(publishSettingsModalElement).modal('show');
                console.log("jQuery: Modal 'show' command sent.");
            } else {
                console.error("Cannot show modal: No Bootstrap JS or jQuery modal instance available.");
                alert("无法打开文章设置，请检查页面。");
            }
        });
        console.log("Event listener for modal trigger attached to topPublishBtnModalTrigger.");
    } else {
        console.error("Top publish button (modal trigger) not found.");
    }


    // --- Confirm Publish Button (Inside Modal) ---
    if (confirmPublishBtn) {
        confirmPublishBtn.addEventListener('click', function() {
            console.log("Confirm publish button in modal clicked.");
            if (!editorInstance || !articleTitleInput || !articleSettingsForm) {
                alert("页面组件未完全加载，无法发布。");
                return;
            }

            const title = articleTitleInput.value;
            const contentMarkdown = editorInstance.getMarkdown();
            const settingsData = {};
            if (articleSettingsForm) {
                const formData = new FormData(articleSettingsForm);
                formData.forEach((value, key) => { settingsData[key] = value; });
            }

            const coverFile = articleCoverUpload ? articleCoverUpload.files[0] : null;
            if (coverFile) settingsData.coverImageFileName = coverFile.name;


            // Validation for modal fields

            if (!settingsData.tags || !settingsData.tags.trim()) {
                alert('请输入至少一个标签！');
                document.getElementById('articleTags')?.focus();
                return;
            }
            if (!settingsData.summary || !settingsData.summary.trim()) {
                alert('请输入文章摘要！');
                document.getElementById('articleSummary')?.focus();
                return;
            }

            const finalArticleData = {
                title: title,
                content: contentMarkdown,
                tags: settingsData.tags.split(',').map(tag => tag.trim()).filter(tag => tag),
                coverImageFileName: settingsData.coverImageFileName || null,
                collection: settingsData.collection,
                summary: settingsData.summary
            };
            console.log("Final data to publish:", finalArticleData);

            alert('（模拟）文章及设置已收集，准备发送到后端！查看控制台。');

            // Close Modal
            if (bsModalInstance) {
                bsModalInstance.hide();
            } else if (typeof $ !== 'undefined' && typeof $(publishSettingsModalElement).modal === 'function') {
                $(publishSettingsModalElement).modal('hide');
            }
        });
        console.log("Event listener attached to confirmPublishBtn.");
    } else {
        console.error("Confirm publish button (#confirmPublishBtn) in modal not found.");
    }
});