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

function goReserve() {
  if (!selectedScheduleNo) {
    alert('회차를 선택해주세요.');
    return;
  }

  // location.href = `/rounds/${selectedScheduleNo}/seats`;
  location.href = '/queue/queue';
}