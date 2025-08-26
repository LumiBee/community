import {showConfirmModal, showToast} from "./common-utils.js";

/**
 * 设置全局的点击事件监听器
 */
document.addEventListener("DOMContentLoaded", () => {
    document.addEventListener('click', handleDelegatedClicks);
});

/**
 * 处理页面上的所有点击事件
 * @param {Event} event - 点击事件对象
 */
function handleDelegatedClicks(event) {
    // 绑定删除草稿按钮的点击事件
    const deleteButton = event.target.closest('.delete-draft-btn');
    if (!deleteButton) {
        return;
    }

    // 阻止链接的默认跳转行为
    event.preventDefault();

    // 从按钮的 data-* 属性中获取草稿ID
    const draftId = deleteButton.dataset.draftId;

    // 从按钮的 DOM 结构中找到对应的标题元素并获取其文本
    // 使用 optional chaining (?.) 避免在找不到元素时出错
    const articleItem = deleteButton.closest('.article-item');
    const title = articleItem?.querySelector('.article-title a')?.textContent || '该草稿';

    // 调用删除处理函数
    handleDeleteDraft(draftId, title, articleItem);
}

/**
 * 处理删除草稿的异步操作
 * @param {string} draftId - 要删除的草稿ID
 * @param {string} title - 草稿的标题
 * @param {HTMLElement} elementToRemove - 要从DOM中移除的HTML元素
 */
async function handleDeleteDraft(draftId, title, elementToRemove) {
    const confirmed = await showConfirmModal(
        '确认删除',
        `您确定要删除草稿 "<strong>${title}</strong>" 吗？<br>此操作不可撤销！`
    );
    if (!confirmed) return;

    const token = document.querySelector("meta[name='_csrf']")?.getAttribute("content");
    const header = document.querySelector("meta[name='_csrf_header']")?.getAttribute("content");

    if (!token || !header) {
        showToast('安全令牌丢失，无法删除。请刷新页面后重试。', 'error');
        return;
    }

    try {
        const response = await fetch(`/api/article/delete/${draftId}`, {
            method: 'DELETE',
            headers: {
                [header]: token
            }
        });

        if (response.ok) {
            showToast(`草稿 "${title}" 已成功删除。`, 'success');
        } else {
            console.error('删除失败，服务器响应:', response.status);
            showToast(`删除草稿 "${title}" 失败，请稍后重试。`, 'error');
        }
    } catch (error) {
        console.error('删除草稿时发生网络错误:', error);
        showToast('删除失败，请检查您的网络连接。', 'error');
    }
}