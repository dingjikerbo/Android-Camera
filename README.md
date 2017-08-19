# Android Camera

本系列会包括Android Camera预览，拍照，视频录制，播放，滤镜及渲染等，还会对相机系统原理做深入分析。

------

## **一、相机预览**

|序号|项目名称|内容简介|
|--- |-------|-------|
|1|GLSurfacePreview|GLSurfaceView + OpenGL相机预览，直接绘制到Display Surface|
|2|GLSurfacePreview2|GLSurfaceView + OpenGL相机预览，先绘制到FBO的Texture上，再处理后(变红)绘制到Display Surface|
|3|SurfacePreview|SurfaceView + OpenGL + EGL相机预览，直接绘制到Display Surface|
|4|SurfacePreview2|SurfaceView + OpenGL + EGL相机预览，先绘制到PBuffer，再Blit到Display Surface|
|5|MultiSurfacePreview|相机预览到两个窗口，一个是原始图像，一个是处理过的图像(变红)|

## **二，视频录制**

|序号|项目名称|内容简介|
|--- |-------|-------|
|1|recorder1|GLSurfaceView + MediaCodec + AudioCodec + MediaMuxer|


## **三，视频播放**

|序号|项目名称|内容简介|
|--- |-------|-------|
|1|video1|SurfaceView|
