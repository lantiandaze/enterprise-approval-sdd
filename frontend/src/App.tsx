import { useEffect, useMemo, useState } from 'react';
import { useMutation, useQuery } from '@tanstack/react-query';
import { Button, Card, Checkbox, DatePicker, Form, Input, InputNumber, Layout, Menu, Modal, Select, Space, Switch, Table, Tree, Typography, Upload, message } from 'antd';
import type { TreeDataNode } from 'antd';
import {
  AuditOutlined,
  BellOutlined,
  CheckSquareOutlined,
  DashboardOutlined,
  DeleteOutlined,
  DownloadOutlined,
  LogoutOutlined,
  PartitionOutlined,
  SafetyOutlined,
  SendOutlined,
  SettingOutlined,
  TeamOutlined,
  UploadOutlined,
  UserOutlined
} from '@ant-design/icons';
import {
  CurrentUser,
  Department,
  Position,
  TOKEN_KEY,
  User,
  UserPayload,
  ApprovalPayload,
  ApprovalCc,
  ApprovalManagementItem,
  ApprovalManagementQuery,
  ApprovalRequest,
  ApprovalTask,
  WorkflowTemplate,
  WorkflowTemplatePayload,
  NotificationItem,
  addSignerApprovalTask,
  approveApprovalTask,
  ccApprovalTask,
  createWorkflowTemplate,
  deleteApprovalAttachment,
  downloadApprovalAttachment,
  createUser,
  exportApprovalManagement,
  fetchApprovalDetail,
  fetchApprovalCcTasks,
  fetchApprovalDoneTasks,
  fetchApprovalTodoTasks,
  fetchCurrentUser,
  fetchDashboardSummary,
  fetchDepartmentTree,
  fetchHealth,
  fetchPermissions,
  fetchPositions,
  fetchRbacRoles,
  fetchRolePermissions,
  fetchRoles,
  fetchUsers,
  fetchMyApprovals,
  fetchApprovalManagement,
  fetchApprovalStatistics,
  fetchAuditApprovalActions,
  fetchAuditWorkflowConfigs,
  fetchNotifications,
  fetchUnreadNotificationCount,
  fetchWorkflowConfigAudits,
  fetchWorkflowTemplates,
  login,
  markAllNotificationsRead,
  markNotificationRead,
  rejectApprovalTask,
  requestMoreInfoApprovalTask,
  resubmitMoreInfoApproval,
  saveRolePermissions,
  saveApprovalDraft,
  setUserStatus,
  submitApproval,
  submitApprovalDraft,
  transferApprovalTask,
  uploadApprovalAttachment,
  updateUser,
  updateWorkflowTemplate,
  setWorkflowTemplateEnabled,
  withdrawApproval
} from './api';

const { Header, Sider, Content } = Layout;

type LoginValues = {
  username: string;
  password: string;
};

const menuItems = [
  { key: 'dashboard', icon: <DashboardOutlined />, label: '工作台', permission: 'menu.dashboard' },
  { key: 'new', icon: <SendOutlined />, label: '发起申请', permission: 'menu.approvals.new' },
  { key: 'my', icon: <CheckSquareOutlined />, label: '我的申请', permission: 'menu.approvals.my' },
  { key: 'todo', icon: <CheckSquareOutlined />, label: '我的待办', permission: 'menu.approvals.todo' },
  { key: 'done', icon: <CheckSquareOutlined />, label: '我的已办', permission: 'menu.approvals.done' },
  { key: 'cc', icon: <BellOutlined />, label: '抄送我的', permission: 'menu.approvals.cc' },
  { key: 'manage', icon: <SettingOutlined />, label: '审批管理', permission: 'menu.approvals.manage' },
  { key: 'organization', icon: <PartitionOutlined />, label: '组织架构', permission: 'menu.organization' },
  { key: 'users', icon: <TeamOutlined />, label: '用户管理', permission: 'menu.users' },
  { key: 'roles', icon: <SafetyOutlined />, label: '角色权限', permission: 'menu.roles' },
  { key: 'workflow', icon: <SettingOutlined />, label: '流程配置', permission: 'menu.workflow_config' },
  { key: 'notifications', icon: <BellOutlined />, label: '通知中心', permission: 'menu.notifications' },
  { key: 'audit', icon: <AuditOutlined />, label: '审计日志', permission: 'menu.audit_logs' }
];

const approvalTypeOptions = [
  { value: 'leave', label: '请假审批' },
  { value: 'expense', label: '报销审批' },
  { value: 'purchase', label: '采购审批' },
  { value: 'overtime', label: '加班审批' },
  { value: 'business_trip', label: '出差审批' }
];

function approvalTypeName(type: string) {
  return approvalTypeOptions.find((item) => item.value === type)?.label || type;
}

function statusName(status: string) {
  if (status === 'draft') return '草稿';
  if (status === 'submitted') return '已提交';
  if (status === 'in_progress') return '审批中';
  if (status === 'approved') return '已通过';
  if (status === 'rejected') return '已驳回';
  if (status === 'withdrawn') return '已撤回';
  if (status === 'need_more_info') return '待补充';
  if (status === 'voided') return '已作废';
  return status;
}

function taskStatusName(status: string) {
  if (status === 'pending') return '待处理';
  if (status === 'completed') return '已处理';
  if (status === 'need_more_info') return '待补充';
  if (status === 'withdrawn') return '已撤回';
  if (status === 'voided') return '已作废';
  return status;
}

function nodeStatusName(status: string) {
  if (status === 'pending') return '未开始';
  if (status === 'active') return '处理中';
  if (status === 'approved') return '已同意';
  if (status === 'rejected') return '已驳回';
  if (status === 'need_more_info') return '待补充';
  if (status === 'withdrawn') return '已撤回';
  if (status === 'voided') return '已作废';
  return status;
}

function approvalRequiresAttachment(type: string) {
  return type === 'expense' || type === 'purchase';
}

function LoginPage({ onLoggedIn }: { onLoggedIn: (user: CurrentUser) => void }) {
  const mutation = useMutation({
    mutationFn: (values: LoginValues) => login(values.username, values.password),
    onSuccess: (result) => {
      if (!result.success) {
        message.error(result.error?.message || '登录失败');
        return;
      }
      localStorage.setItem(TOKEN_KEY, result.data.accessToken);
      onLoggedIn(result.data.user);
      message.success('登录成功');
    },
    onError: () => message.error('登录失败')
  });

  return (
    <main className="login-page">
      <Card className="login-card">
        <Space direction="vertical" size={24} className="full-width">
          <div>
            <Typography.Title level={2}>企业审批管理系统</Typography.Title>
            <Typography.Text type="secondary">使用开发账号进入系统骨架</Typography.Text>
          </div>
          <Form layout="vertical" initialValues={{ username: 'admin', password: 'admin123' }} onFinish={(values) => mutation.mutate(values)}>
            <Form.Item label="用户名" name="username" rules={[{ required: true, message: '请输入用户名' }]}>
              <Input prefix={<UserOutlined />} autoComplete="username" />
            </Form.Item>
            <Form.Item label="密码" name="password" rules={[{ required: true, message: '请输入密码' }]}>
              <Input.Password autoComplete="current-password" />
            </Form.Item>
            <Button type="primary" htmlType="submit" loading={mutation.isPending} block>
              登录
            </Button>
          </Form>
        </Space>
      </Card>
    </main>
  );
}

function Dashboard({ user }: { user: CurrentUser }) {
  const health = useQuery({
    queryKey: ['health'],
    queryFn: fetchHealth,
    enabled: false
  });
  const unread = useQuery({
    queryKey: ['unreadNotificationCount'],
    queryFn: fetchUnreadNotificationCount
  });
  const summary = useQuery({
    queryKey: ['dashboardSummary'],
    queryFn: fetchDashboardSummary
  });
  const summaryData = summary.data?.success ? summary.data.data : null;

  const roleText = useMemo(() => user.roles.join(', '), [user.roles]);

  return (
    <div className="dashboard-grid">
      <Card title="当前用户">
        <Space direction="vertical">
          <Typography.Text>{user.displayName}</Typography.Text>
          <Typography.Text type="secondary">账号：{user.username}</Typography.Text>
          <Typography.Text type="secondary">角色：{roleText}</Typography.Text>
        </Space>
      </Card>
      <Card title="服务状态">
        <Space direction="vertical">
          <Button onClick={() => health.refetch()} loading={health.isFetching}>
            检查后端健康状态
          </Button>
          {health.data && (
            <Typography.Text type={health.data.success ? 'success' : 'danger'}>
              {health.data.success ? `${health.data.data.service}: ${health.data.data.status}` : health.data.error?.message}
            </Typography.Text>
          )}
        </Space>
      </Card>
      <Card title="阶段进度">
        <Space direction="vertical">
          <Typography.Text>审批申请、流程配置、通知提醒和审计日志已接入。</Typography.Text>
          <Typography.Text type="secondary">未读通知：{unread.data?.success ? unread.data.data.count : 0}</Typography.Text>
        </Space>
      </Card>
      <Card title="我的审批概况">
        <Space direction="vertical">
          <Typography.Text>我的待办：{summaryData?.myTodoCount || 0}</Typography.Text>
          <Typography.Text>进行中申请：{summaryData?.myInProgressCount || 0}</Typography.Text>
          <Typography.Text>未读通知：{summaryData?.unreadNotificationCount || 0}</Typography.Text>
        </Space>
      </Card>
      <Card title="管理概况">
        <Space direction="vertical">
          <Typography.Text>全局待办：{summaryData?.globalPendingCount || 0}</Typography.Text>
          <Typography.Text>今日提交：{summaryData?.todaySubmittedCount || 0}</Typography.Text>
        </Space>
      </Card>
    </div>
  );
}

