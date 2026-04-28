import http from 'k6/http';
import { sleep } from 'k6';

export const options = {
	vus:20,
	duration: '20s',
};

const BASE_URL = 'http://localhost:8080';
const SCHEDULE_NO = 1;
const SCHEDULE_SEAT_NO = 20;
const ACCESS_TOKEN = 'eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJnaGtkNTM3MCIsInR5cGUiOiJhY2Nlc3MiLCJpYXQiOjE3NzcxOTM0MzIsImV4cCI6MTc3NzE5NTIzMn0.VmHnRIk8gHGfJKNexSBjM3UtU6d3PVd7DApZPCeEp5r4ZSd_dzTdUB364k_QSqLCIZE1cVkF5tZKtx4Lb6p49g';

export default function() {
	const params = {
		headers: {
			Authorization : `Bearer ${ACCESS_TOKEN}`,
		},
	};
	
	http.post(
		`${BASE_URL}/api/rounds/${SCHEDULE_NO}/hold/${SCHEDULE_SEAT_NO}`,
		null,
		params
	);
	
	sleep(1);
}