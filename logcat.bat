@echo off
adb shell am start -n io.nava.qobuz_test/android.app.NativeActivity
timeout /t 1 /nobreak >nul
for /f %%i in ('adb shell pidof io.nava.qobuz_test') do set PID=%%i
echo Watching PID: %PID%
adb logcat --pid=%PID% *:E qobuz_test:I
