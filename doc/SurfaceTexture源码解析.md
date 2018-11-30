以下源码为Android 5.0

SurfaceTexture在Java层只是个壳，不过有三个成员变量要注意

```
private long mSurfaceTexture;
private long mProducer;
private long mFrameAvailableListener
```

这必定是对应着native层的三个对象，mFrameAvailableListener不是我们Java层设置的那个listener

SurfaceTexture构造函数中会调到Native层的SurfaceTexture_init：

```
static void SurfaceTexture_init(JNIEnv* env, jobject thiz, jboolean isDetached,
        jint texName, jboolean singleBufferMode, jobject weakThiz)
{
    sp<IGraphicBufferProducer> producer;
    sp<IGraphicBufferConsumer> consumer;
    BufferQueue::createBufferQueue(&producer, &consumer);

    sp<GLConsumer> surfaceTexture;

    surfaceTexture = new GLConsumer(consumer, texName,
            GL_TEXTURE_EXTERNAL_OES, true, true);

    SurfaceTexture_setSurfaceTexture(env, thiz, surfaceTexture);
    SurfaceTexture_setProducer(env, thiz, producer);

    jclass clazz = env->GetObjectClass(thiz);

    sp<JNISurfaceTextureContext> ctx(new JNISurfaceTextureContext(env, weakThiz, clazz));
    surfaceTexture->setFrameAvailableListener(ctx);
    SurfaceTexture_setFrameAvailableListener(env, thiz, ctx);
}
```

可见Java层的mSurfaceTexture对应着Native层的GLConsumer，里面包含IGraphicBufferConsumer，而Java层的mProducer对应着IGraphicBufferProducer。Java层的mFrameAvailableListener对应着JNISurfaceTextureContext，这个对象设置到GLConsumer中了。

整个SurfaceTexture.cpp要注意的有以下几个点：

1，updateTexImage的实现

2，BufferQueue::createBufferQueue(&producer, &consumer);

先看createBufferQueue的实现：

```
void BufferQueue::createBufferQueue(sp<IGraphicBufferProducer>* outProducer,
        sp<IGraphicBufferConsumer>* outConsumer,
        const sp<IGraphicBufferAlloc>& allocator) {
    sp<BufferQueueCore> core(new BufferQueueCore(allocator));
    sp<IGraphicBufferProducer> producer(new BufferQueueProducer(core));
    sp<IGraphicBufferConsumer> consumer(new BufferQueueConsumer(core));

    *outProducer = producer;
    *outConsumer = consumer;
}
```

这里先创建一个BufferQueueCore，传入了一个IGraphicBufferAlloc，如果传的为空，则下面会创建一个，这显然是一个Binder。

```
if (allocator == NULL) {
    sp<ISurfaceComposer> composer(ComposerService::getComposerService());
    mAllocator = composer->createGraphicBufferAlloc();
}
```

此处的composer为BpSurfaceComposer，Bn端在SurfaceFlinger中，显然SurfaceFlinger是继承自BnSurfaceComposer的。这里直接创建了一个GraphicBufferAlloc返回了。

```
sp<IGraphicBufferAlloc> SurfaceFlinger::createGraphicBufferAlloc() {
    sp<GraphicBufferAlloc> gba(new GraphicBufferAlloc());
    return gba;
}
```

关于GraphicBufferAlloc，我们下面再讨论，这里先跳过。

接下来看看BufferQueueProducer，继承自BnGraphicBufferProducer，我们重点看dequeueBuffer/queueBuffer。

