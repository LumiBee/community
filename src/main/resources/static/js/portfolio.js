 document.addEventListener('DOMContentLoaded', function() {
    // 格式化日期显示
    formatDates();

    // 搜索功能
    const searchInput = document.getElementById('portfolioSearchInput');
    if (searchInput) {
        searchInput.addEventListener('input', function() {
            const searchValue = this.value.toLowerCase();
            const portfolioItems = document.querySelectorAll('.portfolio-item');

            portfolioItems.forEach(item => {
                const name = item.getAttribute('data-name').toLowerCase();
                const description = item.getAttribute('data-description') ?
                item.getAttribute('data-description').toLowerCase() : '';

                if (name.includes(searchValue) || description.includes(searchValue)) {
                    item.style.display = '';
                } else {
                    item.style.display = 'none';
                }
            });

            // 检查是否有可见的作品集
            const visibleItems = document.querySelectorAll('.portfolio-item:not([style*="display: none"])');
            const noResultsElement = document.getElementById('no-results');

            if (visibleItems.length === 0 && searchValue !== '') {
            // 如果没有搜索结果且有搜索词，显示无结果提示
                if (!noResultsElement) {
                    const noResults = document.createElement('div');
                    noResults.id = 'no-results';
                    noResults.className = 'no-articles';
                    noResults.innerHTML = `
                                            <div class="no-articles-icon"><i class="fas fa-search"></i></div>
                                            <h3>未找到匹配的作品集</h3>
                                            <p>尝试使用其他关键词搜索</p>
                                        `;
                    document.getElementById('portfolioGrid').appendChild(noResults);
                }
            } else if (noResultsElement) {
                // 如果有结果或没有搜索词，移除无结果提示
                noResultsElement.remove();
            }
        });
    }

    // 添加页面加载动画
    setTimeout(() => {
        document.querySelectorAll('.article-card').forEach((card, index) => {
            setTimeout(() => {
                card.classList.add('fade-in');
            }, index * 100);
        });
    }, 300);

    // 作品集卡片点击跳转逻辑
     const portfolioGrid = document.getElementById('portfolioGrid'); // 获取父容器
     if (portfolioGrid) {
         portfolioGrid.addEventListener('click', function(e) {  // 在父容器上绑定单一事件
             // 判断被点击的目标是否为卡片元素或其子元素
             const card = e.target.closest('.portfolio-item');
             if (!card) return ;

             // 阻止默认行为，避免直接跳转
             e.preventDefault();

             const href = card.getAttribute('href');
             if (href) {
                 card.classList.add('card-clicked');
                 setTimeout(() => {
                     window.location.href = href;
                 }, 150);
             }
         });
     }

 });

// 格式化日期函数
function formatDates() {
    const dateElements = document.querySelectorAll('.publish-date');

    dateElements.forEach(element => {
        const dateValue = element.getAttribute('data-date');
        if (dateValue) {
            try {
                const date = new Date(dateValue);
                if (!isNaN(date.getTime())) {
                    const formattedDate = `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')}`;
                    element.textContent = formattedDate;
                } else {
                    console.warn('Invalid date:', dateValue);
                    element.textContent = '未知日期';
                }
            } catch (e) {
                console.error('Error formatting date:', e);
                element.textContent = '未知日期';
            }
        } else {
            element.textContent = '未知日期';
        }
    });
}