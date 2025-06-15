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

// 自定义toast函数
let toastTimeout;

function showToast(title, message, icon = 'info-circle', iconClass = 'text-info') {
    console.log('显示自定义toast消息:', title, message, icon, iconClass);
    
    // 清除任何可能存在的超时
    if (toastTimeout) {
        clearTimeout(toastTimeout);
    }
    
    // 获取toast元素
    const toast = document.getElementById('customToast');
    const toastTitle = document.getElementById('customToastTitle');
    const toastMessage = document.getElementById('customToastMessage');
    const toastIcon = document.getElementById('customToastIcon');
        
    // 设置内容
    toastTitle.textContent = title;
    toastMessage.textContent = message;
    
    // 设置图标
    toastIcon.className = `fas fa-${icon} ${iconClass} mr-2`;
    
    // 显示toast
    toast.style.display = 'block';
    
    // 3秒后自动隐藏
    toastTimeout = setTimeout(hideCustomToast, 3000);
}

function hideCustomToast() {
    const toast = document.getElementById('customToast');
    if (toast) {
        toast.style.display = 'none';
    }
}

/**
 * 处理关注按钮点击
 * @param {HTMLElement} buttonElement - 被点击的按钮元素
 */
async function toggleFollow(buttonElement) {
    if (!buttonElement) return;
    
    // 获取目标用户ID（从页面的profile-header元素获取）
    const profileHeader = document.querySelector('.profile-header');
    const targetUserId = profileHeader ? profileHeader.getAttribute('data-user-id') : null;
    
    if (!targetUserId) {
        console.error('未找到目标用户ID');
        return;
    }
    
    console.log("Toggling follow for user ID:", targetUserId);
    
    // 检查当前登录用户ID
    const currentUserId = document.querySelector('meta[name="current-user-id"]')?.getAttribute("content");
    console.log("Current user ID:", currentUserId, "Target user ID:", targetUserId);
    
    // 如果是自己尝试关注自己
    if (currentUserId && currentUserId === targetUserId) {
        showToast('提示', '不能关注自己哦，试试关注其他作者吧！', 'exclamation-circle', 'text-warning');
        return; // 直接返回，不发送请求
    }
    
    // 1. 从meta标签中获取CSRF安全令牌
    const token = document.querySelector("meta[name='_csrf']").getAttribute("content");
    const header = document.querySelector("meta[name='_csrf_header']").getAttribute("content");
    
    // 2. 发送请求前，禁用按钮
    buttonElement.disabled = true;
    
    try {
        // 3. 构建API的URL
        const apiUrl = `/api/user/${targetUserId}/follow`;
        console.log("API URL:", apiUrl);
        
        const response = await fetch(apiUrl, {
            method: 'POST',
            headers: {
                [header]: token
            }
        });
        
        console.log("Response status:", response.status);
        
            if (!response.ok) {
            throw new Error(`服务器响应失败，状态码: ${response.status}`);
        }
        
        const data = await response.json();
        console.log("Response data:", JSON.stringify(data, null, 2));
        
        // 4. 根据后端返回的真实数据更新UI
        if (data.success) {
            const icon = buttonElement.querySelector('i');
            const textSpan = buttonElement.querySelector('span:last-child'); // 获取按钮内的文本span
            
            if (data.isFollowing) { // 假设后端返回 isFollowing 字段
                buttonElement.classList.remove('btn-warning');
                buttonElement.classList.add('btn-secondary');
                if (icon) icon.className = 'fas fa-user-check'; // 更新图标
                textSpan.textContent = ' 已关注'; // 更新文本
                updateFollowerCount(1);
            } else {
                buttonElement.classList.remove('btn-secondary');
                buttonElement.classList.add('btn-warning');
                if (icon) icon.className = 'fas fa-user-plus'; // 更新图标
                textSpan.textContent = ' 关注'; // 更新文本
                updateFollowerCount(-1);
            }
            
            // 如果后端返回了消息，显示toast提示
            if (data.message) {
                showToast('关注状态', data.message, data.isFollowing ? 'check-circle' : 'info-circle', data.isFollowing ? 'text-success' : 'text-info');
            }
        } else {
            // 处理后端返回操作失败的情况
            if (data.message) {
                showToast('提示', data.message, 'exclamation-circle', 'text-warning');
            } else {
                showToast('提示', '操作失败，请稍后再试。', 'exclamation-triangle', 'text-danger');
            }
        }
    } catch (error) {
        console.error('关注操作失败:', error);
        showToast('错误', '操作失败，请稍后重试。', 'exclamation-triangle', 'text-danger');
    } finally {
        // 5. 无论成功还是失败，最后都必须重新启用按钮
        buttonElement.disabled = false;
    }
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
            <div class="article-item" data-article-id="${article.articleId}" onclick="window.location.href='/article/${article.slug}'">
                <div class="article-content" style="cursor: pointer;">
                    <h3 class="article-title">
                        <span>${article.title}</span>
                    </h3>
                    <p class="article-excerpt">${article.excerpt || '暂无摘要'}</p>
                    <div class="article-meta">
                        <span class="article-date">
                            <i class="far fa-calendar-alt"></i>
                            <span>${formatDate(article.gmtModified)}</span>
                        </span>
                        <div class="article-stats">
                            <span class="article-stat article-views">
                                <i class="far fa-eye"></i>
                                <span>${article.viewCount || 0}</span>
                            </span>
                            <span class="article-stat article-likes" data-article-id="${article.articleId}" onclick="event.stopPropagation();">
                                <i class="${article.likes ? 'fas' : 'far'} fa-heart"></i>
                                <span>${article.likes || 0}</span>
                            </span>
                            <span class="article-stat article-comments">
                                <i class="far fa-comment"></i>
                                <span>${article.viewCount || 0}</span>
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
                <button class="btn ${user.isFollowing ? 'btn-secondary' : 'btn-warning'} btn-sm rounded-pill ms-3" 
                        style="min-width: 70px; padding: 5px 12px; border-radius: 50px !important;"
                        data-user-id="${user.id}" 
                        onclick="toggleFollow(this)">
                    <i class="${user.isFollowing ? 'fas fa-user-check' : 'fas fa-user-plus'}"></i>
                    ${user.isFollowing ? ' 已关注' : ' 关注'}
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
        button.addEventListener('click', function(event) {
            // 阻止事件冒泡，防止触发卡片的点击事件
            event.stopPropagation();
            
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
                showToast('错误', '操作失败，请稍后重试', 'exclamation-triangle', 'text-danger');
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

$(document).ready(function() {

    const changeCoverBtn = $('.change-cover-btn');
    const coverImageInput = $('#coverImageInput');
    const coverImageDisplay = $('#coverImageDisplay');

    if (!changeCoverBtn.length || !coverImageInput.length || !coverImageDisplay.length) {
        console.error("封面修改所需的部分元素未找到。");
        return;
    }

    // 存储原始的图片URL，以便上传失败时恢复
    const initialCoverSrc = coverImageDisplay.attr('src');

    changeCoverBtn.on('click', function() {
        coverImageInput.click();
    });

    coverImageInput.on('change', function(event) {
        const file = event.target.files[0];

        // 如果用户没选文件，则什么都不做
        if (!file) {
            return;
        }

        // A. 校验文件类型
        if (!file.type.startsWith('image/')) {
            alert("请选择有效的图片文件 (JPG, PNG等)。");
            this.value = '';
            return;
        }

        // B. 校验文件大小
        if (file.size > 5 * 1024 * 1024) {
            alert("封面图片大小不能超过5MB。");
            this.value = ''; // 清空选择
            return;
        }

        const reader = new FileReader();
        reader.onload = function(e) {
            // 将选择的图片在页面上预览
            coverImageDisplay.attr('src', e.target.result);
        }
        reader.readAsDataURL(file);
        uploadCoverImage(file);
    });

    /**
     * AJAX 上传函数
     * @param {File} file 要上传的文件对象
     */
    function uploadCoverImage(file) {
        const formData = new FormData();
        formData.append('coverImageFile', file);

        const token = $("meta[name='_csrf']").attr("content");
        const header = $("meta[name='_csrf_header']").attr("content");


        $.ajax({
            url: '/profile/update-cover',
            type: 'POST',
            data: formData,
            processData: false,
            contentType: false,
            beforeSend: function(xhr) {
                if (token && header) {
                    xhr.setRequestHeader(header, token);
                }
            },
            success: function(response) {
                if (response && response.success) {
                    coverImageDisplay.attr('src', response.newImageUrl);
                    showToast('封面更新成功！', 'success');
                } else {
                    coverImageDisplay.attr('src', initialCoverSrc);
                    showToast(response.message || '图片上传失败，请重试。', 'error');
                }
            },
            error: function() {
                coverImageDisplay.attr('src', initialCoverSrc);
                showToast('请求失败，请检查网络连接。', 'error');
            },
            complete: function() {
                coverImageInput.val('');
            }
        });
    }

});