document.addEventListener('DOMContentLoaded', function() {
    const container = document.getElementById('tagBubbleContainer');
    const bubbles = Array.from(container.getElementsByClassName('tag-bubble'));

    if (!container || bubbles.length === 0) {
        return;
    }

    const containerWidth = container.offsetWidth;
    const containerHeight = container.offsetHeight;

    // 预定义一个颜色数组 (可以更丰富)
    const colorPalette = [
        '#FF6B6B', '#4ECDC4', '#45B7D1', '#FED766', '#2AB7CA',
        '#F0B67F', '#FE4A49', '#547980', '#8A9B0F', '#C3D89F',
        '#FF9E9D', '#3D405B', '#81B29A', '#F2CC8F', '#E07A5F',
        '#D81E5B', '#F4A261', '#2A9D8F', '#E9C46A', '#264653'
    ];

    // 根据文章数计算泡泡大小的参数 (可调整)
    const baseSize = 60; // 最小泡泡的直径 (px)
    const maxSize = 120; // 最大泡泡的直径 (px)
    const countFactor = 1; // 每篇文章数增加多少像素直径 (大概值)

    const baseFontSize = 12; // 基础字体大小 (px)
    const maxFontSize = 18; // 最大字体大小 (px)
    const countFactorFont = 0.2; // 每篇文章数增加多少像素字体大小 (大概值)

    let maxArticleCount = 0;
    bubbles.forEach(bubble => {
        const count = parseInt(bubble.getAttribute('data-count') || '0');
        if (count > maxArticleCount) {
            maxArticleCount = count;
        }
    });

    // 用于简单避免重叠的数组，记录已放置泡泡的区域
    let placedBubbles = [];

    bubbles.forEach((bubble, index) => {
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

        const twoLinesHeight = fontSize * 1.3 * 2;
        const paddingVertical = 10;

        if ( (twoLinesHeight + paddingVertical) > diameter && fontSize > baseFontSize) {
            fontSize = Math.max(baseFontSize, fontSize - 1); // 尝试减小1px字体
            // 可以再做一次更精细的调整，或者接受一个稍小的字体
        }

        bubble.style.fontSize = fontSize + 'px';

        const paddingValue = Math.max(2, diameter * 0.05); // 根据直径设置padding
        bubble.style.padding = paddingValue + 'px';

        // 3. 设置位置 (简单的随机分布，尝试避免重叠)
        let attempts = 0;
        let newPos;
        do {
            const x = Math.random() * (containerWidth - diameter);
            const y = Math.random() * (containerHeight - diameter);
            newPos = {
                x: x,
                y: y,
                radius: diameter / 2,
                right: x + diameter,
                bottom: y + diameter
            };
            attempts++;
        } while (attempts < 100 && doesOverlap(newPos, placedBubbles)); // 尝试100次避免重叠

        bubble.style.left = newPos.x + 'px';
        bubble.style.top = newPos.y + 'px';
        bubble.style.zIndex = Math.floor(newPos.y / 10); // 根据y坐标给一个简单的z-index

        placedBubbles.push(newPos);

        // 添加轻微的随机旋转 (可选)
        // const rotation = Math.random() * 20 - 10; // -10到10度
        // bubble.style.transform = `rotate(${rotation}deg)`;
    });

    // 简单的重叠检测函数
    function doesOverlap(newBubble, existingBubbles) {
        for (let existing of existingBubbles) {
            const dx = newBubble.x + newBubble.radius - (existing.x + existing.radius);
            const dy = newBubble.y + newBubble.radius - (existing.y + existing.radius);
            const distance = Math.sqrt(dx * dx + dy * dy);
            if (distance < (newBubble.radius + existing.radius) * 0.8) { // 允许轻微重叠 (0.8因子)
                return true;
            }
        }
        return false;
    }

    // (可选) 添加轻微的漂浮动画
    bubbles.forEach((bubble, index) => {
        bubble.animate([
            { transform: `translateY(0px) translateX(0px)` },
            { transform: `translateY(${Math.random() * 6 - 3}px) translateX(${Math.random() * 6 - 3}px)` },
            { transform: `translateY(0px) translateX(0px)` }
        ], {
            duration: 3000 + Math.random() * 2000, // 3-5秒
            iterations: Infinity,
            direction: 'alternate',
            easing: 'ease-in-out',
            delay: Math.random() * 1000
        });
    });
});