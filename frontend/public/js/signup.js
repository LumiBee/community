document.addEventListener('DOMContentLoaded', function () {
    const signupForm = document.getElementById('signupForm');
    if (!signupForm) return; // 如果页面上没有表单，则不执行任何操作

    // 禁用浏览器默认的HTML5验证
    signupForm.setAttribute('novalidate', '');

    /**
     * 获取与输入框关联的错误提示元素
     * @param {HTMLElement} inputElement - 输入框元素
     * @returns {HTMLElement|null} - 错误提示的DOM元素
     */
    function getFeedbackElement(inputElement) {
        return inputElement.closest('.form-group').querySelector('.invalid-feedback:not([th\\:if])');
    }

    /**
     * 显示错误信息
     * @param {HTMLElement} input - 输入框元素
     * @param {string} message - 要显示的错误信息
     */
    function displayError(input, message) {
        const feedback = getFeedbackElement(input);
        if (feedback) {
            feedback.textContent = message;
        }
        input.classList.add('is-invalid');
        input.classList.remove('is-valid');
    }

    /**
     * 清除错误信息并标记为有效
     * @param {HTMLElement} input - 输入框元素
     */
    function markValid(input) {
        const feedback = getFeedbackElement(input);
        if (feedback) {
            feedback.textContent = '';
        }
        input.classList.remove('is-invalid');
        input.classList.add('is-valid');
    }

    // --- 验证逻辑函数 ---
    function validateUsername(input) {
        const username = input.value.trim();
        if (username.length === 0 && input.required) {
            displayError(input, '用户名为必填项。'); return false;
        } else if (username.length > 0 && (username.length < 3 || username.length > 20)) {
            displayError(input, '用户名长度必须在3到20个字符之间。'); return false;
        } else if (username.length > 0 && !/^[a-zA-Z0-9_]+$/.test(username)) {
            displayError(input, '用户名只能包含字母、数字和下划线。'); return false;
        }
        markValid(input);
        return true;
    }

    function validateEmail(input) {
        const email = input.value.trim();
        const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
        if (email.length === 0 && input.required) {
            displayError(input, '邮箱为必填项。'); return false;
        } else if (email.length > 0 && !emailRegex.test(email)) {
            displayError(input, '请输入有效的邮箱地址。'); return false;
        }
        markValid(input);
        return true;
    }

    function validatePassword(input) {
        const password = input.value;
        if (password.length === 0 && input.required) {
            displayError(input, '密码为必填项。'); return false;
        } else if (password.length > 0 && (password.length < 6 || password.length > 30)) {
            displayError(input, '密码长度必须在6到30个字符之间。'); return false;
        }
        markValid(input);
        return true;
    }

    function validateConfirmPassword(input) {
        const password = document.getElementById('password').value;
        const confirmPassword = input.value;
        if (confirmPassword.length === 0 && input.required) {
            displayError(input, '确认密码为必填项。'); return false;
        } else if (password !== confirmPassword) {
            displayError(input, '两次输入的密码不一致。'); return false;
        }
        markValid(input);
        return true;
    }

    // 将多个事件监听器统一绑定到表单上 ---

    // 字段验证：监听 'blur' (失去焦点) 事件
    signupForm.addEventListener('blur', function(event) {
        const target = event.target;
        if (target.tagName !== 'INPUT') return;

        switch(target.id) {
            case 'username':
                validateUsername(target);
                break;
            case 'email':
                validateEmail(target);
                break;
            case 'password':
                validatePassword(target);
                // 当主密码框失去焦点时，也重新验证确认密码框
                validateConfirmPassword(document.getElementById('confirmPassword'));
                break;
            case 'confirmPassword':
                validateConfirmPassword(target);
                break;
        }
    }, true); // 使用捕获阶段以确保事件处理

    // 实时验证：监听 'input' 事件，用于密码匹配
    signupForm.addEventListener('input', function(event) {
        const target = event.target;
        // 当在主密码框或确认密码框输入时，都实时检查密码是否匹配
        if (target.id === 'password' || target.id === 'confirmPassword') {
            validateConfirmPassword(document.getElementById('confirmPassword'));
        }
    });

    // 密码可见性切换：监听 'click' 事件
    signupForm.addEventListener('click', function(event) {
        const button = event.target.closest('button');
        if (!button || (button.id !== 'togglePassword' && button.id !== 'toggleConfirmPassword')) {
            return;
        }

        const inputId = (button.id === 'togglePassword') ? 'password' : 'confirmPassword';
        const passwordInput = document.getElementById(inputId);
        const icon = button.querySelector('i');

        const isPassword = passwordInput.getAttribute('type') === 'password';
        passwordInput.setAttribute('type', isPassword ? 'text' : 'password');
        icon.classList.toggle('fa-eye', !isPassword);
        icon.classList.toggle('fa-eye-slash', isPassword);
    });

    // 表单提交：监听 'submit' 事件
    signupForm.addEventListener('submit', function (event) {
        const usernameValid = validateUsername(document.getElementById('username'));
        const emailValid = validateEmail(document.getElementById('email'));
        const passwordValid = validatePassword(document.getElementById('password'));
        const confirmPasswordValid = validateConfirmPassword(document.getElementById('confirmPassword'));

        if (!usernameValid || !emailValid || !passwordValid || !confirmPasswordValid) {
            event.preventDefault(); // 阻止表单提交
            event.stopPropagation();

            // 将焦点定位到第一个无效的输入框
            const firstInvalid = signupForm.querySelector('.is-invalid');
            if (firstInvalid) {
                firstInvalid.focus();
            }
        }
        // 如果所有验证都通过，表单将正常提交
    });
});