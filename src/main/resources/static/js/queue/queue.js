const progressFill = document.getElementById('progressFill');
const progressText = document.getElementById('progressText');
const queueCard = document.querySelector('.queue-card');

let scheduleNo = null;
let queueLeaveSent = false;
let pollingId = null;

let heartbeatId = null;

function startHeartbeat() {
	if(heartbeatId) {
		return;
	}
	
	sendHeartbeat();
	
	heartbeatId = setInterval(() => {
		sendHeartbeat();
	}, 10000);
}

function stopHeartbeat() {
	if(heartbeatId) {
		clearInterval(heartbeatId);
		heartbeatId = null;
	}
}

async function sendHeartbeat() {
	const accessToken = localStorage.getItem("accessToken");
	if(!accessToken || !scheduleNo) {
		return;
	}
	
	try {
		await fetch(`/api/queue/${scheduleNo}/heartbeat`, {
			method: 'POST',
			headers: {
				Authorization: `Bearer ${accessToken}`
			}
		});		
	} catch (error){ 
		console.error('heartbeat failed',error);
	}
}

function updateProgress(value) {
    progressFill.style.width = `${value}%`;
    progressText.textContent = `${value}%`;
}

document.addEventListener('DOMContentLoaded', () => {
    scheduleNo = document.querySelector('#scheduleNo').value;
    enterQueue();
	
	window.addEventListener('pagehide', () => {
	  leaveQueueOnExit();
	});

	window.addEventListener('beforeunload', () => {
	  leaveQueueOnExit();
	});
});

async function enterQueue() {
    const accessToken = localStorage.getItem('accessToken');

    if (!accessToken) {
        handleAuthFail();
        return;
    }

    try {
        const response = await fetch(`/api/queue/${scheduleNo}/enter`, {
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
            throw new Error('Queue enter failed');
        }

        const data = await response.json();

        if (data.allowed) {
			stopHeartbeat();
            location.replace(`/rounds/${scheduleNo}/seats`);
            return;
        }

        renderQueueInfo(data);
		startHeartbeat();
        startPolling(scheduleNo);
    } catch (error) {
        console.error(error);
        if (queueCard) {
            queueCard.hidden = false;
        }
        alert('Queue request failed.');
    }
}

function renderQueueInfo(data) {
    const rankEl = document.querySelector('#queueRank');
    const waitEl = document.querySelector('#estimatedWait');
    const statusEl = document.querySelector('#queueStatus');

    const rank = data.rank ?? 1;
    const estimatedMinutes = Math.max(1, Math.ceil(rank / 2));
    const progressValue = Math.min(95, Math.max(5, 100 - rank * 5));

    if (queueCard) {
        queueCard.hidden = false;
    }

    if (rankEl) {
        rankEl.innerText = rank.toLocaleString();
    }

    if (waitEl) {
        waitEl.innerText = `About ${estimatedMinutes} min`;
    }

    if (statusEl) {
        statusEl.innerText = data.allowed ? 'Ready' : 'Waiting';
    }

    updateProgress(progressValue);
}

function startPolling(scheduleNo) {
    if (pollingId) {
        return;
    }

    pollingId = setInterval(() => {
        checkQueueStatus(scheduleNo);
    }, 2000);
}

function stopPolling() {
    if (pollingId) {
        clearInterval(pollingId);
        pollingId = null;
    }
}

async function checkQueueStatus(scheduleNo) {
    try {
        const accessToken = localStorage.getItem('accessToken');

        if (!accessToken) {
            handleAuthFail();
            return;
        }

        const response = await fetch(`/api/queue/${scheduleNo}/status`, {
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
            throw new Error('Queue status failed');
        }

        const data = await response.json();

        if (data.allowed) {
			stopHeartbeat();
            stopPolling();
            location.replace(`/rounds/${scheduleNo}/seats`);
            return;
        }
		
        renderQueueInfo(data);
    } catch (error) {
        console.error(error);
    }
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
  stopHeartbeat();
  stopPolling();

  await fetch(`/api/queue/${scheduleNo}/leave`, {
    method: 'POST',
    headers: {
      Authorization: `Bearer ${accessToken}`
    },
    keepalive: true 
	// keepalive: true는 보통 브라우저를 닫으면 실행중인 모든 네트워크 요청이 취소되지만
	// 해당 옵션은 브라우저가 꺼져도 이 요청은 끝까지 서버에 전달해줘라고 운영체제에 부탁
  }).catch(() => {
    queueLeaveSent = false;
  });
}


function handleAuthFail() {
	stopPolling();
	stopHeartbeat();
    alert('Login is required or your session has expired.');
    localStorage.removeItem('accessToken');
    localStorage.removeItem('refreshToken');
    location.href = '/members/signin';
}
