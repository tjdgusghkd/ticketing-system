import http from 'k6/http';
import { sleep } from 'k6';

export const options = {
	vus: 100,
	duration: '1m',
};

const BASE_URL = 'http://localhost:8080';
const SCHEDULE_NO = 1;
const ACCESS_TOKEN = 'eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJnaGtkNTM3MCIsInR5cGUiOiJhY2Nlc3MiLCJpYXQiOjE3NzcwMjE5MjksImV4cCI6MTc3NzAyMzcyOX0.3UiJiYsSXy0G4KMz89CtMBxnrLZqkX2wig2xfrEwWKLCNmB00a_lJdidCNI7kx469ziIFtpg4vdBBWPiQd3Yuw';

export default function() {
	const params = {
		headers: {
			Authorization: `Bearer ${ACCESS_TOKEN}`,
		},
	};
	
	// 대기열 진입
	const enterRes = http.post(
		`${BASE_URL}/api/queue/${SCHEDULE_NO}/enter`,
		null,
		params
	);
	
	if(enterRes.status !== 200) {
		sleep(1);
		return;
	}
	
	// 대기열 상태 조회 + heartbeat 반복
	for(let i = 1; i <= 10; i++) {
		http.get(`${BASE_URL}/api/queue/${SCHEDULE_NO}/status`,params);
		
		// 5번마다 heartbeat 한번(대략 10초마다 보낸다고 가정)
		if(i % 5 === 0) {
			http.post(`${BASE_URL}/api/queue/${SCHEDULE_NO}/heartbeat`,null,params);
		}
		
		sleep(2);
	}
}