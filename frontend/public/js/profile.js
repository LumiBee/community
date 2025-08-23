import { showToast, showConfirmModal} from "./common-utils.js";

const toastElements = {};
// --- 页面加载主入口 ---
document.addEventListener('DOMContentLoaded', function() {
    console.log("LumiHive :: profile.js (v4 - Final Fix) :: DOM fully loaded. Initializing all features.");

    toastElements.toast = document.getElementById('customToast');
    toastElements.title = document.getElementById('customToastTitle');
    toastElements.message = document.getElementById('customToastMessage');
    toastElements.icon = document.getElementById('customToastIcon');

    // 初始化所有事件监听器和功能
    initializeProfilePage();
});

/**
 * 统一的初始化函数
 */
function initializeProfilePage() {
    // 为所有文章删除按钮绑定事件
    const deleteButtons = document.querySelectorAll('.btn-delete-article');
    deleteButtons.forEach(button => {
        button.addEventListener('click', function(event) {
            event.stopPropagation(); // 阻止事件冒泡
            const articleId = this.getAttribute('data-article-id');
            deleteArticle(articleId);
        });
    });

    // 为页面上的关注按钮绑定事件
    const followButton = document.querySelector('.js-toggle-follow');
    if (followButton) {
        followButton.addEventListener('click', function() {
            toggleFollow(this);
        });
    }


    // 初始化封面图片上传功能
    initCoverImageUpload();
}

/**
 * 删除文章的函数
 * @param {string} articleId - 要删除的文章ID
 */
async function deleteArticle(articleId) {
    event.stopPropagation(); // 阻止事件冒泡
    const confirmd = await showConfirmModal(
        '确认删除',
        '您确定要删除这篇文章吗？<br>此操作无法撤销!',
    );
    if (!confirmd) return;

    const token = document.querySelector("meta[name='_csrf']")?.getAttribute("content");
    const header = document.querySelector("meta[name='_csrf_header']")?.getAttribute("content");

    if (!token || !header) {
        alert('安全令牌丢失，无法删除。请刷新页面后重试。');
        return;
    }

    try {
        const response = await fetch(`/api/article/delete/${articleId}`, {
            method: 'DELETE',
            headers: {
                [header]: token
            }
        });

        if (response.ok) {
            showToast(`文章已成功删除，请刷新页面刷新目录`, 'success');
        } else {
            console.error('删除失败，服务器响应:', response.status);
            showToast(`删除文章失败，请稍后重试。`, 'error');
        }
    } catch (error) {
        console.error('删除文章时发生网络错误:', error);
        showToast('删除失败，请检查您的网络连接。', 'error');
    }
}

/**
 * 处理关注/取关按钮的点击事件
 * @param {HTMLElement} buttonElement - 被点击的按钮元素
 */
async function toggleFollow(buttonElement) {
    if (!buttonElement) return;

    const profileHeader = document.querySelector('.profile-header');
    const targetUserId = profileHeader ? profileHeader.getAttribute('data-user-id') : null;

    if (!targetUserId) {
        showToast( '无法获取用户信息', 'error');
        return;
    }

    const token = document.querySelector("meta[name='_csrf']")?.getAttribute("content");
    const header = document.querySelector("meta[name='_csrf_header']")?.getAttribute("content");

    if (!token || !header) {
        showToast('安全令牌丢失，请刷新页面！', 'error');
        return;
    }

    buttonElement.disabled = true;
    const originalHtml = buttonElement.innerHTML;
    buttonElement.innerHTML = `<i class="fas fa-spinner fa-spin"></i> 处理中...`;

    try {
        const response = await fetch(`/api/user/${targetUserId}/follow`, {
            method: 'POST',
            headers: { [header]: token }
        });

        if (!response.ok) {
            throw new Error(`服务器响应失败: ${response.status}`);
        }

        const data = await response.json();

        if (data.success) {
            updateFollowButtonUI(buttonElement, data.isFollowing);
            updateFollowerCount(data.isFollowing ? 1 : -1);
            showToast(data.message || (data.isFollowing ? '关注成功' : '已取消关注'), 'success');
        } else {
            throw new Error(data.message || '操作失败');
        }
    } catch (error) {
        console.error('关注操作失败:', error);
        showToast(error.message, 'error');
        buttonElement.innerHTML = originalHtml;
    } finally {
        buttonElement.disabled = false;
    }
}

/**
 * 更新关注按钮的UI状态
 * @param {HTMLElement} button - 要更新的按钮
 * @param {boolean} isFollowing - 是否已关注
 */
function updateFollowButtonUI(button, isFollowing) {
    if (isFollowing) {
        button.classList.replace('btn-warning', 'btn-secondary');
        button.innerHTML = '<i class="fas fa-user-check"></i> 已关注';
    } else {
        button.classList.replace('btn-secondary', 'btn-warning');
        button.innerHTML = '<i class="fas fa-user-plus"></i> 关注';
    }
}

/**
 * 更新粉丝数量显示
 * @param {number} change - 变化量 (1 或 -1)
 */
function updateFollowerCount(change) {
    const followerCountElement = document.querySelector('.stat-item:nth-child(2) .stat-value');
    if (followerCountElement) {
        const currentCount = parseInt(followerCountElement.textContent, 10);
        if (!isNaN(currentCount)) {
            followerCountElement.textContent = currentCount + change;
        }
    }
}

/**
 * 初始化封面图片上传功能
 */
function initCoverImageUpload() {
    const changeCoverBtn = document.querySelector('.change-cover-btn');
    const coverImageInput = document.getElementById('coverImageInput');
    const coverImageDisplay = document.getElementById('coverImageDisplay');

    if (!changeCoverBtn || !coverImageInput || !coverImageDisplay) return;

    const initialCoverSrc = coverImageDisplay.src;

    changeCoverBtn.addEventListener('click', () => coverImageInput.click());

    coverImageInput.addEventListener('change', function(event) {
        const file = event.target.files[0];
        if (!file) return;

        if (!file.type.startsWith('image/')) {
            showToast( '请选择有效的图片文件。', 'warning');
            return;
        }

        if (file.size > 5 * 1024 * 1024) { // 5MB
            showToast( '封面图片大小不能超过5MB。', 'warning');
            return;
        }

        const reader = new FileReader();
        reader.onload = e => { coverImageDisplay.src = e.target.result; };
        reader.readAsDataURL(file);

        uploadCoverImage(file, initialCoverSrc);
    });
}

/**
 * AJAX 上传封面图片
 * @param {File} file - 文件对象
 * @param {string} initialSrc - 原始图片URL，用于失败时恢复
 */
async function uploadCoverImage(file, initialSrc) {
    const formData = new FormData();
    formData.append('coverImageFile', file);

    const token = document.querySelector("meta[name='_csrf']")?.getAttribute("content");
    const header = document.querySelector("meta[name='_csrf_header']")?.getAttribute("content");

    try {
        const response = await fetch('/profile/update-cover', {
            method: 'POST',
            headers: { [header]: token },
            body: formData
        });

        const data = await response.json();

        if (response.ok && data.success) {
            document.getElementById('coverImageDisplay').src = data.newImageUrl;
            showToast( '封面更新成功！', 'success');
        } else {
            throw new Error(data.message || '上传失败');
        }
    } catch (error) {
        document.getElementById('coverImageDisplay').src = initialSrc;
        showToast( error.message, 'error');
    } finally {
        document.getElementById('coverImageInput').value = '';
    }
}
