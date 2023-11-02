#!/bin/bash

cd `dirname $0`/../lib
target_dir=`pwd`

pid=`ps ax | grep -i 'agg.server' | grep ${target_dir} | grep java | grep -v grep | awk '{print $1}'`
if [ -z "$pid" ] ; then
        echo "No aggregate-framework-server running."
        exit -1;
fi

echo "The aggregate-framework-server(${pid}) is running..."

kill ${pid}

echo "Send shutdown request to aggregate-framework-server(${pid}) OK"
