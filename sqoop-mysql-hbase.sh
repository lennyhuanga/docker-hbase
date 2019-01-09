#!/bin/bash
#nohup ./sqoop-mysql-hbase.sh 25320106  tbl_user_finance_log_201805 &
#mysql table row num
num=$1
tablename=$2
a=100000
echo "num is $num"
j=`expr $num / $a`
j=`expr $j + 1`
#j= awk '{print int($j)==($j)?int($j):int($j)+1}' 
echo "j is $j"
for ((i=1; i<=j; i++))
do
        #       echo 'haha '
        #start =(i-1)*100000+1  end = i *100000
#       startnum=$((i-1)*100000+1)
        startnum=$((i-1))
        startnum=$((startnum*100000))
        startnum=$((startnum+1))
        endnum=$((i*100000))
        echo "startnum is $startnum endnum is $endnum"
          sqoop import "-Dorg.apache.sqoop.splitter.allow_text_splitter=true" \
        --connect jdbc:mysql://rr-bp1t2pqc4.mysql.rds.aliyuncs.com/xxx --username xxxx --password xxxx \
        --query  "select UUID() as uuid , $tablename.* from $tablename  where ID >=$startnum  and ID <=$endnum and  \$CONDITIONS" \
        --split-by ID   \
        --hbase-table tbl_user_finance_log \
        --column-family info \
        --columns UUID,ID,USERID,TYPE,OPTYPE,AMOUNT,BALANCE,CREATED_AT,UPDATED_AT,IP,DESC,SOURCE \
        --hbase-row-key UUID,USERID,CREATED_AT \
        --hbase-create-table \
        -m 32
done
~       