function toTreeData(departments: Department[]): TreeDataNode[] {
  return departments.map((department) => ({
    key: String(department.id),
    title: `${department.name} (${department.code})`,
    children: toTreeData(department.children || [])
  }));
}

function flattenDepartments(departments: Department[]): Department[] {
  const result: Department[] = [];
  departments.forEach((department) => {
    result.push(department);
    result.push(...flattenDepartments(department.children || []));
  });
  return result;
}

function OrganizationPage() {
  const departments = useQuery({
    queryKey: ['departments'],
    queryFn: fetchDepartmentTree
  });
  const positions = useQuery({
    queryKey: ['positions'],
    queryFn: () => fetchPositions()
  });

  const departmentData = departments.data?.success ? departments.data.data : [];
  const positionData = positions.data?.success ? positions.data.data : [];
  const flatDepartments = flattenDepartments(departmentData);

  return (
    <Space direction="vertical" size={16} className="full-width">
      <Typography.Title level={3}>组织架构</Typography.Title>
      <div className="organization-grid">
        <Card title="部门树" loading={departments.isLoading}>
          <Tree treeData={toTreeData(departmentData)} defaultExpandAll />
        </Card>
        <Card title="岗位列表" loading={positions.isLoading}>
          <Table<Position>
            rowKey="id"
            dataSource={positionData}
            pagination={false}
            columns={[
              { title: '岗位编码', dataIndex: 'code' },
              { title: '岗位名称', dataIndex: 'name' },
              {
                title: '所属部门',
                dataIndex: 'departmentId',
                render: (departmentId: number) => flatDepartments.find((item) => item.id === departmentId)?.name || '-'
              },
              { title: '状态', dataIndex: 'enabled', render: (enabled: boolean) => (enabled ? '启用' : '停用') }
            ]}
          />
        </Card>
      </div>
    </Space>
  );
}

function PlaceholderPage({ title }: { title: string }) {
  return (
    <Card title={title}>
      <Typography.Text type="secondary">该模块尚未进入实现阶段。</Typography.Text>
    </Card>
  );
}

function UserManagementPage() {
  const [form] = Form.useForm<UserPayload>();
  const [modalOpen, setModalOpen] = useState(false);
  const [editingUser, setEditingUser] = useState<User | null>(null);

  const users = useQuery({ queryKey: ['users'], queryFn: fetchUsers });
  const roles = useQuery({ queryKey: ['roles'], queryFn: fetchRoles });
  const departments = useQuery({ queryKey: ['departments'], queryFn: fetchDepartmentTree });
  const positions = useQuery({ queryKey: ['positions'], queryFn: () => fetchPositions() });

  const userData = users.data?.success ? users.data.data : [];
  const roleData = roles.data?.success ? roles.data.data : [];
  const departmentData = departments.data?.success ? flattenDepartments(departments.data.data) : [];
  const positionData = positions.data?.success ? positions.data.data : [];

  const saveMutation = useMutation({
    mutationFn: (payload: UserPayload) => (editingUser ? updateUser(editingUser.id, payload) : createUser(payload)),
    onSuccess: (result) => {
      if (!result.success) {
        message.error(result.error?.message || '保存失败');
        return;
      }
      message.success('保存成功');
      setModalOpen(false);
      setEditingUser(null);
      form.resetFields();
      users.refetch();
    },
    onError: () => message.error('保存失败')
  });

  const statusMutation = useMutation({
    mutationFn: ({ id, status }: { id: number; status: 'active' | 'disabled' }) => setUserStatus(id, status),
    onSuccess: (result) => {
      if (!result.success) {
        message.error(result.error?.message || '状态更新失败');
        return;
      }
      users.refetch();
    },
    onError: () => message.error('状态更新失败')
  });

  const openCreate = () => {
    setEditingUser(null);
    form.resetFields();
    form.setFieldsValue({ roleIds: roleData.find((role) => role.code === 'employee') ? [roleData.find((role) => role.code === 'employee')!.id] : [] });
    setModalOpen(true);
  };

  const openEdit = (record: User) => {
    setEditingUser(record);
    form.setFieldsValue({
      username: record.username,
      displayName: record.displayName,
      employeeNo: record.employeeNo,
      email: record.email,
      phone: record.phone,
      departmentId: record.departmentId || undefined,
      positionId: record.positionId || undefined,
      directManagerId: record.directManagerId || undefined,
      roleIds: record.roles.map((role) => role.id)
    });
    setModalOpen(true);
  };

  return (
    <Space direction="vertical" size={16} className="full-width">
      <Space className="page-toolbar">
        <Typography.Title level={3}>用户管理</Typography.Title>
        <Button type="primary" onClick={openCreate}>新增用户</Button>
      </Space>
      <Card>
        <Table<User>
          rowKey="id"
          dataSource={userData}
          loading={users.isLoading}
          columns={[
            { title: '账号', dataIndex: 'username' },
            { title: '姓名', dataIndex: 'displayName' },
            { title: '员工号', dataIndex: 'employeeNo', render: (value) => value || '-' },
            { title: '部门', dataIndex: 'departmentName', render: (value) => value || '-' },
            { title: '岗位', dataIndex: 'positionName', render: (value) => value || '-' },
            { title: '角色', dataIndex: 'roles', render: (items: User['roles']) => items.map((role) => role.name).join(', ') || '-' },
            { title: '状态', dataIndex: 'status', render: (status) => (status === 'active' ? '启用' : '停用') },
            {
              title: '操作',
              render: (_, record) => (
                <Space>
                  <Button size="small" onClick={() => openEdit(record)}>编辑</Button>
                  <Button
                    size="small"
                    onClick={() => statusMutation.mutate({ id: record.id, status: record.status === 'active' ? 'disabled' : 'active' })}
                  >
                    {record.status === 'active' ? '停用' : '启用'}
                  </Button>
                </Space>
              )
            }
          ]}
        />
      </Card>
      <Modal
        title={editingUser ? '编辑用户' : '新增用户'}
        open={modalOpen}
        onCancel={() => setModalOpen(false)}
        onOk={() => form.submit()}
        confirmLoading={saveMutation.isPending}
        destroyOnHidden
      >
        <Form form={form} layout="vertical" onFinish={(values) => saveMutation.mutate(values)}>
          <Form.Item name="username" label="账号" rules={[{ required: true, message: '请输入账号' }]}>
            <Input />
          </Form.Item>
          <Form.Item name="displayName" label="姓名" rules={[{ required: true, message: '请输入姓名' }]}>
            <Input />
          </Form.Item>
          <Form.Item name="password" label={editingUser ? '新密码' : '初始密码'} rules={editingUser ? [] : [{ required: true, message: '请输入初始密码' }]}>
            <Input.Password />
          </Form.Item>
          <Form.Item name="employeeNo" label="员工号">
            <Input />
          </Form.Item>
          <Form.Item name="email" label="邮箱">
            <Input />
          </Form.Item>
          <Form.Item name="phone" label="手机号">
            <Input />
          </Form.Item>
          <Form.Item name="departmentId" label="部门">
            <Select allowClear options={departmentData.map((item) => ({ label: item.name, value: item.id }))} />
          </Form.Item>
          <Form.Item name="positionId" label="岗位">
            <Select allowClear options={positionData.map((item) => ({ label: item.name, value: item.id }))} />
          </Form.Item>
          <Form.Item name="directManagerId" label="直属上级">
            <Select allowClear options={userData.map((item) => ({ label: item.displayName, value: item.id }))} />
          </Form.Item>
          <Form.Item name="roleIds" label="角色">
            <Select mode="multiple" options={roleData.map((item) => ({ label: item.name, value: item.id }))} />
          </Form.Item>
        </Form>
      </Modal>
    </Space>
  );
}

