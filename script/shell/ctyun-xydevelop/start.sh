PID=`ps aux | grep mtcs-realtime-server | grep java | awk '{print $2}'`
#ip_addr=`ip addr|grep inet|grep -v inet6|grep -v '127.0.0.1'|grep brd|awk '{print $2}'|awk -F '/' '{print $1}'`
if [ -n "$PID" ]; then
    echo "Will shutdown mtcsRealtime: $PID"
    kill -9 $PID
    sleep 2
else echo "No mtcsRealtime Process $PID"
fi
eval $(grep REGISTRY_SERVICE_URL /data01/mtcs/service/common.cfg)
eval $(grep IP_ADDRESS /data01/mtcs/service/common.cfg)
nohup java -jar ../mtcs-realtime-server-*.jar  --REGISTRY_SERVICE_URL="${REGISTRY_SERVICE_URL}" --IP_ADDRESS="${IP_ADDRESS}"  --LOG_DIR_PATH='../../logs' >nohup.out 2>&1 &sleep 2
