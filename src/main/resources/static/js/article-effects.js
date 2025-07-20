
// --- 1. 页面加载主入口 ---
document.addEventListener('DOMContentLoaded', function() {
    console.log("LumiHive :: article-effects.js :: DOM fully loaded. Initializing all features.");

    // 初始化文章页面的各种UI效果和功能
    initializeArticlePage();

    // 初始化全新的动态评论系统
    initializeCommentSection();
});


/**
 * 初始化文章页面的所有非评论功能
 */
function initializeArticlePage() {
    // 为所有pre标签添加复制按钮
    document.querySelectorAll('article.article-post pre').forEach(pre => {
        if (pre.innerText.trim().length === 0) return;
        const button = document.createElement('button');
        button.className = 'copy-btn';
        button.textContent = '复制';
        pre.appendChild(button);
        button.addEventListener('click', () => {
            navigator.clipboard.writeText(pre.innerText).then(() => {
                button.textContent = '已复制!';
                setTimeout(() => { button.textContent = '复制'; }, 2000);
            }).catch(err => console.error('复制失败', err));
        });
    });

    // 返回顶部功能
    const backToTopBtn = document.getElementById('backToTop');
    if (backToTopBtn) {
        window.addEventListener('scroll', () => {
            backToTopBtn.style.display = (window.scrollY > 100) ? 'block' : 'none';
        });
        backToTopBtn.addEventListener('click', () => window.scrollTo({ top: 0, behavior: 'smooth' }));
    }

    // 标题区域渐入效果
    const jumbotron = document.querySelector('.jumbotron');
    if (jumbotron) {
        jumbotron.style.opacity = '0';
        jumbotron.style.transform = 'translateY(20px)';
        jumbotron.style.transition = 'opacity 0.8s ease, transform 0.8s ease';
        setTimeout(() => {
            jumbotron.style.opacity = '1';
            jumbotron.style.transform = 'translateY(0)';
        }, 100);
    }

    // 生成并设置右侧目录导航
    generateTableOfContents();
}


// 自定义toast函数
function showToast(title, message, icon = 'info-circle', iconClass = 'text-info') {
    const toast = document.getElementById('customToast');
    if (!toast) return;
    document.getElementById('customToastTitle').textContent = title;
    document.getElementById('customToastMessage').textContent = message;
    document.getElementById('customToastIcon').className = `fas fa-${icon} ${iconClass} mr-2`;
    toast.style.display = 'block';
    setTimeout(() => { toast.style.display = 'none'; }, 3000);
}

function hideCustomToast() {
    const toast = document.getElementById('customToast');
    if (toast) {
        toast.style.display = 'none';
    }
}

// 点赞功能
async function toggleLike(event) {
    const likeButton = event.currentTarget;
    const likeCountSpan = document.getElementById('likeCount');
    const articleId = likeButton.getAttribute('data-article-id');
    const token = document.querySelector("meta[name='_csrf']")?.getAttribute("content");
    const header = document.querySelector("meta[name='_csrf_header']")?.getAttribute("content");

    if (!token || !header) {
        alert("安全令牌丢失，请刷新页面！");
        return;
    }

    likeButton.disabled = true;
    try {
        const response = await fetch(`/api/article/${articleId}/like`, {
            method: 'POST',
            headers: { [header]: token }
        });
        if (!response.ok) throw new Error(`服务器响应失败: ${response.status}`);
        const data = await response.json();
        if (data.success) {
            likeCountSpan.textContent = data.likeCount;
            if (data.liked) {
                likeButton.classList.add('btn-danger');
                likeButton.classList.remove('btn-outline-danger');
            } else {
                likeButton.classList.add('btn-outline-danger');
                likeButton.classList.remove('btn-danger');
            }
        }
    } catch (error) {
        console.error('点赞操作失败:', error);
        alert("操作失败，请稍后重试。");
    } finally {
        likeButton.disabled = false;
    }
}

