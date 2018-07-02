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
|DeleteUserInfoEx	|**O**			|Todo.|
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
|SSR_DeleteEnrollData	|**X**			| |
|SSR_DeleteEnrollDataExt|**X**			| |
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
|GetUserTmpExStr	|**X**			|Almost the same as GetUserTmpEx.|
|SetUserTmpExStr	|**X**			|Almost the same as SetUserTmpEx.|
|SSR_GetUserTmp		|**X**			|Almost the same as GetUserTmpEx.|
|SSR_GetUserTmpStr	|**X**			|Almost the same as GetUserTmpEx.|
|SSR_SetUserTmp		|**X**			|Almost the same as SetUserTmpEx.|
|SSR_SetUserTmpStr	|**X**			|Almost the same as SetUserTmpEx.|
|GetFPTempLength	|**O**			|Nothing to do with the machine.|
|GetFPTempLengthStr	|**O**			|Nothing to do with the machine.|
|FPTempConvert		|**O**			| |
|FPTempConvertStr	|**O**			| |
|FPTempConvertNew	|**O**			| |
|FPTempConvertNewStr	|**O**			| |
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
|size users info|Total size of user info entries.	|N*72 (<)	|2		|0		|
|zeros		|Null bytes.				|00 00		|2		|2		|
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
|			|Fixed.								|01 00 00 00 00 00 00 00 00				|9		|39	|
|user id		|User ID, stored as a string.					|varies							|9		|48	|
|fixed			|								|00 00 00 00 00 00 00 00 00 00 00 00 00 00 00		|15		|57	|

(<): Little endian format.
(*): The name string should be terminated with the null char `\x00`, so the allowed size for user name is really 23 chars.

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

## Enable User ##

Same as Set User Info procedure, in this case just the bit `E0` should be changed.

## Set User Verification Mode ##

To change the verification style of a given user, use the `CMD_VERIFY_WRQ` command, this packet should be sent with the new verification style, using a specific codification.

	> packet(id=CMD_VERIFY_WRQ, data=<verify info>)
		> packet(id=CMD_ACK_OK)

Where the verify info structure, is 24 bytes long and has the following fields:

|Name			|Description					|Value[hex]	|Size[bytes]	|Offset	|
|---			|---						|---		|---		|---	|
|user sn		|Internal serial number for the user.		|varies (<)	|2		|0	|
|verification mode	|Verification mode to be used, see next table.	|varies		|1		|2	|
|zeros			|Fixed.						|zeros		|21		|3	|

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

## Get User Verification Mode ##

To get the verification style of a given user, use the `CMD_VERIFY_RRQ` command.

	> packet(id=CMD_VERIFY_RRQ, <user sn>)
		> packet(id=CMD_ACK_OK, data=<verify info>)

Where the user sn identifies the user, the value is stored in a 2 byte field in little endian format.

The `verify info` is the same structure shown in the previous section.

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

Where the user sn identifies the user, the value is stored in a 2 byte field in little endian format.

### Get Fingerprint Template ###

The SDK uses this function, when deleting users data, to check if the user has other fingerprint templates, request all the fingerprints one by one.

The procedure uses the command `CMD_USERTEMP_RRQ`, to ask for a template:

	> packet(id=CMD_USERTEMP_RRQ, data=<fp tmp req>, reply number=<rN>)
		> packet(id=CMD_PREPARE_DATA, data=<prep struct>, reply number=<rN>)
		> packet(id=CMD_DATA, data=<dataset>, reply number=<rN>)
		> packet(id=CMD_ACK_OK, reply number=<rN>)
	> packet(id=CMD_REFRESHDATA)
		> packet(id=CMD_ACK_OK)

Where the `fp tmp req` has the following fields:

|Name		|Description					|Value[hex]	|Size[bytes]	|Offset		|
|---		|---						|---		|---		|---		|
|user sn	|Internal serial number for the user.		|varies (<)	|2		|0		|
|finger index	|Fingerprint index, stored as a number (0-9).	|varies		|1		|2		|

If the template doesn't exist, the device would reply with `CMD_ACK_ERROR`.

	> packet(id=CMD_USERTEMP_RRQ, data=<fp tmp req>)
		> packet(id=CMD_ACK_OK)

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
|total size of fptmps	|Total size fp template entries.|varies (<)	|2		|0		|
|zeros			|Null bytes.			|00 00		|2		|2		|
|template1 entry	|Fingerprint template 1.	|varies		|varies(+)	|4		|
|template2 entry	|Fingerprint template 2.	|varies		|varies(+)	|varies		|
|...			|...				|...		|...		|varies		|
|templateN entry	|Fingerprint template N.	|varies		|varies(+)	|varies		|

(<): Little endian format.
(+): The size of each template is store at the beginning of each template entry.

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

How the checksum is calculated is unknown, to check for a correct write operation, one may request the written fingerprint and apply a custom checksum to compare both templates.

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
