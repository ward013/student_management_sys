const state = {
  currentUser: null,
  users: [],
  students: [],
  teachers: [],
  currentProfile: null
};

const authView = document.getElementById("authView");
const appView = document.getElementById("appView");
const topbarActions = document.getElementById("topbarActions");

document.addEventListener("DOMContentLoaded", init);

async function init() {
  renderAuthView();
  await refreshSession();
}

async function fetchJson(url, options = {}) {
  const response = await fetch(url, {
    headers: {
      "Content-Type": "application/json",
      ...(options.headers || {})
    },
    credentials: "same-origin",
    ...options
  });

  let payload = null;
  try {
    payload = await response.json();
  } catch (error) {
    payload = null;
  }

  if (!response.ok) {
    throw new Error(payload?.message || `请求失败: ${response.status}`);
  }

  if (payload?.code && payload.code !== 200) {
    throw new Error(payload.message || "操作失败");
  }

  return payload;
}

async function refreshSession() {
  try {
    const response = await fetchJson("/auth/me", { method: "GET" });
    state.currentUser = response.data;
    renderDashboard();
    if (state.currentUser.role === "ADMIN") {
      await loadAdminData();
    } else {
      await loadUserProfile();
    }
  } catch (error) {
    state.currentUser = null;
    renderAuthView();
  }
}

function renderAuthView() {
  authView.classList.remove("hidden");
  appView.classList.add("hidden");
  topbarActions.innerHTML = `<span class="badge">未登录</span>`;
  authView.innerHTML = `
    <section class="panel section-tint auth-shell">
      <div>
        <p class="section-label">登录系统</p>
        <h3>进入校园身份控制台</h3>
        <p class="hint">适合管理员、学生和老师使用的统一入口。登录后会按角色切换视图和权限范围。</p>
      </div>
      <form id="loginForm">
        <label>用户名
          <input name="username" placeholder="请输入用户名" required>
        </label>
        <label>密码
          <input type="password" name="password" placeholder="请输入密码" required>
        </label>
        <div class="button-row">
          <button class="primary" type="submit">登录</button>
          <button class="ghost" type="button" id="fillAdminBtn">填入管理员账号</button>
        </div>
        <div class="status" id="loginStatus"></div>
      </form>
      <div class="access-grid">
        <div class="access-card">
          <h4>管理员</h4>
          <p>查看所有用户、绑定状态、学生信息，并可维护账号角色和学生资料。</p>
        </div>
        <div class="access-card">
          <h4>普通用户</h4>
          <p>注册后默认为普通角色，绑定学生或老师工号后，才能访问自己的身份资料。</p>
        </div>
      </div>
    </section>
    <section class="panel auth-shell">
      <div>
        <p class="section-label">注册普通用户</p>
        <h3>先创建账号，再绑定身份</h3>
        <p class="hint">适合学生或老师先建立登录账号，随后用工号完成身份关联。</p>
      </div>
      <form id="registerForm">
        <label>用户名
          <input name="username" placeholder="例如 student01" required>
        </label>
        <label>密码
          <input type="password" name="password" placeholder="至少 6 位" required>
        </label>
        <div class="button-row">
          <button class="primary" type="submit">注册</button>
        </div>
        <div class="status" id="registerStatus"></div>
      </form>
      <div class="roster-grid">
        <div class="roster-card">
          <h4>学生工号样例</h4>
          <p>1 张三 · 2 李四 · 3 王五</p>
        </div>
        <div class="roster-card">
          <h4>老师工号样例</h4>
          <p>1001 陈老师 · 1002 周老师</p>
        </div>
      </div>
    </section>
  `;

  document.getElementById("loginForm").addEventListener("submit", onLoginSubmit);
  document.getElementById("registerForm").addEventListener("submit", onRegisterSubmit);
  document.getElementById("fillAdminBtn").addEventListener("click", () => {
    const form = document.getElementById("loginForm");
    form.username.value = "admin";
    form.password.value = "admin123";
  });
}

