import { showToast } from './common-utils.js';


document.addEventListener('DOMContentLoaded', function() {
    // 设置统一的事件委托监听器（点击事件）
    document.addEventListener('click', handleDelegatedClicks);

    // 设置统一的事件委托监听器（提交评论表单）
    document.addEventListener('submit', handleDelegatedSubmits);

    // 初始化非委托的、页面加载时就需要执行的功能
    initializePageFeatures();

    // 初始化动态评论系统
    initializeCommentSection();
});


/**
 * =========================================================================
 * 事件委托处理函数（点击事件）
 * =========================================================================
 * @param {Event} event - 点击事件对象
 */
function handleDelegatedClicks(event) {
    const target = event.target;

    // --- 处理关注按钮 ---
    const followButton = target.closest('.js-toggle-follow');
    if (followButton) {
        const userId = followButton.dataset.userId;
        toggleFollow(userId, followButton);
        return;
    }

    // --- 处理点赞按钮 ---
    const likeButton = target.closest('.js-toggle-like');
    if (likeButton) {
        event.preventDefault();
        toggleLike(event, likeButton);
        return;
    }

    // --- 处理微博分享按钮 ---
    const shareWeiboButton = target.closest('.js-share-weibo');
    if (shareWeiboButton) {
        shareToWeibo();
        return;
    }

    // --- 处理复制链接按钮 ---
    const copyLinkButton = target.closest('.js-copy-link');
    if (copyLinkButton) {
        copyLink();
        return;
    }

    // --- 处理代码块的复制按钮 ---
    const copyCodeButton = target.closest('pre > .copy-btn');
    if (copyCodeButton) {
        const pre = copyCodeButton.parentElement;
        navigator.clipboard.writeText(pre.innerText.replace('复制', '').trim()).then(() => {
            copyCodeButton.textContent = '已复制!';
            setTimeout(() => { copyCodeButton.textContent = '复制'; }, 2000);
        }).catch(err => console.error('复制失败', err));
        return;
    }

    // --- 处理收藏按钮 ---
    const favoriteButton = target.closest('.js-favorite-button');
    if (favoriteButton) {
        handleFavoriteClick(favoriteButton);
        return;
    }

    // --- 处理弹窗中的“添加到已有收藏夹”按钮 ---
    const addToFolderButton = target.closest('.js-add-to-folder');
    if (addToFolderButton) {
        const folderId = addToFolderButton.dataset.folderId;
        const modalElement = document.getElementById('favoriteModal');
        const articleId = modalElement.dataset.articleId;

        if (articleId && folderId) {
            const mainFavoriteButton = document.querySelector(`.js-favorite-button[data-article-id="${articleId}"]`);
            if (mainFavoriteButton) {
                addArticleToExistingFolder(articleId, folderId, mainFavoriteButton);
            } else {
                console.error("无法在页面上找到主收藏按钮。");
                showToast('发生未知错误', 'error');
            }
        } else {
            console.error("数据不完整，无法执行收藏操作。 articleId:", articleId, "folderId:", folderId);
            showToast('操作失败：缺少关键数据', 'error');
        }
        return;
    }

    // --- 处理弹窗中的“创建并收藏”按钮 ---
    const createAndAddButton = target.closest('#create-and-add-btn');
    if (createAndAddButton) {
        createNewFavoriteAndAddArticle(createAndAddButton);
        return;
    }

    // --- 处理侧边栏的评论按钮点击 ---
    const scrollToCommentsButton = target.closest('.js-scroll-to-comments');
    if (scrollToCommentsButton) {
        event.preventDefault(); // 阻止默认行为
        const commentsSection = document.getElementById('comments-section');
        if (commentsSection) {
            commentsSection.scrollIntoView({ behavior: 'smooth', block: 'start' });
        }
        return;
    }

    // --- 处理评论区的“回复”按钮 ---
    const replyButton = target.closest('.reply-btn');
    if (replyButton) {
        const articleId = document.querySelector('meta[name="article-id"]')?.getAttribute('content');
        if (articleId) {
            showReplyForm(replyButton, articleId);
        }
        return;
    }

    // --- 处理动态创建的“取消回复”按钮 ---
    const cancelReplyButton = target.closest('.cancel-reply-btn');
    if (cancelReplyButton) {
        const formContainer = cancelReplyButton.closest('.reply-form-container');
        if (formContainer) {
            formContainer.innerHTML = '';
        }
    }
}

