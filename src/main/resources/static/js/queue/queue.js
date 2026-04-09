  // 임시 예시용
  // 실제로는 서버에서 받은 값으로 갱신하거나 polling / SSE / websocket 사용
  let progress = 32;
  const progressFill = document.getElementById("progressFill");
  const progressText = document.getElementById("progressText");

  function updateProgress(value) {
    progressFill.style.width = value + "%";
    progressText.textContent = value + "%";
  }

  updateProgress(progress);

  // 데모용 애니메이션
  setInterval(() => {
    if (progress < 95) {
      progress += 1;
      updateProgress(progress);
    }
  }, 1500);