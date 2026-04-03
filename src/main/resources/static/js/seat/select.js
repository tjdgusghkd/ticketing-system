const MAX_SELECT_COUNT = 4;
const SEAT_PRICE = 99000;

let seatData = [];
let selectedSeats = [];

document.addEventListener('DOMContentLoaded', async () => {
  await loadSeats();

  const completeBtn = document.querySelector('#completeBtn');
  completeBtn.addEventListener('click', reserveSeats);
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

    if (response.status === 401 || response.status === 403) {
      handleAuthFail();
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

    if (seat.booked) {
      button.classList.add('reserved');
      button.disabled = true;
    } else if (seat.held && !seat.holdByMe) {
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
      seat.section === 'A' ? '#zoneA' :
      seat.section === 'B' ? '#zoneB' : '#zoneC'
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

    if (response.status === 401 || response.status === 403) {
      handleAuthFail();
      return;
    }

	if (response.status === 409) {
	    const message = await response.text();
	    alert(message);
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
    const response = await fetch(
      `/api/rounds/${scheduleNo}/hold/${seat.scheduleSeatNo}`,
      {
        method: 'DELETE',
        headers: {
          Authorization: `Bearer ${accessToken}`
        }
      }
    );

    if (response.status === 401 || response.status === 403) {
      handleAuthFail();
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
        <strong>${SEAT_PRICE.toLocaleString()}원</strong>
      </div>
    `)
    .join('');

  countEl.innerText = String(selectedSeats.length);
  priceEl.innerText = `${(selectedSeats.length * SEAT_PRICE).toLocaleString()}원`;
  completeBtn.disabled = false;
  completeBtn.classList.add('active');
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

    if (response.status === 401 || response.status === 403) {
      handleAuthFail();
      return;
    }

    if (response.status === 409) {
      alert('선택한 좌석 중 이미 예약되었거나 선점이 만료된 좌석이 있습니다.');
      await loadSeats();
      return;
    }

    if (!response.ok) {
      throw new Error('예매 실패');
    }

    alert('예매가 완료되었습니다.');
    location.href = '/mypage';
  } catch (error) {
    console.error(error);
    alert('예매 처리 중 오류가 발생했습니다.');
  }
}

function handleAuthFail() {
  alert('로그인이 필요하거나 인증이 만료되었습니다.');
  localStorage.removeItem('accessToken');
  localStorage.removeItem('refreshToken');
  location.href = '/members/signin';
}