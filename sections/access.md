# Access Operations #

[Go to Main Page](../protocol.md)

Author: Alexander Marin <alexanderm2230@gmail.com>

[TOC]

## Current Descriptions ##

Here is a list of SDK functions, from **Access.h** file, that shows which functions can be replicated with the current spec:

|SDK function name	|Described(X=Yes, O=No)	|Notes|
|---			|:---:			|---|
|GetUserGroup		|**X**			| |
|SetUserGroup		|**X**			| |
|GetTZInfo		|**X**			| |
|SetTZInfo		|**X**			| |
|GetUnlockGroups	|**O**			|Only applicable to BW devices.|
|SetUnlockGroups	|**O**			|Only applicable to BW devices.|
|SSR_SetUnLockGroup	|**X**			| |
|SSR_GetUnlockGroup	|**X**			| |
|GetGroupTZs		|**O**			|Only applicable to BW devices.|
|SetGroupTZs		|**O**			|Only applicable to BW devices.|
|SSR_GetGroupTZ		|**X**			| |
|SSR_SetGroupTZ		|**X**			| |
|GetGroupTZStr		|**O**			|Only applicable to BW devices.|
|SetGroupTZStr		|**O**			|Only applicable to BW devices.|
|GetUserTZs		|**X**			| |
|SetUserTZs		|**X**			| |
|GetUserTZStr		|**X**			| |
|SetUserTZStr		|**X**			| |
|ACUnlock		|**X**			| |
|GetACFun		|**O**			|Todo.|
|GetDoorState		|**X**			| |
|UseGroupTimeZone	|**X**			|See set/get user timezone|
|GetHoliday		|**O**			|Only applicable to BW devices.|
|SetHoliday		|**O**			|Only applicable to BW devices.|
|SSR_GetHoliday		|**O**			|Todo.|
|SSR_SetHoliday		|**O**			|Todo.|
|SetDaylight		|**O**			|Todo.|
|GetDaylight		|**O**			|Todo.|

## Get User Group ##

New users are by default in group 1, but a user may belong to 1 of 100 possible groups, to get the group a user belongs to, send the following command:

	> packet(id=CMD_USERGRP_RRQ, <user sn>)
		> packet(id=CMD_ACK_OK, <group number>)

Where user sn is a field 4 bytes long, where the first byte stores the user's internal index, as a number.

The reply data field is 1 byte long, with the number of the group to which the user belongs.

## Set User Group ##

To set a user in a group, send the following command:

	> packet(id=CMD_USERGRP_WRQ, <user sn + group>)
		> packet(id=CMD_ACK_OK)

Where the command data field has the following fields:

|Name		|Description				|Value[hex]	|Size[bytes]	|Offset		|
|---		|---					|---		|---		|---		|
|user sn	|User's internal index on machine.	|varies		|1		|0		|
|		|Fixed.					|zeros		|3		|1		|
|group number	|Group number, given as a number.	|varies		|1		|4		|


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

(<): Little endian format.

Each timezone is stored in a special format.

If the sequence is:

	B0 B1 B2 B3

The timezone:

	(Start hour):(Start minute) TO (End hour):(End minute)

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

(<): Little endian format.

## Get Unlock Combination ##

Group verification may be used to require that users from different groups verify at same to garant access, by default users use this type of verification, but usually using the unlock combination 01 00 00 00 00, which means that only a user of the group 1 (themselves) is needed to garant access.

There may be 10 unlock combinations, to request an unlock combination send the command CMD_ULG_RRQ with the combination number to request.

	> packet(id=CMD_ULG_RRQ, <comb req>)
		> packet(id=CMD_ACK_OK, <comb grps>)

The structure <comb req> has the following fields

|Name		|Description		|Value[hex]	|Size[bytes]	|Offset		|
|---		|---			|---		|---		|---		|
|comb no	|Combination number.	|varies		|1		|0		|
|		|Fixed.			|zeros		|7		|1		|

And the response has the following fields:

|Name		|Description		|Value[hex]	|Size[bytes]	|Offset		|
|---		|---			|---		|---		|---		|
|comb no	|Combination number.	|varies		|1		|0		|
|group1		|Group 1 of combination.|varies		|1		|1		|
|group2		|Group 2 of combination.|varies		|1		|2		|
|group3		|Group 3 of combination.|varies		|1		|3		|
|group4		|Group 4 of combination.|varies		|1		|4		|
|group5		|Group 5 of combination.|varies		|1		|5		|
|valid groups	|Number of valid groups.|varies (<)	|2		|6		|

(<): Little endian format.

The valid groups field indicates how many groups are included in the current combianation, if that number is less than 5, then the unused values are set to 0.

## Set Unlock Combination ##

To set an unlock combination send the command CMD_ULG_WRQ.

	> packet(id=CMD_ULG_WRQ, <comb grps>)
		> packet(id=CMD_ACK_OK)
	> packet(id=CMD_REFRESHDATA)
		> packet(id=CMD_ACK_OK)

