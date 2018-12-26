#此image在https://github.com/lennyhuanga/hadoopwithdocker
FROM lenny/hadoop:2.7.2

MAINTAINER lennyhuang <524180539@qq.com>

WORKDIR /root

# 升级vim -y的作用是在执行过程中询问yes or no 时选yes
RUN apt-get remove -y vim-common && apt-get install -y vim
# install zookeeper-3.4.10 
RUN wget  http://apache.fayea.com/zookeeper/zookeeper-3.4.10/zookeeper-3.4.10.tar.gz && \
    tar -xzvf zookeeper-3.4.10.tar.gz && \
    mv zookeeper-3.4.10 /usr/local/zookeeper && \
	chmod 777 /usr/local/zookeeper && \
	mkdir /usr/local/zookeeper/data && \
	mkdir /usr/local/zookeeper/logs && \
    rm zookeeper-3.4.10.tar.gz
	
# install hbase1.3.0
RUN wget  http://archive.apache.org/dist/hbase/1.3.0/hbase-1.3.0-bin.tar.gz  && \
    tar -xzvf hbase-1.3.0-bin.tar.gz  && \
    mv hbase-1.3.0 /usr/local/hbase && \
	chmod 777 /usr/local/hbase && \
	mkdir -p /usr/local/hbase/tmp/zk/data && \
    rm hbase-1.3.0-bin.tar.gz 

# install phoenix 4.14.1 and cp lib to hbase_home/lib/ 
RUN wget http://mirrors.tuna.tsinghua.edu.cn/apache/phoenix/apache-phoenix-4.14.1-HBase-1.3/bin/apache-phoenix-4.14.1-HBase-1.3-bin.tar.gz  && \
    tar -xzvf apache-phoenix-4.14.1-HBase-1.3-bin.tar.gz  && \
    mv apache-phoenix-4.14.1-HBase-1.3-bin  /usr/local/phoenix && \
    rm apache-phoenix-4.14.1-HBase-1.3-bin.tar.gz 
    
	


# set environment variable

ENV ZOO_HOME=/usr/local/zookeeper
ENV HBASE_HOME=/usr/local/hbase
ENV PHOENIX_HOME=/usr/local/phoenix  

# 拷贝配置文件
COPY config/* /tmp/

RUN mv /tmp/zoo.cfg $ZOO_HOME/conf/zoo.cfg && \
	mv -f /tmp/regionservers $HBASE_HOME/conf/regionservers && \
	mv -f /tmp/hbase-site.xml $HBASE_HOME/conf/hbase-site.xml && \
	mv -f /tmp/hbase-env.sh $HBASE_HOME/conf/hbase-env.sh && \
	cp  $HADOOP_HOME/etc/hadoop/hdfs-site.xml $HBASE_HOME/conf/hdfs-site.xml && \
	cp  $HADOOP_HOME/etc/hadoop/core-site.xml $HBASE_HOME/conf/core-site.xml && \
	mv  /tmp/run_hosts.sh ~/run_hosts.sh   && \
	cp -rf /tmp/zkEnv.sh $ZOO_HOME/bin/zkEnv.sh && \
	cp -rf /tmp/log4j.properties $ZOO_HOME/conf/log4j.properties && \
	cp /usr/local/phoenix/phoenix-4.14.1-HBase-1.3-server.jar  $HBASE_HOME/lib/phoenix-4.14.1-HBase-1.3-server.jar

CMD [ "sh", "-c", "service ssh start; bash"]
