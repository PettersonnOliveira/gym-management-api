'use strict';

/* =========================================================
   Configuração
   ========================================================= */

// Em produção (Cloudflare Pages) aponte para o serviço no Render.
// Não inclua barra no final.
const RENDER_API_URL = 'https://SEU-SERVICO.onrender.com';

const LOCAL_HOSTNAMES = ['localhost', '127.0.0.1'];

const API_BASE = (
  LOCAL_HOSTNAMES.includes(window.location.hostname)
    ? window.location.origin
    : RENDER_API_URL
).replace(/\/+$/, '');

// O cold start do Render pode levar de 30 a 50 segundos.
const REQUEST_TIMEOUT_MS = 70000;
const COLD_START_BANNER_DELAY_MS = 2500;

const STORAGE_KEYS = {
  token: 'token',
  email: 'userEmail',
  role: 'userRole',
};

/* =========================================================
   Estado e utilitários
   ========================================================= */

const state = {
  members: [],
  workouts: [],
  enrollments: [],
  editingMemberId: null,
  editingWorkoutId: null,
};

const $ = (selector, root = document) => root.querySelector(selector);
const $$ = (selector, root = document) => Array.from(root.querySelectorAll(selector));

class ApiError extends Error {
  constructor(message, status) {
    super(message);
    this.name = 'ApiError';
    this.status = status;
  }
}

/* =========================================================
   Sessão
   ========================================================= */

const getToken = () => localStorage.getItem(STORAGE_KEYS.token);
const getEmail = () => localStorage.getItem(STORAGE_KEYS.email) || '';
const getRole = () => (localStorage.getItem(STORAGE_KEYS.role) || '').toUpperCase();
const isAdmin = () => getRole() === 'ADMIN';

function saveSession({ token, email, role }) {
  localStorage.setItem(STORAGE_KEYS.token, token);
  localStorage.setItem(STORAGE_KEYS.email, email);
  localStorage.setItem(STORAGE_KEYS.role, role);
}

function clearSession() {
  Object.values(STORAGE_KEYS).forEach((key) => localStorage.removeItem(key));
}

/* =========================================================
   Banner de cold start
   ========================================================= */

function showColdStartBanner() {
  const banner = $('#cold-start-banner');
  if (banner) banner.hidden = false;
}

function hideColdStartBanner() {
  const banner = $('#cold-start-banner');
  if (banner) banner.hidden = true;
}

async function pingServerOnStartup() {
  const controller = new AbortController();
  const timeoutId = setTimeout(() => controller.abort(), REQUEST_TIMEOUT_MS);
  const bannerId = setTimeout(showColdStartBanner, COLD_START_BANNER_DELAY_MS);

  try {
    const response = await fetch(`${API_BASE}/health`, {
      method: 'GET',
      headers: { Accept: 'application/json' },
      cache: 'no-store',
      signal: controller.signal,
    });

    if (response.status > 0) {
      hideColdStartBanner();
    }

    return response.ok;
  } catch (error) {
    return false;
  } finally {
    clearTimeout(timeoutId);
    clearTimeout(bannerId);
    hideColdStartBanner();
  }
}

/* =========================================================
   HTTP
   ========================================================= */

async function readErrorMessage(response) {
  try {
    const text = await response.text();
    if (!text) return `Erro ${response.status}.`;

    try {
      const data = JSON.parse(text);
      return data.message || data.error || `Erro ${response.status}.`;
    } catch (error) {
      return text;
    }
  } catch (error) {
    return `Erro ${response.status}.`;
  }
}

