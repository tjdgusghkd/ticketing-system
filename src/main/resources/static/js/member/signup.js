document.addEventListener('DOMContentLoaded', ()=>{
	const passwordInput = document.querySelector('#password');
	const passwordCheck = document.querySelector('#confirmPassword');
	const signupBtn = document.querySelector('.signup-btn');
	const signupForm = document.querySelector('.signup-form');
	const idInput = document.querySelector('#loginId');
	const emailInput = document.querySelector('#email');
	const phoneInput = document.querySelector('#phone');
	
	const phoneRegex = /^010\d{8}$/;
	const passwordRegex = /^(?=.*[A-Za-z])(?=.*\d)(?=.*[!@#$%^&*()_+\-=\[\]{};':"\\|,.<>\/?]).{10,}$/;
	
	signupBtn.addEventListener('click', (e) =>{
		
		e.preventDefault();
		
		let checkFlag = true;
		
		if(passwordInput.value != passwordCheck.value){
			checkFlag = false;
			alert('비밀번호 확인란이 일치하지 않습니다.');
			passwordCheck.focus();
		} else if(!passwordRegex.test(passwordInput.value)){
			checkFlag = false;
			alert('비밀번호는 영문, 숫자, 특수문자 1개 이상을 포함한 10자리 이상이여야 합니다.');
			passwordInput.focus();
		} else if(idInput.value.trim() == ''){
			checkFlag = false;
			alert('아이디를 입력해주세요.');
			idInput.focus();
		} else if(emailInput.value.trim() == ''){
			checkFlag = false;
			alert('이메일을 입력해주세요.');
			emailInput.focus();
		} else if(!phoneRegex.test(phoneInput.value.trim())) {
			checkFlag = false;
			alert('휴대폰번호를 형식에 맞춰서 입력해주세요.');
			phoneInput.focus();
		}
		
		if(checkFlag){
			signupForm.submit();
		}
	});
});