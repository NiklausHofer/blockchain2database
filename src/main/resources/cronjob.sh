#!/bin/bash

export MVN_NR=$(ps -ef | grep -i "blockchain2database" | wc -l)
export DT=$(date +%Y%m%dT%H%M)
export LOGFILE=log_${DT}

if [ "${MVN_NUR}" -ge "2" ]
then
  echo "Another instance of Maven is already running. Abort" >> ${LOGFILE}
  exit 2
fi

java -jar blockchain2database-0.98.jar >> ${LOGFILE} 2> errorlog

