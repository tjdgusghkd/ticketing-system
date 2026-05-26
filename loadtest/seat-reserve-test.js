import http from 'k6/http';
import { sleep } from 'k6';

const BASE_URL = __ENV.BASE_URL || 'https://tickethubs.duckdns.org';
const SCHEDULE_NO = __ENV.SCHEDULE_NO || '1';
const SLEEP_SECONDS = Number(__ENV.SLEEP_SECONDS || 1);
const SEAT_START_NO = Number(__ENV.SEAT_START_NO || 1001);
const SEAT_COUNT = Number(__ENV.SEAT_COUNT || 100);
const VUS = Number(__ENV.VUS || 3);
const USE_SHARED_ITERATIONS = (__ENV.USE_SHARED_ITERATIONS || 'true') === 'true';
const TOTAL_RESERVES = Number(__ENV.TOTAL_RESERVES || SEAT_COUNT);
const TOKENS = (__ENV.TOKENS || '')
	.split(',')
	.map(token => token.trim())
	.filter(Boolean);

export const options = USE_SHARED_ITERATIONS
	? {
			scenarios: {
				reserve_flow: {
					executor: 'shared-iterations',
					vus: VUS,
					iterations: TOTAL_RESERVES,
					maxDuration: __ENV.MAX_DURATION || '2m',
				},
			},
		}
	: {
			vus: VUS,
			duration: __ENV.DURATION || '30s',
		};

function authHeaders(token) {
	return {
		Authorization: `Bearer ${token}`,
		'Content-Type' : 'application/json',
	};
}

export default function() {
	if (TOKENS.length === 0) {
		throw new Error('TOKENS env is required');
	}

	const token = TOKENS[(__VU - 1) % TOKENS.length];
	const headers = authHeaders(token);
	
	const seatOffset = __ITER % SEAT_COUNT;
	const seatNo = SEAT_START_NO + seatOffset;
	http.post(`${BASE_URL}/api/queue/${SCHEDULE_NO}/enter`, null, { headers });
	http.post(`${BASE_URL}/api/queue/${SCHEDULE_NO}/heartbeat`, null, { headers });

	const holdRes = http.post(`${BASE_URL}/api/rounds/${SCHEDULE_NO}/hold/${seatNo}`, null, { headers });
	if (holdRes.status !== 200) {
		sleep(SLEEP_SECONDS);
		return;
	}
	
	const body = JSON.stringify({
		seatIds: [seatNo],
	});
	
	http.post(`${BASE_URL}/api/rounds/${SCHEDULE_NO}/reserve`, body, { headers });
	
	sleep(SLEEP_SECONDS);
}
