document.addEventListener('DOMContentLoaded', function () {
    const signupForm = document.getElementById('signupForm');
    const usernameInput = document.getElementById('username');
    const emailInput = document.getElementById('email');
    const passwordInput = document.getElementById('password');
    const confirmPasswordInput = document.getElementById('confirmPassword');
    const togglePasswordButton = document.getElementById('togglePassword');
    const toggleConfirmPasswordButton = document.getElementById('toggleConfirmPassword');

    if (signupForm) {
        signupForm.setAttribute('novalidate', '');
    }

    function getFeedbackElement(inputElement) {
        // 对于密码字段，它们在input-group内，feedback元素也在input-group下
        if (inputElement.id === 'password' || inputElement.id === 'confirmPassword') {
            return inputElement.closest('.input-group').querySelector('.invalid-feedback');
        }
        return inputElement.closest('.form-group').querySelector('.invalid-feedback:not([th\\:if])'); // 选择不由Thymeleaf动态添加的那个
    }


    function displayError(inputElement, message) {
        const feedbackElement = getFeedbackElement(inputElement);
        if (feedbackElement) {
            feedbackElement.textContent = message;
            inputElement.classList.add('is-invalid');
            inputElement.classList.remove('is-valid');
        }
    }

    function clearError(inputElement) {
        const feedbackElement = getFeedbackElement(inputElement);
        if (feedbackElement) {
            feedbackElement.textContent = ''; // 清空前端错误信息
            inputElement.classList.remove('is-invalid');
            // 只有在字段非空且通过所有验证时才添加is-valid
            // 为了避免在用户清空字段后仍然显示is-valid，这里先不加
        }
    }

    function markValid(inputElement) {
        clearError(inputElement); // 先清除可能存在的错误状态
        inputElement.classList.add('is-valid');
    }

    // --- 验证函数 ---
    function validateUsername() {
        const username = usernameInput.value.trim();
        if (username.length === 0 && usernameInput.hasAttribute('required')) {
            displayError(usernameInput, '用户名为必填项。'); return false;
        } else if (username.length > 0 && username.length < 3) {
            displayError(usernameInput, '用户名至少需要3个字符。'); return false;
        } else if (username.length > 20) {
            displayError(usernameInput, '用户名不能超过20个字符。'); return false;
        } else if (username.length > 0 && !/^[a-zA-Z0-9_]+$/.test(username)) {
            displayError(usernameInput, '用户名只能包含字母、数字和下划线。'); return false;
        }
        if (username.length > 0) markValid(usernameInput); else clearError(usernameInput); // 如果清空了，移除is-valid
        return true;
    }

    function validateEmail() {
        const email = emailInput.value.trim();
        const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
        if (email.length === 0 && emailInput.hasAttribute('required')) {
            displayError(emailInput, '邮箱为必填项。'); return false;
        } else if (email.length > 0 && !emailRegex.test(email)) {
            displayError(emailInput, '请输入有效的邮箱地址。'); return false;
        }
        if (email.length > 0) markValid(emailInput); else clearError(emailInput);
        return true;
    }

    function validatePassword() {
        const password = passwordInput.value;
        if (password.length === 0 && passwordInput.hasAttribute('required')) {
            displayError(passwordInput, '密码为必填项。'); return false;
        } else if (password.length > 0 && password.length < 6) {
            displayError(passwordInput, '密码至少需要6个字符。'); return false;
        } else if (password.length > 30) {
            displayError(passwordInput, '密码不能超过30个字符。'); return false;
        }
        if (password.length > 0) markValid(passwordInput); else clearError(passwordInput);
        // 密码更改后，如果确认密码字段有内容，也需要重新验证它
        if (confirmPasswordInput.value.length > 0) {
            validateConfirmPassword();
        }
        return true;
    }

    function validateConfirmPassword() {
        const password = passwordInput.value;
        const confirmPassword = confirmPasswordInput.value;
        if (confirmPassword.length === 0 && confirmPasswordInput.hasAttribute('required')) {
            displayError(confirmPasswordInput, '确认密码为必填项。'); return false;
        } else if (confirmPassword.length > 0 && password !== confirmPassword) {
            displayError(confirmPasswordInput, '两次输入的密码不一致。'); return false;
        }
        if (confirmPassword.length > 0 && password === confirmPassword) markValid(confirmPasswordInput); else clearError(confirmPasswordInput);
        return true;
    }

    // --- 事件监听器绑定到 'blur' 事件 ---
    if (usernameInput) usernameInput.addEventListener('blur', validateUsername);
    if (emailInput) emailInput.addEventListener('blur', validateEmail);
    if (passwordInput) passwordInput.addEventListener('blur', validatePassword);
    if (confirmPasswordInput) confirmPasswordInput.addEventListener('blur', validateConfirmPassword);

    // 为了密码匹配的实时性，当密码字段输入时，如果确认密码已有内容，则立即验证确认密码
    if (passwordInput) {
        passwordInput.addEventListener('input', function() {
            if (confirmPasswordInput.value.length > 0) {
                validateConfirmPassword(); // 当主密码变动时，如果确认密码有值，实时校验确认密码
            }
            // 也可以在这里做一些非常轻量级的实时提示，比如密码长度，但主要验证放在blur
        });
    }


    // --- 密码可见性切换 (保持不变) ---
    function togglePasswordVisibility(inputElement, buttonElement) {
        const type = inputElement.getAttribute('type') === 'password' ? 'text' : 'password';
        inputElement.setAttribute('type', type);
        const icon = buttonElement.querySelector('i');
        if (type === 'password') {
            icon.classList.remove('fa-eye-slash'); icon.classList.add('fa-eye');
        } else {
            icon.classList.remove('fa-eye'); icon.classList.add('fa-eye-slash');
        }
    }
    if (togglePasswordButton && passwordInput) {
        togglePasswordButton.addEventListener('click', function () { togglePasswordVisibility(passwordInput, this); });
    }
    if (toggleConfirmPasswordButton && confirmPasswordInput) {
        toggleConfirmPasswordButton.addEventListener('click', function () { togglePasswordVisibility(confirmPasswordInput, this); });
    }

    // --- 表单提交时的整体验证 (保持不变) ---
    if (signupForm) {
        signupForm.addEventListener('submit', function (event) {
            // 在提交时，确保所有字段都执行一次blur事件的验证逻辑
            const usernameValid = usernameInput ? validateUsername() : true;
            const emailValid = emailInput ? validateEmail() : true;
            const passwordValid = passwordInput ? validatePassword() : true;
            const confirmPasswordValid = confirmPasswordInput ? validateConfirmPassword() : true;

            if (!usernameValid || !emailValid || !passwordValid || !confirmPasswordValid) {
                event.preventDefault();
                event.stopPropagation();
                // 尝试聚焦到第一个无效字段
                const firstInvalid = signupForm.querySelector('.form-control.is-invalid');
                if (firstInvalid) {
                    firstInvalid.focus();
                }
                // 可以选择不显示全局alert，因为每个字段已有提示
                // alert('请修正表单中的错误后再提交。');
            }
        });
    }
});