// 更新关注按钮的UI状态
function updateFollowButtonUI(buttonElement, isFollowing) {
    const icon = buttonElement.querySelector('i');
    const textSpan = buttonElement.querySelector('span');

    // 1. 定义两种状态下的UI数据
    const states = {
        followed: {
            btnClass: 'btn-secondary',
            iconClass: 'fas fa-user-check',
            text: ' 已关注'
        },
        notFollowed: {
            btnClass: 'btn-outline-primary',
            iconClass: 'fas fa-user-plus',
            text: ' 关注'
        }
    };

    // 2. 根据状态选择对应的数据
    const currentState = isFollowing ? states.followed : states.notFollowed;
    const otherState = isFollowing ? states.notFollowed : states.followed;

    // 3. 应用新的UI状态
    buttonElement.classList.remove(otherState.btnClass);
    buttonElement.classList.add(currentState.btnClass);
    if (icon) icon.className = currentState.iconClass;
    if (textSpan) textSpan.textContent = currentState.text;
}


/**
 * 切换用户的关注状态
 * @param {number} userId - 被关注用户的ID
 * @param {HTMLElement} buttonElement - 被点击的关注按钮
 */
async function toggleFollow(userId, buttonElement) {
    // --- 1. 前置校验 ---
    const currentUserId = document.querySelector('meta[name="current-user-id"]')?.getAttribute("content");
    if (currentUserId && currentUserId === String(userId)) {
        showToast('提示', '不能关注自己喵，试试关注其他作者吧！', 'warning');
        return;
    }

    const token = document.querySelector("meta[name='_csrf']")?.getAttribute("content");
    const header = document.querySelector("meta[name='_csrf_header']")?.getAttribute("content");
    if (!token || !header) {
        alert("安全令牌丢失，请刷新页面！");
        return;
    }

    // --- 2. 发送API请求 ---
    buttonElement.disabled = true;
    try {
        const response = await fetch(`/api/user/${userId}/follow`, {
            method: 'POST',
            headers: { [header]: token }
        });

        if (!response.ok) {
            throw new Error(`服务器响应失败: ${response.status}`);
        }

        const data = await response.json();

        // --- 3. 处理响应并更新UI ---
        if (data.success) {
            // 调用独立的UI更新函数
            updateFollowButtonUI(buttonElement, data.isFollowing);

            // 显示成功提示
            if (data.message) {
                const toastType = data.isFollowing ? 'success' : 'info';
                showToast('关注状态', data.message, toastType);
            }
        } else {
            // 显示失败提示
            showToast('提示', data.message || '操作失败，请稍后再试。', 'warning');
        }

    } catch (error) {
        console.error('关注操作失败:', error);
        showToast('错误', '操作失败，请稍后重试。', 'error');
    } finally {
        // --- 4. 无论成功失败，最后都恢复按钮可用 ---
        buttonElement.disabled = false;
    }
}

// 收藏功能
// 总的点击处理函数
function handleFavoriteClick(buttonElement) {
    const isFavorited = buttonElement.getAttribute('data-is-favorited') === 'true';
    const articleId = buttonElement.getAttribute('data-article-id');

    if (isFavorited) {
        // 如果已收藏，则执行取消全部收藏的操作
        removeAllFavorites(articleId, buttonElement);
    } else {
        // 如果未收藏，则打开选择收藏夹的模态框
        openFavoriteModal(articleId, buttonElement);
    }
}

// 1. 打开并填充收藏夹模态框
async function openFavoriteModal(articleId, buttonElement) {
    const modal = $('#favoriteModal');
    modal.modal('show');
    modal.data('article-id', articleId); // 将 articleId 存储在模态框上

    modal.data('main-button-element', buttonElement);
    const listContainer = document.getElementById('favorite-folders-list');
    listContainer.innerHTML = `<div class="text-center"><div class="spinner-border text-primary" role="status"></div></div>`;

    try {
        const response = await fetch('/api/favorites/my-folders');
        if (!response.ok) {
            if (response.status === 401) throw new Error('请先登录');
            throw new Error('无法获取收藏夹列表');
        }
        const folders = await response.json();
        listContainer.innerHTML = ''; // 清空加载动画

        if (folders.length === 0) {
            listContainer.innerHTML = '<p class="text-muted">您还没有创建任何收藏夹。</p>';
        } else {
            folders.forEach(folder => {
                const folderEl = document.createElement('button');
                folderEl.className = 'btn btn-outline-primary btn-block text-left mb-2';
                folderEl.textContent = folder.name;
                folderEl.onclick = () => addArticleToExistingFolder(articleId, folder.id, buttonElement);
                listContainer.appendChild(folderEl);
            });
        }
    } catch (error) {
        listContainer.innerHTML = `<p class="text-danger">${error.message}</p>`;
    }
}

