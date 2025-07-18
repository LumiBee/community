document.addEventListener('DOMContentLoaded', function () {
    // --- DOM 元素获取 ---
    const avatarUploadInput = document.getElementById('avatarUploadInput');
    const avatarPreviewDisplay = document.getElementById('avatarPreviewDisplay');
    const cropModalElement = document.getElementById('cropModal');
    const imageToCrop = document.getElementById('imageToCrop');
    const cropAndUploadBtn = document.getElementById('cropAndUpload');

    // --- 变量初始化 ---
    let cropper;
    let cropModal;

    if (!cropModalElement || !avatarUploadInput || !imageToCrop || !cropAndUploadBtn) {
        console.error("初始化失败：缺少必要的页面元素（如模态框、输入框或按钮）。");
        return; // 如果关键元素不存在，则终止脚本
    }

    cropModal = new bootstrap.Modal(cropModalElement);

    // --- 事件监听 ---

    // 1. 监听文件选择框的 change 事件
    avatarUploadInput.addEventListener('change', function(event) {
        const file = event.target.files[0];

        // 文件校验
        if (!file || !file.type.startsWith('image/')) {
            if (file) alert("请选择有效的图片文件 (如 JPG, PNG)。");
            this.value = ''; // 清空选择，以便能再次选择相同文件
            return;
        }

        if (file.size > 2 * 1024 * 1024) { // 限制为2MB，与HTML提示一致
            alert("图片文件大小不能超过2MB。");
            this.value = '';
            return;
        }

        // 使用 FileReader 读取文件并在模态框中显示
        const reader = new FileReader();
        reader.onload = function(e) {
            imageToCrop.src = e.target.result;
            cropModal.show(); // 显示模态框
        };
        reader.readAsDataURL(file);
    });

    // 2. 监听模态框的事件，在显示时初始化 Cropper，在隐藏时销毁
    cropModalElement.addEventListener('shown.bs.modal', function () {
        if (imageToCrop.src) {
            cropper = new Cropper(imageToCrop, {
                aspectRatio: 1 / 1,       // 保持1:1的宽高比
                viewMode: 1,              // 限制裁剪框不能超出画布
                autoCropArea: 0.9,        // 自动裁剪区域占90%
                movable: true,
                zoomable: true,
                rotatable: true,
                scalable: false,          // 禁用缩放图片（非裁剪框）
            });
        }
    });

    cropModalElement.addEventListener('hidden.bs.modal', function () {
        if (cropper) {
            cropper.destroy(); // 销毁实例，释放资源
            cropper = null;
            avatarUploadInput.value = ''; // 清空文件选择
        }
    });

    // 3. "剪裁并上传" 按钮的点击事件
    cropAndUploadBtn.addEventListener('click', function() {
        if (!cropper) {
            return;
        }

        // --- 按钮状态管理 ---
        const originalButtonHtml = this.innerHTML;
        this.disabled = true;
        this.innerHTML = `<span class="spinner-border spinner-border-sm" role="status" aria-hidden="true"></span> 正在上传...`;

        // --- 获取剪裁后的高质量 Canvas ---
        const canvas = cropper.getCroppedCanvas({
            width: 200,  // 预览图的宽度
            height: 200, // 预览图的高度
            imageSmoothingEnabled: true,
            imageSmoothingQuality: 'high',
        });

        const avatarPreview = document.getElementById('avatarPreview');
        avatarPreview.src = canvas.toDataURL('image/png');

        // --- 将 Canvas 转换为 Blob 对象并上传 ---
        canvas.toBlob((blob) => {
            // 创建 FormData 来包装要上传的数据
            const formData = new FormData();
            // 将 blob 添加到表单中，'avatarFile' 必须与后端 @RequestParam 的名称一致
            formData.append('avatarFile', blob, 'cropped-avatar.png');

            formData.append('username', document.getElementById('username').value);
            formData.append('bio', document.getElementById('bio').value);
            formData.append('email', document.getElementById('email_profile').value);

            // --- 获取 CSRF Token ---
            const token = document.querySelector("meta[name='_csrf']").getAttribute("content");
            const header = document.querySelector("meta[name='_csrf_header']").getAttribute("content");

            // --- 发送 Fetch 请求 ---
            fetch('/user/settings/profile', {
                method: 'POST',
                headers: {
                    // 注意：当使用 FormData 和 fetch 时，浏览器会自动设置 'Content-Type': 'multipart/form-data'
                    // 所以我们不需要手动设置它，但必须设置 CSRF
                    [header]: token
                },
                body: formData
            })
                .then(response => {
                    if (response.ok) {
                        // 如果请求成功，更新前端显示的头像
                        avatarPreviewDisplay.src = URL.createObjectURL(blob);
                        cropModal.hide(); // 关闭模态框
                        // 可以在这里显示一个成功的提示信息
                        alert('头像更新成功！');
                        // 如果后端重定向，浏览器不会自动跳转，需要JS处理
                        // 如果希望上传后刷新页面，可以取消下面这行注释
                        // window.location.reload();
                    } else {
                        // 服务器返回错误
                        response.text().then(text => {
                            alert('上传失败：' + text);
                        });
                    }
                })
                .catch(err => {
                    console.error('上传过程中发生网络错误:', err);
                    alert('上传出错，请检查您的网络连接。');
                })
                .finally(() => {
                    // 恢复按钮状态
                    this.disabled = false;
                    this.innerHTML = originalButtonHtml;
                });

        }, 'image/png'); // 指定 blob 的 MIME 类型
    });

    // 在模态框显示时初始化 Cropper
    cropModalElement.addEventListener('shown.bs.modal', function () {
        if (imageToCrop.src) {
            cropper = new Cropper(imageToCrop, {
                aspectRatio: 1 / 1,
                viewMode: 1,
                autoCropArea: 0.9,
                movable: true,
                zoomable: true,
                rotatable: true,
                scalable: false,
                crop: function (event) {
                    // 可以在裁剪过程中实时更新预览 (可选)
                    if (cropper) {
                        const previewCanvas = cropper.getCroppedCanvas({
                            width: 100,
                            height: 100
                        });
                        document.getElementById('avatarPreview').src = previewCanvas.toDataURL('image/png');
                    }
                }
            });
        }
    });
});