// 前端状态中心：
// 这里统一保存当前登录用户、管理员列表数据和普通用户的个人资料。
// 这样各个渲染函数只从 state 取数据，不需要彼此传很多参数。
const state = {
  currentUser: null,
  users: [],
  students: [],
  teachers: [],
  currentProfile: null,
  scores: [],
  visibleStudents: [],
  visibleScores: []
};

// 先缓存几个顶层容器，后面切换登录页 / 控制台时会反复用到。
const authView = document.getElementById("authView");
const appView = document.getElementById("appView");
const topbarActions = document.getElementById("topbarActions");

// 等页面结构加载完成后，再去挂事件和拉取登录态。
document.addEventListener("DOMContentLoaded", init);

// 页面启动入口：先渲染登录页，再尝试恢复已有 Session。
async function init() {
  renderAuthView();
  await refreshSession();
}

// 统一封装 fetch：
// 1. 默认用 JSON 和后端通信
// 2. 自动携带同源 cookie，维持 Session 登录态
// 3. 把后端统一返回的 Result<T> 解包并转换成前端可读的异常
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

// 刷新当前登录态。
// 如果 Session 还有效，就直接进入对应角色的面板；否则回到登录注册页。
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

// 渲染未登录状态下的页面。
// 这里只负责输出 HTML 和绑定登录/注册事件，不做业务请求。
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

