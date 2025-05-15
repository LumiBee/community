
document.addEventListener("DOMContentLoaded", function() {
    // 从这里开始是您原来的JS代码
    const editorElement = document.getElementById('toastUiEditor');
    let editorInstance = null;

    let mainNavbarHeight = 0;
    const mainNavbar = document.querySelector('nav.fixed-top');
    if (mainNavbar) {
        mainNavbarHeight = mainNavbar.offsetHeight;
    } else {
        console.warn("Main navbar not found, using default offset for top-action-bar.");
        mainNavbarHeight = parseFloat(getComputedStyle(document.body).paddingTop) || 70;
        if (isNaN(mainNavbarHeight)) mainNavbarHeight = 70;
    }

    let topActionBarHeight = 0;
    const topActionBar = document.querySelector('.top-action-bar.fixed-top');
    if (topActionBar) {
        topActionBar.style.top = mainNavbarHeight + 'px';
        topActionBarHeight = topActionBar.offsetHeight;
    } else {
        console.warn("Top action bar not found.");
    }

    const publishPageWrapper = document.querySelector('.publish-page-wrapper');
    if (publishPageWrapper) {
        const totalTopOffset = mainNavbarHeight + topActionBarHeight;
        publishPageWrapper.style.paddingTop = totalTopOffset + 'px';

        let bottomStatusBarHeight = 0;
        const bottomStatusBar = document.querySelector('.bottom-status-bar.fixed-bottom');
        if (bottomStatusBar) {
            bottomStatusBarHeight = bottomStatusBar.offsetHeight;
        }
        publishPageWrapper.style.paddingBottom = bottomStatusBarHeight + 'px';
    } else {
        console.warn("Publish page wrapper not found.");
    }

    if (!editorElement) {
        console.error("CRITICAL: Toast UI Editor mount point (#toastUiEditor) not found!");
        return;
    }

    try {
        let editorHeight = '82vh';
        if (publishPageWrapper && mainNavbarHeight > 0 && topActionBarHeight > 0) {
            const bottomBarHeight = document.querySelector('.bottom-status-bar.fixed-bottom')?.offsetHeight || 40;
            const editorContainerPadding = (parseFloat(getComputedStyle(document.querySelector('.editor-content-container.container')).paddingTop) || 16) +
                (parseFloat(getComputedStyle(document.querySelector('.editor-content-container.container')).paddingBottom) || 16);
            const availableHeight = window.innerHeight - (mainNavbarHeight + topActionBarHeight + bottomBarHeight + editorContainerPadding);
            editorHeight = Math.max(200, availableHeight) + 'px';
            console.log(`Calculated editor height: ${editorHeight}, Available: ${availableHeight}, Navbar: ${mainNavbarHeight}, ActionBar: ${topActionBarHeight}, BottomBar: ${bottomBarHeight}, Padding: ${editorContainerPadding}`);
        } else {
            console.warn("Could not calculate dynamic editor height, using default '60vh'.");
        }

        console.log("Available plugins in toastui.Editor.plugin:", toastui.Editor.plugin);

        let editorPlugins = [];
        if (toastui.Editor.plugin && toastui.Editor.plugin['code-syntax-highlight']) {
            editorPlugins.push(toastui.Editor.plugin['code-syntax-highlight']);
            console.log("Code syntax highlight plugin found and added (using key 'code-syntax-highlight').");
        } else if (toastui.Editor.plugin && toastui.Editor.plugin.codeSyntaxHighlight) {
            editorPlugins.push(toastui.Editor.plugin.codeSyntaxHighlight);
            console.log("Code syntax highlight plugin (camelCase key 'codeSyntaxHighlight') found and added.");
        } else {
            console.warn("Code syntax highlight plugin not found in toastui.Editor.plugin. Editor will initialize without it if this was the only plugin.");
        }
        if (editorPlugins.length > 0 && typeof hljs !== 'undefined') {
            console.log("hljs is available globally.");
        }

        // ---- 处理 initialContent ----
        // 由于JS文件不能直接解析Thymeleaf表达式，我们需要从HTML中获取这个值
        let resolvedInitialContent = '';
        const initialContentDataElement = document.getElementById('initial-content-data');
        if (initialContentDataElement) {
            resolvedInitialContent = initialContentDataElement.textContent || ''; // 或者 .innerHTML，取决于你如何存储
        }
        // ---------------------------

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
                        callback(event.target.result, blob.name);
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

    const wordCountEl = document.getElementById('wordCount');
    const charCountEl = document.getElementById('charCount');
    function updateStats() {
        if (!wordCountEl || !charCountEl || !editorInstance) return;
        const markdownText = editorInstance.getMarkdown();
        const plainText = markdownText.replace(/<[^>]*>?/gm, '');
        charCountEl.textContent = plainText.length;
        wordCountEl.textContent = plainText.trim() ? plainText.trim().split(/\s+/).length : 0;
    }
    if (editorInstance) {
        editorInstance.on('change', updateStats);
        updateStats();
    } else {
        console.error("Cannot setup stats update: Editor instance is not available.");
    }

    const publishBtn = document.querySelector('.publish-btn');
    const titleInput = document.getElementById('articleTitleInput');
    const formArticleContentInput = document.getElementById('formArticleContent');
    if (publishBtn && titleInput && formArticleContentInput) {
        publishBtn.addEventListener('click', function() {
            if (!editorInstance) {
                alert('编辑器未正确初始化！');
                return;
            }
            const title = titleInput.value;
            const contentMarkdown = editorInstance.getMarkdown();
            if (!title.trim()) {
                alert('请输入文章标题！');
                titleInput.focus();
                return;
            }
            if (!contentMarkdown.trim()) {
                alert('请输入文章内容！');
                editorInstance.focus();
                return;
            }
            formArticleContentInput.value = contentMarkdown;
            console.log('文章标题:', title);
            console.log('Markdown 内容:', contentMarkdown);
            alert('文章已准备好“发布”。');
            // document.getElementById('publishForm').submit();
        });
    } else {
        if (!publishBtn) console.error("Publish button not found");
        if (!titleInput) console.error("Title input not found");
        // editorInstance 检查已在上面
        if (!formArticleContentInput) console.error("Form content input not found");
    }
});