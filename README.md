HBASE 的安装依赖于hadoop ，上节基于docker的hadoop安装参见https://github.com/lennyhuanga/hadoopwithdocker
所以HBASE的安装是在hadoop的image基础上进行的。
参照：https://www.cnblogs.com/netbloomy/p/6677883.html
hadoop2.7.2
hbase1.3.0
zookeeper-3.4.10

第一步  完成hadoop的iamge构建
按照https://github.com/lennyhuanga/hadoopwithdocker 完成hadoop 的image 构建

第二步 完成zookeeper的构建 参考：https://www.cnblogs.com/netbloomy/p/6658041.html

1.myid 设置
hadoop-master节点
echo "1" >> /usr/local/zookeeper/data/myid
docker restart $(docker ps -a|grep zookeeper|awk '{print $1}')
hadoop-slave1节点
echo "2" >> /usr/local/zookeeper/data/myid
docker restart $(docker ps -a|grep zookeeper|awk '{print $1}')
hadoop-slave2节点
echo "3" >> /usr/local/zookeeper/data/myid
docker restart $(docker ps -a|grep zookeeper|awk '{print $1}')

2.分别启动hadoop-master、hadoop-slave1、hadoop-slave2上的zookeeper 并查看状态
 bin/zkServer.sh start
 查看zookeeper的状态
 bin/zkServer.sh status

3.验证zookeeper集群

bin/zkCli.sh -server c7003:2181
PS:

1、由于zk运行一段时间后，会产生大量的日志文件，把磁盘空间占满，导致整个机器进程都不能活动了，所以需要定期清理这些日志文件，方法如下：

1）、写一个脚本文件cleanup.sh内容如下：

 java -cp zookeeper.jar:lib/slf4j-api-1.6.1.jar:lib/slf4j-log4j12-1.6.1.jar:lib/log4j-1.2.15.jar:conf org.apache.zookeeper.server.PurgeTxnLog <dataDir> <snapDir> -n <count>
 其中：

　　dataDir：即上面配置的dataDir的目录

      snapDir：即上面配置的dataLogDir的目录

　　count：保留前几个日志文件，默认为3

2）、通过crontab写定时任务，来完成定时清理日志的需求

crontab -e 0 0 * *  /opt/zookeeper-3.4.10/bin/cleanup.sh

3)、在其它2台机器做同样操作
 
 
 第三步构建hbase
 
 ./start-hbase.sh
 
 bin/hbase version



