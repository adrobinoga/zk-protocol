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


## Managing Attendance ##

Before going into the actual protocol it would be convenient to clarify the type of things that can be done with ZKAccess Standalone Devices.

### User Properties ###

A user is the model of an employee, an employee has a list of attributes:

- ID number: This could be the working ID, students ID, license ID, but should be unique.
- Index: This is an internal index associated with the user, it is used in a large set of commands to refer to a given user, a 'common user' doesn't have knowledge of this number, and it may be different across different devices.
- Name: The name of the employee, this is optional.
- Permissions: This sets the level of actions that a user may perform, regular employees are 'common users' while the IT admins may be 'superadmins'.
- Enable flag: A user may be enabled or disabled.
- Card number: This corresponds to the number of an RFID card, this depends on the verify style.
- Password: Password to access, this depends on the verify style.
- Group: New users are by default on group 1, but there may be 100 different groups, a user can only belong to one group, they could inherit permissions and settings from the group to which they belong, at the same time a group may have 3 timezones and a verify style.
- Timezones: A user may have a maximum of three timezones.
- Verify style: Sets the way the user verifies on the machine, e.g. use password and fingerprint, use RFID card or fingerprint.

### Granting Access ###

To grant access there are several tweaks, timezones, group unlock combinations, multiple verification modes and holidays, there is also a daylight correction but this it isn't considered.

To open door a user must comply with the following conditions:

- Perform a successful verification.
- Be in a valid timezone.
- Be in a a group for a valid unlock combination.

#### Verify Style ####

There are several combinations of allowed verification modes, they are all a subset of the following three options:

- Fingerprint.
- RFID card.
- Password.
- User ID.

A user may have a personal verify style or could inherit the verify style from the group.

To open a door, the user must perform a successful verification, this is a necessary condition but not sufficient.

#### Timezones ####

A timezone is just a definition of allowed hours to validate:

	(Start time):(End time)

For each timezone there is one definition for each day of the week, i.e. per timezone we have 7 time intervals.

Each device may store a maximum of 50 timezones.

To open a door is needed that at least one of the timezones is satisfied, that means that the timezones are **ORed**.

It is possible to make a user use the timezones of the group instead of his own timezones.

A user is commonly assigned only one timezone.

### Unlock Combination ###

To open a door the user should be in a valid unlock combination, per device there may be 10 unlock combinations, each unlock combination may consist of a maximum of 5 groups:

	combination N: G1 G2 G3 G4 G5

To open a door at least one unlock combination must be satisfied, that means that users from the 5 different groups G1-G5 must validate at the same time to unlock the door. Though it isn't required that the 5 groups must be defined, so a common unlock combination is:

	01: 01 00 00 00 00

This combination indicates that only is needed a user from the group 1 to satisfy the unlock combination 1.


## Protocol Overview ##

The **standalone** terminals are called in that way because they may be used without any communication with a "coordinator" or "manager", like the ZKAccess software, so the protocol is designed to:

- **Get info from terminals**: Attendance records, new users(added in the standalone terminal), device model, status, etc. The ZKAccess program, builds a database with this info.
- **Set info in terminals**: Add users, change access permissions, change device settings, etc.
- **Report events**: Corresponds to packets sent from the machine when specific actions take place, they are sent without a previous request.

Note that the communication can be done through:

- TCP/IP
- UDP/IP
- Serial
- USB

Here we consider a communication setup with TCP/IP.

The terminals are considered a **server**, so when attendance records are requested, this is referred as a **download** operation, in a similar way, when data is sent from PC to the device, this is referred as an **upload** operation.

When there are "intensive" operations (i.e. too much changes/transactions) the procedures begin with a disable-device command, this could be to prevent undefined behavior in the device. For small tasks, like get-device-time, there is no need for disabling and enabling the device. Simple tasks usually consist of a request followed by a reply.

Based on the **Standalone SDK** design, a classification of the protocol functions can be made:

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

- **Regular packets**: These are the packets used for normal request and reply procedures.
- **Realtime packets**: These are the packets used to report events, the are sent by the machine without a previous reply, if a connection exists.

Both type of packets follow this general structure.

**Example of a regular type packet**:

	00000000: 50 50 82 7D 0C 00 00 00  0B 00 58 EF C5 C0 05 00  PP.}......X.....
	00000010: 7E 4F 53 00

In this example the size of the payload is:

	0x000C

And the payload is:

	0B 00 58 EF C5 C0 05 00 7E 4F 53 00

### Regular Packet Payload ###

For regular packets the payload can be decomposed in the following fields:

