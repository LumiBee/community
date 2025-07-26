import { showToast } from "./common-utils.js";

document.addEventListener('DOMContentLoaded', function () {
    // --- 元素获取与变量初始化 ---
    const avatarPreviewDisplay = document.getElementById('avatarPreviewDisplay');
    const cropModalElement = document.getElementById('cropModal');
    const imageToCrop = document.getElementById('imageToCrop');
    const modalAlertPlaceholder = document.getElementById('modalAlertPlaceholder');

    let cropper;
    let cropModal;
    let originalAvatarSrc = avatarPreviewDisplay ? avatarPreviewDisplay.src : '';

    // --- Bootstrap 模态框初始化 ---
    if (cropModalElement) {
        if (typeof bootstrap !== 'undefined' && bootstrap.Modal) {
            cropModal = new bootstrap.Modal(cropModalElement); // Bootstrap 5
        } else if (typeof $ !== 'undefined' && $.fn.modal) {
            cropModal = $(cropModalElement); // Bootstrap 4 (jQuery)
        }
    }

    // --- 辅助函数定义 ---
    function hideCropModal() {
        if (!cropModalElement) return;
        // 统一的关闭逻辑
        if (typeof bootstrap !== 'undefined' && bootstrap.Modal.getInstance(cropModalElement)) {
            bootstrap.Modal.getInstance(cropModalElement).hide();
        } else if (typeof $ !== 'undefined' && $(cropModalElement).data('bs.modal')) {
            $(cropModalElement).modal('hide');
        }
    }

    const initializeCropper = () => {
        if (cropper) {
            cropper.destroy();
        }
        cropper = new Cropper(imageToCrop, {
            aspectRatio: 1,
            viewMode: 1,
            autoCropArea: 0.9,
            movable: true,
            zoomable: true,
            rotatable: true,
            scalable: false,
            crop: function () {
                if (!cropper) return;
                const previewCanvas = cropper.getCroppedCanvas({ width: 150, height: 150 });
                const avatarPreviewInModal = document.getElementById('avatarPreview');
                if (avatarPreviewInModal) {
                    avatarPreviewInModal.src = previewCanvas.toDataURL('image/png');
                }
            }
        });
    };

    const destroyCropper = () => {
        const avatarUploadInput = document.getElementById('avatarUploadInput');
        if (cropper) {
            cropper.destroy();
            cropper = null;
        }
        if (avatarUploadInput) {
            avatarUploadInput.value = '';
        }
        if (modalAlertPlaceholder) {
            modalAlertPlaceholder.innerHTML = '';
        }
    };

    // --- 非委托事件：为Bootstrap模态框插件绑定事件 ---
    if (cropModalElement) {
        $(cropModalElement).on('shown.bs.modal', initializeCropper);
        $(cropModalElement).on('hidden.bs.modal', destroyCropper);
    }

    // ===================================================================
    // == 主事件委托监听器
    // ===================================================================

    // 1. 委托 'change' 事件 (处理文件上传)
    document.addEventListener('change', function(event) {
        // 检查事件是否由头像上传输入框触发
        if (event.target.matches('#avatarUploadInput')) {
            const fileInput = event.target;
            const file = fileInput.files[0];

            if (!file) return;

            if (!file.type.startsWith('image/')) {
                showToast("请选择有效的图片文件 (如 JPG, PNG)。", "warning");
                fileInput.value = '';
                return;
            }

            if (file.size > 2 * 1024 * 1024) { // 限制为2MB
                showToast("图片文件大小不能超过2MB。", "warning");
                fileInput.value = '';
                return;
            }

            const reader = new FileReader();
            reader.onload = function(e) {
                imageToCrop.src = e.target.result;
                if (cropModal) {
                    // 显示模态框
                    if (typeof bootstrap !== 'undefined' && bootstrap.Modal) {
                        cropModal.show();
                    } else {
                        $(cropModalElement).modal('show');
                    }
                }
            };
            reader.readAsDataURL(file);
        }
    });

    // 2. 委托 'click' 事件 (处理按钮点击)
    document.addEventListener('click', function(event) {
        // 检查事件是否由“剪裁并上传”按钮触发
        if (event.target.matches('#cropAndUpload')) {
            const cropAndUploadBtn = event.target;
            if (!cropper) {
                showToast('Cropper 未初始化，请重试。', 'warning');
                return;
            }

            const originalButtonHtml = cropAndUploadBtn.innerHTML;
            cropAndUploadBtn.disabled = true;
            cropAndUploadBtn.innerHTML = `<span class="spinner-border spinner-border-sm" role="status" aria-hidden="true"></span> 正在上传...`;

            const canvas = cropper.getCroppedCanvas({
                width: 200,
                height: 200,
                imageSmoothingEnabled: true,
                imageSmoothingQuality: 'high',
            });

            if (avatarPreviewDisplay) {
                avatarPreviewDisplay.src = canvas.toDataURL('image/png');
            }

            canvas.toBlob((blob) => {
                if (!blob) {
                    showToast('图片处理失败，无法创建Blob对象。', 'error');
                    cropAndUploadBtn.disabled = false;
                    cropAndUploadBtn.innerHTML = originalButtonHtml;
                    if (avatarPreviewDisplay) avatarPreviewDisplay.src = originalAvatarSrc;
                    return;
                }

                const formData = new FormData();
                formData.append('avatarFile', blob, 'cropped-avatar.png');
                ['username', 'bio', 'email_profile'].forEach(id => {
                    const field = document.getElementById(id);
                    const name = (id === 'email_profile') ? 'email' : id;
                    if (field) formData.append(name, field.value);
                });

                const tokenMeta = document.querySelector("meta[name='_csrf']");
                const headerMeta = document.querySelector("meta[name='_csrf_header']");
                const headers = {};
                if (tokenMeta && headerMeta) {
                    headers[headerMeta.getAttribute("content")] = tokenMeta.getAttribute("content");
                }

                fetch('/user/settings/profile', {
                    method: 'POST',
                    headers: headers,
                    body: formData
                })
                    .then(response => {
                        if (response.ok) {
                            window.location.reload();
                            showToast('头像及资料更新成功！', 'success');
                        } else {
                            return response.text().then(text => {
                                throw new Error(text || '上传失败，服务器返回错误。');
                            });
                        }
                    })
                    .catch(err => {
                        showToast('上传出错：' + err.message, 'error');
                        if (avatarPreviewDisplay) {
                            avatarPreviewDisplay.src = originalAvatarSrc; // 上传失败，恢复原始头像
                        }
                    })
                    .finally(() => {
                        cropAndUploadBtn.disabled = false;
                        cropAndUploadBtn.innerHTML = originalButtonHtml;
                    });
            }, 'image/png');
        }
    });

    // 3. 委托 'submit' 事件 (处理所有表单提交)
    document.addEventListener('submit', function(event) {
        const form = event.target;

        // --- 处理个人资料设置表单 ---
        if (form.matches('#profileSettingsForm')) {
            event.preventDefault(); // 阻止页面刷新

            const submitButton = form.querySelector('button[type="submit"]');
            const originalButtonHtml = submitButton.innerHTML;
            submitButton.disabled = true;
            submitButton.innerHTML = `<span class="spinner-border spinner-border-sm"></span> 正在保存...`;

            const formData = new FormData(form);
            // 此表单提交不应包含文件，文件由裁剪流程处理
            formData.delete('avatarFile');

            const headers = {};
            const csrfTokenInput = form.querySelector("input[name*='_csrf']");
            const csrfHeaderMeta = document.querySelector("meta[name='_csrf_header']");
            if (csrfTokenInput && csrfHeaderMeta) {
                headers[csrfHeaderMeta.getAttribute("content")] = csrfTokenInput.value;
            }

            fetch(form.action, {
                method: 'POST',
                headers: headers,
                body: formData // 发送 multipart/form-data，但不包含文件
            })
                .then(response => response.text().then(text => ({ ok: response.ok, text })))
                .then(result => {
                    if (result.ok) {
                        showToast(result.text || '基本资料更新成功！', 'success');
                    } else {
                        throw new Error(result.text || '资料更新失败，服务器返回错误。');
                    }
                })
                .catch(error => {
                    showToast('操作失败：' + error.message, 'error');
                })
                .finally(() => {
                    submitButton.disabled = false;
                    submitButton.innerHTML = originalButtonHtml;
                });
        }

        // --- 处理密码修改表单 ---
        if (form.matches('#accountSettingsForm')) {
            event.preventDefault(); // 阻止表单默认提交

            const newPasswordInput = form.querySelector('#newPassword');
            const confirmNewPasswordInput = form.querySelector('#confirmNewPassword');
            const submitButton = form.querySelector('button[type="submit"]');
            const originalButtonHtml = submitButton.innerHTML;

            if (!newPasswordInput.value || !confirmNewPasswordInput.value) {
                showToast('新密码和确认密码均不能为空。', 'warning');
                return;
            }
            if (newPasswordInput.value !== confirmNewPasswordInput.value) {
                showToast('两次输入的密码不匹配，请重新输入。', 'warning');
                return;
            }

            submitButton.disabled = true;
            submitButton.innerHTML = `<span class="spinner-border spinner-border-sm" role="status" aria-hidden="true"></span> 正在更新...`;

            const formData = new FormData(form);
            const headers = {};
            const csrfTokenInput = form.querySelector("input[name*='_csrf']");
            const csrfHeaderMeta = document.querySelector("meta[name='_csrf_header']");
            if (csrfTokenInput && csrfHeaderMeta) {
                headers[csrfHeaderMeta.getAttribute("content")] = csrfTokenInput.value;
            }

            fetch(form.action, {
                method: 'POST',
                headers: headers,
                body: new URLSearchParams(formData)
            })
                .then(response => {
                    return response.text().then(text => ({
                        ok: response.ok,
                        text: text
                    }));
                })
                .then(result => {
                    if (result.ok) {
                        showToast( '密码更新成功！', 'success');
                        newPasswordInput.value = '';
                        confirmNewPasswordInput.value = '';
                    } else {
                        throw new Error('密码更新失败，服务器返回错误。');
                    }
                })
                .catch(error => {
                    showToast('密码更新失败：' + error.message, 'error');
                })
                .finally(() => {
                    submitButton.disabled = false;
                    submitButton.innerHTML = originalButtonHtml;
                });
        }
    });
});