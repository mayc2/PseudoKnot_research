:: The DATAPATH variable in this file must be set to the directory in which
:: thermodynamic files are found.
:: This file is meant to be used with Windows machines only.
@ECHO OFF
CD %~dp0\..
SET RNA_PROG=%CD%
SET DATAPATH=%RNA_PROG%\data_tables
SET PATH=%PATH%;%RNA_PROG%\exe
:: echo RNA_PROG=%RNA_PROG%
cd "%USERPROFILE%"
IF EXIST "%USERPROFILE%\Documents" (
	CD "%USERPROFILE%\Documents" 
) ELSE ( 
	IF EXIST "%USERPROFILE%\My Documents" CD "%USERPROFILE%\My Documents" )
)
IF EXIST .\RNAstructure CD .\RNAstructure

TYPE "%RNA_PROG%\Version.txt"