|Name		|Description					|Value[hex]	|Size[bytes]	|Offset	|Overall Offset	|
|---		|---						|---		|---		|---	|---		|
|command id	|Command identifier/Reply code.			|varies(<)	|2		|0	|8		|
|checksum	|Checksum.					|varies(<)	|2		|2	|10		|
|session id	|Session id.					|varies(<)	|2		|4	|12		|
|reply number	|Reply number.					|varies(<)	|2		|6	|14		|
|data		|Specific data for the given command/reply.	|varies		|payload_size-8	|8	|16		|

(<): Little endian format.

**Example**:

Following the previous example

	0B 00 58 EF C5 C0 05 00 7E 4F 53 00

The fields contents are:

|Name		|Value[hex]	|
|---		|---		|
|command id	|000B		|
|checksum	|EF58		|
|session id	|C0C5		|
|reply number	|0005		|
|data		|[ 7E 4F 53 00 ]|

#### Command/Reply Identifiers ####

The command/reply id field may be used for two purposes:

1. Instruct the machine to do something.
2. Return an exit code from a given procedure.
3. Report events (corresponds to `CMD_REG_EVENT`).

The command id correspondence is given in the following table:

|Name			|Description						|Value[base10]	|Value[hex]	|
|---			|---							|---		|---		|
|CMD_CONNECT		|Begin connection.					|1000		|03e8		|
|CMD_EXIT		|Disconnect.						|1001		|03e9		|
|CMD_ENABLEDEVICE	|Change machine state to "normal work".			|1002		|03ea		|
|CMD_DISABLEDEVICE	|Disables fingerprint, rfid reader and keyboard.	|1003		|03eb		|
|CMD_RESTART		|Restart machine.					|1004		|03ec		|
|CMD_POWEROFF		|Shut-down machine.					|1005		|03ed		|
|CMD_SLEEP		|Change machine state to "idle".			|1006		|03ee		|
|CMD_RESUME		|Change machine state to "awaken".			|1007		|03ef		|
|CMD_CAPTUREFINGER	|Capture fingerprint picture.				|1009		|03f1		|
|CMD_TEST_TEMP		|Test if fingerprint exists.				|1011		|03f3		|
|CMD_CAPTUREIMAGE	|Capture the entire image.				|1012		|03f4		|
|CMD_REFRESHDATA	|Refresh the machine stored data.			|1013		|03f5		|
|CMD_REFRESHOPTION	|Refresh the configuration parameters.			|1014		|03f6		|
|CMD_TESTVOICE		|Test voice.						|1017		|03f9		|
|CMD_GET_VERSION	|Request the firmware edition.				|1100		|044c		|
|CMD_CHANGE_SPEED	|Change transmission speed.				|1101		|044d		|
|CMD_AUTH		|Request to begin session using commkey.		|1102		|044e		|
|CMD_PREPARE_DATA	|Prepare for data transmission.				|1500		|05dc		|
|CMD_DATA		|Data packet.						|1501		|05dd		|
|CMD_FREE_DATA		|Release buffer used for data transmission.		|1502		|05de		|
|CMD_DATA_WRRQ		|Read/Write a large data set.				|1503		|05df		|
|CMD_DATA_RDY		|Indicates that it is ready to receive data.		|1504		|05e0		|
|CMD_DB_RRQ		|Read saved data.					|7		|0007		|
|CMD_USER_WRQ		|Upload user data.					|8		|0008		|
|CMD_USERTEMP_RRQ	|Read user fingerprint template.			|9		|0009		|
|CMD_USERTEMP_WRQ	|Upload user fingerprint template.			|10		|000a		|
|CMD_OPTIONS_RRQ	|Read configuration value of the machine.		|11		|000b		|
|CMD_OPTIONS_WRQ	|Change configuration value of the machine.		|12		|000c		|
|CMD_ATTLOG_RRQ		|Request attendance log.				|13		|000d		|
|CMD_CLEAR_DATA		|Delete data.						|14		|000e		|
|CMD_CLEAR_ATTLOG	|Delete attendance record.				|15		|000f		|
|CMD_DELETE_USER	|Delete user.						|18		|0012		|
|CMD_DELETE_USERTEMP	|Delete user fingerprint template.			|19		|0013		|
|CMD_CLEAR_ADMIN	|Clears admins privileges.				|20		|0014		|
|CMD_USERGRP_RRQ	|Read user group.					|21		|0015		|
|CMD_USERGRP_WRQ	|Set user group.					|22		|0016		|
|CMD_USERTZ_RRQ		|Get user timezones.					|23		|0017		|
|CMD_USERTZ_WRQ		|Set the user timezones.				|24		|0018		|
|CMD_GRPTZ_RRQ		|Get group timezone.					|25		|0019		|
|CMD_GRPTZ_WRQ		|Set group timezone.					|26		|001a		|
|CMD_TZ_RRQ		|Get device timezones.					|27		|001b		|
|CMD_TZ_WRQ		|Set device timezones.					|28		|001c		|
|CMD_ULG_RRQ		|Get group combination to unlock.			|29		|001d		|
|CMD_ULG_WRQ		|Set group combination to unlock.			|30		|001e		|
|CMD_UNLOCK		|Unlock door for a specified amount of time.		|31		|001f		|
|CMD_CLEAR_ACC		|Restore access control to default.			|32		|0020		|
|CMD_CLEAR_OPLOG	|Delete operations log.					|33		|0021		|
|CMD_OPLOG_RRQ		|Read operations log.					|34		|0022		|
|CMD_GET_FREE_SIZES	|Request machine status (remaining space).		|50		|0032		|
|CMD_ENABLE_CLOCK	|Enables the ":" in screen clock.			|57		|0039		|
|CMD_STARTVERIFY	|Set the machine to authentication state.		|60		|003c		|
|CMD_STARTENROLL	|Start enroll procedure.				|61		|003d		|
|CMD_CANCELCAPTURE	|Disable normal authentication of users.		|62		|003e		|
|CMD_STATE_RRQ		|Query state.						|64		|0040		|
|CMD_WRITE_LCD		|Prints chars to the device screen.			|66		|0042		|
|CMD_CLEAR_LCD		|Clear screen captions.					|67		|0043		|
|CMD_GET_PINWIDTH	|Request max size for users id.				|69		|0045		|
|CMD_SMS_WRQ		|Upload short message.					|70		|0046		|
|CMD_SMS_RRQ		|Download short message.				|71		|0047		|
|CMD_DELETE_SMS		|Delete short message.					|72		|0048		|
|CMD_UDATA_WRQ		|Set user short message.				|73		|0049		|
|CMD_DELETE_UDATA	|Delete user short message.				|74		|004a		|
|CMD_DOORSTATE_RRQ	|Get door state.					|75		|004b		|
|CMD_WRITE_MIFARE	|Write data to Mifare card.				|76		|004c		|
|CMD_EMPTY_MIFARE	|Clear Mifare card.					|78		|004e		|
|CMD_VERIFY_WRQ		|Change verification style of a given user.		|79		|004f		|
|CMD_VERIFY_RRQ		|Read verification style of a given user.		|80		|0050		|
|CMD_TMP_WRITE		|Transfer fp template from buffer.			|87		|0057		|
|CMD_CHECKSUM_BUFFER	|Get checksum of machine's buffer.			|119		|0077		|
|CMD_DEL_FPTMP		|Deletes fingerprint template.				|134		|0086		|
|CMD_GET_TIME		|Request machine time.					|201		|00c9		|
|CMD_SET_TIME		|Set machine time.					|202		|00ca		|
|CMD_REG_EVENT		|Realtime events.					|500		|01f4		|