```
status_t BufferQueueProducer::dequeueBuffer(int *outSlot,
        sp<android::Fence> *outFence, bool async,
        uint32_t width, uint32_t height, uint32_t format, uint32_t usage) {
    status_t returnFlags = NO_ERROR;
    
    int found;
    status_t status = waitForFreeSlotThenRelock("dequeueBuffer", async,
            &found, &returnFlags);

    *outSlot = found;

    mSlots[found].mBufferState = BufferSlot::DEQUEUED;

    const sp<GraphicBuffer>& buffer(mSlots[found].mGraphicBuffer);
    if ((buffer == NULL) ||
            (static_cast<uint32_t>(buffer->width) != width) ||
            (static_cast<uint32_t>(buffer->height) != height) ||
            (static_cast<uint32_t>(buffer->format) != format) ||
            ((static_cast<uint32_t>(buffer->usage) & usage) != usage))
    {
        mSlots[found].mGraphicBuffer = NULL;
        returnFlags |= BUFFER_NEEDS_REALLOCATION;
    }

    if (returnFlags & BUFFER_NEEDS_REALLOCATION) {
        sp<GraphicBuffer> graphicBuffer(mCore->mAllocator->createGraphicBuffer(
                    width, height, format, usage, &error));

        mSlots[*outSlot].mGraphicBuffer = graphicBuffer;
    }
    return returnFlags;
}
```

这个dequeueBuffer逻辑很简单，首先找有不有可用的buffer，如果没有或尺寸不对，则重新分配buffer，否则将其状态置为DEQUEUED，表示被占用了。稍后我们再看这个allocator是怎么createGraphicBuffer的。

接下来看queueBuffer的实现，这里为了让逻辑清晰，省略了一些代码：

```
status_t BufferQueueProducer::queueBuffer(int slot,
        const QueueBufferInput &input, QueueBufferOutput *output) {
    sp<IConsumerListener> listener;

    mSlots[slot].mBufferState = BufferSlot::QUEUED;

    BufferItem item;
    item.mGraphicBuffer = mSlots[slot].mGraphicBuffer;
    item.mSlot = slot;

    mCore->mQueue.push_back(item);
    listener = mCore->mConsumerListener;

    if (listener != NULL) {
        listener->onFrameAvailable();
    }

    return NO_ERROR;
}
```

首先给buffer状态设置为QUEUED，然后创建了一个BufferItem，丢到BufferQueueCore的FIFO队列中，然后回调通知Consumer。

接下来回到GraphicBufferAlloc，是继承自BnGraphicBufferAlloc，看createGraphicBuffer的实现：

```
sp<GraphicBuffer> GraphicBufferAlloc::createGraphicBuffer(uint32_t w, uint32_t h, PixelFormat format, uint32_t usage, status_t* error) {
    sp<GraphicBuffer> graphicBuffer(new GraphicBuffer(w, h, format, usage));
    return graphicBuffer;
}

```

注意，allocator是运行在SurfaceFlinger中的，创建的GraphicBuffer如何跨进程返回到Surface所在进程？我们看一下这个GraphicBuffer有什么神奇的地方。我们看下这个类定义，实现了Flattenable接口，这允许它序列化到ByteBuffer和文件描述符数组。我们看其实现：

```

status_t GraphicBuffer::unflatten(
        void const*& buffer, size_t& size, int const*& fds, size_t& count) {
    int const* buf = static_cast<int const*>(buffer);
    if (buf[0] != 'GBFR') return BAD_TYPE;

    const size_t numFds  = buf[8];
    const size_t numInts = buf[9];

    const size_t sizeNeeded = (10 + numInts) * sizeof(int);
    size_t fdCountNeeded = 0;

    if (handle) {
        // free previous handle if any
        free_handle();
    }

    if (numFds || numInts) {
        width  = buf[1];
        height = buf[2];
        stride = buf[3];
        format = buf[4];
        usage  = buf[5];
        native_handle* h = native_handle_create(numFds, numInts);
        memcpy(h->data,          fds,     numFds*sizeof(int));
        memcpy(h->data + numFds, &buf[10], numInts*sizeof(int));
        handle = h;
    } else {
        width = height = stride = format = usage = 0;
        handle = NULL;
    }

    mId = static_cast<uint64_t>(buf[6]) << 32;
    mId |= static_cast<uint32_t>(buf[7]);

    mOwner = ownHandle;

    if (handle != 0) {
        mBufferMapper.registerBuffer(handle);
    }

    buffer = reinterpret_cast<void const*>(static_cast<int const*>(buffer) + sizeNeeded);
    size -= sizeNeeded;
    fds += numFds;
    count -= numFds;

    return NO_ERROR;
}
```

