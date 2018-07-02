# Other Operations #

[Go to Main Page](../protocol.md)

Author: Alexander Marin <alexanderm2230@gmail.com>

[TOC]

## Current Descriptions ##

Here is a list of SDK functions, from **Data-Other.h** and **Other.h** files, that shows which functions can be replicated with the current spec:

|SDK function name		|Described(X=Yes, O=No)	|Notes|
|---				|:---:			|---|
|TurnOffAlarm			|**O**			|Applicable to newer devices.|
|SetEventMode			|**O**			|Applicable to newer devices.|
|GetLastError			|**O**			|Applicable to newer devices.|
|ClearAdministrators		|**X**			| |
|EnableDevice			|**X**			| |
|EnableClock			|**X**			| |
|DisableDeviceWithTimeOut	|**X**			|This can be done with the enable command.|
|PowerOffDevice			|**X**			| |
|RestartDevice			|**X**			| |
|StartEnroll			|**O**			|Only applicable to BW devices.|
|StartEnrollEx			|**X**			| |
|StartVerify			|**O**			| |
|StartIdentify			|**X**			|Used inside enrolling procedure.|
|CancelOperation		|**X**			|Used inside enrolling procedure.|
|WriteLCD			|**O**			|Only applicable to BW devices.|
|ClearLCD			|**O**			|Only applicable to BW devices.|
|WriteCard			|**O**			|Todo.|
|EmptyCard			|**O**			|Todo.|
|GetHIDEventCardNumAsStr	|**O**			|Todo.|
|CaptureImage			|**O**			|Todo.|
|UpdateFirmware			|**O**			|Irrelevant.|
|BeginBatchUpdate		|**O**			|Batch operations aren't considered.|
|BatchUpdate			|**O**			|Batch operations aren't considered.|
|CancelBatchUpdate		|**O**			|Batch operations aren't considered.|
|PlayVoice			|**O**			|Irrelevant.|
|PlayVoiceByIndex		|**O**			|Irrelevant.|
|ReadAttRule			|**O**			|Only applicable to BW devices.|
|SaveTheDataToFile		|**O**			|Only applicable to BW devices.|
|ReadTurnInfo			|**O**			|Only applicable to BW devices.|
|SSR_OutPutHTMLRep		|**O**			|Only applicable to BW devices.|
|Connect_P4P			|**O**			|Applicable to some P2P devices.|
|GetDeviceStatusEx		|**O**			|Applicable to some P2P devices.|
|GetConnectStatus		|**O**			| |
|SetSysOption			|**X**			|Same as set device info, see [terminal.md](./terminal.md)|
|SearchDevice			|**O**			|Can be easily replicated.|
|SetCommProType			|**O**			|Nothing to do with the machine.|
|SSR_DelUserTmpExt		|**X**			|.|

## Clear Admins ##

This procedure clears all admin privileges, sets the admin level of all users to "common user", this procedure doesn't need any additional data.


	> packet(id=CMD_CLEAR_ADMIN)
		> packet(id=CMD_ACK_OK)

## Enable/Disable Device ##

If the device is disabled the fingerprint, keyboard and the rfid modules are unavailable, this is usually done when reading several parameters or when uploading data to the machine.

To enable the device send:

	> packet(id=CMD_ENABLEDEVICE)
		> packet(id=CMD_ACK_OK)

To disable the device send:

	> packet(id=CMD_DISABLEDEVICE)
		> packet(id=CMD_ACK_OK)

## Enable Clock ##

To enable/disable the clock dots on the machine screen, send the command `CMD_ENABLE_CLOCK`.

	> packet(id=CMD_ENABLE_CLOCK)
		> packet(id=CMD_ACK_OK)

## Power Off ##

To shut down the device send the command `CMD_POWEROFF` followed by the command `CMD_EXIT`:

	> packet(id=CMD_POWEROFF)
		> packet(id=CMD_ACK_OK)
	> packet(id=CMD_EXIT)
		> packet(id=CMD_ACK_OK)