// 根据当前用户角色渲染管理员视图或普通用户视图。
// 管理员看到的是完整的管理台，普通用户看到的是自己的身份与资料。
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
      <section class="summary-strip" id="adminSummaryStrip">
        ${renderSummaryCard("用户总数", state.users.length, "当前系统里的登录账号", "warm")}
        ${renderSummaryCard("学生总数", state.students.length, "可绑定学生身份的记录")}
        ${renderSummaryCard("老师总数", state.teachers.length, "可绑定老师身份的记录")}
        ${renderSummaryCard("成绩总数", state.scores.length, "当前成绩记录条数")}
        ${renderSummaryCard("待绑定账号", countUnboundUsers(), "还没有绑定身份的普通用户")}
      </section>
      <section class="overview-grid">
        ${userInfo}
        <section class="section">
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
              <p class="section-label">数据列表</p>
              <h3>用户列表</h3>
            </div>
          </div>
          <div id="userTableWrap"></div>
        </section>
        <section class="section">
          <div class="section-header">
            <div>
              <p class="section-label">编辑窗口</p>
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
              <p class="section-label">数据列表</p>
              <h3>学生信息表</h3>
            </div>
          </div>
          <div id="studentTableWrap"></div>
        </section>
        <section class="section">
          <div class="section-header">
            <div>
              <p class="section-label">筛选搜索</p>
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
      <section class="dashboard-grid">
        <section class="section">
          <div class="section-header">
            <div>
              <p class="section-label">数据列表</p>
              <h3>老师信息表</h3>
            </div>
          </div>
          <div id="teacherTableWrap"></div>
        </section>
        <section class="section">
          <div class="section-header">
            <div>
              <p class="section-label">录入窗口</p>
              <h3>老师维护表单</h3>
            </div>
          </div>
          <form id="teacherForm" class="stack">
            <label>老师工号
              <input name="id" type="number" placeholder="例如 1003" required>
            </label>
            <div class="row">
              <label>姓名
                <input name="name" placeholder="请输入老师姓名" required>
              </label>
              <label>职称
                <input name="title" placeholder="例如 讲师 / 班主任" required>
              </label>
            </div>
            <label>手机号
              <input name="phone" placeholder="请输入手机号" required>
            </label>
            <div class="button-row">
              <button class="primary" type="submit">新增老师</button>
              <button class="secondary" type="button" id="updateTeacherBtn">更新当前教师</button>
              <button class="ghost" type="button" id="resetTeacherFormBtn">清空表单</button>
            </div>
            <div class="status" id="teacherStatus"></div>
          </form>
        </section>
      </section>
      <section class="dashboard-grid">
        <section class="section">
          <div class="section-header">
            <div>
              <p class="section-label">数据列表</p>
              <h3>学生成绩表</h3>
            </div>
          </div>
          <div id="scoreTableWrap"></div>
        </section>
        <section class="section">
          <div class="section-header">
            <div>
              <p class="section-label">成绩维护</p>
              <h3>成绩维护表单</h3>
            </div>
          </div>
          <form id="scoreForm" class="stack">
            <input name="id" type="hidden">
            <label>学生工号
              <input name="studentId" type="number" placeholder="例如 1" required>
            </label>
            <div class="row">
              <label>课程名称
                <input name="courseName" placeholder="例如 高等数学" required>
              </label>
              <label>成绩
                <input name="score" type="number" step="0.01" min="0" max="100" placeholder="0 - 100" required>
              </label>
            </div>
            <label>学期
              <input name="semester" placeholder="例如 2026-春" required>
            </label>
            <div class="button-row">
              <button class="primary" type="submit">新增成绩</button>
              <button class="secondary" type="button" id="updateScoreBtn">更新当前成绩</button>
              <button class="ghost" type="button" id="resetScoreFormBtn">清空表单</button>
            </div>
            <div class="status" id="scoreStatus"></div>
          </form>
        </section>
      </section>
    `;

    // 管理员面板里的表单和按钮，都在对应区域渲染完成后统一绑定事件。
    document.getElementById("userEditForm").addEventListener("submit", onUserEditSubmit);
    document.getElementById("studentForm").addEventListener("submit", onStudentCreateSubmit);
    document.getElementById("updateStudentBtn").addEventListener("click", onStudentUpdateSubmit);
    document.getElementById("resetStudentFormBtn").addEventListener("click", resetStudentForm);
    document.getElementById("teacherForm").addEventListener("submit", onTeacherCreateSubmit);
    document.getElementById("updateTeacherBtn").addEventListener("click",onTeacherUpadateSubmit);
    document.getElementById("resetTeacherFormBtn").addEventListener("click", resetTeacherForm);
    document.getElementById("scoreForm").addEventListener("submit", onScoreCreateSubmit);
    document.getElementById("updateScoreBtn").addEventListener("click", onScoreUpdateSubmit);
    document.getElementById("resetScoreFormBtn").addEventListener("click", () => resetScoreForm("scoreForm", "scoreStatus"));
    return;
  }

  appView.innerHTML = `
      <section class="summary-strip">
      ${renderSummaryCard("账号角色", escapeHtml(state.currentUser.role), "当前登录视角", "warm")}
      ${renderSummaryCard("身份类型", escapeHtml(state.currentUser.identityType || "NONE"), "决定可访问的数据范围")}
      ${renderSummaryCard("绑定工号", state.currentUser.identityId ?? "未绑定", "学生或老师工号")}
      ${renderSummaryCard("访问范围", state.currentUser.identityType === "STUDENT" ? "本人资料与成绩" : state.currentUser.identityType === "TEACHER" ? "老师资料、学生列表与成绩" : "需先绑定", state.currentUser.identityType === "TEACHER" ? "教师登录后可查看并维护学生成绩" : state.currentUser.identityType === "STUDENT" ? "学生只能查看自己的资料和成绩" : "普通用户不能查看别人信息")}
    </section>
    <section class="overview-grid">
      ${userInfo}
      <section class="section section-tint" id="identityPanel"></section>
    </section>
    <section class="section" id="profilePanel"></section>
  `;
}

// 渲染摘要卡片。
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

// 统计未绑定身份的普通用户数量，供管理员快速查看系统状态。
function countUnboundUsers() {
  return state.users.filter(user => (user.role || "").toUpperCase() !== "ADMIN" && (user.identityType || "NONE") === "NONE").length;
}

// 管理员加载全部用户、学生、老师数据。
// 这里用 Promise.all 并发请求，避免面板分三次慢慢刷新。
async function loadAdminData() {
  const [usersRes, studentsRes, teachersRes, scoresRes] = await Promise.all([
    fetchJson("/users"),
    fetchJson("/students"),
    fetchJson("/teachers"),
    fetchJson("/scores")
  ]);
  state.users = usersRes.data;
  state.students = studentsRes.data;
  state.teachers = teachersRes.data;
  state.scores = scoresRes.data;
  renderAdminMetrics();
  renderUsersTable();
  renderStudentsTable();
  renderTeachersTable();
  renderScoresTable("scoreTableWrap", state.scores, "score", true);
}

// 管理员数据加载完成后刷新顶部统计，避免初始渲染时显示旧数字。
function renderAdminMetrics() {
  const wrap = document.getElementById("adminSummaryStrip");
  if (!wrap) {
    return;
  }
  wrap.innerHTML = `
    ${renderSummaryCard("用户总数", state.users.length, "当前系统里的登录账号", "warm")}
    ${renderSummaryCard("学生总数", state.students.length, "可绑定学生身份的记录")}
    ${renderSummaryCard("老师总数", state.teachers.length, "可绑定老师身份的记录")}
    ${renderSummaryCard("成绩总数", state.scores.length, "当前成绩记录条数")}
    ${renderSummaryCard("待绑定账号", countUnboundUsers(), "还没有绑定身份的普通用户")}
  `;
}

// 普通用户根据当前绑定的身份加载自己的资料。
// 学生去 /students/me，老师去 /teachers/me，没有绑定则只显示绑定表单。
async function loadUserProfile() {
  renderUserPanelsLoading();
  state.visibleStudents = [];
  state.visibleScores = [];
  if (state.currentUser.identityType === "STUDENT") {
    const [profileRes, scoresRes] = await Promise.all([
      fetchJson("/students/me"),
      fetchJson("/scores/me")
    ]);
    state.currentProfile = profileRes.data;
    state.visibleScores = scoresRes.data;
  } else if (state.currentUser.identityType === "TEACHER") {
    const [teacherRes, studentsRes, scoresRes] = await Promise.all([
      fetchJson("/teachers/me"),
      fetchJson("/students"),
      fetchJson("/scores")
    ]);
    state.currentProfile = teacherRes.data;
    state.visibleStudents = studentsRes.data;
    state.visibleScores = scoresRes.data;
  } else {
    state.currentProfile = null;
  }
  renderUserPanels();
}

// 普通用户资料加载过程中的占位内容。
function renderUserPanelsLoading() {
  const identityPanel = document.getElementById("identityPanel");
  const profilePanel = document.getElementById("profilePanel");
  if (!identityPanel || !profilePanel) {
    return;
  }
  identityPanel.innerHTML = `<p class="section-label">身份状态</p><h3>正在加载</h3><p class="hint">请稍候，我们正在读取你的绑定信息。</p>`;
  profilePanel.innerHTML = `<p class="empty-state">正在加载个人资料...</p>`;
}

// 渲染普通用户的“身份绑定 / 个人资料”区域。
// 这部分只服务于普通用户，所以管理员不会进到这里。
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
    ]) + renderStudentScoreSection();
    renderScoresTable("studentScoreTableWrap", state.visibleScores, "student-score", false);
    return;
  }

  profilePanel.innerHTML = renderProfileCard("老师个人资料", [
    ["老师工号", state.currentProfile.id],
    ["姓名", state.currentProfile.name],
    ["职称", state.currentProfile.title],
    ["手机号", state.currentProfile.phone]
  ]) + renderTeacherStudentSection() + renderTeacherScoreSection();

  renderScoresTable("teacherScoreTableWrap", state.visibleScores, "teacher-score", true);

  const teacherScoreForm = document.getElementById("teacherScoreForm");
  if (teacherScoreForm) {
    teacherScoreForm.addEventListener("submit", onTeacherScoreCreateSubmit);
    document.getElementById("updateTeacherScoreBtn").addEventListener("click", onTeacherScoreUpdateSubmit);
    document.getElementById("resetTeacherScoreFormBtn").addEventListener("click", () => resetScoreForm("teacherScoreForm", "teacherScoreStatus"));
  }
}

function renderStudentScoreSection() {
  return `
    <div class="teacher-student-section">
      <div class="card-titlebar">
        <div>
          <p class="section-label">我的成绩</p>
          <h3>学生成绩列表</h3>
        </div>
        <span class="badge">${state.visibleScores.length} 条</span>
      </div>
      <div id="studentScoreTableWrap"></div>
    </div>
  `;
}

function renderTeacherStudentSection() {
  if (!state.visibleStudents.length) {
    return `
      <div class="teacher-student-section">
        <div class="card-titlebar">
          <div>
            <p class="section-label">学生信息</p>
            <h3>当前暂无学生数据</h3>
          </div>
        </div>
      </div>
    `;
  }

  return `
    <div class="teacher-student-section">
      <div class="card-titlebar">
        <div>
          <p class="section-label">学生信息</p>
          <h3>教师可查看的学生列表</h3>
        </div>
        <span class="badge">${state.visibleStudents.length} 条</span>
      </div>
      <div class="table-wrap">
        <table>
          <thead>
            <tr>
              <th>工号</th>
              <th>姓名</th>
              <th>年龄</th>
              <th>手机号</th>
            </tr>
          </thead>
          <tbody>
            ${state.visibleStudents.map(student => `
              <tr>
                <td>${student.id}</td>
                <td>${escapeHtml(student.name)}</td>
                <td>${student.age}</td>
                <td>${escapeHtml(student.phone)}</td>
              </tr>
            `).join("")}
          </tbody>
        </table>
      </div>
    </div>
  `;
}

function renderTeacherScoreSection() {
  return `
    <div class="teacher-student-section">
      <div class="card-titlebar">
        <div>
          <p class="section-label">学生成绩</p>
          <h3>教师可查看和维护的成绩</h3>
        </div>
        <span class="badge">${state.visibleScores.length} 条</span>
      </div>
      <div id="teacherScoreTableWrap"></div>
      <form id="teacherScoreForm" class="stack score-form-inline">
        <input name="id" type="hidden">
        <label>学生工号
          <input name="studentId" type="number" placeholder="例如 1" required>
        </label>
        <div class="row">
          <label>课程名称
            <input name="courseName" placeholder="例如 高等数学" required>
          </label>
          <label>成绩
            <input name="score" type="number" step="0.01" min="0" max="100" placeholder="0 - 100" required>
          </label>
        </div>
        <label>学期
          <input name="semester" placeholder="例如 2026-春" required>
        </label>
        <div class="button-row">
          <button class="primary" type="submit">新增成绩</button>
          <button class="secondary" type="button" id="updateTeacherScoreBtn">更新当前成绩</button>
          <button class="ghost" type="button" id="resetTeacherScoreFormBtn">清空表单</button>
        </div>
        <div class="status" id="teacherScoreStatus"></div>
      </form>
    </div>
  `;
}

function renderScoreTableMarkup(scores, actionPrefix, editable) {
  if (!scores.length) {
    return `<p class="empty-state">暂无成绩数据。</p>`;
  }

  return `
    <div class="table-wrap">
      <table>
        <thead>
          <tr>
            <th>记录ID</th>
            <th>学生工号</th>
            <th>课程</th>
            <th>成绩</th>
            <th>学期</th>
            <th>任课老师</th>
            ${editable ? "<th>操作</th>" : ""}
          </tr>
        </thead>
        <tbody>
          ${scores.map(score => `
            <tr>
              <td>${score.id}</td>
              <td>${score.studentId}</td>
              <td>${escapeHtml(score.courseName)}</td>
              <td>${formatScore(score.score)}</td>
              <td>${escapeHtml(score.semester)}</td>
              <td>${escapeHtml(score.teacherName || "-")}</td>
              ${editable ? `
                <td>
                  <div class="table-actions">
                    <button class="ghost" type="button" data-${actionPrefix}-edit="${score.id}">填入表单</button>
                  </div>
                </td>
              ` : ""}
            </tr>
          `).join("")}
        </tbody>
      </table>
    </div>
  `;
}

// 把一组键值对渲染成简洁的资料卡。
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

// 渲染管理员的用户表格。
// 每一行的“编辑”按钮会把对应账号信息填入右侧表单。
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
    <div class="table-wrap">
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
    </div>
  `;

  wrap.querySelectorAll("[data-user-id]").forEach(button => {
    button.addEventListener("click", () => fillUserForm(Number(button.dataset.userId)));
  });
}

