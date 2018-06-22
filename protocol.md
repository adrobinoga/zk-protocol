# Protocol Description of ZKTeco's Standalone Devices #

Author: Alexander Marin <alexanderm2230@gmail.com>

[TOC]


## Introduction ##

ZKTeco is a security company that provides business solutions to manage the attendance and access of employees to restricted areas.

Their devices may use biometric information like employee's face or fingerprint to grant access, also a PIN or an RFID card may be used.

They provide the [ZKAccess](https://www.zkteco.com/en/product_detail/158.html) software to manage the standalone terminals. And also and [SDK](https://www.zkteco.com/en/download_catgory.html) for standalone devices, to develop custom applications, but both options are targeted to the Windows platform, so in order to use this devices from a Linux system, a library is needed but to develop such lib, we first need the protocol specification, which can be extracted from some ZKTeco's documents and additional analysis of captured packets between Windows software and attendance terminals. There is currently a python package [zklib](https://pypi.org/project/zklib/) to do basic tasks but the documentation is very poor and the library lacks several functions.

**Disclaimer**: The protocol extraction was made with only one device(f19, a TFT series device), so the spec may present some flaws (e.g constant fields that aren't constant).


## Terminology ##

The documentation provided by ZKTeco, sometimes shows acronyms without prior definition, here we provide a list of useful terms that you may found in this wiki and in other documents.

|Term		|Meaning|
|---		|---|
|terminal	|Standalone device|
|machine	|Standalone device|
|FRT		|Fingerprint reader terminal|
|FFR		|Fingerprint and facial recognition terminal|
|BW devices	|Refers to all the standalone devices with a black & white screen|
|TFT devices	|Refers to all the standalone devices with a color TFT screen|
|iFace devices	|Refers to all the stantalone devices with face recognition feature, these devices have a touch screen|
|SSR		|Self service recorder|


## Protocol Overview ##

The **standalone** terminals are called in that way because they may be used without any communication with a "coordinator" or "manager", like the ZKAccess software, so the protocol is designed to:

- Get info from terminals: Attendance records, new users(added in the standalone terminal), device model, status, etc. The ZKAccess program, builds a database with this info.
- Set info in terminals: Add users, change access permissions, change device settings, etc.
- Report events: Corresponds to packets sent from the machine when specific actions take place, they are sent without a previous request.

Note that the communication can be done through:

- TCP/IP
- Serial
- USB

Here we consider a communication setup with TCP/IP.

The terminals are considered a **server**, so when attendance records are requested, this is referred as a **download** operation, in a similar way, when data is sent from PC to the device, this is referred as an **upload** operation.

When there are "intensive" operations (i.e. too much changes/transactions) the procedures begin with a disable-device command, this could be to prevent undefined behavior in the device. For small tasks, like get-device-time, there is no need for disabling and enabling the device. Simple tasks usually consist of a request followed by a reply.

Based on the SDK design, a classification of the protocol functions can be made.

- **Terminal operations**: Includes, procedures to manage communication with the machine, get/set time of device and to request generic information(device type, matching algorithm, etc).
- **Data operations**: Procedures to manage data in the device, they can be further divided according to the data to be modified.
   - **User**: Related to the modification of user info/settings.
   - **Record**: Related to attendance logs.
   - **Shorcut**: Related to configuration of shorcut keys.
   - **Workcode**: Related to modification of work codes.
   - **SMS**: Procedures to config short messaging options.
   - **Bell**: Procedures to config the bell behavior.
   - **Photo**: Related to modification of users photos.
   - **Voice**: Related to management of announcement settings.
   - **Theme**: Related to management of background picture in the device.
   - **App**: Related to available functions in the standalone device.
   - **Biometric**: Related to management of biometric data stored in the device.
- **Access**: Procedures to manage general access configuration (manage groups, timezones, holidays)
- **Realtime**: Realtime procedures.
- **Misc**: Miscellaneous procedures (take fingerprint picture, restart device, poweroff, etc).


## Packet Fields ##

All packets have the following fields:

|Name		|Description			|Value[hex]	|Size[bytes]	|Offset	|
|---		|---				|---		|---		|---	|
|start		|Indicates start of packet.	|5050827d	|4		|0	|
|payload size	|Size of packet payload.	|payload_size(<)|2		|4	|
|zeros		|Null bytes.			|0000		|2		|6	|
|payload	|Packet payload.		|varies		|payload_size	|8	|

(<): Little endian format.

The packets can be divided in regular packets and in realtime packets:
- Regular packets: These are the packets used for normal request and reply procedures.
- Realtime packets: These are the packets used to report events, the are sent by the machine without a previous connection, if a connection exists.


### Regular Packet Payload ###

For regular packets the payload can be decomposed in the following fields:

|Name		|Description					|Value[hex]	|Size[bytes]	|Offset	|
|---		|---						|---		|---		|---	|
|command id	|Command identifier/Reply code.			|varies(<)	|2		|0	|
|checksum	|Checksum.					|varies(<)	|2		|2	|
|session id	|Session id.					|varies(<)	|2		|4	|
|reply number	|Reply number.					|varies(<)	|2		|6	|
|data		|Specific data for the given command/reply.	|varies		|payload_size-8	|8	|

(<): Little endian format.


#### Command/Reply Identifiers ####

The command/reply id field may be used for two purposes:

1. Instruct the machine to do something.
2. Return an exit code from a given procedure.

The command id correspondence is given in the following table:

|Name			|Description				|Value[base10]	|Value[hex]	|
|---			|---					|---		|---		|
|CMD_CONNECT		|Begin connection.			|1000		|3e8		|
|CMD_EXIT		|Disconnect.				|1001		|3e9		|
|CMD_ENABLEDEVICE	|Change machine state to "normal work".	|1002		|3ea		|
|CMD_DISABLEDEVICE	|Change machine state to "busy".	|1003		|3eb		|
|CMD_RESTART		|Restart machine.			|1004		|3ec		|
|CMD_POWEROFF		|Shut-down machine.			|1005		|3ed		|
|CMD_SLEEP		|Change machine state to "idle".	|1006		|3ee		|
|CMD_RESUME		|Change machine state to "awaken".	|1007		|3ef		|
|CMD_CAPTUREFINGER	|Capture fingerprint picture.		|1009		|3f1		|
|CMD_TEST_TEMP		|Test if fingerprint exists.		|1011		|3f3		|
|CMD_CAPTUREIMAGE	|Capture the entire image.		|1012		|3f4		|
|CMD_REFRESHDATA	|Refresh the machine interior data.	|1013		|3f5		|
|CMD_REFRESHOPTION	|Refresh the configuration parameter.	|1014		||
|CMD_TESTVOICE		|Play voice.				|1017		||
|CMD_GET_VERSION	|Obtain the firmware edition.		|1100		||
|CMD_CHANGE_SPEED	|Change transmission speed.		|1101		||
|CMD_AUTH		| |1102		|		|
|CMD_PREPARE_DATA	| |1500		|		|
|CMD_DATA		| |1501		|		|
|CMD_FREE_DATA		| |1502		|		|
|CMD_DB_RRQ		| |7		|		|
|CMD_USER_WRQ		| |8		|		|
|CMD_USERTEMP_RRQ	| |9		|		|
|CMD_USERTEMP_WRQ	| |10		|		|
|CMD_OPTIONS_RRQ	| |11		|		|
|CMD_OPTIONS_WRQ	| |12		|		|
|CMD_ATTLOG_RRQ		| |13		|		|
|CMD_CLEAR_DATA		| |14		|		|
|CMD_CLEAR_ATTLOG	| |15		|		|
|CMD_DELETE_USER	| |18		|		|
|CMD_DELETE_USERTEMP	| |19		|		|
|CMD_CLEAR_ADMIN	| |20		|		|
|CMD_USERGRP_RRQ	| |21		|		|
|CMD_USERGRP_WRQ	| |22		|		|
|CMD_USERTZ_RRQ		| |23		|		|
|CMD_USERTZ_WRQ		| |24		|		|
|CMD_GRPTZ_RRQ		| |25		|		|
|CMD_GRPTZ_WRQ		| |26		|		|
|CMD_TZ_RRQ		| |27		|		|
|CMD_TZ_WRQ		| |28		|		|
|CMD_ULG_RRQ		| |29		|		|
|CMD_ULG_WRQ		| |30		|		|
|CMD_UNLOCK		| |31		|		|
|CMD_CLEAR_ACC		| |32		|		|
|CMD_CLEAR_OPLOG	| |33		|		|
|CMD_OPLOG_RRQ		| |34		|		|
|CMD_GET_FREE_SIZES	| |50		|		|
|CMD_ENABLE_CLOCK	| |57		|		|
|CMD_STARTVERIFY	| |60		|		|
|CMD_STARTENROLL	| |61		|		|
|CMD_CANCELCAPTURE	| |62		|		|
|CMD_STATE_RRQ		| |64		|		|
|CMD_WRITE_LCD		| |66		|		|
|CMD_CLEAR_LCD		| |67		|		|
|CMD_GET_PINWIDTH	| |69		|		|
|CMD_SMS_WRQ		| |70		|		|
|CMD_SMS_RRQ		| |71		|		|
|CMD_DELETE_SMS		| |72		|		|
|CMD_UDATA_WRQ		| |73		|		|
|CMD_DELETE_UDATA	| |74		|		|
|CMD_DOORSTATE_RRQ	| |75		|		|
|CMD_WRITE_MIFARE	| |76		|		|
|CMD_EMPTY_MIFARE	| |78		|		|
|CMD_GET_TIME		| |201		|		|
|CMD_SET_TIME		| |202		|		|
|CMD_REG_EVENT		| |500		|		|

See the codification of reply codes in the following table:

|Name			|Description						|Value[base10]	|Value[hex]	|
|---			|---							|---		|---		|
|CMD_ACK_OK		|The request was processed sucessfully.			|2000		|07d0		|
|CMD_ACK_ERROR		|There was an error when processing the request.	|2001		|07d1		|
|CMD_ACK_DATA		|							|2002		|07d2		|
|CMD_ACK_RETRY		|							|2003		|07d3		|
|CMD_ACK_REPEAT		|							|2004		|07d4		|
|CMD_ACK_UNAUTH		|							|2005		|07d5		|
|CMD_ACK_UNKNOWN	|							|65535		|ffff		|
|CMD_ACK_ERROR_CMD	|							|65533		|fffd		|
|CMD_ACK_ERROR_INIT	|							|65532		|fffc		|
|CMD_ACK_ERROR_DATA	|							|65531		|fffb		|


#### Checksum ####

The calculated checksum is a 16 bit integer, but it is calculated using a 32 bit integer.

1. Sum all the contents of the payload packet(without the checksum, obviously) as integers of 16 bits in little endian format.
2. If there is an odd number of bytes then fill the last short with zeros.
3. Then, from this result, extract a short integer from the positions 31-16 and add this number to the short integer given by 15-0 positions. 
4. Calculate the ones-complement to the number obtained in step 3.


The calculation of the checksum is presented with the following code in python:

```python
chk_32b = 0 # accumulates short integers to calculate checksum
j = 1 # iterates through payload

# make odd length packets, even
if len(payload)%2 == 1:
	payload += [0x00]

while j<len(payload):
	# extract short integer, in little endian, from payload
	num_16b = payload[j-1] + (payload[j]<<8)
	# accumulate
	chk_32b = chk_32b + num_16b
	# increment index by 2 bytes
	j += 2 

# adds the two first bytes to the other two bytes
chk_32b = (chk_32b & 0xffff) + ((chk_32b & 0xffff0000)>>16)

# calculate ones complement to get final checksum (in big endian)
chk_16b = hex(chk_32b ^ 0xFFFF)
```

**Test Vectors**

**Note**: The header is not included, these are only payloads.

**1.**
```
0b005a17f38d03005a4b4661636556657273696f6e00
```
After removing the checksum `5a17`.

```
0b00f38d03005a4b4661636556657273696f6e00
```

The calculation of the checksum should give you `175a`.

**2.**

Example packet, with checksum:
```
d007296af38d0a0009
```

The checksum should give you `6a29`.


#### Session ID ####

The session identifier it is a unique number assigned for every new connection, the machine returns the assigned session id after a connection request.


#### Reply Number ####

After a successful connection the counter starts from zero counting the number of replies, a command sent to the machine carries this reply counter and this number is the same for a valid reply from the machine.


#### Data ####

The contents of this field depend on the procedure. See Specific Operations sections.


### Realtime Packet Payload###

For realtime packets the payload differs a little from a regular packet:

- The command id is always CMD_REG_EVENT.
- The session id field is used to store the event code.
- The reply number is set to zero.

|Name		|Description					|Value[hex]	|Size[bytes]	|Offset	|
|---		|---						|---		|---		|---	|
|command id	|Command identifier/Reply code.			|0xf401(t)	|2		|0	|
|checksum	|Checksum.					|varies(<)	|2		|2	|
|event		|Event code identifier.				|varies(<)	|2		|4	|
|reply number	|Reply number.					|0x0000		|2		|6	|
|data		|Specific data for the given report.		|varies		|payload_size-8	|8	|

(<): Little endian format.
(t): This id corresponds to the command CMD_REG_EVENT(0x1f4).

#### Event Codes ####

The following table shows the codification for realtime events:

|Name			|Description		|Value[base10]	|Value[hex]	|
|---			|---			|---		|---		|
|EF_ATTLOG		|			|1		|1		|
|EF_FINGER		|			|2		|2		|
|EF_ENROLLUSER		|			|4		|4		|
|EF_ENROLLFINGER	|			|8		|8		|
|EF_BUTTON		|			|16		|10		|
|EF_UNLOCK		|			|32		|20		|
|EF_VERIFY		|			|128		|80		|
|EF_FPFTR		|			|256		|100		|
|EF_ALARM		|			|512		|200		|


## Specific Operations ##

### Conventions ###

In the following descriptions some conventions are used to make protocol descriptions simple but precise.

#### Regular Packet Creation ####

The packet formation process was presented in previous sections, the notation:

	packet(id=<command/reply code>, data=<payload data>)

Is a compact format to refer to a packet with the format:

|Name		|Description					|Value[hex]	|Size[bytes]	|Offset	|
|---		|---						|---		|---		|---	|
|start		|Indicates start of packet.			|5050827d	|4		|0	|
|payload size	|Size of packet payload.			|payload_size(<)|2		|4	|
|zeros		|Null bytes.					|0000		|2		|6	|
|**id**		|Command identifier/Reply code.			|varies(<)	|2		|8	|
|checksum	|Checksum.					|varies(<)	|2		|10	|
|session id	|Session id.					|varies(<)	|2		|12	|
|reply number	|Reply number.					|varies(<)	|2		|14	|
|**data**	|Specific data for the given command/reply.	|varies		|payload_size-8	|16	|

(<): Little endian format.

Where the others fields, the `payload size`, `checksum`, `session id` and `reply number` are calculated from context and the given packet parameters.

When data parameter is absent, an empty `payload data` field should be assumed.

Also, in this notation the id code is given in big endian (as seen on the table of command codes), and the data could be given as a textual description or as a sequence of hex numbers.

#### Realtime Packet Creation ####

What differs from a regular packet and a realtime packets are just the `session id` and `reply number` fields. Beyond that the other fields `payload size` and `checksum`, are calculated same as before.

This is summarized with the notation:

	rtpacket(event=<event code>, data=<payload data>)

In this notation the event code is given in big endian (as seen on the table of event codes), and the data could be given as a textual description or as a sequence of hex numbers.

### Terminal Operations ###

See [terminal.md](./sections/terminal.md)

### Data Operations ###

See [data.md](./sections/data.md)

### Access Operations ###

See [access.md](./sections/access.md)

### Realtime Operations ###

See [realtime.md](./sections/realtime.md)

### Misc Operations ###

See [misc.md](./sections/misc.md)

## Links and Sources ##

- [Python zklib repo](https://github.com/dnaextrim/python_zklib)
- ZKTeco Inc, *A series of standalone product: Communication protocol manual*
- [Facial & Fingerprint Recognition Product Series User Manual](https://www.zkteco.eu/uploads/downloads/technical_documents/user_manual/IFace-User-Manual.pdf)
- [2.4 inch Color Screen Series User Manual](http://www.zkteco.co.za/manuals/F18_User_Manual.pdf)
***