/**
 * =========================================================================
 * 事件委托处理函数（提交表单事件）
 * =========================================================================
 * @param {Event} event - 提交表单事件对象
 */
function handleDelegatedSubmits(event) {
    const form = event.target;

    // --- 处理主评论表单的提交 ---
    if (form.matches('#commentForm')) {
        event.preventDefault();
        const articleId = document.querySelector('meta[name="article-id"]')?.getAttribute('content');
        const contentTextarea = document.getElementById('commentContent');
        const content = contentTextarea.value.trim();
        if (content && articleId) {
            submitComment(articleId, content, null)
                .then(data => {
                    if (data.success) {
                        contentTextarea.value = '';
                        loadComments(articleId);
                    } else {
                        showToast('评论失败: ' + (data.message || '未知错误'), 'error');
                    }
                })
                .catch(err => {
                    showToast('评论失败: ' + err.message, 'error');
                });
        }
    }

    // --- 处理回复表单的提交 ---
    if (form.matches('.reply-form')) {
        event.preventDefault();
        const articleId = document.querySelector('meta[name="article-id"]')?.getAttribute('content');
        const parentId = form.dataset.parentId;
        const content = form.querySelector('textarea').value.trim();
        if (content && articleId && parentId) {
            submitComment(articleId, content, parentId)
                .then(() => {
                    const container = form.parentElement;
                    if(container) container.innerHTML = '';
                    loadComments(articleId);
                })
                .catch(err => showToast('回复失败: ' + err.message, 'error'));
        }
    }
}

/**
 * =========================================================================
 * 初始化页面加载时就需要执行的、非委托的功能
 * =========================================================================
 */