// 渲染管理员的学生表格。
// 学生区除了展示，还支持“填入表单”和“删除”这两个动作。
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
    <div class="table-wrap">
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
    </div>
  `;

  wrap.querySelectorAll("[data-student-edit]").forEach(button => {
    button.addEventListener("click", () => fillStudentForm(Number(button.dataset.studentEdit)));
  });
  wrap.querySelectorAll("[data-student-delete]").forEach(button => {
    button.addEventListener("click", () => onDeleteStudent(Number(button.dataset.studentDelete)));
  });
}

// 渲染老师表格。
// 这里暂时只做展示和“填入表单”，因为后端目前只开放了新增和查询。
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
    <div class="table-wrap">
    <table>
      <thead>
        <tr>
          <th>工号</th>
          <th>姓名</th>
          <th>职称</th>
          <th>手机号</th>
          <th>操作</th>
        </tr>
      </thead>
      <tbody>
        ${state.teachers.map(teacher => `
          <tr>
            <td>${teacher.id}</td>
            <td>${escapeHtml(teacher.name)}</td>
            <td>${escapeHtml(teacher.title)}</td>
            <td>${escapeHtml(teacher.phone)}</td>
            <td>
              <div class="table-actions">
                <button class="ghost" type="button" data-teacher-fill="${teacher.id}">填入表单</button>
              </div>
            </td>
          </tr>
        `).join("")}
      </tbody>
    </table>
    </div>
  `;

  wrap.querySelectorAll("[data-teacher-fill]").forEach(button => {
    button.addEventListener("click", () => fillTeacherForm(Number(button.dataset.teacherFill)));
  });
}

