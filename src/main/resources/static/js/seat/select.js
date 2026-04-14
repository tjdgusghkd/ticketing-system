const MAX_SELECT_COUNT = 4;

let seatData = [];
let selectedSeats = [];
let holdTimerInterval = null;
let holdDeadline = null;
let queueLeaveSent = false;

document.addEventListener('DOMContentLoaded', async () => {
  console.log('select.js loaded');
  await loadSeats();

  const completeBtn = document.querySelector('#completeBtn');
  const confirmBookingBtn = document.querySelector('#confirmBookingBtn');
  const closeBookingModalBtn = document.querySelector('#closeBookingModalBtn');
  const bookingModalBackdrop = document.querySelector('#bookingModalBackdrop');

  completeBtn.addEventListener('click', openBookingModal);
  confirmBookingBtn.addEventListener('click', reserveSeats);
  closeBookingModalBtn.addEventListener('click', closeBookingModal);
  bookingModalBackdrop.addEventListener('click', closeBookingModal);
});

window.addEventListener('pagehide', () => {
  leaveQueueOnExit();
});

window.addEventListener('beforeunload', () => {
  leaveQueueOnExit();
});

async function loadSeats() {
  const accessToken = localStorage.getItem('accessToken');

  if (!accessToken) {
    handleAuthFail();
    return;
  }

  try {
    const response = await fetch(`/api/rounds/${scheduleNo}/seats`, {
      method: 'GET',
      headers: {
        Authorization: `Bearer ${accessToken}`
      }
    });

    console.log('seats response status =', response.status);

    if (response.status === 401) {
      handleAuthFail();
      return;
    }

    if (response.status === 403) {
      handleQueueBlocked();
      return;
    }

    if (!response.ok) {
      throw new Error('좌석 조회 실패');
    }

    seatData = await response.json();
    syncSelectedSeats();
    renderSeats();
    renderSelectedSeatPanel();
  } catch (error) {
    console.error(error);
    alert('좌석 정보를 불러오지 못했습니다.');
  }
}

function syncSelectedSeats() {
  selectedSeats = seatData.filter(seat => seat.holdByMe);
  syncHoldDeadline();
}

function syncHoldDeadline() {
  if (selectedSeats.length === 0) {
    holdDeadline = null;
    stopHoldTimer();
    updateHoldTimerText(0);
    return;
  }

  const expiresInValues = selectedSeats
    .map(seat => seat.holdExpiresInSeconds)
    .filter(value => typeof value === 'number' && value > 0);

  if (expiresInValues.length === 0) {
    holdDeadline = null;
    stopHoldTimer();
    updateHoldTimerText(0);
    return;
  }

  holdDeadline = Date.now() + Math.min(...expiresInValues) * 1000;

  if (!document.querySelector('#bookingModal').hidden) {
    startHoldTimer();
  }
}

function renderSeats() {
  document.querySelector('#zoneA').innerHTML = '';
  document.querySelector('#zoneB').innerHTML = '';
  document.querySelector('#zoneC').innerHTML = '';

  seatData.forEach(seat => {
    const button = document.createElement('button');
    button.type = 'button';
    button.className = 'seat-btn';
    button.dataset.seatId = seat.scheduleSeatNo;
    button.dataset.seatLabel = `${seat.section}-${seat.rowNum}-${seat.seatNumber}`;
    button.title = `${seat.section}-${seat.rowNum}-${seat.seatNumber}`;

    if (seat.booked || (seat.held && !seat.holdByMe)) {
      button.classList.add('reserved');
      button.disabled = true;
    } else {
      if (seat.holdByMe) {
        button.classList.add('selected');
      }

      button.addEventListener('click', async () => {
        await toggleSeat(seat);
      });
    }

    const targetZone = document.querySelector(
      seat.section === 'A' ? '#zoneA'
        : seat.section === 'B' ? '#zoneB'
          : '#zoneC'
    );

    targetZone.appendChild(button);
  });
}

async function toggleSeat(seat) {
  const isSelected = selectedSeats.some(
    item => item.scheduleSeatNo === seat.scheduleSeatNo
  );

  if (isSelected) {
    await unholdSeat(seat);
    return;
  }

  if (selectedSeats.length >= MAX_SELECT_COUNT) {
    alert(`최대 ${MAX_SELECT_COUNT}좌석까지 선택할 수 있습니다.`);
    return;
  }

  await holdSeat(seat);
}

async function holdSeat(seat) {
  const accessToken = localStorage.getItem('accessToken');

  try {
    const response = await fetch(`/api/rounds/${scheduleNo}/hold/${seat.scheduleSeatNo}`, {
      method: 'POST',
      headers: {
        Authorization: `Bearer ${accessToken}`
      }
    });

    if (response.status === 401) {
      handleAuthFail();
      return;
    }

    if (response.status === 403) {
      handleQueueBlocked();
      return;
    }

    if (response.status === 409) {
      const message = await response.text();
      alert(message);
      await loadSeats();
      return;
    }

    if (!response.ok) {
      throw new Error('좌석 선점 실패');
    }

    await loadSeats();
  } catch (error) {
    console.error(error);
    alert('좌석 선택 중 오류가 발생했습니다.');
  }
}