Where the data field comb grps, has the same fields of the response for the get unlock combination command.

## Get Group Info ##

To request parameters of a group, like the timezones, verify style and valid holidays flag, send the CMD_GRPTZ_RRQ command:

	> packet(id=CMD_GRPTZ_RRQ, <grp req>)
		> packet(id=CMD_ACK_OK, <grp settings>)

Where the grp req has the following fields:

|Name		|Description	|Value[hex]	|Size[bytes]	|Offset		|
|---		|---		|---		|---		|---		|
|group no	|Group number.	|varies		|1		|0		|
|		|Fixed.		|zeros		|7		|1		|

The response data field, grp settings, has the following fields:

|Name		|Description						|Value[hex]	|Size[bytes]	|Offset		|
|---		|---							|---		|---		|---		|
|group no	|Group number.						|varies		|1		|0		|
|tz 1		|Timezone 1.						|varies (<)	|2		|1		|
|tz 2		|Timezone 2.						|varies (<)	|2		|3		|
|tz 3		|Timezone 3.						|varies (<)	|2		|5		|
|verify+holiday	|Sets the verify style and carries the holiday flag.	|varies		|1		|7		|

(<): Little endian format.

The field verify+holiday may be further broken down in the following fields:

|B7			|B6-B0		|
|---			|---		|
|Holiday enable flag	|Verify style.	|

If the holiday flag is equal to 1, the holidays are considered.

The verifiy style is almost the same codification used in the user verification mode, the only difference is the bit B7.

|Verification Mode(x)	|Value[base 10]	|Value[hex]	|
|---			|---		|---		|
|FP+PW+RF		|0		|0		|
|FP			|1		|1		|
|PIN			|2		|2		|
|PW			|3		|3		|
|RF			|4		|4		|
|FP+PW			|5		|5		|
|FP+RF			|6		|6		|
|PW+RF			|7		|7		|
|PIN&FP			|8		|8		|
|FP&PW			|9		|9		|
|FP&RF			|10		|a		|
|PW&RF			|11		|b		|
|FP&PW&RF		|12		|c		|
|PIN&FP&PW		|13		|d		|
|FP&RF+PIN		|14		|e		|

## Set Group Info ##

To create or modify an existing group send a command CMD_GRPTZ_WRQ with the new settings:

	> packet(id=CMD_GRPTZ_WRQ, <grp settings>)
		> packet(id=CMD_ACK_OK)
	> packet(id=CMD_REFRESHDATA)
		> packet(id=CMD_ACK_OK)

Where the data field grp settings has the same fields given on the get group info procedure.

## Get User Timezones ##

To request the user timezones send a CMD_USERTZ_RRQ command with the user index.

	> packet(id=CMD_USERTZ_RRQ, <usertz req>)
		> packet(id=CMD_ACK_OK, <user tz>)

Where the usertz req is only the user's internal index on machine, followed by 3 zeros (user sn in lil-endian (?)).

|Name		|Description		|Value[hex]	|Size[bytes]	|Offset		|
|---		|---			|---		|---		|---		|
|user sn	|Users internal index.	|varies		|1		|0		|
|		|Fixed.			|zeros		|3		|1		|

The structure user tz has the following fields:

|Name		|Description								|Value[hex]	|Size[bytes]	|Offset		|
|---		|---									|---		|---		|---		|
|group tz flag	|Indicates wheter the user is using the group timezone (yes=1, no=0).	|varies (<)	|2		|0		|
|tz 1		|Timezone 1								|varies (<)	|2		|2		|
|tz 2		|Timezone 2								|varies (<)	|2		|4		|
|tz 3		|Timezone 3								|varies (<)	|2		|6		|

(<): Little endian format.

If the user has less than 3 timezones, the unused fields are set to zero.

To check if the user is using the group's timezones, just request the user timezones and check the flag group tz flag.

## Set User Timezones ##

To set user's timezones use the command CMD_USERTZ_WRQ:

	> packet(id=CMD_USERTZ_WRQ, <new tz>)
		> packet(id=CMD_ACK_OK)
	> packet(id=CMD_REFRESHDATA)
		> packet(id=CMD_ACK_OK)

Where the new tz structure has the following fields:

|Name		|Description								|Value[hex]	|Size[bytes]	|Offset		|
|---		|---									|---		|---		|---		|
|user sn	|Users internal index.							|varies (<)	|4		|0		|
|user tz flag	|Indicates wheter the user is using the his own timezones (yes=1, no=0).|varies (<)	|4		|4		|
|tz 1		|Timezone 1								|varies (<)	|4		|8		|
|tz 2		|Timezone 2								|varies (<)	|4		|12		|
|tz 3		|Timezone 3								|varies (<)	|4		|16		|

If there are less than 3 timezones, the unused fields are set to zero.

To make the user use the group's timezones just sent a new tz structure with the user tz flag and timezones, set to zero.

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
