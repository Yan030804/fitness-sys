# 智能健身推荐系统

基于 Spring Boot + Vue 3 的智能健身推荐系统，采用前后端分离架构，集成 AI 对话与 ItemCF 协同过滤推荐算法，为用户提供个性化健身计划、饮食记录、运动管理等一站式健身服务。

## 技术栈

### 后端

- **框架**: Spring Boot 3.5
- **安全**: Spring Security + JWT
- **ORM**: MyBatis
- **数据库**: MySQL 8.x
- **AI**: LangChain4j + OpenAI API
- **构建工具**: Maven
- **JDK**: 17

### 前端

- **框架**: Vue 3 + Vite
- **UI 组件库**: Element Plus
- **状态管理**: Pinia
- **路由**: Vue Router 4
- **图表**: ECharts
- **HTTP**: Axios

## 项目结构

```
fitness-sys-end/
├── fitness-auth-skeleton/      # 后端 Spring Boot 项目
│   └── src/main/java/com/example/fitness/
│       ├── admin/              # 管理员模块（用户/计划/预约/动作/食材管理）
│       ├── ai/                 # AI 对话模块（智能问答、预约辅助）
│       ├── appointment/        # 预约模块
│       ├── auth/               # 认证模块（登录/注册）
│       ├── common/             # 公共组件（统一返回、异常处理、工具类）
│       ├── config/             # 配置（CORS、Security）
│       ├── diet/               # 饮食记录模块
│       ├── exercise/           # 动作库模块
│       ├── food/               # 食材库模块
│       ├── plan/               # 健身计划模块（含计划明细 CRUD）
│       ├── recommendation/     # 推荐模块（ItemCF + 规则兜底）
│       ├── record/             # 记录实体（饮食/训练）
│       ├── security/           # 安全模块（JWT 过滤器、用户详情）
│       ├── system/             # 系统模块（用户/角色实体与 Mapper）
│       ├── user/               # 用户模块（个人信息、修改密码）
│       └── workout/            # 训练记录模块
├── fitness-sys-vue/            # 前端 Vue 3 项目
│   └── src/
│       ├── api/                # 接口请求封装
│       ├── components/         # 公共组件（Layout、修改密码弹窗）
│       ├── router/             # 路由配置（含权限守卫）
│       ├── store/              # Pinia 状态管理
│       ├── styles/             # 全局样式
│       ├── utils/              # 工具函数（认证、请求拦截）
│       └── views/              # 页面视图
│           ├── admin/          # 管理员页面（仪表盘、用户管理）
│           ├── ai/             # AI 对话页面
│           ├── appointment/    # 预约管理
│           ├── diet/           # 饮食记录
│           ├── exercise/       # 动作库
│           ├── food/           # 食材库
│           ├── plan/           # 健身计划
│           ├── recommendation/ # 推荐中心
│           └── training/       # 训练记录
├── sql/                        # 数据库建表 SQL
└── README.md
```

## 功能模块

| 模块 | 说明 |
|------|------|
| 用户认证 | 注册、登录、JWT 鉴权、角色权限（管理员/普通用户） |
| 用户管理 | 个人信息维护、密码修改、身体数据管理 |
| 动作库 | 动作浏览与详情、管理员 CRUD（逻辑删除） |
| 食材库 | 食材浏览与详情、管理员 CRUD（逻辑删除） |
| 健身计划 | 计划生成、计划列表、明细 CRUD、打卡完成 |
| 训练记录 | 训练记录的增删改查 |
| 饮食记录 | 饮食记录的增删改查 |
| 预约管理 | 预约创建、取消、状态流转 |
| 智能推荐 | 基于 ItemCF 协同过滤 + 规则兜底的个性化推荐 |
| AI 对话 | 基于 LangChain4j 的智能健身问答与预约辅助 |
| 管理员后台 | 数据仪表盘、用户/计划/动作/食材统一管理 |

## 快速开始

### 环境要求

- JDK 17+
- Node.js 18+
- MySQL 8.x
- Maven 3.8+

### 1. 数据库初始化

```bash
# 登录 MySQL 后执行
source sql/fitness_recommend_system.sql
# 或使用完整建表脚本
source sql/fitness_recommend_system_revised.sql
```

### 2. 启动后端

```bash
cd fitness-auth-skeleton

# 修改数据库连接信息（如需要）
# src/main/resources/application.yml

mvn spring-boot:run
```

后端默认运行在 `http://localhost:8080`。

### 3. 启动前端

```bash
cd fitness-sys-vue

npm install
npm run dev
```

前端默认运行在 `http://localhost:5173`。

### 4. 配置 AI（可选）

在 `application.yml` 中配置 OpenAI API Key 以启用 AI 对话功能：

```yaml
openai:
  api-key: your-api-key-here
```

## 数据库设计

系统共包含以下核心数据表：

| 表名 | 说明 |
|------|------|
| `sys_user` | 用户表（含身体数据、健身目标、角色关联） |
| `sys_role` | 角色表（管理员/普通用户） |
| `fit_exercise` | 动作库 |
| `fit_food` | 食材库 |
| `fitness_plan` | 健身计划 |
| `fitness_plan_item` | 计划明细 |
| `user_workout_record` | 训练记录 |
| `user_diet_record` | 饮食记录 |
| `appointment_info` | 预约信息 |
| `recommendation_record` | 推荐记录 |

## 接口规范

- 统一前缀：`/api/v1`
- 认证方式：Bearer Token（JWT）
- 返回格式：`{ "code": 200, "message": "success", "data": {} }`
- 详细接口文档参见：[接口设计说明](./智能健身推荐系统-接口设计说明-修订版.md)

## 许可证

本项目仅供学习与毕业设计使用。
