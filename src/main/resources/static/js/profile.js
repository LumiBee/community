/**
 * profile.js - 个人空间页面交互功能
 */

document.addEventListener('DOMContentLoaded', function() {
    // 关注按钮功能
    initFollowButton();
    
    // 导航栏切换功能
    initNavigation();
    
    // 文章点赞功能
    initArticleLikes();
    
    // 技能进度条动画
    initSkillBars();
});

/**
 * 初始化关注按钮功能
 */
function initFollowButton() {
    const followBtn = document.querySelector('.btn-follow');
    if (!followBtn) return;
    
    followBtn.addEventListener('click', function() {
        const userId = this.getAttribute('data-user-id');
        const isFollowing = this.classList.contains('following');
        
        // 乐观UI更新
        if (isFollowing) {
            this.textContent = '关注';
            this.classList.remove('following');
            this.style.backgroundColor = '#ffda58';
            this.style.color = '#333';
            updateFollowerCount(-1);
        } else {
            this.textContent = '已关注';
            this.classList.add('following');
            this.style.backgroundColor = '#f0f0f0';
            this.style.color = '#555';
            updateFollowerCount(1);
        }
        
        // 发送关注/取消关注请求
        fetch(`/api/user/${userId}/${isFollowing ? 'unfollow' : 'follow'}`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'X-CSRF-TOKEN': document.querySelector('meta[name="_csrf"]').getAttribute('content')
            }
        })
        .then(response => {
            if (!response.ok) {
                throw new Error('关注操作失败');
            }
            return response.json();
        })
        .then(data => {
            console.log('关注状态更新成功', data);
        })
        .catch(error => {
            console.error('关注操作出错:', error);
            // 恢复原状态（如果请求失败）
            if (isFollowing) {
                followBtn.textContent = '已关注';
                followBtn.classList.add('following');
                followBtn.style.backgroundColor = '#f0f0f0';
                followBtn.style.color = '#555';
                updateFollowerCount(1);
            } else {
                followBtn.textContent = '关注';
                followBtn.classList.remove('following');
                followBtn.style.backgroundColor = '#ffda58';
                followBtn.style.color = '#333';
                updateFollowerCount(-1);
            }
            
            // 显示错误提示
            showToast('操作失败，请稍后重试', 'error');
        });
    });
}

/**
 * 更新粉丝数量
 * @param {number} change - 变化数量，1表示增加，-1表示减少
 */
function updateFollowerCount(change) {
    const followerCountElement = document.querySelector('.stat-item:nth-child(2) .stat-value');
    if (followerCountElement) {
        const currentCount = parseInt(followerCountElement.textContent, 10);
        followerCountElement.textContent = currentCount + change;
    }
}

/**
 * 初始化导航栏切换功能
 */
function initNavigation() {
    const navLinks = document.querySelectorAll('.profile-nav-link');
    const userId = document.querySelector('.profile-header').getAttribute('data-user-id');
    
    navLinks.forEach(link => {
        link.addEventListener('click', function(e) {
            e.preventDefault();
            
            // 更新活动状态
            navLinks.forEach(l => l.classList.remove('active'));
            this.classList.add('active');
            
            // 获取内容类型
            const contentType = this.getAttribute('data-content-type');
            
            // 显示加载状态
            const contentContainer = document.querySelector('.col-lg-8');
            contentContainer.innerHTML = '<div class="text-center py-5"><div class="spinner-border text-warning" role="status"><span class="visually-hidden">加载中...</span></div><p class="mt-3">正在加载内容...</p></div>';
            
            // 加载对应内容
            fetch(`/api/user/${userId}/content/${contentType}`)
                .then(response => {
                    if (!response.ok) {
                        throw new Error('内容加载失败');
                    }
                    return response.json();
                })
                .then(data => {
                    // 根据内容类型渲染不同的UI
                    switch(contentType) {
                        case 'articles':
                            renderArticles(data, contentContainer);
                            break;
                        case 'favorites':
                            renderFavorites(data, contentContainer);
                            break;
                        case 'following':
                            renderUsers(data, contentContainer, '关注');
                            break;
                        case 'followers':
                            renderUsers(data, contentContainer, '粉丝');
                            break;
                        default:
                            contentContainer.innerHTML = '<div class="alert alert-warning">未知内容类型</div>';
                    }
                })
                .catch(error => {
                    console.error('加载内容出错:', error);
                    contentContainer.innerHTML = '<div class="alert alert-danger">内容加载失败，请稍后重试</div>';
                });
        });
    });
}

