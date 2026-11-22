@echo off
set "TOOL=%~dp0..\..\.github\scripts\mod_release.py"
if /I "%~1"=="-Modrinth" goto modrinth
python "%TOOL%" links %*
goto :eof
:modrinth
python "%TOOL%" links %2 %3 %4 %5
if errorlevel 1 goto :eof
python "%TOOL%" modrinth-links %2 %3 %4 %5
