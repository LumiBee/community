document.addEventListener('DOMContentLoaded', function() {
    const container = document.getElementById('tagBubbleContainer');
    if (!container) return;
    
    const bubbles = Array.from(container.getElementsByClassName('tag-bubble'));
    if (bubbles.length === 0) return;

    const containerWidth = container.offsetWidth;
    const containerHeight = container.offsetHeight;

    // 颜色调色板
    const colorPalette = [
        '#FF6B6B', '#4ECDC4', '#45B7D1', '#FED766', '#2AB7CA',
        '#F0B67F', '#FE4A49', '#547980', '#8A9B0F', '#C3D89F',
        '#FF9E9D', '#3D405B', '#81B29A', '#F2CC8F', '#E07A5F',
        '#D81E5B', '#F4A261', '#2A9D8F', '#E9C46A', '#264653'
    ];

    // 根据文章数计算泡泡大小的参数
    const baseSize = 70; // 最小泡泡的直径 (px)
    const maxSize = 130; // 最大泡泡的直径 (px)
    const countFactor = 1.2; // 每篇文章数增加多少像素直径

    const baseFontSize = 13; // 基础字体大小 (px)
    const maxFontSize = 18; // 最大字体大小 (px)
    const countFactorFont = 0.2; // 每篇文章数增加多少像素字体大小

    // 计算最大文章数
    let maxArticleCount = 0;
    bubbles.forEach(bubble => {
        const count = parseInt(bubble.getAttribute('data-count') || '0');
        if (count > maxArticleCount) {
            maxArticleCount = count;
        }
    });

    // 用于避免重叠的数组，记录已放置泡泡的区域
    let placedBubbles = [];

    // 首先根据文章数量排序泡泡，确保较大的泡泡先放置
    const sortedBubbles = [...bubbles].sort((a, b) => {
        const countA = parseInt(a.getAttribute('data-count') || '0');
        const countB = parseInt(b.getAttribute('data-count') || '0');
        return countB - countA; // 降序排列
    });

    sortedBubbles.forEach((bubble, index) => {
        const articleCount = parseInt(bubble.getAttribute('data-count') || '0');

        // 1. 设置颜色
        bubble.style.backgroundColor = colorPalette[index % colorPalette.length];

        // 2. 设置大小 (基于文章数)
        let diameter = baseSize + articleCount * countFactor;
        if (maxArticleCount > 0 && articleCount === maxArticleCount) { // 给文章最多的标签一个更大的尺寸
            diameter = Math.min(maxSize, diameter + 20);
        }
        diameter = Math.max(baseSize, Math.min(maxSize, diameter)); // 限制在min/max之间

        bubble.style.width = diameter + 'px';
        bubble.style.height = diameter + 'px';

        // 字体大小也可以根据泡泡大小动态调整
        let fontSize = baseFontSize + (diameter - baseSize) * countFactorFont;
        fontSize = Math.max(baseFontSize, Math.min(maxFontSize, fontSize)); // 限制在min/max之间

        bubble.style.fontSize = fontSize + 'px';

        const paddingValue = Math.max(2, diameter * 0.05); // 根据直径设置padding
        bubble.style.padding = paddingValue + 'px';

        // 3. 设置位置 (尝试避免重叠)
        let bestPos = null;
        let minOverlap = Infinity;
        
        // 尝试多个位置，选择重叠最小的
        for (let i = 0; i < 100; i++) {
            const x = Math.random() * (containerWidth - diameter);
            const y = Math.random() * (containerHeight - diameter);
            const pos = {
                x: x,
                y: y,
                radius: diameter / 2,
                right: x + diameter,
                bottom: y + diameter
            };
            
            const overlapAmount = calculateOverlap(pos, placedBubbles);
            if (overlapAmount < minOverlap) {
                minOverlap = overlapAmount;
                bestPos = pos;
                
                // 如果找到完全不重叠的位置，立即使用
                if (overlapAmount === 0) break;
            }
        }
        
        // 使用找到的最佳位置
        if (bestPos) {
            bubble.style.left = bestPos.x + 'px';
            bubble.style.top = bestPos.y + 'px';
            placedBubbles.push(bestPos);
        } else {
            // 如果实在找不到好位置，随机放置
            const x = Math.random() * (containerWidth - diameter);
            const y = Math.random() * (containerHeight - diameter);
            bubble.style.left = x + 'px';
            bubble.style.top = y + 'px';
        }

        // 添加轻微的随机旋转
        const rotation = Math.random() * 6 - 3; // -3到3度
        bubble.style.transform = `rotate(${rotation}deg)`;
    });

    // 计算重叠程度
    function calculateOverlap(newBubble, existingBubbles) {
        let totalOverlap = 0;
        for (let existing of existingBubbles) {
            const dx = newBubble.x + newBubble.radius - (existing.x + existing.radius);
            const dy = newBubble.y + newBubble.radius - (existing.y + existing.radius);
            const distance = Math.sqrt(dx * dx + dy * dy);
            const minDistance = newBubble.radius + existing.radius;
            
            if (distance < minDistance) {
                totalOverlap += (minDistance - distance);
            }
        }
        return totalOverlap;
    }

    // 为部分泡泡添加动画
    const animatedBubbles = bubbles.filter((_, index) => index % 3 === 0); // 每3个泡泡中选1个添加动画
    
    animatedBubbles.forEach((bubble) => {
        const floatY = Math.random() * 8 + 3; // 3-11px
        
        bubble.animate([
            { transform: `translateY(0px)` },
            { transform: `translateY(-${floatY}px)` },
            { transform: `translateY(0px)` }
        ], {
            duration: 3000 + Math.random() * 2000, // 3-5秒
            iterations: Infinity,
            direction: 'alternate',
            easing: 'ease-in-out',
            delay: Math.random() * 1000
        });
    });
});