# SrsRtcAndroidClient
基于[SRS](https://github.com/ossrs/srs)视频服务器实现简易音视频通话系统——Android客户端

## 系统组成
- 信令服务器[srs-rtc-server](https://github.com/shenbengit/srs-rtc-server) 
- Android客户端[SrsRtcAndroidClient](https://github.com/shenbengit/SrsRtcAndroidClient) 
- Web客户端[srs-rtc-web-client](https://github.com/shenbengit/srs-rtc-web-client) （功能开发中...）
## 功能特点
- 支持用户注册、登录
- 支持私聊
- 支持群聊
- 支持聊天室
- 支持私聊、群聊中继续添加会见人

## 运行效果
|用户注册|用户登录|
|:---:|:---:|
|<img src="https://github.com/shenbengit/SrsRtcAndroidClient/blob/master/screenshots/%E6%B3%A8%E5%86%8C.gif" alt="用户注册" width="250px">|<img src="https://github.com/shenbengit/SrsRtcAndroidClient/blob/master/screenshots/%E7%99%BB%E5%BD%95.gif" alt="用户登录" width="250px">|

|私聊|
|:---:|
|<img src="https://github.com/shenbengit/SrsRtcAndroidClient/blob/master/screenshots/%E7%A7%81%E8%81%8A.gif" alt="私聊" width="530px">|


|群聊|
|:---:|
|<img src="https://github.com/shenbengit/SrsRtcAndroidClient/blob/master/screenshots/%E7%BE%A4%E8%81%8A.gif" alt="群聊" width="530px">|


|聊天室|
|:---:|
|<img src="https://github.com/shenbengit/SrsRtcAndroidClient/blob/master/screenshots/%E8%81%8A%E5%A4%A9%E5%AE%A4.gif" alt="聊天室" width="530px">|
## Getting Started

### 依赖环境
- srs-rtc-server用户注册、信令服务，部署步骤详见[srs-rtc-server](https://github.com/shenbengit/srs-rtc-server)。
- SRS视频服务器，部署步骤详见[SRS-Wiki](https://github.com/ossrs/srs/wiki/v4_CN_Home#getting-started)，启用WebRTC。

### 配置文件修改
修改[Constant.kt](https://github.com/shenbengit/SrsRtcAndroidClient/blob/master/app/src/main/java/com/shencoder/srs_rtc_android_client/constant/Constant.kt)；
- 信令服务相关参数

```kotlin
/**
 * 信令服务相关
 */
object SIGNAL {
    /**
     * 信令服务地址
     * ip或域名
     */
    const val SERVER_ADDRESS = "192.168.10.185"

    /**
     * api请求http端口
     */
    const val API_HTTP_PORT = 9898

    /**
     * api请求https端口
     */
    const val API_HTTPS_PORT = 9899

    /**
     * socket.io http端口
     */
    const val SOCKET_IO_IP_HTTP_PORT = 9998

    /**
     * socket.io https端口
     */
    const val SOCKET_IO_IP_HTTPS_PORT = 9999
}
```
- SRS服务相关参数
```kotlin
/**
 * SRS服务相关
 */
object SRS {
    /**
     * SRS服务地址
     * ip或域名
     */
    const val SERVER_ADDRESS = "192.168.10.185"

    /**
     * SRS服务api请求http端口
     */
    const val API_HTTP_PORT = 1985

    /**
     * SRS服务api请求https端口
     */
    const val API_HTTPS_PORT = 1990
}
```

## 实现流程解析
- [Android端从SRS服务器拉取WebRTC流](https://blog.csdn.net/csdn_shen0221/article/details/120269707)
- [Android端向SRS服务器推送WebRTC流](https://blog.csdn.net/csdn_shen0221/article/details/120331004)
- [Android端WebRTC启用H264编码-sdp中无H264信息](https://blog.csdn.net/csdn_shen0221/article/details/119982257)

## 作者其他的开源项目
- 基于RecyclerView实现网格分页布局：[PagerGridLayoutManager](https://github.com/shenbengit/PagerGridLayoutManager)
- Android端WebRTC一些扩展方法：[WebRTCExtension](https://github.com/shenbengit/WebRTCExtension)
- 基于Netty封装UDP收发工具：[UdpNetty](https://github.com/shenbengit/UdpNetty)
- Android端基于JavaCV实现人脸检测功能：[JavaCV-FaceDetect](https://github.com/shenbengit/JavaCV-FaceDetect)
- 使用Kotlin搭建Android MVVM快速开发框架：[MVVMKit](https://github.com/shenbengit/MVVMKit)


## [LICENSE](https://github.com/shenbengit/SrsRtcAndroidClient/blob/master/LICENSE)