function initializePageFeatures() {
    // 为所有 pre 标签添加复制按钮
    document.querySelectorAll('article.article-post pre').forEach(pre => {
        // 检查 pre 标签内是否真的有代码（排除只有按钮的情况）
        const codeText = pre.innerText.replace(pre.querySelector('.copy-btn')?.textContent || '', '').trim();
        if (codeText.length === 0) return;

        // 如果已经有按钮，则不再添加
        if (pre.querySelector('.copy-btn')) return;

        const button = document.createElement('button');
        button.className = 'copy-btn';
        button.textContent = '复制';
        pre.appendChild(button);
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


/* ==========================================================================
   业务逻辑函数
   ========================================================================== */

// --- 点赞功能 ---
async function toggleLike(event, buttonElement) {
    event.stopPropagation();
    const likeCountSpan = buttonElement.querySelector('.like-count');
    const articleId = buttonElement.dataset.articleId;
    const token = document.querySelector("meta[name='_csrf']")?.getAttribute("content");
    const header = document.querySelector("meta[name='_csrf_header']")?.getAttribute("content");

    if (!likeCountSpan) {
        console.error("点赞功能错误: 未找到点赞计数元素。请检查HTML结构。");
        return;
    }
    if (!token || !header) {
        showToast('安全令牌丢失，请刷新页面后重试!', 'error');
        return;
    }

    buttonElement.disabled = true;
    try {
        const response = await fetch(`/api/article/${articleId}/like`, {
            method: 'POST',
            headers: { [header]: token }
        });
        if (!response.ok) throw new Error(`服务器响应失败: ${response.status}`);
        const data = await response.json();
        if (data.success) {
            likeCountSpan.textContent = data.likeCount;
            buttonElement.classList.toggle('btn-danger', data.liked);
            buttonElement.classList.toggle('btn-outline-danger', !data.liked);
        }
    } catch (error) {
        console.error('点赞操作失败:', error);
        showToast(error.message || '操作失败，请稍后再试。', 'error');
    } finally {
        buttonElement.disabled = false;
    }
}

// --- 关注功能 ---
function updateFollowButtonUI(buttonElement, isFollowing) {
    const icon = buttonElement.querySelector('i');
    const textSpan = buttonElement.querySelector('span');
    const states = {
        followed: { btnClass: 'btn-secondary', iconClass: 'fas fa-user-check', text: ' 已关注' },
        notFollowed: { btnClass: 'btn-outline-primary', iconClass: 'fas fa-user-plus', text: ' 关注' }
    };
    const currentState = isFollowing ? states.followed : states.notFollowed;
    const otherState = isFollowing ? states.notFollowed : states.followed;

    buttonElement.classList.remove(otherState.btnClass);
    buttonElement.classList.add(currentState.btnClass);
    if (icon) icon.className = currentState.iconClass;
    if (textSpan) textSpan.textContent = currentState.text;
}

async function toggleFollow(userId, buttonElement) {
    // 确保用户ID作为字符串处理，避免JavaScript大整数精度丢失
    const userIdStr = String(userId);
    console.log('关注操作 - 目标用户ID (字符串):', userIdStr);
    
    const currentUserId = document.querySelector('meta[name="current-user-id"]')?.getAttribute("content");
    if (currentUserId && currentUserId === userIdStr) {
        showToast('不能关注自己喵，试试关注其他作者吧！', 'warning');
        return;
    }

    const token = document.querySelector("meta[name='_csrf']")?.getAttribute("content");
    const header = document.querySelector("meta[name='_csrf_header']")?.getAttribute("content");
    if (!token || !header) {
        showToast("安全令牌丢失，请刷新页面！", 'error');
        return;
    }

    buttonElement.disabled = true;
    try {
        const response = await fetch(`/api/user/${userId}/follow`, {
            method: 'POST',
            headers: { [header]: token }
        });
        if (!response.ok) throw new Error(`服务器响应失败: ${response.status}`);
        const data = await response.json();
        if (data.success) {
            updateFollowButtonUI(buttonElement, data.isFollowing);
            if (data.message) {
                showToast(data.message, data.isFollowing ? 'success' : 'info');
            }
        } else {
            showToast(data.message || '操作失败，请稍后再试。', 'error');
        }
    } catch (error) {
        console.error('关注操作失败:', error);
        showToast('操作失败，请稍后重试。', 'error');
    } finally {
        buttonElement.disabled = false;
    }
}

// --- 收藏功能 ---
function handleFavoriteClick(buttonElement) {
    const isFavorited = buttonElement.getAttribute('data-is-favorited') === 'true';
    const articleId = buttonElement.getAttribute('data-article-id');
    if (isFavorited) {
        removeAllFavorites(articleId, buttonElement);
    } else {
        openFavoriteModal(articleId, buttonElement);
    }
}

async function openFavoriteModal(articleId, buttonElement) {
    const modal = $('#favoriteModal');
    modal.modal('show');
    document.getElementById('favoriteModal').dataset.articleId = articleId;
    const listContainer = document.getElementById('favorite-folders-list');
    listContainer.innerHTML = `<div class="text-center"><div class="spinner-border text-primary" role="status"></div></div>`;

    try {
        const response = await fetch('/api/favorites/my-folders');
        if (!response.ok) throw new Error(response.status === 401 ? '请先登录' : '无法获取收藏夹列表');
        const folders = await response.json();
        listContainer.innerHTML = '';
        if (folders.length === 0) {
            listContainer.innerHTML = '<p class="text-muted">您还没有创建收藏夹。</p>';
        } else {
            folders.forEach(folder => {
                const folderEl = document.createElement('button');
                folderEl.className = 'btn btn-outline-primary btn-block text-left mb-2 js-add-to-folder';
                folderEl.textContent = folder.name;
                folderEl.dataset.folderId = folder.id;
                listContainer.appendChild(folderEl);
            });
        }
    } catch (error) {
        listContainer.innerHTML = `<p class="text-danger">${error.message}</p>`;
    }
}

async function addArticleToExistingFolder(articleId, favoriteId, buttonElement) {
    console.log(favoriteId);
    const token = document.querySelector("meta[name='_csrf']")?.getAttribute("content");
    const header = document.querySelector("meta[name='_csrf_header']")?.getAttribute("content");
    try {
        const response = await fetch('/api/favorites/add-to-folder', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json', [header]: token },
            body: JSON.stringify({ articleId, favoriteId })
        });
        console.log(response.status);
        const result = await response.json();
        console.log(result);
        if (result.success) {
            showToast(result.message, 'success');
            $('#favoriteModal').modal('hide');
            updateFavoriteButtonUI(buttonElement, true, result.articlesCount);
        } else {
            showToast(result.message, 'error');
        }
    } catch (err) {
        showToast('网络请求失败', 'error');
    }
}

async function createNewFavoriteAndAddArticle(buttonElement) {
    const newNameInput = document.getElementById('new-favorite-name');
    if (!newNameInput) {
        showToast('页面存在一个错误，请联系管理员。', 'error');
        return;
    }
    const favoriteName = newNameInput.value.trim();
    const articleId = document.getElementById('favoriteModal').dataset.articleId;
    if (!favoriteName) {
        showToast('收藏夹名称不能为空！', 'error');
        return;
    }
    if (!articleId) {
        showToast('无法获取文章ID，请刷新页面后重试。', 'error');
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
            body: JSON.stringify({ articleId: articleId, favoriteName: favoriteName })
        });
        const result = await response.json();
        if (result.success) {
            showToast(result.message, 'success');
            $('#favoriteModal').modal('hide');
            newNameInput.value = '';
            const mainFavoriteButton = document.querySelector(`.js-favorite-button[data-article-id="${articleId}"]`);
            if (mainFavoriteButton) {
                updateFavoriteButtonUI(mainFavoriteButton, true, result.articlesCount);
            }
        } else {
            showToast(result.message, 'error');
        }
    } catch (err) {
        showToast('网络请求失败', 'error');
    } finally {
        buttonElement.disabled = false;
        buttonElement.textContent = '创建并收藏';
    }
}

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
            showToast(result.message, 'success');
            updateFavoriteButtonUI(buttonElement, false);
        } else {
            showToast(result.message, 'error');
        }
    } catch (err) {
        showToast('网络请求失败', 'error');
    }
}

