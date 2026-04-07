document.addEventListener('DOMContentLoaded', () => {
	const accessToken = localStorage.getItem('accessToken');
	const authSection = document.getElementById('auth-section');
	
	if(accessToken) {
		authSection.innerHTML = `
			<a class="btn btn-login" href="javascript:void(0);" onclick="handleLogout()">로그아웃</a>
		`;
	} else {
		authSection.innerHTML = `
			<a class="btn btn-login" href="/members/signin">로그인</a>
			<a class="btn btn-signup" href="/members/signup">회원가입</a>
		`;
	}
	
});

async function handleLogout() {
	if(!confirm("로그아웃 하시겠습니까?")) return;
	
	const accessToken = localStorage.getItem('accessToken');
	
	try {
		
		await fetch('/api/auth/logout', {
			method: 'POST',
			headers: {
				'Authorization' : `Bearer ${accessToken}`
			}
		});
		localStorage.removeItem('accessToken');
		localStorage.removeItem('refreshToken');
		
		alert("로그아웃 되었습니다.");
		location.href= "/";
	} catch(error) {
		console.error("Logout failed", error);
	}
}