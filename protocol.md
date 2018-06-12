# Protocol Description of ZKTeco's Standalone Devices #

Author: Alexander Marin <alexanderm2230@gmail.com>

[TOC]


## Introduction ##

ZKTeco is a security company that provides business solutions to manage the attendance and access of employees to restricted areas.

Their devices may use biometric information like employee's face or fingerprint to grant access, also a PIN or an RFID card may be used.

They provide the [ZKAccess](https://www.zkteco.com/en/product_detail/158.html) software to manage the standalone terminals. And also and [SDK](https://www.zkteco.com/en/download_catgory.html) for standalone devices, to develop custom applications, but both options are targeted to the Windows platform, so in order to use this devices from a Linux system, a library is needed but to develop such lib, we first need the protocol specification, which can be extracted from some ZKTeco's documents and additional analysis of captured packets between Windows software and attendance terminals. There is currently a python package [zklib](https://pypi.org/project/zklib/) to do basic tasks but the documentation is very poor and the library lacks several functions.

**Disclaimer**: The protocol extraction was made with only one device(f19, a TFT series device), so the spec may present some flaws (e.g constant fields that aren't constant).


## Terminology ##

The documentation provided by ZKTeco, sometimes shows acronyms without a prior definition, here we provide a list of useful terms that you may found in this wiki and in other documents.

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

The standalone terminals are called in that way because they may be used without any communication with a "coordinator" or "manager", like the ZKAccess software, so the protocol is designed to:

- Get info from terminals: Attendance records, new users(added in the standalone terminal), device model, status, etc. The ZKAccess program, builds a database with this info.
- Set info in terminals: Add users, change access permissions, change device settings, etc.

Note that the communication can be done through:

- TCP/IP
- Serial
- USB

Here we consider a communication setup with TCP/IP.

The terminals are considered a **server**, so when attendance records are requested, this is referred as a **download** operation, in a similar way, when data is sent from PC to the device, this is referred as an **upload** operation.

When there are "intensive" operations (i.e. too much changes/transactions) the procedures begin with a disable-device command, this could be to prevent undefined behavior in the device. For small tasks, like get-device-time, there is no need for disabling and enabling the device. Simple task usually consist of a request followed by a reply.

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

|Name		|Description			|Value[hex]	|Size[bytes]	|Offset|
|---		|---				|---		|---		|---	|
|start		|Indicates start of packet.	|5050827D	|4		|0	|
|payload size	|Size of packet payload.	|payload_size(<)|2		|4	|
|zeros		|Null bytes.			|0000		|2		|6	|
|payload	|Packet payload.		|varies(<)	|payload_size	|8	|
(<): Little endian format.


### Payload Packet ###

The payload packet can also be decomposed in more fields:

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
|CMD_CONNECT		|Begin connection.			|1000		|3E8		|
|CMD_EXIT		|Disconnect.				|1001		|3E9		|
|CMD_ENABLEDEVICE	|Change machine state to "normal work".	|1002		|3EA		|
|CMD_DISABLEDEVICE	|Change machine state to "busy".	|1003		|3EB		|
|CMD_RESTART		|Restart machine.			|1004		|3EC		|
|CMD_POWEROFF		|Shut-down machine.			|1005		|3ED		|
|CMD_SLEEP		|Change machine state to "idle".	|1006		|3EE		|
|CMD_RESUME		|Change machine state to "awaken".	|1007		|3EF		|
|CMD_CAPTUREFINGER	|Capture fingerprint picture.		|1009		| |
|CMD_TEST_TEMP		|Test if fingerprint exists.		|1011		| |
|CMD_CAPTUREIMAGE	|Capture the entire image.		|1012		| |
|CMD_REFRESHDATA	|Refresh the machine interior data.	|1013		| |
|CMD_REFRESHOPTION	|Refresh the configuration parameter.	|1014		| |
|CMD_TESTVOICE		|Play voice.				|1017		| |
|CMD_GET_VERSION	|Obtain the firmware edition.		|1100		| |



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
chk_32b = (chk_32b & 0xFFFF) + ((chk_32b & 0xFFFF0000)>>16)

# calculate ones complement to get final checksum (in big endian)
chk_16b = hex(chk_32b ^ 0xFFFF)
```

**Test Vectors**

**Note**: The header is not included, these are only payloads.

**1.**
```
0B005A17F38D03005A4B4661636556657273696F6E00
```
After removing the checksum `5A17`.

```
0B00F38D03005A4B4661636556657273696F6E00
```

The calculation of the checksum should give you `175A`.

**2.**

Example packet, with checksum:
```
D007296AF38D0A0009
```

The checksum should give you `6A29`.


#### Session ID ####
The session identifier it is a unique number assigned for every new connection, the machine returns the assigned session id after a connection request.


#### Reply Number ####
After a successful connection the counter starts from zero counting the number of replies, a command sent to the machine carries this reply counter and this number is the same for a valid reply from the machine, this is a way to discard reply packets that do not correspond to the sent packets.


#### Data ####

The contents of this field depend on the procedure command/reply.
See specific sections.


## Terminal Operations ##


## Data Operations ##


## Access Operations ##


## Realtime Operations ##


## Misc Operations ##


## Links and Sources ##

- [Python zklib repo](https://github.com/dnaextrim/python_zklib)
- ZKTeco Inc, *A series of standalone product: Communication protocol manual*
- [Facial & Fingerprint Recognition Product Series User Manual](https://www.zkteco.eu/uploads/downloads/technical_documents/user_manual/IFace-User-Manual.pdf)
- [2.4 inch Color Screen Series User Manual](http://www.zkteco.co.za/manuals/F18_User_Manual.pdf)
***
