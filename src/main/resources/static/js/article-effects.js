/**
 * 文章页面增强效果
 */
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