# Android Camera Demos

------

## **一、相机预览**


|序号|项目名称|内容简介|状态|
|--- |-------|-------|------|
|1|GLSurfacePreview|[拿到相机帧数据，直接绘制到屏幕](doc/glsurfacepreview.md)|Done|
|2|GLSurfacePreview2|[拿到相机帧数据，先绘制到FBO，离线处理后(变红)绘制到屏幕](doc/glsurfacepreview2.md)|Done|
|3|GLSurfacePreview3|[直接给相机的预览纹理绘制到屏幕](doc/glsurfacepreview3.md)|Done|
|4|SurfacePreview|[拿到相机帧数据，直接绘制到屏幕](doc/surfacepreview.md)|Done|
|5|SurfacePreview2|[拿到相机帧数据，先绘制到PBuffer，再Blit到WindowSurface](doc/surfacepreview2.md)|Done|
|6|MultiSurfacePreview|[拿到相机帧数据，先转成纹理，再分别绘制到两个SurfaceView](doc/multisurfacepreview.md)|Done|

## **二、RGB转换**
利用GPU将相机帧(NV21)转成RGB并传至CPU，帧为1920 * 1080，RGBA

另开一个线程做RGB转换，不然如果和相机共用上下文，渲染时需要来回切换，且可能阻塞相机渲染，对性能不利。

|序号|模块名称|内容简介|状态|
|--- |-------|-------|-----|
|1|RgbConverter1|从Display Surface直接readPixels，性能很差，~550ms|done|
|2|RgbConverter2|从Pbuffer调readPixels，性能有较大提升，~30ms|done|
|3|RgbConverter3|从FBO调readPixels，性能比PBuffer稍好一点，~27ms|done|
|4|RgbConverter4|从FBO读到PBO，readPixels阻塞, glMapBuffer阻塞，~11ms|done|
|5|RgbConverter5|从Pbuffer读到PBO，readPixels异步, glMapBuffer阻塞，~6ms|done|
|6|RgbConverter6|Transform Feedback|pending|

这里方式4和5的结果差别的原因暂时没搞清楚，方式5是从Pbuffer的默认FBO读到PBO，方式4是另开的一个FBO读到PBO，这两种应该没太大区别，而结果表明方式5比较理想，glReadPixels应该是异步，阻塞只在glMapBuffer。

## **三，视频录制**

|序号|项目名称|内容简介|状态|
|--- |-------|-------|----|
|1|recorder1|GLSurfaceView + MediaMuxer，不共享EglContext，只能录制相机预览|Done|
|2|recorder2|SurfaceView + MediaMuxer，共享EglContext，可以录制整个Surface|Done|


## **四，视频播放**

|序号|项目名称|内容简介|状态|
|--- |-------|-------|----|
|1|video1|SurfaceView播放原始视频|Pending|
|2|video2|视频裁剪播放，并增加一层遮罩|Pending|


## **技术要点**

 - [相机遇到的问题](doc/相机遇到的问题.md)

------
有问题或建议可以给我邮件

Email: dingjikerbo@gmail.com