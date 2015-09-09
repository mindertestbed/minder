@echo off

rem full path to scala compiler
set SCALA_COMPILER="C:\Program Files (x86)\scala\bin\scalac.bat"

rem in case of wrong version warnings put in the full path to java
set JAVA=java

set JVM_OPTS=-Dplay.evolutions.db.default.autoApply=true -Dplay.evolutions.db.default.autoApplyDowns=true

set HTTP_PORT=9001
set HTTPS_PORT=9000
set APPLICATION_SECRET="sdf56asdfalsdkn80hedehodobeaasdlfjasdksdkjfhsdlkjhgksjdfkdfg"

echo JAVA VERSION
%JAVA% -version

echo off
FOR %%X IN (lib/*.jar) DO call :body %%X
goto :there

:body
set CP=%CP%lib/%~n1.jar;
goto :eof

:there

echo on
%JAVA% -cp "%CP%" %JVM_OPTS% -DSCALA_COMPILER=%SCALA_COMPILER% -Dapplication.secret=%APPLICATION_SECRET% -Dhttps.port=%HTTPS_PORT% -Dhttp.port=%HTTP_PORT% play.core.server.ProdServerStart

pause