Then close the socket.

## Restart Device ##

To restart the device just send the restart command:

	packet(id=CMD_RESTART)

The machine will not acknowledge this command, close the connection and then wait a few seconds to create a new connection.

## Enroll User ##

Before enrolling a new user a set of values should be requested.

	> packet(id=CMD_OPTIONS_RRQ, data="~PIN2Width\x00")
		> packet(id=CMD_ACK_OK, data="~PIN2Width=<user-id width>\x00")

With this request we may found how long the user id can be, the given result should be converted to int.

After that the  `~IsABCPinEnable` parameter is requested:

	packet(id=CMD_OPTIOS_RRQ, data="~IsABCPinEnable\x00")

Depending on the device the reply may be a `CMD_ACK_OK` or `CMD_ACK_ERROR`, if the reply is `CMD_ACK_ERROR`, that means that the device doesn't support alphanumeric symbols for user id values.

The parameter `~T9FunOn` is also requested, but its purpose is still unknown, since the reply is `CMD_ACK_ERROR` for F19 terminals.

Then send a cancel capture command `CMD_CANCELCAPTURE`, to disable the normal reading from the fingerprint module.

	> packet(id=CMD_CANCELCAPTURE)
		> packet(id=CMD_ACK_OK)

Send enroll parameters with the `CMD_STARTENROLL` command

	> packet(id=CMD_STARTENROLL, data=<enroll data>)
		> packet(id=CMD_ACK_OK)

Where the enroll data field is a structure of 26 bytes

|Name		|Description				|Value[hex]	|Size[bytes]	|Offset		|
|---		|---					|---		|---		|---		|
|user id	|User's id given as a string.		|varies		|user-id width	|0		|
|zeros		|Fixed.					|		|		|user-id width	|
|finger index	|Fingerprint index.			|varies		|1		|24		|
|fp flag	|Fingerprint flag.			|vareis		|1		|25		|

Where the finger index is a number from 0 to 9, and it is stored as a number.

The flag may have 3 values

|Value	|Meaning		|
|---	|---			|
|0	|Invalid fingerprint	|
|1	|Valid fingerprint	|
|3	|Duress fingerprint	|

Then send the `CMD_STARTVERIFY` command to prompt the user to place the fingerprints:

	> packet(id=CMD_STARTVERIFY)
		> packet(id=CMD_ACK_OK)

At this point, expect realtime packets from the machine, see [realtime.md](./realtime.md) for more details.

The device will show messages on screen, asking the user for the fingerprints, which will be three for a valid enrolling. When the user puts the fingerprint on the reader, the machine will send a `EF_FINGER` event, followed by a `EF_FPFTR` event with the score of the given fingerprint:

		> rtpacket(event=EF_FINGER)
	> packet(id=CMD_ACK_OK)
		> rtpacket(event=EF_FPFTR, data=<score>)
	> packet(id=CMD_ACK_OK)

For a valid fingerprint sample, the score should be 100(0x64)

After three valid samples or after an invalid sample, the device should send a packet with the event `EF_ENROLLFINGER`, with a data structure:

		> rtpacket(event=EF_ENROLLFINGER, data=<enroll result>)
	> packet(id=CMD_ACK_OK)

The structure enroll result is described on [Realtime/Enrolled Finger](./realtime.md) section.

## Start Identify ##

Put the machine at 1 to N comparison state:

	> packet(id=CMD_STARTVERIFY)
		> packet(id=CMD_ACK_OK)

This procedure is used internally in the enrolling fingerprint procedure.

## Cancel Operation ##

Disable normal verification state of the machine:

	> packet(id=CMD_CANCELCAPTURE)
		> packet(id=CMD_ACK_OK)

This procedure is used internally in the enrolling fingerprint procedure.

## Delete User's Fingerprint Template ##

See [data.md](./data.md).

[Go to Main Page](../protocol.md)
