# Other Operations #

[Go to Main Page](../protocol.md)

Author: Alexander Marin <alexanderm2230@gmail.com>

[TOC]

## Current Descriptions ##

Here is a list of SDK functions, from **Data-Other.h** and **Other.h** files, that shows which functions can be replicated with the current spec:

| SDK function name | Described(X=Yes, O=No)| Notes  |
| --- |  :---:  |--- |
|TurnOffAlarm | **O** | Applicable to newer devices.|
|SetEventMode| **O** | Applicable to newer devices.|
|GetLastError| **O**| Applicable to newer devices.|
|ClearAdministrators| **O**| |
|EnableDevice | **X**| |
|EnableClock | **X**| |
|DisableDeviceWithTimeOut| **X**| |
|PowerOffDevice | **O**| |
|RestartDevice | **O**| |
|StartEnroll |**O**|Only applicable to BW devices.|
|StartEnrollEx |**X**| |
|StartVerify |**O**| |
|StartIdentify | **O**| |
|CancelOperation | **O**| |
|WriteLCD | **O**| |
|ClearLCD | **O**| |
|WriteCard | **O**| |
|EmptyCard | **O**| |
|GetHIDEventCardNumAsStr | **O**| |
|CaptureImage | **O**| |
|UpdateFirmware | **O**| |
|BeginBatchUpdate | **O**| |
|BatchUpdate | **O**| |
|CancelBatchUpdate | **O**| |
|PlayVoice | **O**| |
|PlayVoiceByIndex| **O**| |
|ReadAttRule | **O**| |
|SaveTheDataToFile | **O**| |
|ReadTurnInfo | **O**| |
|SSR_OutPutHTMLRep | **O**| |
|Connect_P4P| **O**| |
|GetDeviceStatusEx | **O**| |
|GetConnectStatus| **O**| |
|SetSysOption | **O**| |
|SearchDevice | **O**| |
|SetCommProType| **O**| |
|SSR_DelUserTmpExt | **O**| |

## Enable/Disable Device ##

If the device is disabled the fingerprint, keyboard and the rfid modules are unavailable, this is usually done when reading several parameters or when uploading data to the machine.

To enable the device send:

	> packet(id=CMD_ENABLEDEVICE)
		> packet(id=CMD_ACK_OK)

To disable the device send:

	> packet(id=CMD_ENABLEDEVICE)
		> packet(id=CMD_ACK_OK)

## Restart Device ##

To restart the device just send the restart command:

	packet(id=CMD_RESTART)

The machine will not acknowledge this command, after sending this command wait a few seconds to create a new connection.

## Enroll User ##

Before enrolling a new user a set of values should be requested.

	> packet(id=CMD_OPTIONS_RRQ, data="~PIN2Width\x00")
		> packet(id=CMD_ACK_OK, data="~PIN2Width=<user-id width>\x00")

With this request we may found how long the user id can be.

After that the  `~IsABCPinEnable` parameter is requested:

	packet(id=CMD_OPTIOS_RRQ, data="~IsABCPinEnable\x00")

Depending on the device the reply may be a `CMD_ACK_OK` or `CMD_ACK_ERROR`, if the reply is `CMD_ACK_ERROR`, that means that the device doesn't support alphanumeric symbols for user id values.

The parameter `~T9FunOn` is also requested, but its purpose is still unknown, since the reply is `CMD_ACK_ERROR` for F19 terminals.

Then send a cancel capture command `CMD_CANCELCAPTURE` to disable the normal reading from the fingerprint module.

	> packet(id=CMD_CANCELCAPTURE)
		> packet(id=CMD_ACK_OK)

Send enroll parameters with the `CMD_STARTENROLL` command

	> packet(id=CMD_STARTENROLL, data=<enroll data>)
		> packet(id=CMD_ACK_OK)

Where the enroll data field is a structure of 26 bytes

|Offset[base 10]|Field|
|---|---|
|0|user id (stored as a string)|
|user id size | zeros |
|24|finger index|
|25|flag|

Where the finger index is a number from 0 to 9, and it is store as a byte.

The flag may have 3 values

|Value|Meaning|
|---|---|
|0|Invalid fingerprint|
|1|Valid fingerprint|
|3|Duress fingerprint|

Then send the `CMD_STARTVERIFY` command to prompt the user to place the fingerprints:

	> packet(id=CMD_STARTVERIFY)
		> packet(id=CMD_ACK_OK)

At this point expect realtime packets from the machine


[Go to Main Page](../protocol.md)