async function unholdSeat(seat) {
  const accessToken = localStorage.getItem('accessToken');

  try {
    const response = await fetch(`/api/rounds/${scheduleNo}/hold/${seat.scheduleSeatNo}`, {
      method: 'DELETE',
      headers: {
        Authorization: `Bearer ${accessToken}`
      }
    });

    if (response.status === 401) {
      handleAuthFail();
      return;
    }

    if (response.status === 403) {
      handleQueueBlocked();
      return;
    }

    if (!response.ok) {
      throw new Error('좌석 선점 해제 실패');
    }

    await loadSeats();
  } catch (error) {
    console.error(error);
    alert('좌석 선택 해제 중 오류가 발생했습니다.');
  }
}

function renderSelectedSeatPanel() {
  const listEl = document.querySelector('#selectedSeatList');
  const countEl = document.querySelector('#selectedCount');
  const priceEl = document.querySelector('#totalPrice');
  const completeBtn = document.querySelector('#completeBtn');

  if (selectedSeats.length === 0) {
    listEl.innerHTML = '<p class="empty-text">선택한 좌석이 없습니다.</p>';
    countEl.innerText = '0';
    priceEl.innerText = '0원';
    completeBtn.disabled = true;
    completeBtn.classList.remove('active');
    return;
  }

  listEl.innerHTML = selectedSeats
    .map(seat => `
      <div class="selected-seat-item">
        <span>${seat.section}-${seat.rowNum}-${seat.seatNumber}</span>
        <strong>${seat.price.toLocaleString()}원</strong>
      </div>
    `)
    .join('');

  countEl.innerText = String(selectedSeats.length);
  priceEl.innerText = `${selectedSeats
    .reduce((total, seat) => total + seat.price, 0)
    .toLocaleString()}원`;
  completeBtn.disabled = false;
  completeBtn.classList.add('active');
}

function renderModalSeatList() {
  const modalSeatList = document.querySelector('#modalSeatList');

  if (selectedSeats.length === 0) {
    modalSeatList.innerHTML = '<p class="empty-text">선택한 좌석이 없습니다.</p>';
    return;
  }

  modalSeatList.innerHTML = selectedSeats
    .map(seat => `
      <div class="selected-seat-item">
        <span>${seat.section}-${seat.rowNum}-${seat.seatNumber}</span>
        <strong>${seat.price.toLocaleString()}원</strong>
      </div>
    `)
    .join('');
}

function openBookingModal() {
  if (selectedSeats.length === 0) {
    return;
  }

  renderModalSeatList();
  document.querySelector('#bookingModal').hidden = false;
  startHoldTimer();
}

function closeBookingModal() {
  document.querySelector('#bookingModal').hidden = true;
  stopHoldTimer();
}

function startHoldTimer() {
  stopHoldTimer();
  updateHoldTimer();
  holdTimerInterval = window.setInterval(updateHoldTimer, 1000);
}

function stopHoldTimer() {
  if (holdTimerInterval !== null) {
    window.clearInterval(holdTimerInterval);
    holdTimerInterval = null;
  }
}

function updateHoldTimer() {
  if (!holdDeadline) {
    updateHoldTimerText(0);
    return;
  }

  const remainingSeconds = Math.max(0, Math.floor((holdDeadline - Date.now()) / 1000));
  updateHoldTimerText(remainingSeconds);

  if (remainingSeconds <= 0) {
    handleHoldExpired();
  }
}

function updateHoldTimerText(totalSeconds) {
  const minutes = String(Math.floor(totalSeconds / 60)).padStart(2, '0');
  const seconds = String(totalSeconds % 60).padStart(2, '0');
  document.querySelector('#holdTimer').innerText = `${minutes}:${seconds}`;
}

async function handleHoldExpired() {
  stopHoldTimer();
  closeBookingModal();
  alert('선점 시간이 만료되어 좌석이 자동 해제되었습니다.');
  await loadSeats();
}

async function reserveSeats() {
  if (selectedSeats.length === 0) {
    return;
  }

  const accessToken = localStorage.getItem('accessToken');

  try {
    const response = await fetch(`/api/rounds/${scheduleNo}/reserve`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        Authorization: `Bearer ${accessToken}`
      },
      body: JSON.stringify({
        seatIds: selectedSeats.map(seat => seat.scheduleSeatNo)
      })
    });

    if (response.status === 401) {
      handleAuthFail();
      return;
    }

    if (response.status === 403) {
      handleQueueBlocked();
      return;
    }

    if (response.status === 409) {
      const message = await response.text();
      alert(message);
      await loadSeats();
      return;
    }

    if (!response.ok) {
      throw new Error('예약 실패');
    }

    closeBookingModal();
    alert('예약이 완료되었습니다.');
    location.href = '/members/me/reservations';
  } catch (error) {
    console.error(error);
    alert('예약 처리 중 오류가 발생했습니다.');
  }
}

function handleAuthFail() {
  alert('로그인이 필요하거나 인증이 만료되었습니다.');
  localStorage.removeItem('accessToken');
  localStorage.removeItem('refreshToken');
  location.href = '/members/signin';
}

function handleQueueBlocked() {
  console.log('handleQueueBlocked called');
  alert('현재 좌석 페이지 입장 권한이 없습니다. 대기열로 이동합니다.');
  location.href = `/queue/queue/${scheduleNo}`;
}

async function leaveQueueOnExit() {
  if (queueLeaveSent) {
    return;
  }

  const accessToken = localStorage.getItem('accessToken');
  if (!accessToken || !scheduleNo) {
    return;
  }

  queueLeaveSent = true;

  await fetch(`/api/queue/${scheduleNo}/leave`, {
    method: 'POST',
    headers: {
      Authorization: `Bearer ${accessToken}`
    },
    keepalive: true
  }).catch(() => {
    queueLeaveSent = false;
  });
}
