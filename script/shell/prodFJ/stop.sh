PID=`ps aux | grep mtcs-realtime-server-1.0.jar | grep java | awk '{print $2}'`

if [ -n "$PID" ]; then
    echo "Will shutdown mtcsRealtimeServer: $PID"
    kill -9 $PID
else echo "No mtcsRealtimeServer Process $PID"
fi
