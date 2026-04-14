let selectedScheduleNo = null;

function selectSchedule(scheduleNo, element) {
    selectedScheduleNo = scheduleNo;

    document.querySelectorAll('.schedule-card').forEach(card => {
        card.classList.remove('active');
    });

    element.classList.add('active');

    const reserveBtn = document.querySelector('#reserveBtn');
    reserveBtn.disabled = false;

    const dateText = element.querySelector('.schedule-date')?.innerText ?? '';
    const timeText = element.querySelector('.schedule-time')?.innerText ?? '';

    document.querySelector('#selectedScheduleText').innerText = `${dateText} ${timeText}`;
}

async function goReserve() {
    if (!selectedScheduleNo) {
        alert('회차를 선택해주세요.');
        return;
    }

    const accessToken = localStorage.getItem('accessToken');

    if (!accessToken) {
        handleAuthFail();
        return;
    }

    try {
        const response = await fetch(`/api/queue/${selectedScheduleNo}/enter`, {
            method: 'POST',
            headers: {
                Authorization: `Bearer ${accessToken}`
            }
        });

        if (response.status === 401 || response.status === 403) {
            handleAuthFail();
            return;
        }

        if (!response.ok) {
            throw new Error('대기열 진입 실패');
        }

        const data = await response.json();

        if (data.allowed) {
            location.replace(`/rounds/${selectedScheduleNo}/seats`);
            return;
        }

        location.href = `/queue/queue/${selectedScheduleNo}`;
    } catch (error) {
        console.error(error);
        alert('대기열 시스템 통신 중 오류가 발생했습니다.');
    }
}

function handleAuthFail() {
    alert('로그인이 필요하거나 인증이 만료되었습니다.');
    localStorage.removeItem('accessToken');
    localStorage.removeItem('refreshToken');
    location.href = '/members/signin';
}
