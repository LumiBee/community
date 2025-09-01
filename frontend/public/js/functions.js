import { showToast } from "./common-utils.js";

// 使用原生JavaScript替代jQuery
document.addEventListener('DOMContentLoaded', function() {
    // 滚动导航栏效果
    window.addEventListener('scroll', function() {
        const topnav = document.querySelector('.topnav');
        if (topnav) {
            if (window.scrollY > 50) {
                topnav.classList.add('scrollednav', 'py-0');
            } else {
                topnav.classList.remove('scrollednav', 'py-0');
            }
        }
    });
});

document.addEventListener('DOMContentLoaded', function () {
    const passwordSetupModalElement = document.getElementById('passwordSetupModal');

    if (passwordSetupModalElement) {
        let bsModalInstance = null;

        // --- Bootstrap 模态框初始化逻辑 (v5 or v4) ---
        if (typeof bootstrap !== 'undefined' && bootstrap.Modal && typeof bootstrap.Modal.getInstance === 'function') { // Bootstrap 5+
            bsModalInstance = bootstrap.Modal.getInstance(passwordSetupModalElement) || new bootstrap.Modal(passwordSetupModalElement);
            console.log("Password setup prompt modal instance (BS5) obtained/created.");
        } else if (typeof $ !== 'undefined' && typeof $(passwordSetupModalElement).modal === 'function') { // Bootstrap 4 (jQuery)
            console.log("Password setup prompt modal using jQuery (BS4).");
        } else {
            console.warn("Bootstrap Modal JS not found for passwordSetupModal.");
            return;
        }

        // --- 显示模态框 ---
        if (bsModalInstance) {
            bsModalInstance.show();
            console.log("Password setup prompt modal shown via BS5 JS.");
        } else if (typeof $ !== 'undefined' && typeof $(passwordSetupModalElement).modal === 'function') {
            $(passwordSetupModalElement).modal('show');
            console.log("Password setup prompt modal shown via jQuery/BS4.");
        }

        // --- 按钮的点击事件 ---
        const laterPasswordSetupBtn = document.getElementById('laterPasswordSetupBtn');
        if (laterPasswordSetupBtn) {
            laterPasswordSetupBtn.addEventListener('click', function () {
                console.log("'Later' button clicked for password setup prompt.");
                // --- 隐藏模态框 ---
                if (bsModalInstance) {
                    bsModalInstance.hide();
                } else if (typeof $ !== 'undefined' && typeof $(passwordSetupModalElement).modal === 'function') {
                    $(passwordSetupModalElement).modal('hide');
                }

                // --- 调用后端API，并根据结果显示Toast提示 ---
                fetch('/api/user/dismiss-password-prompt', {
                    method: 'POST',
                    headers: {
                        'Content-Type': 'application/json',
                    }
                })
                    .then(response => {
                        if (response.ok) {
                            console.log("Password setup prompt dismissed on server for this session.");
                            // 成功提示
                            showToast('操作成功，已在本会话中关闭此提示。', 'success');
                        } else {
                            response.text().then(text => {
                                console.error("Failed to dismiss password setup prompt on server:", response.status, text);
                                // 失败提示
                                showToast('操作失败，服务器返回错误。', 'error');
                            });
                        }
                    })
                    .catch(error => {
                        console.error("Error dismissing password setup prompt via API:", error);
                        // 网络或其它错误提示
                        showToast('网络请求失败，请稍后重试。', 'error');
                    });
            });
        }
    }
});