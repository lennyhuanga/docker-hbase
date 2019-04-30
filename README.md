HBASE 的安装依赖于hadoop ，上节基于docker的hadoop安装参见https://github.com/lennyhuanga/hadoopwithdocker
所以HBASE的安装是在hadoop的image基础上进行的。

hadoop2.7.2
hbase1.3.0
zookeeper-3.4.10
参考 https://www.cnblogs.com/netbloomy/p/6677883.html
第一步  完成hadoop的iamge构建
按照https://github.com/lennyhuanga/hadoopwithdocker 完成hadoop 的image 构建

第二步 完成zookeeper 和 hbase的构建 参考：https://www.cnblogs.com/netbloomy/p/6658041.html

./build-image.sh
./start-container.sh



第三部 配置 zookeeper 和hbase

设置各个节点的/etc/hosts文件
./run_hosts.sh (ip地址根据事实情况进行修改)
1.myid 设置
hadoop-master节点
echo "1" >> /usr/local/zookeeper/data/myid

hadoop-slave1节点
echo "2" >> /usr/local/zookeeper/data/myid

hadoop-slave2节点
echo "3" >> /usr/local/zookeeper/data/myid


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
 
 
4. 启动hbase
 
 ./start-hbase.sh
 
 bin/hbase version
 
 
 错误大全：
 1\The node /hbase is not in ZooKeeper，Hbase端口占用无法正常启动
 解决方案：https://blog.csdn.net/wing_93/article/details/78559838
 
	2\搭建好hbase集群并启动集群后发现，HMaster在启动后几秒内自动关闭，HRegionServer运行正常。
	: Failed to become active master 
java.net.ConnectException: Call From hadoop1/192.168.2.1 to hadoop1:8020 failed on connection exception: java.net.ConnectException: Connection refused;

https://blog.csdn.net/embracejava/article/details/53189123
3\R: org.apache.hadoop.hbase.PleaseHoldException: Master is initializing
解决方案：https://blog.csdn.net/liuxiao723846/article/details/53146304
4\hbase:meta state=FAILED_OPEN 
http://community.cloudera.com/t5/Storage-Random-Access-HDFS/hbase-meta-state-FAILED-OPEN/td-p/33668


5\
,报错failed open of region

https://blog.csdn.net/qq_31598113/article/details/79585323
https://blog.csdn.net/shekey92/article/details/46549519

6\regionserver 报 Failed open of region=hbase:meta,,1.1588230740, starting to roll back the global memstore size.
java.lang.IllegalArgumentException: Wrong FS: hdfs://hadoop-master:9000/tmp/hbase-root/hbase/data/hbase/meta/1588230740/.regioninfo, expected: file:///
解决方案：按照上边两个链接是解决不掉这个问题的。因为hbase：meta是元数据，一般不会出问题，而且我按如下思路
1.停止HBase 
2. ./zkCli.sh -server hadoop-master:2181
 并运行“rmr / hbase”删除HBase znodes 
3.hdfs 中也将与hbase meta相关表删掉
4.重新启动HBase。它将重新创建znodes
还是不可以。

查了一下java.lang.IllegalArgumentException: Wrong FS: hdfs://hadoop-master:9000/tmp/hbase-root/hbase/data/hbase/meta/1588230740/.regioninfo, expected: file:///
这个错误，在hadoop中出现这个问题如hadoop项目中出现以下报错：java.lang.IllegalArgumentException: Wrong FS: hdfs://......，expected: file:///.......
解决方法：需要把hadoop集群上的core-site.xml和hdfs-site.xml放到当前工程下，然后运行即可。
那么我们将这两个文件放到hbase/conf 下，重启试了一下就ok了
cp /usr/local/hadoop/etc/hadoop/core-site.xml /usr/local/hbase/conf/


----------------------add by huanglin at 2019.1.9--------------------------------
sqoop list-databases --connect jdbc:mysql://rr-bp1t2.mysql.rds.aliyuncs.com/xxxx --username xxxx --password xxxxx
通过sqoop 将mysql大表数据同步到hbase，同步了很多次，发现hbase中数据出现了重复值，每1000w数据会比mysql多出来100w条左右，怎么搞也找不到原因
于是试了很多方式，讲大表的数据每次同步10w条不会出现重复值，20w条就出现。于是做了一个脚本分批同步
将sqoop-mysql-hbase.sh cp到/usr/local/sqoop/bin 下。然后运行：
nohup ./sqoop-mysql-hbase.sh rownums  tablename  &
比如rownums 一共多上行数据：25320106 。nohup  xxx.sh & 是不间断执行，xshell断开不影响命令执行。

