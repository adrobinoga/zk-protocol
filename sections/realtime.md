# Realtime Operations #

[Go to Main Page](../protocol.md)

Author: Alexander Marin <alexanderm2230@gmail.com>

[TOC]

## Current Descriptions ##

|SDK function name	|Described(X=Yes, O=No)	|Notes|
|---			|:---:			|---|
|OnConnected		|**X**			|This can be easily checked with a device info request.|
|OnDisConnected		|**X**			|This can be easily checked with a device info request.|
|OnAlarm		|**X**			| |
|OnDoor			|**X**			|Some tests need to be performed.|
|OnAttTransaction	|**O**			|Applicable only to BW.|
|OnAttTransactionEx	|**X**			| |
|OnDeleteTemplate	|**O**			|Todo.|
|OnEnrollFinger		|**X**			| |
|OnFinger		|**X**			| |
|OnFingerFeature	|**X**			| |
|OnHIDNum		|**O**			|Todo.|
|OnKeyPress		|**O**			|Todo.|
|OnNewUser		|**O**			|Todo.|
|OnVerify		|**X**			| |
|OnWriteCard		|**O**			|Todo.|
|OnEmptyCard		|**O**			|Todo.|
|OnEMData		|**O**			|Just triggers the event for unknown inputs.|
|RegEvent		|**O**			|Just checks for several events.|
|ReadRTLog		|**O**			|Involves reading PC buffer.|
|GetRTLog		|**O**			|Involves reading PC buffer.|

## Realtime Events ##

The realtime events are useful to monitor the state of doors and to receive alarms of abnormal situations, this type of packets are also used to follow the steps of an enrolling procedure.

To receive this type of packets a connection needs to be setup, so the idea is to connect to all the devices to be monitored and interpret the incoming packets from the machines.

This packets are sent without prior request since they depend on external situations, so the general conversation flow is as follows:

		> rtpacket(event=EF_X, data=<...>, reply number=0000)
	> packet(id=CMD_ACK_OK, session id=<s>, reply number=0000)

Where the `session id` of the client reply is the same as the id set from the connection, and the `reply number` is the same of the incoming packet which for realtime packets, happens to be `0000`.

## Alarm ##

There may be several causes to trigger the alarm, this info is included in the realtime packet sent to the device, the general form of the packet is given by:

	rtpacket(event=EF_ALARM, data=<alarm info>)

Where the `alarm info` structure varies according to the event trigger, if the alarm is triggered.

1.If the alarm is triggered because of an exit button, tamper or misoperation.

The `alarm info` is 4 bytes long and the first byte indicates the alarm type, with the following codification:

|Alarm type	|Value[hex]	|
|---		|---		|
|Misoperation	|3A		|
|Tamper		|37		|
|Exit button	|35		|

2.If the alarm is triggered because the door closes the `alarm info` is 8 bytes long and the first byte indicates the door is closing, with the value `0x54`.

3.For duress alarms the `alarm info` is 12 bytes long

|Name		|Description				|Value[hex]	|Size[bytes]	|Offset		|
|---		|---					|---		|---		|---		|
|		|Fixed.					|ffffffff	|4		|2		|
|alarm type	|Alarm type, indicates cause.		|varies (<)	|2		|4		|
|user sn	|User's serial number.			|varies (<)	|2		|6		|
|match type	|Matching way.				|varies (<)	|4		|8		|

(<): Little endian format.

The alarm type indicates what triggered the alarm:

|Alarm type	|Value[hex]	|
|---		|---		|
|Duress		|20		|
|Passback	|22		|

## On Door ##

See Alarm section.

## Attendace Realtime Log ##

|Name		|Description				|Value[hex]	|Size[bytes]	|Offset		|
|---		|---					|---		|---		|---		|
|user id	|User's pin number, stores as a string.	|varies		|9		|0		|
|		|Fixed.					|zeros		|15		|9		|
|		|Unknown.				|?		|2		|24		|
|att time	|Time given in a special format.	|H0H1H2H3H4H5	|6		|26		|

The time of attendance is given with a special format, if the time is given by the sequence:

	H0 H1 H2 H3 H4 H5

The date 

	YEAR/MONTH/DAY HOURS:MINUTES:SECONDS

can be obtained by extracting each byte as a number, and entering them in the following formula:

	date = 20<H0>/<H1>/<H2> <H3>:<H4>:<H5>

## Enrolled Finger ##

After a fingerprint enrolling process the machine returns a structure indicating how the procedure went, it returns a structure with the following fields:

|Name		|Description				|Value[hex]	|Size[bytes]	|Offset		|
|---		|---					|---		|---		|---		|
|result		|Error code.				|varies (<)	|2		|0		|
|fp size	|Size of fingerprint template.		|varies (<)	|2		|2		|
|user id	|User's pin number, stores as a string.	|varies		|9		|4		|
|finger index	|Finger index of the template.		|varies		|1		|13		|

If the error code is equal to zero, that means the there was no error in the enrolling process, if the value is not zero that means that there was an error, in that case the fields `fp size`, `user id` and `finger index`, aren't even included.

## On Finger ##

After someone puts finger on the machine reader, the machine sends a packet to indicate this, this happens in the enroll procedure and also in normal operation, whether the user is registered or not.

The packet doesn't carry any additional data.

	rtpacket(event=EF_FINGER)

## Finger Score ##

When performing the enrolling procedure, the machine sends a packet after a fingerprint sample to indicate the score of the given sample:

	rtpacket(event=EF_FPFTR, data=<score>)

The score is measure of the "quality" of the fingerprint sample, the value is given as number, that may be 0 or 100(0x64).

## On Verify ##

When someone tries to perform verification, the machine sends a packet to the client with the serial number of the user.

	rtpacket(event=EF_VERIFY, data=<verify info>)

The returned structure has the following fields:

|Name		|Description				|Value[hex]	|Size[bytes]	|Offset		|
|---		|---					|---		|---		|---		|
|user sn	|User's serial number.			|varies (<)	|4		|0		|
|		|Fixed.					|01		|1		|4		|

If the user can't be identified, then the user sn equals to `ffffffff`

[Go to Main Page](../protocol.md)
