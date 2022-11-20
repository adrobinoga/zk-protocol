# User Data Operations #

[Go to Main Page](../protocol.md)

Author: Alexander Marin <alexanderm2230@gmail.com>

[TOC]

## Current Descriptions ##

Here is a list of SDK functions, from **Data-User.h** file, that shows which functions can be replicated with the current spec:


|SDK function name	|Described(X=Yes, O=No)	|Notes|
|---			|:---:			|---|
|ReadAllUserID		|**X**			| |
|GetAllUserID		|**O**			|Applicable only to BW.|
|GetAllUserInfo		|**O**			|Applicable only to BW.|
|EnableUser		|**O**			|Applicable only to BW.|
|SSR_EnableUser		|**X**			| |
|ModifyPrivilege	|**O**			|Applicable only to BW.|
|SetUserInfo		|**O**			|Applicable only to BW.|
|GetUserInfo		|**O**			|Applicable only to BW.|
|SetUserInfoEx		|**X**			| |
|GetUserInfoEx		|**X**			| |
|DeleteUserInfoEx	|**X**			|Same as using SetUserInfoEx with value 0.|
|SSR_GetAllUserInfo	|**X**			|Operates on the info obtained with ReadAllUserID.|
|SSR_GetUserInfo	|**X**			|Operates on the info obtained with ReadAllUserID.|
|SSR_SetUserInfo	|**X**			| |
|GetUserInfoByPIN2	|**O**			|Applicable only to BW.|
|GetUserInfoByCard	|**O**			|Applicable only to BW.|
|GetUserIDByPIN2	|**O**			|Applicable only to BW.|
|GetPIN2		|**O**			|Applicable only to BW.|
|GetEnrollData		|**O**			|Applicable only to BW.|
|SetEnrollData		|**O**			|Applicable only to BW.|
|DeleteEnrollData	|**O**			|Applicable only to BW.|
|SSR_DeleteEnrollData	|**X**			|See Delete Enroll Data.|
|SSR_DeleteEnrollDataExt|**X**			|See Delete Enroll Data.|
|GetEnrollDataStr	|**O**			|Applicable only to BW.|
|SetEnrollDataStr	|**O**			|Applicable only to BW.|
|ReadAllTemplate	|**X**			| |
|DelUserTmp		|**O**			|Applicable only to BW.|
|SSR_DelUserTmp		|**X**			|See Delete Enroll Data.|
|SSR_SetUserTmpExt	|**X**			| |
|GetUserTmp		|**O**			|Applicable only to BW.|
|SetUserTmp		|**O**			|Applicable only to BW.|
|GetUserTmpStr		|**O**			|Applicable only to BW.|
|SetUserTmpStr		|**O**			|Applicable only to BW.|
|GetUserTmpEx		|**X**			|May be done individually(see Get Fingerprint Template) or with all the templates in memory(see Read All Templates).|
|SetUserTmpEx		|**X**			| |
|GetUserTmpExStr	|**X**			|See Get Fingerprint Template.|
|SetUserTmpExStr	|**X**			|See Upload Fingerprint Template.|
|SSR_GetUserTmp		|**X**			|See Get Fingerprint Template.|
|SSR_GetUserTmpStr	|**X**			|See Get Fingerprint Template.|
|SSR_SetUserTmp		|**X**			|See Upload Fingerprint Template.|
|SSR_SetUserTmpStr	|**X**			|See Upload Fingerprint Template.|
|GetFPTempLength	|**O**			|Nothing to do with the machine.|
|GetFPTempLengthStr	|**O**			|Nothing to do with the machine.|
|FPTempConvert		|**O**			|Nothing to do with the machine.|
|FPTempConvertStr	|**O**			|Nothing to do with the machine.|
|FPTempConvertNew	|**O**			|Nothing to do with the machine.|
|FPTempConvertNewStr	|**O**			|Nothing to do with the machine.|
|SetUserFace		|**O**			|Applicable only to iFace devices.
|GetUserFace		|**O**			|Applicable only to iFace devices.
|DelUserFace		|**O**			|Applicable only to iFace devices.
|GetUserFaceStr		|**O**			|Applicable only to iFace devices.
|SetUserFaceStr		|**O**			|Applicable only to iFace devices.


## Read All User IDs ##

With this procedure the users info can be obtained, except the fingerprint templates.