function renderDashboard() {
  authView.classList.add("hidden");
  appView.classList.remove("hidden");
  topbarActions.innerHTML = `
    <span class="badge">${state.currentUser.role === "ADMIN" ? "管理员" : "普通用户"}</span>
    <span class="badge warm">${escapeHtml(state.currentUser.username)}</span>
    <button class="ghost" id="refreshBtn" type="button">刷新数据</button>
    <button class="secondary" id="logoutBtn" type="button">退出登录</button>
  `;

  document.getElementById("logoutBtn").addEventListener("click", onLogout);
  document.getElementById("refreshBtn").addEventListener("click", async () => {
    if (state.currentUser.role === "ADMIN") {
      await loadAdminData();
    } else {
      await loadUserProfile();
    }
  });

  const userInfo = `
    <section class="section section-tint">
      <div class="section-header">
        <div>
          <p class="section-label">当前账号</p>
          <h3>${escapeHtml(state.currentUser.username)}</h3>
        </div>
        <span class="badge">${escapeHtml(state.currentUser.role)}</span>
      </div>
      <div class="meta-list">
        <div class="meta-item">
          <span class="meta-key">账号ID</span>
          <span class="meta-value">${state.currentUser.id}</span>
        </div>
        <div class="meta-item">
          <span class="meta-key">绑定身份类型</span>
          <span class="meta-value">${escapeHtml(state.currentUser.identityType || "NONE")}</span>
        </div>
        <div class="meta-item">
          <span class="meta-key">绑定工号</span>
          <span class="meta-value">${state.currentUser.identityId ?? "未绑定"}</span>
        </div>
      </div>
    </section>
  `;

  if (state.currentUser.role === "ADMIN") {
    appView.innerHTML = `
      <section class="summary-strip">
        ${renderSummaryCard("用户总数", state.users.length, "当前系统里的登录账号", "warm")}
        ${renderSummaryCard("学生总数", state.students.length, "可绑定学生身份的记录")}
        ${renderSummaryCard("老师总数", state.teachers.length, "可绑定老师身份的记录")}
        ${renderSummaryCard("待绑定账号", countUnboundUsers(), "还没有绑定身份的普通用户")}
      </section>
      <section class="overview-grid">
        ${userInfo}
        <section class="section section-tint">
          <p class="section-label">控制台摘要</p>
          <h3>先处理身份，再处理资料</h3>
          <p class="hint">这套面板把账号、身份绑定和学生档案拆开显示，方便先排查账号权限，再定位具体资料记录。</p>
          <div class="control-note">建议流程：先在“所有用户”里确认账号角色和绑定状态，再到学生信息表里维护具体资料。</div>
        </section>
      </section>
      <section class="dashboard-grid">
        <section class="section">
          <div class="section-header">
            <div>
              <p class="section-label">用户管理</p>
              <h3>所有用户</h3>
            </div>
          </div>
          <div id="userTableWrap"></div>
        </section>
        <section class="section">
          <div class="section-header">
            <div>
              <p class="section-label">编辑用户</p>
              <h3>更新账号角色与绑定</h3>
            </div>
          </div>
          <form id="userEditForm" class="stack">
            <input type="hidden" name="id">
            <label>用户名
              <input name="username" placeholder="请选择一个用户">
            </label>
            <div class="row">
              <label>角色
                <select name="role">
                  <option value="USER">USER</option>
                  <option value="ADMIN">ADMIN</option>
                </select>
              </label>
              <label>身份类型
                <select name="identityType">
                  <option value="NONE">NONE</option>
                  <option value="STUDENT">STUDENT</option>
                  <option value="TEACHER">TEACHER</option>
                </select>
              </label>
            </div>
            <div class="row">
              <label>绑定工号
                <input name="identityId" type="number" placeholder="学生工号或老师工号">
              </label>
              <label>重置密码
                <input name="password" type="password" placeholder="留空则不修改">
              </label>
            </div>
            <div class="button-row">
              <button class="primary" type="submit">保存用户修改</button>
            </div>
            <div class="status" id="userEditStatus"></div>
          </form>
        </section>
      </section>
      <section class="dashboard-grid">
        <section class="section">
          <div class="section-header">
            <div>
              <p class="section-label">学生管理</p>
              <h3>学生信息表</h3>
            </div>
          </div>
          <div id="studentTableWrap"></div>
        </section>
        <section class="section">
          <div class="section-header">
            <div>
              <p class="section-label">新增或修改学生</p>
              <h3>学生维护表单</h3>
            </div>
          </div>
          <form id="studentForm" class="stack">
            <label>学生工号
              <input name="id" type="number" placeholder="例如 4" required>
            </label>
            <div class="row">
              <label>姓名
                <input name="name" placeholder="请输入学生姓名" required>
              </label>
              <label>年龄
                <input name="age" type="number" placeholder="请输入年龄" required>
              </label>
            </div>
            <label>手机号
              <input name="phone" placeholder="请输入手机号" required>
            </label>
            <div class="button-row">
              <button class="primary" type="submit" data-mode="create">新增学生</button>
              <button class="secondary" type="button" id="updateStudentBtn">更新当前学生</button>
              <button class="ghost" type="button" id="resetStudentFormBtn">清空表单</button>
            </div>
            <div class="status" id="studentStatus"></div>
          </form>
        </section>
      </section>
      <section class="section">
        <div class="section-header">
          <div>
            <p class="section-label">老师工号参考</p>
            <h3>绑定老师身份时可使用</h3>
          </div>
        </div>
        <div id="teacherTableWrap"></div>
      </section>
    `;

    document.getElementById("userEditForm").addEventListener("submit", onUserEditSubmit);
    document.getElementById("studentForm").addEventListener("submit", onStudentCreateSubmit);
    document.getElementById("updateStudentBtn").addEventListener("click", onStudentUpdateSubmit);
    document.getElementById("resetStudentFormBtn").addEventListener("click", resetStudentForm);
    return;
  }

  appView.innerHTML = `
    <section class="summary-strip">
      ${renderSummaryCard("账号角色", escapeHtml(state.currentUser.role), "当前登录视角", "warm")}
      ${renderSummaryCard("身份类型", escapeHtml(state.currentUser.identityType || "NONE"), "决定可访问的数据范围")}
      ${renderSummaryCard("绑定工号", state.currentUser.identityId ?? "未绑定", "学生或老师工号")}
      ${renderSummaryCard("访问范围", state.currentUser.identityType === "STUDENT" ? "本人学生资料" : state.currentUser.identityType === "TEACHER" ? "本人老师资料" : "需先绑定", "普通用户不能查看别人信息")}
    </section>
    <section class="overview-grid">
      ${userInfo}
      <section class="section section-tint" id="identityPanel"></section>
    </section>
    <section class="section" id="profilePanel"></section>
  `;
}

