document.addEventListener('DOMContentLoaded', function () {
    const avatarUploadInput = document.getElementById('avatarUploadInput');
    const avatarPreviewDisplay = document.getElementById('avatarPreviewDisplay');
    const defaultAvatarSrc = '/img/default01.jpg'; // 默认头像路径
    const cropModalElement = document.getElementById('cropModal');
    const imageToCrop = document.getElementById('imageToCrop');
    const cropAndUploadBtn = document.getElementById('cropAndUpload');
    let cropper;
    let cropModal;

    // 仅当模态框元素存在时才初始化
    if (cropModalElement) {
        cropModal = new bootstrap.Modal(cropModalElement);
    } else {
        console.error("错误：未在HTML中找到ID为 'cropModal' 的模态框元素。图片剪裁功能将无法使用。");
    }

    // 激活URL参数中指定的标签页
    const urlParams = new URLSearchParams(window.location.search);
    const tabToActivate = urlParams.get('tab');
    if (tabToActivate) {
        const tabElement = document.querySelector(`#settingsTabs a[href="#${tabToActivate}"]`);
        if (tabElement) {
            new bootstrap.Tab(tabElement).show();
        }
    }

    // 头像上传处理
    if (avatarUploadInput && avatarPreviewDisplay) {
        let initialAvatarSrc = avatarPreviewDisplay.src;
        // 如果头像SRC无效，则设置为默认头像
        if (!initialAvatarSrc || initialAvatarSrc.endsWith(window.location.pathname)) {
            initialAvatarSrc = defaultAvatarSrc;
            avatarPreviewDisplay.src = defaultAvatarSrc;
        }
        avatarPreviewDisplay.dataset.defaultSrc = initialAvatarSrc;

        // 点击头像图片也可以触发文件选择
        avatarPreviewDisplay.addEventListener('click', function() {
            avatarUploadInput.click();
        });

        // 当用户选择了新文件时
        avatarUploadInput.addEventListener('change', function(event) {
            const file = event.target.files[0];
            if (!file || !file.type.startsWith('image/')) {
                if(file) alert("请选择有效的图片文件 (JPG, PNG等)。");
                this.value = ''; // 清空选择
                return;
            }

            if (file.size > 5 * 1024 * 1024) { // 限制为5MB
                alert("图片文件大小不能超过5MB");
                this.value = ''; // 清空选择
                return;
            }

            // 读取文件并在模态框中显示以进行剪裁
            const reader = new FileReader();
            reader.onload = function(e) {
                if (imageToCrop) {
                    imageToCrop.src = e.target.result;
                    if (cropModal) {
                        cropModal.show();
                    }
                } else {
                    console.error("错误：未在HTML中找到ID为 'imageToCrop' 的img元素。");
                }
            };
            reader.readAsDataURL(file);
            this.value = ''; // 清空文件输入，以便下次选择相同文件也能触发change事件
        });
    }

    // 密码校验逻辑
    const accountForm = document.getElementById('accountSettingsForm');
    if (accountForm) {
        accountForm.addEventListener('submit', function(event) {
            const newPassword = document.getElementById('newPassword').value;
            const confirmNewPassword = document.getElementById('confirmNewPassword').value;

            if (newPassword && newPassword.length < 6) {
                alert('新密码长度至少为6个字符。');
                event.preventDefault();
                return false;
            }

            if (newPassword !== confirmNewPassword) {
                alert('两次输入的密码不一致。');
                event.preventDefault();
                return false;
            }
            return true;
        });
    }

    // --- Cropper.js 相关的事件监听 ---

    if (cropModalElement) {
        // 当模态框完全显示后，初始化 Cropper
        cropModalElement.addEventListener('shown.bs.modal', function () {
            if (imageToCrop) {
                cropper = new Cropper(imageToCrop, {
                    aspectRatio: 1 / 1,
                    viewMode: 1,
                    autoCropArea: 0.9,
                    movable: true,
                    zoomable: true,
                    scalable: true,
                    rotatable: true,
                });
            }
        });

        // 当模态框隐藏时，销毁 Cropper 实例以释放资源
        cropModalElement.addEventListener('hidden.bs.modal', function () {
            if (cropper) {
                cropper.destroy();
                cropper = null;
            }
        });
    }

    // “剪裁并上传”按钮的点击事件
    if (cropAndUploadBtn) {
        cropAndUploadBtn.addEventListener('click', function() {
            if (!cropper) {
                return;
            }

            const originalButtonHtml = this.innerHTML;
            this.disabled = true;
            this.innerHTML = `<span class="spinner-border spinner-border-sm" role="status" aria-hidden="true"></span> 处理中...`;

            // 获取剪裁后的高质量Canvas
            const canvas = cropper.getCroppedCanvas({
                width: 400,
                height: 400,
                imageSmoothingEnabled: true,
                imageSmoothingQuality: 'high',
            });

            // 将Canvas转换为Blob对象并上传
            canvas.toBlob(function (blob) {
                const formData = new FormData();
                formData.append('avatarFile', blob, 'avatar_cropped.png'); // 上传的文件

                // 从表单中获取其他需要一同提交的数据
                formData.append('username', document.getElementById('username').value);
                formData.append('bio', document.getElementById('bio').value);
                formData.append('email', document.getElementById('email_profile').value);

                // CSRF Token
                const token = document.querySelector("meta[name='_csrf']")?.getAttribute("content");
                const header = document.querySelector("meta[name='_csrf_header']")?.getAttribute("content");
                if (!token || !header) {
                    alert('安全验证失败，请刷新页面。');
                    cropAndUploadBtn.disabled = false;
                    cropAndUploadBtn.innerHTML = originalButtonHtml;
                    return;
                }

                // 发送Fetch请求
                fetch('/user/settings/profile', {
                    method: 'POST',
                    headers: {
                        [header]: token
                    },
                    body: formData
                })
                    .then(response => {
                        if (response.ok) {
                            return response.text(); // 或者 response.json() 如果后端返回JSON
                        }
                        throw new Error('上传失败，服务器响应错误。');
                    })
                    .then(data => {
                        // 更新页面上的头像预览
                        avatarPreviewDisplay.src = URL.createObjectURL(blob);
                        if (cropModal) cropModal.hide();
                        alert('头像更新成功！');
                        // 可根据后端返回信息刷新页面或部分内容
                        // window.location.reload();
                    })
                    .catch(err => {
                        console.error('上传出错:', err);
                        alert('上传出错，请检查网络或联系管理员。');
                    })
                    .finally(() => {
                        // 恢复按钮状态
                        cropAndUploadBtn.disabled = false;
                        cropAndUploadBtn.innerHTML = originalButtonHtml;
                    });

            }, 'image/png');
        });
    }
});