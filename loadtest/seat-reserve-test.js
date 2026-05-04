import http from 'k6/http';
import { sleep } from 'k6';

export const options= {
	vus: Number(__ENV.VUS || 20),
	duration: __ENV.DURATION || '20s',
};

const BASE_URL = __ENV.BASE_URL || 'http://localhost:8080';
const ACCESS_TOKEN = __ENV.ACCESS_TOKEN || 'REPLACE_ME';
const SCHEDULE_NO = __ENV.SCHEDULE_NO || '1';
const SLEEP_SECONDS = Number(__ENV.SLEEP_SECONDS || 1);

function authHeaders() {
	return {
		Authorization: `Bearer ${ACCESS_TOKEN}`,
		'Content-Type' : 'application/json',
	};
}

export default function() {
	const headers = authHeaders();
	
	const seatNo = ((__VU - 1) * 20 + (__ITER % 20)) + 1;
	http.post(`${BASE_URL}/api/queue/${SCHEDULE_NO}/enter`, null, { headers });
	http.post(`${BASE_URL}/api/queue/${SCHEDULE_NO}/heartbeat`, null, { headers });
	http.post(`${BASE_URL}/api/rounds/${SCHEDULE_NO}/hold/${seatNo}`, null, { headers });
	
	const body = JSON.stringify({
		seatIds: [seatNo],
	});
	
	http.post(`${BASE_URL}/api/rounds/${SCHEDULE_NO}/reserve`, body, { headers });
	
	sleep(SLEEP_SECONDS);
}