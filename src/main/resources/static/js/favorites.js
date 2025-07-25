import { showToast, showConfirmModal} from "./common-utils.js";

"use strict";

// 全局状态变量，用于存储当前选中的收藏夹ID
let activeFolderId = null;

/**
 * 页面加载完成后执行的主函数
 */
function initializePage() {
    // 绑定搜索框事件
    const searchInput = document.getElementById('favoriteSearchInput');
    if (searchInput) {
        searchInput.addEventListener('input', handleFavoriteSearch);
    }

    // 绑定“添加收藏夹”按钮事件
    const addBtn = document.querySelector('.add-collection-btn');
    if (addBtn) {
        addBtn.onclick = showAddCollectionModal;
    }

    // 绑定模态框中“确认添加”按钮的事件
    const confirmAddBtn = document.querySelector('#addCollectionModal .modern-create-btn');
    if (confirmAddBtn) {
        confirmAddBtn.onclick = handleAddNewCollection;
    }

    // 页面加载时，首先获取并渲染收藏夹列表
    fetchAndRenderFolders();
}

/**
 * 从后端获取收藏夹列表并渲染到侧边栏
 * @param {boolean} selectLast - 是否选中最后一个，默认为 false
 */
async function fetchAndRenderFolders(selectLast = false) {
    const listContainer = document.getElementById('favorite-folders-list');
    const spinner = document.getElementById('sidebar-loading-spinner');
    if (!listContainer || !spinner) return;

    spinner.style.display = 'block';
    listContainer.innerHTML = '';
    listContainer.appendChild(spinner);

    try {
        const response = await fetch('/api/favorites/my-folders');
        if (!response.ok) throw new Error(`获取收藏夹列表失败: ${response.status}`);

        const folders = await response.json();
        spinner.style.display = 'none';
        listContainer.innerHTML = '';

        if (!folders || folders.length === 0) {
            listContainer.innerHTML = '<li class="sidebar-item text-muted">暂无收藏夹</li>';
            renderArticles([]);
        } else {
            folders.forEach(folder => {
                const listItem = createFolderListItem(folder);
                listContainer.appendChild(listItem);
            });

            // 根据 selectLast 参数决定选中哪一个
            let elementToSelect = null;
            if (selectLast) {
                // 选中最后一个
                elementToSelect = listContainer.querySelector('.sidebar-item:last-child');
            } else {
                // 默认选中第一个
                elementToSelect = listContainer.querySelector('.sidebar-item:first-child');
            }

            if (elementToSelect) {
                elementToSelect.click();
            }
        }
    } catch (error) {
        console.error(error);
        spinner.style.display = 'none';
        listContainer.innerHTML = `<li class="sidebar-item text-danger">加载失败，请重试</li>`;
    }
}

/**
 * 根据收藏夹数据，使用模板创建侧边栏列表项
 * @param {object} folder - 收藏夹数据对象
 * @returns {HTMLElement|null} - 创建的 <li> 元素，如果模板不存在则返回 null
 */
function createFolderListItem(folder) {
    const template = document.getElementById('favoriteFolderTemplate');
    if (!template) {
        console.error('错误: 未找到 ID 为 "favoriteFolderTemplate" 的模板!');
        return null;
    }

    const clone = template.content.cloneNode(true);
    const listItem = clone.querySelector('.sidebar-item');
    const folderNameSpan = clone.querySelector('.folder-name span');
    const editBtn = clone.querySelector('.btn-edit-folder');
    const deleteBtn = clone.querySelector('.btn-delete-folder');

    // 填充数据
    listItem.dataset.folderId = folder.id;
    folderNameSpan.textContent = folder.name || '未命名';

    // 绑定事件
    listItem.addEventListener('click', () => handleFolderClick(listItem, folder.id, folder.name));
    editBtn.addEventListener('click', (event) => handleEditFolder(event, folder.id, folder.name));
    deleteBtn.addEventListener('click', (event) => handleDeleteFolder(event, folder.id, folder.name));

    return listItem;
}

