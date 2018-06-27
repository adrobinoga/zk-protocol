# Terminal Operations #

[Go to Main Page](../protocol.md)

Author: Alexander Marin <alexanderm2230@gmail.com>

[TOC]

## Current Descriptions ##

Here is a list of SDK functions, from **Terminal.h** and **Property.h** files, that shows which functions can be replicated with the current spec:

|SDK function name		|Described (X=Yes, O=No)|Notes							|
|---				|:---:			|---							|
|Connect_Net			|**X**			|Without communication key.				|
|Connect_Com			|**O**			|The serial communication it isn't considered.		|
|Connect_USB			|**O**			|The USB communication it isn't considered.		|
|Disconnect			|**X**			|Only for TCP/IP.					|
|IsTFTMachine			|**X**			|Same result can be obtained with GetPlatform function.	|
|GetDeviceStatus		|**X**			|							|
|GetDeviceInfo			|**X**			|See Generic Requests section.				|
|SetDeviceInfo			|**X**			|See Generic Requests section.				|
|GetDeviceTime			|**X**			|							|
|SetDeviceTime			|**X**			|							|
|GetSerialNumber		|**X**			|See Generic Requests section.				|
|GetProductCode			|**X**			|See Generic Requests section.				|
|GetFirmwareVersion		|**X**			|							|
|GetSDKVersion			|**O**			|This has nothing to do with the machine.		|
|GetDeviceIP			|**O**			|Irrelevant.						|
|SetDeviceIP			|**O**			|Irrelevant.						|
|GetDeviceMAC			|**O**			|Irrelevant.						|
|SetDeviceMAC			|**O**			|Irrelevant.						|
|GetWiegandFmt			|**O**			|Irrelevant.						|
|SetWiegandFmt			|**O**			|Irrelevant.						|
|GetCardFun			|**X**			|See Generic Requests section.				|
|SetDeviceCommPwd		|**O**			|Connection with commkey is not supported.		|
|SetCommPassword		|**O**			|Connection with commkey is not supported.		|
|QueryState			|**X**			|							|
|GetVendor			|**X**			|See Generic Requests section.				|
|GetDeviceStrInfo		|**X**			|See Generic Requests section.				|
|GetPlatform			|**X**			|See Generic Requests section.				|
|GetStrCardNumber		|**O**			|This is for BW devices.				|
|SetStrCardNumber		|**O**			|This is for BW devices.				|
|IsNewFirmwareMachine		|**O**			|Same result can be obtained with GetPlatform function.	|
|GetDeviceFirmwareVersion	|**O**			|This is only for newer firmware.			|

## Connection ##

In this document only connection using TCP/IP is considered.

### Without Communication Key ###

To set a connection with the device we first need de ip address of the device, that can be manually set in the standalone machine.

First you need to setup a socket connection, using TCP/IP, with the given ip address and with the port 4370.

Then send a packet with the command `CMD_CONNECT`.

	packet(id=CMD_CONNECT)

Keep in mind that for this packet the session id and reply number, must be zero, since the session id hasn't been assigned by the machine, and the reply number starts at zero.

The device should reply with the reply code `CMD_ACK_OK`.

	packet(id=CMD_ACK_OK)

Keep in mind that the reply number must be the same of the sent packet(zero), but the session id is defined with this packet, the client should parse and store the session id from this reply packet.

After sending the connection command is acknowledged, set the SDKBuild parameter to 1 using a `CMD_OPTIONS_WRQ` command.

	> packet(id=CMD_OPTIONS_WRQ, data="SDKBuild=1\x00")
		> packet(id=CMD_ACK_OK)

### With Communication Key ###

When a communication key is set in the device, and if the device doesn't receive the corresponding key, then it will reply with the reply code `CMD_ACK_UNAUTH`, that means that the connection hasn't been authorized.

To do this a packet with the command `CMD_AUTH` must be sent, the data field carries the communication key, the problem is that the key appears to be hashed, and the hash function it isn't known yet, so currently there is no way to reproduce this procedure. Though this feature it isn't very useful, it only prevents other clients to send commands to the machine, but if someone have access to the network, it may still see all the trafic, since the communication it isn't encrypted, given that case, an attacker could capture the hashed value and just send that value, to start a session and then send commands (like unlock door). **Therefore the network used for the system must be dedicated and kept away from intruders**.

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
|3		|fp count		|Number of fingerprint templates on the machine.|24		|
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

	packet(id=CMD_ACK_OK, data=<enc_t>)

Where `enc_t` is calculated with the following formula:

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

	> packet(id=CMD_SET_TIME, data=<enc_t>)
		> packet(id=CMD_ACK_OK)
	> packet(id=CMD_REFRESHDATA)
		> packet(id=CMD_ACK_OK)