function renderScoresTable(containerId, scores, actionPrefix, editable) {
  const wrap = document.getElementById(containerId);
  if (!wrap) {
    return;
  }
  wrap.innerHTML = renderScoreTableMarkup(scores, actionPrefix, editable);
  if (!editable) {
    return;
  }

  wrap.querySelectorAll(`[data-${actionPrefix}-edit]`).forEach(button => {
    button.addEventListener("click", () => {
      const scoreId = Number(button.dataset[`${camelize(actionPrefix)}Edit`]);
      if (containerId === "scoreTableWrap") {
        fillScoreForm("scoreForm", scoreId, state.scores, "scoreStatus");
        return;
      }
      fillScoreForm("teacherScoreForm", scoreId, state.visibleScores, "teacherScoreStatus");
    });
  });
}

// 把左侧表格里的用户数据填入右侧编辑表单。
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

// 把左侧表格里的学生数据填入右侧维护表单。
// 这样管理员可以先点选，再决定是否更新。
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

// 把老师表格里的数据填入老师维护表单。
// 目前主要是为了减少重复录入，也方便你后面继续扩更新功能。
function fillTeacherForm(teacherId) {
  const teacher = state.teachers.find(item => item.id === teacherId);
  if (!teacher) {
    return;
  }
  const form = document.getElementById("teacherForm");
  form.elements["id"].value = teacher.id;
  form.elements["name"].value = teacher.name;
  form.elements["title"].value = teacher.title;
  form.elements["phone"].value = teacher.phone;
  setStatus("teacherStatus", `已载入老师 ${teacher.name}，可以作为新增参考。`, "success");
}

