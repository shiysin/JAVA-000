学习笔记

## 各种GC的不同

#### 1. Serial串行GC：

- Serial是最基本、历史最悠久的垃圾收集器，使用复制算法，曾经是JDK1.3.1之前新生代唯一的垃圾收集器。

- Serial是一个单线程的收集器，它不仅仅只会使用一个CPU或一条线程去完成垃圾收集工作，并且在进行垃圾收集的同时，必须暂停其他所有的工作线程，直到垃圾收集结束。

- Serial垃圾收集器虽然在收集垃圾过程中需要暂停所有其他的工作线程，但是它简单高效，对于限定单个CPU环境来说，没有线程交互的开销，可以获得最高的单线程垃圾收集效率，因此Serial垃圾收集器依然是java虚拟机运行在Client模式下默认的新生代垃圾收集器。

#### 2. Parallel并行GC：

- arallel Scavenge收集器也是一个新生代垃圾收集器，同样使用复制算法，也是一个多线程的垃圾收集器，它重点关注的是程序达到一个可控制的吞吐量（Thoughput，CPU用于运行用户代码的时间/CPU总消耗时间，即吞吐量=运行用户代码时间/(运行用户代码时间+垃圾收集时间)），高吞吐量可以最高效率地利用CPU时间，尽快地完成程序的运算任务，主要适用于在后台运算而不需要太多交互的任务。

-  Parallel Scavenge收集器提供了两个参数用于精准控制吞吐量：

  - -XX:MaxGCPauseMillis：控制最大垃圾收集停顿时间，是一个大于0的毫秒数。

  - -XX:GCTimeRation：直接设置吞吐量大小，是一个大于0小于100的整数，也就是程序运行时间占总时间的比率，默认值是99，即垃圾收集运行最大1%（1/(1+99)）的垃圾收集时间。

- Parallel Scavenge是吞吐量优先的垃圾收集器，它还提供一个参数：-XX:+UseAdaptiveSizePolicy，这是个开关参数，打开之后就不需要手动指定新生代大小(-Xmn)、Eden与Survivor区的比例(-XX:SurvivorRation)、新生代晋升年老代对象年龄(-XX:PretenureSizeThreshold)等细节参数，虚拟机会根据当前系统运行情况收集性能监控信息，动态调整这些参数以达到最大吞吐量，这种方式称为GC自适应调节策略，自适应调节策略也是ParallelScavenge收集器与ParNew收集器的一个重要区别。

#### 3. CMS

- Concurrent mark sweep(CMS)收集器是一种年老代垃圾收集器，其最主要目标是获取最短垃圾回收停顿时间，和其他年老代使用标记-整理算法不同，它使用多线程的标记-清除算法。

- 最短的垃圾收集停顿时间可以为交互比较高的程序提高用户体验，CMS收集器是Sun HotSpot虚拟机中第一款真正意义上并发垃圾收集器，它第一次实现了让垃圾收集线程和用户线程同时工作。

- CMS工作机制相比其他的垃圾收集器来说更复杂，整个过程分为以下4个阶段：
  - 初始标记：只是标记一下GC Roots能直接关联的对象，速度很快，仍然需要暂停所有的工作线程。
  - 并发标记：进行GC Roots跟踪的过程，和用户线程一起工作，不需要暂停工作线程。

  - 重新标记：为了修正在并发标记期间，因用户程序继续运行而导致标记产生变动的那一部分对象的标记记录，仍然需要暂停所有的工作线程。

  - 并发清除：清除GC Roots不可达对象，和用户线程一起工作，不需要暂停工作线程。

- 由于耗时最长的并发标记和并发清除过程中，垃圾收集线程可以和用户现在一起并发工作，所以总体上来看CMS收集器的内存回收和用户线程是一起并发地执行。

- CMS收集器有以下三个不足：
  - CMS收集器对CPU资源非常敏感，其默认启动的收集线程数=(CPU数量+3)/4，在用户程序本来CPU负荷已经比较高的情况下，如果还要分出CPU资源用来运行垃圾收集器线程，会使得CPU负载加重。

  - CMS无法处理浮动垃圾(Floating Garbage)，可能会导致Concurrent ModeFailure失败而导致另一次Full GC。由于CMS收集器和用户线程并发运行，因此在收集过程中不断有新的垃圾产生，这些垃圾出现在标记过程之后，CMS无法在本次收集中处理掉它们，只好等待下一次GC时再将其清理掉，这些垃圾就称为浮动垃圾。
  - CMS收集器是基于标记-清除算法，因此不可避免会产生大量不连续的内存碎片，如果无法找到一块足够大的连续内存存放对象时，将会触发因此Full GC。CMS提供一个开关参数-XX:+UseCMSCompactAtFullCollection，用于指定在Full GC之后进行内存整理，内存整理会使得垃圾收集停顿时间变长，CMS提供了另外一个参数-XX:CMSFullGCsBeforeCompaction，用于设置在执行多少次不压缩的Full GC之后，跟着再来一次内存整理。

>  CMS垃圾收集器不能像其他垃圾收集器那样等待年老代机会完全被填满之后再进行收集，需要预留一部分空间供并发收集时的使用，可以通过参数-XX:CMSInitiatingOccupancyFraction来设置年老代空间达到多少的百分比时触发CMS进行垃圾收集，默认是68%。

> 如果在CMS运行期间，预留的内存无法满足程序需要，就会出现一次ConcurrentMode Failure失败，此时虚拟机将启动预备方案，使用Serial Old收集器重新进行年老代垃圾回收。



#### 4. G1

- Garbage first垃圾收集器是目前垃圾收集器理论发展的最前沿成果，相比与CMS收集器，G1收集器两个最突出的改进是：

  - 基于标记-整理算法，不产生内存碎片。

  - 可以非常精确控制停顿时间，在不牺牲吞吐量前提下，实现低停顿垃圾回收。

- G1收集器避免全区域垃圾收集，它把堆内存划分为大小固定的几个独立区域，并且跟踪这些区域的垃圾收集进度，同时在后台维护一个优先级列表，每次根据所允许的收集时间，优先回收垃圾最多的区域。

- 区域划分和优先级区域回收机制，确保G1收集器可以在有限时间获得最高的垃圾收集效率。



## HttpClient

```java
package com.geektime.week2;


import okhttp3.*;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

import java.io.IOException;
import java.util.Scanner;

public class HttpClientTest {

    private static final String url = "http://localhost:8801/";

    public static void main(String[] args) {
        try {
            httpClient();
//            OkHttpClient();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void httpClient() throws IOException {
        CloseableHttpClient client = HttpClients.custom().build();
        HttpUriRequest request = RequestBuilder.get()
                .setUri(url)
                .setHeader(HttpHeaders.CONTENT_TYPE, "application/json")
                .build();
        HttpResponse response = client.execute(request);

        Scanner scanner = new Scanner(response.getEntity().getContent());

        System.out.println(response.getStatusLine());
        while (scanner.hasNext()) {
            System.out.println(scanner.nextLine());
        }
    }

    public static void OkHttpClient() throws IOException {
        OkHttpClient okHttpClient = new OkHttpClient();
        Request request = new Request.Builder()
                .url(url)
                .header("User-Agent","")
                .build();
        Response response = okHttpClient.newCall(request).execute();
        System.out.println(response.isSuccessful());
        System.out.println(response.headers());
        System.out.println("responseBody: " + response.body().string());
    }
}

```



