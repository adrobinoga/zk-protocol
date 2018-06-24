# Terminal Operations #

[Return](../protocol.md)

Author: Alexander Marin <alexanderm2230@gmail.com>

[TOC]

## Present Descriptions ##

Here is a list of SDK functions, from **Terminal.h** and **Property.h** files, that shows which functions can be replicated with the current spec:

|SDK function name		|Described (X=Yes, O=No)|Notes|
|---				|:---:			|---|
|Connect_Net			|**X**			|Without communication key.|
|Connect_Com			|**O**			|The serial communication it isn't considered.|
|Connect_USB			|**O**			|The USB communication it isn't considered.|
|Disconnect			|**X**			|Only for TCP/IP.|
|IsTFTMachine			|**X**			|Same result can be obtained with GetPlatform function.|
|GetDeviceStatus		|**X**			| |
|GetDeviceInfo			|**X**			| |
|SetDeviceInfo			|**X**			| |
|GetDeviceTime			|**X**			| |
|SetDeviceTime			|**X**			| |
|GetSerialNumber		|**X**			|See Generic Requests section.|
|GetProductCode			|**X**			|See Generic Requests section.|
|GetFirmwareVersion		|**X**			|See Get Parameters section.|
|GetSDKVersion			|**O**			|This has nothing to do with the machine.|
|GetDeviceIP			|**O**			|Irrelevant.|
|SetDeviceIP			|**O**			|Irrelevant.|
|GetDeviceMAC			|**O**			|Irrelevant.|
|SetDeviceMAC			|**O**			|Irrelevant.|
|GetWiegandFmt			|**O**			|Irrelevant.|
|SetWiegandFmt			|**O**			|Irrelevant.|
|GetCardFun			|**X**			|See Generic Requests section.|
|SetDeviceCommPwd|**O**|Irrelevant.|
|SetCommPassword|**O**|Irrelevant.|
|QueryState			|**X**			|See Get Parameters section.|
|GetVendor			|**X**			|See Generic Requests section.|
|GetDeviceStrInfo		|**X**			|See Generic Requests section.|
|GetPlatform			|**X**			|See Generic Requests section.|
|GetStrCardNumber		|**O**			|This is for BW devices.|
|SetStrCardNumber		|**O**			|This is for BW devices.|
|IsNewFirmwareMachine		|**O**			|This is only for newer firmware.|
|GetDeviceFirmwareVersion	|**O**			|This is only for newer firmware.|

## Connection ##

In this document only connection using TCP/IP is considered.

### Without Communication Key ###

To set a connection with the device we first need de ip address of the device, that can be manually set in the standalone machine.

First you need to setup a socket connection, using TCP/IP, with the given ip address and with the port 4370.

Then send a packet with the command `CMD_CONNECT`.

	packet(id=CMD_CONNECT)

Keep in mind that the session id and reply number, must be zero, since the session id hasn't been assigned by the machine, and the reply number starts at zero.

The device should reply with the reply code `CMD_ACK_OK`.

	packet(id=CMD_ACK_OK)

Keep in mind that the reply number must be the same of the sent packet(zero), but the session id is defined with this packet, the client should parse and store the session id from this reply packet.

The session id seems to be just a seconds counter, but the easiest way to get this number is just to extract the number from the device's connect reply.

Also it is worth to note that performing a connection after another connection doesn't change the session id, that means that the session is closed only after closing the connection.

After sending the connection command is acknowledged, set the SDKBuild parameter to 1 using a `CMD_OPTIONS_WRQ` command.

	> packet(id=CMD_OPTIONS_WRQ, data="SDKBuild=1\x00")
		> packet(id=CMD_ACK_OK)

### With Communication Key ###

When a communication key is set in the device, and if the device doesn't receive the corresponding key, then it will reply with the reply code `CMD_ACK_UNAUTH`, that means that the connection hasn't been authorized.

To do this a packet with the command `CMD_AUTH` must be sent, the data field carries the communication key, the problem is that the key appears to be hashed, and the hash function it isn't known yet, so currently there is no way to reproduce this procedure. Though this feature it isn't very useful, it only prevents other clients to send commands to the machine, but if someone have access to the network, it may see all the trafic, since the communication it isn't encrypted, given that case, an attacker could capture the hashed value and just send that value, to start a session and then send commands.

## Disconnection ##

To terminate a connection with a device just send packet with command `CMD_EXIT`.

	> packet(id=CMD_EXIT)
		> packet(id=CMD_ACK_OK)

Keep in mind that the session id and reply number must be consistent with previous steps.

After that you may terminate the socket connection.

## Get Device Status ##

To request status info from device, a disable command `CMD_DISABLEDEVICE` must be sent first, after that the device should reply with the command `CMD_ACK_OK`, then send the command `CMD_GET_FREE_SIZES`.

