在Android中当需要上传或者下载多个图片或者文件到手机时，往往需要开启多个线程工作来提高效率。多线程的调度就需要用到线程池了，由于Android是基于java语言实现，所以Android中用到的多线程跟java中的多线程是一样的。下面介绍下java的线程池。

线程池分类

(1)newCachedThreadPool 
创建一个可缓存线程池，如果线程池线程数量超过处理需要，可灵活回收空闲线程，若无可回收，则新建线程。 
线程池为无限大，当执行第二个任务时第一个任务已经完成，会复用执行第一个任务的线程，而不用每次新 
建线程。 
(2)newFixedThreadPool 
创建一个定长线程池，可控制线程最大并发数，超出的线程会在队列中等待。 
newFixedThreadPool 是创建固定大小的线程池。每次提交一个任务就创建一个线程，直到线程数量达到线程池的最大大 
小。线程池的大小一旦达到最大值就会保持不变，对于超出的线程会在 LinkedBlockingQueue 队列中等待。 
(3)newScheduledThreadPool 
创建一个定长线程池，支持定时及周期性任务执行。ScheduledExecutorService比Timer更安全，功能更强 
大 (4) 
newSingleThreadExecutor 
创建一个单线程化的线程池，它只会用唯一的工作线程来执行任务，保证所有任务按照指定顺序(FIFO, LIFO, 
优先级)执行
我们主要是使用newFixedThreadPool来创建核心线程数量固定的线程池，一般设置线程数量为手机CPU核数一样多时系统效率最高了，在现实中应该根据实际需求来设置线程池的线程数量。

线程池的主要方法

在做多线程工作时，一般会使用到如下的几个API：

shutdown 
void shutdown() 
启动一次顺序关闭，执行以前提交的任务，但不接受新任务。如果已经关闭，则调用没有其他作用。

shutdownNow 
List shutdownNow() 
试图停止所有正在执行的活动任务，暂停处理正在等待的任务，并返回等待执行的任务列表。 
无法保证能够停止正在处理的活动执行任务，但是会尽力尝试。例如，通过 Thread.interrupt() 来取消典型的实现，所以任何任务无法响应中断都可能永远无法终止。

submit 
future submit(Runnable task) 
提交一个 Runnable 任务用于执行，并返回一个表示该任务的 Future。该 Future 的 get 方法在 成功 完成时将会返回 null。
当提交完所有任务后，线程池必须调用shutdown来关闭线程池释放资源，否则线程池一直在等待新任务的加入。当需要中断所有任务的执行时，可以调用shutdownNow来强制中断正在执行的线程，等待执行的线程会返回，不会被中断。submit主要是用来提交一个任务到线程中去执行的。

CountDownLatch来监听结果

当使用多线程去下载或者上传时，由于多个线程互不干扰的执行，怎么判断所有的线程是否执行完毕呢？线程池没有提供这样的方法，那么只能自己去实现了。一般可以设置一个整形的标志位，初始化为0，当一个线程完成后就把这个标志位+1，然后判断标志位是否等于=任务的数量，等于就代表所有任务都执行完成了，但是这样感觉不是很优雅。java中提供了一个计数器，我们可以使用CountDownLatch来判断所有任务是否完成。

官方定义

CountDownLatch 
一个同步辅助类，在完成一组正在其他线程中执行的操作之前，它允许一个或多个线程一直等待。 
用给定的计数 初始化 CountDownLatch。由于调用了 countDown() 方法，所以在当前计数到达零之前，await 方法会一直受阻塞。之后，会释放所有等待的线程，await 的所有后续调用都将立即返回。这种现象只出现一次——计数无法被重置。
CountDownLatch是JAVA提供在java.util.concurrent包下的一个辅助类，可以把它看成是一个计数器，其内部维护着一个count计数，只不过对这个计数器的操作都是原子操作，同时只能有一个线程去操作这个计数器，CountDownLatch通过构造函数传入一个初始计数值，调用者可以通过调用CounDownLatch对象的countDown()方法，来使计数减1；如果调用对象上的await()方法，那么调用者就会一直阻塞在这里，直到别人通过countDown方法，将计数减到0，然后唤醒await阻塞的线程，才可以继续执行。

根据上面的定义，CountDownLatch在初始化时必须传入一个整形数，设为需要执行的任务数量。CountDownLatch里面的计数值无法重置，每次只能通过重新new来生成新的对象。我们可以分成两种线程：一种是上传或者下载的工作线程，当执行完毕后调用countDown使计数器-1，表示执行完毕；一种是监听线程，调用await方法阻塞，直到CounDownLatch里面的计数为0才开始执行，这时就可以通过异步回调通知主线程所有任务都已经完成了。

实现过程

首先需要定义3个接口，1个是主线程异步回调使用，主要用来将结果回调到UI线程上，从而来控制UI显示；1个是任务线程的回调接口，包括了进度，完成或者失败的回调，主要用来监听任务线程的状态；1个是监听所有线程执行结果的回调结果，在上面说道了需要一个监听线程，来判断所有线程是否执行完成。

主线程异步回调接口：

public interface OnUploadListener {//主线程回调
    void onAllSuccess();
    void onAllFailed();
    void onThreadProgressChange(int position,int percent);
    void onThreadFinish(int position);
    void onThreadInterrupted(int position);
}

任务线程的回调接口：

public interface OnThreadResultListener {
    void onProgressChange(int percent);//进度变化回调
    void onFinish();//线程完成时回调
    void onInterrupted();//线程被中断回调
}

监听线程回调接口：

public interface OnAllThreadResultListener {
    void onSuccess();//所有线程执行完毕
    void onFailed();//所有线程执行出现问题
}

定义Runnable对象UploadFile的任务线程：

public class UploadFile implements Runnable {
    private CountDownLatch downLatch;//计数器
    private String fileName;//文件名
    private OnThreadResultListener listener;//任务线程回调接口
    private int percent=0;//进度
    private Random mRandom;//随机数 模拟上传

    public UploadFile(CountDownLatch downLatch,String fileName,OnThreadResultListener listener){
        this.downLatch=downLatch;
        this.fileName=fileName;
        this.listener=listener;

        mRandom=new Random();
    }

    @Override
    public void run() {
        try {
            while(percent<=100){
                listener.onProgressChange(percent);
                percent+=1;
                Thread.sleep(mRandom.nextInt(60)+30);//模拟延迟
            }
            this.downLatch.countDown();
            listener.onFinish();//顺利完成
        } catch (InterruptedException e) {
            listener.onInterrupted();//被中断
        }
    }
}

定义监听线程UploadListener来判断所有线程的执行与否

public class UploadListener implements Runnable {
    private CountDownLatch downLatch;
    private OnAllThreadResultListener listener;

    public UploadListener(CountDownLatch countDownLatch,OnAllThreadResultListener listener){
        this.downLatch=countDownLatch;
        this.listener=listener;
    }

    @Override
    public void run() {
        try {
            downLatch.await();
            listener.onSuccess();//顺利完成
        } catch (InterruptedException e) {
            listener.onFailed();
        }
    }
}

最后封装一个UploadUtil对象来操作，参考了AsyncTask实现，AsyncTask的本质就是线程池和handler的封装，跟我们多线程上传下载很相似。在子线程中做耗时操作，完成后将结果回调到UI线程上。
