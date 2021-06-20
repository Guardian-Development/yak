#!/bin/bash

#
# This script takes a jfr recording, and a heap dump, and stores them inside the container at /opt/yak/metrics/output
#

# get the java pid using jcmd to run further commands on
jcmd_pid=$(docker exec docker_yak-server_1 /jre/bin/jcmd | grep app.jar | cut -d ' ' -f 1);

# trigger heap dump and jfr to metrics output location under the date and time
output_pretext=$(date +%F-%T)
docker exec docker_yak-server_1 /jre/bin/jcmd "${jcmd_pid}" GC.heap_dump /opt/yak/metrics/output/"${output_pretext}"-yak-server-heap-dump.bin;
docker exec docker_yak-server_1 /jre/bin/jcmd "${jcmd_pid}" JFR.dump filename=/opt/yak/metrics/output/"${output_pretext}"-yak_jfr_recording.jfr;