/**
 * 渲染文章列表
 * @param {Array} articles - 文章数据
 * @param {HTMLElement} container - 容器元素
 */
function renderArticles(data, container) {
    if (!data.articles || data.articles.length === 0) {
        container.innerHTML = '<div class="alert alert-info">暂无文章</div>';
        return;
    }
    
    let html = '<div class="article-list">';
    
    data.articles.forEach(article => {
        html += `
            <div class="article-item" data-article-id="${article.id}">
                <div class="article-content">
                    <h3 class="article-title">
                        <a href="/article/${article.id}">${article.title}</a>
                    </h3>
                    <p class="article-excerpt">${article.summary || '暂无摘要'}</p>
                    <div class="article-meta">
                        <span class="article-date">
                            <i class="far fa-calendar-alt"></i>
                            <span>${formatDate(article.gmtCreate)}</span>
                        </span>
                        <div class="article-stats">
                            <span class="article-stat article-views">
                                <i class="far fa-eye"></i>
                                <span>${article.viewCount || 0}</span>
                            </span>
                            <span class="article-stat article-likes" data-article-id="${article.id}">
                                <i class="${article.isLiked ? 'fas' : 'far'} fa-heart"></i>
                                <span>${article.likeCount || 0}</span>
                            </span>
                            <span class="article-stat article-comments">
                                <i class="far fa-comment"></i>
                                <span>${article.commentCount || 0}</span>
                            </span>
                        </div>
                    </div>
                </div>
            </div>
        `;
    });
    
    html += '</div>';
    
    // 添加分页
    if (data.totalPages > 1) {
        html += renderPagination(data.currentPage, data.totalPages);
    }
    
    container.innerHTML = html;
    
    // 重新初始化文章点赞功能
    initArticleLikes();
}

/**
 * 渲染收藏列表
 * @param {Array} favorites - 收藏数据
 * @param {HTMLElement} container - 容器元素
 */
function renderFavorites(data, container) {
    if (!data.favorites || data.favorites.length === 0) {
        container.innerHTML = '<div class="alert alert-info">暂无收藏</div>';
        return;
    }
    
    let html = '<div class="article-list">';
    
    data.favorites.forEach(favorite => {
        html += `
            <div class="article-item">
                <div class="article-content">
                    <h3 class="article-title">
                        <a href="/article/${favorite.article.id}">${favorite.article.title}</a>
                    </h3>
                    <p class="article-excerpt">${favorite.article.summary || '暂无摘要'}</p>
                    <div class="article-meta">
                        <span class="article-date">
                            <i class="far fa-calendar-alt"></i>
                            <span>${formatDate(favorite.article.gmtCreate)}</span>
                        </span>
                        <div class="article-stats">
                            <span class="article-stat article-author">
                                <i class="far fa-user"></i>
                                <span>${favorite.article.author.name}</span>
                            </span>
                            <span class="article-stat article-favorite-time">
                                <i class="far fa-bookmark"></i>
                                <span>收藏于 ${formatDate(favorite.gmtCreate)}</span>
                            </span>
                        </div>
                    </div>
                </div>
            </div>
        `;
    });
    
    html += '</div>';
    
    // 添加分页
    if (data.totalPages > 1) {
        html += renderPagination(data.currentPage, data.totalPages);
    }
    
    container.innerHTML = html;
}

/**
 * 渲染用户列表（关注/粉丝）
 * @param {Array} users - 用户数据
 * @param {HTMLElement} container - 容器元素
 * @param {string} type - 类型（关注/粉丝）
 */
