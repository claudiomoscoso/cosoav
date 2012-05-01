cls
@echo off
for %%i in (sp-*.sql.txt) do mysql -u root -t -padmin < %%i

mysql -u root -t -padmin < testSP.sql.txt