function RolePermissionPage() {
  const [selectedRoleId, setSelectedRoleId] = useState<number | null>(null);
  const [checkedPermissionIds, setCheckedPermissionIds] = useState<number[]>([]);

  const roles = useQuery({ queryKey: ['rbacRoles'], queryFn: fetchRbacRoles });
  const permissions = useQuery({ queryKey: ['permissions'], queryFn: fetchPermissions });
  const rolePermissions = useQuery({
    queryKey: ['rolePermissions', selectedRoleId],
    queryFn: () => fetchRolePermissions(selectedRoleId!),
    enabled: Boolean(selectedRoleId)
  });

  useEffect(() => {
    if (!selectedRoleId && roles.data?.success && roles.data.data.length > 0) {
      setSelectedRoleId(roles.data.data[0].id);
    }
  }, [roles.data, selectedRoleId]);

  useEffect(() => {
    if (rolePermissions.data?.success) {
      setCheckedPermissionIds(rolePermissions.data.data.permissionIds);
    }
  }, [rolePermissions.data]);

  const saveMutation = useMutation({
    mutationFn: () => saveRolePermissions(selectedRoleId!, checkedPermissionIds),
    onSuccess: (result) => {
      if (!result.success) {
        message.error(result.error?.message || '保存失败');
        return;
      }
      message.success('保存成功');
      rolePermissions.refetch();
    },
    onError: () => message.error('保存失败')
  });

  const roleData = roles.data?.success ? roles.data.data : [];
  const permissionData = permissions.data?.success ? permissions.data.data : [];

  return (
    <Space direction="vertical" size={16} className="full-width">
      <Typography.Title level={3}>角色权限</Typography.Title>
      <div className="role-permission-grid">
        <Card title="角色">
          <Table
            rowKey="id"
            dataSource={roleData}
            pagination={false}
            loading={roles.isLoading}
            rowClassName={(record) => (record.id === selectedRoleId ? 'selected-row' : '')}
            onRow={(record) => ({ onClick: () => setSelectedRoleId(record.id) })}
            columns={[
              { title: '编码', dataIndex: 'code' },
              { title: '名称', dataIndex: 'name' }
            ]}
          />
        </Card>
        <Card
          title="权限点"
          extra={<Button type="primary" disabled={!selectedRoleId} loading={saveMutation.isPending} onClick={() => saveMutation.mutate()}>保存权限</Button>}
        >
          <Checkbox.Group value={checkedPermissionIds} onChange={(values) => setCheckedPermissionIds(values as number[])} className="permission-list">
            {permissionData.map((permission) => (
              <Checkbox key={permission.id} value={permission.id}>
                {permission.name} <Typography.Text type="secondary">({permission.code})</Typography.Text>
              </Checkbox>
            ))}
          </Checkbox.Group>
        </Card>
      </div>
    </Space>
  );
}

function buildApprovalPayload(values: any): ApprovalPayload {
  const formData: Record<string, unknown> = {};
  if (values.type === 'leave') {
    formData.leaveType = values.leaveType;
    formData.reason = values.reason;
  } else if (values.type === 'expense') {
    formData.expenseCategory = values.expenseCategory;
    formData.reason = values.reason;
  } else if (values.type === 'purchase') {
    formData.itemName = values.itemName;
    formData.reason = values.reason;
  } else if (values.type === 'overtime') {
    formData.reason = values.reason;
  } else if (values.type === 'business_trip') {
    formData.destination = values.destination;
    formData.reason = values.reason;
  }
  return {
    title: values.title,
    type: values.type,
    urgent: values.urgent,
    amount: values.amount,
    startTime: values.startTime ? values.startTime.toISOString() : undefined,
    endTime: values.endTime ? values.endTime.toISOString() : undefined,
    formData
  };
}

function NewApprovalPage() {
  const [form] = Form.useForm();
  const [selectedType, setSelectedType] = useState('leave');
  const [draftRequest, setDraftRequest] = useState<ApprovalRequest | null>(null);

  const draftMutation = useMutation({
    mutationFn: (payload: ApprovalPayload) => saveApprovalDraft(payload),
    onSuccess: (result) => {
      if (!result.success) {
        message.error(result.error?.message || '保存草稿失败');
        return;
      }
      setDraftRequest(result.data);
      message.success('草稿已保存');
    },
    onError: () => message.error('保存草稿失败')
  });

  const submitMutation = useMutation({
    mutationFn: (payload: ApprovalPayload) => submitApproval(payload),
    onSuccess: (result) => {
      if (!result.success) {
        message.error(result.error?.message || '提交失败');
        return;
      }
      message.success('申请已提交');
      setDraftRequest(null);
      form.resetFields();
    },
    onError: () => message.error('提交失败')
  });

  const submitDraftMutation = useMutation({
    mutationFn: ({ id, payload }: { id: number; payload: ApprovalPayload }) => submitApprovalDraft(id, payload),
    onSuccess: (result) => {
      if (!result.success) {
        message.error(result.error?.message || '提交失败');
        return;
      }
      message.success('申请已提交');
      setDraftRequest(null);
      form.resetFields();
    },
    onError: () => message.error('提交失败')
  });

  const uploadDraftAttachmentMutation = useMutation({
    mutationFn: ({ requestId, file }: { requestId: number; file: File }) => uploadApprovalAttachment(requestId, file),
    onSuccess: (result) => {
      if (!result.success) {
        message.error(result.error?.message || '附件上传失败');
        return;
      }
      setDraftRequest((current) => current ? { ...current, attachments: [...(current.attachments || []), result.data] } : current);
      message.success('附件已上传');
    },
    onError: () => message.error('附件上传失败')
  });

  const deleteDraftAttachmentMutation = useMutation({
    mutationFn: ({ requestId, attachmentId }: { requestId: number; attachmentId: number }) => deleteApprovalAttachment(requestId, attachmentId),
    onSuccess: (result, variables) => {
      if (!result.success) {
        message.error(result.error?.message || '附件删除失败');
        return;
      }
      setDraftRequest((current) => current ? { ...current, attachments: (current.attachments || []).filter((item) => item.id !== variables.attachmentId) } : current);
      message.success('附件已删除');
    },
    onError: () => message.error('附件删除失败')
  });

  const renderSpecificFields = () => {
    if (selectedType === 'leave') {
      return <Form.Item name="leaveType" label="请假类型" rules={[{ required: true, message: '请选择请假类型' }]}><Select options={[{ value: 'annual', label: '年假' }, { value: 'sick', label: '病假' }, { value: 'personal', label: '事假' }]} /></Form.Item>;
    }
    if (selectedType === 'expense') {
      return <Form.Item name="expenseCategory" label="报销类别" rules={[{ required: true, message: '请选择报销类别' }]}><Select options={[{ value: 'travel', label: '差旅' }, { value: 'office', label: '办公' }, { value: 'other', label: '其他' }]} /></Form.Item>;
    }
    if (selectedType === 'purchase') {
      return <Form.Item name="itemName" label="采购物品" rules={[{ required: true, message: '请输入采购物品' }]}><Input /></Form.Item>;
    }
    if (selectedType === 'business_trip') {
      return <Form.Item name="destination" label="出差地点" rules={[{ required: true, message: '请输入出差地点' }]}><Input /></Form.Item>;
    }
    return null;
  };

  const submitForm = (mode: 'draft' | 'submit') => {
    form.validateFields().then((values) => {
      const payload = buildApprovalPayload(values);
      if (mode === 'draft') {
        draftMutation.mutate(payload);
      } else if (draftRequest) {
        if (approvalRequiresAttachment(payload.type) && (draftRequest.attachments || []).length === 0) {
          message.error('请先上传附件');
          return;
        }
        submitDraftMutation.mutate({ id: draftRequest.id, payload });
      } else if (approvalRequiresAttachment(payload.type)) {
        draftMutation.mutate(payload, {
          onSuccess: (result) => {
            if (result.success) {
              message.info('请上传附件后提交');
            }
          }
        });
      } else {
        submitMutation.mutate(payload);
      }
    });
  };

  return (
    <Card title="发起申请">
      <Form
        form={form}
        layout="vertical"
        initialValues={{ type: 'leave', urgent: false }}
        onValuesChange={(changed) => {
          if (changed.type) {
            setSelectedType(changed.type);
            setDraftRequest(null);
          }
        }}
      >
        <Form.Item name="type" label="审批类型" rules={[{ required: true, message: '请选择审批类型' }]}>
          <Select options={approvalTypeOptions} />
        </Form.Item>
        <Form.Item name="title" label="申请标题" rules={[{ required: true, message: '请输入申请标题' }]}>
          <Input />
        </Form.Item>
        <Form.Item name="urgent" label="紧急程度" valuePropName="checked">
          <Switch checkedChildren="紧急" unCheckedChildren="普通" />
        </Form.Item>
        {(selectedType === 'expense' || selectedType === 'purchase') && (
          <Form.Item name="amount" label="金额" rules={[{ required: true, message: '请输入金额' }]}>
            <InputNumber min={0.01} precision={2} className="full-width" />
          </Form.Item>
        )}
        {(selectedType === 'leave' || selectedType === 'overtime' || selectedType === 'business_trip') && (
          <div className="two-column-form">
            <Form.Item name="startTime" label="开始时间" rules={[{ required: true, message: '请选择开始时间' }]}>
              <DatePicker showTime className="full-width" />
            </Form.Item>
            <Form.Item name="endTime" label="结束时间" rules={[{ required: true, message: '请选择结束时间' }]}>
              <DatePicker showTime className="full-width" />
            </Form.Item>
          </div>
        )}
        {renderSpecificFields()}
        <Form.Item name="reason" label="申请原因" rules={[{ required: true, message: '请输入申请原因' }]}>
          <Input.TextArea rows={4} />
        </Form.Item>
        {approvalRequiresAttachment(selectedType) && (
          <Space direction="vertical" size={8} className="full-width">
            <Space className="page-toolbar">
              <Typography.Title level={5}>附件</Typography.Title>
              <Upload
                showUploadList={false}
                disabled={!draftRequest}
                customRequest={(options) => {
                  if (!draftRequest) {
                    options.onError?.(new Error('draft required'));
                    return;
                  }
                  uploadDraftAttachmentMutation.mutate(
                    { requestId: draftRequest.id, file: options.file as File },
                    {
                      onSuccess: () => options.onSuccess?.({}),
                      onError: () => options.onError?.(new Error('upload failed'))
                    }
                  );
                }}
              >
                <Button icon={<UploadOutlined />} disabled={!draftRequest} loading={uploadDraftAttachmentMutation.isPending}>上传附件</Button>
              </Upload>
            </Space>
            <Table
              rowKey="id"
              size="small"
              pagination={false}
              dataSource={draftRequest?.attachments || []}
              columns={[
                { title: '文件名', dataIndex: 'fileName' },
                { title: '大小', dataIndex: 'fileSize', render: (value: number) => `${(value / 1024).toFixed(1)} KB` },
                {
                  title: '操作',
                  render: (_, record) => (
                    <Space>
                      <Button size="small" icon={<DownloadOutlined />} onClick={() => downloadApprovalAttachment(draftRequest!.id, record.id, record.fileName)} />
                      <Button size="small" danger icon={<DeleteOutlined />} loading={deleteDraftAttachmentMutation.isPending} onClick={() => deleteDraftAttachmentMutation.mutate({ requestId: draftRequest!.id, attachmentId: record.id })} />
                    </Space>
                  )
                }
              ]}
            />
          </Space>
        )}
        <Space>
          <Button onClick={() => submitForm('draft')} loading={draftMutation.isPending}>保存草稿</Button>
          <Button type="primary" onClick={() => submitForm('submit')} loading={submitMutation.isPending || submitDraftMutation.isPending}>提交申请</Button>
        </Space>
      </Form>
    </Card>
  );
}