----------------add by huanglin at 2019.1.10-------------------------------------
sqoop 导入数据时总多出来数据的问题找到了，hadoop job map的时候 map 有killed map出现，也就是map在执行的过程中，可能耗时太长被hadoop自动kill掉，又重新启动了新的map。所以在mapred-site.xml文件中加入以下属性，也就是把hadoop 时间推测机制 speculative 设置成false。这样hadoop就不会自主kill map了。
<property>
         <name>mapreduce.map.speculative</name>
         <value>false</value>
    </property>
   <property>
         <name>mapreduce.reduce.speculative</name>
        <value>false</value>
   </property>

---------------add by huanglin at 2019.4.16 -------------------------------------
在实际操作中 通过rabbit 和phoenix  往hbase中写日志。频繁出现regionserver 宕机问题，最后找到的原因是 hbase master 内存优化问题 
参考以下文章，将hbase-env.sh 中 HBASE_MASTER_OPTS   HBASE_REGIONSERVER_OPTS两个参数内存设置的大一点即可。
https://stackoverflow.com/questions/37879254/hbase-error-memstore-size-is-xxxxxx
https://www.jianshu.com/p/605086750c37
更深入的：https://blog.csdn.net/cm_chenmin/article/details/52994980



---------------add by huanglin at 2019.4.29 -------------------------------------

通过phoenix 从rabbitmq 消费数据到hbase 应用突然出问题
1.rabbitmq的消费和ack突然降为0，当重启spring 消费应用时 消费几十条数据就又降到0；hbase master 和regionserver 日志差不多错误。spring应用也查不到错误
2.过一段时间以后，spring应用出现了报错
Caused by: org.apache.hadoop.hbase.client.RetriesExhaustedWithDetailsException: Failed 1 action: IOException: 1 time, servers with issues: null, 


同时有这个报错

hbase的regionserver
MetaDataRegionObserver: Unable to rebuild  xxx   indexes
以为是hbase表的索引坏了，删掉索引后以上问题还是存在，所以排除了索引出问题的可能


通过这个报错查到了https://blog.csdn.net/davylee2008/article/details/70158136


a 按照上述查询 hdfs fsck /  查看hadoop集群磁盘并没有任何问题
b ./hbase hbck   
https://blog.csdn.net/xiao_jun_0820/article/details/28602213 解决region 集群一致性介绍（并没有解决问题）
hbase hbck
              Status：OK，表示没有发现不一致问题。
              Status：INCONSISTENT，表示有不一致问题。
发现了很多问题，状态是Status：INCONSISTENT报错由以下几种
1.Multiple regions have the same startkey
https://blog.csdn.net/microGP/article/details/81233132
但没有按照上述方式进行解决
2.ERROR: Found lingering reference file hdfs://xxx
http://developer.51cto.com/art/201708/549419.htm
使用./hbase -fixSplitParents 
./hbase -removeParents 
./hbase -fixReferenceFiles  
三个命令均没有解决掉该问题

然后查询了：
http://www.zhyea.com/2017/07/29/hbase-hbck-usage.html（hbase hbck用法大全）

-repair  是以下指令的简写：-fixAssignments -fixMeta -fixHdfsHoles -fixHdfsOrphans -fixHdfsOverlaps -fixVersionFile -sidelineBigOverlaps -fixReferenceFiles -fixTableLocks -fixOrphanedTableZnodes；
最后用./hbase -repair  运行后  Status：OK 
再运行一遍 ./hbase hbck  运行后  Status：OK 
关闭hbase ./stop-all.sh
启动hbase ./start-all.sh

至此该问题解决。重新启动docker 中的spring 消费应用。消费重新开始


其中spring 还有一个诡异的现象：com.alibaba.druid.pool.DruidDataSource - {dataSource-1} inited
应用打印出这句话，一直卡在这不往下运行。问题就出在hbase region出现了问题
使用hbase-repair 解决了问题就ok了。

---------------add by huanglin at 2019.4.30 -------------------------------------

ERROR: Empty REGIONINFO_QUALIFIER found in hbase:meta 问题
本质是region中有空行
https://blog.csdn.net/wyl9527/article/details/78628453
使用hbase hbck -fixEmptyMetaCells 修复。
