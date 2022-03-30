#!/bin/bash
############# 加载环境
source /etc/profile
############# 伪码设置
CodeShellPath='%CodeShellPath%'
CodeLogExePath='%CodeLogExePath%'
CodeLogRunPath='%CodeLogRunPath%'
CodeSparkDir='%CodeSparkDir%'
CodeSparkSubmit='%CodeSparkSubmit%'
CodeSparkQueue='%CodeSparkQueue%'
CodeSparkAuthType='%CodeSparkAuthType%'
CodeSparkKeytab='%CodeSparkKeytab%'
CodeSparkPrincipal='%CodeSparkPrincipal%'
CodeSparkResource='%CodeSparkResource%'
CodeSparkConf='%CodeSparkConf%'
CodeSparkJars='%CodeSparkJars%'
CodeSparkFiles='%CodeSparkFiles%'
CodeSparkName='%CodeSparkName%'
CodeSparkJarMain='%CodeSparkJarMain%'
CodeSparkJar='%CodeSparkJar%'
CodeSparkParam='%CodeSparkParam%'
############# 执行命令
if [[ -n ${CodeSparkSubmit} ]];
 then
    master_parameter='--master '${CodeSparkSubmit}
 else
    master_parameter=' '
fi

if [[ -n ${CodeSparkQueue} ]];
 then
   queue_parameter='--queue '${CodeSparkQueue}
 else
   queue_parameter=' '
fi

if [[ -n ${CodeSparkKeytab} ]];
 then
   keytab_parameter='--keytab '${CodeSparkKeytab}
 else
   keytab_parameter=' '
fi

if [[ -n ${CodeSparkPrincipal} ]];
 then
   principal_parameter='--principal '${CodeSparkPrincipal}
 else
   principal_parameter=' '
fi

if [[ -n ${CodeSparkResource} ]];
 then
   resource_parameter=${CodeSparkResource}
 else
   resource_parameter=' '
fi

if [[ -n ${CodeSparkConf} ]];
 then
   conf_parameter=${CodeSparkConf}
 else
   conf_parameter=' '
fi

if [[ -d ${CodeSparkJars} ]];
 then
    jars=`ls ${CodeSparkJars}/*jar`
	output_Jars=''
    for jar in ${jars}
		do
		  output_Jars=${output_Jars},$jar
		done
   jars_parameter=${output_Jars}
   # 去掉最前面的逗号
   jars_parameter='--jars '${jars_parameter:1:${#jars_parameter}}
 else
   jars_parameter=' '
fi

if [[ -d ${CodeSparkFiles} ]];
 then
	files=`ls ${CodeSparkFiles}`
	output_Files=''
	for file in ${files}
	do
		output_Files=${output_Files},${CodeSparkFiles}'/'${file}
	done
	files_parameter=${output_Files}
	files_parameter='--files '${files_parameter:1:${#files_parameter}}
 else
   files_parameter=' '
fi

if [[ ${CodeSparkAuthType}='kerberos' ]] && [[ -d ${CodeSparkFiles} ]];
 then
	jaas_files=`ls ${CodeSparkFiles}`
	for jaas_file in ${jaas_files}
	do
	    if [[ ${jaas_file} =~ '1-kafka.jaas' ]];
	        then
	            jaas_driver=${CodeSparkFiles}'/'${jaas_file}
	    fi
	    if [[ ${jaas_file} =~ '2-kafka.jaas' ]];
	        then
	            jaas_executor='./'${jaas_file}
	    fi
	done
	jaas_parameter=' --conf spark.driver.extraJavaOptions=-Djava.security.auth.login.config='${jaas_driver}
	jaas_parameter=${jaas_parameter}' --conf spark.executor.extraJavaOptions=-Djava.security.auth.login.config='${jaas_executor}
 else
   jaas_parameter=' '
fi

if [[ -n ${CodeSparkName} ]];
 then
   name_parameter='--name '${CodeSparkName}
 else
   name_parameter=' '
fi

if [[ -n ${CodeSparkJarMain} ]];
 then
   class_parameter='--class '${CodeSparkJarMain}
 else
   class_parameter=' '
fi

if [[ -n ${CodeSparkJar} ]];
 then
   jar_parameter=${CodeSparkJar}
 else
   jar_parameter=' '
fi

if [[ -n ${CodeSparkParam} ]];
 then
   param_parameter=${CodeSparkParam}
 else
   param_parameter=' '
fi

echo ${master_parameter}
echo ${queue_parameter}
echo ${keytab_parameter}
echo ${principal_parameter}
echo ${resource_parameter}
echo ${conf_parameter}
echo ${jars_parameter}
echo ${files_parameter}
echo ${jaas_parameter}
echo ${name_parameter}
echo ${class_parameter}
echo ${jar_parameter}
echo ${param_parameter}
echo ${CodeLogExePath}

if [[ -n ${CodeSparkDir} ]];
 then
    if [[ -n ${CodeLogExePath} ]];
     then
        cd ${CodeSparkDir}
        ./bin/spark-submit \
        ${master_parameter} \
		${queue_parameter} \
        ${keytab_parameter} \
        ${principal_parameter} \
        ${resource_parameter} \
        ${conf_parameter} \
        ${jars_parameter} \
        ${files_parameter} \
		${jaas_parameter} \
        ${name_parameter} \
        ${class_parameter} \
        ${jar_parameter} \
        ${param_parameter}  >> ${CodeLogExePath} 2>&1 &
    fi
 else
    exit;
fi