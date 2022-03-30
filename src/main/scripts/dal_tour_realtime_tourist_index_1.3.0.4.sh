#!/bin/bash
#***********************************************************************************
# **  文件名称: dal_tour_realtime_tourist_index_1.3.0.4.sh
# **  创建日期:  2019年04月08日 
# **  编写人员:  丁静波
# 
# **  输入信息: 
# **  输出信息: 
# **
# **  功能描述: 
# **  处理过程:
# **  Copyright(c) 2018 TianYi Cloud Technologies (China), Inc.
# **  All Rights Reserved.
#***********************************************************************************

#***********************************************************************************
#==修改日期==|===修改人=====|======================================================|
# .修改时间
#***********************************************************************************
baseDirForScriptSelf=$(cd "$(dirname "$0")"; pwd)
BASE_DIR=${baseDirForScriptSelf}
BIN_DIR=${BASE_DIR}/../../bin
JAR_DIR=${BASE_DIR}/../../lib
LOG_DIR=${BASE_DIR}/../../logs
CONF_DIR=${BASE_DIR}/../../conf
cd ${BIN_DIR}
source ~/.bash_profile
source ${baseDirForScriptSelf}/default.properties
source ${baseDirForScriptSelf}/common.fun
echo "${baseDirForScriptSelf}/default.properties"
echo "${baseDirForScriptSelf}/common.fun"
ScriptName=$0

###############输入参数校验区####################################################################
#获取输入的第一个参数的长度
para_len=$(echo $1 | awk -F '' '{print NF}')
echo "The length of the input parameter is $para_len."
#获取文件名称被"_"切割成多少段
num=$(echo $ScriptName | awk -F '_' '{print NF}')
#段数-1
str_type=$(($num-1))
#获取表的类型(月/日表)
table_type=$(echo $ScriptName | cut -d '_' -f $str_type)
if [ $# != 1 ];then
    echo "The paramemter you entered is not one.Please adjust it."
    exit 1;
elif [ "$para_len" = 6 -a "$table_type" = "m" ];then
    para_type=$(echo $1 | grep '^[0-9]\{6\}$' | grep '^[1-9]')
    if [ "$para_type" = "" ];then
        echo "The parameters you enter contain non-numeric values."
        exit 1;
    fi
elif [ "$para_len" = 8 -a "$table_type" = "d" ] || [ "$para_len" = 8 -a "$table_type" = "w" ];then
    para_type=$(echo $1 | grep '^[0-9]\{8\}$' | grep '^[1-9]')
    if [ "$para_type" = "" ];then
        echo "The parameters you enter contain non-numeric values."
        exit 1;
    fi
else
    echo "The length of the parameter you entered is incorrect."
    #exit 1;
fi

#日期值的合规性校验
function Date_Check(){
para=$1
para_len=$(expr length $para)
year=$(expr substr $para 1 4)
month=$(expr substr $para 5 2)
day=$(expr substr $para 7 2)
#取年份部分除以4，100，400的余数，用以判断是平年还是闰年
remainder1=$(expr $year % 4)
remainder2=$(expr $year % 100)
remainder3=$(expr $year % 400)
#根据月份和平年闰年进行判断给出返回值
case $month in
01|03|05|07|08|10|12)
    if [ "$para_len" = 6 ];then
        return 0
    elif [ $day -le 31 -a $day -ge 01 ];then
        return 0
    else
        return 1
    fi
    ;;
04|06|09|11)
    if [ "$para_len" = 6 ];then
        return 0
    elif [ $day -ge 01 -a $day -le 30 ];then
        return 0
    else
        return 1
            fi
    ;;
02)
    if [ "$para_len" = 6 ];then
        return 0
    elif [ $remainder1 -eq 0 -a $remainder2 -ne 0 -o $remainder3 -eq 0 ];then
        if [ "$day" -ge 01 -a "$day" -le 29 ];then
            return 0
        else
            return 1
        fi
    elif [ "$day" -ge 01 -a "$day" -le 28 ];then
        return 0
    else
        return 1
    fi
    ;;