function renderSummaryCard(label, value, caption, tone = "") {
  const toneClass = tone ? ` ${tone}` : "";
  return `
    <section class="summary-card${toneClass}">
      <p class="section-label">${label}</p>
      <div class="summary-value">${value}</div>
      <div class="summary-caption">${caption}</div>
    </section>
  `;
}

function countUnboundUsers() {
  return state.users.filter(user => (user.role || "").toUpperCase() !== "ADMIN" && (user.identityType || "NONE") === "NONE").length;
}

async function loadAdminData() {
  const [usersRes, studentsRes, teachersRes] = await Promise.all([
    fetchJson("/users"),
    fetchJson("/students"),
    fetchJson("/teachers")
  ]);
  state.users = usersRes.data;
  state.students = studentsRes.data;
  state.teachers = teachersRes.data;
  renderUsersTable();
  renderStudentsTable();
  renderTeachersTable();
}

async function loadUserProfile() {
  renderUserPanelsLoading();
  if (state.currentUser.identityType === "STUDENT") {
    const response = await fetchJson("/students/me");
    state.currentProfile = response.data;
  } else if (state.currentUser.identityType === "TEACHER") {
    const response = await fetchJson("/teachers/me");
    state.currentProfile = response.data;
  } else {
    state.currentProfile = null;
  }
  renderUserPanels();
}