function fillScoreForm(formId, scoreId, scores, statusId) {
  const score = scores.find(item => item.id === scoreId);
  if (!score) {
    return;
  }
  const form = document.getElementById(formId);
  if (!form) {
    return;
  }
  form.elements["id"].value = score.id;
  form.elements["studentId"].value = score.studentId;
  form.elements["courseName"].value = score.courseName;
  form.elements["score"].value = formatScore(score.score);
  form.elements["semester"].value = score.semester;
  setStatus(statusId, `已载入成绩记录 ${score.id}，可以更新。`, "success");
}

// 清空学生表单。
function resetStudentForm() {
  const form = document.getElementById("studentForm");
  form.reset();
  setStatus("studentStatus", "", "");
}

// 清空老师表单。
function resetTeacherForm() {
  const form = document.getElementById("teacherForm");
  form.reset();
  setStatus("teacherStatus", "", "");
}

function resetScoreForm(formId, statusId) {
  const form = document.getElementById(formId);
  if (!form) {
    return;
  }
  form.reset();
  if (form.elements["id"]) {
    form.elements["id"].value = "";
  }
  setStatus(statusId, "", "");
}

// 登录表单提交。
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

// 注册表单提交。
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

// 退出登录。
async function onLogout() {
  await fetchJson("/auth/logout", { method: "POST" });
  state.currentUser = null;
  renderAuthView();
}

// 普通用户绑定身份。
// 绑定成功后重新刷新 Session 对应的用户信息和资料面板。
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

// 管理员更新用户信息。
// 账号资料改完后重新拉一遍管理员数据，让表格和统计保持同步。
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

// 管理员新增学生。
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

// 管理员更新学生。
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
// 管理员更新教师
async function onTeacherUpadateSubmit(){
 const form =document.getElementById("teacherForm");
 const payload =readTeacherForm(form);
    if(!payload.id){
      setStatus("teacherStatus","请先输入或载入教师工号","error");
      return;
    }
    try{
        await fetchJson(`/teachers/${payload.id}`,{
        method:"PUT",
        body:JSON.stringify(payload)
        });
        setStatus("teacherStatus","成功修改教师信息","success");
        await loadAdminData();
    }catch(error){
        setStatus("teacherStatus",error.message,"error");
    }

}
// 管理员删除学生。
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