See the codification of reply codes in the following table:

|Name			|Description						|Value[base10]	|Value[hex]	|
|---			|---							|---		|---		|
|CMD_ACK_OK		|The request was processed sucessfully.			|2000		|07d0		|
|CMD_ACK_ERROR		|There was an error when processing the request.	|2001		|07d1		|
|CMD_ACK_DATA		|							|2002		|07d2		|
|CMD_ACK_RETRY		|							|2003		|07d3		|
|CMD_ACK_REPEAT		|							|2004		|07d4		|
|CMD_ACK_UNAUTH		|Connection not authorized.				|2005		|07d5		|
|CMD_ACK_UNKNOWN	|Received unknown command.				|65535		|ffff		|
|CMD_ACK_ERROR_CMD	|							|65533		|fffd		|
|CMD_ACK_ERROR_INIT	|							|65532		|fffc		|
|CMD_ACK_ERROR_DATA	|							|65531		|fffb		|

#### Checksum ####

The calculated checksum is a 16 bit integer, but it is calculated using a 32 bit integer.

To calculate the checksum follow this steps:

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
chk_16b = chk_32b ^ 0xFFFF
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

The session id seems to be just a seconds counter, but the easiest way to get this number is just to extract the number from the device's connect reply.

Also it is worth to note that performing a connection after another connection doesn't change the session id, that means that the session is closed only after closing the connection.

#### Reply Number ####

After a successful socket connection the counter starts from zero counting the number of replies, a command sent to the machine carries this reply counter and this number is the same for a valid reply from the machine. After receiving a reply from the machine, the counter becomes incremented and this new value is sent in the next request.

The reply number should evolve like this:

	> command sent with reply number: 0000
		> reply received with reply numer: 0000
	> command sent with reply number: 0001
		> reply received with reply numer: 0001
	> command sent with reply number: 0002
		> reply received with reply numer: 0002

**Notes**:

- For large amounts of data this flow differs a little, see "Exchange of Data".
- The reply number counter is unaffected for realtime packets.

#### Data ####

The contents of this field depend on the procedure. See Specific Operations sections.

### Realtime Packet Payload ###