## Generic Requests ##

The general procedure to read/write a list of parameters, is:

1. Disable the device with a `CMD_DISABLEDEVICE` command.
2. Read/write a set of parameters.
3. Enable the device with a `CMD_ENABLEDEVICE` command.

### Generic Read of Parameters ###

Some parameters of the device can be requested with a separate command but there is a set of parameters that can be requested with the one command, `CMD_OPTIONS_RRQ`,  and an specific argument, which would be the name of the parameter.

	> packet(id=CMD_OPTIONS_RRQ, data="<param name>\x00")
		> packet(id=CMD_ACK_OK, data="<param name>=<param value>\x00")

**Notes**:

- The param values are given in string format, that means that if a 1 is returned, it actually corresponds to `0x31`.
- Booleans are represented with "1" and "0".
- Integers are given as a string number in base 10.
- Some of these values may not be present on the machine, in that case the machine would reply with `CMD_ACK_ERROR`.

Here is a list of some parameters that can be requested using that format:

|Parameter name		|Returns	|
|---			|---		|
|~Platform		|Plaform name	|
|~ZKFPVersion		|Integer	|
|ZKFaceVersion		|-		|
|~OS			|-		|
|~ExtendFmt		|-		|
|ExtendOPLog		|-		|
|WorkCode		|Bool		|
|Language		|Integer	|
|BiometricType		|-		|
|FingerFunOn		|Bool		|
|~IsOnlyRFMachine	|Bool		|
|FaceFunOn		|Bool		|
|~OEMVendor		|Vendor name	|
|~DeviceName		|Device name	|
|MAC			|MAC address	|
|~SerialNumber		|Serial number	|
|~ProductTime		|Date string	|
|~PIN2Width		|Integer	|
|~IsABCPinEnable	|Bool		|
|~T9FunOn		|Bool		|

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

This procedure it is performed in the same way that the Generic Read of Parameters:

	> packet(id=CMD_OPTIONS_RRQ, data="<param name>\x00")
		> packet(id=CMD_ACK_OK, data="<param name>=<param value>\x00")

This table is based on the SDK definitions.