The device should reply with a command with the code `CMD_ACK_OK` and a data structure of 92 bytes.

Finally send the enable command `CMD_ENABLEDEVICE`, the device should reply with the `CMD_ACK_OK` code.

	> packet(id=CMD_DISABLEDEVICE)
		> packet(id=CMD_ACK_OK)
	> packet(id=CMD_GET_FREE_SIZES)
		> packet(id=CMD_ACK_OK, data=<status>)
	> packet(id=CMD_ENABLEDEVICE)
		> packet(id=CMD_ACK_OK)

Where **status** is the structure of 92 bytes with the relevant info, which has the following fields:

|SDK Value	|Name			|Description					|Position	|
|---		|---			|---						|---		|
|1		|admin count		|Number of administrators.			|48		|
|2		|user count		|Number of registered users.			|16		|
|3		|fp count		|Number of fingerprint templates on the machine.	|24		|
|4		|pwd count		|Number of passwords.				|52		|
|5		|oplog count		|Number of operation records.			|40		|
|6		|attlog count		|Number of attendance records.			|32		|
|7		|fp capacity		|Fingerprint template capacity.			|56		|
|8		|user capacity		|User capacity.					|60		|
|9		|attlog capacity	|Attendance record capacity.			|64		|
|10		|remaining fp		|Remaining fingerprint template capacity.	|68		|
|11		|remaining user		|Remaining user capacity.			|72		|
|12		|remaining attlog	|Remaining attendance record capacity.		|76		|
|21		|face count		|Number of faces.				|80		|
|22		|face capacity		|Face capacity.					|88		|

**Note**: All the values are 4-Byte wide and stored in little endian format.

The full data structure looks like this:

|Offset(base 10)	|0		|4		|8		|12			|
|:---:			|:---:		|:---:		|:---:		|:---:			|
|**0**			|0		|0		|0		|0			|
|**16**			|user count	|0		|fp count	|0			|
|**32**			|attlog count	|0		|oplog count	|0			|
|**48**			|admin count	|pwd count	|fp capacity	|user capacity		|
|**64**			|attlog capacity|remaining fp	|remaining user	|remaining attlog	|
|**80**			|face count	|0		|face capacity	|

The zeroed bytes can be seen as reserved bytes, some of them are actually accessed with the values [13,20] in the SDK function.

## Device Time ##

Time is a very important parameter for access control, if the time is not set correctly the timezone settings will not work as expected.

### Get Time ###

To request the device time send a `CMD_GET_TIME` command.

	packet(id=CMD_GET_TIME)

The device should reply with `CMD_ACK_OK` code and a 4 byte integer with the device time, stored in little endian format.

	packet(id=CMD_ACK_OK, data=<time>)

Where `time` is calculated with the following formula:

```python
enc_t = ((year%100)*12*31+((month-1)*31)+day-1)*(24*60*60)+(hour*60+minutes)*60+seconds
```
Which is approximately the number of seconds since 29 Aug 1999.

So to decode the date from this number, the following Python snippet may be used:

```python
seconds = int(enc_t % 60)
minutes = int((enc_t/60.)%60)
hour = int((enc_t/(3600.))%24)
day = int(((enc_t/(3600.*24.))%31))+1
month = int(((enc_t/(3600.*24.*31.))%12))+1
year = int((enc_t/(3600.*24.)) / 365)+2000
```

### Set Time ###

To change time send a packet with the command `CMD_SET_TIME` with a 4 byte integer with the new time, stored in little endian format, in the same format given for the get time function.

After that the machine should reply with `CMD_ACK_OK`, to make changes take effect send a `CMD_REFRESHDATA` command to load the new value, the machine should reply with `CMD_ACK_OK`.

This procedure can be summarized as follows:

	> packet(id=CMD_SET_TIME, data=<time>)
		> packet(id=CMD_ACK_OK)
	> packet(id=CMD_REFRESHDATA)
		> packet(id=CMD_ACK_OK)




## Generic Requests ##

The general procedure to request a list of parameters is:

1. Disable the device with a `CMD_DISABLEDEVICE` command.
2. Request a set of parameters.
3. Enable the device with a `CMD_ENABLEDEVICE` command.

Some parameters of the device can be requested with a separate command but there is a set of parameters that can be requested with the one command, `CMD_OPTIONS_RRQ`,  and an specific argument, which would be the name of the requested parameter.

	> packet(id=CMD_OPTIONS_RRQ, data="<param name>\x00")
		> packet(id=CMD_ACK_OK, data="<param name>=<param value>\x00")

