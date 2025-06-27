/**
 * =================================================================================
 * profile.js - 个人空间页面交互功能 (最终修正和适配版)
 *
 * 功能说明:
 * - 统一管理个人主页的所有交互。
 * - 修正了 showToast 函数，使其能够正确操作 profile.html 中已有的 #customToast 元素。
 * - 整合了文章删除、用户关注、封面图更换等功能，并确保所有提示都使用自定义的 Toast。
 * - 移除了所有冲突代码和重复的事件监听器，保证代码干净、高效。
 * =================================================================================
 */
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
    // 为页面上的关注按钮（如果有）绑定事件
    const followButton = document.querySelector('.profile-action-buttons .btn-warning, .profile-action-buttons .btn-secondary');
    if (followButton) {
        followButton.addEventListener('click', function() {
            toggleFollow(this);
        });
    }

    // 为Toast的关闭按钮绑定事件
    const toastCloseButton = document.querySelector('#customToast button');
    if (toastCloseButton) {
        toastCloseButton.addEventListener('click', hideCustomToast);
    }

    // 初始化封面图片上传功能
    initCoverImageUpload();
}


// =================================================================
// 核心交互功能
// =================================================================

/**
 * 删除文章的函数
 * @param {string} articleId - 要删除的文章ID
 */
function deleteArticle(articleId) {
    Swal.fire({
        title: '您确定要删除这篇文章吗？',
        text: "此操作不可撤销，文章将被永久删除！",
        icon: 'warning',
        showCancelButton: true,
        confirmButtonColor: '#d33',
        cancelButtonColor: '#3085d6',
        confirmButtonText: '是的，删除它！',
        cancelButtonText: '取消'
    }).then((result) => {
        // 当用户点击 "是的，删除它！" 按钮后
        if (result.isConfirmed) {
            // --- 这里是原来 confirm 成功后的所有逻辑 ---
            const token = document.querySelector("meta[name='_csrf']")?.getAttribute("content");
            const header = document.querySelector("meta[name='_csrf_header']")?.getAttribute("content");

            if (!token || !header) {
                showToast('错误', '安全验证失败，请刷新页面后重试。', 'error');
                return;
            }

            fetch(`/api/article/${articleId}`, {
                method: 'DELETE',
                headers: {
                    [header]: token
                }
            })
                .then(response => {
                    if (response.ok) {
                        const articleElement = document.querySelector(`.article-item[data-article-id='${articleId}']`);
                        if(articleElement) articleElement.remove();
                        // 删除成功后，可以用 SweetAlert2 的成功提示，也可以用您自己的 Toast
                        showToast('成功', '文章已成功删除。', 'success');
                    } else {
                        return response.json().then(data => {
                            throw new Error(data.message || '删除失败，服务器返回未知错误。');
                        });
                    }
                })
                .catch(error => {
                    console.error('删除文章时出错:', error);
                    showToast('错误', error.message || '删除操作时发生网络错误。', 'error');
                });
            // --- 确认后的逻辑结束 ---
        }
    });
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
        showToast('错误', '无法获取用户信息', 'error');
        return;
    }

    const token = document.querySelector("meta[name='_csrf']")?.getAttribute("content");
    const header = document.querySelector("meta[name='_csrf_header']")?.getAttribute("content");

    if (!token || !header) {
        showToast('错误', '安全令牌丢失，请刷新页面！', 'error');
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
            showToast(data.isFollowing ? '成功' : '提示', data.message || (data.isFollowing ? '关注成功' : '已取消关注'), data.isFollowing ? 'success' : 'info');
        } else {
            throw new Error(data.message || '操作失败');
        }
    } catch (error) {
        console.error('关注操作失败:', error);
        showToast('错误', error.message, 'error');
        buttonElement.innerHTML = originalHtml;
    } finally {
        buttonElement.disabled = false;
    }
}


// =================================================================
// UI 更新与辅助函数
// =================================================================

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
            showToast('提示', '请选择有效的图片文件。', 'warning');
            return;
        }

        if (file.size > 5 * 1024 * 1024) { // 5MB
            showToast('提示', '封面图片大小不能超过5MB。', 'warning');
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
            showToast('成功', '封面更新成功！', 'success');
        } else {
            throw new Error(data.message || '上传失败');
        }
    } catch (error) {
        document.getElementById('coverImageDisplay').src = initialSrc;
        showToast('错误', error.message, 'error');
    } finally {
        document.getElementById('coverImageInput').value = '';
    }
}

/**
 * 显示一个全局的 Toast 通知 (适配 profile.html 的版本)
 * @param {string} title - 提示的标题
 * @param {string} message - 消息内容
 * @param {string} type - 'success', 'error', 'warning', 'info'
 */
let toastTimeout;
function showToast(title, message, type = 'info') {
    if (toastTimeout) {
        clearTimeout(toastTimeout);
    }

    // const toast = document.getElementById('customToast');
    // const toastTitle = document.getElementById('customToastTitle');
    // const toastMessage = document.getElementById('customToastMessage');
    // const toastIcon = document.getElementById('customToastIcon');
    const { toast, title: toastTitle, message: toastMessage, icon: toastIcon } = toastElements;

    const icons = {
        success: 'fa-check-circle text-success',
        error: 'fa-times-circle text-danger',
        warning: 'fa-exclamation-triangle text-warning',
        info: 'fa-info-circle text-info'
    };

    toastTitle.textContent = title;
    toastMessage.textContent = message;
    toastIcon.className = `fas ${icons[type] || icons['info']} mr-2`;

    toast.style.display = 'block';

    toastTimeout = setTimeout(hideCustomToast, 3000);
}

/**
 * 隐藏自定义Toast
 */
function hideCustomToast() {
    const toast = document.getElementById('customToast');
    if (toast) {
        toast.style.display = 'none';
    }
}