|SDK Number	|Parameter name		|Description										|Permisions (RW/R)	|Notes|
|---		|---			|---											|---			|---|
|1		|			|Maximum number of admins.								|NA			|Fixed value it isn't requested to the machine.|
|2		|DeviceID		|Device ID.										|RW			|Value ranges from 1 to 254.|
|3		|NewLng			|Language.										|RW			|For english it is 97.|
|4		|IdleMinute		|The machine will enter standby state or power off, after this time elapses.		|RW			|Given in minutes.|
|5		|LockOn			|Lock control time.									|RW			|Given in seconds.|
|6		|AlarmAttLog		|Attendance record quantity alarm.							|RW			| |
|7		|AlarmOpLog		|Operation record quantity alarm.							|RW			| |
|8		|AlarmReRec		|Minimun time to record the same attendance state.					|RW			|Units are unknown.|
|9		|RS232BaudRate		|Baud rate for RS232/485.								|RW			|Valid values are 1200, 2400, 4800, 9600, 19200, 38400 57600, 115200.|
|10		|NA			|Parity check bit.									|NA			| Fixed value at 0.|
|11		|NA			|Stop bit.										|NA			| Fixed value at 0.|
|12		|NA			|Date separator.									|NA			| Return value fixed at 1.|
|13		|NetworkOn		|Enable flag for network functions.							|RW			| |
|14		|RS232On		|Enable flag for RS232.									|RW			| |
|15		|RS485On		|Enable flag for RS485.									|RW			| |
|16		|VoiceOn		|Enable announcements(voice).								|RW			| |
|17		|MSpeed			|Perform high-speed comparison.								|RW			|Value codification is unknown.|
|18		|IdlePower		|Idle mode.										|RW			|87 indicates shutdown and 88 indicates hibernation.|
|19		|AutoPowerOff		|Automatic shutdown time.								|RW			|Value 255 indicates the machine to not shutdown automatically.|
|20		|AutoPowerOn		|Automatic startup time.								|RW			|Value 255 indicates the machine to not startup automatically.|
|21		|AutoPowerSuspend	|Automatic hibernation time.								|RW			|Value 255 indicates the machine to not suspend automatically.|
|22		|AutoAlarm1		|Alarm 1 time.										|RW			|Value 65535 disables the alarm(t).|
|23		|MThreshold		|1:N comparison threshold.								|RW			|Integer.|
|24		|EThreshold		|Registration threshold.								|RW			|Integer.|
|25		|VThreshold		|1:1 comparison threshold.								|RW			|Integer.|
|26		|ShowScore		|Display matching score during verification.						|RW			|Bool.|
|27		|UnlockPerson		|Number of people that may unlock the door at the same time.				|RW			|Integer.|
|28		|OnlyPINCard		|Verify only the card number.								|RW			|Bool.|
|29		|HiSpeedNet		|Network speed.										|RW			|Value correspondence: 1=100M-H, 4=10M-F, 5=100M-F, 8=Auto, others=10M-H.|
|30		|MustEnroll		|Accept only registered cards.								|RW			|Bool.|
|31		|TOState		|Timeout to return to the initial state.						|RW			|Given in seconds.|
|32		|TOState		|Timeout to return to the initial state if there are no inputs after entering PIN.	|RW			|Given in seconds.|
|33		|TOMenu			|Timeout to return to the initial state if there are no inputs after entering menu.	|RW			|Given in seconds.|
|34		|DtFmt			|Time format.										|NA			|Value codification is unknown.|
|35		|Must1To1		|Flag for mandatory 1:1 comparison.							|RW			|Bool.|
|36		|AutoAlarm2		|Alarm 2 time.										|RW			|Value 65535 disables the alarm(t).|
|37		|AutoAlarm3		|Alarm 3 time.										|RW			|Value 65535 disables the alarm(t).|
|38		|AutoAlarm4		|Alarm 4 time.										|RW			|Value 65535 disables the alarm(t).|
|39		|AutoAlarm5		|Alarm 5 time.										|RW			|Value 65535 disables the alarm(t).|
|40		|AutoAlarm6		|Alarm 6 time.										|RW			|Value 65535 disables the alarm(t).|
|41-56		|AS{N}			|Automatic status changing times.							|?			|-1 value indicates that the status will not change automatically.|
|41		|AS1			|											|?			| |
|42		|AS2			|											|?			| |
|...		|AS{..}			|											|?			| |
|56		|AS16			|											|?			| |
|57		|WGFailedID		|Wiegand failure ID.									|?			| |
|58		|WGDuressID		|Wiegand duress ID.									|?			| |
|59		|WGSiteCode		|Wiegand zone bit.									|?			| |
|60		|WGPulseWidth		|Pulse width of Wiegand outputs.							|?			| |
|61		|WGPulseInterval	|Pulse interval for Wiegand outputs.							|?			| |
|62		|~RFSStart		|ID of the start sector on the Mifare card where fingerprints are stored.		|?			| |
|63		|~RFSLen		|Total number of sectors on the Mifare card where fingerprints are stored.		|?			| |
|64		|~RFFPC			|Number of fingerprints stored on the Mifare card.					|?			| |
|65		|			|Forbidden.										|NA			| |
|66		|~ShowState		|Wheter to display the attendance status.						|RW			| |
|67		|			|Unused											|NA			| |
|68		|			|Unused											|NA			| |
|69		|TCPPort		|TCP Port.										|?			| |
|70		|UDPPort		|UDP Port.										|?			| |
|71		|~ZKFPVersion		|Fingerprint algorithm version.								|R			| |
|72		|~ZKFaceVersion		|Face algorithm version.								|R			| |
|73		|~ZKFVVersion		|Finger vein version.									|R			| |
|74		|~FaceFunOn		|Face function.										|R			| |
|75		|~PIN2Width		|User id max length.									|R			| |
|76		|IsSupportABCPin	|Does the user id support chars.							|R			| |
|77		|IMEFunOn		|?											|?			| |
|78		|IsSupportAlarmExt	|?											|?			| |
|79		|~DCTZ			|?											|?			| |
|80		|~DOTZ			|?											|?			| |
|81		|			|Specify the param with the name.							|NA			| |

(t) : To obtain time, convert the string number to bin, then take the 8 most significant bits, the given number will be the hour, the minutes are simply the number given by the 8 least significant bits.

? : Stands for unknown.

NA : Stands for Not Applicable.

### Set Device Info ###

This procedure is used to change parameters of the device (see table of Device Info Parameters).

	> packet(id=CMD_OPTIONS_WRQ, data="<parameter name>=<new value>\x00")
		> packet(id=CMD_ACK_OK)
	> packet(id=CMD_REFRESHOPTION)
		> packet(id=CMD_ACK_OK)

Also, keep in mind that some parameters are read only.

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

|Value	|Description				|
|---	|---					|
|0	|Waiting state.				|
|1	|Fingerprint registration state.	|
|2	|Fingerprint identification state.	|
|3	|Menu access state.			|
|4	|Busy.					|
|5	|Waiting for card writing.		|

[Go to Main Page](../protocol.md)
