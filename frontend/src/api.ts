import axios from 'axios';

export type ApiError = {
  code: string;
  message: string;
};

export type ApiResponse<T> = {
  success: boolean;
  data: T;
  error: ApiError | null;
};

export type CurrentUser = {
  id: number;
  username: string;
  displayName: string;
  roles: string[];
  permissions: string[];
};

export type LoginResponse = {
  accessToken: string;
  tokenType: string;
  user: CurrentUser;
};

export type Department = {
  id: number;
  code: string;
  name: string;
  parentId: number | null;
  leaderUserId: number | null;
  sortOrder: number;
  enabled: boolean;
  children: Department[];
};

export type Position = {
  id: number;
  departmentId: number;
  code: string;
  name: string;
  sortOrder: number;
  enabled: boolean;
};

export type Role = {
  id: number;
  code: string;
  name: string;
  enabled: boolean;
};

export type Permission = {
  id: number;
  code: string;
  name: string;
  type: string;
  parentId: number | null;
  sortOrder: number;
  enabled: boolean;
};

export type User = {
  id: number;
  username: string;
  displayName: string;
  employeeNo?: string;
  email?: string;
  phone?: string;
  status: string;
  departmentId: number | null;
  departmentName: string | null;
  positionId: number | null;
  positionName: string | null;
  directManagerId: number | null;
  directManagerName: string | null;
  roles: Role[];
};

export type UserPayload = {
  username: string;
  displayName: string;
  password?: string;
  employeeNo?: string;
  email?: string;
  phone?: string;
  departmentId?: number;
  positionId?: number;
  directManagerId?: number;
  roleIds?: number[];
};

export type ApprovalPayload = {
  title: string;
  type: string;
  urgent?: boolean;
  amount?: number;
  startTime?: string;
  endTime?: string;
  formData: Record<string, unknown>;
};

export type ApprovalActionLog = {
  id: number;
  actorName: string;
  action: string;
  fromStatus?: string;
  toStatus?: string;
  comment?: string;
  createdAt: string;
};

export type ApprovalAttachment = {
  id: number;
  requestId: number;
  fileName: string;
  contentType?: string;
  fileSize: number;
  createdAt: string;
};

export type ApprovalTask = {
  id: number;
  status: string;
  requestId: number;
  requestNo: string;
  title: string;
  type: string;
  requestStatus: string;
  applicantName: string;
  departmentName?: string;
  assigneeId: number;
  assigneeName: string;
  nodeName?: string;
  comment?: string;
  assignedAt: string;
  actedAt?: string;
  dueAt?: string;
};

export type ApprovalCc = {
  id: number;
  requestId: number;
  requestNo: string;
  title: string;
  type: string;
  requestStatus: string;
  applicantName: string;
  comment?: string;
  readAt?: string;
  createdAt: string;
};

export type ApprovalTimelineNode = {
  id: number;
  nodeName: string;
  approverName: string;
  status: string;
  sortOrder: number;
  taskStatus?: string;
  comment?: string;
  startedAt?: string;
  completedAt?: string;
  assignedAt?: string;
  actedAt?: string;
};

export type ApprovalRequest = {
  id: number;
  requestNo: string;
  title: string;
  type: string;
  status: string;
  applicantName: string;
  departmentName?: string;
  urgent: boolean;
  amount?: number;
  startTime?: string;
  endTime?: string;
  formData: Record<string, unknown>;
  submittedAt?: string;
  createdAt: string;
  actionLogs: ApprovalActionLog[];
  attachments: ApprovalAttachment[];
  timelineNodes: ApprovalTimelineNode[];
};

export type WorkflowNodeTemplate = {
  id?: number;
  nodeName: string;
  approverRule: string;
  approverUserId?: number;
  amountGreaterThan?: number;
  sortOrder: number;
};

export type WorkflowTemplate = {
  id: number;
  approvalType: string;
  name: string;
  enabled: boolean;
  createdAt: string;
  updatedAt: string;
  nodes: WorkflowNodeTemplate[];
};