/**
 * 处理收藏夹点击事件
 * @param {HTMLElement} element - 被点击的 <li> 元素
 * @param {number} folderId - 收藏夹ID
 * @param {string} folderName - 收藏夹名称
 */
function handleFolderClick(element, folderId, folderName) {
    if (activeFolderId === folderId) return; // 如果已经是激活状态，则不重复加载

    activeFolderId = folderId;

    // 更新侧边栏UI
    document.querySelectorAll('#favorite-folders-list .sidebar-item').forEach(item => {
        item.classList.remove('active');
    });
    element.classList.add('active');

    // 更新主内容区标题
    const titleElement = document.getElementById('favoriteListTitle');
    if(titleElement) titleElement.textContent = `(${folderName})`;

    // 获取并渲染该收藏夹内的文章
    fetchAndRenderArticles(folderId);
}

/**
 * 处理编辑收藏夹按钮点击事件
 * @param {Event} event - 点击事件对象
 * @param {number} folderId - 收藏夹ID
 * @param {string} folderName - 当前收藏夹名称
 */
function handleEditFolder(event, folderId, folderName) {
    event.stopPropagation(); // 阻止事件冒泡，防止触发父元素的点击事件（即切换收藏夹）

    console.log(`点击了编辑按钮，ID: ${folderId}, 名称: ${folderName}`);
    // 弹出输入框让用户输入新名称
    const newName = prompt(`请输入新的收藏夹名称：`, folderName);

    if (newName && newName.trim() !== '' && newName !== folderName) {
        // 在这里调用您的后端API来更新收藏夹名称
        alert(`您输入了新名称: "${newName}"\n接下来请在这里实现调用后端API的逻辑。`);
        // 示例:
        // updateFolderNameOnServer(folderId, newName);
    }
}

/**
 * 根据收藏夹ID，从后端获取文章列表并渲染
 * @param {number} folderId - 收藏夹ID
 */
async function fetchAndRenderArticles(folderId) {
    const articleGrid = document.getElementById('articleGrid');
    const spinner = document.getElementById('loadingSpinner');
    if (!articleGrid || !spinner) return;

    spinner.style.display = 'block';
    articleGrid.innerHTML = '';

    try {
        const response = await fetch(`/api/favorites/details/${folderId}`);
        if (!response.ok) throw new Error(`获取文章列表失败: ${response.status}`);

        // 从返回的对象中，提取出 articles 数组
        const folderDetails = await response.json();
        const articles = folderDetails.articles;

        // 确保提取出的是一个数组
        if (!Array.isArray(articles)) {
            console.warn("API返回的数据中 'articles' 字段不是一个数组:", folderDetails);
            renderArticles([]); // 如果不是数组，则视为空列表
            return;
        }
        renderArticles(articles);

    } catch (error) {
        console.error(error);
        renderArticles(null, true); // 传入错误标记
    } finally {
        spinner.style.display = 'none';
    }
}

/**
 * 将文章数据显示在主内容区
 * @param {Array|null} articles - 文章数据列表。如果为null，表示出错。
 * @param {boolean} isError - 是否为错误状态
 */
function renderArticles(articles, isError = false) {
    const articleGrid = document.getElementById('articleGrid');
    articleGrid.innerHTML = '';

    if (isError) {
        const errorTemplate = document.getElementById('errorTemplate');
        if (errorTemplate) {
            const errorContent = errorTemplate.content.cloneNode(true);
            errorContent.querySelector('.retry-button').onclick = () => fetchAndRenderArticles(activeFolderId);
            articleGrid.appendChild(errorContent);
        }
        return;
    }

    if (!articles || articles.length === 0) {
        const emptyTemplate = document.getElementById('emptyFavoriteTemplate');
        if (emptyTemplate) articleGrid.appendChild(emptyTemplate.content.cloneNode(true));
        return;
    }

    articles.forEach((article, index) => {
        setTimeout(() => {
            const card = createFavoriteCard(article);
            articleGrid.appendChild(card);
            setTimeout(() => card.classList.add('fade-in'), 50);
        }, index * 80);
    });
}

