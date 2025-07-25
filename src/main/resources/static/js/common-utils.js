// --- Toast 提示框功能 ---
const toastStyles = `
  .toast-container-custom {
    position: fixed;
    top: 80px;
    right: 20px;
    z-index: 1090;
    display: flex;
    flex-direction: column;
    align-items: flex-end;
    gap: 10px;
  }
  .toast-custom {
    min-width: 300px;
    background-color: #fff;
    color: #333;
    border-radius: 12px;
    box-shadow: 0 5px 15px rgba(0, 0, 0, 0.15);
    display: flex;
    align-items: center;
    padding: 12px 20px;
    border-left: 5px solid;
    opacity: 0;
    transform: translateX(100%);
    transition: all 0.4s cubic-bezier(0.25, 0.8, 0.25, 1);
  }
  .toast-custom.show {
    opacity: 1;
    transform: translateX(0);
  }
  .toast-custom-icon {
    font-size: 1.5rem;
    margin-right: 15px;
  }
  .toast-custom-body {
    flex-grow: 1;
  }
  .toast-custom-title {
    font-weight: 700;
    margin-bottom: 3px;
    font-family: 'Playfair Display', Georgia, serif;
  }
  .toast-custom-message {
    font-size: 0.95rem;
    color: #555;
  }
  /* 根据类型设置颜色 */
  .toast-custom.success { border-color: #ffda58; }
  .toast-custom.success .toast-custom-icon { color: #ffda58; }
  .toast-custom.error { border-color: #dc3545; }
  .toast-custom.error .toast-custom-icon { color: #dc3545; }
  .toast-custom.warning { border-color: #ffc107; }
  .toast-custom.warning .toast-custom-icon { color: #ffc107; }
`;
// 将样式注入到页面中
const styleSheet = document.createElement("style");
styleSheet.innerText = toastStyles;
document.head.appendChild(styleSheet);

/**
 * 显示一个会自动消失的 Toast 提示框
 * @param {string} message - 要显示的消息
 * @param {('success'|'error'｜'warning')} type - 提示类型 ('success'/'error'/'warning')
 * @param {number} duration - 显示时长（毫秒），默认为 3000ms
 */
export function showToast(message, type = 'success', duration = 3000) {
    // 检查容器是否存在，不存在则创建
    let container = document.querySelector('.toast-container-custom');
    if (!container) {
        container = document.createElement('div');
        container.className = 'toast-container-custom';
        document.body.appendChild(container);
    }

    // 创建 toast 元素
    const toast = document.createElement('div');
    toast.className = `toast-custom ${type}`;

    let iconClass, title;
    switch (type) {
        case 'success':
            iconClass = 'fas fa-check-circle';
            title = '操作成功';
            break;
        case 'error':
            iconClass = 'fas fa-times-circle';
            title = '发生错误';
            break;
        case 'warning':
            iconClass = 'fas fa-exclamation-triangle';
            title = '请注意';
            break;
        default:
            iconClass = 'fas fa-info-circle';
            title = '提示';
    }

    toast.innerHTML = `
        <div class="toast-custom-icon">
            <i class="${iconClass}"></i>
        </div>
        <div class="toast-custom-body">
            <div class="toast-custom-title">${title}</div>
            <div class="toast-custom-message">${message}</div>
        </div>
    `;

    // 添加到容器
    container.appendChild(toast);

    // 触发显示动画
    setTimeout(() => {
        toast.classList.add('show');
    }, 10); // 短暂延迟确保动画触发

    // 设置定时器，在 duration 后移除 toast
    setTimeout(() => {
        toast.classList.remove('show');
        // 在动画结束后从 DOM 中移除元素
        toast.addEventListener('transitionend', () => toast.remove());
    }, duration);
}

