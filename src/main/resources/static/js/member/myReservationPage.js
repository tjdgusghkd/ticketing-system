document.addEventListener('DOMContentLoaded', () => {
    loadMyPage();
});

async function loadMyPage() {
    const token = localStorage.getItem('accessToken');

    if (!token) {
        alert('로그인이 필요합니다.');
        location.href = '/members/signin';
        return;
    }

    try {
        const res = await fetch('/api/auth/members/me/reservations', {
            headers: {
                Authorization: 'Bearer ' + token
            }
        });

        if (res.status === 401 || res.status === 403) {
            alert('로그인이 필요하거나 인증이 만료되었습니다.');
            localStorage.removeItem('accessToken');
            localStorage.removeItem('refreshToken');
            location.href = '/members/signin';
            return;
        }

        if (!res.ok) {
            throw new Error('예매내역 조회 실패');
        }

        const data = await res.json();
        renderReservationList(data);
    } catch (error) {
        console.error(error);
        alert('예매내역을 불러오는 중 오류가 발생했습니다.');
    }
}

function renderReservationList(reservationList) {
    const listEl = document.querySelector('#reservationList');
    const resultCountEl =
        document.querySelector('#resultCount');
    const emptyBoxEl = document.querySelector('#emptyBox');

    resultCountEl.innerText = `총 ${reservationList.length}건
  `;

    if (!reservationList || reservationList.length === 0) {
        listEl.innerHTML = '';
        emptyBoxEl.style.display = 'block';
        return;
    }

    emptyBoxEl.style.display = 'none';

    listEl.innerHTML = reservationList.map(reservation => `
      <article class="reservation-item">
        <div class="reservation-main">
          <div class="reservation-thumb">
            <img src="${reservation.posterUrl}"
  alt="${reservation.concertTitle}" />
          </div>
          <div class="reservation-info">
            <div class="reservation-badge">예매완료</div>
            <h3>${reservation.concertTitle}</h3>
            <p class="reservation-place">${reservation.artist}
  </p>
            <p class="reservation-date">관람일시 :
  ${reservation.scheduleDateTime}</p>
            <p class="reservation-seat">좌석 :
  ${reservation.seatSummary}</p>
          </div>
        </div>
        <div class="reservation-action">
          <div class="price-
  info">${Number(reservation.totalPrice).toLocaleString()}원</
  div>
        </div>
      </article>
    `).join('');
}