:: GOTO EndComment
:: The DATAPATH variable in this file must be set to the directory in which
:: thermodynamic files are found.
:: This file is meant to be used with Windows machines only.
cd %~dp0
SET DATAPATH=%~dp0\..\data_tables
java -jar RNAstructure.jar
