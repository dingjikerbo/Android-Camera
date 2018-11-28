# multisurfacepreview工程

本工程的意义在于要展示如何共享gl上下文。先在主surfaceview中将相机帧绘制到纹理上，然后将纹理绘制到屏幕上显示。另外开一个mini surfaceview，用于同步显示滤镜效果，需要复用主surfaceview中的纹理，但是这两个surfaceview的gl上下文不同，如何能共享纹理呢？

所谓的gl上下文就是EGLContext，而纹理是和gl上下文绑定的，即对于两个surfaceview，由于各自有独立的gl上下文，所以纹理是不能共享的。

如果要做到纹理共享，需在创建gl上下文时指定sharedContext。这样在surfaceviewA的gl上下文中创建的纹理对于surfaceviewB也是可用的。

关于gl上下文，可参考[官方文档](https://source.android.com/devices/graphics/arch-egl-opengl)

原文如下：

`
在使用 GLES 进行任何操作之前，需要创建一个 GL 上下文。在 EGL 中，这意味着要创建一个 EGLContext 和一个 EGLSurface。GLES 操作适用于当前上下文，该上下文通过线程局部存储访问，而不是作为参数进行传递。这意味着您必须注意渲染代码在哪个线程上执行，以及该线程上的当前上下文。
`