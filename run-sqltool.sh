#!/bin/bash

# --------------------------------------------------------------------------
# Start the "sqltool" utility
# --------------------------------------------------------------------------
JAVA_OPTS="-Xms192m -Xmx384m -Xss24m -Dswing.aatext=true"
JAR_LIB=./jar

CP="./lib/sqltool.jar"
for file in `ls $JAR_LIB`
do
   CP=${CP}:${JAR_LIB}/${file}
done

echo `pwd`
echo ${JAVA_HOME}/bin/java -classpath ${CP} ${JAVA_OPTS} sqltool.SqlToolMain

"${JAVA_HOME}/bin/java" -classpath "${CP}" ${JAVA_OPTS} sqltool.SqlToolMain