/**
 * 处理删除收藏夹按钮点击事件
 * @param {Event} event - 点击事件对象
 * @param {number} folderId - 收藏夹ID
 * @param {string} folderName - 收藏夹名称
 */
async function handleDeleteFolder(event, folderId, folderName) {
    event.stopPropagation(); // 阻止事件冒泡

    const confirmed = await showConfirmModal(
        '确认删除',
        `您确定要删除收藏夹 "<strong>${folderName}</strong>" 吗？<br>此操作不可撤销！`
    );
    if (!confirmed) return;


    const token = document.querySelector("meta[name='_csrf']")?.getAttribute("content");
    const header = document.querySelector("meta[name='_csrf_header']")?.getAttribute("content");

    if (!token || !header) {
        alert('安全令牌丢失，无法删除。请刷新页面后重试。');
        return;
    }

    try {
        const response = await fetch(`/api/favorites/remove-folder/${folderId}`, {
            method: 'DELETE',
            headers: {
                [header]: token
            }
        });

        if (response.ok) {
            showToast(`收藏夹 "${folderName}" 已成功删除，请刷新页面刷新目录`, 'success');
            await fetchAndRenderFolders();
        } else {
            console.error('删除失败，服务器响应:', response.status);
            showToast(`删除收藏夹 "${folderName}" 失败，请稍后重试。`, 'error');
        }
    } catch (error) {
        console.error('删除收藏夹时发生网络错误:', error);
        showToast('删除失败，请检查您的网络连接。', 'error');
    }
}

/**
 * 处理添加新收藏夹的逻辑
 * @param {Event} event - 表单提交事件
 */
async function handleAddNewCollection() {
    const collectionNameInput = document.getElementById('collectionName');
    const name = collectionNameInput.value.trim();
    const token = document.querySelector("meta[name='_csrf']")?.getAttribute("content");
    const header = document.querySelector("meta[name='_csrf_header']")?.getAttribute("content");

    if (!name) {
        alert('请输入收藏夹名称');
        return;
    }

    const confirmBtn = document.querySelector('#addCollectionModal .modern-create-btn');
    if (!confirmBtn) return;

    const originalBtnContent = confirmBtn.innerHTML;
    confirmBtn.disabled = true;
    confirmBtn.innerHTML = '<span class="spinner-border spinner-border-sm" role="status" aria-hidden="true"></span> 创建中...';

    try {
        const response = await fetch('/api/favorites/create-folder', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                [header]: token
            },
            body: JSON.stringify({ favoriteName: name })
        });

        if (response.ok) {
            showToast(`收藏夹 "${name}" 已成功创建。`, 'success');
            const updatedFolders = await response.json();
            renderFoldersList(updatedFolders);
            if(window.jQuery) $('#addCollectionModal').modal('hide');
        } else {
            console.error('创建失败，服务器响应:', response.status);
            showToast(`创建收藏夹 "${name}" 失败，请稍后重试。`, 'error');
        }


    } catch (error) {
        console.error(error);
        alert('创建收藏夹失败，请稍后重试。');
    } finally {
        confirmBtn.disabled = false;
        confirmBtn.innerHTML = originalBtnContent;
        if (collectionNameInput) collectionNameInput.value = '';
    }
}

function renderFoldersList(folders) {
    const listContainer = document.getElementById('favorite-folders-list');
    listContainer.innerHTML = ''; // 清空

    if (!folders || folders.length === 0) {
        listContainer.innerHTML = '<li class="sidebar-item text-muted">暂无收藏夹</li>';
        renderArticles([]);
    } else {
        folders.forEach(folder => {
            const listItem = createFolderListItem(folder);
            listContainer.appendChild(listItem);
        });
        // 自动点击第一个
        const firstFolderElement = listContainer.querySelector('.sidebar-item');
        if (firstFolderElement) {
            firstFolderElement.click();
        }
    }
}
function showAddCollectionModal() {
    if (window.jQuery) $('#addCollectionModal').modal('show');
}