export type WorkflowTemplatePayload = {
  approvalType: string;
  name: string;
  enabled?: boolean;
  nodes: WorkflowNodeTemplate[];
};

export type WorkflowConfigAudit = {
  id: number;
  templateId?: number;
  actorName: string;
  action: string;
  detail?: string;
  createdAt: string;
};

export type NotificationItem = {
  id: number;
  type: string;
  title: string;
  content?: string;
  relatedRequestId?: number;
  relatedTaskId?: number;
  read: boolean;
  readAt?: string;
  createdAt: string;
};

export type ApprovalManagementQuery = {
  type?: string;
  status?: string;
  applicantKeyword?: string;
  departmentId?: number;
  startCreatedAt?: string;
  endCreatedAt?: string;
  minAmount?: number;
  maxAmount?: number;
  urgent?: boolean;
  page?: number;
  pageSize?: number;
};

export type PagedResult<T> = {
  items: T[];
  totalCount: number;
  page: number;
  pageSize: number;
};

export type ApprovalManagementItem = {
  id: number;
  requestNo: string;
  title: string;
  type: string;
  status: string;
  applicantId: number;
  applicantName: string;
  departmentId?: number;
  departmentName?: string;
  urgent: boolean;
  amount?: number;
  submittedAt?: string;
  createdAt: string;
};

export type DashboardSummary = {
  myTodoCount: number;
  myInProgressCount: number;
  unreadNotificationCount: number;
  globalPendingCount: number;
  todaySubmittedCount: number;
};

export type ApprovalStatistics = {
  typeCounts: Record<string, number>;
  statusCounts: Record<string, number>;
  pendingCount: number;
  approvedCount: number;
  rejectedCount: number;
  overdueTaskCount: number;
};

export const TOKEN_KEY = 'enterprise_approval_token';

export const api = axios.create({
  baseURL: '/api'
});

