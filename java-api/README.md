
----java连接hbase

第一步：
	利用spring boot 创建一个项目，按照文件夹里的pom.xml 将依赖配置好。

	注意：依赖的版本必须跟环境中的hbase 版本 hadoop版本一致，否则会出现很多诡异的事情。

第二步：
	要将hbase-site.xml 文件放到reources文件夹下

第三步：

	要根据相应的情况配置，比如： configuration.set("hbase.zookeeper.quorum","hadoop-master");

	将etc/hosts 中将hadoop-master 添加以下相应的ip地址



注意：在java连接hbase时经常出各种各样的问题，大体就上边几个问题。
	再就是java 连接hbase时需要连接 16010 16020 6000 2181 等端口号，如果是docker 运行的hbase的话，要将这些端口映射一下。