esac
    return 1
}

Date_Check $1

if [ $? != 0 ];then
    echo "The date value you entered is irregular."
    exit 1;
fi

###############配置区############################################################################
#日志输出路径
#/data11/dacp/dws_pro/logs
#dwi层的脚本统一路径为/data11/dacp/dwi_pro/logs
#dws层的脚本统一路径为/data11/dacp/dws_pro/logs
#dal层的脚本需要分集市，除了BDCSC用：/data11/dacp/dal_pro/logs
#其余用/data11/dacp/集市功能账号（一般为库名，不明确的需确认）/logs
DACPDIR=$logdir
LOGPATH=${DACPDIR}
echo LOGPATH=${LOGPATH}
if [ ! -d "${LOGPATH}" ]; then
        mkdir -p ${LOGPATH}
fi
#表名称
LOGNAME="dal_tour_realtime_scenic_tourist_index"

#库名、队列名
USERNAME=$USERNAME
QUEUENAME=$QUEUENAME
#测试区队列，提交验收时注释掉
#QUEUENAME="root.test.test15"

##############SQL变量############################################################################
##############逗号分割###########################################################################
DATES=20170301



if [[ ! ${concurrency} ]];then
   PROVS=1
else
   #ods省份编码
    PROVS=$PROVS
fi
#################################################################################################
#报错发送信息,联系邮箱#邮件组
ARREMAIL='chenkai@bigdata.com'
echo $PROVS+$ARREMAIL