First disable the device:

	> packet(id=CMD_DISABLEDEVICE)
		> packet(id=CMD_ACK_OK)

Then send a command with the id `CMD_DATA_WRRQ` and with a fixed payload of 11 bytes, field description for this payload it is still unknown.

	packet(id=CMD_DATA_WRRQ, data=0109000500000000000000)

Depending of the size of the users info structure, the device may send this info in two ways:

1.For "small" structures, the machine would send the info structure immediately

	> packet(id=CMD_DATA_WRRQ, data=0109000500000000000000)
		> packet(id=CMD_DATA, data=<users info>)

2.For bigger structures see the [Exchange of Data](ex_data.md) spec.

The fields of the `users info` structure are given in the following table:

|Name		|Description				|Value[hex]	|Size[bytes]	|Offset		|
|---		|---					|---		|---		|---		|
|size users info|Total size of user info entries.	|N*72 (<)	|4		|0		|
|user1 entry	|Info of user 1.			|varies		|72		|4		|
|user2 entry	|Info of user 2.			|varies		|72		|76		|
|...		|...					|varies		|72		|...		|
|userN entry	|info of user N.			|varies		|72		|info_size-72+4	|

(<): Little endian format.

The contents of each user entry, are shown in the next table:

|Name			|Description							|Value[hex]						|Size[bytes]	|Offset	|
|---			|---								|---							|---		|---	|
|user sn		|Internal serial number for the user.				|varies (<)						|2		|0	|
|permission token	|Sets permission for the given user and carries enable flag.	|varies							|1		|2	|
|password		|User password, stored as a string.				|varies							|8		|3	|
|name(*)		|User's name.							|varies							|24		|11	|
|card number		|User's card number, stored as int.				|varies (<)						|4		|35	|
|group no		|Group number to which the user belongs.			|varies							|1		|39	|
|user tz flag		|Indicates if the user is using his own's timezones.		|varies (<)						|2		|40	|
|tz1			|User's timezone 1, integer.					|varies (<)(t)						|2		|42	|
|tz2			|User's timezone 2. integer.					|varies (<)(t)						|2		|44	|
|tz3			|User's timezone 3. integer.					|varies (<)(t)						|2		|46	|
|user id		|User ID, stored as a string.					|varies							|9		|48	|
|			|Fixed zeros.							|00 00 00 00 00 00 00 00 00 00 00 00 00 00 00		|15		|57	|

(<): Little endian format.

(*): The name string should be terminated with the null char `\x00`, so the allowed size for user name is really 23 chars.

(t): If a timezone it isn't used or the users it is using group's timezone, these values are set to zero.

The permission token defines the permissions of the user and the also sets the state of the user.

|Bit Offset	|7-4	|3	|2	|1	|0	|
|---		|---	|---	|---	|---	|---	|
|--		|Unused	|P2	|P1	|P0	|E0	|

The number given by `P2P1P0` indicates the user admin level:

|P2P1P0	|Level		|
|---	|---		|
|000	|Common user	|
|001	|Enroll user	|
|011	|Admin		|
|111	|Super admin	|

The `E0` bit only enables/disables the user

|E0	|State		|
|---	|---		|
|0	|Eneabled	|
|1	|Disabled	|

Finally send the enable device command to put the device in normal operation:

	> packet(id=CMD_ENABLEDEVICE)
		> packet(id=CMD_ACK_OK)

### Example of a User Entry ###

This is an example of user entry:

	00000000: 0D 00 00 34 34 34 00 C6  9A 80 7C 4E 65 64 00 00  ...444....|Ned..
	00000010: 00 00 00 00 00 00 00 00  00 00 00 00 00 00 00 00  ................
	00000020: 00 00 00 DE 00 00 00 02  01 00 01 00 02 00 00 00  ................
	00000030: 35 35 35 00 00 00 00 00  00 00 00 00 00 00 00 00  555.............
	00000040: 00 00 00 00 00 00 00 00                           ........

In this case we have that the user's internal index is `0x000D`, the password is "444", and it should be noted that the password field contains non-zero bytes after the termination char, these could bytes are simply ignored.

From this entry we see that the user's name is "Ned", his card number is `0x000000DE`, he belongs to the group 2, he is using his own timezones, 1 and 2, and his user id is "555".

## Enable User ##

