# Android Camera Demos

**本项目主要有以下几点内容：**

**一、围绕Android相机的各种Demo，包括预览，RGB转换，视频录制和播放，滤镜等，每种Demo都会给出若干种解决方案**

**二、围绕Android相机及图像音频架构和原理做深入分析**



------

## **一、相机预览**

|序号|项目名称|内容简介|状态|
|--- |-------|-------|------|
|1|GLSurfacePreview|GLSurfaceView + OpenGL相机预览，直接绘制到Display Surface|Done|
|2|GLSurfacePreview2|GLSurfaceView + OpenGL相机预览，先绘制到FBO的Texture上，再处理后(变红)绘制到Display Surface|Done|
|3|SurfacePreview|SurfaceView + OpenGL + EGL相机预览，直接绘制到Display Surface|Done|
|4|SurfacePreview2|SurfaceView + OpenGL + EGL相机预览，先绘制到PBuffer，再Blit到Display Surface|Done|
|5|MultiSurfacePreview|相机预览到两个窗口，一个是原始图像绘制到Texture，再将Texture处理后画到一个小窗口|Done|

## **二、RGB转换**
利用GPU将相机帧(NV21)转成RGB并传回CPU，1920 * 1080，RGBA

|序号|项目名称|内容简介|状态|
|--- |-------|-------|-----|
|1|RGBConverter1|从Display Surface调glReadPixels，性能很差，~500ms|Pending|
|2|RGBConverter2|从EGL Pbuffer调glReadPixels，性能有较大提升，~60ms|Pending|
|3|RGBConverter3|从FBO调glReadPixels，性能比PBuffer好一点，~25ms|Pending|
|4|RGBConverter4|采用FBO + PBO，~5ms|Pending|
|5|RGBConverter5|采用FBO + 2PBO，~3ms|Pending|


## **二，视频录制**

|序号|项目名称|内容简介|状态|
|--- |-------|-------|----|
|1|recorder1|GLSurfaceView + MediaMuxer，不共享EglContext，只能录制相机预览|Pending|
|2|recorder2|SurfaceView + MediaMuxer，共享EglContext，可以录制整个Surface|Pending|
|3|recorder3|只录制Surface上某一个部分，如人脸|Pending|


## **三，视频播放**

|序号|项目名称|内容简介|状态|
|--- |-------|-------|----|
|1|video1|SurfaceView播放原始视频|Pending|
|2|video2|视频裁剪播放，并增加一层遮罩|Pending|
