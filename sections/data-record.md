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
|ReadAllGLogData		|**X**			|Same as ReadGeneralLogData.|
|GetGeneralLogData		|**O**			|Applicable only to BW.|
|SSR_GetGeneralLogData		|**O**			|Only operates on memory.|
|GetAllGLogData			|**O**			|Applicable only to BW.|
|GetGeneralLogDataStr		|**O**			|Applicable only to BW.|
|GetGeneralExtLogData		|**O**			|Applicable only to BW.|
|ClearGLog			|**X**			| |
|ReadSuperLogData		|**X**			| |
|ReadAllSLogData		|**X**			|Same as ReadSuperLogData.|
|GetSuperLogData		|**O**			|Only operates on memory.|
|GetAllSLogData			|**O**			|Only operates on memory.|
|ClearSLog			|**X**			| |
|GetSuperLogData2		|**O**			|Only operates on memory.|
|ClearKeeperData		|**X**			| |
|ClearData			|**X**			| |
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

Depending of the size of the `att rec` structure, the device may send this info in two ways:

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

The verification type codification is:

|Verification type	|Value	|
|---			|---	|
|Password		|0	|
|Fingerprint		|1	|
|RF card		|2	|

The verification state codification is:

|Verification state	|Value	|
|---			|---	|
|Check in (default)	|0	|
|Check out		|1	|
|Break out		|2	|
|Break in		|3	|
|OT in			|4	|
|OT out			|5	|

Finally send the enable device command to put the device in normal operation:

	> packet(id=CMD_ENABLEDEVICE)
		> packet(id=CMD_ACK_OK)

### Example of an Attendance Entry ###

This is an example of an attendance entry, the user index is `0x000D` and his id number is "999111333", the verification type is 1, and the encoded time is given by `0x2368B36B`.

	00000000: 0D 00 39 39 39 31 31 31  33 33 33 00 00 00 00 00  ..999111333.....
	00000010: 00 00 00 00 00 00 00 00  00 00 01 6B B3 68 23 00  ...........k.h#.
	00000020: 00 00 00 00 FF 00 00 00                           ........

## Clear All Attendance Records ##

To clear the attendance records, first disable the device, then send a `CMD_CLEAR_ATTLOG` command, refresh the data and finally enable the device.

	> packet(id=CMD_DISABLEDEVICE)
		> packet(id=CMD_ACK_OK)
	> packet(id=CMD_CLEAR_ATTLOG)
		> packet(id=CMD_ACK_OK)
	> packet(id=CMD_REFRESHDATA)
		> packet(id=CMD_ACK_OK)
	> packet(id=CMD_ENABLEDEVICE)
		> packet(id=CMD_ACK_OK)

Note that this will delete all the attendance records, time-interval delete operations are only supported on newer firmware versions.

## Read Operation Records ##

To read the operation records, first disable the device:

	> packet(id=CMD_DISABLEDEVICE)
		> packet(id=CMD_ACK_OK)

Then send a command with the id `CMD_DATA_WRRQ` and with a fixed payload of 11 bytes, field description for this payload it is still unknown.

	packet(id=CMD_DATA_WRRQ, data=0122000000000000000000)

Depending of the size of the `ops rec` structure, the device may send this info in two ways:

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
|param2		|Parameter 2.		|varies (<)	|2		|10	|
|param3		|Parameter 3.		|varies (<)	|2		|12	|
|param4		|Parameter 4.		|varies (<)	|2		|14	|

(<): Little endian format.

Where the time is encoded in the same format used in set/get device time procedure, see [terminal.md](./terminal.md).

Finally send the enable device command to put the device in normal operation:

	> packet(id=CMD_ENABLEDEVICE)
		> packet(id=CMD_ACK_OK)

**Note**: Codification of operation IDs and corresponding param interpretation, remains as a ToDo.

### Example Operation Record Entry ###

Here the operation id is 6, the encoded time is `0x23689BC2`, param1 is `0x0003`, param2 is `0x0000`, param3 is `0x0007` and param4 is `0x0502`.

	00000000: 00 00 06 2A C2 9B 68 23  03 00 00 00 07 00 02 05  ...*..h#........

## Clear All Operation Records ##

To clear the operation records, first disable the device, then send a `CMD_CLEAR_OPLOG` command, refresh the data and finally enable the device.

	> packet(id=CMD_DISABLEDEVICE)
		> packet(id=CMD_ACK_OK)
	> packet(id=CMD_CLEAR_OPLOG)
		> packet(id=CMD_ACK_OK)
	> packet(id=CMD_REFRESHDATA)
		> packet(id=CMD_ACK_OK)
	> packet(id=CMD_ENABLEDEVICE)
		> packet(id=CMD_ACK_OK)

## Clear Data ##

To clear data on the device use the command `CMD_CLEAR_DATA`.

	> packet(id=CMD_DISABLEDEVICE)
		> packet(id=CMD_ACK_OK)
	> packet(id=CMD_CLEAR_DATA, data=<data type>)
		> packet(id=CMD_ACK_OK)
	> packet(id=CMD_REFRESHDATA)
		> packet(id=CMD_ACK_OK)

The `data type` is just one byte with the type of data to delete:

|Data			|Value	|
|---			|---	|
|Attendance records	|1	|
|Fingerprint templates	|2	|
|None			|3	|
|Operation records	|4	|
|User information	|5	|

If the data type is ommited the device will delete all the info.

## Refresh Data ##

After uploading a fingerprint or changing the users data, send the refresh data command, so the changes take effect.

	> packet(id=CMD_REFRESHDATA)
		> packet(id=CMD_ACK_OK)