Same as Set User Info procedure, in this case just the bit `E0` should be changed.

## Set User Verification Mode ##

To change the verification style of a given user, use the `CMD_VERIFY_WRQ` command, this packet should be sent with the new verification style, using a specific codification.

	> packet(id=CMD_VERIFY_WRQ, data=<verify info>)
		> packet(id=CMD_ACK_OK)

Where the `verify info` structure, is 24 bytes long and has the following fields:

|Name			|Description					|Value[hex]	|Size[bytes]	|Offset	|
|---			|---						|---		|---		|---	|
|user sn		|Internal serial number for the user.		|varies (<)	|2		|0	|
|verification mode	|Verification mode to be used, see next table.	|varies		|1		|2	|
|			|Fixed zeros.					|zeros		|21		|3	|

(<): Little endian format.

|Verification Mode(x)	|Value[base 10]	|Value[hex]	|
|---			|---		|---		|
|Group Verify		|0		|0		|
|FP+PW+RF		|128		|80		|
|FP			|129		|81		|
|PIN			|130		|82		|
|PW			|131		|83		|
|RF			|132		|84		|
|FP+PW			|133		|85		|
|FP+RF			|134		|86		|
|PW+RF			|135		|87		|
|PIN&FP			|136		|88		|
|FP&PW			|137		|89		|
|FP&RF			|138		|8a		|
|PW&RF			|139		|8b		|
|FP&PW&RF		|140		|8c		|
|PIN&FP&PW		|141		|8d		|
|FP&RF+PIN		|142		|8e		|

(x): The symbol "+" is used as a logic **or**, that means the verification may be performed in either way, while the symbol "&" is used as logic **and**, that means the verication needs both methods to be accepted.

### Example ###

In this case the user with the index `0x000D` is set to use the PIN&FP&PW verification style.

	00000000: 50 50 82 7D 20 00 00 00  4F 00 EA 79 E7 84 45 00  PP.} ...O..y..E.
	00000010: 0D 00 8D 00 00 00 00 00  00 00 00 00 00 00 00 00  ................
	00000020: 00 00 00 00 00 00 00 00

## Get User Verification Mode ##

To get the verification style of a given user, use the `CMD_VERIFY_RRQ` command.

	> packet(id=CMD_VERIFY_RRQ, <user sn>)
		> packet(id=CMD_ACK_OK, data=<verify info>)

Where the `user sn` field identifies the user, the value is stored in a 2 byte field in little endian format.

The `verify info` is the same structure used for the "Set User Verification Mode" procedure.

## Set User Info ##

This procedure is used to modify info of existing users, if the user doesn't exist, then it will be created.

Here is a list of the parameters that may be changed with this procedure:

- User ID.
- User name.
- User password.
- User admin level.
- Enable state.

The idea is to send a new user entry to overwrite the previous user data, to do this first disable the device, use the `CMD_USER_WRQ` command to send the new user entry, which has the same fields shown in the "Read All User IDs" section. Finally refresh the data and enable the device.

	> packet(id=CMD_DISABLEDEVICE)
		> packet(id=CMD_ACK_OK)
	> packet(id=CMD_USER_WRQ, data=<new entry>)
		> packet(id=CMD_ACK_OK)
	> packet(id=CMD_REFRESHDATA)
		> packet(id=CMD_ACK_OK)
	> packet(id=CMD_ENABLEDEVICE)
		> packet(id=CMD_ACK_OK)

### Example ###

In this example, a `new entry` structure is shown, the user index is `0x000E`, the user is enabled and set with admin permissions `0x06`, is using password "123456", his name is "Nuevo", his card number is `6543`, belongs to group 1, it is using group's timezones, and his id number is "11224488".

	00000000: 50 50 82 7D 50 00 00 00  08 00 FC 57 9D 8A 5C 00  PP.}P......W..\.
	00000010: 0E 00 06 31 32 33 34 35  36 00 7C 4E 75 65 76 6F  ...123456.|Nuevo
	00000020: 00 00 00 00 00 00 00 00  00 00 00 00 00 00 00 00  ................
	00000030: 00 00 00 8F 19 00 00 01  00 00 00 00 00 00 00 00  ................
	00000040: 31 31 32 32 34 34 38 38  00 00 00 00 00 00 00 00  11224488........
	00000050: 00 00 00 00 00 00 00 00

