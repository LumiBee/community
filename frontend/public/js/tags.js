// 全局变量
let activeSlug = 'all';

// DOM加载完成后执行
document.addEventListener('DOMContentLoaded', function() {
    const tagSearchInput = document.getElementById('tagSearchInput');
    const tagListContainer = document.getElementById('tagListContainer');

    // 在父容器上添加事件监听器
    if (tagListContainer) {
        tagListContainer.addEventListener('click', handleTagContainerClick);
    }

    // 添加搜索功能
    if (tagSearchInput) {
        tagSearchInput.addEventListener('input', handleTagSearch);
    }

    // 检查URL中是否有标签slug
    const hash = window.location.hash;
    if (hash && hash.length > 1) {
        // 移除#号，获取标签名称并解码
        const tagNameFromUrl = decodeURIComponent(hash.substring(1));
        // 尝试通过data-slug查找
        const tagElement = document.querySelector(`.tag-item[data-slug="${tagNameFromUrl}"]`);

        if (tagElement) {
            handleTagClick(tagElement);
        } else {
            fetchAndRenderArticles(activeSlug);
        }
    } else {
        fetchAndRenderArticles(activeSlug);
    }

    // 添加页面加载动画
    setTimeout(() => {
        const filterSection = document.querySelector('.filter-section');
        if (filterSection) {
            filterSection.classList.add('fade-in');
        }
    }, 300);
});

// 事件处理函数
function handleTagContainerClick(event) {
    const clickedTag = event.target.closest('.tag-item');
    if (!clickedTag) {
        return;
    }
    event.preventDefault();
    handleTagClick(clickedTag);
}

// 处理标签点击事件
function handleTagClick(element) {
    // 移除所有激活状态
    document.querySelectorAll('.tag-item').forEach(tag => {
        tag.classList.remove('active');
    });

    // 激活当前标签
    element.classList.add('active');

    // 更新活跃slug和标题
    activeSlug = element.getAttribute('data-slug');
    const tagName = activeSlug === 'all' ? '所有文章' : element.textContent.trim();
    document.getElementById('articleListTitle').textContent = `(${tagName})`;

    // 更新URL，但不刷新页面
    if (activeSlug === 'all') {
        history.replaceState(null, null, ' ');
    } else {
        history.replaceState(null, null, `#${activeSlug}`);
    }

    // 获取文章
    fetchAndRenderArticles(activeSlug);
}

// 处理标签搜索事件
function handleTagSearch(event) {
    const filterValue = event.target.value.toLowerCase();
    const tags = document.querySelectorAll('#tagListContainer .tag-item');

    tags.forEach(tag => {
        if (tag.getAttribute('data-slug') === 'all') return;

        const tagName = tag.textContent.toLowerCase();
        if (tagName.includes(filterValue)) {
            tag.style.display = '';
            tag.style.animation = 'fadeIn 0.3s ease-out';
        } else {
            tag.style.display = 'none';
        }
    });
}

// 获取并渲染文章列表
async function fetchAndRenderArticles(slug) {
    const articleGrid = document.getElementById('articleGrid');
    const spinner = document.getElementById('loadingSpinner');

    // 显示加载动画
    spinner.style.display = 'block';
    articleGrid.innerHTML = '';
    articleGrid.classList.remove('stagger-children');

    const apiUrl = `/api/tags/${slug}`;

    try {
        const response = await fetch(apiUrl);
        if (!response.ok) throw new Error('网络响应错误');

        const articles = await response.json();

        if (articles.length === 0) {
            articleGrid.innerHTML = `
                <div class="no-articles">
                    <div class="no-articles-icon"><i class="fas fa-file-alt"></i></div>
                    <h3>暂无文章</h3>
                    <p>该标签下还没有文章，期待更多精彩内容...</p>
                </div>
            `;
        } else {
            // 添加交错动画类
            articleGrid.classList.add('stagger-children');
            articleGrid.innerHTML = ''; // 清除现有内容

            articles.forEach((article, index) => {
                const card = createArticleCard(article);
                articleGrid.appendChild(card);

                // 延迟添加淡入动画
                setTimeout(() => {
                    const cardElement = articleGrid.children[index];
                    if (cardElement) {
                        cardElement.classList.add('fade-in');
                    }
                }, index * 100);
            });
        }
    } catch (error) {
        console.error('获取文章失败:', error);
        articleGrid.innerHTML = `
            <div class="error-message">
                <div class="error-icon"><i class="fas fa-exclamation-triangle"></i></div>
                <h3>加载失败</h3>
                <p>无法加载文章列表，请检查网络连接后重试</p>
                <button onclick="fetchAndRenderArticles(activeSlug)" class="retry-button">
                    <i class="fas fa-sync-alt"></i> 重试
                </button>
            </div>
        `;
    } finally {
        spinner.style.display = 'none';
    }
}

// 创建文章卡片
function createArticleCard(article) {
    const template = document.getElementById('articleCardTemplate');
    const cardClone = template.content.cloneNode(true);

    const cardContainer = cardClone.querySelector('.card-flip-container');
    const frontLink = cardClone.querySelector('.card-front .article-card');
    const backLink = cardClone.querySelector('.read-more-btn');

    const title = cardClone.querySelector('.article-title');
    const backTitle = cardClone.querySelector('.back-title');
    const excerpt = cardClone.querySelector('.article-excerpt');
    const authorName = cardClone.querySelector('.author-name');
    const backAuthorName = cardClone.querySelector('.back-author-name');
    const authorAvatar = cardClone.querySelector('.author-avatar');
    const backAvatar = cardClone.querySelector('.back-avatar');

    const viewCount = cardClone.querySelector('.view-count');
    const likeCount = cardClone.querySelector('.like-count');
    const modifiedDate = cardClone.querySelector('.modified-date');
    const backViewCount = cardClone.querySelector('.back-view-count');
    const backLikeCount = cardClone.querySelector('.back-like-count');

    const articleUrl = `/article/${article.slug}`;
    frontLink.href = articleUrl;
    backLink.href = articleUrl;

    title.textContent = article.title;
    backTitle.textContent = article.title;
    excerpt.textContent = article.excerpt || '暂无摘要...';
    authorName.textContent = article.userName || '佚名';
    backAuthorName.textContent = article.userName || '佚名';

    const avatarUrl = article.avatarUrl || '/img/default01.jpg';
    authorAvatar.src = avatarUrl;
    backAvatar.src = avatarUrl;

    viewCount.textContent = article.viewCount !== undefined ? article.viewCount : 0;
    backViewCount.textContent = article.viewCount !== undefined ? article.viewCount : 0;
    likeCount.textContent = article.likes !== undefined ? article.likes : 0;
    backLikeCount.textContent = article.likes !== undefined ? article.likes : 0;

    if (article.gmtModified) {
        const modDate = new Date(article.gmtModified);
        const formattedModDate = `${modDate.getFullYear()}-${String(modDate.getMonth() + 1).padStart(2, '0')}-${String(modDate.getDate()).padStart(2, '0')}`;
        modifiedDate.textContent = formattedModDate;
    } else {
        modifiedDate.textContent = '未知';
    }

    cardContainer.addEventListener('click', function(e) {
        const isLink = e.target.tagName === 'A' || e.target.closest('a');
        if (isLink) {
            return;
        }
        e.preventDefault();
        e.stopPropagation();
        this.classList.toggle('flipped');
    });

    return cardClone;
}