
伪分布式
[kla90@v9x-gw-1 conf]$ ll
total 32
-rw-r--r--  1 kla90 staff  535 Aug 23 00:39 configuration.xsl
-rw-r--r--  1 kla90 staff 2161 Aug 23 00:39 log4j.properties
-rw-r--r--  1 kla90 staff 1025 Dec 18 23:56 zoo.cfg
-rw-r--r--  1 kla90 staff  922 Aug 23 00:39 zoo_sample.cfg
-rw-r--r--  1 kla90 staff 1025 Dec 18 23:57 zoo_server1.cfg
-rw-r--r--  1 kla90 staff 1025 Dec 18 23:57 zoo_server2.cfg
-rw-r--r--  1 kla90 staff 1025 Dec 18 23:58 zoo_server3.cfg
-rw-r--r--  1 kla90 staff  922 Nov 21 01:04 zoo_standalone.cfg

[kla90@v9x-gw-1 conf]$ cat  zoo_server1.cfg

dataDir=/klaL/jiazhao/expr/zookeeper/1  
# the port at which the clients will connect
clientPort=2181

server.1=localhost:6887:7887
server.2=localhost:6888:7888
server.3=localhost:6889:7889

[kla90@v9x-gw-1 conf]$ cat  zoo_server2.cfg

dataDir=/klaL/jiazhao/expr/zookeeper/2  
# the port at which the clients will connect
clientPort=2182

server.1=localhost:6887:7887
server.2=localhost:6888:7888
server.3=localhost:6889:7889


bin/zkServer.sh start zk1.cfg
bin/zkServer.sh status zk1.cfg

[kla90@v9x-gw-1 zookeeper-3.4.9]$ sh bin/zkServer.sh status conf/zoo_server1.cfg
ZooKeeper JMX enabled by default
Using config: conf/zoo_server1.cfg
Mode: follower

bin/zkCli.sh -server 192.168.1.201:2181

[kla90@v9x-gw-1 zookeeper-3.4.9]$ pwd
/klaL/jiazhao/frameworks/zookeeper-3.4.9
[kla90@v9x-gw-1 zookeeper-3.4.9]$ java -cp .:zookeeper-qs.jar:zookeeper-3.4.9.jar:lib/slf4j-api-1.6.1.jar qs.zookeeper.example01.Main localhost:2181 /test nodeContent /klaL/jiazhao/frameworks/zookeeper-3.4.9/test.sh