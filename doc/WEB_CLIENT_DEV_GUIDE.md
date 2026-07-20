# Web 端开发说明

> 目标：基于当前 Android 端，实现 Web 端和 Android 端之间的音视频通话。  
> 这是给 AI 直接使用的开发说明，重点写清楚功能、页面、协议、时序和 UI 风格。

## 1. 项目目标

Web 端需要和现有 Android 端互通，优先完成这条主链路：

1. 用户注册、登录
2. 连接信令服务器并显示在线状态
3. 根部不同模式（私聊、群聊、聊天室、p2p）选择用户发起音视频通话
4. 接听 / 拒绝来电
5. 浏览器端完成 WebRTC 音视频向srs流媒体服务进行音视频推拉流

可选扩展：

1. 群呼
2. 房间聊天室 / 多人推拉流
3. 在线用户列表、强制下线、通话中掉线处理

## 2. 建议技术栈

如果没有既有 Web 项目，建议直接用：

- `Vite`
- `Vue`
- `TypeScript`
- `socket.io-client`
- 原生 `WebRTC` API

如果已经有前端框架，就沿用现有框架，不要重写。

## 3. 核心约定

### 3.1 用户类型

- 目前只支持普通客户端
- `userType = "0"`
- 管理员类型暂不考虑

### 3.2 必须保存的本地信息

- `userId`
- `username`
- `userType`
- 登录态
- 当前 socket 连接状态
- 当前通话房间号 `roomId`

### 3.3 连接规则

- 登录成功后再连接信令服务器
- 连接成功后要刷新在线用户列表
- 如果收到强制下线通知，要立刻断开所有 WebRTC 连接并回到登录页
- 页面销毁时要主动释放摄像头、麦克风、PeerConnection、Socket

## 4. 页面结构

### 4.1 登录页

风格参考 Android 端：

- 深色背景
- 大标题
- 两个输入框：`User Id`、`Password`
- 一个主按钮：`Sign in`
- 一个文字入口：`Register?`

### 4.2 注册页

- 大标题
- 输入框：`User Id`、`Username`、`Password`、`Confirm Password`
- 一个主按钮：`Register`

### 4.3 主页面

主页面要显示：

- 当前用户头像
- 用户名
- `userId`
- Socket 连接状态
- 四个入口：
  - 私聊
  - 群聊
  - 聊天室
  - P2P

### 4.4 选人页

功能是选择要通话的用户。

- 私聊：单选
- 群聊：多选
- P2P：单选，并可切换音频 / 视频
- 当前登录用户本人不可选择

### 4.5 来电页
> 注意：私聊和群聊是同一个邀请信令，p2p是单独的信令 

收到来电后弹出全屏页或模态层，必须有：

- 来电人信息
- 通话类型
- 接听按钮
- 拒绝按钮

### 4.6 通话页

P2P 通话页参考 Android 端布局：

- 远端画面全屏
- 本地画面右上角小窗
- 底部控制区：
  - 麦克风静音
  - 切换摄像头
  - 扬声器
  - 挂断
  - 通话时长

### 4.7 聊天室页

如果做房间模式，页面要支持：

- 本地预览
- 多路远端视频列表
- 邀请更多人
- 挂断 / 退出房间

## 5. UI 风格要求

参考 Android 端的整体风格，不要做成营销站。

- 主背景偏灰：`#8E8C8C`
- 文字以白色为主
- 主按钮用青绿色：`#03DAC5`
- 强调链接用橙色
- 卡片和面板简洁，圆角小
- 页面信息密度要高
- 音视频页尽量全屏、少装饰
- 图标按钮优先，不要用大段说明文字
- 主页面可以保留白色信息卡，但通话页不要套卡片

Android 端常见视觉特征：

- 标题大而粗
- 按钮高而圆
- 通话控制图标在上，文字在下
- 头像使用多个固定样式
- 连接状态有明显的在线 / 离线视觉差异

## 6. 接口配置

### 6.1 信令服务

默认参考 Android 端配置：

- API HTTP：`9898`
- API HTTPS：`9899`
- Socket.IO HTTP：`9998`
- Socket.IO HTTPS：`9999`
- namespace：`/srs_rtc/signal/client`

