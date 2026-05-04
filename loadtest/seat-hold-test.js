import http from 'k6/http';
import { sleep } from 'k6';

export const options = {
    vus: Number(__ENV.VUS || 20),
    duration: __ENV.DURATION || '20s',
};

const BASE_URL = __ENV.BASE_URL || 'http://localhost:8080';
const ACCESS_TOKEN = __ENV.ACCESS_TOKEN || 'REPLACE_ME';
const SCHEDULE_NO = __ENV.SCHEDULE_NO || '1';
const SCHEDULE_SEAT_NO = __ENV.SCHEDULE_SEAT_NO || '20';
const SLEEP_SECONDS =
    Number(__ENV.SLEEP_SECONDS || 1);

function authParams() {
    return {
        headers: {
            Authorization: `Bearer ${ACCESS_TOKEN}`,
        },
    };
}

export default function() {
    const params = authParams();

    http.post(`${BASE_URL}/api/queue/${SCHEDULE_NO}/enter`, null, params);
    http.post(`${BASE_URL}/api/queue/${SCHEDULE_NO}/heartbeat`, null, params);
    http.post(`${BASE_URL}/api/rounds/${SCHEDULE_NO}/hold/${SCHEDULE_SEAT_NO}`, null, params);

    sleep(SLEEP_SECONDS);
}