# glsurfacepreview3工程

本工程和前两个工程的区别在于，前两个工程都需要通过onPreviewFrame拿到相机的帧数据，然后再做后续处理。而本工程直接给相机的预览纹理绘制到屏幕上。

注意这两种纹理的区别，前两个工程都要先给相机的NV21转成RGB，所用的纹理类型是普通的2D纹理，而本工程用的GL_TEXTURE_EXTERNAL_OES类型的纹理，所以在fragment shader的写法也略有不同，如下：

```
#extension GL_OES_EGL_image_external : require

precision mediump float;

varying vec2 vTextureCoord;
uniform samplerExternalOES sTexture;

void main() {
    gl_FragColor = texture2D(sTexture, vTextureCoord);
}
```

普通纹理的类型是sampler2D，而此处纹理类型是samplerExternalOES，注意这两者区别。