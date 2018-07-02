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
|ReadGeneralLogData		|**X**			| |
|ReadAllGLogData		|**O**			|Same as ReadGeneralLogData.|
|GetGeneralLogData		|**O**			|Applicable only to BW.|
|SSR_GetGeneralLogData		|**O**			|Only operates on memory.|
|GetAllGLogData			|**O**			|Applicable only to BW.|
|GetGeneralLogDataStr		|**O**			|Applicable only to BW.|
|GetGeneralExtLogData		|**O**			|Applicable only to BW.|
|ClearGLog			|**X**			| |
|ReadSuperLogData		|**X**			| |
|ReadAllSLogData		|**O**			|Same as ReadSuperLogData.|
|GetSuperLogData		|**O**			|Only operates on memory.|
|GetAllSLogData			|**O**			|Only operates on memory.|
|ClearSLog			|**X**			| |
|GetSuperLogData2		|**O**			|Only operates on memory.|
|ClearKeeperData		|**O**			|Todo.|
|ClearData			|**O**			| |
|GetDataFile			|**O**			| |
|SendFile			|**O**			|Todo.|
|ReadFile			|**O**			|Applicable only to BW.|
|RefreshData			|**X**			| |
|ReadTimeGLogData		|**O**			|This is only for newer firmware.|
|ReadNewGLogData		|**O**			|This is only for newer firmware.|
|DeleteAttlogBetweenTheDate	|**O**			|This is only for newer firmware.|
|DeleteAttlogByTime		|**O**			|This is only for newer firmware.|

## Read Attendance Records ##

To read the attendance records, first disable the device:

	> packet(id=CMD_DISABLEDEVICE)
		> packet(id=CMD_ACK_OK)

Then send a command with the id `CMD_DATA_WRRQ` and with a fixed payload of 11 bytes, field description for this payload it is still unknown.

	packet(id=CMD_DATA_WRRQ, data=010d000000000000000000)

Depending of the size of the att rec structure, the device may send this info in two ways:

1.For "small" structures, the machine would send the info structure immediately

	> packet(id=CMD_DATA_WRRQ, data=010d000000000000000000)
		> packet(id=CMD_DATA, data=<att rec>)

2.For bigger structures see the [Exchange of Data](ex_data.md) spec.

The fields of the `att rec` structure are given in the following table:

|Name		|Description				|Value[hex]	|Size[bytes]	|Offset		|
|---		|---					|---		|---		|---		|
|att_size	|Total size of user attendance entries.	|N*40 (<)	|2		|0		|
|zeros		|Null bytes.				|00 00		|2		|2		|
|att1 entry	|Attendance log 1.			|varies		|40		|4		|
|att2 entry	|Attendance log 2.			|varies		|40		|44		|
|...		|...					|varies		|40		|...		|
|attN entry	|Attendance log N.			|varies		|40		|att_size-40+4	|

(<): Little endian format.

Each attendance entry has the following fields:

|Name			|Description					|Value[hex]					|Size[bytes]	|Offset	|
|---			|---						|---						|---		|---	|
|user sn		|Internal serial number for the user.		|varies (<)					|2		|0	|
|user id		|User ID, stored as a string.			|varies						|9		|2	|
|			|Fixed.						|00 00 00 00 00 00 00 00 00 00 00 00 00 00 00	|15		|11	|
|verify type		|Verification type.				|varies						|1		|26	|
|record time		|Time of the attendance event.			|varies (<)					|4		|27	|
|verify state		|Verification state.				|varies						|1		|31	|
|			|Fixed.						|00 00 00 00 FF 00 00 00			|8		|32	|

(<): Little endian format.

Where the time is encoded in the same format used in set/get device time procedure, see [terminal.md](./terminal.md).

Finally send the enable device command to put the device in normal operation:

	> packet(id=CMD_ENABLEDEVICE)
		> packet(id=CMD_ACK_OK)

## Clear All Attendance Records ##

To clear the attendance records, first disable the device, then send a CMD_CLEAR_ATTLOG command, refresh the data and finally enable the device.

	> packet(id=CMD_DISABLEDEVICE)
		> packet(id=CMD_ACK_OK)
	> packet(id=CMD_CLEAR_ATTLOG)
		> packet(id=CMD_ACK_OK)
	> packet(id=CMD_REFRESHDATA)
		> packet(id=CMD_ACK_OK)
	> packet(id=CMD_ENABLEDEVICE)
		> packet(id=CMD_ACK_OK)

Note that this will delete all the attendance records, time interval delete operations are only supported on newer firmware versions.

## Read Operation Records ##

To read the operation records, first disable the device:

	> packet(id=CMD_DISABLEDEVICE)
		> packet(id=CMD_ACK_OK)

Then send a command with the id `CMD_DATA_WRRQ` and with a fixed payload of 11 bytes, field description for this payload it is still unknown.

	packet(id=CMD_DATA_WRRQ, data=0122000000000000000000)

Depending of the size of the att rec structure, the device may send this info in two ways:

1.For "small" structures, the machine would send the info structure immediately

	> packet(id=CMD_DATA_WRRQ, data=0122000000000000000000)
		> packet(id=CMD_DATA, data=<ops rec>)

2.For bigger structures see the [Exchange of Data](ex_data.md) spec.

The fields of the `ops rec` structure are given in the following table:

|Name		|Description				|Value[hex]	|Size[bytes]	|Offset		|
|---		|---					|---		|---		|---		|
|ops_size	|Total size of operation record entries.|N*16 (<)	|2		|0		|
|zeros		|Null bytes.				|00 00		|2		|2		|
|oplog1 entry	|Operation log 1.			|varies		|16		|4		|
|oplog2 entry	|Operation log 2.			|varies		|16		|20		|
|...		|...					|varies		|16		|...		|
|oplogN entry	|Operation log N.			|varies		|16		|ops_size-16+4	|

(<): Little endian format.

Each operation entry has the following fields:

|Name		|Description		|Value[hex]	|Size[bytes]	|Offset	|
|---		|---			|---		|---		|---	|
|		|Fixed.			|0000		|2		|0	|
|operation id	|Operation ID.		|varies		|1		|2	|
|unknown	|			|varies		|1		|3	|
|record time	|Record log time.	|varies (<)	|4		|4	|
|param1		|Parameter 1.		|varies (<)	|2		|8	|
|param2		|Parameter 1.		|varies (<)	|2		|10	|
|param3		|Parameter 1.		|varies (<)	|2		|12	|
|param4		|Parameter 1.		|varies (<)	|2		|14	|

(<): Little endian format.

Where the time is encoded in the same format used in set/get device time procedure, see [terminal.md](./terminal.md).

Finally send the enable device command to put the device in normal operation:

	> packet(id=CMD_ENABLEDEVICE)
		> packet(id=CMD_ACK_OK)

## Clear All Operation Records ##

To clear the operation records, first disable the device, then send a CMD_CLEAR_OPLOG command, refresh the data and finally enable the device.

	> packet(id=CMD_DISABLEDEVICE)
		> packet(id=CMD_ACK_OK)
	> packet(id=CMD_CLEAR_OPLOG)
		> packet(id=CMD_ACK_OK)
	> packet(id=CMD_REFRESHDATA)
		> packet(id=CMD_ACK_OK)
	> packet(id=CMD_ENABLEDEVICE)
		> packet(id=CMD_ACK_OK)

## Refresh Data ##

After uploading a fingerprint or changing the users data, send the refresh data command, so the changes take effect.

	> packet(id=CMD_REFRESHDATA)
		> packet(id=CMD_ACK_OK)