function handleFavoriteSearch(event) {
    const filterValue = event.target.value.toLowerCase().trim();
    const favoriteItems = document.querySelectorAll('#articleGrid .book-item');
    let visibleCount = 0;

    favoriteItems.forEach(item => {
        const title = item.querySelector('.book-title')?.textContent.toLowerCase() || '';
        const description = item.querySelector('.book-description')?.textContent.toLowerCase() || '';

        // 如果标题或描述中包含过滤词，则显示卡片，否则隐藏
        if (title.includes(filterValue) || description.includes(filterValue)) {
            item.style.display = '';
            visibleCount++;
        } else {
            item.style.display = 'none';
        }
    });

    // 检查过滤后是否还有可见的卡片
    const emptySearchElement = document.querySelector('.empty-search-result');
    if (visibleCount === 0 && filterValue !== '') {
        // 如果没有可见项且搜索词不为空，则显示“无结果”提示
        if (!emptySearchElement) {
            const articleGrid = document.getElementById('articleGrid');
            if (articleGrid) {
                const emptyResult = document.createElement('div');
                emptyResult.className = 'empty-search-result';
                emptyResult.innerHTML = `
                <div class="empty-search-icon"><i class="fas fa-search"></i></div>
                <h3>未找到匹配的收藏</h3>
                <p>尝试使用其他关键词搜索</p>
            `;
                articleGrid.appendChild(emptyResult);
            }
        }
    } else {
        // 如果有可见项，则移除“无结果”提示
        if (emptySearchElement) {
            emptySearchElement.remove();
        }
    }
}

function createFavoriteCard(article) {
    const template = document.getElementById('articleFavoriteTemplate');
    if (!template) {
        console.error('错误：找不到ID为 "articleFavoriteTemplate" 的HTML模板！');
        return null; // 返回 null，让调用者处理
    }

    const cardClone = template.content.cloneNode(true);

    // 获取卡片内部的各个元素
    const bookItem = cardClone.querySelector('.book-item');
    const link = cardClone.querySelector('a');
    const titleElement = cardClone.querySelector('.book-title');
    const descriptionElement = cardClone.querySelector('.book-description');
    const authorNameElement = cardClone.querySelector('.author-name');
    const authorAvatarElement = cardClone.querySelector('.author-avatar');
    const favoriteDateElement = cardClone.querySelector('.favorite-date');
    const bookCover = cardClone.querySelector('.book-cover');

    const name = article.title || '无标题';
    const description = article.excerpt || '暂无描述...';
    const url = `/article/${article.slug}`;

    // 填充数据到元素中
    if(link) link.href = url;
    if(titleElement) titleElement.textContent = name;
    if(descriptionElement) descriptionElement.textContent = description;

    // 填充作者信息
    authorNameElement.textContent = article.userName || '佚名';
    authorAvatarElement.src = article.avatarUrl || '/img/default01.jpg';

    // 格式化并填充日期
    if (article.gmtModified && favoriteDateElement) {
        try {
            const date = new Date(article.gmtModified);
            // 格式化为 YYYY-MM-DD
            favoriteDateElement.textContent = `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')}`;
        } catch (e) {
            console.error('日期格式化失败:', article.gmtModified, e);
            favoriteDateElement.textContent = '未知日期';
        }
    }

    // 设置统一的封面样式
    if(bookCover) {
        const existingIcon = bookCover.querySelector('.book-icon');
        if(existingIcon) existingIcon.remove();
        bookCover.style.backgroundImage = "linear-gradient(45deg, rgba(189, 195, 199, 0.8), rgba(44, 62, 80, 0.9))";
        bookCover.style.backgroundSize = "cover";
        bookCover.innerHTML += '<div class="book-icon"><i class="fas fa-bookmark"></i></div>';
    }

    return bookItem; // 返回最终创建的卡片元素
}

// --- 脚本入口 ---
if (document.readyState === 'loading') {
    document.addEventListener('DOMContentLoaded', initializePage);
} else {
    initializePage();
}
