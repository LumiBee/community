console.log('article-effects.js 文件已加载并开始执行');

// 在文档加载完成后初始化Bootstrap组件
$(document).ready(function() {
    // 初始化toast组件，但不自动显示
    $('.toast').toast({
        delay: 3000,
        autohide: true,
        animation: true,
        show: false // 确保不会自动显示
    });
    
    // 确保关闭按钮可以正常工作
    $('.toast .close').on('click', function() {
        $(this).closest('.toast').toast('hide');
    });
    
    // 返回顶部功能
    $(window).scroll(function() {
        if ($(this).scrollTop() > 100) {
            $('#backToTop').fadeIn();
        } else {
            $('#backToTop').fadeOut();
        }
    });

    $('#backToTop').click(function() {
        $('html, body').animate({scrollTop: 0}, 800);
        return false;
    });
    
    // 为所有pre标签添加复制按钮
    const allPres = document.querySelectorAll('article.article-post pre');

    allPres.forEach(pre => {
        if (pre.innerText.trim().length === 0) {
            return;
        }

        const button = document.createElement('button');
        button.className = 'copy-btn';
        button.textContent = '复制';

        pre.appendChild(button);

        button.addEventListener('click', (e) => {
            e.stopPropagation();
            const codeElement = pre.querySelector('code');
            const textToCopy = codeElement ? codeElement.innerText : pre.innerText;

            const originalButtonText = button.textContent;
            button.textContent = '';
            const cleanText = pre.innerText;
            button.textContent = originalButtonText;

            navigator.clipboard.writeText(cleanText).then(() => {
                button.textContent = '已复制!';
                setTimeout(() => {
                    button.textContent = '复制';
                }, 2000);
            }).catch(err => {
                console.error('复制失败', err);
            });
        });
    });
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

// 点赞功能
async function toggleLike(event) {
    // 1. 获取需要操作的页面元素
    const likeButton = event.currentTarget; // 获取当前被点击的按钮
    const likeCountSpan = document.getElementById('likeCount'); // 获取显示点赞数的元素
    const likeIcon = likeButton.querySelector('i'); // 获取按钮上的心形图标

    // 2. 从HTML中获取文章ID（假设Thymeleaf的article对象中包含id属性）
    const articleId = '[[${article.articleId}]]';

    // 3. 从meta标签中获取CSRF安全令牌
    const token = document.querySelector("meta[name='_csrf']").getAttribute("content");
    const header = document.querySelector("meta[name='_csrf_header']").getAttribute("content");

    // 4. 在发送请求前，先禁用按钮，防止用户重复点击，提升用户体验
    likeButton.disabled = true;

    try {
        // 5. 使用 fetch API 发送POST请求到后端接口
        const response = await fetch(`/api/article/${articleId}/like`, {
            method: 'POST',
            headers: {
                [header]: token
            }
        });

        // 6. 检查后端的响应是否成功（HTTP状态码是否为2xx）
        if (!response.ok) {
            throw new Error(`服务器响应失败，状态码: ${response.status}`);
        }

        // 7. 解析后端返回的JSON格式数据
        const data = await response.json();

        // 8. 根据后端返回的真实数据来更新前端UI
        if (data.success) {
            likeCountSpan.textContent = data.likeCount; // 使用服务器返回的最新点赞数更新页面

            // 根据服务器返回的最新点赞状态，更新按钮的视觉样式
            if (data.liked) {
                likeButton.classList.add('btn-danger'); // 按钮变为实心红色
                likeButton.classList.remove('btn-outline-danger'); // 移除线框样式
            } else {
                likeButton.classList.add('btn-outline-danger'); // 按钮变为线框样式
                likeButton.classList.remove('btn-danger'); // 移除实心红色样式
            }
        }

    } catch (error) {
        // 9. 如果在上述任何步骤中发生错误（网络问题或程序异常），在控制台打印
        console.error('点赞操作失败:', error);
    } finally {
        // 10. 无论请求成功还是失败，最后都必须重新启用按钮
        likeButton.disabled = false;
    }
}

async function toggleFollow(userId, buttonElement) {
    console.log("Toggling follow for author ID:", userId);

    // 检查当前登录用户ID
    const currentUserId = document.querySelector('meta[name="current-user-id"]')?.getAttribute("content");
    console.log("Current user ID:", currentUserId, "Author ID:", userId);
    
    // 如果是自己尝试关注自己
    if (currentUserId && currentUserId === userId) {
        showToast('提示', '不能关注自己喵，试试关注其他作者吧！', 'exclamation-circle', 'text-warning');
        return; // 直接返回，不发送请求
    }

    // 1. 从meta标签中获取CSRF安全令牌 (与toggleLike中相同)
    const token = document.querySelector("meta[name='_csrf']").getAttribute("content");
    const header = document.querySelector("meta[name='_csrf_header']").getAttribute("content");

    // 2. 发送请求前，禁用按钮 (可选，但推荐)
    buttonElement.disabled = true;

    try {
        // 3. 构建API的URL
        const apiUrl = `/api/user/${userId}/follow`;
        console.log("API URL:", apiUrl);
        
        const response = await fetch(apiUrl, {
            method: 'POST', // 通常关注/取消关注是POST请求
            headers: {
                [header]: token
            }
        });

        console.log("Response status:", response.status);

        if (!response.ok) {
            throw new Error(`服务器响应失败，状态码: ${response.status}`);
        }

        const data = await response.json(); // 后端返回 { success: true, isFollowing: true/false }
        console.log("Response data:", JSON.stringify(data, null, 2));

        // 4. 根据后端返回的真实数据更新UI
        if (data.success) {
            const icon = buttonElement.querySelector('i');
            const textSpan = buttonElement.querySelector('span:last-child'); // 获取按钮内的文本span

            if (data.isFollowing) { // 假设后端返回 isFollowing 字段
                buttonElement.classList.remove('btn-outline-primary');
                buttonElement.classList.add('btn-secondary');
                if (icon) icon.className = 'fas fa-user-check'; // 更新图标
                textSpan.textContent = ' 已关注'; // 更新文本
            } else {
                buttonElement.classList.remove('btn-secondary');
                buttonElement.classList.add('btn-outline-primary');
                if (icon) icon.className = 'fas fa-user-plus'; // 更新图标
                textSpan.textContent = ' 关注'; // 更新文本
            }

            // 如果后端返回了消息，显示toast提示
            if (data.message) {
                showToast('关注状态', data.message, data.isFollowing ? 'check-circle' : 'info-circle', data.isFollowing ? 'text-success' : 'text-info');
            }
        } else {
            // 处理后端返回操作失败的情况，包括自关注
            if (data.message) {
                // 使用toast显示消息
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

// 收藏功能
function toggleFavorite() {
    // TODO: 实现收藏功能
    alert('收藏功能开发中...');
}

// 分享功能
function shareToWechat() {
    // 微信分享逻辑
    alert('微信分享功能开发中...');
}

function shareToWeibo() {
    const url = encodeURIComponent(window.location.href);
    const title = encodeURIComponent(document.title);
    window.open(`http://service.weibo.com/share/share.php?url=${url}&title=${title}`);
}

function copyLink() {
    navigator.clipboard.writeText(window.location.href).then(function() {
        alert('链接已复制到剪贴板');
    });
}

// 滚动到评论区
function scrollToComments() {
    const commentsSection = document.getElementById('comments-section');
    if (commentsSection) {
        commentsSection.scrollIntoView({ behavior: 'smooth' });
        // 聚焦到评论输入框
        setTimeout(() => {
            const commentTextarea = document.getElementById('commentContent');
            if (commentTextarea) {
                commentTextarea.focus();
            }
        }, 800);
    }
}

// 评论功能
$(document).ready(function() {
    // 提交评论
    $('#commentForm').on('submit', function(e) {
        e.preventDefault();

        const commentContent = $('#commentContent').val().trim();
        if (!commentContent) {
            alert('评论内容不能为空');
            return;
        }

        const articleId = '[[${article.articleId}]]';
        const token = document.querySelector("meta[name='_csrf']").getAttribute("content");
        const header = document.querySelector("meta[name='_csrf_header']").getAttribute("content");

        $.ajax({
            url: `/api/article/${articleId}/comment`,
            type: 'POST',
            headers: {
                [header]: token,
                'Content-Type': 'application/json'
            },
            data: JSON.stringify({
                content: commentContent
            }),
            success: function(response) {
                if (response.success) {
                    // 刷新页面显示新评论
                    location.reload();
                } else {
                    alert(response.message || '评论失败，请稍后再试');
                }
            },
            error: function() {
                alert('评论失败，请检查网络连接');
            }
        });
    });

    // 回复评论
    $('.reply-btn').on('click', function() {
        const commentId = $(this).data('comment-id');
        const commentItem = $(this).closest('.comment-item');

        // 如果回复表单已存在，则移除
        if (commentItem.find('.reply-form').length > 0) {
            commentItem.find('.reply-form').remove();
            return;
        }

        // 创建回复表单
        const replyForm = $(`
				<div class="reply-form mt-2 mb-3">
					<div class="form-group">
						<textarea class="form-control reply-textarea" rows="2" placeholder="回复评论..."></textarea>
					</div>
					<div class="text-right">
						<button type="button" class="btn btn-sm btn-secondary cancel-reply-btn mr-2">取消</button>
						<button type="button" class="btn btn-sm btn-primary submit-reply-btn" data-comment-id="${commentId}">回复</button>
					</div>
				</div>
			`);

        // 添加到评论项中
        $(this).closest('.comment-actions').after(replyForm);

        // 聚焦到回复输入框
        replyForm.find('.reply-textarea').focus();

        // 取消回复
        replyForm.find('.cancel-reply-btn').on('click', function() {
            replyForm.remove();
        });

        // 提交回复
        replyForm.find('.submit-reply-btn').on('click', function() {
            const replyContent = replyForm.find('.reply-textarea').val().trim();
            if (!replyContent) {
                alert('回复内容不能为空');
                return;
            }

            const articleId = '[[${article.articleId}]]';
            const token = document.querySelector("meta[name='_csrf']").getAttribute("content");
            const header = document.querySelector("meta[name='_csrf_header']").getAttribute("content");

            $.ajax({
                url: `/api/comment/${commentId}/reply`,
                type: 'POST',
                headers: {
                    [header]: token,
                    'Content-Type': 'application/json'
                },
                data: JSON.stringify({
                    content: replyContent
                }),
                success: function(response) {
                    if (response.success) {
                        // 刷新页面显示新回复
                        location.reload();
                    } else {
                        alert(response.message || '回复失败，请稍后再试');
                    }
                },
                error: function() {
                    alert('回复失败，请检查网络连接');
                }
            });
        });
    });
});

document.addEventListener('DOMContentLoaded', function() {
    // 1. 标题区域渐入效果
    const jumbotron = document.querySelector('.jumbotron');
    if (jumbotron) {
        jumbotron.style.opacity = '0';
        jumbotron.style.transform = 'translateY(20px)';
        jumbotron.style.transition = 'opacity 0.8s ease, transform 0.8s ease';
        
        setTimeout(function() {
            jumbotron.style.opacity = '1';
            jumbotron.style.transform = 'translateY(0)';
        }, 200);
    }
    
    // 2. 文章内容中的标题添加装饰效果
    const articleHeadings = document.querySelectorAll('.article-content h2, .article-content h3');
    articleHeadings.forEach(heading => {
        // 为每个标题添加左侧装饰线
        heading.style.position = 'relative';
        heading.style.paddingLeft = '1rem';
        
        const decoration = document.createElement('div');
        decoration.style.position = 'absolute';
        decoration.style.left = '0';
        decoration.style.top = '0';
        decoration.style.bottom = '0';
        decoration.style.width = '4px';
        decoration.style.borderRadius = '2px';
        decoration.style.backgroundColor = '#ffda58';
        decoration.style.transform = 'scaleY(0)';
        decoration.style.transformOrigin = 'top';
        decoration.style.transition = 'transform 0.3s ease';
        
        heading.insertBefore(decoration, heading.firstChild);
        
        // 当滚动到标题位置时显示装饰线
        const observer = new IntersectionObserver((entries) => {
            entries.forEach(entry => {
                if (entry.isIntersecting) {
                    decoration.style.transform = 'scaleY(1)';
                }
            });
        }, { threshold: 0.2 });
        
        observer.observe(heading);
    });
    
    // 3. 图片懒加载和淡入效果
    const articleImages = document.querySelectorAll('.article-content img');
    articleImages.forEach(img => {
        // 添加懒加载
        img.loading = 'lazy';
        
        // 添加点击放大效果
        img.style.cursor = 'pointer';
        img.addEventListener('click', function() {
            const overlay = document.createElement('div');
            overlay.style.position = 'fixed';
            overlay.style.top = '0';
            overlay.style.left = '0';
            overlay.style.right = '0';
            overlay.style.bottom = '0';
            overlay.style.backgroundColor = 'rgba(0,0,0,0.9)';
            overlay.style.zIndex = '1000';
            overlay.style.display = 'flex';
            overlay.style.alignItems = 'center';
            overlay.style.justifyContent = 'center';
            overlay.style.padding = '2rem';
            overlay.style.opacity = '0';
            overlay.style.transition = 'opacity 0.3s ease';
            
            const imgClone = img.cloneNode();
            imgClone.style.maxWidth = '90%';
            imgClone.style.maxHeight = '90vh';
            imgClone.style.objectFit = 'contain';
            imgClone.style.boxShadow = 'none';
            imgClone.style.borderRadius = '0';
            
            overlay.appendChild(imgClone);
            document.body.appendChild(overlay);
            
            // 淡入效果
            setTimeout(() => {
                overlay.style.opacity = '1';
            }, 10);
            
            // 点击关闭
            overlay.addEventListener('click', function() {
                overlay.style.opacity = '0';
                setTimeout(() => {
                    document.body.removeChild(overlay);
                }, 300);
            });
        });
    });
    
    // 4. 平滑滚动到评论区
    const scrollToCommentsBtn = document.querySelector('button[onclick="scrollToComments()"]');
    if (scrollToCommentsBtn) {
        scrollToCommentsBtn.onclick = function(e) {
            e.preventDefault();
            const commentsSection = document.getElementById('comments-section');
            if (commentsSection) {
                commentsSection.scrollIntoView({ behavior: 'smooth' });
            }
        };
    }
    
    // 5. 分享按钮悬停效果
    const shareButtons = document.querySelectorAll('.share .btn');
    shareButtons.forEach(btn => {
        btn.addEventListener('mouseenter', function() {
            this.style.transform = 'translateY(-3px)';
            this.style.boxShadow = '0 4px 8px rgba(0,0,0,0.15)';
        });
        
        btn.addEventListener('mouseleave', function() {
            this.style.transform = '';
            this.style.boxShadow = '';
        });
    });
    
    // 6. 代码块语法高亮增强
    const codeBlocks = document.querySelectorAll('pre code');
    if (codeBlocks.length > 0 && typeof hljs !== 'undefined') {
        codeBlocks.forEach(block => {
            hljs.highlightBlock(block);
        });
    }
    
    // 7. 目录导航（如果文章较长）
    generateTableOfContents();
    
    // 8. 监听滚动，高亮当前阅读的标题
    setupScrollSpy();
});

/**
 * 生成文章目录导航（作为右侧边栏）
 */
function generateTableOfContents() {
    const articleContent = document.querySelector('.article-content');
    // 获取所有标题，包括h2、h3和h4
    const headings = articleContent ? articleContent.querySelectorAll('h2, h3, h4') : [];
    
    if (headings.length >= 1) {  // 只有当标题数量足够多时才生成目录
        // 1. 创建目录容器
        const tocContainer = document.createElement('div');
        tocContainer.className = 'toc-sidebar';
        tocContainer.id = 'toc-sidebar';
        
        // 2. 创建目录标题
        const tocHeader = document.createElement('div');
        tocHeader.className = 'toc-header';
        tocHeader.innerHTML = '<h5>目录</h5>';
        tocContainer.appendChild(tocHeader);
        
        // 3. 创建目录列表
        const tocList = document.createElement('ul');
        tocList.className = 'toc-list';
        
        // 4. 为每个标题创建目录项
        headings.forEach((heading, index) => {
            // 为每个标题添加ID
            const headingId = `heading-${index}`;
            heading.id = headingId;
            
            // 创建目录项
            const listItem = document.createElement('li');
            listItem.className = `toc-item toc-${heading.tagName.toLowerCase()}`;
            
            const link = document.createElement('a');
            link.href = `#${headingId}`;
            link.textContent = heading.textContent;
            link.addEventListener('click', function(e) {
                e.preventDefault();
                
                // 获取标题元素的位置
                const headingElement = document.getElementById(headingId);
                const headerOffset = 80; // 导航栏高度 + 一些额外空间
                const elementPosition = headingElement.getBoundingClientRect().top;
                const offsetPosition = elementPosition + window.pageYOffset - headerOffset;
                
                // 平滑滚动到标题位置
                window.scrollTo({
                    top: offsetPosition,
                    behavior: 'smooth'
                });
            });
            
            listItem.appendChild(link);
            tocList.appendChild(listItem);
        });
        
        tocContainer.appendChild(tocList);
        
        // 5. 创建右侧边栏
        const rightSidebar = document.createElement('div');
        rightSidebar.className = 'col-lg-2 d-none d-lg-block';
        rightSidebar.style.paddingLeft = '0';
        
        const stickyContainer = document.createElement('div');
        stickyContainer.className = 'sticky-top';
        stickyContainer.style.top = '80px'; // 导航栏高度 + 一些额外空间
        stickyContainer.appendChild(tocContainer);
        rightSidebar.appendChild(stickyContainer);
        
        // 6. 找到文章内容所在的行，并调整布局
        const articleRow = document.querySelector('.container-fluid.pt-4.pb-4 .row');
        if (articleRow) {
            // 将目录侧边栏添加到行的末尾
            articleRow.appendChild(rightSidebar);
            
            // 添加样式
            const style = document.createElement('style');
            style.textContent = `
                .toc-sidebar {
                    background-color: #f8f9fa;
                    border-radius: 8px;
                    padding: 1.25rem;
                    border-left: 4px solid #ffda58;
                    max-height: calc(100vh - 100px);
                    overflow-y: auto;
                    scrollbar-width: thin;
                }
                
                .toc-sidebar::-webkit-scrollbar {
                    width: 4px;
                }
                
                .toc-sidebar::-webkit-scrollbar-thumb {
                    background-color: rgba(0,0,0,0.2);
                    border-radius: 4px;
                }
                
                .toc-header h5 {
                    margin-top: 0;
                    margin-bottom: 1rem;
                    font-size: 1.1rem;
                    font-weight: 600;
                    color: #2c3e50;
                }
                
                .toc-list {
                    list-style: none;
                    padding-left: 0;
                    margin-bottom: 0;
                }
                
                .toc-item {
                    margin-bottom: 0.5rem;
                    line-height: 1.3;
                }
                
                .toc-item a {
                    color: #4a5568;
                    text-decoration: none;
                    transition: all 0.2s ease;
                    display: block;
                    padding: 0.25rem 0;
                    border-bottom: none;
                    font-size: 0.9rem;
                    overflow: hidden;
                    text-overflow: ellipsis;
                    white-space: nowrap;
                }
                
                .toc-item a:hover {
                    color: #ffda58;
                    transform: translateX(3px);
                }
                
                .toc-item.active a {
                    color: #ffda58;
                    font-weight: 600;
                }
                
                .toc-h3 {
                    padding-left: 1rem;
                    font-size: 0.85rem;
                }
                
                .toc-h4 {
                    padding-left: 2rem;
                    font-size: 0.8rem;
                }
                
                @media (max-width: 991.98px) {
                    .toc-sidebar {
                        display: none;
                    }
                }
            `;
            document.head.appendChild(style);
        }
    }
}

/**
 * 设置滚动监听，高亮当前阅读的标题
 */
function setupScrollSpy() {
    // 等待目录生成完成
    setTimeout(() => {
        const headings = document.querySelectorAll('.article-content h2, .article-content h3, .article-content h4');
        const tocItems = document.querySelectorAll('.toc-item');
        
        if (headings.length > 0 && tocItems.length > 0) {
            // 创建Intersection Observer
            const observer = new IntersectionObserver((entries) => {
                entries.forEach(entry => {
                    if (entry.isIntersecting) {
                        // 获取当前可见标题的ID
                        const id = entry.target.id;
                        
                        // 移除所有目录项的active类
                        tocItems.forEach(item => {
                            item.classList.remove('active');
                        });
                        
                        // 为当前标题对应的目录项添加active类
                        const activeItem = document.querySelector(`.toc-item a[href="#${id}"]`);
                        if (activeItem) {
                            activeItem.parentElement.classList.add('active');
                        }
                    }
                });
            }, { threshold: 0.3, rootMargin: '-100px 0px -66% 0px' });
            
            // 监听所有标题元素
            headings.forEach(heading => {
                observer.observe(heading);
            });
        }
    }, 500);
}