function MyApprovalsPage() {
  const [detailId, setDetailId] = useState<number | null>(null);
  const list = useQuery({ queryKey: ['myApprovals'], queryFn: fetchMyApprovals });
  const detail = useQuery({
    queryKey: ['approvalDetail', detailId],
    queryFn: () => fetchApprovalDetail(detailId!),
    enabled: Boolean(detailId)
  });
  const data = list.data?.success ? list.data.data : [];
  const detailData = detail.data?.success ? detail.data.data : null;

  const uploadMutation = useMutation({
    mutationFn: ({ requestId, file }: { requestId: number; file: File }) => uploadApprovalAttachment(requestId, file),
    onSuccess: (result) => {
      if (!result.success) {
        message.error(result.error?.message || '附件上传失败');
        return;
      }
      message.success('附件已上传');
      detail.refetch();
      list.refetch();
    },
    onError: () => message.error('附件上传失败')
  });

  const deleteMutation = useMutation({
    mutationFn: ({ requestId, attachmentId }: { requestId: number; attachmentId: number }) => deleteApprovalAttachment(requestId, attachmentId),
    onSuccess: (result) => {
      if (!result.success) {
        message.error(result.error?.message || '附件删除失败');
        return;
      }
      message.success('附件已删除');
      detail.refetch();
      list.refetch();
    },
    onError: () => message.error('附件删除失败')
  });

  const withdrawMutation = useMutation({
    mutationFn: (request: ApprovalRequest) => withdrawApproval(request.id, '申请人撤回'),
    onSuccess: (result) => {
      if (!result.success) {
        message.error(result.error?.message || '撤回失败');
        return;
      }
      message.success('已撤回');
      detail.refetch();
      list.refetch();
    },
    onError: () => message.error('撤回失败')
  });

  const resubmitMutation = useMutation({
    mutationFn: (request: ApprovalRequest) => resubmitMoreInfoApproval(request.id, {
      title: request.title,
      type: request.type,
      urgent: request.urgent,
      amount: request.amount,
      startTime: request.startTime,
      endTime: request.endTime,
      formData: request.formData
    }),
    onSuccess: (result) => {
      if (!result.success) {
        message.error(result.error?.message || '重新提交失败');
        return;
      }
      message.success('已重新提交');
      detail.refetch();
      list.refetch();
    },
    onError: () => message.error('重新提交失败')
  });

  return (
    <Space direction="vertical" size={16} className="full-width">
      <Typography.Title level={3}>我的申请</Typography.Title>
      <Card>
        <Table<ApprovalRequest>
          rowKey="id"
          dataSource={data}
          loading={list.isLoading}
          columns={[
            { title: '单号', dataIndex: 'requestNo' },
            { title: '标题', dataIndex: 'title' },
            { title: '类型', dataIndex: 'type', render: approvalTypeName },
            { title: '状态', dataIndex: 'status', render: statusName },
            { title: '金额', dataIndex: 'amount', render: (value) => value || '-' },
            { title: '创建时间', dataIndex: 'createdAt', render: (value) => value ? new Date(value).toLocaleString() : '-' },
            {
              title: '操作',
              render: (_, record) => (
                <Space>
                  <Button size="small" onClick={() => setDetailId(record.id)}>详情</Button>
                  {record.status === 'in_progress' && <Button size="small" danger loading={withdrawMutation.isPending} onClick={() => withdrawMutation.mutate(record)}>撤回</Button>}
                  {record.status === 'need_more_info' && <Button size="small" type="primary" loading={resubmitMutation.isPending} onClick={() => resubmitMutation.mutate(record)}>补充后提交</Button>}
                </Space>
              )
            }
          ]}
        />
      </Card>
      <Modal title="申请详情" open={Boolean(detailId)} onCancel={() => setDetailId(null)} footer={null} width={720}>
        {detailData && (
          <Space direction="vertical" size={12} className="full-width">
            <Typography.Text>单号：{detailData.requestNo}</Typography.Text>
            <Typography.Text>标题：{detailData.title}</Typography.Text>
            <Typography.Text>类型：{approvalTypeName(detailData.type)}</Typography.Text>
            <Typography.Text>状态：{statusName(detailData.status)}</Typography.Text>
            <Typography.Text>申请人：{detailData.applicantName}</Typography.Text>
            <Typography.Text>表单：{JSON.stringify(detailData.formData)}</Typography.Text>
            <Space className="page-toolbar">
              <Typography.Title level={5}>附件</Typography.Title>
              <Upload
                showUploadList={false}
                customRequest={(options) => {
                  uploadMutation.mutate(
                    { requestId: detailData.id, file: options.file as File },
                    {
                      onSuccess: () => options.onSuccess?.({}),
                      onError: () => options.onError?.(new Error('upload failed'))
                    }
                  );
                }}
              >
                <Button icon={<UploadOutlined />} loading={uploadMutation.isPending}>上传附件</Button>
              </Upload>
            </Space>
            <Table
              rowKey="id"
              size="small"
              pagination={false}
              dataSource={detailData.attachments || []}
              columns={[
                { title: '文件名', dataIndex: 'fileName' },
                { title: '大小', dataIndex: 'fileSize', render: (value: number) => `${(value / 1024).toFixed(1)} KB` },
                { title: '上传时间', dataIndex: 'createdAt', render: (value: string) => value ? new Date(value).toLocaleString() : '-' },
                {
                  title: '操作',
                  render: (_, record) => (
                    <Space>
                      <Button size="small" icon={<DownloadOutlined />} onClick={() => downloadApprovalAttachment(detailData.id, record.id, record.fileName)} />
                      <Button size="small" danger icon={<DeleteOutlined />} loading={deleteMutation.isPending} onClick={() => deleteMutation.mutate({ requestId: detailData.id, attachmentId: record.id })} />
                    </Space>
                  )
                }
              ]}
            />
            <Typography.Title level={5}>审批流程</Typography.Title>
            <Table
              rowKey="id"
              size="small"
              pagination={false}
              dataSource={detailData.timelineNodes || []}
              columns={[
                { title: '节点', dataIndex: 'nodeName' },
                { title: '审批人', dataIndex: 'approverName' },
                { title: '状态', dataIndex: 'status', render: nodeStatusName },
                { title: '意见', dataIndex: 'comment', render: (value) => value || '-' },
                { title: '处理时间', dataIndex: 'actedAt', render: (value: string) => value ? new Date(value).toLocaleString() : '-' }
              ]}
            />
            <Typography.Title level={5}>动作记录</Typography.Title>
            {(detailData.actionLogs || []).map((log) => (
              <Typography.Text key={log.id}>{new Date(log.createdAt).toLocaleString()} {log.actorName} {log.action}</Typography.Text>
            ))}
          </Space>
        )}
      </Modal>
    </Space>
  );
}

