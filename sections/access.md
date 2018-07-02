# Access Operations #

[Go to Main Page](../protocol.md)

Author: Alexander Marin <alexanderm2230@gmail.com>

[TOC]

## Current Descriptions ##

Here is a list of SDK functions, from **Access.h** file, that shows which functions can be replicated with the current spec:

|SDK function name	|Described(X=Yes, O=No)	|Notes|
|---			|:---:			|---|
|GetUserGroup		|**O**			|Todo.|
|SetUserGroup		|**O**			|Todo.|
|GetTZInfo		|**X**			| |
|SetTZInfo		|**X**			| |
|GetUnlockGroups	|**O**			|Only applicable to BW devices.|
|SetUnlockGroups	|**O**			|Only applicable to BW devices.|
|SSR_SetUnLockGroup	|**O**			|Todo.|
|SSR_GetUnlockGroup	|**O**			|Todo.|
|GetGroupTZs		|**O**			|Only applicable to BW devices.|
|SetGroupTZs		|**O**			|Only applicable to BW devices.|
|SSR_GetGroupTZ		|**O**			|Todo.|
|SSR_SetGroupTZ		|**O**			|Todo.|
|GetGroupTZStr		|**O**			|Only applicable to BW devices.|
|SetGroupTZStr		|**O**			|Only applicable to BW devices.|
|GetUserTZs		|**O**			|Todo.|
|SetUserTZs		|**O**			|Todo.|
|GetUserTZStr		|**O**			|Todo.|
|SetUserTZStr		|**O**			|Todo.|
|ACUnlock		|**X**			|Todo.|
|GetACFun		|**O**			|Todo.|
|GetDoorState		|**X**			|Todo.|
|UseGroupTimeZone	|**O**			|Todo.|
|GetHoliday		|**O**			|Only applicable to BW devices.|
|SetHoliday		|**O**			|Only applicable to BW devices.|
|SSR_GetHoliday		|**O**			|Todo.|
|SSR_SetHoliday		|**O**			|Todo.|
|SetDaylight		|**O**			|Todo.|
|GetDaylight		|**O**			|Todo.|

## Get TZ Info ##

To request definition of a timezone send the command CMD_TZ_RRQ:

	> packet(id=CMD_TZ_RRQ, data=<timezone index>)
		> packet(id=CMD_ACK_OK, data=<timezone info>)

Where the timezone index stores the number of the timezone to read, it is stored as a number of 4 bytes and in little endian format.

The result, timezone info, stores the definition of the timezone:

|Name		|Description					|Value[hex]	|Size[bytes]	|Offset		|
|---		|---						|---		|---		|---		|
|timezone index	|Stores the number of the requested timezone.	|varies (<)	|2		|0		|
|sunday tz	|Sunday timezone.				|varies		|4		|2		|
|monday tz	|Monday timezone.				|varies		|4		|6		|
|tuesday tz	|Tuesday timezone.				|varies		|4		|10		|
|wednesday tz	|Wednesday timezone.				|varies		|4		|14		|
|thursday tz	|Thursday timezone.				|varies		|4		|18		|
|friday tz	|Friday timezone.				|varies		|4		|22		|
|saturday tz	|Saturday timezone.				|varies		|4		|26		|
|		|Fixed						|a71c		|2		|30		|

Each timezone is stored in a special format.

If the sequence is:

	B0 B1 B2 B3

The timezone:

	Start hour:Start minute TO End hour:End minute

Will be given by:

	B0:B1 TO B2:B3

The numbers are given directly as numbers so there is no need for ascii conversion.

## Set TZ Info ##

To change a timezone definition send a CMD_TZ_WRQ, with the new timezone definition:

	> packet(id=CMD_TZ_WRQ, data=<new timezone info>)
		> packet(id=CMD_ACK_OK)
	> packet(id=CMD_REFRESHDATA)
		> packet(id=CMD_ACK_OK)

The new timezone info structure is very similar to the structure given for a get operation, check the following table:

|Name		|Description					|Value[hex]	|Size[bytes]	|Offset		|
|---		|---						|---		|---		|---		|
|timezone index	|Stores the number of the requested timezone.	|varies (<)	|4		|0		|
|sunday tz	|Sunday timezone.				|varies		|4		|4		|
|monday tz	|Monday timezone.				|varies		|4		|8		|
|tuesday tz	|Tuesday timezone.				|varies		|4		|12		|
|wednesday tz	|Wednesday timezone.				|varies		|4		|16		|
|thursday tz	|Thursday timezone.				|varies		|4		|20		|
|friday tz	|Friday timezone.				|varies		|4		|24		|
|saturday tz	|Saturday timezone.				|varies		|4		|28		|

## Door Unlock ##

Send the command CMD_UNLOCK to open the door, the doors remains open for the specified delay(seconds).

	> packet(id=CMD_UNLOCK, data=<delay>)
		> packet(id=CMD_ACK_OK)

Where the delay is given as a 4 byte number stored in little endian format.

## Get Door State ##

To request the door state send the command CMD_DOORSTATE_RRQ


	> packet(id=CMD_DOORSTATE_RRQ)
		> packet(id=CMD_ACK_OK, data=<door flag>)

Where the door flag is just one byte with the value 1 if the door is open, otherwise if the door is closed, the value is 0.

[Go to Main Page](../protocol.md)
