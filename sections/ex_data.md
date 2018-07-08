# Exchange of Data #

[Go to Main Page](../protocol.md)

Author: Alexander Marin <alexanderm2230@gmail.com>

[TOC]

## Introduction ##

When sending/receiving, small sets of data (approx. less than 1KB) to/from the machine, using `CMD_DATA_WRRQ` command, the protocol follows a very simple scheme. For a reading operation we have:

	> packet(id=CMD_DISABLEDEVICE)
		> packet(id=CMD_ACK_OK)
	> packet(id=CMD_DATA_WRRQ, data=<data id>)
		> packet(id=CMD_DATA, data=<dataset>)
	> packet(id=CMD_ENABLEDEVICE)
		> packet(id=CMD_ACK_OK)

**Notes**:

- The `dataset` and `data id` fields are specific to each procedure and are described in the corresponding sections.
- Writing of small datasets is performed with specific commands described in the corresponding sections.

For larger sets of data (approx. more than 1KB) a set of additional commands should be used.

## Receiving Large Datasets ##

Before requesting a large dataset a disable command should be sent.

Then a request of data can be done with the command `CMD_DATA_WRRQ`, if the data is large enough the device may decide to not send the data in one command, like in the previous section. If that's the case the device will reply with packet with the code `CMD_ACK_OK` and a data structure.

	> packet(id=CMD_DISABLEDEVICE)
		> packet(id=CMD_ACK_OK)
	> packet(id=CMD_DATA_WRRQ, data=<data id>)
		> packet(id=CMD_ACK_OK, data=<data stat>)

The `data stat` structure contains the size of the data to be sent to the client:

|Name			|Description					|Value[hex]	|Size[bytes]	|Offset	|
|---			|---						|---		|---		|---	|
|			|Fixed.						|00		|1		|0	|
|size dataset (*)	|Size of the dataset to be sent from the device.|varies (<)	|4		|1	|
|size dataset (*)	|""						|varies (<)	|4		|5	|
|unknown		|Unknown value, seems to be a kind of checksum.	|varies		|4		|9	|

(<): Little endian format.

(*): These values show the same value, but for larger datasets we dont know if that would be the case, since it doesn't make sense to have the value duplicated.

After that, send a `CMD_DATA_RDY` command to indicate the device to trasmit the data. This command carries a structure, `rdy struct`, with the following fields:

|Name		|Description					|Value[hex]	|Size[bytes]	|Offset	|
|---		|---						|---		|---		|---	|
|		|Fixed.						|0000		|4		|0	|
|size dataset	|Size of the dataset to be sent from the device.|varies (<)	|4		|4	|

(<): Little endian format.

After that the device would send a packet with the command `CMD_PREPARE_DATA` and a structure, `prep struct`, which has the following fields:

|Name		|Description					|Value[hex]	|Size[bytes]	|Offset	|
|---		|---						|---		|---		|---	|
|size dataset	|Size of the dataset to be sent from the device.|varies (<)	|4		|0	|
|		|Fixed.						|0010		|4		|4	|

(<): Little endian format.

Just after that, without a reply from the client, the machine sends the data with a `CMD_DATA` command, the data field of this packet carries the requested info. Keep in mind that the length of the data field of this packet equals to `size dataset`. After sending the dataset and without waiting for a client reply, the device sends an acknowledge command, without any data.

It is worth to note that the reply number for this sequence of commands(`CMD_PREPARE_DATA`, `CMD_DATA`, `CMD_ACK_OK`) is the same.

The procedure ends with a command to free the buffer of the machine, using `CMD_FREE_DATA` command, and an enable command, to return to normal operation.

	> packet(id=CMD_DATA_RDY, data=<rdy struct>, reply number=<rN>)
		> packet(id=CMD_PREPARE_DATA, data=<prep struct>, reply number=<rN>)
		> packet(id=CMD_DATA, data=<dataset>, reply number=<rN>)
		> packet(id=CMD_ACK_OK, reply number=<rN>)
	> packet(id=CMD_FREE_DATA, reply number=<rN+1>)
		> packet(id=CMD_ACK_OK, reply number=<rN+1>)
	> packet(id=CMD_ENABLEDEVICE, reply number=<rN+2>)
		> packet(id=CMD_ACK_OK, reply number=<rN+2>)

## Sending Large Datasets ##

When using the SDK, the writing operations may be performed individually(i.e. small datasets), each user's info and each template is modified one at a time. However in tipical sync procedures (used in ZKAccess), large datasets are written in one operation, same result could be obtained by using individual write operations, so in this project writing operations of large datasets aren't considered.

[Go to Main Page](../protocol.md)
