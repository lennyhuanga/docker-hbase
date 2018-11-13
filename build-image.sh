#!/bin/bash

echo ""

echo -e "\nbuild docker hive  image\n"
sudo docker build -t lenny/hbase:1.0 .

echo ""
