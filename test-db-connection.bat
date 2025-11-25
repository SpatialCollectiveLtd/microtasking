@echo off
REM Quick MySQL Connection Test for Windows

echo ========================================
echo Microtasking Database Connection Test
echo ========================================
echo.

set DB_HOST=169.255.58.54
set DB_PORT=3306
set DB_NAME=spatialcoke_dpw_microtasking_prod
set DB_USER=spatialcoke_dpw_prod_user
set DB_PASS=NtDcdgPoadgxT5

echo Testing connection to:
echo   Host: %DB_HOST%
echo   Database: %DB_NAME%
echo.

REM Check if MySQL client is installed
where mysql >nul 2>&1
if %ERRORLEVEL% NEQ 0 (
    echo [ERROR] MySQL client not found in PATH
    echo Please install MySQL client from: https://dev.mysql.com/downloads/mysql/
    echo.
    pause
    exit /b 1
)

echo MySQL client found!
echo.

REM Test connection
echo Testing database connection...
mysql -h %DB_HOST% -P %DB_PORT% -u %DB_USER% -p%DB_PASS% %DB_NAME% -e "SHOW TABLES;"

if %ERRORLEVEL% EQU 0 (
    echo.
    echo [SUCCESS] Connected to database successfully!
    echo.
    echo Do you want to run the database setup script? (y/n)
    set /p RUN_SETUP=
    
    if /i "%RUN_SETUP%"=="y" (
        echo Running database-setup.sql...
        mysql -h %DB_HOST% -P %DB_PORT% -u %DB_USER% -p%DB_PASS% %DB_NAME% < database-setup.sql
        
        if %ERRORLEVEL% EQU 0 (
            echo [SUCCESS] Database tables created successfully!
        ) else (
            echo [ERROR] Failed to create database tables
        )
    )
) else (
    echo.
    echo [ERROR] Failed to connect to database
    echo.
    echo Troubleshooting:
    echo   1. Check if MySQL server is running
    echo   2. Verify firewall allows port 3306
    echo   3. Confirm database credentials are correct
    echo   4. Try using domain name instead of IP
)

echo.
pause