async function apiRequest(path, options = {}) {
  const {
    method = 'GET',
    body,
    auth = true,
    skipUnauthorizedRedirect = false,
  } = options;

  const controller = new AbortController();
  const timeoutId = setTimeout(() => controller.abort(), REQUEST_TIMEOUT_MS);

  const headers = { Accept: 'application/json' };
  const token = getToken();

  if (auth && token) headers.Authorization = `Bearer ${token}`;
  if (body !== undefined) headers['Content-Type'] = 'application/json';

  try {
    const response = await fetch(`${API_BASE}${path}`, {
      method,
      headers,
      body: body !== undefined ? JSON.stringify(body) : undefined,
      signal: controller.signal,
    });

    if (response.status === 401) {
      if (!skipUnauthorizedRedirect) handleUnauthorized();
      throw new ApiError('Credenciais inválidas.', 401);
    }

    if (response.status === 403) {
      throw new ApiError('Você não tem permissão para executar esta ação.', 403);
    }

    if (!response.ok) {
      throw new ApiError(await readErrorMessage(response), response.status);
    }

    if (response.status === 204) return null;

    const text = await response.text();
    if (!text) return null;

    try {
      return JSON.parse(text);
    } catch (error) {
      return text;
    }
  } catch (error) {
    if (error.name === 'AbortError') {
      throw new ApiError('O servidor demorou demais para responder. Tente novamente em instantes.', 0);
    }

    if (error instanceof ApiError) throw error;

    throw new ApiError('Não foi possível conectar ao servidor. Verifique sua conexão.', 0);
  } finally {
    clearTimeout(timeoutId);
  }
}

function handleUnauthorized() {
  const hadSession = Boolean(getToken());
  clearSession();
  closeAllDialogs();
  showAuthView();

  if (hadSession) {
    showToast('Sua sessão expirou. Faça login novamente.', 'error');
  }
}

/* =========================================================
   Feedback visual
   ========================================================= */

function showToast(message, type = 'info', duration = 4500) {
  const container = $('#toast-container');
  if (!container) return;

  const toast = document.createElement('div');
  toast.className = `toast ${type}`;
  toast.textContent = message;
  container.appendChild(toast);

  setTimeout(() => {
    toast.style.transition = 'opacity 0.2s ease';
    toast.style.opacity = '0';
    setTimeout(() => toast.remove(), 220);
  }, duration);
}

function setFormError(selector, message) {
  const element = $(selector);
  if (element) element.textContent = message || '';
}

function setButtonBusy(button, busy, busyLabel = 'Aguarde...') {
  if (!button) return;

  if (busy) {
    button.dataset.originalLabel = button.textContent;
    button.textContent = busyLabel;
    button.disabled = true;
  } else {
    button.textContent = button.dataset.originalLabel || button.textContent;
    button.disabled = false;
  }
}

