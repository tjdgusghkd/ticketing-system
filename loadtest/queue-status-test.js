import http from 'k6/http';
import { sleep } from 'k6';

export const options = {
	vus: 50,
	duration: '1m',
};

const BASE_URL = 'http://localhost:8080';
const ACCESS_TOKEN = 'eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJnaGtkNTM3MCIsInR5cGUiOiJhY2Nlc3MiLCJpYXQiOjE3NzcwMTg2NDUsImV4cCI6MTc3NzAyMDQ0NX0.Umi3JvL9AQB3FA673U51IhfbdyrFvxIG7usP2OZVvYH7BkVUX0NdzaMkVmEqeMndEDdl2P3wt6NBh4UdYrHEOA';

export default function() {
	const params = {
		headers: {
			'Content-Type' : 'application/json',
			Authorization: `Bearer ${ACCESS_TOKEN}`,
		},
	};
	
	http.get(`${BASE_URL}/api/queue/1/status`,
		params
	);
	sleep(0.1);
}