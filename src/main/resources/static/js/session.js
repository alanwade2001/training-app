document.addEventListener("DOMContentLoaded", () => {

    const timer = document.getElementById("session-timer");

    if (!timer) {
        return;
    }

    const display = document.getElementById("timer-display");

    const state = timer.dataset.state;

    const startedAt = new Date(
        timer.dataset.startedAt
    ).getTime();

    const recoverySeconds = Number(
        timer.dataset.recoverySeconds
    );

    function updateTimer() {

        const now = Date.now();

        const elapsedSeconds = Math.max(
            0,
            Math.floor((now - startedAt) / 1000)
        );

        if (state === "ACTIVE") {

            // Exercise timer counts UP
            display.textContent =
                formatTime(elapsedSeconds);

        } else if (state === "RESTING") {

            // Rest timer counts DOWN
            const remainingSeconds = Math.max(
                0,
                recoverySeconds - elapsedSeconds
            );

            display.textContent =
                formatTime(remainingSeconds);
        }
    }

    function formatTime(totalSeconds) {

        const minutes = Math.floor(
            totalSeconds / 60
        );

        const seconds = totalSeconds % 60;

        return String(minutes).padStart(2, "0")
            + ":"
            + String(seconds).padStart(2, "0");
    }

    // Display correct value immediately
    updateTimer();

    // Refresh display every second
    setInterval(updateTimer, 1000);
});