function renderUserPanelsLoading() {
  const identityPanel = document.getElementById("identityPanel");
  const profilePanel = document.getElementById("profilePanel");
  if (!identityPanel || !profilePanel) {
    return;
  }
  identityPanel.innerHTML = `<p class="section-label">身份状态</p><h3>正在加载</h3><p class="hint">请稍候，我们正在读取你的绑定信息。</p>`;
  profilePanel.innerHTML = `<p class="empty-state">正在加载个人资料...</p>`;
}

function renderUserPanels() {
  const identityPanel = document.getElementById("identityPanel");
  const profilePanel = document.getElementById("profilePanel");
  if (!identityPanel || !profilePanel) {
    return;
  }

  if (state.currentUser.identityType === "NONE") {
    identityPanel.innerHTML = `
      <p class="section-label">身份绑定</p>
      <h3>当前账号还没有绑定工号</h3>
      <p class="hint">输入学生工号或老师工号完成绑定。学生示例工号：1、2、3；老师示例工号：1001、1002。</p>
      <form id="bindForm">
        <div class="row">
          <label>身份类型
            <select name="identityType">
              <option value="STUDENT">STUDENT</option>
              <option value="TEACHER">TEACHER</option>
            </select>
          </label>
          <label>工号
            <input name="identityId" type="number" placeholder="请输入工号" required>
          </label>
        </div>
        <div class="button-row">
          <button class="primary" type="submit">绑定身份</button>
        </div>
        <div class="status" id="bindStatus"></div>
      </form>
    `;
    profilePanel.innerHTML = `<p class="empty-state">绑定成功后，这里会显示你的个人信息。</p>`;
    document.getElementById("bindForm").addEventListener("submit", onBindSubmit);
    return;
  }

  identityPanel.innerHTML = `
    <p class="section-label">身份状态</p>
    <h3>已绑定 ${escapeHtml(state.currentUser.identityType)}</h3>
    <p class="hint">当前绑定工号：${state.currentUser.identityId}</p>
  `;

  if (state.currentUser.identityType === "STUDENT") {
    profilePanel.innerHTML = renderProfileCard("学生个人资料", [
      ["学生工号", state.currentProfile.id],
      ["姓名", state.currentProfile.name],
      ["年龄", state.currentProfile.age],
      ["手机号", state.currentProfile.phone]
    ]);
    return;
  }

  profilePanel.innerHTML = renderProfileCard("老师个人资料", [
    ["老师工号", state.currentProfile.id],
    ["姓名", state.currentProfile.name],
    ["职称", state.currentProfile.title],
    ["手机号", state.currentProfile.phone]
  ]);
}

function renderProfileCard(title, fields) {
  return `
    <p class="section-label">个人信息</p>
    <h3>${title}</h3>
    <div class="meta-list">
      ${fields.map(([key, value]) => `
        <div class="meta-item">
          <span class="meta-key">${escapeHtml(String(key))}</span>
          <span class="meta-value">${escapeHtml(String(value ?? ""))}</span>
        </div>
      `).join("")}
    </div>
  `;
}

function renderUsersTable() {
  const wrap = document.getElementById("userTableWrap");
  if (!wrap) {
    return;
  }
  if (!state.users.length) {
    wrap.innerHTML = `<p class="empty-state">暂无用户数据。</p>`;
    return;
  }

  wrap.innerHTML = `
    <table>
      <thead>
        <tr>
          <th>ID</th>
          <th>用户名</th>
          <th>角色</th>
          <th>身份类型</th>
          <th>绑定工号</th>
          <th>操作</th>
        </tr>
      </thead>
      <tbody>
        ${state.users.map(user => `
          <tr>
            <td>${user.id}</td>
            <td>${escapeHtml(user.username)}</td>
            <td>${escapeHtml(user.role)}</td>
            <td>${escapeHtml(user.identityType || "NONE")}</td>
            <td>${user.identityId ?? "-"}</td>
            <td>
              <div class="table-actions">
                <button class="ghost" type="button" data-user-id="${user.id}">编辑</button>
              </div>
            </td>
          </tr>
        `).join("")}
      </tbody>
    </table>
  `;

  wrap.querySelectorAll("[data-user-id]").forEach(button => {
    button.addEventListener("click", () => fillUserForm(Number(button.dataset.userId)));
  });
}

