#  FileDownload
##感谢 慕课网XRay_Chen老师的[Android-Service断点续传系列课程](http://www.imooc.com/u/1395824/courses?sort=publish)

##说明
	断点续传指的就是我们可以随时停止我们的下载任务，当下次再次开始的时候，可以从上次下载到的位置继续下载，节省下载时间，
	很方便也很实用，做法就是在下载的过程中，纪录下下载到的位置，当再次开始下载的时候，我们从上一次的位置继续请求服务器即可。

##演示
  ![](https://github.com/busySnail/FileDownload/blob/master/resource/demo.gif)

##下载逻辑
	1.下载逻辑组织在Service组件中。下载文件前首先会利用HTTP Cotent-Length字段初始化RandomAccessFile和FileInfo实体（RandomAccessFile
	是实现断点续传的核心类，允许我们从我们想要的位置进行读写操作，因此，我们可以把我们要下载的文件切分成几部分，然后开启多个线程，分别
	从文件不同的位置进行下载，这样等所有的部分都下载完成之后，我们就能够得到一个完整的文件了；FileInfo记录文件完成进度）
	2.通过HTTP Range字段可以下载离散地下载一个文件的各段，对应本地文件就是RandomAccess的各段，每一段都由一个线程执行，任务起始、
	长度和完成度由ThreadInfo记录，保存在SQLite数据库中，每次重新开启下载任务，初始化线程会获取对应下载任务的ThreadInfo线程信息，
	初始化工作线程，并统一交给newCachedThreadPool进行调度执行。
	3.初始线程和工作线程、Service和MainActivity之间的消息传递是通过Messenger+Handler实现的。

##踩过的坑
	1.一开始更新UI采用在线程中发送Broadcast实现，当多个任务开启（每个任务开启3个工作线程）时，界面非常卡顿。因为Broadcast比较重量，换成
	Handler后界面响应快多了。
	2.数据类型转换的坑，文件长度一开始用int，进度条出现很灵异的现象，（跳跃，超过100%），改为long后又出现一个被截断的错误，总之，类型转换
	要格外小心，调试bug时转型前后打log
	3.多个线程都需要访问数据库，要解决同步问题：（1）数据库帮助类设置为单例，保证个线程访问的是同一个数据库。（2）对数据库访问的更新、插入、
	删除方法用synchronized进行同步。
