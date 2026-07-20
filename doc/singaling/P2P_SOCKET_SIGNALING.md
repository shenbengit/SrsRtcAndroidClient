# P2P 模式 Socket 信令整理

基于以下代码整理：
`MainViewModel.kt`、`CheckUserActivity.kt`、`CheckUserView.kt`、`CheckUserViewModel.kt`、
`P2pCallerActivity.kt`、`P2pCallerViewModel.kt`、`P2pCalleeActivity.kt`、`P2pCalleeViewModel.kt`、
`CallSocketIoClient.kt`、`SignalProtocols.kt` 及相关 JSON 字段。

## 1. 入口与通话类型

- `MainViewModel.p2p()`：进入 P2P 入口页，携带
  - `CHAT_MODE = PRIVATE_MODE`
  - `IS_P2P = true`
- `CheckUserView` 在 P2P 模式下显示通话类型选择。
- 选择结果：
  - `rbAudioCall` -> `CallType.Audio` -> `type = 1`
  - 其他情况 -> `CallType.Video` -> `type = 2`
- `CheckUserViewModel.toMesh(list, callType)` 会启动 `P2pCallerActivity`，并把被叫列表和 `callType` 传过去。
- 被叫侧通话类型由服务端返回的 `type` 转换而来，`type = 1` 表示音频，`type = 2` 表示音视频。

## 2. P2P 主流程

### 主叫

1. 选择 1 个被叫用户。
2. 选择音频或音视频。
3. 调用 `req_p2p_invite_someone` 发起邀请。
4. 等待被叫 `accept/reject`。
5. 接收 `accept` 后创建 offer 并发送。
6. 继续交换 answer / ice。
7. 任一方可挂断。

### 被叫

1. 收到 `notify_p2p_request_call`。
2. 根据 `callType` 决定音频或视频模式。
3. 选择接受或拒绝。
4. 接受后等待 offer，创建 answer 并发送。
5. 继续交换 ice。
6. 任一方可挂断。

## 3. 信令总表

| 场景 | 事件名 | 方向 | 请求参数/通知数据 | 成功回参 | 说明 |
| --- | --- | --- | --- | --- | --- |
| 发起 P2P 邀请 | `req_p2p_invite_someone` | 主叫 -> 服务端 | `{"userId":"xxx","callType":1}` | `{"code":0,"data":{"inviteeInfo":{"createdAt":"","id":0,"userId":"","userType":"","username":""},"roomId":""},"msg":"ok"}` | `callType=1` 音频，`callType=2` 音视频 |
| 通知被叫收到邀请 | `notify_p2p_request_call` | 服务端 -> 被叫 | `{"inviteInfo":{"createdAt":"","id":0,"userId":"","userType":"","username":""},"callList":[{"createdAt":"","id":0,"userId":"","userType":"","username":""}],"callType":1,"roomId":""}` | 无 ack | `callList` 可为空或不存在 |
| 拒绝邀请 | `req_p2p_reject_call` | 被叫 -> 服务端 | `"roomId"` | `{"code":0,"data":null,"msg":"ok"}` | 对端收到 `notify_p2p_reject_call` |
| 通知主叫被拒绝 | `notify_p2p_reject_call` | 服务端 -> 主叫 | `{"userInfo":{"createdAt":"","id":0,"userId":"","userType":"","username":""},"callEnded":true,"roomId":""}` | 无 ack | `callEnded` 表示是否结束通话 |
| 接受邀请 | `req_p2p_accept_call` | 被叫 -> 服务端 | `"roomId"` | `{"code":0,"data":{"alreadyInRoomList":[{"userInfo":{"createdAt":"","id":0,"userId":"","userType":"","username":""},"publishStreamUrl":null}],"roomId":""},"msg":"ok"}` | 对端收到 `notify_p2p_accept_call` |
| 通知主叫被接受 | `notify_p2p_accept_call` | 服务端 -> 主叫 | `{"userInfo":{"createdAt":"","id":0,"userId":"","userType":"","username":""},"roomId":""}` | 无 ack | 主叫收到后创建 offer |
| 发送 offer | `req_p2p_send_offer` | 主叫 -> 服务端 | `["roomId","sdp"]` | `{"code":0,"data":{"alreadyInRoomList":[{"userInfo":{"createdAt":"","id":0,"userId":"","userType":"","username":""},"publishStreamUrl":null}],"roomId":""},"msg":"ok"}` | 对端收到 `notify_p2p_receive_offer` |
| 通知被叫收到 offer | `notify_p2p_receive_offer` | 服务端 -> 被叫 | `{"userInfo":{"createdAt":"","id":0,"userId":"","userType":"","username":""},"roomId":"","sdp":""}` | 无 ack | 被叫收到后创建 answer |
| 发送 answer | `req_p2p_send_answer` | 被叫 -> 服务端 | `["roomId","sdp"]` | `{"code":0,"data":{"alreadyInRoomList":[{"userInfo":{"createdAt":"","id":0,"userId":"","userType":"","username":""},"publishStreamUrl":null}],"roomId":""},"msg":"ok"}` | 对端收到 `notify_p2p_receive_answer` |
| 通知主叫收到 answer | `notify_p2p_receive_answer` | 服务端 -> 主叫 | `{"userInfo":{"createdAt":"","id":0,"userId":"","userType":"","username":""},"roomId":"","sdp":""}` | 无 ack | 主叫设置 remote answer |
| 发送 ICE | `req_p2p_send_ice` | 任一端 -> 服务端 | `["roomId",{"sdpMid":"","sdpMLineIndex":0,"sdp":""}]` | `{"code":0,"data":{"alreadyInRoomList":[{"userInfo":{"createdAt":"","id":0,"userId":"","userType":"","username":""},"publishStreamUrl":null}],"roomId":""},"msg":"ok"}` | 对端收到 `notify_p2p_receive_ice` |
| 通知对端收到 ICE | `notify_p2p_receive_ice` | 服务端 -> 对端 | `{"userInfo":{"createdAt":"","id":0,"userId":"","userType":"","username":""},"roomId":"","ice":{"sdpMid":"","sdpMLineIndex":0,"sdp":""}}` | 无 ack | 对端添加 ICE Candidate |
| 挂断 | `req_p2p_hang_up` | 任一端 -> 服务端 | `"roomId"` | `{"code":0,"data":{"alreadyInRoomList":[{"userInfo":{"createdAt":"","id":0,"userId":"","userType":"","username":""},"publishStreamUrl":null}],"roomId":""},"msg":"ok"}` | 对端收到 `notify_p2p_hang_up` |
| 通知对端挂断 | `notify_p2p_hang_up` | 服务端 -> 对端 | `{"userInfo":{"createdAt":"","id":0,"userId":"","userType":"","username":""},"callEnded":true,"roomId":""}` | 无 ack | `callEnded` 表示是否结束通话 |
| 通话中离线 | `notify_p2p_offline_during_call` | 服务端 -> 对端 | `{"userInfo":{"createdAt":"","id":0,"userId":"","userType":"","username":""},"callEnded":true,"reason":null,"roomId":""}` | 无 ack | `reason` 可为空 |
| 清理状态 | `req_p2p_reset_status` | 本地 -> 服务端 | 无 | 无 | 仅生命周期清理使用 |