// 2. 将文章添加到已存在的收藏夹
async function addArticleToExistingFolder(articleId, favoriteId, buttonElement) {
    showToast('提示', '正在收藏...', 'info');
    const token = document.querySelector("meta[name='_csrf']")?.getAttribute("content");
    const header = document.querySelector("meta[name='_csrf_header']")?.getAttribute("content");

    try {
        const response = await fetch('/api/favorites/add-to-folder', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json', [header]: token },
            body: JSON.stringify({ articleId, favoriteId })
        });
        const result = await response.json();
        if (result.success) {
            showToast('成功', result.message, 'success');
            $('#favoriteModal').modal('hide');
            // 收藏成功后，更新按钮状态
            updateFavoriteButtonUI(buttonElement, true, result.articlesCount);
        } else {
            showToast('失败', result.message, 'warning');
        }
    } catch (err) {
        showToast('错误', '网络请求失败', 'error');
    }
}

// 3. 创建新收藏夹并添加文章 (绑定到按钮)
document.addEventListener('DOMContentLoaded', function() {
});

async function createNewFavoriteAndAddArticle(buttonElement) {
    const newNameInput = document.getElementById('new-favorite-name');

    // 如果因为某些原因还是找不到元素，我们可以提前给出更明确的提示
    if (!newNameInput) {
        console.error("代码错误: 无法找到ID为 'new-favorite-name' 的输入框！");
        showToast('错误', '页面存在一个错误，请联系管理员。', 'error');
        return;
    }

    const FavoriteName = newNameInput.value.trim();
    const articleId = $('#favoriteModal').data('article-id');
    const mainFavoriteButton = $('#favoriteModal').data('main-button-element');

    if (!FavoriteName) {
        showToast('提示', '收藏夹名称不能为空！', 'warning');
        return;
    }

    buttonElement.disabled = true;
    buttonElement.textContent = '创建中...';

    const token = document.querySelector("meta[name='_csrf']")?.getAttribute("content");
    const header = document.querySelector("meta[name='_csrf_header']")?.getAttribute("content");

    try {
        const response = await fetch('/api/favorites/create-and-add', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json', [header]: token },
            body: JSON.stringify({ articleId: articleId, favoriteName: FavoriteName })
        });
        const result = await response.json();

        if (result.success) {
            showToast('成功', result.message, 'success');
            $('#favoriteModal').modal('hide');
            newNameInput.value = '';

            if (mainFavoriteButton) {
                updateFavoriteButtonUI(mainFavoriteButton, true, result.articlesCount);
            }
        } else {
            showToast('失败', result.message, 'error');
        }
    } catch (err) {
        showToast('错误', '网络请求失败', 'error');
    } finally {
        buttonElement.disabled = false;
        buttonElement.textContent = '创建并收藏';
    }
}


// 4. 取消对该文章的所有收藏
async function removeAllFavorites(articleId, buttonElement) {
    const token = document.querySelector("meta[name='_csrf']")?.getAttribute("content");
    const header = document.querySelector("meta[name='_csrf_header']")?.getAttribute("content");

    try {
        const response = await fetch(`/api/favorites/remove-all/${articleId}`, {
            method: 'DELETE',
            headers: { [header]: token }
        });
        const result = await response.json();
        if (result.success) {
            showToast('成功', result.message, 'info');
            updateFavoriteButtonUI(buttonElement, false);
        } else {
            showToast('失败', result.message, 'error');
        }
    } catch (err) {
        showToast('错误', '网络请求失败', 'error');
    }
}