function ApprovalTasksPage({ mode }: { mode: 'todo' | 'done' }) {
  const [rejectingTask, setRejectingTask] = useState<ApprovalTask | null>(null);
  const [actionTask, setActionTask] = useState<ApprovalTask | null>(null);
  const [actionType, setActionType] = useState<'more_info' | 'transfer' | 'add_signer' | 'cc' | null>(null);
  const [detailId, setDetailId] = useState<number | null>(null);
  const [rejectForm] = Form.useForm<{ comment: string }>();
  const [actionForm] = Form.useForm<{ targetUserId?: number; targetUserIds?: number[]; comment?: string }>();
  const tasks = useQuery({
    queryKey: ['approvalTasks', mode],
    queryFn: mode === 'todo' ? fetchApprovalTodoTasks : fetchApprovalDoneTasks
  });
  const users = useQuery({ queryKey: ['approvalActionUsers'], queryFn: fetchUsers });
  const userOptions = (users.data?.success ? users.data.data : []).map((user) => ({ value: user.id, label: `${user.displayName}（${user.username}）` }));
  const data = tasks.data?.success ? tasks.data.data : [];
  const detail = useQuery({
    queryKey: ['approvalDetailFromTask', detailId],
    queryFn: () => fetchApprovalDetail(detailId!),
    enabled: Boolean(detailId)
  });
  const detailData = detail.data?.success ? detail.data.data : null;

  const approveMutation = useMutation({
    mutationFn: (task: ApprovalTask) => approveApprovalTask(task.id, '同意'),
    onSuccess: (result) => {
      if (!result.success) {
        message.error(result.error?.message || '审批失败');
        return;
      }
      message.success('已同意');
      tasks.refetch();
    },
    onError: () => message.error('审批失败')
  });

  const rejectMutation = useMutation({
    mutationFn: ({ task, comment }: { task: ApprovalTask; comment: string }) => rejectApprovalTask(task.id, comment),
    onSuccess: (result) => {
      if (!result.success) {
        message.error(result.error?.message || '驳回失败');
        return;
      }
      message.success('已驳回');
      setRejectingTask(null);
      rejectForm.resetFields();
      tasks.refetch();
    },
    onError: () => message.error('驳回失败')
  });

  const actionMutation = useMutation({
    mutationFn: ({ task, values }: { task: ApprovalTask; values: { targetUserId?: number; targetUserIds?: number[]; comment?: string } }) => {
      if (actionType === 'more_info') {
        return requestMoreInfoApprovalTask(task.id, values.comment || '');
      }
      if (actionType === 'transfer') {
        return transferApprovalTask(task.id, values.targetUserId!, values.comment);
      }
      if (actionType === 'add_signer') {
        return addSignerApprovalTask(task.id, values.targetUserId!, values.comment);
      }
      return ccApprovalTask(task.id, values.targetUserIds || [], values.comment);
    },
    onSuccess: (result) => {
      if (!result.success) {
        message.error(result.error?.message || '操作失败');
        return;
      }
      message.success('操作成功');
      setActionTask(null);
      setActionType(null);
      actionForm.resetFields();
      tasks.refetch();
    },
    onError: () => message.error('操作失败')
  });

  const openAction = (type: 'more_info' | 'transfer' | 'add_signer' | 'cc', task: ApprovalTask) => {
    setActionType(type);
    setActionTask(task);
    actionForm.resetFields();
  };

  const actionTitle = actionType === 'more_info' ? '要求补充材料' : actionType === 'transfer' ? '转交审批' : actionType === 'add_signer' ? '加签审批' : '抄送';

  return (
    <Space direction="vertical" size={16} className="full-width">
      <Typography.Title level={3}>{mode === 'todo' ? '我的待办' : '我的已办'}</Typography.Title>
      <Card>
        <Table<ApprovalTask>
          rowKey="id"
          dataSource={data}
          loading={tasks.isLoading}
          columns={[
            { title: '单号', dataIndex: 'requestNo' },
            { title: '标题', dataIndex: 'title' },
            { title: '类型', dataIndex: 'type', render: approvalTypeName },
            { title: '节点', dataIndex: 'nodeName', render: (value) => value || '-' },
            { title: '申请人', dataIndex: 'applicantName' },
            { title: '任务状态', dataIndex: 'status', render: taskStatusName },
            { title: mode === 'todo' ? '到达时间' : '处理时间', dataIndex: mode === 'todo' ? 'assignedAt' : 'actedAt', render: (value) => value ? new Date(value).toLocaleString() : '-' },
            ...(mode === 'todo' ? [{
              title: '操作',
              render: (_: unknown, record: ApprovalTask) => (
                <Space>
                  <Button size="small" onClick={() => setDetailId(record.requestId)}>详情</Button>
                  <Button size="small" type="primary" loading={approveMutation.isPending} onClick={() => approveMutation.mutate(record)}>同意</Button>
                  <Button size="small" danger onClick={() => setRejectingTask(record)}>驳回</Button>
                  <Button size="small" onClick={() => openAction('more_info', record)}>补充材料</Button>
                  <Button size="small" onClick={() => openAction('transfer', record)}>转交</Button>
                  <Button size="small" onClick={() => openAction('add_signer', record)}>加签</Button>
                  <Button size="small" onClick={() => openAction('cc', record)}>抄送</Button>
                </Space>
              )
            }] : [{
              title: '操作',
              render: (_: unknown, record: ApprovalTask) => <Button size="small" onClick={() => setDetailId(record.requestId)}>详情</Button>
            }])
          ]}
        />
      </Card>
      <Modal
        title="驳回申请"
        open={Boolean(rejectingTask)}
        onCancel={() => setRejectingTask(null)}
        onOk={() => rejectForm.submit()}
        confirmLoading={rejectMutation.isPending}
        destroyOnHidden
      >
        <Form form={rejectForm} layout="vertical" onFinish={(values) => rejectingTask && rejectMutation.mutate({ task: rejectingTask, comment: values.comment })}>
          <Form.Item name="comment" label="驳回原因" rules={[{ required: true, message: '请输入驳回原因' }]}>
            <Input.TextArea rows={4} />
          </Form.Item>
        </Form>
      </Modal>
      <Modal
        title={actionTitle}
        open={Boolean(actionTask)}
        onCancel={() => {
          setActionTask(null);
          setActionType(null);
        }}
        onOk={() => actionForm.submit()}
        confirmLoading={actionMutation.isPending}
        destroyOnHidden
      >
        <Form form={actionForm} layout="vertical" onFinish={(values) => actionTask && actionMutation.mutate({ task: actionTask, values })}>
          {(actionType === 'transfer' || actionType === 'add_signer') && (
            <Form.Item name="targetUserId" label="目标审批人" rules={[{ required: true, message: '请选择目标审批人' }]}>
              <Select options={userOptions} showSearch optionFilterProp="label" />
            </Form.Item>
          )}
          {actionType === 'cc' && (
            <Form.Item name="targetUserIds" label="抄送人" rules={[{ required: true, message: '请选择抄送人' }]}>
              <Select mode="multiple" options={userOptions} showSearch optionFilterProp="label" />
            </Form.Item>
          )}
          <Form.Item name="comment" label={actionType === 'more_info' ? '补充要求' : '说明'} rules={actionType === 'more_info' ? [{ required: true, message: '请输入补充要求' }] : []}>
            <Input.TextArea rows={4} />
          </Form.Item>
        </Form>
      </Modal>
      <Modal title="审批详情" open={Boolean(detailId)} onCancel={() => setDetailId(null)} footer={null} width={760}>
        {detailData && (
          <Space direction="vertical" size={12} className="full-width">
            <Typography.Text>单号：{detailData.requestNo}</Typography.Text>
            <Typography.Text>标题：{detailData.title}</Typography.Text>
            <Typography.Text>类型：{approvalTypeName(detailData.type)}</Typography.Text>
            <Typography.Text>状态：{statusName(detailData.status)}</Typography.Text>
            <Typography.Text>申请人：{detailData.applicantName}</Typography.Text>
            <Typography.Text>表单：{JSON.stringify(detailData.formData)}</Typography.Text>
            <Typography.Title level={5}>审批流程</Typography.Title>
            <Table
              rowKey="id"
              size="small"
              pagination={false}
              dataSource={detailData.timelineNodes || []}
              columns={[
                { title: '节点', dataIndex: 'nodeName' },
                { title: '审批人', dataIndex: 'approverName' },
                { title: '状态', dataIndex: 'status', render: nodeStatusName },
                { title: '意见', dataIndex: 'comment', render: (value) => value || '-' },
                { title: '处理时间', dataIndex: 'actedAt', render: (value: string) => value ? new Date(value).toLocaleString() : '-' }
              ]}
            />
          </Space>
        )}
      </Modal>
    </Space>
  );
}