|Function|Description|`<parameter  request>`||
|---|---|---|
|-|-|**Hex representation** |  **Ascii representation**|
|Get platform|Returns ...|7e 50 6c 61 74 66 6f 72  6d 00|~Platform\x00|
| | |7e 5a 4b 46 50 56 65 72 73 69 6f 6e 00|~ZKFPVersion\x00|
| | |5a 4b 46 61 63 65 56 65  72 73 69 6f 6e 00|ZKFaceVersion\x00|
| | |7e 4f 53 00|~OS\x00|
| | |7e 45 78 74 65 6e 64 46  6d 74 00|~ExtendFmt\x00|
| | |45 78 74 65 6e 64 4f 50  4c 6f 67 00|ExtendOPLog\x00|
| | |57 6f 72 6b 43 6f 64 65  00|WorkCode\x00|
| | |4c 61 6e 67 75 61 67 65  00|Language\x00|
| | |42 69 6f 6d 65 74 72 69  63 54 79 70 65 00|BiometricType\x00|
| | |46 69 6e 67 65 72 46 75  6e 4f 6e 00|FingerFunOn\x00|
| | |7e 49 73 4f 6e 6c 79 52  46 4d 61 63 68 69 6e 65 00|~IsOnlyRFMachine\x00|
| | |46 61 63 65 46 75 6e 4f  6e 00|FaceFunOn\x00|
| | |7e 4f 45 4d 56 65 6e 64  6f 72 00|~OEMVendor\x00|
| | |7e 44 65 76 69 63 65 4e  61 6d 65 00|~DeviceName\x00|
| | |4d 41 43 00|MAC\x00|
| | |7e 53 65 72 69 61 6c 4e  75 6d 62 65 72 00|~SerialNumber\x00|
| | |7e 50 72 6f 64 75 63 74  54 69 6d 65 00|~ProductTime\x00|
| | |7e 50 49 4e 32 57 69 64  74 68 00|~PIN2Width\x00|
| | |7e 49 73 41 42 43 50 69  6e 45 6e 61 62 6c 65 00|~IsABCPinEnable\x00|
| | |7e 54 39 46 75 6e 4f 6e  00|~T9FunOn\x00|

### Get Serial Number ###

To request the serial number of the device, send:

	> packet(id=CMD_OPTIONS_RRQ, data="~SerialNumber\x00")
		> packet(id=CMD_ACK_OK, data="~SerialNumber=<serial number>\x00")

### Get Product Code ###

To request the product code, send:

	> packet(id=CMD_OPTIONS_RRQ, data="~DeviceName\x00")
		> packet(id=CMD_ACK_OK, data="~DeviceName=<device name>\x00")

### Get Card Function ###

This procedure comprises the request of two parameters:

	> packet(id=CMD_OPTIONS_RRQ, data="~IsOnlyRFMachine\x00")
		> packet(id=CMD_ACK_OK, data="~IsOnlyRFMachine=<is_rf>\x00")
	> packet(id=CMD_OPTIONS_RRQ, data="~RFCardOn\x00")
		> packet(id=CMD_ACK_OK, data="~RFCardOn=<card_on>\x00")

Where `is_rf` is a boolean variable, 1-Byte wide, that corresponds to 1 if the machine **only** supports RFID tags and 0 in the opposite case. The variable `card_on` is also a boolean variable, 1-Byte wide, that corresponds to 1 if the machine supports the RFID tags.

### Get Vendor ###

Procedure to request the vendor name:

	> packet(id=CMD_OPTIONS_RRQ, data="~OEMVendor\x00")
		> packet(id=CMD_ACK_OK, data="~OEMVendor=<vendor name>\x00")

### Get Device String Info ###

Procedure to request the product time:

	> packet(id=CMD_OPTIONS_RRQ, data="~ProductTime\x00")
		> packet(id=CMD_ACK_OK, data="~ProductTime=yyyy-mm-dd HH:MM:SS\x00")

Where the time can be easily parsed, consider the time to be in 24-hour format.

### Get Platform ###

Procedure to request the platform:

	> packet(id=CMD_OPTIONS_RRQ, data="~Platform\x00")
		> packet(id=CMD_ACK_OK, data="~Platform=<platform name>\x00")

Where the  `platform name` specifies the device platform.

### Get Device Info ###

This procedure it is performed in the same way that the Generic Requests, that means that you should disable the device, get the parameters and then enable the device.


