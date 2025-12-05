function loadLoggedIn() {
    let pageTimeout = document.getElementById("page-timeout");
    let countdownLogout = document.getElementById("countdown-logout");
    let stopIdle = document.getElementById("stop-idle");

    const idleLimit = 14 * 60;
    const topLimit = 15 * 60;
    let idleTime = 0;

    // Increment the idle time counter every second
    const timerIncrement = () => {
        idleTime++;

        // Exceeded limit
        if (idleTime >= topLimit) {
            window.location = "logout";
            return true;
        }

        // Show idle pop up
        if (idleTime >= idleLimit) {
            showIdlePopup();
            countdownLogout.innerText = (topLimit - idleTime).toString();
        }
    };

    // Reset the idle time counter on user activity
    const resetTimer = () => {
        idleTime = 0;
    };

    // Show a warning popup to the user
    const showIdlePopup = () => {
        pageTimeout.classList.remove("hidden");
    };

    // Set up event listener to reset the timer on user activity
    document.body.querySelector('.ds_page').addEventListener('click', resetTimer);

    // Stop idling
    stopIdle.addEventListener("click", function () {
        resetTimer();
        pageTimeout.classList.add("hidden");
        countdownLogout.innerText = "60";
    })

    setInterval(timerIncrement, 1000);
}