function ApprovalCcPage() {
  const [detailId, setDetailId] = useState<number | null>(null);
  const list = useQuery({ queryKey: ['approvalCc'], queryFn: fetchApprovalCcTasks });
  const detail = useQuery({
    queryKey: ['approvalCcDetail', detailId],
    queryFn: () => fetchApprovalDetail(detailId!),
    enabled: Boolean(detailId)
  });
  const data = list.data?.success ? list.data.data : [];
  const detailData = detail.data?.success ? detail.data.data : null;

  return (
    <Space direction="vertical" size={16} className="full-width">
      <Typography.Title level={3}>抄送我的</Typography.Title>
      <Card>
        <Table<ApprovalCc>
          rowKey="id"
          dataSource={data}
          loading={list.isLoading}
          columns={[
            { title: '单号', dataIndex: 'requestNo' },
            { title: '标题', dataIndex: 'title' },
            { title: '类型', dataIndex: 'type', render: approvalTypeName },
            { title: '申请人', dataIndex: 'applicantName' },
            { title: '状态', dataIndex: 'requestStatus', render: statusName },
            { title: '说明', dataIndex: 'comment', render: (value) => value || '-' },
            { title: '抄送时间', dataIndex: 'createdAt', render: (value) => value ? new Date(value).toLocaleString() : '-' },
            { title: '操作', render: (_, record) => <Button size="small" onClick={() => setDetailId(record.requestId)}>详情</Button> }
          ]}
        />
      </Card>
      <Modal title="审批详情" open={Boolean(detailId)} onCancel={() => setDetailId(null)} footer={null} width={760}>
        {detailData && (
          <Space direction="vertical" size={12} className="full-width">
            <Typography.Text>单号：{detailData.requestNo}</Typography.Text>
            <Typography.Text>标题：{detailData.title}</Typography.Text>
            <Typography.Text>类型：{approvalTypeName(detailData.type)}</Typography.Text>
            <Typography.Text>状态：{statusName(detailData.status)}</Typography.Text>
            <Typography.Text>申请人：{detailData.applicantName}</Typography.Text>
            <Typography.Text>表单：{JSON.stringify(detailData.formData)}</Typography.Text>
            <Typography.Title level={5}>审批流程</Typography.Title>
            <Table
              rowKey="id"
              size="small"
              pagination={false}
              dataSource={detailData.timelineNodes || []}
              columns={[
                { title: '节点', dataIndex: 'nodeName' },
                { title: '审批人', dataIndex: 'approverName' },
                { title: '状态', dataIndex: 'status', render: nodeStatusName },
                { title: '意见', dataIndex: 'comment', render: (value) => value || '-' },
                { title: '处理时间', dataIndex: 'actedAt', render: (value: string) => value ? new Date(value).toLocaleString() : '-' }
              ]}
            />
          </Space>
        )}
      </Modal>
    </Space>
  );
}

const approverRuleOptions = [
  { value: 'direct_manager', label: '直属主管' },
  { value: 'finance', label: '财务人员' },
  { value: 'general_manager', label: '总经理' },
  { value: 'specified_user', label: '指定人员' }
];

function approverRuleName(rule: string) {
  return approverRuleOptions.find((item) => item.value === rule)?.label || rule;
}

function WorkflowConfigPage() {
  const [editing, setEditing] = useState<WorkflowTemplate | null>(null);
  const [modalOpen, setModalOpen] = useState(false);
  const [form] = Form.useForm<WorkflowTemplatePayload>();
  const templates = useQuery({ queryKey: ['workflowTemplates'], queryFn: fetchWorkflowTemplates });
  const audits = useQuery({ queryKey: ['workflowConfigAudits'], queryFn: fetchWorkflowConfigAudits });
  const users = useQuery({ queryKey: ['workflowUsers'], queryFn: fetchUsers });
  const data = templates.data?.success ? templates.data.data : [];
  const auditData = audits.data?.success ? audits.data.data : [];
  const userOptions = (users.data?.success ? users.data.data : []).map((user) => ({ value: user.id, label: `${user.displayName}（${user.username}）` }));

  const saveMutation = useMutation({
    mutationFn: (values: WorkflowTemplatePayload) => {
      const payload = {
        ...values,
        nodes: values.nodes.map((node, index) => ({
          ...node,
          sortOrder: node.sortOrder || index + 1
        }))
      };
      return editing ? updateWorkflowTemplate(editing.id, payload) : createWorkflowTemplate(payload);
    },
    onSuccess: (result) => {
      if (!result.success) {
        message.error(result.error?.message || '保存失败');
        return;
      }
      message.success('流程配置已保存');
      setModalOpen(false);
      setEditing(null);
      form.resetFields();
      templates.refetch();
      audits.refetch();
    },
    onError: () => message.error('保存失败')
  });

  const enableMutation = useMutation({
    mutationFn: ({ id, enabled }: { id: number; enabled: boolean }) => setWorkflowTemplateEnabled(id, enabled),
    onSuccess: (result) => {
      if (!result.success) {
        message.error(result.error?.message || '状态更新失败');
        return;
      }
      message.success('状态已更新');
      templates.refetch();
      audits.refetch();
    },
    onError: () => message.error('状态更新失败')
  });

  const openCreate = () => {
    setEditing(null);
    form.setFieldsValue({
      approvalType: 'leave',
      name: '新审批流程',
      enabled: true,
      nodes: [{ nodeName: '主管审批', approverRule: 'direct_manager', sortOrder: 1 }]
    });
    setModalOpen(true);
  };

  const openEdit = (template: WorkflowTemplate) => {
    setEditing(template);
    form.setFieldsValue({
      approvalType: template.approvalType,
      name: template.name,
      enabled: template.enabled,
      nodes: template.nodes
    });
    setModalOpen(true);
  };

  return (
    <Space direction="vertical" size={16} className="full-width">
      <Space className="page-toolbar">
        <Typography.Title level={3}>流程配置</Typography.Title>
        <Button type="primary" onClick={openCreate}>新增流程</Button>
      </Space>
      <Card>
        <Table<WorkflowTemplate>
          rowKey="id"
          dataSource={data}
          loading={templates.isLoading}
          expandable={{
            expandedRowRender: (record) => (
              <Table
                rowKey={(node) => `${record.id}-${node.sortOrder}-${node.nodeName}`}
                size="small"
                pagination={false}
                dataSource={record.nodes}
                columns={[
                  { title: '顺序', dataIndex: 'sortOrder', width: 80 },
                  { title: '节点', dataIndex: 'nodeName' },
                  { title: '审批人规则', dataIndex: 'approverRule', render: approverRuleName },
                  { title: '指定人员', dataIndex: 'approverUserId', render: (value) => userOptions.find((item) => item.value === value)?.label || '-' },
                  { title: '金额条件', dataIndex: 'amountGreaterThan', render: (value) => value ? `金额 > ${value}` : '-' }
                ]}
              />
            )
          }}
          columns={[
            { title: '审批类型', dataIndex: 'approvalType', render: approvalTypeName },
            { title: '流程名称', dataIndex: 'name' },
            { title: '状态', dataIndex: 'enabled', render: (value) => value ? '启用' : '停用' },
            { title: '更新时间', dataIndex: 'updatedAt', render: (value) => value ? new Date(value).toLocaleString() : '-' },
            {
              title: '操作',
              render: (_, record) => (
                <Space>
                  <Button size="small" onClick={() => openEdit(record)}>编辑</Button>
                  <Button size="small" danger={record.enabled} loading={enableMutation.isPending} onClick={() => enableMutation.mutate({ id: record.id, enabled: !record.enabled })}>
                    {record.enabled ? '停用' : '启用'}
                  </Button>
                </Space>
              )
            }
          ]}
        />
      </Card>
      <Card title="配置审计">
        <Table
          rowKey="id"
          size="small"
          pagination={{ pageSize: 5 }}
          dataSource={auditData}
          loading={audits.isLoading}
          columns={[
            { title: '时间', dataIndex: 'createdAt', render: (value) => value ? new Date(value).toLocaleString() : '-' },
            { title: '操作人', dataIndex: 'actorName' },
            { title: '动作', dataIndex: 'action' },
            { title: '说明', dataIndex: 'detail', render: (value) => value || '-' }
          ]}
        />
      </Card>
      <Modal
        title={editing ? '编辑流程' : '新增流程'}
        open={modalOpen}
        onCancel={() => setModalOpen(false)}
        onOk={() => form.submit()}
        width={860}
        confirmLoading={saveMutation.isPending}
        destroyOnHidden
      >
        <Form form={form} layout="vertical" onFinish={(values) => saveMutation.mutate(values)}>
          <Space size={16} className="full-width">
            <Form.Item name="approvalType" label="审批类型" rules={[{ required: true, message: '请选择审批类型' }]} className="flex-1">
              <Select options={approvalTypeOptions} />
            </Form.Item>
            <Form.Item name="name" label="流程名称" rules={[{ required: true, message: '请输入流程名称' }]} className="flex-1">
              <Input />
            </Form.Item>
            <Form.Item name="enabled" label="启用" valuePropName="checked">
              <Switch />
            </Form.Item>
          </Space>
          <Form.List name="nodes">
            {(fields, { add, remove }) => (
              <Space direction="vertical" className="full-width">
                {fields.map((field, index) => (
                  <Card key={field.key} size="small">
                    <Space align="start" className="full-width">
                      <Form.Item name={[field.name, 'sortOrder']} label="顺序" rules={[{ required: true, message: '请输入顺序' }]}>
                        <InputNumber min={1} />
                      </Form.Item>
                      <Form.Item name={[field.name, 'nodeName']} label="节点名称" rules={[{ required: true, message: '请输入节点名称' }]} className="flex-1">
                        <Input />
                      </Form.Item>
                      <Form.Item name={[field.name, 'approverRule']} label="审批人规则" rules={[{ required: true, message: '请选择审批人规则' }]} className="flex-1">
                        <Select options={approverRuleOptions} />
                      </Form.Item>
                      <Form.Item name={[field.name, 'approverUserId']} label="指定人员" className="flex-1">
                        <Select options={userOptions} allowClear showSearch optionFilterProp="label" />
                      </Form.Item>
                      <Form.Item name={[field.name, 'amountGreaterThan']} label="金额大于">
                        <InputNumber min={0} />
                      </Form.Item>
                      <Button danger disabled={fields.length === 1} onClick={() => remove(field.name)}>删除</Button>
                    </Space>
                  </Card>
                ))}
                <Button onClick={() => add({ nodeName: '审批节点', approverRule: 'direct_manager', sortOrder: fields.length + 1 })}>新增节点</Button>
              </Space>
            )}
          </Form.List>
        </Form>
      </Modal>
    </Space>
  );
}

