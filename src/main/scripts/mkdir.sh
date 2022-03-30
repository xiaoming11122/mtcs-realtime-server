#!/bin/bash

source /etc/profile

targetDir=$1

echo "目标目录：${targetDir}"

if [ ! -d "${targetDir}" ]; then
  mkdir -p ${targetDir}
fi