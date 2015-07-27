@echo off

REM ===================================================================
REM Run the "SqlTool" utility
REM ===================================================================
set base=C:\Users\wjohnson000\git\sql-tool
set jars=%base%\jar


REM ===================================================================
REM Java options for memory settings, anti-aliasing
REM ===================================================================
REM set jopts=-Xms192m -Xmx384m -Xss24m -Dswing.aatext=true -Dderby.system.home="C:/temp/meme/derby"
set jopts=-Xms384m -Xmx768m -Xss96m -Dfile.encoding=UTF-8 -Dsun.jnu.encoding=UTF-8 -Dswing.aatext=true

REM ===================================================================
REM Add all of the JAR files to the class path and start the app
REM ===================================================================
set cp=.
for %%j in (%jars%\*.jar) do call :AddToPath %%j
REM set cp=%cp%;%base%\lib\sqltool.jar
set cp=%cp%;%base%\target\sqltool-1.0.0.jar

java -classpath "%cp%" %jopts% sqltool.SqlToolMain
goto :EOF


REM ===================================================================
REM Simple process to add a JAR file to the class path
REM ===================================================================
:AddToPath
set cp=%cp%;%1
goto :EOF

:EOF
