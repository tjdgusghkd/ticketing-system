document.addEventListener('DOMContentLoaded', ()=>{
	const passwordInput = document.querySelector('#password');
	const passwordCheck = document.querySelector('#confirmPassword');
	const signupBtn = document.querySelector('.signup-btn');
	const signupForm = document.querySelector('.signup-form');
	const idInput = document.querySelector('#loginId');
	const emailInput = document.querySelector('#email');
	const phoneInput = document.querySelector('#phone');
	const idCheckMessage = document.querySelector('#idCheckMessage');
	const idInputWrap = idInput.closest('.input-wrap');

	

	
	const phoneRegex = /^010\d{8}$/;
	const passwordRegex = /^(?=.*[A-Za-z])(?=.*\d)(?=.*[!@#$%^&*()_+\-=\[\]{};':"\\|,.<>\/?]).{10,}$/;
	const idRegex = /^[a-zA-Z0-9]{4,20}$/;
	
	
	let isIdCheck = false;	
	let checkedIdValue = '';
	
	idInput.addEventListener('blur', async () => {
		const loginId = idInput.value.trim();
		
		if(checkedIdValue == loginId) return;
		
		resetIdCheckMessage();
		
		if(loginId == ''){
			idCheckMessage.textContent = '아이디를 입력해주세요.';
			idCheckMessage.classList.add('error');
			idInputWrap.classList.add('error');
			isIdCheck = false;
			return;
		}
		
		if(!idRegex.test(loginId)){
			idCheckMessage.textContent = '아이디는 영문 또는 숫자 4~20자리여야 합니다.';
			idCheckMessage.classList.add('error');
			idInputWrap.classList.add('error');
			isIdCheck = false;
			return;
		}
		
		try {
			const available = await isIdAvailable(loginId);

			if (available) {
				idCheckMessage.textContent = '사용 가능한 아이디입니다.';
				idCheckMessage.classList.add('success');
				idInputWrap.classList.add('success');
				isIdCheck = true;
				checkedIdValue = loginId;
			} else {
				idCheckMessage.textContent = '사용 불가능한 아이디입니다.';
				idCheckMessage.classList.add('error');
				idInputWrap.classList.add('error');
				isIdCheck = false;
				checkedIdValue = '';
			}
		} catch (error) {
			idCheckMessage.textContent = '아이디 중복 확인 중 오류가 발생했습니다.';
			idCheckMessage.classList.add('error');
			idInputWrap.classList.add('error');
			isIdCheck = false;
			checkedIdValue = '';
			console.error(error);
		}
	});
	
	idInput.addEventListener('input', () => {
		isIdCheck = false;
		checkedIdValue = '';
		resetIdCheckMessage();
	});
	
	signupBtn.addEventListener('click', (e) =>{
		const checkedGender = document.querySelector('input[name="gender"]:checked');
		
		e.preventDefault();
		
		if (idInput.value.trim() === '') {
			alert('아이디를 입력해주세요.');
			idInput.focus();
			return;
		}

		if (!isIdCheck || checkedIdValue !== idInput.value.trim()) {
			alert('아이디 중복확인을 완료해주세요.');
			idInput.focus();
			return;
		}

		if (emailInput.value.trim() === '') {
			alert('이메일을 입력해주세요.');
			emailInput.focus();
			return;
		}

		if (!phoneRegex.test(phoneInput.value.trim())) {
			alert('휴대폰번호를 형식에 맞춰서 입력해주세요.');
			phoneInput.focus();
			return;
		}

		if (!passwordRegex.test(passwordInput.value)) {
			alert('비밀번호는 영문, 숫자, 특수문자를 포함한 10자리 이상이어야 합니다.');
			passwordInput.focus();
			return;
		}

		if (passwordInput.value !== passwordCheck.value) {
			alert('비밀번호 확인란이 일치하지 않습니다.');
			passwordCheck.focus();
			return;
		}
		
		if(!checkedGender){
			alert('성별을 선택해주세요.');
			return;
		}

		signupForm.submit();
		
		
	});
	
	function resetIdCheckMessage() {
		idCheckMessage.textContent = '';
		idCheckMessage.classList.remove('success', 'error');
		idInputWrap.classList.remove('success', 'error');
	}
});



async function isIdAvailable(id) {
	const response = await fetch(`/members/check-id?loginId=${encodeURIComponent(id)}`, {
		method: 'GET'
	});
	
	if(!response.ok) {
		throw new Error('아이디 중복확인 실패');
	}
	
	const result = await response.json();
	return result.available;
}