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