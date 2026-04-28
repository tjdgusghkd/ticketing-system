import http from 'k6/http';
import { sleep } from 'k6';

export const options = {
  vus: Number(__ENV.VUS || 100),
  duration: __ENV.DURATION || '1m',
};

const BASE_URL = __ENV.BASE_URL || 'http://localhost:8080';
const ACCESS_TOKEN = __ENV.ACCESS_TOKEN || 'REPLACE_ME';
const SCHEDULE_NO = __ENV.SCHEDULE_NO || '1';
const SLEEP_SECONDS = Number(__ENV.SLEEP_SECONDS || 10);

export default function () {
  const params = {
    headers: {
      Authorization: `Bearer ${ACCESS_TOKEN}`,
    },
  };

  http.post(`${BASE_URL}/api/queue/${SCHEDULE_NO}/heartbeat`, null, params);
  sleep(SLEEP_SECONDS);
}
