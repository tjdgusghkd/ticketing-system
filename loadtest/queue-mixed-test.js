import http from 'k6/http';
import { sleep } from 'k6';

export const options = {
  vus: Number(__ENV.VUS || 100),
  duration: __ENV.DURATION || '1m',
};

const BASE_URL = __ENV.BASE_URL || 'http://localhost:8080';
const SCHEDULE_NO = __ENV.SCHEDULE_NO || '1';
const ACCESS_TOKEN = __ENV.ACCESS_TOKEN || 'REPLACE_ME';
const STATUS_SLEEP_SECONDS = Number(__ENV.STATUS_SLEEP_SECONDS || 2);
const HEARTBEAT_INTERVAL = Number(__ENV.HEARTBEAT_INTERVAL || 5);
const LOOP_COUNT = Number(__ENV.LOOP_COUNT || 10);

export default function () {
  const params = {
    headers: {
      Authorization: `Bearer ${ACCESS_TOKEN}`,
    },
  };

  const enterRes = http.post(
    `${BASE_URL}/api/queue/${SCHEDULE_NO}/enter`,
    null,
    params
  );

  if (enterRes.status !== 200) {
    sleep(1);
    return;
  }

  for (let i = 1; i <= LOOP_COUNT; i++) {
    http.get(`${BASE_URL}/api/queue/${SCHEDULE_NO}/status`, params);

    if (i % HEARTBEAT_INTERVAL === 0) {
      http.post(`${BASE_URL}/api/queue/${SCHEDULE_NO}/heartbeat`, null, params);
    }

    sleep(STATUS_SLEEP_SECONDS);
  }
}