// 管理员新增老师。
// 这里和新增学生的流程保持一致：读表单、发请求、提示结果、刷新列表。
async function onTeacherCreateSubmit(event) {
  event.preventDefault();
  const form = event.currentTarget;
  const payload = readTeacherForm(form);

  try {
    await fetchJson("/teachers", {
      method: "POST",
      body: JSON.stringify(payload)
    });
    setStatus("teacherStatus", "老师新增成功。", "success");
    resetTeacherForm();
    await loadAdminData();
  } catch (error) {
    setStatus("teacherStatus", error.message, "error");
  }
}

async function onScoreCreateSubmit(event) {
  event.preventDefault();
  const form = event.currentTarget;
  const payload = readScoreForm(form);

  try {
    await fetchJson("/scores", {
      method: "POST",
      body: JSON.stringify(payload)
    });
    setStatus("scoreStatus", "成绩新增成功。", "success");
    resetScoreForm("scoreForm", "scoreStatus");
    await loadAdminData();
  } catch (error) {
    setStatus("scoreStatus", error.message, "error");
  }
}

async function onScoreUpdateSubmit() {
  const form = document.getElementById("scoreForm");
  const payload = readScoreForm(form);
  if (!form.elements["id"].value) {
    setStatus("scoreStatus", "请先从左侧选择一条成绩记录。", "error");
    return;
  }

  try {
    await fetchJson(`/scores/${form.elements["id"].value}`, {
      method: "PUT",
      body: JSON.stringify(payload)
    });
    setStatus("scoreStatus", "成绩信息更新成功。", "success");
    await loadAdminData();
  } catch (error) {
    setStatus("scoreStatus", error.message, "error");
  }
}

async function onTeacherScoreCreateSubmit(event) {
  event.preventDefault();
  const form = event.currentTarget;
  const payload = readScoreForm(form);

  try {
    await fetchJson("/scores", {
      method: "POST",
      body: JSON.stringify(payload)
    });
    setStatus("teacherScoreStatus", "成绩新增成功。", "success");
    resetScoreForm("teacherScoreForm", "teacherScoreStatus");
    await loadUserProfile();
  } catch (error) {
    setStatus("teacherScoreStatus", error.message, "error");
  }
}

async function onTeacherScoreUpdateSubmit() {
  const form = document.getElementById("teacherScoreForm");
  const payload = readScoreForm(form);
  if (!form.elements["id"].value) {
    setStatus("teacherScoreStatus", "请先选择一条成绩记录。", "error");
    return;
  }

  try {
    await fetchJson(`/scores/${form.elements["id"].value}`, {
      method: "PUT",
      body: JSON.stringify(payload)
    });
    setStatus("teacherScoreStatus", "成绩信息更新成功。", "success");
    await loadUserProfile();
  } catch (error) {
    setStatus("teacherScoreStatus", error.message, "error");
  }
}

// 从学生表单中读取数据并组装成请求体。
function readStudentForm(form) {
  return {
    id: Number(form.elements["id"].value),
    name: form.elements["name"].value.trim(),
    age: Number(form.elements["age"].value),
    phone: form.elements["phone"].value.trim()
  };
}

// 从老师表单中读取数据并组装成请求体。
function readTeacherForm(form) {
  return {
    id: Number(form.elements["id"].value),
    name: form.elements["name"].value.trim(),
    title: form.elements["title"].value.trim(),
    phone: form.elements["phone"].value.trim()
  };
}

function readScoreForm(form) {
  return {
    studentId: Number(form.elements["studentId"].value),
    courseName: form.elements["courseName"].value.trim(),
    score: Number(form.elements["score"].value),
    semester: form.elements["semester"].value.trim()
  };
}

// 统一设置页面上的提示信息。
// 同一个函数服务于登录提示、注册提示、表单保存结果等多个位置。
function setStatus(id, message, type) {
  const element = document.getElementById(id);
  if (!element) {
    return;
  }
  element.textContent = message;
  element.className = `status ${type || ""}`.trim();
}

// 简单的 HTML 转义，防止把字符串直接塞进 innerHTML 时破坏页面结构。
function escapeHtml(value) {
  return value
    .replaceAll("&", "&amp;")
    .replaceAll("<", "&lt;")
    .replaceAll(">", "&gt;")
    .replaceAll('"', "&quot;")
    .replaceAll("'", "&#39;");
}

function formatScore(value) {
  const number = Number(value);
  if (Number.isNaN(number)) {
    return "";
  }
  return number.toFixed(2);
}

function camelize(value) {
  return value.replace(/-([a-z])/g, (_, letter) => letter.toUpperCase());
}