For realtime packets the payload differs a little from a regular packet:

- The command id is always `CMD_REG_EVENT`.
- The session id field is used to store the event code.
- The reply number is set to zero.

|Name		|Description				|Value[hex]	|Size[bytes]	|Offset	|Overall Offset	|
|---		|---					|---		|---		|---	|---		|
|command id	|Command identifier/Reply code.		|0xf401(t)	|2		|0	|8		|
|checksum	|Checksum.				|varies(<)	|2		|2	|10		|
|event		|Event code identifier.			|varies(<)	|2		|4	|12		|
|reply number	|Reply number.				|0x0000		|2		|6	|14		|
|data		|Specific data for the given report.	|varies		|payload_size-8	|8	|16		|

(<): Little endian format.
(t): This id corresponds to the command `CMD_REG_EVENT`(0x1f4).

#### Event Codes ####

The following table shows the codification for realtime events:

|Name			|Description				|Value[base10]	|Value[hex]	|
|---			|---					|---		|---		|
|EF_ATTLOG		|Attendance entry.			|1		|1		|
|EF_FINGER		|Pressed finger.			|2		|2		|
|EF_ENROLLUSER		|Enrolled user.				|4		|4		|
|EF_ENROLLFINGER	|Enrolled fingerprint.			|8		|8		|
|EF_BUTTON		|Pressed keyboard key.			|16		|10		|
|EF_UNLOCK		|					|32		|20		|
|EF_VERIFY		|Registered user placed finger.		|128		|80		|
|EF_FPFTR		|Fingerprint score in enroll procedure.	|256		|100		|
|EF_ALARM		|Triggered alarm.			|512		|200		|

#### Example ####

This is an example of a realtime packet:

	00000000: 50 50 82 7D 09 00 00 00  F4 01 A7 FC 00 01 00 00  PP.}............
	00000010: 64

This corresponds to a `EF_FPFTR`(`0x0100`) event, and the data field of the payload has only one byte `0x64`.

Note how the `session id` field is reused and the reply number is set to zero.

## Specific Operations ##

### Conventions ###

In the following descriptions some conventions are used to make protocol descriptions simple but precise.

#### Regular Packet Creation ####

The packet formation process was presented in previous sections, the notation:

	packet(id=<command/reply code>, data=<payload data>)

Is a compact form to refer to a packet with the format:

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

Sometimes is helpful to explicitly show the values of the other packet fields, to indicate this we use the notation:

	packet(id=<command/reply code>, data=<payload data>, <field>=<value>)

Example:

	packet(id=CMD_ACK_OK, session id=0000)

**Notes**:

- When data parameter is absent, an empty `payload data` field should be assumed.

- Also, in this notation the hex codes are given in big endian (as seen on the table of command codes), and the data could be given as a textual description or as a sequence of hex numbers.

- Some type of commands have a data field that it is always terminated in `0x00`, this is a way to indicate the end of a string, in this documentation this value is always shown to prevent ambiguity, it may be shown as `00` if it is a sequence of hex numbers or it may be shown as `\x00`, if a string is used.


#### Realtime Packet Creation ####

What differs from a regular packet and a realtime packets are just the `session id` and `reply number` fields. Beyond that the other fields `payload size` and `checksum`, are calculated same as before.

This is summarized with the notation:

	rtpacket(event=<event code>, data=<payload data>)

In this notation the event code is given in big endian (as seen on the table of event codes), and the data could be given as a textual description or as a sequence of hex numbers.

#### Conversations ####

For packet conversations the following notation is used:

	> "message from client to machine"
		> "message from machine to client"
	> "message from client to machine"
		> "message from machine to client"

### Exchange of Data ###

For specific steps used to send/receive large amounts of data, see [ex_data.md](./sections/ex_data.md)

### Terminal Operations ###

For operations related to read/write of machine parameters, see [terminal.md](./sections/terminal.md)

### Data Operations ###

For operations to manage users data, see [data-user.md](./sections/data-user.md).

For operations to manage record data, see [data-record.md](sections/data-record.md)

### Access Operations ###

For operations to manage access settings, see [access.md](./sections/access.md)

### Realtime Operations ###

Realtime events are explained in [realtime.md](./sections/realtime.md)

### Other Operations ###

Other operations can be found in [other.md](./sections/other.md)


## Links and Sources ##

- [Python zklib repo](https://github.com/dnaextrim/python_zklib)
- ZKTeco Inc, *A series of standalone product: Communication protocol manual*
- [Facial & Fingerprint Recognition Product Series User Manual](https://www.zkteco.eu/uploads/downloads/technical_documents/user_manual/IFace-User-Manual.pdf)
- [2.4 inch Color Screen Series User Manual](http://www.zkteco.co.za/manuals/F18_User_Manual.pdf)
***
