const API_BASE_URL = 'https://yogurtdev.site';

const STORAGE_KEYS = {
    TOKEN: 'yogurt_admin_token',
    EMAIL: 'yogurt_admin_email',
};

const loginScreen = document.getElementById('login-screen');
const requestsScreen = document.getElementById('requests-screen');

const loginForm = document.getElementById('login-form');
const loginBtn = document.getElementById('login-btn');
const loginError = document.getElementById('login-error');

const logoutBtn = document.getElementById('logout-btn');
const refreshBtn = document.getElementById('refresh-btn');
const adminEmailSpan = document.getElementById('admin-email');

const requestsTbody = document.getElementById('requests-tbody');
const requestsTable = document.getElementById('requests-table');
const loadingDiv = document.getElementById('loading');
const emptyState = document.getElementById('empty-state');
const requestsError = document.getElementById('requests-error');

function showScreen(screenName) {
    if(screenName === 'login') {
        loginScreen.classList.remove('hidden');
        requestsScreen.classList.add('hidden');
    } else {
        loginScreen.classList.add('hidden');
        requestsScreen.classList.remove('hidden');
    }
}

function showError(element, message) {
    element.textContent = message;
    element.classList.remove('hidden');
}

function hideError(element) {
    element.classList.add('hidden');
    element.textContent = '';
}


async function apiRequest(path, options = {}) {
    const token = localStorage.getItem(STORAGE_KEYS.TOKEN);

    const headers = {
        'Content-Type': 'application/json',
        ...options.headers,
    };

    if(token) {
        headers['Authorization'] = `Bearer ${token}`;
    }

    const response = await fetch(`${API_BASE_URL}${path}`, {
        ...options,
        headers,
    });

    if(response.status === 401) {
        logout();
        throw new Error('Сессия истекла, войдите снова');
    }

    const contentType = response.headers.get('content-type');
    const isJson = contentType && contentType.includes('application/json');
    const data = isJson ? await response.json() : null;

    if (!response.ok) {
        const errorMessage = (data && data.message) || `Ошибка ${response.status}`;
        throw new Error(errorMessage);
    }

    return data;
}

async function login(email, password) {
    const data = await apiRequest('/api/v1/auth/login', {
        method: 'POST',
        body: JSON.stringify({ email, password }),
    });

    if (data.role !== 'ADMIN') {
        throw new Error('Недостаточно прав. Доступ только для администраторов.');
    }

    localStorage.setItem(STORAGE_KEYS.TOKEN, data.token);
    localStorage.setItem(STORAGE_KEYS.EMAIL, data.email);

    return data;
}

function logout() {
    localStorage.removeItem(STORAGE_KEYS.TOKEN);
    localStorage.removeItem(STORAGE_KEYS.EMAIL);
    showScreen('login');
}

function isLoggedIn() {
    return Boolean(localStorage.getItem(STORAGE_KEYS.TOKEN));
}


async function fetchRequests() {
    return await apiRequest('/api/v1/admin/access-requests');
}

async function approveRequest(id) {
    return await apiRequest(`/api/v1/admin/access-requests/${id}/approve`, {
        method: 'POST',
    });
}

async function rejectRequest(id) {
    return await apiRequest(`/api/v1/admin/access-requests/${id}/reject`, {
        method: 'POST',
    });
}



function formatDate(isoString) {
    const date = new Date(isoString);
    return date.toLocaleString('ru-RU', {
        day: '2-digit',
        month: '2-digit',
        year: 'numeric',
        hour: '2-digit',
        minute: '2-digit',
    });
}

function getStatusBadge(status) {
    const labels = {
        PENDING: 'На рассмотрении',
        APPROVED: 'Одобрено',
        REJECTED: 'Отклонено',
    };
    const cssClass = `status-${status.toLowerCase()}`;
    return `<span class="status-badge ${cssClass}">${labels[status] || status}</span>`;
}

function renderRequests(requests) {
    // Очищаем таблицу
    requestsTbody.innerHTML = '';

    if (requests.length === 0) {
        requestsTable.classList.add('hidden');
        emptyState.classList.remove('hidden');
        return;
    }

    requestsTable.classList.remove('hidden');
    emptyState.classList.add('hidden');

    requests.forEach((request) => {
        const tr = document.createElement('tr');

        const actionsHtml = request.status === 'PENDING'
            ? `
                <button class="btn-approve" data-action="approve" data-id="${request.id}">
                    Одобрить
                </button>
                <button class="btn-reject" data-action="reject" data-id="${request.id}">
                    Отклонить
                </button>
            `
            : '—';

        tr.innerHTML = `
            <td>${escapeHtml(request.userEmail)}</td>
            <td>${formatDate(request.requestedAt)}</td>
            <td>${getStatusBadge(request.status)}</td>
            <td>${actionsHtml}</td>
        `;

        requestsTbody.appendChild(tr);
    });
}

function escapeHtml(str) {
    if (!str) return '';
    const div = document.createElement('div');
    div.textContent = str;
    return div.innerHTML;
}

async function loadAndRenderRequests() {
    hideError(requestsError);
    loadingDiv.classList.remove('hidden');
    requestsTable.classList.add('hidden');
    emptyState.classList.add('hidden');

    try {
        const requests = await fetchRequests();
        renderRequests(requests);
    } catch (error) {
        showError(requestsError, error.message);
    } finally {
        loadingDiv.classList.add('hidden');
    }
}

loginForm.addEventListener('submit', async (event) => {
    event.preventDefault();

    const email = document.getElementById('email').value.trim();
    const password = document.getElementById('password').value;

    hideError(loginError);
    loginBtn.disabled = true;
    loginBtn.textContent = 'Вход...';

    try {
        const data = await login(email, password);
        adminEmailSpan.textContent = data.email;
        showScreen('requests');
        await loadAndRenderRequests();
    } catch (error) {
        showError(loginError, error.message);
    } finally {
        loginBtn.disabled = false;
        loginBtn.textContent = 'Войти';
    }
});


logoutBtn.addEventListener('click', () => {
    logout();
});


refreshBtn.addEventListener('click', () => {
    loadAndRenderRequests();
});

requestsTbody.addEventListener('click', async (event) => {
    const button = event.target.closest('[data-action]');
    if (!button) return;

    const action = button.dataset.action;
    const id = button.dataset.id;

    if (action === 'reject') {
        if (!confirm('Точно отклонить заявку?')) return;
    }

    button.disabled = true;
    button.textContent = '...';

    try {
        if (action === 'approve') {
            await approveRequest(id);
        } else if (action === 'reject') {
            await rejectRequest(id);
        }
        await loadAndRenderRequests();
    } catch (error) {
        alert(`Ошибка: ${error.message}`);
        button.disabled = false;
        button.textContent = action === 'approve' ? 'Одобрить' : 'Отклонить';
    }
});


async function init() {
    if (isLoggedIn()) {
        const email = localStorage.getItem(STORAGE_KEYS.EMAIL);
        adminEmailSpan.textContent = email;
        showScreen('requests');
        await loadAndRenderRequests();
    } else {
        showScreen('login');
    }
}

init();