// 5. 统一的UI更新函数
function updateFavoriteButtonUI(buttonElement, isFavorited, newCount) {
    const icon = buttonElement.querySelector('i');
    const countSpan = document.getElementById('favoriteCount');
    let currentCount = parseInt(countSpan.textContent, 10);

    buttonElement.setAttribute('data-is-favorited', isFavorited);

    if (isFavorited) {
        buttonElement.classList.remove('btn-outline-primary');
        buttonElement.classList.add('btn-primary');
        icon.classList.remove('far');
        icon.classList.add('fas');
        if (newCount !== null && newCount !== undefined) {
            countSpan.textContent = newCount;
        } else {
            countSpan.textContent = currentCount + 1;
        }
    } else {
        buttonElement.classList.remove('btn-primary');
        buttonElement.classList.add('btn-outline-primary');
        icon.classList.remove('fas');
        icon.classList.add('far');
        countSpan.textContent = Math.max(0, currentCount - 1); // 避免变为负数
    }
}

// 分享功能
function shareToWeibo() {
    const url = encodeURIComponent(window.location.href);
    const title = encodeURIComponent(document.title);
    window.open(`https://service.weibo.com/share/share.php?url=${url}&title=${title}`);
}

function copyLink() {
    navigator.clipboard.writeText(window.location.href).then(() => {
        alert('链接已复制到剪贴板');
    });
}

function scrollToComments() {
    const commentsSection = document.getElementById('comments-section');
    if (commentsSection) {
        commentsSection.scrollIntoView({ behavior: 'smooth' });
    }
}

// --- 右侧目录导航功能 ---
function generateTableOfContents() {
    const articleContent = document.querySelector('.article-content');
    const headings = articleContent ? Array.from(articleContent.querySelectorAll('h2, h3, h4')) : [];

    // 如果页面上没有标题，则不生成目录
    if (headings.length < 1) return;

    // 1. 创建右侧边栏容器
    const rightSidebar = document.createElement('div');
    rightSidebar.className = 'col-lg-2 d-none d-lg-block pl-lg-4'; // 在大屏幕上显示
    rightSidebar.style.paddingLeft = '0';

    // 2. 创建固定容器
    const stickyContainer = document.createElement('div');
    stickyContainer.className = 'sticky-top';
    stickyContainer.style.top = '80px';
    rightSidebar.appendChild(stickyContainer);

    // 3. 创建目录容器
    const tocContainer = document.createElement('div');
    tocContainer.className = 'toc-sidebar';
    tocContainer.id = 'toc-sidebar';
    stickyContainer.appendChild(tocContainer);

    // 4. 创建目录标题
    const tocHeader = document.createElement('div');
    tocHeader.className = 'toc-header';
    tocHeader.innerHTML = '<h5>文章目录</h5>';
    tocContainer.appendChild(tocHeader);

    // 5. 创建目录列表
    const tocList = document.createElement('ul');
    tocList.className = 'toc-list';

    // 6. 为每个标题创建目录项
    headings.forEach((heading, index) => {
        const id = `heading-${index}`;
        heading.id = id;
        const listItem = document.createElement('li');
        listItem.className = `toc-item toc-${heading.tagName.toLowerCase()}`;
        const link = document.createElement('a');
        link.href = `#${id}`;
        link.textContent = heading.textContent;
        link.addEventListener('click', function(e) {
            e.preventDefault();
            const targetElement = document.getElementById(id);
            if (targetElement) {
                const headerOffset = 80;
                const elementPosition = targetElement.getBoundingClientRect().top;
                const offsetPosition = elementPosition + window.pageYOffset - headerOffset;
                window.scrollTo({ top: offsetPosition, behavior: 'smooth' });
            }
        });
        listItem.appendChild(link);
        tocList.appendChild(listItem);
    });

    tocContainer.appendChild(tocList);

    // 7. 将整个侧边栏添加到页面布局中
    const articleRow = document.querySelector('.container-fluid .row');
    if (articleRow) {
        articleRow.appendChild(rightSidebar);
        // 为目录注入CSS样式
        const style = document.createElement('style');
        style.textContent = `
            .toc-sidebar{background-color:#f8f9fa;border-radius:8px;padding:1.25rem;border-left:4px solid #ffda58;max-height:calc(100vh - 100px);overflow-y:auto;}.toc-header h5{margin-top:0;margin-bottom:1rem;font-size:1.1rem;font-weight:600;}.toc-list{list-style:none;padding-left:0;margin-bottom:0;}.toc-item{margin-bottom:0.5rem;line-height:1.3;}.toc-item a{color:#4a5568;text-decoration:none;transition:all .2s ease;display:block;padding:.25rem 0;border-bottom:none;font-size:.9rem;}.toc-item a:hover{color:#ffda58;transform:translateX(3px);}.toc-item.active a{color:#ffda58;font-weight:600;}.toc-h3{padding-left:1rem;font-size:.85rem;}.toc-h4{padding-left:2rem;font-size:.8rem;}@media (max-width:991.98px){.toc-sidebar{display:none;}}
        `;
        document.head.appendChild(style);
        setupScrollSpy(); // 设置滚动监听
    }
}

