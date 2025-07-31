document.addEventListener('DOMContentLoaded', () => {
    const chatBox = document.getElementById('chat-box');
    const userInput = document.getElementById('user-input');
    // REMOVED reference to normal-send-button
    const ranchoSendButton = document.getElementById('rancho-send-button');
    const authContainer = document.getElementById('auth-container');
    const notificationList = document.getElementById('notification-list');

    const converter = new showdown.Converter({ literalMidWordUnderscores: true });

    function connectToNotifications() {
        const eventSource = new EventSource('/api/notifications');
        eventSource.onmessage = function(event) {
            const notifications = JSON.parse(event.data);
            notificationList.innerHTML = '';
            notifications.forEach(notificationText => {
                const notificationItem = document.createElement('div');
                notificationItem.className = 'notification-item';
                notificationItem.textContent = notificationText;
                notificationList.append(notificationItem);
            });
        };
        eventSource.onerror = function(err) {
            console.error("EventSource failed:", err);
            eventSource.close();
        };
    }

    async function checkAuthStatus() {
        try {
            const response = await fetch('/api/auth/status');
            const data = await response.json();
            if (data.isAuthenticated) {
                authContainer.innerHTML = '<a href="/logout">Logout</a>';
                connectToNotifications();
            } else {
                authContainer.innerHTML = '<a href="/login">Login</a>';
            }
        } catch (error) {
            console.error("Auth check failed:", error);
            authContainer.innerHTML = '<a href="/login">Login</a>';
        }
    }

    function addMessage(sender, message) {
        const messageDiv = document.createElement('div');
        messageDiv.classList.add('message', sender === 'user' ? 'user-message' : 'bot-message');
        if (sender === 'bot') {
            messageDiv.innerHTML = converter.makeHtml(message);
        } else {
            messageDiv.textContent = message;
        }
        chatBox.appendChild(messageDiv);
        chatBox.scrollTop = chatBox.scrollHeight;
    }

    async function sendMessage() {
        const message = userInput.value.trim();
        if (message) {
            addMessage('user', message);
            userInput.value = '';
            try {
                // Always send to the single rancho-chat endpoint
                const response = await fetch('/api/rancho-chat', {
                    method: 'POST',
                    headers: { 'Content-Type': 'application/json' },
                    body: JSON.stringify({ message: message })
                });
                if (!response.ok) throw new Error(`HTTP error! status: ${response.status}`);
                const data = await response.json();
                addMessage('bot', data.response);
            } catch (error) {
                console.error('Error sending message:', error);
                addMessage('bot', 'Sorry, an error occurred.');
            }
        }
    }

    // REMOVED event listener for normal-send-button
    ranchoSendButton.addEventListener('click', sendMessage);

    userInput.addEventListener('keypress', (event) => {
        if (event.key === 'Enter') {
            sendMessage();
        }
    });

    checkAuthStatus();
});