## Delete Enroll Data ##

The SDK provides functions to delete:

1. Individual fingerprint templates.
2. Delete the password and if no fingerprint data or password exist, then also remove the user.
3. Delete all the fingerprint templates of a given user.
4. Delete a user.

These options are based on the following basic operations:

1. Delete indivitual fingerprint templates.
2. Delete password.
3. Delete all fingerprint templates.
4. Delete user.
5. Get fingerprint template.

These procedures are explained below.

### Deleting a Fingerprint Template ###

To delete one fingerprint of a given user, follow the next procedure:

	> packet(id=CMD_DEL_FPTMP, data=<del info>)
		> packet(id=CMD_ACK_OK)
	> packet(id=CMD_REFRESHDATA)
		> packet(id=CMD_ACK_OK)

Where the `del info` structure includes data about the fingerprint to delete:

|Name		|Description				|Value[hex]	|Size[bytes]	|Offset		|
|---		|---					|---		|---		|---		|
|user id	|User's id given as a string.		|varies		|user-id width	|0		|
|zeros		|Fixed.					|		|		|user-id width	|
|finger index	|Fingerprint index.			|varies		|1		|24		|

### Delete Password ###

This can be easily done with the set user info procedure, just overwrite the password with zeros.

### Delete All Fingerprint Templates ###

To delete all fingerprint templates of a given user, follow the next procedure:

	> packet(id=CMD_DELETE_USERTEMP, data=<del all info>)
		> packet(id=CMD_ACK_OK)
	> packet(id=CMD_REFRESHDATA)
		> packet(id=CMD_ACK_OK)

Where the `del all info` structure specifies the user:

|Name		|Description				|Value[hex]	|Size[bytes]	|Offset		|
|---		|---					|---		|---		|---		|
|user sn	|Internal serial number for the user.	|varies (<)	|2		|0		|
|		|Fixed.					|00		|1		|2		|

(<): Little endian format.

### Delete User ###

To delete a user, follow the next procedure:

	> packet(id=CMD_DELETE_USER, data=<user sn>)
		> packet(id=CMD_ACK_OK)
	> packet(id=CMD_REFRESHDATA)
		> packet(id=CMD_ACK_OK)

Where the` user sn` identifies the user, the value is stored in a 2 byte field in little endian format.

### Get Fingerprint Template ###

The SDK uses this function, when deleting users data, to check if the user has other fingerprint templates, it requests all the fingerprints one by one.

The procedure uses the command `CMD_USERTEMP_RRQ`, to ask for a template:

	> packet(id=CMD_USERTEMP_RRQ, data=<fp tmp req>, reply number=<rN>)
		> packet(id=CMD_PREPARE_DATA, data=<prep struct>, reply number=<rN>)
		> packet(id=CMD_DATA, data=<dataset>, reply number=<rN>)
		> packet(id=CMD_ACK_OK, reply number=<rN>)

Where the `fp tmp req` structure has the following fields:

|Name		|Description					|Value[hex]	|Size[bytes]	|Offset		|
|---		|---						|---		|---		|---		|
|user sn	|Internal serial number for the user.		|varies (<)	|2		|0		|
|finger index	|Fingerprint index, stored as a number (0-9).	|varies		|1		|2		|

And `dataset`, is just the binary fingerprint template, without any additional fields.

If the template doesn't exist, the device would reply with `CMD_ACK_ERROR`.

	> packet(id=CMD_USERTEMP_RRQ, data=<fp tmp req>)
		> packet(id=CMD_ACK_ERROR)

## Read All Templates ##

Follow the next procedure to get all the fingerprint templates.

First disable the device:

	> packet(id=CMD_DISABLEDEVICE)
		> packet(id=CMD_ACK_OK)

Then send a command with the id `CMD_DATA_WRRQ` and with a fixed payload, field description for this payload it is still unknown.

	packet(id=CMD_DATA_WRRQ, data=0107000200000000000000)

Depending of the size of the `fp templates` structure, the device may send this info in two ways:

1.For "small" structures, the machine would send the info structure immediately

	> packet(id=CMD_DATA_WRRQ, data=0107000200000000000000)
		> packet(id=CMD_DATA, data=<fp templates>)

2.For bigger structures see the [Exchange of Data](ex_data.md) spec.

The fields of the `fp templates` structure are given in the following table:

