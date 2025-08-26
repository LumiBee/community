import { showToast } from "./common-utils.js";

document.addEventListener('DOMContentLoaded', function () {

    // 注册成功后的 Toast 提示
    const urlParams = new URLSearchParams(window.location.search);

    // 检查是否存在 'registered' 参数且其值为 'true'
    if (urlParams.get('registered') === 'true') {
        // 如果存在，则调用 showToast 函数显示成功提示
        showToast('注册成功！现在您可以登录了。', 'success');

        // 从 URL 中移除该参数，防止用户刷新页面时再次弹出提示
        const newUrl = window.location.protocol + "//" + window.location.host + window.location.pathname;
        window.history.replaceState({path: newUrl}, '', newUrl);
    }

    // 密码可见性切换
    const togglePassword = document.getElementById('togglePassword');
    const password = document.getElementById('password');

    if(togglePassword && password) {
        togglePassword.addEventListener('click', function() {
            // 判断当前密码框的类型
            const isPassword = password.getAttribute('type') === 'password';
            const icon = this.querySelector('i');

            if (isPassword) {
                // 如果是密码，就变成文本
                password.setAttribute('type', 'text');
                // 图标变成“闭眼”
                icon.classList.remove('fa-eye');
                icon.classList.add('fa-eye-slash');
            } else {
                // 如果是文本，就变回密码
                password.setAttribute('type', 'password');
                // 图标变回“睁眼”
                icon.classList.remove('fa-eye-slash');
                icon.classList.add('fa-eye');
            }
        });
    }
});