function renderStudentsTable() {
  const wrap = document.getElementById("studentTableWrap");
  if (!wrap) {
    return;
  }
  if (!state.students.length) {
    wrap.innerHTML = `<p class="empty-state">暂无学生数据。</p>`;
    return;
  }

  wrap.innerHTML = `
    <table>
      <thead>
        <tr>
          <th>工号</th>
          <th>姓名</th>
          <th>年龄</th>
          <th>手机号</th>
          <th>操作</th>
        </tr>
      </thead>
      <tbody>
        ${state.students.map(student => `
          <tr>
            <td>${student.id}</td>
            <td>${escapeHtml(student.name)}</td>
            <td>${student.age}</td>
            <td>${escapeHtml(student.phone)}</td>
            <td>
              <div class="table-actions">
                <button class="ghost" type="button" data-student-edit="${student.id}">填入表单</button>
                <button class="danger" type="button" data-student-delete="${student.id}">删除</button>
              </div>
            </td>
          </tr>
        `).join("")}
      </tbody>
    </table>
  `;

  wrap.querySelectorAll("[data-student-edit]").forEach(button => {
    button.addEventListener("click", () => fillStudentForm(Number(button.dataset.studentEdit)));
  });
  wrap.querySelectorAll("[data-student-delete]").forEach(button => {
    button.addEventListener("click", () => onDeleteStudent(Number(button.dataset.studentDelete)));
  });
}

function renderTeachersTable() {
  const wrap = document.getElementById("teacherTableWrap");
  if (!wrap) {
    return;
  }
  if (!state.teachers.length) {
    wrap.innerHTML = `<p class="empty-state">暂无老师数据。</p>`;
    return;
  }

  wrap.innerHTML = `
    <table>
      <thead>
        <tr>
          <th>工号</th>
          <th>姓名</th>
          <th>职称</th>
          <th>手机号</th>
        </tr>
      </thead>
      <tbody>
        ${state.teachers.map(teacher => `
          <tr>
            <td>${teacher.id}</td>
            <td>${escapeHtml(teacher.name)}</td>
            <td>${escapeHtml(teacher.title)}</td>
            <td>${escapeHtml(teacher.phone)}</td>
          </tr>
        `).join("")}
      </tbody>
    </table>
  `;
}

function fillUserForm(userId) {
  const user = state.users.find(item => item.id === userId);
  if (!user) {
    return;
  }
  const form = document.getElementById("userEditForm");
  form.elements["id"].value = user.id;
  form.elements["username"].value = user.username;
  form.elements["role"].value = user.role;
  form.elements["identityType"].value = user.identityType || "NONE";
  form.elements["identityId"].value = user.identityId ?? "";
  form.elements["password"].value = "";
  setStatus("userEditStatus", `正在编辑用户 ${user.username}`, "success");
}

function fillStudentForm(studentId) {
  const student = state.students.find(item => item.id === studentId);
  if (!student) {
    return;
  }
  const form = document.getElementById("studentForm");
  form.elements["id"].value = student.id;
  form.elements["name"].value = student.name;
  form.elements["age"].value = student.age;
  form.elements["phone"].value = student.phone;
  setStatus("studentStatus", `已载入学生 ${student.name}，可以更新。`, "success");
}

function resetStudentForm() {
  const form = document.getElementById("studentForm");
  form.reset();
  setStatus("studentStatus", "", "");
}

async function onLoginSubmit(event) {
  event.preventDefault();
  const form = event.currentTarget;
  setStatus("loginStatus", "正在登录...", "success");

  try {
    await fetchJson("/auth/login", {
      method: "POST",
      body: JSON.stringify({
        username: form.username.value.trim(),
        password: form.password.value
      })
    });
    await refreshSession();
  } catch (error) {
    setStatus("loginStatus", error.message, "error");
  }
}

