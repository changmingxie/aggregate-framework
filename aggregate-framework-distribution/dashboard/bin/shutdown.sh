#!/bin/bash

cd `dirname $0`/../lib
target_dir=`pwd`

pid=`ps ax | grep -i 'agg.dashboard' | grep ${target_dir} | grep java | grep -v grep | awk '{print $1}'`
if [ -z "$pid" ] ; then
        echo "No aggregate-framework-dashboard running."
        exit -1;
fi

echo "The aggregate-framework-dashboard(${pid}) is running..."

kill ${pid}

echo "Send shutdown request to aggregate-framework-dashboard(${pid}) OK"