这里unflatten是Parcel调用的，当跨进程收到数据后，Parcel会解包出buf和fds，然后调用对应类的unflatten函数还原出对象。注意这里fds跨进程后可能已经变了。再来看registerBuffer，这是GraphicBufferMapper类中的函数，

```
GraphicBufferMapper::GraphicBufferMapper()
    : mAllocMod(0) {
    hw_module_t const* module;
    int err = hw_get_module(GRALLOC_HARDWARE_MODULE_ID, &module);
    if (err == 0) {
        mAllocMod = (gralloc_module_t const *)module;
    }
}

status_t GraphicBufferMapper::registerBuffer(buffer_handle_t handle) {
    status_t err;
    err = mAllocMod->registerBuffer(mAllocMod, handle);
    return err;
}
```

此处registerBuffer为平台相关的，作用是将handle中的描述符都映射到本进程的地址空间。

好了，总结一下，SurfaceTexture包含一个producer和consumer，相机预览的时候，将producer端挂到相机上，当有新的帧queueBuffer时，会受到frameAvailable的回调，此时updateTexImage从最近的图像刘中获取一帧更新到纹理上。

再来看updateTexImage的实现，就是

```
static void SurfaceTexture_updateTexImage(JNIEnv* env, jobject thiz) {
    sp<GLConsumer> surfaceTexture(SurfaceTexture_getSurfaceTexture(env, thiz));
    status_t err = surfaceTexture->updateTexImage();
}

status_t GLConsumer::updateTexImage() {
    BufferQueue::BufferItem item;

    err = acquireBufferLocked(&item, 0);

    // Release the previous buffer.
    err = updateAndReleaseLocked(item);

    // Bind the new buffer to the GL texture, and wait until it's ready.
    return bindTextureImageLocked();
}

status_t GLConsumer::bindTextureImageLocked() {
    glBindTexture(mTexTarget, mTexName);、
    EGLImageKHR image = mEglSlots[mCurrentTexture].mEglImage;
    glEGLImageTargetTexture2DOES(mTexTarget, (GLeglImageOES)image);
    return doGLFenceWaitLocked();
}
```

总结一下，surfaceTexture初始化时就是创建一个BufferQueue，而创建BufferQueue就是要创建好producer和consumer，此外初始化好buffer的队列，还有allocator。这个allocator是个binder，具体实现在SurfaceFlinger中。而此处的BufferQueueProducer是Bn端，为什么呢，因为producer通常是是camera或者decoder，是另一个进程的，相当于client，而surfaceTexture相当于service，所以持有bn端。远端的camera如有帧数据来时，会调用dequeBuffer取一个buffer塞数据，塞好后再queueBuffer将其丢回到BufferQueue，然后通知consumer。BufferQueue是跑在surfaceTexture进程的，队列也是维护在自己进程，只不过allocator是要调到SurfaceFlinger进程，这么重要的事当然要交给系统来干。

这里面很重要的一件事是allocator在SurfaceFlinger进程分配的buffer，如何同步到surfaceTexture进程，看一下GraphicBuffer，实现了Flattenable接口，每次SurfaceFlinger分配好后，就生成一个描述符，然后跨进程传到对端进程，对端收到后，只要在unflatten解包时顺便映射一下，就能获取该buffer在本进程的内存地址，这个映射应该是通过gralloc的驱动。GraphicBuffer中真正表示内存的是ANativeWindowBuffer对象。

整个BufferQueue的数据结构就是一个slots数组和一个fifo队列。slots用于表示所有的buffer，记录buffer各种状态。而fifo队列是可用的buffer队列，方便consumer使用，直接从队列头取一个buffer就能用了。producer来一帧数据直接丢到队列尾即可。

而Camera的setPreviewTexture，其实就是给surfaceTexture的producer端设置给camera，然后camera内部拿着这个producer创建一个surface，作为preview window。