| SDK Number | Parameter name | Description | Permisions (RW/R) | Notes|
|---|---|---|---|
|1| NA | Maximum number of admins | NA | Fixed value it isn't requested to the machine. |
|2| DeviceID| Device ID.|RW|Value ranges from 1 to 254|
|3| NewLng |Language.|RW|For english it is 97|
|4|IdleMinute|Idle time.|RW|Given in minutes|
|5|LockOn|Lock control time.|RW|Given in seconds|
|6|AlarmAttLog|Attendance record quantity alarm|RW| |
|7|AlarmOpLog|Operation record quantity alarm|RW| |
|8|AlarmReRec|Minimun time to record the same attendance state.|RW| |
|9|RS232BaudRate|Baud rate for RS232/485|Valid values are 2400, 4800, 9600, 19200, 38400 57600, 115200. |
|10|NA|Parity check bit| NA | Fixed value at 0. |
|11|NA|Stop bit| NA | Fixed value at 0. |
|12|NA|Date separator| NA| Fixed value at 1.|
|13|NetworkOn|Enable flag for network functions| RW| |
|14|RS232On|Enable flag for RS232 | RW | |
|15|RS485On|Enable flag for RS485.| RW | |
|16|VoiceOn|Enable announcements(voice).| RW | |
|17|MSpeed|Perform high-speed comparison.|RW| |
|18|IdlePower|Idle mode. | RW | 87 indicates shutdown and 88 indicates hibernation. |
|19|AutoPowerOff|Automatic shutdown time.| RW| Value 255 indicates the machine to not shutdown automatically |
|20|AutoPowerOn|Automatic startup time| RW | |
|21|AutoPowerSuspend|Automatic hibernation time | RW |
|22|AutoAlarm1|Alarm 1 time| RW | |
|23|MThreshold|1:N comparison threshold. |RW | |
|24|EThreshold|Registration threshold. | RW| |
|25|VThreshold|1:1 comparison threshold. | RW | |
|26|ShowScore|Display matching score during verification| |
|27|UnlockPerson|Number of people that may unlock the door at the same time | | |
|28|OnlyPINCard|Verify only the card number. | | |
|29|HiSpeedNet|Network speed | | |
|30|MustEnroll|Accept only for registered cards.
|31|TOState|Timeout to return to the initial state |  | |
|32|TOState|Timeout to return to the initial state if there are no inputs after entering PIN | |
|33|TOMenu|Timeout to return to the initial state if there are no inputs after entering PIN | |
|34|DtFmt|Time format | |
|35|Must1To1|Flag for mandatory 1:1 comparison | | |
|36|AutoAlarm2|Alarm 2 time. | |
|37|AutoAlarm3|Alarm 3 time. | |
|38|AutoAlarm4|Alarm 4 time. | |
|39|AutoAlarm5|Alarm 5 time. | |
|40|AutoAlarm6|Alarm 6 time. | |
|41-56|AS{N}| Automatic status changing times| | -1 value indicates that the status will not change automatically.|
|41 |AS1| |
|42 |AS2 | |
|...| | |
|56 |AS16 | |
|57	|WGFailed ID| Wiegand failure ID | | |
|58|Wiegand duress ID| | |
|59|Wiegand zone bit | | |
|60|Pulse width of Wiegand outputs | | |
|61|Pulse interval for Wiegand outputs | | |
|62|ID of the start sector on the Mifare card where fingerprints are stored. | |
|63|Total number of sectors on the Mifare card where fingerprints are stored.| |
|64|Number of fingerprints stored on the Mifare card | | |
|65| Forbidden | | |
|66|Wheter to display the attendance status | |
|67|Unused | | |
|68|Unused | | |
|69|TCP Port | | |
|70|UDP Port | | |
|71|Fingerprint algorithm version | | |
|72|Face algorithm version | | |
|73|Finger vein version | | |
|74|FaceFunOn| | |
|75|PIN2Width| | |
|76|IsSupportABCPin| | |
|77|IMEFunOn | | |
|78|IsSupportAlarmExt| | |
|79|~DCTZ| | |
|80|~DOTZ| | |
|81|

### Set Device Info ###

This procedure is used to change parameters of the device (see table of Device Info Parameters).

	> packet(id=CMD_OPTIONS_WRQ, data="<parameter name>=<new value>\x00")
		> packet(id=CMD_ACK_OK)

Keep in mind that some parameters are read only.

## Get Firmware Version ##

To request the firmware version send a `CMD_GET_VERSION` command.

	packet(id=CMD_GET_VERSION)

The device should reply with a `CMD_ACK_OK` code and a data structure with the version tag and date of the firmware:

	packet(id=CMD_ACK_OK, data="Ver <version tag>  <date>\x00")

Example:

	packet(id=CMD_ACK_OK, data="Ver 6.81  Apr 28 2015\x00")

## Get Device State ##

To request the device current state send a `CMD_STATE_RRQ` command.

	packet(id=CMD_STATE_RRQ)

The device should reply with a `CMD_ACK_OK` code and the current device state, stored in the `session id` field in little endian format.

	packet(id=CMD_ACK_OK, session id=<state>)

Where the state is 1-Byte wide and may have one of the following values:

|Value|Description|
|---|---|
|0|Waiting state.|
|1|Fingerprint registration state.|
|2|Fingerprint identification state.|
|3|Menu access state.|
|4|Busy.|
|5|Waiting for card writing.|

[Return](../protocol.md)
