# 健康助手
车载app
## 功能
### 语音唤醒和语音播报
语音唤醒采用百度，在 http://ai.baidu.com/tech/speech/wake 自定义唤醒词，将生成的WakeUp.bin放到assets文件中。自己将语音唤醒功能包装，对外提供start和stop方法，方便使用。  
语音识别和语音播报采用科大讯飞，自己包装成XfyunASR类，对外提供startSpeechDialog和speekText方法，完成语音识别和合成播报。主要流程：startSpeechDialog出现对话框监听用户指令——识别语音指令——调用speekText合成回应语音，根据语音识别结果完成预定义指令操作。
### 人脸识别登录
自定义CameraSurfaceView类实现自定义相机，登录时监测到人脸自动拍照发给后台识别，注册时手动拍照发到后台留存。
### 蓝牙通信获取传感器数据
与蓝牙建立socket连接，获取数据流。app按照约定好的格式分割出一条完整数据，并从其中获取传感器数据交应用处理。
### app存活心跳包
开启LiveService，每隔5分钟向后台发送心跳包。
### 模拟数据
身体体征数据不能完全由传感器获得，所以需要模拟数据。开启service与后台连接，当需要的时候向后台请求模拟数据。后台也可以随时更改生成的模拟数据的范围，以模拟正常和不正常数据。
### 智能报警
在行车过程中监测身体数据，当监测到体征数据异常时自动拨打紧急联系人的电话。在拨打之前由语音提示，假如是误报可以发出指令“一切正常取消报警”。