建议 Web 端把这些做成环境变量：

```bash
VITE_SIGNAL_HOST=
VITE_SIGNAL_HTTP_PORT=9898
VITE_SIGNAL_HTTPS_PORT=9899
VITE_SIGNAL_WS_PORT=9998
VITE_SIGNAL_WSS_PORT=9999
```

### 6.2 SRS 服务

- API HTTP：`1985`
- API HTTPS：`1990`
- play 接口：`/rtc/v1/play/`
- publish 接口：`/rtc/v1/publish/`

### 6.3 用户接口

```http
POST /srs_rtc/user/userLogin
POST /srs_rtc/user/checkUserId
POST /srs_rtc/user/insertUser
GET  /srs_rtc/user/getAllUserInfo
GET  /srs_rtc/client/getAllOnlineUsers
```

### 6.4 SRS 请求体

```json
{
  "sdp": "string",
  "streamurl": "webrtc://..."
}
```

## 7. 信令协议拆分索引

信令协议已按模块拆分到 `doc/singaling/` 下，主文档不再维护总表。请直接查看对应模块文档：

- [私聊 Socket 信令](./singaling/PRIVATE_CHAT_SOCKET_SIGNALING.md)
- [群聊 Socket 信令](./singaling/GROUP_CHAT_SOCKET_SIGNALING.md)
- [聊天室 Socket 信令](./singaling/CHAT_ROOM_SOCKET_SIGNALING.md)
- [P2P Socket 信令](./singaling/P2P_SOCKET_SIGNALING.md)

Web 端实现时以这些模块文档为准，主文档仅保留页面、流程和交付要求。

## 8. 页面实现建议

### 8.1 主要组件

- `LoginPage`
- `RegisterPage`
- `MainPage`
- `UserSelectPage`
- `IncomingCallModal`
- `OutgoingCallPage`
- `P2PCallPage`
- `RoomCallPage`

### 8.2 通用组件

- 用户头像
- 在线状态点
- 主按钮
- 图标按钮
- 视频卡片
- 通话控制栏

## 9. 错误处理

必须处理这些情况：

- 登录失败
- 注册失败
- socket 断开
- 强制下线
- 对方拒绝
- 对方挂断
- 对方离线
- 摄像头 / 麦克风权限拒绝
- `getUserMedia` 失败
- WebRTC 协商失败
- SRS `play/publish` 失败

失败时的行为建议：

- 提示简短错误信息
- 释放本地资源
- 返回上一页或登录页

## 10. 交付要求

AI 开发 Web 端时，优先满足下面几点：

1. 先打通登录、连接、P2P 音视频通话
2. 再补房间 / 群呼
3. UI 要接近 Android 端，不要做成营销页
4. 所有信令命名必须和 Android 端保持一致
5. 所有 `roomId`、`userId`、`callType`、`publishStreamUrl` 字段不要擅自改名

## 11. 验收标准

最小可用版本必须满足：

- 能注册、登录
- 能连接信令服务器
- 能拉到在线用户
- 能发起私聊、群聊视频通话，及接受视频通话邀请
- 能加入聊天室进行视频通话
- 能发起P2P音视频通话，及接受音视频通话邀请
- Android 端和 Web 端能互相接听
- 能看到本地画面和远端画面
- 能静音、切换摄像头、挂断
- 能正确处理对方拒绝、挂断、离线

## 12. 参考来源
### 服务端源码
`https://github.com/shenbengit/srs-rtc-server`
- `README.md`
- `src/constant/*`
- `src/controller/*`
### Android端源码
`https://github.com/shenbengit/SrsRtcAndroidClient`
- `README.md`
- `app/src/main/java/com/shencoder/srs_rtc_android_client/helper/call/SignalProtocols.kt`
- `app/src/main/java/com/shencoder/srs_rtc_android_client/helper/call/CallSocketIoClient.kt`
- `app/src/main/java/com/shencoder/srs_rtc_android_client/webrtc/widget/P2PCallLayout.kt`
- `app/src/main/java/com/shencoder/srs_rtc_android_client/webrtc/widget/CallLayout.kt`
- `app/src/main/java/com/shencoder/srs_rtc_android_client/ui/*`
