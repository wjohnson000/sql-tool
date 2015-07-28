#!/bin/bash

# --------------------------------------------------------------------------
# Start the "sqltool" utility
# --------------------------------------------------------------------------
JAVA_OPTS="-Xms384m -Xmx768m -Xss96m -Dfile.encoding=UTF-8 -Dsun.jnu.encoding=UTF-8 -Dswing.aatext=true"
JAR_LIB=jar

CP=target/sqltool-1.0.0.jar
for jarfile in `ls jar`
do
   XX=`cygpath -u jar/${jarfile}`
   CP="${CP};${XX}"
done

"${JAVA_HOME}/bin/java" -classpath ${CP} ${JAVA_OPTS} sqltool.SqlToolMain