// --- 自定义确认对话框 (Modal) 功能 ---
const confirmModalStyles = `
  .confirm-modal-backdrop {
    position: fixed;
    top: 0;
    left: 0;
    width: 100vw;
    height: 100vh;
    background-color: rgba(0, 0, 0, 0.5);
    z-index: 1060;
    opacity: 0;
    transition: opacity 0.2s ease-in-out;
  }
  .confirm-modal-backdrop.show {
    opacity: 1;
  }
  .confirm-modal-dialog {
    position: fixed;
    top: 40%;
    left: 50%;
    transform: translate(-50%, -50%) scale(0.9);
    z-index: 1070;
    background-color: #fff;
    border-radius: 16px;
    box-shadow: 0 10px 30px rgba(0, 0, 0, 0.2);
    width: 90%;
    max-width: 450px;
    opacity: 0;
    transition: all 0.3s ease-in-out;
    overflow: hidden;
  }
  .confirm-modal-dialog.show {
    opacity: 1;
    top: 50%;
    transform: translate(-50%, -50%) scale(1);
  }
  .confirm-modal-header {
    padding: 20px 24px;
    border-bottom: 1px solid #f0f0f0;
    display: flex;
    align-items: center;
    gap: 12px;
  }
  .confirm-modal-header .icon {
    font-size: 1.5rem;
    color: #ffc107;
  }
  .confirm-modal-title {
    font-size: 1.25rem;
    font-weight: 600;
    font-family: 'Playfair Display', Georgia, serif;
    margin: 0;
  }
  .confirm-modal-body {
    padding: 24px;
    font-size: 1rem;
    line-height: 1.6;
    color: #555;
  }
  .confirm-modal-footer {
    padding: 16px 24px;
    background-color: #f8f9fa;
    display: flex;
    justify-content: flex-end;
    gap: 12px;
  }
  .confirm-modal-btn {
    padding: 10px 20px;
    border: none;
    border-radius: 8px;
    font-weight: 600;
    cursor: pointer;
    transition: all 0.2s ease;
  }
  .confirm-modal-btn.btn-cancel {
    background-color: #e9ecef;
    color: #495057;
  }
  .confirm-modal-btn.btn-cancel:hover {
    background-color: #dee2e6;
  }
  .confirm-modal-btn.btn-confirm-delete {
    background-color: #dc3545;
    color: white;
  }
  .confirm-modal-btn.btn-confirm-delete:hover {
    background-color: #c82333;
    box-shadow: 0 4px 10px rgba(220, 53, 69, 0.3);
  }
`;

const confirmModalStyleSheet = document.createElement("style");
confirmModalStyleSheet.innerText = confirmModalStyles;
document.head.appendChild(confirmModalStyleSheet);

/**
 * 显示一个自定义的确认对话框，并返回一个 Promise
 * @param {string} title - 对话框标题
 * @param {string} message - 对话框内容
 * @param {string} confirmText - 确认按钮文字 (可选)
 * @param {string} cancelText - 取消按钮文字 (可选)
 * @returns {Promise<boolean>} - 当用户点击确认时 resolve(true)，否则 resolve(false)
 */
export function showConfirmModal(title, message, confirmText = '确认删除', cancelText = '取消') {
    return new Promise(resolve => {
        // 创建遮罩
        const backdrop = document.createElement('div');
        backdrop.className = 'confirm-modal-backdrop';

        // 创建对话框
        const modal = document.createElement('div');
        modal.className = 'confirm-modal-dialog';
        modal.innerHTML = `
            <div class="confirm-modal-header">
                <i class="icon fas fa-exclamation-triangle"></i>
                <h5 class="confirm-modal-title">${title}</h5>
            </div>
            <div class="confirm-modal-body">
                ${message}
            </div>
            <div class="confirm-modal-footer">
                <button class="confirm-modal-btn btn-cancel">${cancelText}</button>
                <button class="confirm-modal-btn btn-confirm-delete">${confirmText}</button>
            </div>
        `;

        document.body.appendChild(backdrop);
        document.body.appendChild(modal);

        // 触发显示动画
        setTimeout(() => {
            backdrop.classList.add('show');
            modal.classList.add('show');
        }, 10);

        const btnCancel = modal.querySelector('.btn-cancel');
        const btnConfirm = modal.querySelector('.btn-confirm-delete');

        const closeModal = (result) => {
            modal.classList.remove('show');
            backdrop.classList.remove('show');
            modal.addEventListener('transitionend', () => {
                document.body.removeChild(modal);
                document.body.removeChild(backdrop);
                resolve(result);
            }, { once: true });
        };

        btnConfirm.onclick = () => closeModal(true);
        btnCancel.onclick = () => closeModal(false);
        backdrop.onclick = () => closeModal(false);
    });
}