// 目录滚动监听
function setupScrollSpy() {
    const headings = document.querySelectorAll('.article-content h2, .article-content h3, .article-content h4');
    if (headings.length === 0) return;
    const tocLinks = document.querySelectorAll('.toc-item a');

    const observer = new IntersectionObserver((entries) => {
        entries.forEach(entry => {
            if (entry.isIntersecting) {
                tocLinks.forEach(link => {
                    link.parentElement.classList.remove('active');
                    if (link.getAttribute('href') === `#${entry.target.id}`) {
                        link.parentElement.classList.add('active');
                    }
                });
            }
        });
    }, { rootMargin: '-80px 0px -60% 0px' });

    headings.forEach(heading => observer.observe(heading));
}

// --- 动态评论区功能 ---
function initializeCommentSection() {
    const articleIdMeta = document.querySelector('meta[name="article-id"]');
    if (!articleIdMeta) {
        console.error("评论功能错误: 页面缺少 <meta name='article-id'> 标签。");
        return;
    }
    const articleId = articleIdMeta.getAttribute('content');
    loadComments(articleId);

    const mainCommentForm = document.getElementById('commentForm');
    if (mainCommentForm) {
        // 先移除可能存在的旧的jQuery事件监听器，避免冲突
        if (window.jQuery) {
            $(mainCommentForm).off('submit');
        }
        mainCommentForm.addEventListener('submit', function (e) {
            e.preventDefault();
            const contentTextarea = document.getElementById('commentContent');
            const content = contentTextarea.value.trim();
            if (content) {
                submitComment(articleId, content, null)
                    .then(data => {
                        if (data.success) {
                            contentTextarea.value = '';
                            loadComments(articleId);
                        } else {
                            alert('评论失败: ' + (data.message || '未知错误'));
                        }
                    })
                    .catch(err => {
                        console.error("评论提交失败:", err);
                        alert('评论失败: ' + err.message);
                    });
            }
        });
    }
}

async function loadComments(articleId) {
    const commentsList = document.getElementById('comments-list');
    if (!commentsList) return;
    commentsList.innerHTML = '<div class="text-center py-5"><div class="spinner-border text-warning" role="status"></div></div>';
    try {
        const response = await fetch(`/api/article/${articleId}/comments`);
        if (!response.ok) throw new Error(`网络请求失败 (状态: ${response.status})`);
        const comments = await response.json();
        commentsList.innerHTML = '';
        if (comments.length === 0) {
            commentsList.innerHTML = '<div class="text-center py-4 text-muted">暂无评论，快来抢沙发吧！</div>';
        } else {
            comments.forEach(comment => commentsList.appendChild(createCommentElement(comment, articleId)));
        }
    } catch (error) {
        console.error("加载评论时出错:", error);
        commentsList.innerHTML = `<div class="text-center py-4 text-danger">${error.message}</div>`;
    }
}