function escapeHtml(value) {
  return String(value ?? '')
    .replace(/&/g, '&amp;')
    .replace(/</g, '&lt;')
    .replace(/>/g, '&gt;')
    .replace(/"/g, '&quot;')
    .replace(/'/g, '&#39;');
}

function formatDate(value) {
  if (!value) return '—';

  const date = new Date(value);
  if (Number.isNaN(date.getTime())) return escapeHtml(value);

  return date.toLocaleString('pt-BR', { dateStyle: 'short', timeStyle: 'short' });
}

/* =========================================================
   Navegação entre telas
   ========================================================= */

function setAuthTab(name) {
  $$('[data-auth-tab]').forEach((tab) => {
    const active = tab.dataset.authTab === name;
    tab.classList.toggle('active', active);
    tab.setAttribute('aria-selected', String(active));
  });

  $('#login-form').hidden = name !== 'login';
  $('#register-form').hidden = name !== 'register';
}

function showAuthView() {
  $('#app-view').hidden = true;
  $('#auth-view').hidden = false;
  setFormError('#login-error', '');
  setFormError('#register-error', '');
  setAuthTab('login');
}

function showAppView() {
  $('#auth-view').hidden = true;
  $('#app-view').hidden = false;
  $('#user-email').textContent = getEmail();
  applyRolePermissions();
  switchSection('dashboard');
}

function switchSection(name) {
  $$('.nav-link').forEach((button) => {
    button.classList.toggle('active', button.dataset.section === name);
  });

  $$('.section').forEach((section) => {
    section.classList.toggle('active', section.id === `section-${name}`);
  });
}

function applyRolePermissions() {
  const admin = isAdmin();

  $$('.admin-only').forEach((element) => {
    element.hidden = !admin;
  });

  const badge = $('#user-role');
  badge.textContent = admin ? 'Administrador' : 'Usuário';
  badge.className = `badge ${admin ? 'badge-admin' : 'badge-user'}`;
}

/* =========================================================
   Autenticação
   ========================================================= */

async function handleLogin(event) {
  event.preventDefault();

  const emailInput = $('#login-email');
  const passwordInput = $('#login-password');

  if (!emailInput.reportValidity() || !passwordInput.reportValidity()) return;

  const submitButton = $('#login-form button[type="submit"]');
  setFormError('#login-error', '');
  setButtonBusy(submitButton, true, 'Entrando...');

  try {
    const data = await apiRequest('/auth/login', {
      method: 'POST',
      auth: false,
      skipUnauthorizedRedirect: true,
      body: {
        email: emailInput.value.trim(),
        password: passwordInput.value,
      },
    });

    saveSession(data);
    $('#login-form').reset();
    showAppView();
    await loadData();
    showToast(`Bem-vindo(a), ${data.email}.`, 'success');
  } catch (error) {
    setFormError(
      '#login-error',
      error.status === 401 ? 'E-mail ou senha inválidos.' : error.message,
    );
  } finally {
    setButtonBusy(submitButton, false);
  }
}

async function handleRegister(event) {
  event.preventDefault();

  const emailInput = $('#register-email');
  const passwordInput = $('#register-password');
  const confirmInput = $('#register-confirm');

  if (
    !emailInput.reportValidity()
    || !passwordInput.reportValidity()
    || !confirmInput.reportValidity()
  ) {
    return;
  }

  if (passwordInput.value !== confirmInput.value) {
    setFormError('#register-error', 'As senhas não coincidem.');
    return;
  }

  const submitButton = $('#register-form button[type="submit"]');
  setFormError('#register-error', '');
  setButtonBusy(submitButton, true, 'Criando...');

  try {
    const data = await apiRequest('/auth/register', {
      method: 'POST',
      auth: false,
      skipUnauthorizedRedirect: true,
      body: {
        email: emailInput.value.trim(),
        password: passwordInput.value,
      },
    });

    saveSession(data);
    $('#register-form').reset();
    showAppView();
    await loadData();
    showToast('Conta criada com sucesso.', 'success');
  } catch (error) {
    setFormError('#register-error', error.message);
  } finally {
    setButtonBusy(submitButton, false);
  }
}

function logout() {
  clearSession();
  closeAllDialogs();
  $('#login-form').reset();
  $('#register-form').reset();
  showAuthView();
  showToast('Você saiu da sua conta.', 'info');
}

/* =========================================================
   Carregamento de dados
   ========================================================= */

function renderLoadingRows() {
  const loadingRow = '<tr><td colspan="4" class="table-loading">Carregando...</td></tr>';

  ['members-body', 'workouts-body', 'enrollments-body', 'recent-enrollments-body'].forEach((id) => {
    const body = document.getElementById(id);
    if (body) body.innerHTML = loadingRow;
  });

  ['members-empty', 'workouts-empty', 'enrollments-empty', 'recent-enrollments-empty'].forEach((id) => {
    const empty = document.getElementById(id);
    if (empty) empty.hidden = true;
  });
}

async function loadData() {
  if (!getToken()) return;

  renderLoadingRows();

  try {
    const [members, workouts, enrollments] = await Promise.all([
      apiRequest('/members'),
      apiRequest('/workouts'),
      apiRequest('/enrollments'),
    ]);

    state.members = Array.isArray(members) ? members : [];
    state.workouts = Array.isArray(workouts) ? workouts : [];
    state.enrollments = Array.isArray(enrollments) ? enrollments : [];

    renderAll();
  } catch (error) {
    if (error.status === 401) return;

    showToast(error.message, 'error');
    renderAll();
  }
}

function renderAll() {
  renderMembers();
  renderWorkouts();
  renderEnrollments();
  renderDashboard();
  populateEnrollmentSelects();
}

/* =========================================================
   Renderização
   ========================================================= */

function renderMembers() {
  const body = $('#members-body');
  const empty = $('#members-empty');

  if (!state.members.length) {
    body.innerHTML = '';
    empty.hidden = false;
    return;
  }

  empty.hidden = true;

  body.innerHTML = state.members.map((member) => `
    <tr>
      <td class="cell-strong">${escapeHtml(member.name)}</td>
      <td>${escapeHtml(member.email)}</td>
      <td class="cell-muted">${escapeHtml(member.phone || '—')}</td>
      <td class="actions">${isAdmin() ? `
        <button type="button" class="btn btn-ghost btn-sm" data-action="edit-member" data-id="${member.id}">Editar</button>
        <button type="button" class="btn btn-danger btn-sm" data-action="delete-member" data-id="${member.id}">Excluir</button>
      ` : '—'}</td>
    </tr>
  `).join('');
}

function renderWorkouts() {
  const body = $('#workouts-body');
  const empty = $('#workouts-empty');

  if (!state.workouts.length) {
    body.innerHTML = '';
    empty.hidden = false;
    return;
  }

  empty.hidden = true;

  body.innerHTML = state.workouts.map((workout) => `
    <tr>
      <td class="cell-strong">${escapeHtml(workout.name)}</td>
      <td class="cell-muted">${escapeHtml(workout.description || '—')}</td>
      <td>${workout.duration ? `${escapeHtml(workout.duration)} min` : '—'}</td>
      <td class="actions">${isAdmin() ? `
        <button type="button" class="btn btn-ghost btn-sm" data-action="edit-workout" data-id="${workout.id}">Editar</button>
        <button type="button" class="btn btn-danger btn-sm" data-action="delete-workout" data-id="${workout.id}">Excluir</button>
      ` : '—'}</td>
    </tr>
  `).join('');
}

function renderEnrollments() {
  const body = $('#enrollments-body');
  const empty = $('#enrollments-empty');

  if (!state.enrollments.length) {
    body.innerHTML = '';
    empty.hidden = false;
    return;
  }

  empty.hidden = true;

  body.innerHTML = state.enrollments.map((enrollment) => `
    <tr>
      <td class="cell-strong">${escapeHtml(enrollment.member?.name || '—')}</td>
      <td>${escapeHtml(enrollment.workout?.name || '—')}</td>
      <td class="cell-muted">${formatDate(enrollment.enrollmentDate)}</td>
      <td class="actions">${isAdmin() ? `
        <button type="button" class="btn btn-danger btn-sm" data-action="delete-enrollment" data-id="${enrollment.id}">Cancelar</button>
      ` : '—'}</td>
    </tr>
  `).join('');
}

function renderDashboard() {
  $('#stat-members').textContent = state.members.length;
  $('#stat-workouts').textContent = state.workouts.length;
  $('#stat-enrollments').textContent = state.enrollments.length;

  const recent = [...state.enrollments]
    .sort((a, b) => new Date(b.enrollmentDate) - new Date(a.enrollmentDate))
    .slice(0, 5);

  const body = $('#recent-enrollments-body');
  const empty = $('#recent-enrollments-empty');

  if (!recent.length) {
    body.innerHTML = '';
    empty.hidden = false;
    return;
  }

  empty.hidden = true;

  body.innerHTML = recent.map((enrollment) => `
    <tr>
      <td class="cell-strong">${escapeHtml(enrollment.member?.name || '—')}</td>
      <td>${escapeHtml(enrollment.workout?.name || '—')}</td>
      <td class="cell-muted">${formatDate(enrollment.enrollmentDate)}</td>
    </tr>
  `).join('');
}

function populateEnrollmentSelects() {
  const memberSelect = $('#enrollment-member');
  const workoutSelect = $('#enrollment-workout');

  const memberValue = memberSelect.value;
  const workoutValue = workoutSelect.value;

  memberSelect.innerHTML = '<option value="">Selecione um aluno</option>'
    + state.members.map((member) => `<option value="${member.id}">${escapeHtml(member.name)}</option>`).join('');

  workoutSelect.innerHTML = '<option value="">Selecione um treino</option>'
    + state.workouts.map((workout) => `<option value="${workout.id}">${escapeHtml(workout.name)}</option>`).join('');

  memberSelect.value = memberValue;
  workoutSelect.value = workoutValue;
}

/* =========================================================
   Modais
   ========================================================= */

function openDialog(id) {
  const dialog = document.getElementById(id);
  if (!dialog) return;

  if (typeof dialog.showModal === 'function') {
    dialog.showModal();
  } else {
    dialog.setAttribute('open', '');
  }
}

function closeDialog(id) {
  const dialog = document.getElementById(id);
  if (!dialog) return;

  if (typeof dialog.close === 'function') {
    dialog.close();
  } else {
    dialog.removeAttribute('open');
  }
}

function closeAllDialogs() {
  $$('dialog[open]').forEach((dialog) => closeDialog(dialog.id));
}

function openMemberDialog(member = null) {
  if (!isAdmin()) return;

  state.editingMemberId = member ? member.id : null;
  $('#member-dialog-title').textContent = member ? 'Editar aluno' : 'Novo aluno';
  $('#member-name').value = member?.name ?? '';
  $('#member-email').value = member?.email ?? '';
  $('#member-phone').value = member?.phone ?? '';
  setFormError('#member-error', '');
  openDialog('member-dialog');
}

function openWorkoutDialog(workout = null) {
  if (!isAdmin()) return;

  state.editingWorkoutId = workout ? workout.id : null;
  $('#workout-dialog-title').textContent = workout ? 'Editar treino' : 'Novo treino';
  $('#workout-name').value = workout?.name ?? '';
  $('#workout-description').value = workout?.description ?? '';
  $('#workout-duration').value = workout?.duration ?? '';
  setFormError('#workout-error', '');
  openDialog('workout-dialog');
}

function openEnrollmentDialog() {
  if (!isAdmin()) return;

  if (!state.members.length || !state.workouts.length) {
    showToast('Cadastre ao menos um aluno e um treino antes de criar uma matrícula.', 'warning');
    return;
  }

  populateEnrollmentSelects();
  setFormError('#enrollment-error', '');
  openDialog('enrollment-dialog');
}

/* =========================================================
   CRUD
   ========================================================= */

async function handleMemberSubmit(event) {
  event.preventDefault();
  if (!isAdmin()) return;

  const form = $('#member-form');
  if (!form.reportValidity()) return;

  const payload = {
    name: $('#member-name').value.trim(),
    email: $('#member-email').value.trim(),
    phone: $('#member-phone').value.trim(),
  };

  const editingId = state.editingMemberId;
  const submitButton = form.querySelector('button[type="submit"]');
  setButtonBusy(submitButton, true, 'Salvando...');

  try {
    if (editingId) {
      await apiRequest(`/members/${editingId}`, { method: 'PUT', body: payload });
    } else {
      await apiRequest('/members', { method: 'POST', body: payload });
    }

    closeDialog('member-dialog');
    showToast(editingId ? 'Aluno atualizado.' : 'Aluno cadastrado.', 'success');
    state.editingMemberId = null;
    await loadData();
  } catch (error) {
    if (error.status !== 401) setFormError('#member-error', error.message);
  } finally {
    setButtonBusy(submitButton, false);
  }
}

async function handleWorkoutSubmit(event) {
  event.preventDefault();
  if (!isAdmin()) return;

  const form = $('#workout-form');
  if (!form.reportValidity()) return;

  const durationValue = $('#workout-duration').value;

  const payload = {
    name: $('#workout-name').value.trim(),
    description: $('#workout-description').value.trim(),
    duration: durationValue ? Number(durationValue) : null,
  };

  const editingId = state.editingWorkoutId;
  const submitButton = form.querySelector('button[type="submit"]');
  setButtonBusy(submitButton, true, 'Salvando...');

  try {
    if (editingId) {
      await apiRequest(`/workouts/${editingId}`, { method: 'PUT', body: payload });
    } else {
      await apiRequest('/workouts', { method: 'POST', body: payload });
    }

    closeDialog('workout-dialog');
    showToast(editingId ? 'Treino atualizado.' : 'Treino cadastrado.', 'success');
    state.editingWorkoutId = null;
    await loadData();
  } catch (error) {
    if (error.status !== 401) setFormError('#workout-error', error.message);
  } finally {
    setButtonBusy(submitButton, false);
  }
}

async function handleEnrollmentSubmit(event) {
  event.preventDefault();
  if (!isAdmin()) return;

  const memberId = $('#enrollment-member').value;
  const workoutId = $('#enrollment-workout').value;

  if (!memberId || !workoutId) {
    setFormError('#enrollment-error', 'Selecione um aluno e um treino.');
    return;
  }

  const form = $('#enrollment-form');
  const submitButton = form.querySelector('button[type="submit"]');
  setButtonBusy(submitButton, true, 'Matriculando...');

  try {
    await apiRequest('/enrollments', {
      method: 'POST',
      body: { memberId: Number(memberId), workoutId: Number(workoutId) },
    });

    closeDialog('enrollment-dialog');
    showToast('Matrícula criada.', 'success');
    await loadData();
  } catch (error) {
    if (error.status !== 401) setFormError('#enrollment-error', error.message);
  } finally {
    setButtonBusy(submitButton, false);
  }
}

async function deleteMember(id) {
  if (!isAdmin()) return;

  const member = state.members.find((item) => String(item.id) === String(id));
  if (!window.confirm(`Excluir o aluno "${member?.name ?? id}"?`)) return;

  try {
    await apiRequest(`/members/${id}`, { method: 'DELETE' });
    showToast('Aluno excluído.', 'success');
    await loadData();
  } catch (error) {
    if (error.status !== 401) showToast(error.message, 'error');
  }
}

async function deleteWorkout(id) {
  if (!isAdmin()) return;

  const workout = state.workouts.find((item) => String(item.id) === String(id));
  if (!window.confirm(`Excluir o treino "${workout?.name ?? id}"?`)) return;

  try {
    await apiRequest(`/workouts/${id}`, { method: 'DELETE' });
    showToast('Treino excluído.', 'success');
    await loadData();
  } catch (error) {
    if (error.status !== 401) showToast(error.message, 'error');
  }
}

async function deleteEnrollment(id) {
  if (!isAdmin()) return;
  if (!window.confirm('Cancelar esta matrícula?')) return;

  try {
    await apiRequest(`/enrollments/${id}`, { method: 'DELETE' });
    showToast('Matrícula cancelada.', 'success');
    await loadData();
  } catch (error) {
    if (error.status !== 401) showToast(error.message, 'error');
  }
}

/* =========================================================
   Eventos
   ========================================================= */

function bindEvents() {
  $$('[data-auth-tab]').forEach((tab) => {
    tab.addEventListener('click', () => setAuthTab(tab.dataset.authTab));
  });

  $('#login-form').addEventListener('submit', handleLogin);
  $('#register-form').addEventListener('submit', handleRegister);
  $('#logout-btn').addEventListener('click', logout);

  $$('.nav-link').forEach((button) => {
    button.addEventListener('click', () => switchSection(button.dataset.section));
  });

  $('#refresh-btn').addEventListener('click', () => loadData());

  $('#new-member-btn').addEventListener('click', () => openMemberDialog());
  $('#new-workout-btn').addEventListener('click', () => openWorkoutDialog());
  $('#new-enrollment-btn').addEventListener('click', openEnrollmentDialog);

  $('#member-form').addEventListener('submit', handleMemberSubmit);
  $('#workout-form').addEventListener('submit', handleWorkoutSubmit);
  $('#enrollment-form').addEventListener('submit', handleEnrollmentSubmit);

  $$('[data-close-dialog]').forEach((button) => {
    button.addEventListener('click', () => closeDialog(button.dataset.closeDialog));
  });

  $$('dialog.modal').forEach((dialog) => {
    dialog.addEventListener('click', (event) => {
      if (event.target === dialog) closeDialog(dialog.id);
    });
  });

  document.addEventListener('click', (event) => {
    const button = event.target.closest('[data-action]');
    if (!button) return;

    const { action, id } = button.dataset;

    switch (action) {
      case 'edit-member':
        openMemberDialog(state.members.find((item) => String(item.id) === String(id)));
        break;
      case 'delete-member':
        deleteMember(id);
        break;
      case 'edit-workout':
        openWorkoutDialog(state.workouts.find((item) => String(item.id) === String(id)));
        break;
      case 'delete-workout':
        deleteWorkout(id);
        break;
      case 'delete-enrollment':
        deleteEnrollment(id);
        break;
      default:
        break;
    }
  });
}

/* =========================================================
   Inicialização
   ========================================================= */

function init() {
  bindEvents();
  pingServerOnStartup();

  if (getToken()) {
    showAppView();
    loadData();
  } else {
    showAuthView();
  }
}

document.addEventListener('DOMContentLoaded', init);
