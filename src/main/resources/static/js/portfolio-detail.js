document.addEventListener('DOMContentLoaded', function() {
    // 页面加载完成后立即执行的初始化操作
    initializePage();

    // 设置事件监听器
    setupEventListeners();
});

/**
 * 页面初始化函数
 * @description 负责执行页面加载时就需要完成的任务，如日期格式化和动画效果。
 */
function initializePage() {
    // 格式化所有需要显示的日期
    formatDates();

    // 为文章卡片添加入场动画
    setTimeout(() => {
        document.querySelectorAll('.article-card').forEach((card) => {
            card.classList.add('fade-in');
        });
    }, 100);
}

/**
 * 设置事件监听器
 * @description 将所有的事件监听器集中在此处管理，包括使用事件委托的监听器。
 */
function setupEventListeners() {
    // 搜索功能：监听输入框的 input 事件
    const searchInput = document.getElementById('articleSearchInput');
    if (searchInput) {
        searchInput.addEventListener('input', handleSearch);
    }

    // --- 排序功能 ---
    // 将点击事件监听器添加到排序选项的父容器上
    const sortContainer = document.getElementById('sortOptionsContainer');
    if (sortContainer) {
        sortContainer.addEventListener('click', handleSort);
    }

    // --- 文章卡片点击 ---
    // 将点击事件监听器添加到文章卡片的网格容器上
    const articleGrid = document.getElementById('articleGrid');
    if (articleGrid) {
        articleGrid.addEventListener('click', handleArticleClick);
    }
}

/**
 * 处理搜索逻辑
 * @param {Event} e - 事件对象
 */
function handleSearch(e) {
    const searchValue = e.target.value.toLowerCase();
    const articleCards = document.querySelectorAll('.article-card');

    articleCards.forEach(card => {
        const title = card.getAttribute('data-title').toLowerCase();
        // 如果标题包含搜索词，则显示，否则隐藏
        card.style.display = title.includes(searchValue) ? '' : 'none';
    });

    // 检查搜索结果是否为空，并显示相应的提示
    checkAndDisplayEmptyState(searchValue);
}

/**
 * 处理排序逻辑
 * @param {Event} e - 点击事件对象
 */
function handleSort(e) {
    // 阻止 a 标签的默认跳转行为
    e.preventDefault();

    // 使用 .closest() 确保我们点击的是排序选项或其子元素
    const sortOption = e.target.closest('.sort-option');

    // 如果点击的不是一个排序选项，则直接返回
    if (!sortOption) return;

    const sortType = sortOption.getAttribute('data-sort');
    const articleGrid = document.getElementById('articleGrid');
    const articles = Array.from(articleGrid.querySelectorAll('.article-card'));

    // 根据排序类型对文章数组进行排序
    articles.sort((a, b) => {
        const dateA = new Date(a.getAttribute('data-date'));
        const dateB = new Date(b.getAttribute('data-date'));
        return sortType === 'newest' ? dateB - dateA : dateA - dateB;
    });

    // 重新排列 DOM 中的文章卡片
    articles.forEach((article, index) => {
        // 重设动画相关的样式
        article.style.setProperty('--child-index', index);
        article.classList.remove('fade-in');
        articleGrid.appendChild(article);
    });

    // 延迟重新应用入场动画
    setTimeout(() => {
        articles.forEach(article => {
            article.classList.add('fade-in');
        });
    }, 50);

    // 更新排序按钮的文本提示
    document.getElementById('sortDropdown').innerHTML =
        `<i class="fas fa-sort"></i> ${sortType === 'newest' ? '最新发布' : '最早发布'}`;
}

/**
 * 处理文章卡片点击逻辑
 * @param {Event} e - 点击事件对象
 */
function handleArticleClick(e) {
    // 找到被点击的文章卡片 (<a> 标签)
    const card = e.target.closest('.article-card');

    // 如果没有找到卡片 (例如点击了卡片之间的空白区域)，则不执行任何操作
    if (!card) return;

    // 阻止链接的默认立即跳转行为，以便播放动画
    e.preventDefault();

    const href = card.getAttribute('href');
    if (href) {
        // 添加点击动画效果
        card.classList.add('card-clicked');

        // 延迟一小段时间后跳转页面，让动画有时间播放
        setTimeout(() => {
            window.location.href = href;
        }, 150);
    }
}

/**
 * 检查并根据需要显示/隐藏“无结果”的提示
 * @param {string} searchValue - 当前的搜索关键词
 */
function checkAndDisplayEmptyState(searchValue) {
    const articleGrid = document.getElementById('articleGrid');
    const visibleCards = document.querySelectorAll('.article-card:not([style*="display: none"])');
    let noResultsState = articleGrid.querySelector('.empty-state.no-results');

    // 如果没有可见卡片且搜索框不为空
    if (visibleCards.length === 0 && searchValue !== '') {
        // 如果“无结果”提示还不存在，则创建并添加它
        if (!noResultsState) {
            const noResults = document.createElement('div');
            noResults.className = 'empty-state no-results'; // 添加一个特殊的类名以区分
            noResults.innerHTML = `
                <div class="empty-icon">
                    <i class="fas fa-search"></i>
                </div>
                <h3 class="empty-title">未找到匹配的文章</h3>
                <p class="empty-description">尝试使用其他关键词搜索</p>
            `;
            articleGrid.appendChild(noResults);
        }
    } else if (noResultsState) {
        // 如果有可见卡片或搜索框为空，则移除“无结果”提示
        noResultsState.remove();
    }
}

/**
 * 格式化日期函数
 * @description 将所有 class 为 .publish-date 的元素的日期字符串格式化为 YYYY-MM-DD
 */
function formatDates() {
    const dateElements = document.querySelectorAll('.publish-date');
    dateElements.forEach(element => {
        const dateValue = element.getAttribute('data-date');
        if (dateValue) {
            try {
                const date = new Date(dateValue);
                // 检查日期是否有效
                if (!isNaN(date.getTime())) {
                    const formattedDate = `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')}`;
                    element.textContent = formattedDate;
                } else {
                    element.textContent = '未知日期';
                }
            } catch (e) {
                console.error('格式化日期时出错:', e);
                element.textContent = '未知日期';
            }
        } else {
            element.textContent = '未知日期';
        }
    });
}