async function submitComment(articleId, content, parentId) {
    const token = document.querySelector("meta[name='_csrf']")?.getAttribute("content");
    const header = document.querySelector("meta[name='_csrf_header']")?.getAttribute("content");
    if (!token || !header) throw new Error("安全令牌(CSRF Token)丢失，请刷新页面后重试。");

    const payload = { content: content, parentId: parentId };
    const response = await fetch(`/api/article/${articleId}/comment`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json', [header]: token },
        body: JSON.stringify(payload)
    });

    if (!response.ok) {
        if (response.status === 401 || response.status === 403) throw new Error("无权限操作，请先登录。");
        try {
            const errorData = await response.json();
            throw new Error(errorData.message || '发生未知错误');
        } catch (e) {
            throw new Error(`发生网络错误 (状态: ${response.status})`);
        }
    }
    return response.json();
}

function createCommentElement(comment, articleId) {
    const commentDiv = document.createElement('div');
    commentDiv.className = 'comment-item';
    let repliesHtml = '';
    if (comment.replies && comment.replies.length > 0) {
        repliesHtml = `<div class="replies-container">${comment.replies.map(reply => createReplyElement(reply).outerHTML).join('')}</div>`;
    }
    const commentDate = new Date(comment.gmtCreate).toLocaleString('zh-CN', { hour12: false });
    commentDiv.innerHTML = `
        <div class="d-flex">
            <img src="${comment.avatarUrl || '/img/default01.jpg'}" alt="${comment.userName}" class="comment-avatar">
            <div class="w-100">
                <div class="comment-meta d-flex justify-content-between">
                    <strong class="comment-author">${comment.userName}</strong>
                    <small class="comment-date text-muted">${commentDate}</small>
                </div>
                <p class="comment-content">${comment.content.replace(/\n/g, '<br>')}</p>
                <div class="comment-actions">
                    <button class="btn btn-sm btn-link reply-btn" data-comment-id="${comment.id}"><i class="fas fa-reply"></i> 回复</button>
                </div>
            </div>
        </div>
        ${repliesHtml}
        <div class="reply-form-container" id="reply-form-for-${comment.id}"></div>
    `;
    commentDiv.querySelector('.reply-btn').addEventListener('click', (e) => showReplyForm(e.currentTarget, articleId));
    return commentDiv;
}

function createReplyElement(reply) {
    const replyDiv = document.createElement('div');
    replyDiv.className = 'reply-item d-flex';
    const replyDate = new Date(reply.gmtCreate).toLocaleString('zh-CN', { hour12: false });
    replyDiv.innerHTML = `
        <img src="${reply.avatarUrl || '/img/default01.jpg'}" alt="${reply.userName}" class="reply-avatar">
        <div class="w-100">
            <div class="comment-meta d-flex justify-content-between">
                <strong class="comment-author">${reply.userName}</strong>
                <small class="comment-date text-muted">${replyDate}</small>
            </div>
            <p class="comment-content">${reply.content.replace(/\n/g, '<br>')}</p>
        </div>
    `;
    return replyDiv;
}

function showReplyForm(button, articleId) {
    const commentId = button.dataset.commentId;
    const container = document.getElementById(`reply-form-for-${commentId}`);
    if (container.querySelector('form')) {
        container.innerHTML = ''; return;
    }
    document.querySelectorAll('.reply-form-container').forEach(c => c.innerHTML = '');
    const authorName = button.closest('.comment-item').querySelector('.comment-author').innerText;
    const form = document.createElement('form');
    form.className = 'reply-form mt-2';
    form.innerHTML = `
        <div class="form-group"><textarea class="form-control" rows="2" placeholder="回复 @${authorName}..." required></textarea></div>
        <div class="text-right"><button type="button" class="btn btn-sm btn-secondary cancel-reply-btn mr-2">取消</button><button type="submit" class="btn btn-sm btn-primary">回复</button></div>
    `;
    container.appendChild(form);
    form.querySelector('textarea').focus();
    form.querySelector('.cancel-reply-btn').addEventListener('click', () => container.innerHTML = '');
    form.addEventListener('submit', (e) => {
        e.preventDefault();
        const content = form.querySelector('textarea').value.trim();
        if (content) {
            submitComment(articleId, content, commentId)
                .then(() => { container.innerHTML = ''; loadComments(articleId); })
                .catch(err => alert('回复失败: ' + err.message));
        }
    });
}