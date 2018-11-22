package com.jxy.util;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.Cell;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.HColumnDescriptor;
import org.apache.hadoop.hbase.HTableDescriptor;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.*;
import org.apache.hadoop.hbase.master.handler.CreateTableHandler;
import org.apache.hadoop.hbase.util.Bytes;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;
 
/**
 * HBase工具类
 * Author JiaPeng_lv
 */
public class HBaseUtils {
    private static Connection connection;
    private static Configuration configuration;
    private static HBaseUtils hBaseUtils;
    private static Properties properties;
    private static Admin admin;
 
    /**
     * 创建连接池并初始化环境配置
     */
    public void init(){
        properties = System.getProperties();
        //实例化HBase配置类
        if (configuration==null){
            configuration = HBaseConfiguration.create();
        }
        try {
            //加载本地hadoop二进制包
            properties.setProperty("hadoop.home.dir", "D:\\hadoop-2.7.2");
            //zookeeper集群的URL配置信息
            configuration.set("hbase.zookeeper.quorum","192.168.1.22");
            //HBase的Master
            configuration.set("hbase.master","hadoop-master:6000");
            //客户端连接zookeeper端口
            configuration.set("hbase.zookeeper.property.clientPort","2181");
            //HBase RPC请求超时时间，默认60s(60000)
            configuration.setInt("hbase.rpc.timeout",20000);
            //客户端重试最大次数，默认35
            configuration.setInt("hbase.client.retries.number",10);
            //客户端发起一次操作数据请求直至得到响应之间的总超时时间，可能包含多个RPC请求，默认为2min
            configuration.setInt("hbase.client.operation.timeout",30000);
            //客户端发起一次scan操作的rpc调用至得到响应之间的总超时时间
            configuration.setInt("hbase.client.scanner.timeout.period",200000);
            //获取hbase连接对象
            if (connection==null||connection.isClosed()){
                connection = ConnectionFactory.createConnection(configuration);
            }
            admin = connection.getAdmin();
    		if(admin !=null){
    			System.out.println(admin+"------------");
                try {
                    //获取到数据库所有表信息
                    HTableDescriptor[] allTable = admin.listTables();
                    System.out.println(allTable.length+"------------");
                    for (HTableDescriptor hTableDescriptor : allTable) {
                        System.out.println(hTableDescriptor.getNameAsString());
                    }
                }catch (IOException e) {
                    e.printStackTrace();
                }  
            }
            
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
 
    /**
     * 关闭连接池
     */
    public static void close(){
        try {
            if (connection!=null)connection.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
 
    /**
     * 私有无参构造方法
     */
    private HBaseUtils(){}
 
    /**
     * 唯一实例，线程安全，保证连接池唯一
     * @return
     */
    public static HBaseUtils getInstance(){
        if (hBaseUtils == null){
            synchronized (HBaseUtils.class){
                if (hBaseUtils == null){
                    hBaseUtils = new HBaseUtils();
                    hBaseUtils.init();
                }
            }
        }
        return hBaseUtils;
    }
 
    /**
     * 获取单条数据
     * @param tablename
     * @param row
     * @return
     * @throws IOException
     */
    public static Result getRow(String tablename, byte[] row) throws IOException{
        Table table = null;
        Result result = null;
        try {
            table = connection.getTable(TableName.valueOf(tablename));
            Get get = new Get(row);
            result = table.get(get);
        }finally {
            table.close();
        }
        return result;
    }
 
    /**
     * 查询多行信息
     * @param tablename
     * @param rows
     * @return
     * @throws IOException
     */
    public static Result[] getRows(String tablename,List<byte[]> rows) throws  IOException{
        Table table = null;
        List<Get> gets = null;
        Result[] results = null;
        try {
            table = connection.getTable(TableName.valueOf(tablename));
            gets = new ArrayList<Get>();
            for (byte[] row : rows){
                if(row!=null){
                    gets.add(new Get(row));
                }
            }
            if (gets.size() > 0) {
                results = table.get(gets);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            table.close();
        }
        return results;
    }
 
    /**
     * 获取整表数据
     * @param tablename
     * @return
     */
    public static ResultScanner get(String tablename) throws IOException{
        Table table = null;
        ResultScanner results = null;
        try {
            table = connection.getTable(TableName.valueOf(tablename));
            Scan scan = new Scan();
            scan.setCaching(1000);
            results = table.getScanner(scan);
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            table.close();
        }
        return results;
    }
 
    /**
     * 单行插入数据
     * @param tablename
     * @param rowkey
     * @param family
     * @param cloumns
     * @throws IOException
     */
    public static void put(String tablename, String rowkey, String family, Map<String,String> cloumns) throws IOException{
        Table table = null;
        try {
            table = connection.getTable(TableName.valueOf(tablename));
            Put put = new Put(rowkey.getBytes());
            for (Map.Entry<String,String> entry : cloumns.entrySet()){
                put.addColumn(family.getBytes(),entry.getKey().getBytes(),entry.getValue().getBytes());
            }
            table.put(put);
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            table.close();
            close();
        }
    }
    public static void main(String arg[]) {
    	getInstance();
    	getTablesName();
    }
    /**
     * 创建表
     */
    public static void createTable() {

        // 数据表表名
        String tableNameString = "student";
        String columnName = "info";

        // 新建一个数据表表名对象
        TableName tableName = TableName.valueOf(tableNameString);

        // 如果需要新建的表已经存在
        try {
            if (admin.tableExists(tableName)) {
                System.out.println("表已经存在！");
            } else {
                // 数据表描述对象
                HTableDescriptor tableDescriptor = new HTableDescriptor(tableName);

                // 列族描述对象
                HColumnDescriptor columnDescriptor = new HColumnDescriptor(columnName);

                // 在数据表中新建一个列族
                tableDescriptor.addFamily(columnDescriptor);

                // 新建数据表
                admin.createTable(tableDescriptor);
                System.out.println("创建表成功!");
            }
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("创建表失败!");
        }
    }

  /**
     * 获取所有表名
     */
    public static void getTablesName() {
        TableName[] tableNames;
        try {
            tableNames = admin.listTableNames();
            for (TableName tableName : tableNames) {
                System.out.println("已存在的表名：" + tableName);
            }
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("获取所有表名失败！");
        }
    }

   /**
     * 删除表
     * @param tableNameStr
     */
    public static void deleteTable(String tableNameStr) {
        TableName tableName = TableName.valueOf(tableNameStr);
        try {
            if (admin.tableExists(tableName)) {
                admin.disableTable(tableName);
                admin.deleteTable(tableName);
                System.out.println("表删除成功！");
            } else {
                System.out.println("表不存在！");
            }
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("表删除失败！");
        }
    }

   /**
     * 删除一条记录
     * @param tableName
     * @param rowKey
     */
    public static void deleteRecord(String tableName, String rowKey) {
        try {
            Table table = connection.getTable(TableName.valueOf(tableName));
            Delete del = new Delete(rowKey.getBytes());
            table.delete(del);
            System.out.println(tableName + " 表删除数据成功！");
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println(tableName + " 表删除数据失败！");
        }
    }

    /**
     * 插入一条数据
     * @param tableName
     * @param rowKey
     * @param columnFamily
     * @param qualifier
     * @param value
     * @return
     */
    public static boolean insertRecord(String tableName, String rowKey,
                                String columnFamily, String qualifier, String value) {
        try {
            Table table = connection.getTable(TableName.valueOf(tableName));
            Put put = new Put(rowKey.getBytes());
            put.addColumn(columnFamily.getBytes(), qualifier.getBytes(), value.getBytes());
            table.put(put);
            System.out.println("插入数据成功！！！");
            return true;
        } catch (IOException e) {
            System.out.println("插入数据失败！！！");
        }
        return false;
    }

   /**
     * 查询一条记录
     * @param tableName
     * @param rowKey
     * @return
     */
    public static Result getOneRecord(String tableName, String rowKey) {
        try {
            Table table = connection.getTable(TableName.valueOf(tableName));
            Get get = new Get(rowKey.getBytes());
            Result rs = table.get(get);
            System.out.println(tableName + " 表获取数据成功！");
            for (Cell cell : rs.rawCells()) {
                System.out.println("family:"+Bytes.toString(cell.getFamilyArray(),cell.getFamilyOffset(),cell.getFamilyLength()));
                System.out.println("qualifier:"+Bytes.toString(cell.getQualifierArray(),cell.getQualifierOffset(),cell.getQualifierLength()));
                System.out.println("value:"+Bytes.toString(cell.getValueArray(),cell.getValueOffset(),cell.getValueLength()));
                System.out.println("Timestamp:"+cell.getTimestamp());
                System.out.println("---------------");
            }
            return rs;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }



    /**
     * 查询所有的记录
     * @param tableName
     * @return
     */
    public static List<Result> getAllRecords(String tableName) {
        ResultScanner scanner = null;
        List<Result> list = new ArrayList<>();
        try {
            Table table = connection.getTable(TableName.valueOf(tableName));
            Scan scan = new Scan();
            scanner = table.getScanner(scan);
            Iterator<Result> iterator = scanner.iterator();

            while (iterator.hasNext()) {
                Result rs = iterator.next();
                list.add(rs);
                for (Cell cell : rs.rawCells()) {
                    System.out.println("Family: " + Bytes.toString(cell.getFamilyArray(), cell.getFamilyOffset(), cell.getFamilyLength()));
                    System.out.println("Qualifier: " + Bytes.toString(cell.getQualifierArray(), cell.getQualifierOffset(), cell.getQualifierLength()));
                    System.out.println("Value: " + Bytes.toString(cell.getValueArray(), cell.getValueOffset(), cell.getValueLength()));
                    System.out.println("----------------------");
                }
            }
            System.out.println(list);
            scanner.close();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (!Objects.isNull(scanner)) {
                scanner.close();
            }
        } 
        return list;
    }
}