###############脚本参数判断######################################################################
#不输入参数,月份默认上个月,省份默认为配置省份
#输入参数为月份（例如201608）,月份默认上个月,省份默认为配置省份
#输入参数为dates,月份、省份为配置月份、省份
#################################################################################################
QUEUE1=$(echo $1|awk -F '.' '{print $1}')
QUEUE2=$(echo $2|awk -F '.' '{print $1}')
if [ $# -eq 1 ] && [ "$1"x != "dates"x ]  && [ "$QUEUE1"x != "queue"x ];then
  if [ "$1"x != "dates"x ];then
	   if echo $1|grep -Eq "[0-9]{8}" && date -d $1 +%Y%m%d > /dev/null 2>&1;then
          DATES=($1)
       else 
          echo "输入的日期格式不正确"
          exit 1 
       fi        
    fi
elif [ $# -eq 1 ] && [ "$QUEUE1"x = "queue"x ];then
    QUEUENAME=$(echo $1 |awk -F 'queue\\.' '{print $2}') 
    #默认上个月
    # DATES=($(date -d "$(date +%Y%m)01 -1 month" +%Y%m))
    #默认昨天
    DATES=($(date +"%Y%m%d" -d "-1day"))
    
elif [ $# -eq 2 ];then
  if [ "$1"x != "dates"x ];then
	   if echo $1|grep -Eq "[0-9]{8}" && date -d $1 +%Y%m%d > /dev/null 2>&1;then
          DATES=($1)
       else 
          echo "输入的日期格式不正确"
          exit 1 
       fi        
    fi
    if [ "$QUEUE2"x = "queue"x ];then
             QUEUENAME=$(echo $2 |awk -F 'queue\\.' '{print $2}')
        else PROVS=($2)
    fi
elif [ $# -eq 3 ];then
   if [ "$1"x != "dates"x ];then
	   if echo $1|grep -Eq "[0-9]{8}" && date -d $1 +%Y%m%d > /dev/null 2>&1;then
          DATES=($1)
		  PROVS=($2)
          QUEUENAME=$(echo $3 |awk -F 'queue\\.' '{print $2}')
       else 
          echo "输入的日期格式不正确"
          exit 1 
       fi        
    fi
else
    #默认上个月
    # DATES=($(date -d "$(date +%Y%m)01 -1 month" +%Y%m))
    #默认昨天
    DATES=($(date +"%Y%m%d" -d "-1day"))
fi
echo ${QUEUENAME}
PROVS=(${PROVS//,/ })
DATES=(${DATES//,/ })
ARREMAIL=(${ARREMAIL//,/ })
echo ${DATES[*]}
echo ${PROVS[*]}

#############HIVE参数区###########################################################################
#常用jar包 路径 /home/st001/soft/
#md5 输出结果md5大写
#add jar /home/st001/soft/BoncHiveUDF.jar;
#CREATE TEMPORARY FUNCTION MD5Encode AS 'com.bonc.hive.MyMD5';
#常规参数
COMMON_VAR=$COMMON_VAR
#合并小文件参数
MERGE_VAR="use ${USERNAME};
set mapreduce.job.queuename=${QUEUENAME};
set hive.exec.dynamic.partition.mode=nonstrict;
set hive.merge.mapfiles = true;
set hive.merge.mapredfiles= true;
set hive.merge.size.per.task=134217728;
set hive.merge.smallfiles.avgsize=150000000;
set mapred.max.split.size=134217728;
set mapred.min.split.size.per.node=100000000;
set mapred.min.split.size.per.rack = 100000000;
set mapred.output.compression.codec=com.hadoop.compression.lzo.LzopCodec; 
set hive.exec.compress.output = true;
set hive.hadoop.supports.splittable.combineinputformat=true;"

###############函数区################################################################################
###############时间配置函数##########################################################################
function CONFIGURE(){
    DAY_ID=$1
    MONTH_ID=${DAY_ID:0:6}
    PRE_MONTH_ID=$(date -d "${MONTH_ID}01  -1 month" +%Y%m)
    NEXT_MONTH_ID=$(date -d "${MONTH_ID}01  1 month" +%Y%m)
}
RunMr "spark2-submit --master yarn-client --class cn.ctyun.bigdata.bdse.tour.boot.CreateTable $JAR_DIR/bddwh-market-tour-1.0.jar cn.ctyun.bigdata.bddwh.market.tour.realtime.domain $recreatetable"

#flag  空，1
#空：第一次执行现在脚本
#1：运行过一次脚本

#####################################################################################################
#程序执行开始时间
current_date=`date "+%Y%m%d"`
start_dt=`date "+%Y-%m-%d %H:%M:%S"`
start_date=`date "+%Y%m%d%H%M%S"`
i=0
#执行时间段,使用下面注释的for循环
#for (( DAY_ID=20170501; DAY_ID<=20170531; DAY_ID=`date -d "${DAY_ID} +1 day" "+%Y%m%d"` ))
for DAY_ID in ${DATES[@]};
do
    CONFIGURE ${DAY_ID}
    for PROV_ID in ${PROVS[@]};
    do
    let i+=1
#################################FOR循环开始##########################################################
#v_acct_day=${DAY_ID}
##查询是否有分区
##停留表
#v_day=`hadoop fs -ls /daas/motl/dwi/msk/res/regn/dwi_res_regn_staypoint_msk_d/|awk -F '/' '{print $NF}'|grep -v 'Found'|awk -F '=' '{print $2}'|sort -nur|awk '{ if($1=='$v_acct_day'){print$1}}'|xargs|awk '{print $1}'`
#if [[ $v_day ==  $v_acct_day ]];then
#         echo "dwi_res_regn_staypoint_msk_d表存在"$v_day"分区"
#         v_rownum_temp=`hadoop fs -du /daas/motl/dwi/msk/res/regn/dwi_res_regn_staypoint_msk_d |grep day_id=$v_day|awk -F ' ' '{print $1}'`
#         v_rownum=`expr $v_rownum_temp `
#         if [[ $v_rownum -gt 10 ]];then
#         echo "dwi_res_regn_staypoint_msk_d存在分区且分区有数据，继续执行"
#         else
#            echo "dwi_res_regn_staypoint_msk_d存在表分区,但表分区没有数据,退出"
#            exit 1
#         fi
#else
#  echo "dwi_res_regn_staypoint_msk_d该表不存在"${v_acct_day}"分区,退出"
#  exit 1
#fi
###############################以下为SQL编辑区########################################################
echo `yarn application -list|grep RealTimeTourist`|while read line
do
 pid=`echo $line|cut -d " " -f 1`
 echo "yarn application -kill $pid"
 yarn application -kill $pid
done
#正常执行hql
files=$(ls $JAR_DIR)
jars=''
for filename in $files
do
 jars="${jars},$JAR_DIR/$filename"
done
jars=${jars#*,}
export SPARK_KAFKA_VERSION=0.10
#执行mr
command="spark2-submit
--files ${CONF_DIR}/jaas.conf,/home/dal_pro/dal_pro.keytab,${CONF_DIR}/log4j.xml,${CONF_DIR}/default.properties
--jars $jars
--class cn.ctyun.bigdata.bddwh.market.tour.realtime.RealTimeTouristApp
--queue $QUEUENAME
$SPARK_VAR
$JAR_DIR/bddwh-market-tour-1.0.jar"
echo $command
echo $command > ${BASE_DIR}/temp.sh
RunMr "sh ${BASE_DIR}/temp.sh"

#执行mr
#SQL="hadoop jar sort.jar /apps/hive/warehouse/st001.db/ztj_test3/ /apps/hive/warehouse/st001.db/ztj_test2/ mt1"
#RunMr "${SQL}"

#计算单表条数
#value=0
#SQL="
#select count(*) from odsbstl.d_area_code where crm_code='811';
#"
#value "${SQL}"
#可以根据返回值进行表的条数，空，波动性判断
#if [ ${value} -eq 0 ] ; then
#SendMessage "bss_d_mask${DAY_ID}账期,表记录数为0"
#fi

#取分区最大最小值例子
#par_max "dwi_integ.dwi_sev_user_normal_info_bss_d" "prov_id=811"
#echo par_max=$par_max
#par_min "dwi_integ.dwi_sev_user_normal_info_bss_d" "prov_id=811"
#echo par_min=$par_min

##################################FOR循环结束#########################################################
    echo "================================================================================"
    done
done
wait
######################################################################################################
##################################开始合并小文件#######################################################
#合并小文件方法
#Mergefile "${USERNAME}" "dm_ind_req_zhjt_4guser_m" "where month_id = '${MONTH_ID}'"

#程序执行结束时间
end_dt=`date "+%Y-%m-%d %H:%M:%S"`
time1=$(($(date +%s -d "$end_dt") - $(date +%s -d "$start_dt")))
if [ -f "${LOGPATH}/${LOGNAME}_error_${start_date}.log" ];then
    echo "执行失败"
    exit -1
#else
#v_rownum_temp1=`hadoop fs -du /tmp/daas/motl/dws/msk/wdtb/dws_wdtb_residentwork_msk_mid/live |grep day_id=$v_acct_day|awk -F ' ' '{print $1}'`
#v_rownum_temp2=`hadoop fs -du /tmp/daas/motl/dws/msk/wdtb/dws_wdtb_residentwork_msk_mid/work |grep day_id=$v_acct_day|awk -F ' ' '{print $1}'`
#
#  if [ ! $v_rownum_temp1 ];then
#       v_rownum_temp1=1
#   else v_rownum_temp1=$v_rownum_temp1
#fi
#  if [ ! $v_rownum_temp2 ];then
#       v_rownum_temp2=1
#   else v_rownum_temp2=$v_rownum_temp2
#fi
#v_rownum1=`expr $v_rownum_temp1 `
#v_rownum2=`expr $v_rownum_temp2 `
#         if [[ $v_rownum1 -gt 10 ]] && [[ $v_rownum2 -gt 10 ]];then
#              echo "执行成功"
#          else
#              echo "执行失败"
#           exit -1
#fi
else
 echo "执行成功"
fi
######################################################################################################