## 4. 关键 JSON 示例

### `req_p2p_invite_someone`

请求：

```json
{
  "userId": "目标用户ID",
  "callType": 1
}
```

回参：

```json
{
  "code": 0,
  "data": {
    "inviteeInfo": {
      "createdAt": "2026-07-20T00:00:00.000Z",
      "id": 1,
      "userId": "user_002",
      "userType": "normal",
      "username": "被叫用户名"
    },
    "roomId": "xxxx"
  },
  "msg": "ok"
}
```

### `notify_p2p_request_call`

通知数据：

```json
{
  "inviteInfo": {
    "createdAt": "2026-07-20T00:00:00.000Z",
    "id": 1,
    "userId": "user_001",
    "userType": "normal",
    "username": "主叫用户名"
  },
  "callList": [
    {
      "createdAt": "2026-07-20T00:00:00.000Z",
      "id": 2,
      "userId": "user_002",
      "userType": "normal",
      "username": "被叫用户名"
    }
  ],
  "callType": 1,
  "roomId": "xxxx"
}
```

其中 `callType = 1` 表示音频，`callType = 2` 表示音视频。

### `notify_p2p_receive_offer` / `notify_p2p_receive_answer`

通知数据：

```json
{
  "userInfo": {
    "createdAt": "2026-07-20T00:00:00.000Z",
    "id": 1,
    "userId": "user_001",
    "userType": "normal",
    "username": "用户名"
  },
  "roomId": "xxxx",
  "sdp": "v=0..."
}
```

### `req_p2p_send_ice`

请求参数：

```json
[
  "xxxx",
  {
    "sdpMid": "0",
    "sdpMLineIndex": 0,
    "sdp": "candidate:..."
  }
]
```

对端通知数据：

```json
{
  "userInfo": {
    "createdAt": "2026-07-20T00:00:00.000Z",
    "id": 1,
    "userId": "user_001",
    "userType": "normal",
    "username": "用户名"
  },
  "roomId": "xxxx",
  "ice": {
    "sdpMid": "0",
    "sdpMLineIndex": 0,
    "sdp": "candidate:..."
  }
}
```

### 通用回参包装

所有 `emitEvent(...){}` 形式的请求，服务端回参都按以下 JSON 包装解析：

```json
{
  "code": 0,
  "data": {},
  "msg": "ok"
}
```

代码里判断成功的条件是 `code == Constant.RESULT_OK`。

## 5. 音频/视频差异

- `CallType.Audio`：只发起音频轨道。
- `CallType.Video`：发起音频 + 视频轨道。
- 在 `P2PPeerConnectionFactory.startCapture()` 中：
  - 音频轨道必加
  - 仅视频模式才添加视频轨道

## 6. 代码对应位置

- `app/src/main/java/com/shencoder/srs_rtc_android_client/ui/main/MainViewModel.kt`
- `app/src/main/java/com/shencoder/srs_rtc_android_client/widget/CheckUserView.kt`
- `app/src/main/java/com/shencoder/srs_rtc_android_client/ui/check_user/CheckUserActivity.kt`
- `app/src/main/java/com/shencoder/srs_rtc_android_client/ui/check_user/CheckUserViewModel.kt`
- `app/src/main/java/com/shencoder/srs_rtc_android_client/helper/call/SignalProtocols.kt`
- `app/src/main/java/com/shencoder/srs_rtc_android_client/helper/call/CallSocketIoClient.kt`
- `app/src/main/java/com/shencoder/srs_rtc_android_client/helper/call/bean/`
- `app/src/main/java/com/shencoder/srs_rtc_android_client/ui/p2p/P2pCallerActivity.kt`
- `app/src/main/java/com/shencoder/srs_rtc_android_client/ui/p2p/P2pCallerViewModel.kt`
- `app/src/main/java/com/shencoder/srs_rtc_android_client/ui/p2p/P2pCalleeActivity.kt`
- `app/src/main/java/com/shencoder/srs_rtc_android_client/ui/p2p/P2pCalleeViewModel.kt`
