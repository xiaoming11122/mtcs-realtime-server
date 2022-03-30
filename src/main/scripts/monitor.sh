#!/bin/bash
############# 加载环境
source /etc/profile
############# 伪码设置
CodeMonitorLib='%CodeMonitorLib%'
CodeMonitorJar='%CodeMonitorJar%'
CodeMonitorMain='%CodeMonitorMain%'
CodeMonitorParam='%CodeMonitorParam%'

if [[ ${CodeMonitorLib:1:${#CodeMonitorLib}} = '*' ]];
 then
    lib_param=${CodeMonitorLib}
 elif [[ ${CodeMonitorLib:1:${#CodeMonitorLib}} = '/' ]];
  then
    lib_param=${CodeMonitorLib}'*'
  else
    lib_param=${CodeMonitorLib}'/*'
fi

java -cp ${lib_param}':'${CodeMonitorJar} ${CodeMonitorMain} ${CodeMonitorParam}