function renderUsers(data, container, type) {
    if (!data.users || data.users.length === 0) {
        container.innerHTML = `<div class="alert alert-info">暂无${type}</div>`;
        return;
    }
    
    let html = `<h3 class="mb-4">我的${type}</h3><div class="user-list">`;
    
    data.users.forEach(user => {
        html += `
            <div class="user-item d-flex align-items-center p-3 mb-3 bg-white rounded-3 shadow-sm">
                <img src="${user.avatarUrl || 'https://via.placeholder.com/100x100'}" alt="${user.name}" class="rounded-circle me-3" width="60" height="60">
                <div class="user-info flex-grow-1">
                    <h5 class="mb-1"><a href="/user/${user.id}" class="text-decoration-none">${user.name}</a></h5>
                    <p class="text-muted mb-1">@${user.name}</p>
                    <p class="small mb-0">${user.bio || '这个人很懒，还没有填写个人简介...'}</p>
                </div>
                <button class="profile-btn btn-follow ms-3" data-user-id="${user.id}">
                    ${user.isFollowing ? '已关注' : '关注'}
                </button>
            </div>
        `;
    });
    
    html += '</div>';
    
    // 添加分页
    if (data.totalPages > 1) {
        html += renderPagination(data.currentPage, data.totalPages);
    }
    
    container.innerHTML = html;
    
    // 重新初始化关注按钮
    initFollowButton();
}

/**
 * 渲染分页组件
 * @param {number} currentPage - 当前页码
 * @param {number} totalPages - 总页数
 * @returns {string} 分页HTML
 */
function renderPagination(currentPage, totalPages) {
    let html = `
        <div class="pagination-container">
            <ul class="pagination">
                <li class="page-item ${currentPage === 1 ? 'disabled' : ''}">
                    <a class="page-link" href="#" data-page="${currentPage - 1}" aria-label="Previous">
                        <i class="fas fa-chevron-left"></i>
                    </a>
                </li>
    `;
    
    // 计算显示的页码范围
    let startPage = Math.max(1, currentPage - 2);
    let endPage = Math.min(totalPages, startPage + 4);
    
    if (endPage - startPage < 4) {
        startPage = Math.max(1, endPage - 4);
    }
    
    for (let i = startPage; i <= endPage; i++) {
        html += `
            <li class="page-item ${i === currentPage ? 'active' : ''}">
                <a class="page-link" href="#" data-page="${i}">${i}</a>
            </li>
        `;
    }
    
    html += `
                <li class="page-item ${currentPage === totalPages ? 'disabled' : ''}">
                    <a class="page-link" href="#" data-page="${currentPage + 1}" aria-label="Next">
                        <i class="fas fa-chevron-right"></i>
                    </a>
                </li>
            </ul>
        </div>
    `;
    
    return html;
}

/**
 * 初始化文章点赞功能
 */
function initArticleLikes() {
    const likeButtons = document.querySelectorAll('.article-likes');
    
    likeButtons.forEach(button => {
        button.addEventListener('click', function() {
            const articleId = this.getAttribute('data-article-id');
            const likeIcon = this.querySelector('i');
            const likeCount = this.querySelector('span');
            const isLiked = likeIcon.classList.contains('fas');
            
            // 乐观UI更新
            if (isLiked) {
                likeIcon.classList.replace('fas', 'far');
                likeCount.textContent = parseInt(likeCount.textContent, 10) - 1;
            } else {
                likeIcon.classList.replace('far', 'fas');
                likeCount.textContent = parseInt(likeCount.textContent, 10) + 1;
                
                // 添加点赞动画
                likeIcon.classList.add('pulse-animation');
                setTimeout(() => {
                    likeIcon.classList.remove('pulse-animation');
                }, 1000);
            }
            
            // 发送点赞/取消点赞请求
            fetch(`/api/article/${articleId}/${isLiked ? 'unlike' : 'like'}`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                    'X-CSRF-TOKEN': document.querySelector('meta[name="_csrf"]').getAttribute('content')
                }
            })
            .then(response => {
                if (!response.ok) {
                    throw new Error('点赞操作失败');
                }
                return response.json();
            })
            .then(data => {
                console.log('点赞状态更新成功', data);
            })
            .catch(error => {
                console.error('点赞操作出错:', error);
                // 恢复原状态（如果请求失败）
                if (isLiked) {
                    likeIcon.classList.replace('far', 'fas');
                    likeCount.textContent = parseInt(likeCount.textContent, 10) + 1;
                } else {
                    likeIcon.classList.replace('fas', 'far');
                    likeCount.textContent = parseInt(likeCount.textContent, 10) - 1;
                }
                
                // 显示错误提示
                showToast('操作失败，请稍后重试', 'error');
            });
        });
    });
}

