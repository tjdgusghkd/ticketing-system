document.addEventListener('DOMContentLoaded', () => {
    const loginForm = document.querySelector('#loginForm');
    const loginIdInput = document.querySelector('#loginId');
    const passwordInput = document.querySelector('#password');

    loginForm.addEventListener('submit', async (e) => {
        e.preventDefault();

        const loginId = loginIdInput.value.trim();
        const password = passwordInput.value.trim();

        if (loginId === '') {
            alert('아이디를 입력해주세요.');
            loginIdInput.focus();
            return;
        }

        if (password === '') {
            alert('비밀번호를 입력해주세요.');
            passwordInput.focus();
            return;
        }

        try {
            const response = await fetch('/api/auth/login', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify({
                    loginId: loginId,
                    password: password
                })
            });

            if (!response.ok) {
                throw new Error('로그인 실패');
            }

            const result = await response.json();

            localStorage.setItem('accessToken', result.accessToken);
            localStorage.setItem('refreshToken', result.refreshToken);

            alert('로그인 성공');
            location.href = '/';
        } catch (error) {
            alert('아이디 또는 비밀번호를 확인해주세요.');
            console.error(error);
        }
    });
});