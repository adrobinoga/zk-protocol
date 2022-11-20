    /**
    *    I'm not in the mood to explain the code.
    **/
    public static int calculateChecksum32(byte[] buffer) {
        int ecx = 0; // sum
        for (byte b : buffer) {
            int eax = Byte.toUnsignedInt(b);
            ecx <<= 4;
            ecx += eax;
            eax = ecx & 0xF0000000;
            if (eax != 0) {
                int edi = eax;
                edi >>= 0x18;
                edi = edi ^ eax;
                ecx = ecx ^ edi;
            }
        }
        long rax = Integer.toUnsignedLong(ecx) * 1077952641L;
        int edx = (int) (rax >>> 32);
        edx = (edx >>> 0x16) * 0x00FEFFFF;
        return ecx - edx;
    }