|Name			|Description			|Value[hex]	|Size[bytes]	|Offset		|
|---			|---				|---		|---		|---		|
|total size of fptmps	|Total size fp template entries.|varies (<)	|4		|0		|
|template1 entry	|Fingerprint template 1.	|varies		|varies(+)	|4		|
|template2 entry	|Fingerprint template 2.	|varies		|varies(+)	|varies		|
|...			|...				|...		|...		|varies		|
|templateN entry	|Fingerprint template N.	|varies		|varies(+)	|varies		|

(<): Little endian format.
(+): The size of each template is at the beginning of each template entry.

The contents of each template entry, are shown in the next table:

|Name		|Description					|Value[hex]		|Size[bytes]	|Offset	|
|---		|---						|---			|---		|---	|
|size tmp entry	|Size of fp template entry.			|(tmp size + 6) (<)	|2		|0	|
|user sn	|Internal serial number for the user.		|varies (<)		|2		|2	|
|fp index	|Fingerprint index, stored as a number (0-9).	|varies			|1		|4	|
|fp flag	|Fingerprint flag.				|varies			|1		|5	|
|fp template	|The binary fingerprint template.		|varies			|tmp size	|6	|

(<): Little endian format.

The fp flag indicates fingerprint type:

|Fingerprint	|Value	|
|---		|---	|
|invalid	|0	|
|valid		|1	|
|duress		|3	|

Finally send the enable device command to put the device in normal operation:

	> packet(id=CMD_ENABLEDEVICE)
		> packet(id=CMD_ACK_OK)

Note that in order to know which user is the owner of a fingerprint you should read all the users info.

### Example of a Template Entry ###

In this example, only the first 24 bytes are shown, the template entry is `0x04C4` bytes long, belongs to the user with index `0x000E`, the finger index is 4 and it is a valid fingerprint.

	00000000: C4 04 0E 00 04 01 4D FD  53 53 32 31 00 00 04 BE  ......M.SS21....
	00000010: BE 04 08 05 07 09 CE D0  ...

## Upload Fingerprint Template ##

Follow the next procedure to upload a fingerprint template.

First disable the device:

	> packet(id=CMD_DISABLEDEVICE)
		> packet(id=CMD_ACK_OK)

Upload a fingerprint template with the following sequence:

	> packet(id=CMD_PREPARE_DATA, data=<prep struct>)
		> packet(id=CMD_ACK_OK)
	> packet(id=CMD_DATA, data=<fp template>)
		> packet(id=CMD_ACK_OK)
	> packet(id=CMD_CHECKSUM_BUFFER)
		> packet(id=CMD_ACK_OK, data=<checksum>)
	> packet(id=CMD_TMP_WRITE, data=<tmp wreq>)
		> packet(id=CMD_ACK_OK)
	> packet(id=CMD_FREE_DATA)
		> packet(id=CMD_ACK_OK)

The `prep struct` indicates the size of the data to send, it has the following fields:

|Name		|Description			|Value[hex]		|Size[bytes]	|Offset	|
|---		|---				|---			|---		|---	|
|data size	|Size of the data to send.	|(fp template size) (<)	|2		|0	|
|		|Fixed.				|0000			|2		|2	|

(<): Little endian format.

The checksum is a bit complicated to compute, check out [../code/Checksum32_CMD119.java](the code is java).

The command `CMD_TMP_WRITE` seems to be the command that transfers the buffer contents to the proper location of fingerprint templates.

The `tmp wreq` structure has the following fields:

|Name			|Description					|Value[hex]		|Size[bytes]	|Offset	|
|---			|---						|---			|---		|---	|
|user sn		|Internal serial number for the user.		|varies (<)		|2		|0	|
|fp index		|Fingerprint index, stored as a number (0-9).	|varies			|1		|2	|
|fp flag		|Fingerprint flag.				|varies			|1		|3	|
|fp template size	|Size of the fingerprint template.		|varies (<)		|2		|4	|

(<): Little endian format.

The previous sequence should be executed for every upload, if there are no more templates to upload, refresh the data and then enable the device:

	> packet(id=CMD_REFRESHDATA)
		> packet(id=CMD_ACK_OK)
	> packet(id=CMD_ENABLEDEVICE)
		> packet(id=CMD_ACK_OK)

[Go to Main Page](../protocol.md)