api.interceptors.request.use((config) => {
  const token = localStorage.getItem(TOKEN_KEY);
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

export async function login(username: string, password: string) {
  const response = await api.post<ApiResponse<LoginResponse>>('/auth/login', { username, password });
  return response.data;
}

export async function fetchCurrentUser() {
  const response = await api.get<ApiResponse<CurrentUser>>('/auth/me');
  return response.data;
}

export async function fetchHealth() {
  const response = await api.get<ApiResponse<{ status: string; service: string; time: string }>>('/health');
  return response.data;
}

export async function fetchDepartmentTree() {
  const response = await api.get<ApiResponse<Department[]>>('/organization/departments/tree');
  return response.data;
}

export async function fetchPositions(departmentId?: number) {
  const response = await api.get<ApiResponse<Position[]>>('/organization/positions', {
    params: departmentId ? { departmentId } : undefined
  });
  return response.data;
}

export async function fetchUsers() {
  const response = await api.get<ApiResponse<User[]>>('/users');
  return response.data;
}

export async function createUser(payload: UserPayload) {
  const response = await api.post<ApiResponse<User>>('/users', payload);
  return response.data;
}

export async function updateUser(id: number, payload: UserPayload) {
  const response = await api.put<ApiResponse<User>>(`/users/${id}`, payload);
  return response.data;
}

export async function setUserStatus(id: number, status: 'active' | 'disabled') {
  const response = await api.patch<ApiResponse<User>>(`/users/${id}/status`, null, { params: { status } });
  return response.data;
}

export async function fetchRoles() {
  const response = await api.get<ApiResponse<Role[]>>('/roles');
  return response.data;
}

export async function fetchRbacRoles() {
  const response = await api.get<ApiResponse<Role[]>>('/rbac/roles');
  return response.data;
}

export async function fetchPermissions() {
  const response = await api.get<ApiResponse<Permission[]>>('/rbac/permissions');
  return response.data;
}

export async function fetchRolePermissions(roleId: number) {
  const response = await api.get<ApiResponse<{ roleId: number; permissionIds: number[] }>>(`/rbac/roles/${roleId}/permissions`);
  return response.data;
}

export async function saveRolePermissions(roleId: number, permissionIds: number[]) {
  const response = await api.put<ApiResponse<{ roleId: number; permissionIds: number[] }>>(`/rbac/roles/${roleId}/permissions`, { permissionIds });
  return response.data;
}

export async function saveApprovalDraft(payload: ApprovalPayload) {
  const response = await api.post<ApiResponse<ApprovalRequest>>('/approvals/drafts', payload);
  return response.data;
}

export async function submitApproval(payload: ApprovalPayload) {
  const response = await api.post<ApiResponse<ApprovalRequest>>('/approvals', payload);
  return response.data;
}

export async function submitApprovalDraft(id: number, payload: ApprovalPayload) {
  const response = await api.post<ApiResponse<ApprovalRequest>>(`/approvals/${id}/submit`, payload);
  return response.data;
}

export async function fetchMyApprovals() {
  const response = await api.get<ApiResponse<ApprovalRequest[]>>('/approvals/mine');
  return response.data;
}

export async function fetchApprovalDetail(id: number) {
  const response = await api.get<ApiResponse<ApprovalRequest>>(`/approvals/${id}`);
  return response.data;
}

export async function fetchApprovalTodoTasks() {
  const response = await api.get<ApiResponse<ApprovalTask[]>>('/approvals/todo');
  return response.data;
}

export async function fetchApprovalDoneTasks() {
  const response = await api.get<ApiResponse<ApprovalTask[]>>('/approvals/done');
  return response.data;
}

export async function fetchApprovalCcTasks() {
  const response = await api.get<ApiResponse<ApprovalCc[]>>('/approvals/cc');
  return response.data;
}

export async function approveApprovalTask(taskId: number, comment?: string) {
  const response = await api.post<ApiResponse<ApprovalTask>>(`/approvals/tasks/${taskId}/approve`, { comment });
  return response.data;
}

export async function rejectApprovalTask(taskId: number, comment: string) {
  const response = await api.post<ApiResponse<ApprovalTask>>(`/approvals/tasks/${taskId}/reject`, { comment });
  return response.data;
}

export async function requestMoreInfoApprovalTask(taskId: number, comment: string) {
  const response = await api.post<ApiResponse<ApprovalTask>>(`/approvals/tasks/${taskId}/request-more-info`, { comment });
  return response.data;
}

export async function transferApprovalTask(taskId: number, targetUserId: number, comment?: string) {
  const response = await api.post<ApiResponse<ApprovalTask>>(`/approvals/tasks/${taskId}/transfer`, { targetUserId, comment });
  return response.data;
}

export async function addSignerApprovalTask(taskId: number, targetUserId: number, comment?: string) {
  const response = await api.post<ApiResponse<ApprovalTask>>(`/approvals/tasks/${taskId}/add-signer`, { targetUserId, comment });
  return response.data;
}

export async function ccApprovalTask(taskId: number, targetUserIds: number[], comment?: string) {
  const response = await api.post<ApiResponse<ApprovalTask>>(`/approvals/tasks/${taskId}/cc`, { targetUserIds, comment });
  return response.data;
}

export async function withdrawApproval(id: number, comment?: string) {
  const response = await api.post<ApiResponse<void>>(`/approvals/${id}/withdraw`, { comment });
  return response.data;
}

export async function voidApproval(id: number, comment?: string) {
  const response = await api.post<ApiResponse<void>>(`/approvals/${id}/void`, { comment });
  return response.data;
}

export async function resubmitMoreInfoApproval(id: number, payload: ApprovalPayload) {
  const response = await api.post<ApiResponse<ApprovalRequest>>(`/approvals/${id}/resubmit`, payload);
  return response.data;
}

export async function uploadApprovalAttachment(requestId: number, file: File) {
  const formData = new FormData();
  formData.append('file', file);
  const response = await api.post<ApiResponse<ApprovalAttachment>>(`/approvals/${requestId}/attachments`, formData, {
    headers: { 'Content-Type': 'multipart/form-data' }
  });
  return response.data;
}

export async function deleteApprovalAttachment(requestId: number, attachmentId: number) {
  const response = await api.delete<ApiResponse<void>>(`/approvals/${requestId}/attachments/${attachmentId}`);
  return response.data;
}

export async function downloadApprovalAttachment(requestId: number, attachmentId: number, fileName: string) {
  const response = await api.get(`/approvals/${requestId}/attachments/${attachmentId}/download`, { responseType: 'blob' });
  const url = window.URL.createObjectURL(response.data);
  const link = document.createElement('a');
  link.href = url;
  link.download = fileName;
  document.body.appendChild(link);
  link.click();
  link.remove();
  window.URL.revokeObjectURL(url);
}

export async function fetchWorkflowTemplates() {
  const response = await api.get<ApiResponse<WorkflowTemplate[]>>('/workflow-templates');
  return response.data;
}

export async function createWorkflowTemplate(payload: WorkflowTemplatePayload) {
  const response = await api.post<ApiResponse<WorkflowTemplate>>('/workflow-templates', payload);
  return response.data;
}

export async function updateWorkflowTemplate(id: number, payload: WorkflowTemplatePayload) {
  const response = await api.put<ApiResponse<WorkflowTemplate>>(`/workflow-templates/${id}`, payload);
  return response.data;
}

export async function setWorkflowTemplateEnabled(id: number, enabled: boolean) {
  const response = await api.patch<ApiResponse<WorkflowTemplate>>(`/workflow-templates/${id}/enabled`, null, { params: { enabled } });
  return response.data;
}

export async function fetchWorkflowConfigAudits() {
  const response = await api.get<ApiResponse<WorkflowConfigAudit[]>>('/workflow-templates/audits');
  return response.data;
}

export async function fetchNotifications() {
  const response = await api.get<ApiResponse<NotificationItem[]>>('/notifications');
  return response.data;
}

export async function fetchUnreadNotificationCount() {
  const response = await api.get<ApiResponse<{ count: number }>>('/notifications/unread-count');
  return response.data;
}

export async function markNotificationRead(id: number) {
  const response = await api.patch<ApiResponse<NotificationItem>>(`/notifications/${id}/read`);
  return response.data;
}

export async function markAllNotificationsRead() {
  const response = await api.patch<ApiResponse<void>>('/notifications/read-all');
  return response.data;
}

export async function fetchAuditApprovalActions() {
  const response = await api.get<ApiResponse<ApprovalActionLog[]>>('/audit-logs/approval-actions');
  return response.data;
}

export async function fetchAuditWorkflowConfigs() {
  const response = await api.get<ApiResponse<WorkflowConfigAudit[]>>('/audit-logs/workflow-configs');
  return response.data;
}

export async function fetchApprovalManagement(query?: ApprovalManagementQuery) {
  const response = await api.get<ApiResponse<PagedResult<ApprovalManagementItem>>>('/approval-management', { params: query });
  return response.data;
}

export async function exportApprovalManagement(query?: ApprovalManagementQuery) {
  const response = await api.get('/approval-management/export', { params: query, responseType: 'blob' });
  const url = window.URL.createObjectURL(response.data);
  const link = document.createElement('a');
  link.href = url;
  link.download = 'approval-records.csv';
  document.body.appendChild(link);
  link.click();
  link.remove();
  window.URL.revokeObjectURL(url);
}

export async function fetchDashboardSummary() {
  const response = await api.get<ApiResponse<DashboardSummary>>('/approval-management/dashboard');
  return response.data;
}

export async function fetchApprovalStatistics() {
  const response = await api.get<ApiResponse<ApprovalStatistics>>('/approval-management/statistics');
  return response.data;
}