async function onRegisterSubmit(event) {
  event.preventDefault();
  const form = event.currentTarget;
  setStatus("registerStatus", "正在注册...", "success");

  try {
    await fetchJson("/auth/register", {
      method: "POST",
      body: JSON.stringify({
        username: form.username.value.trim(),
        password: form.password.value
      })
    });
    setStatus("registerStatus", "注册成功，现在可以直接登录。", "success");
    form.reset();
  } catch (error) {
    setStatus("registerStatus", error.message, "error");
  }
}

async function onLogout() {
  await fetchJson("/auth/logout", { method: "POST" });
  state.currentUser = null;
  renderAuthView();
}

async function onBindSubmit(event) {
  event.preventDefault();
  const form = event.currentTarget;
  setStatus("bindStatus", "正在绑定身份...", "success");

  try {
    await fetchJson("/auth/bind", {
      method: "POST",
      body: JSON.stringify({
        identityType: form.identityType.value,
        identityId: Number(form.identityId.value)
      })
    });
    await refreshSession();
  } catch (error) {
    setStatus("bindStatus", error.message, "error");
  }
}

async function onUserEditSubmit(event) {
  event.preventDefault();
  const form = event.currentTarget;
  if (!form.elements["id"].value) {
    setStatus("userEditStatus", "请先从左侧选择一个用户。", "error");
    return;
  }

  const payload = {
    username: form.elements["username"].value.trim(),
    role: form.elements["role"].value,
    identityType: form.elements["identityType"].value,
    identityId: form.elements["identityId"].value ? Number(form.elements["identityId"].value) : null
  };

  if (form.elements["password"].value.trim()) {
    payload.password = form.elements["password"].value.trim();
  }

  try {
    await fetchJson(`/users/${form.elements["id"].value}`, {
      method: "PUT",
      body: JSON.stringify(payload)
    });
    setStatus("userEditStatus", "用户信息已更新。", "success");
    await loadAdminData();
  } catch (error) {
    setStatus("userEditStatus", error.message, "error");
  }
}

async function onStudentCreateSubmit(event) {
  event.preventDefault();
  const form = event.currentTarget;
  const payload = readStudentForm(form);

  try {
    await fetchJson("/students", {
      method: "POST",
      body: JSON.stringify(payload)
    });
    setStatus("studentStatus", "学生新增成功。", "success");
    resetStudentForm();
    await loadAdminData();
  } catch (error) {
    setStatus("studentStatus", error.message, "error");
  }
}

async function onStudentUpdateSubmit() {
  const form = document.getElementById("studentForm");
  const payload = readStudentForm(form);
  if (!payload.id) {
    setStatus("studentStatus", "请先输入或载入学生工号。", "error");
    return;
  }

  try {
    await fetchJson(`/students/${payload.id}`, {
      method: "PUT",
      body: JSON.stringify(payload)
    });
    setStatus("studentStatus", "学生信息更新成功。", "success");
    await loadAdminData();
  } catch (error) {
    setStatus("studentStatus", error.message, "error");
  }
}

async function onDeleteStudent(studentId) {
  if (!window.confirm(`确认删除学生工号 ${studentId} 吗？`)) {
    return;
  }

  try {
    await fetchJson(`/students/${studentId}`, { method: "DELETE" });
    await loadAdminData();
  } catch (error) {
    alert(error.message);
  }
}

function readStudentForm(form) {
  return {
    id: Number(form.elements["id"].value),
    name: form.elements["name"].value.trim(),
    age: Number(form.elements["age"].value),
    phone: form.elements["phone"].value.trim()
  };
}

function setStatus(id, message, type) {
  const element = document.getElementById(id);
  if (!element) {
    return;
  }
  element.textContent = message;
  element.className = `status ${type || ""}`.trim();
}

function escapeHtml(value) {
  return value
    .replaceAll("&", "&amp;")
    .replaceAll("<", "&lt;")
    .replaceAll(">", "&gt;")
    .replaceAll('"', "&quot;")
    .replaceAll("'", "&#39;");
}