/**
 * 初始化技能进度条动画
 */
function initSkillBars() {
    const skillBars = document.querySelectorAll('.skill-progress');
    
    // 使用Intersection Observer检测元素是否进入视口
    const observer = new IntersectionObserver((entries) => {
        entries.forEach(entry => {
            if (entry.isIntersecting) {
                const targetWidth = entry.target.getAttribute('data-width') || entry.target.style.width;
                entry.target.style.width = '0';
                
                // 添加过渡效果
                entry.target.style.transition = 'width 1s ease-out';
                
                // 延迟一下以确保过渡效果生效
                setTimeout(() => {
                    entry.target.style.width = targetWidth;
                }, 100);
                
                // 停止观察已经动画的元素
                observer.unobserve(entry.target);
            }
        });
    }, { threshold: 0.2 });
    
    // 开始观察所有技能条
    skillBars.forEach(bar => {
        // 保存目标宽度
        const width = bar.style.width;
        bar.setAttribute('data-width', width);
        bar.style.width = '0';
        
        observer.observe(bar);
    });
}

/**
 * 显示提示消息
 * @param {string} message - 消息内容
 * @param {string} type - 消息类型 (success, error, info, warning)
 */
function showToast(message, type = 'info') {
    // 检查是否已有toast容器
    let toastContainer = document.querySelector('.toast-container');
    
    if (!toastContainer) {
        toastContainer = document.createElement('div');
        toastContainer.className = 'toast-container position-fixed bottom-0 end-0 p-3';
        document.body.appendChild(toastContainer);
    }
    
    // 创建toast元素
    const toastId = 'toast-' + Date.now();
    const toastHTML = `
        <div id="${toastId}" class="toast align-items-center text-white bg-${getToastBgClass(type)} border-0" role="alert" aria-live="assertive" aria-atomic="true">
            <div class="d-flex">
                <div class="toast-body">
                    ${message}
                </div>
                <button type="button" class="btn-close btn-close-white me-2 m-auto" data-bs-dismiss="toast" aria-label="Close"></button>
            </div>
        </div>
    `;
    
    toastContainer.insertAdjacentHTML('beforeend', toastHTML);
    
    const toastElement = document.getElementById(toastId);
    const toast = new bootstrap.Toast(toastElement, {
        autohide: true,
        delay: 3000
    });
    
    toast.show();
    
    // 自动移除toast元素
    toastElement.addEventListener('hidden.bs.toast', function() {
        this.remove();
    });
}

/**
 * 获取Toast的背景类名
 * @param {string} type - 消息类型
 * @returns {string} 背景类名
 */
function getToastBgClass(type) {
    switch (type) {
        case 'success': return 'success';
        case 'error': return 'danger';
        case 'warning': return 'warning';
        case 'info':
        default: return 'info';
    }
}

/**
 * 格式化日期
 * @param {string} dateString - 日期字符串
 * @returns {string} 格式化后的日期字符串
 */
function formatDate(dateString) {
    const date = new Date(dateString);
    return date.toLocaleDateString('zh-CN', {
        year: 'numeric',
        month: '2-digit',
        day: '2-digit'
    }).replace(/\//g, '-');
} 