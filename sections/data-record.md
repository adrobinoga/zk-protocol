# Record Data Operations #

[Go to Main Page](../protocol.md)

Author: Alexander Marin <alexanderm2230@gmail.com>

[TOC]

## Current Descriptions ##

Here is a list of SDK functions, from **Data-Record.h** file, that shows which functions can be replicated with the current spec:

|SDK function name		|Described(X=Yes, O=No)	|Notes|
|---				|:---:			|---|
|SSR_SetDeviceData		|**O**			|This is only for newer firmware.|
|SSR_GetDeviceData		|**O**			|This is only for newer firmware.|
|ReadGeneralLogData		|**O**			|Todo.|
|ReadAllGLogData		|**O**			|Only operates on memory.|
|GetGeneralLogData		|**O**			|Applicable only to BW.|
|SSR_GetGeneralLogData		|**O**			|Only operates on memory.|
|GetAllGLogData			|**O**			|Applicable only to BW.|
|GetGeneralLogDataStr		|**O**			|Applicable only to BW.|
|GetGeneralExtLogData		|**O**			|Applicable only to BW.|
|ClearGLog			|**O**			|Todo.|
|ReadSuperLogData		|**O**			|Only operates on memory.|
|ReadAllSLogData		|**O**			|Only operates on memory.|
|GetSuperLogData		|**O**			|Only operates on memory.|
|GetAllSLogData			|**O**			|Only operates on memory.|
|ClearSLog			|**O**			|Todo.|
|GetSuperLogData2		|**O**			|Only operates on memory.|
|ClearKeeperData		|**O**			|Todo.|
|ClearData			|**O**			| |
|GetDataFile			|**O**			| |
|SendFile			|**O**			|Todo.|
|ReadFile			|**O**			|Applicable only to BW.|
|RefreshData			|**O**			|Todo.|
|ReadTimeGLogData		|**O**			|This is only for newer firmware.|
|ReadNewGLogData		|**O**			|This is only for newer firmware.|
|DeleteAttlogBetweenTheDate	|**O**			|This is only for newer firmware.|
|DeleteAttlogByTime		|**O**			|This is only for newer firmware.|