function updateFavoriteButtonUI(buttonElement, isFavorited, newCount) {
    const icon = buttonElement.querySelector('i');
    const countSpan = document.getElementById('favoriteCount');
    if (!countSpan) return;
    let currentCount = parseInt(countSpan.textContent, 10);
    buttonElement.setAttribute('data-is-favorited', isFavorited);
    if (isFavorited) {
        buttonElement.classList.replace('btn-outline-primary', 'btn-primary');
        icon.classList.replace('far', 'fas');
        countSpan.textContent = newCount !== null && newCount !== undefined ? newCount : currentCount + 1;
    } else {
        buttonElement.classList.replace('btn-primary', 'btn-outline-primary');
        icon.classList.replace('fas', 'far');
        countSpan.textContent = Math.max(0, currentCount - 1);
    }
}

// --- 分享功能 ---
function shareToWeibo() {
    const url = encodeURIComponent(window.location.href);
    const title = encodeURIComponent(document.title);
    window.open(`https://service.weibo.com/share/share.php?url=${url}&title=${title}`);
}
function copyLink() {
    navigator.clipboard.writeText(window.location.href).then(() => {
        showToast('链接已复制到剪贴板', 'success');
    });
}

// --- 右侧目录导航功能 ---
function generateTableOfContents() {
    const articleContent = document.querySelector('.article-content');
    const headings = articleContent ? Array.from(articleContent.querySelectorAll('h2, h3, h4')) : [];
    if (headings.length < 1) return;

    const rightSidebar = document.createElement('div');
    rightSidebar.className = 'col-lg-2 d-none d-lg-block pl-lg-4';
    rightSidebar.style.paddingLeft = '0';
    const stickyContainer = document.createElement('div');
    stickyContainer.className = 'sticky-top';
    stickyContainer.style.top = '80px';
    rightSidebar.appendChild(stickyContainer);
    const tocContainer = document.createElement('div');
    tocContainer.className = 'toc-sidebar';
    tocContainer.id = 'toc-sidebar';
    stickyContainer.appendChild(tocContainer);
    const tocHeader = document.createElement('div');
    tocHeader.className = 'toc-header';
    tocHeader.innerHTML = '<h5>文章目录</h5>';
    tocContainer.appendChild(tocHeader);
    const tocList = document.createElement('ul');
    tocList.className = 'toc-list';

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
    const articleRow = document.querySelector('.container-fluid .row');
    if (articleRow) {
        articleRow.appendChild(rightSidebar);
        const style = document.createElement('style');
        style.textContent = `.toc-sidebar{background-color:#f8f9fa;border-radius:8px;padding:1.25rem;border-left:4px solid #ffda58;max-height:calc(100vh - 100px);overflow-y:auto;}.toc-header h5{margin-top:0;margin-bottom:1rem;font-size:1.1rem;font-weight:600;}.toc-list{list-style:none;padding-left:0;margin-bottom:0;}.toc-item{margin-bottom:0.5rem;line-height:1.3;}.toc-item a{color:#4a5568;text-decoration:none;transition:all .2s ease;display:block;padding:.25rem 0;border-bottom:none;font-size:.9rem;}.toc-item a:hover{color:#ffda58;transform:translateX(3px);}.toc-item.active a{color:#ffda58;font-weight:600;}.toc-h3{padding-left:1rem;font-size:.85rem;}.toc-h4{padding-left:2rem;font-size:.8rem;}@media (max-width:991.98px){.toc-sidebar{display:none;}}`;
        document.head.appendChild(style);
        setupScrollSpy();
    }
}

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
    if (!articleIdMeta) return;
    const articleId = articleIdMeta.getAttribute('content');
    loadComments(articleId);

    const mainCommentForm = document.getElementById('commentForm');
    if (mainCommentForm) {
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
                            showToast('评论失败: ' + (data.message || '未知错误'), 'error');
                        }
                    })
                    .catch(err => {
                        showToast('评论失败: ' + err.message, 'error');
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
        const errorData = await response.json().catch(() => ({ message: '发生未知错误' }));
        throw new Error(errorData.message);
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
        container.innerHTML = '';
        return;
    }
    document.querySelectorAll('.reply-form-container').forEach(c => c.innerHTML = '');

    const authorName = button.closest('.comment-item').querySelector('.comment-author').innerText;
    const form = document.createElement('form');
    // 给表单也加上一个class，用于事件委托
    form.className = 'reply-form mt-2';
    // 将父评论ID存到表单的data属性上
    form.dataset.parentId = commentId;
    form.innerHTML = `
        <div class="form-group"><textarea class="form-control" rows="2" placeholder="回复 @${authorName}..." required></textarea></div>
        <div class="text-right"><button type="button" class="btn btn-sm btn-secondary cancel-reply-btn mr-2">取消</button><button type="submit" class="btn btn-sm btn-primary">回复</button></div>
    `;
    container.appendChild(form);
    form.querySelector('textarea').focus();
}