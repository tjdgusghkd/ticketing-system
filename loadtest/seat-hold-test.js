import http from 'k6/http';
import { sleep } from 'k6';

export const options = {
    vus: Number(__ENV.VUS || 20),
    duration: __ENV.DURATION || '30s',
};

const BASE_URL = __ENV.BASE_URL || 'https://tickethubs.duckdns.org';
const ACCESS_TOKEN = __ENV.ACCESS_TOKEN || 'REPLACE_ME';
const SCHEDULE_NO = __ENV.SCHEDULE_NO || '1';
const SEAT_START_NO = Number(__ENV.SEAT_START_NO || 1);
const SEAT_COUNT = Number(__ENV.SEAT_COUNT || 50);
const SLEEP_SECONDS = Number(__ENV.SLEEP_SECONDS || 1);

function authParams() {
    return {
        headers: {
            Authorization: `Bearer ${ACCESS_TOKEN}`,
        },
    };
}

export default function() {
    const params = authParams();
    const scheduleSeatNo = SEAT_START_NO + (((__VU - 1) + __ITER) % SEAT_COUNT);

    const enterRes = http.post(`${BASE_URL}/api/queue/${SCHEDULE_NO}/enter`, null, params);
    if (enterRes.status !== 200) {
        console.log(`enter status=${enterRes.status} body=${enterRes.body}`);
    }

    const heartbeatRes = http.post(`${BASE_URL}/api/queue/${SCHEDULE_NO}/heartbeat`, null, params);
    if (heartbeatRes.status !== 200) {
        console.log(`heartbeat status=${heartbeatRes.status} body=${heartbeatRes.body}`);
    }

    const holdRes = http.post(`${BASE_URL}/api/rounds/${SCHEDULE_NO}/hold/${scheduleSeatNo}`, null, params);
    if (holdRes.status !== 200) {
        console.log(`hold seat=${scheduleSeatNo} status=${holdRes.status} body=${holdRes.body}`);
        sleep(SLEEP_SECONDS);
        return;
    }

    const unholdRes = http.del(`${BASE_URL}/api/rounds/${SCHEDULE_NO}/hold/${scheduleSeatNo}`, null, params);
    if (unholdRes.status !== 200) {
        console.log(`unhold seat=${scheduleSeatNo} status=${unholdRes.status} body=${unholdRes.body}`);
    }

    sleep(SLEEP_SECONDS);
}