const approvalStatusOptions = [
  { value: 'draft', label: '草稿' },
  { value: 'in_progress', label: '审批中' },
  { value: 'approved', label: '已通过' },
  { value: 'rejected', label: '已驳回' },
  { value: 'withdrawn', label: '已撤回' },
  { value: 'need_more_info', label: '待补充' },
  { value: 'voided', label: '已作废' }
];

function ApprovalManagementPage() {
  const [filters, setFilters] = useState<ApprovalManagementQuery>({ page: 1, pageSize: 20 });
  const [detailId, setDetailId] = useState<number | null>(null);
  const [form] = Form.useForm();
  const list = useQuery({
    queryKey: ['approvalManagement', filters],
    queryFn: () => fetchApprovalManagement(filters)
  });
  const statistics = useQuery({ queryKey: ['approvalStatistics'], queryFn: fetchApprovalStatistics });
  const departments = useQuery({ queryKey: ['manageDepartments'], queryFn: fetchDepartmentTree });
  const detail = useQuery({
    queryKey: ['approvalManagementDetail', detailId],
    queryFn: () => fetchApprovalDetail(detailId!),
    enabled: Boolean(detailId)
  });
  const paged = list.data?.success ? list.data.data : null;
  const data = paged?.items || [];
  const totalCount = paged?.totalCount || 0;
  const currentPage = paged?.page || filters.page || 1;
  const currentPageSize = paged?.pageSize || filters.pageSize || 20;
  const stats = statistics.data?.success ? statistics.data.data : null;
  const departmentOptions = flattenDepartments(departments.data?.success ? departments.data.data : []).map((department) => ({ value: department.id, label: department.name }));
  const detailData = detail.data?.success ? detail.data.data : null;

  const buildQuery = (values: any): ApprovalManagementQuery => ({
    type: values.type,
    status: values.status,
    applicantKeyword: values.applicantKeyword,
    departmentId: values.departmentId,
    startCreatedAt: values.startCreatedAt ? values.startCreatedAt.toISOString() : undefined,
    endCreatedAt: values.endCreatedAt ? values.endCreatedAt.toISOString() : undefined,
    minAmount: values.minAmount,
    maxAmount: values.maxAmount,
    urgent: values.urgent,
    page: 1,
    pageSize: currentPageSize
  });

  const exportMutation = useMutation({
    mutationFn: () => exportApprovalManagement(filters),
    onSuccess: () => message.success('已导出 CSV'),
    onError: () => message.error('导出失败')
  });

  return (
    <Space direction="vertical" size={16} className="full-width">
      <Space className="page-toolbar">
        <Typography.Title level={3}>审批管理</Typography.Title>
        <Button onClick={() => exportMutation.mutate()} loading={exportMutation.isPending}>导出 CSV</Button>
      </Space>
      <Card>
        <Form form={form} layout="inline" onFinish={(values) => setFilters(buildQuery(values))}>
          <Form.Item name="type" label="类型">
            <Select allowClear options={approvalTypeOptions} style={{ width: 140 }} />
          </Form.Item>
          <Form.Item name="status" label="状态">
            <Select allowClear options={approvalStatusOptions} style={{ width: 130 }} />
          </Form.Item>
          <Form.Item name="applicantKeyword" label="申请人">
            <Input allowClear style={{ width: 140 }} />
          </Form.Item>
          <Form.Item name="departmentId" label="部门">
            <Select allowClear options={departmentOptions} style={{ width: 150 }} />
          </Form.Item>
          <Form.Item name="startCreatedAt" label="开始">
            <DatePicker showTime />
          </Form.Item>
          <Form.Item name="endCreatedAt" label="结束">
            <DatePicker showTime />
          </Form.Item>
          <Form.Item name="minAmount" label="金额从">
            <InputNumber min={0} />
          </Form.Item>
          <Form.Item name="maxAmount" label="到">
            <InputNumber min={0} />
          </Form.Item>
          <Form.Item name="urgent" label="紧急">
            <Select allowClear options={[{ value: true, label: '是' }, { value: false, label: '否' }]} style={{ width: 100 }} />
          </Form.Item>
          <Space>
            <Button type="primary" htmlType="submit">查询</Button>
            <Button onClick={() => { form.resetFields(); setFilters({ page: 1, pageSize: currentPageSize }); }}>重置</Button>
          </Space>
        </Form>
      </Card>
      <div className="dashboard-grid">
        <Card title="审批中">{stats?.pendingCount || 0}</Card>
        <Card title="已通过">{stats?.approvedCount || 0}</Card>
        <Card title="已驳回">{stats?.rejectedCount || 0}</Card>
        <Card title="超时任务">{stats?.overdueTaskCount || 0}</Card>
      </div>
      <Card title="类型统计">
        <Space wrap>
          {Object.entries(stats?.typeCounts || {}).map(([type, count]) => <Typography.Text key={type}>{approvalTypeName(type)}：{count}</Typography.Text>)}
        </Space>
      </Card>
      <Card>
        <Table<ApprovalManagementItem>
          rowKey="id"
          dataSource={data}
          loading={list.isLoading}
          pagination={{
            current: currentPage,
            pageSize: currentPageSize,
            total: totalCount,
            showSizeChanger: true,
            showTotal: (total) => `共 ${total} 条`,
            onChange: (nextPage, nextSize) => setFilters({ ...filters, page: nextPage, pageSize: nextSize })
          }}
          columns={[
            { title: '单号', dataIndex: 'requestNo' },
            { title: '标题', dataIndex: 'title' },
            { title: '类型', dataIndex: 'type', render: approvalTypeName },
            { title: '状态', dataIndex: 'status', render: statusName },
            { title: '申请人', dataIndex: 'applicantName' },
            { title: '部门', dataIndex: 'departmentName', render: (value) => value || '-' },
            { title: '金额', dataIndex: 'amount', render: (value) => value || '-' },
            { title: '紧急', dataIndex: 'urgent', render: (value) => value ? '是' : '否' },
            { title: '提交时间', dataIndex: 'submittedAt', render: (value) => value ? new Date(value).toLocaleString() : '-' },
            { title: '创建时间', dataIndex: 'createdAt', render: (value) => value ? new Date(value).toLocaleString() : '-' },
            { title: '操作', render: (_, record) => <Button size="small" onClick={() => setDetailId(record.id)}>详情</Button> }
          ]}
        />
      </Card>
      <Modal title="审批详情" open={Boolean(detailId)} onCancel={() => setDetailId(null)} footer={null} width={760}>
        {detailData && (
          <Space direction="vertical" size={12} className="full-width">
            <Typography.Text>单号：{detailData.requestNo}</Typography.Text>
            <Typography.Text>标题：{detailData.title}</Typography.Text>
            <Typography.Text>类型：{approvalTypeName(detailData.type)}</Typography.Text>
            <Typography.Text>状态：{statusName(detailData.status)}</Typography.Text>
            <Typography.Text>申请人：{detailData.applicantName}</Typography.Text>
            <Typography.Text>表单：{JSON.stringify(detailData.formData)}</Typography.Text>
            <Typography.Title level={5}>审批流程</Typography.Title>
            <Table
              rowKey="id"
              size="small"
              pagination={false}
              dataSource={detailData.timelineNodes || []}
              columns={[
                { title: '节点', dataIndex: 'nodeName' },
                { title: '审批人', dataIndex: 'approverName' },
                { title: '状态', dataIndex: 'status', render: nodeStatusName },
                { title: '意见', dataIndex: 'comment', render: (value) => value || '-' },
                { title: '处理时间', dataIndex: 'actedAt', render: (value: string) => value ? new Date(value).toLocaleString() : '-' }
              ]}
            />
          </Space>
        )}
      </Modal>
    </Space>
  );
}

