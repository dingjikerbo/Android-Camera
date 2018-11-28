# glsurfacepreview工程

GLSurfaceView.Renderer的几个接口如下：

```
onSurfaceCreated
onSurfaceChanged
onDrawFrame
```

注意，这几个回调都是在render线程，这是GLSurfaceView专为OpenGL渲染创建的一个子线程。而相机的帧回调onPreviewFrame是在ui线程，如果涉及到buffer操作，记得上锁。

这个工程显示Camera预览的原理是，首先通过onPreviewFrame获取相机的NV21数据帧，然后用YUVProgram直接绘制到屏幕上。

YUVProgram中涉及NV21到RGB的转换，转换过程看yuv_fragment.glsl文件



# 深度解析

有以下几个疑问，

1. GLSurfaceView.Renderer这几个接口是怎么触发的

2. GLSurfaceView.requestRender();干嘛的

3. SurfaceTexture.updateTexImage();干嘛的

首先看第一个问题，我们需要从GLSurfaceView的源码入手，注释是这么写的

```
An implementation of SurfaceView that uses the dedicated surface for displaying OpenGL rendering.

A GLSurfaceView provides the following features:
1) Manages a surface, which is a special piece of memory that can be composited into the Android view system.
2) Manages an EGL display, which enables OpenGL to render into a surface.
3) Accepts a user-provided Renderer object that does the actual rendering.
4) Renders on a dedicated thread to decouple rendering performance from the UI thread.
5) Supports both on-demand and continuous rendering.
6) Optionally wraps, traces, and/or error-checks the renderer's OpenGL calls.
```

GLSurfaceView的核心是GLThread，这就是渲染线程，在setRenderer的时候，这个线程启动。创建gl上下文，包括glcontext和glsurface，然后通过eglMakeCurrent将该glsurface设置为当前生效。Renderer接口的几个回调都是在GLThread线程中调到的，根据状态进行回调。

再来看第二个问题，requestRender。这个函数只是设置了一个bool标志，表示readToDraw，接下来就会回调到onDrawFrame。

再来看第三个问题，可以参考[官方文档](https://developer.android.com/reference/android/graphics/SurfaceTexture)，意思是说surfaceTexture一般作为相机/MediaCodec/MediaPlayer的输出，当调用updateTexImage时，就从输出流中取一帧更新到texture上。这里要注意，我个人理解的是不论你取不取，输出流都不会停止，假如相机每秒30帧，而我们每秒只调一次updateTexImage的话，就意味着另外29帧就跳过了。不过通常情况下，我们会为surfaceTexture设置frameAvailableListener，当有帧可用时会收到回调，此时调用requestRender要求绘制，在onDrawFrame中调用updateTexImage取一帧图像更新到surfaceTexture纹理，这样只要onDrawFrame不阻塞，相机的每一帧都能及时更新到surfaceTexture的纹理上。这个frameAvailableListener也是挺奇怪的，它不是说来一帧图像，自动更新到surfaceTexture的纹理上后给我们回调，而是来一帧图像的时候给我们回调，然后由我们自己决定要不要更新到surfaceTexture的纹理上。另外，对于本工程，由于是用的相机的onPreviewFrame获取帧数据来做后续绘制的，没有用到surfaceTexture的纹理，所以这里不调用updateTexImage也没关系。但是对于glsurfacepreview3工程就需要及时调用updateTexImage了，因为后续绘制需要用到surfaceTexture的纹理。