function notificationTypeName(type: string) {
  const names: Record<string, string> = {
    todo: '新待办',
    approved: '已通过',
    rejected: '已驳回',
    request_more_info: '补充材料',
    resubmit_more_info: '已补充',
    transfer: '转交',
    add_signer: '加签',
    cc: '抄送',
    withdraw: '撤回',
    voided: '作废',
    overdue: '超时'
  };
  return names[type] || type;
}

function NotificationCenterPage() {
  const notifications = useQuery({ queryKey: ['notifications'], queryFn: fetchNotifications });
  const data = notifications.data?.success ? notifications.data.data : [];

  const readMutation = useMutation({
    mutationFn: (id: number) => markNotificationRead(id),
    onSuccess: (result) => {
      if (!result.success) {
        message.error(result.error?.message || '标记失败');
        return;
      }
      notifications.refetch();
    },
    onError: () => message.error('标记失败')
  });

  const readAllMutation = useMutation({
    mutationFn: markAllNotificationsRead,
    onSuccess: (result) => {
      if (!result.success) {
        message.error(result.error?.message || '标记失败');
        return;
      }
      message.success('已全部标记为已读');
      notifications.refetch();
    },
    onError: () => message.error('标记失败')
  });

  return (
    <Space direction="vertical" size={16} className="full-width">
      <Space className="page-toolbar">
        <Typography.Title level={3}>通知中心</Typography.Title>
        <Button onClick={() => readAllMutation.mutate()} loading={readAllMutation.isPending}>全部已读</Button>
      </Space>
      <Card>
        <Table<NotificationItem>
          rowKey="id"
          dataSource={data}
          loading={notifications.isLoading}
          columns={[
            { title: '状态', dataIndex: 'read', width: 90, render: (value) => value ? '已读' : '未读' },
            { title: '类型', dataIndex: 'type', width: 120, render: notificationTypeName },
            { title: '标题', dataIndex: 'title' },
            { title: '内容', dataIndex: 'content', render: (value) => value || '-' },
            { title: '时间', dataIndex: 'createdAt', render: (value) => value ? new Date(value).toLocaleString() : '-' },
            {
              title: '操作',
              render: (_, record) => (
                <Space>
                  {!record.read && <Button size="small" onClick={() => readMutation.mutate(record.id)} loading={readMutation.isPending}>已读</Button>}
                </Space>
              )
            }
          ]}
        />
      </Card>
    </Space>
  );
}

function actionName(action: string) {
  const names: Record<string, string> = {
    save_draft: '保存草稿',
    submit: '提交',
    approve: '同意',
    reject: '驳回',
    request_more_info: '要求补充',
    resubmit_more_info: '补充后提交',
    transfer: '转交',
    add_signer: '加签',
    cc: '抄送',
    withdraw: '撤回',
    void: '作废',
    upload_attachment: '上传附件',
    delete_attachment: '删除附件'
  };
  return names[action] || action;
}

function AuditLogPage() {
  const actions = useQuery({ queryKey: ['auditApprovalActions'], queryFn: fetchAuditApprovalActions });
  const workflowAudits = useQuery({ queryKey: ['auditWorkflowConfigs'], queryFn: fetchAuditWorkflowConfigs });
  const actionData = actions.data?.success ? actions.data.data : [];
  const workflowData = workflowAudits.data?.success ? workflowAudits.data.data : [];

  return (
    <Space direction="vertical" size={16} className="full-width">
      <Typography.Title level={3}>审计日志</Typography.Title>
      <Card title="审批动作">
        <Table
          rowKey="id"
          dataSource={actionData}
          loading={actions.isLoading}
          pagination={{ pageSize: 8 }}
          columns={[
            { title: '时间', dataIndex: 'createdAt', render: (value) => value ? new Date(value).toLocaleString() : '-' },
            { title: '操作人', dataIndex: 'actorName' },
            { title: '动作', dataIndex: 'action', render: actionName },
            { title: '原状态', dataIndex: 'fromStatus', render: (value) => value ? statusName(value) : '-' },
            { title: '新状态', dataIndex: 'toStatus', render: (value) => value ? statusName(value) : '-' },
            { title: '说明', dataIndex: 'comment', render: (value) => value || '-' }
          ]}
        />
      </Card>
      <Card title="流程配置">
        <Table
          rowKey="id"
          dataSource={workflowData}
          loading={workflowAudits.isLoading}
          pagination={{ pageSize: 8 }}
          columns={[
            { title: '时间', dataIndex: 'createdAt', render: (value) => value ? new Date(value).toLocaleString() : '-' },
            { title: '操作人', dataIndex: 'actorName' },
            { title: '动作', dataIndex: 'action' },
            { title: '模板ID', dataIndex: 'templateId', render: (value) => value || '-' },
            { title: '说明', dataIndex: 'detail', render: (value) => value || '-' }
          ]}
        />
      </Card>
    </Space>
  );
}

function AppShell({ user, onLogout }: { user: CurrentUser; onLogout: () => void }) {
  const permissionSet = useMemo(() => new Set(user.permissions || []), [user.permissions]);
  const visibleMenuItems = useMemo(
    () => menuItems
        .filter((item) => permissionSet.has(item.permission))
        .map(({ permission, ...rest }) => rest),
    [permissionSet]
  );
  const allowedKeys = useMemo(() => new Set(visibleMenuItems.map((item) => item.key)), [visibleMenuItems]);
  const defaultKey = visibleMenuItems[0]?.key || 'dashboard';
  const [selectedKey, setSelectedKey] = useState(defaultKey);
  useEffect(() => {
    if (!allowedKeys.has(selectedKey)) {
      setSelectedKey(defaultKey);
    }
  }, [allowedKeys, defaultKey, selectedKey]);
  const currentMenu = menuItems.find((item) => item.key === selectedKey);
  const canShow = (key: string) => allowedKeys.has(key) && selectedKey === key;

  return (
    <Layout className="app-shell">
      <Sider width={232} className="app-sider">
        <div className="brand">企业审批</div>
        <Menu mode="inline" selectedKeys={[selectedKey]} onClick={(event) => setSelectedKey(event.key)} items={visibleMenuItems} />
      </Sider>
      <Layout>
        <Header className="app-header">
          <Typography.Text className="app-title">企业审批管理系统</Typography.Text>
          <Space>
            <Typography.Text type="secondary">{user.displayName}</Typography.Text>
            <Button icon={<LogoutOutlined />} onClick={onLogout}>
              退出
            </Button>
          </Space>
        </Header>
        <Content className="app-content">
          {canShow('dashboard') && <Dashboard user={user} />}
          {canShow('new') && <NewApprovalPage />}
          {canShow('my') && <MyApprovalsPage />}
          {canShow('todo') && <ApprovalTasksPage mode="todo" />}
          {canShow('done') && <ApprovalTasksPage mode="done" />}
          {canShow('cc') && <ApprovalCcPage />}
          {canShow('manage') && <ApprovalManagementPage />}
          {canShow('organization') && <OrganizationPage />}
          {canShow('users') && <UserManagementPage />}
          {canShow('roles') && <RolePermissionPage />}
          {canShow('workflow') && <WorkflowConfigPage />}
          {canShow('notifications') && <NotificationCenterPage />}
          {canShow('audit') && <AuditLogPage />}
          {visibleMenuItems.length === 0 && <PlaceholderPage title={String(currentMenu?.label || '无可访问模块')} />}
        </Content>
      </Layout>
    </Layout>
  );
}

export default function App() {
  const [user, setUser] = useState<CurrentUser | null>(null);
  const [checkingSession, setCheckingSession] = useState(Boolean(localStorage.getItem(TOKEN_KEY)));

  useEffect(() => {
    if (!localStorage.getItem(TOKEN_KEY)) {
      return;
    }
    fetchCurrentUser()
      .then((result) => {
        if (result.success) {
          setUser(result.data);
        } else {
          localStorage.removeItem(TOKEN_KEY);
        }
      })
      .catch(() => localStorage.removeItem(TOKEN_KEY))
      .finally(() => setCheckingSession(false));
  }, []);

  if (checkingSession) {
    return <div className="loading-screen">正在恢复登录状态...</div>;
  }

  if (!user) {
    return <LoginPage onLoggedIn={setUser} />;
  }

  return (
    <AppShell
      user={user}
      onLogout={() => {
        localStorage.removeItem(TOKEN_KEY);
        setUser(null);
      }}
    />
  );
}
