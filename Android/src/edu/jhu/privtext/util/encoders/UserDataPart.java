/*
 * Copyright (c) 2011 Gary Belvin
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package edu.jhu.privtext.util.encoders;

import java.nio.ByteBuffer;

/**
 * Encapsulates a raw PDU.
 * 
 * @author Gary Belvin
 * @version 0.1
 * 
 * <pre>
   7 6 5 4 3 2 1 0 
   +-+-+-+-+-+-+-+
9  |   SEQ       |
   +-+-+-+-+-+-+-+<-+
10 |  Data Len   |  |
11 |  payload    |  +-- 127 Encrypted octets
.. |    " "      |  |   126 Octet maximum payload size
   | 0x00 Padding|  |
   +-+-+-+-+-+-+-+<-+   
137|             |
138|   24 bit    |
139|    MAC      |
   +-+-+-+-+-+-+-+
   </pre>
 */
public class UserDataPart {
  /** The size of the sequence number in bytes. */
  public static final int SEQ_SIZE = 1;

  /** The maximum size of the total PDU in bytes. */
  public static final int MAX_PDU_SIZE = 140;
  
  /** The size of the UserDataHeader in bytes. */
  protected static final int UDH_SIZE = UserDataHeader.UHDL + 1;

  /**
   * The size of the mac tag in bytes. If field scenarios detect that the MACs
   * are being attacked, this value can be increased to 32 or 48 bits.
   */
  private static final int DefaultMAC_size = (24 / 8);
  /** The number of bytes in the MAC. */
  private final int my_macbytes;
  /** The maximum size of the encrypted data payload - excluding the mac. */
  private final int my_maxencryptedbytes;
  /** the maximum size of the authenticated and encrypted payload. */
  private final int my_maxpayloadbytes; 

  /** The user data header. */
  private UserDataHeader my_userdataheader;
  /** The Sequence Number. */
  private byte my_sequencenum;
  /** The Authenticated Encryption payload. */
  private byte[] my_aepayload;

  /** Create a UserDataPart using the default MAC size. */
  public UserDataPart() {
    this(DefaultMAC_size);
  }

  /**
   * Create the user data part with a custom MAC size.
   * 
   * @param the_macsize in bytes
   */
  public UserDataPart(final int the_macsize) {
    assert the_macsize >= 2 && the_macsize <= (MAX_PDU_SIZE - SEQ_SIZE);
    my_maxencryptedbytes = MAX_PDU_SIZE - (UDH_SIZE + SEQ_SIZE + the_macsize);
    my_maxpayloadbytes = MAX_PDU_SIZE - (UDH_SIZE + SEQ_SIZE);

    my_macbytes = the_macsize;
    my_sequencenum = 0;
    my_userdataheader = new UserDataHeader((short)0, (short)0);
    my_aepayload = new byte[my_maxpayloadbytes];
  }

  /**
   * Breaks down a PDU into its parts according to this format.
   * 
   * @param the_userdata of the message to be parsed
   * @param the_macsize of the message to be parsed
   */
  public UserDataPart(final byte[] the_userdata, final UserDataHeader the_udh, final int the_macsize) {
    this(the_macsize);
    my_userdataheader = the_udh;
    parse(the_userdata);
  }

  /**
   * Create a custom UserDataPart with the default MAC size.
   * 
   * @param the_seq Sequence number
   * @param the_ciphertext Authenticated Ciphertext
   */
  public UserDataPart(final byte the_seq, final byte[] the_ciphertext) {
    this(the_seq, the_ciphertext, DefaultMAC_size);
  }

  /**
   * Create a completely custom UserDataPart.
   * 
   * @param the_seq sequence number
   * @param the_ciphertext authenticated ciphertext
   * @param the_macsize custom message authentication code size in bytes
   */
  public UserDataPart(final byte the_seq, final byte[] the_ciphertext,
                      final int the_macsize) {
    this(the_macsize);
    setSequenceNumber(the_seq);
    setEncryptedPayload(the_ciphertext);
  }

  /** Extracts the appropriate fields from the payload. */
  private void parse(final byte[] the_payload) {
    
    my_sequencenum = 0;
    final int ctextlen = the_payload.length - SEQ_SIZE;
    my_aepayload = new byte[ctextlen];

    
    my_sequencenum = the_payload[0];
    System.arraycopy(the_payload, SEQ_SIZE, my_aepayload, 0, ctextlen);
  }

  /** @return everything past the User Data Header */
  public byte[] getUserData() {
    final int len = SEQ_SIZE + my_maxencryptedbytes + my_macbytes;
    final ByteBuffer ud = ByteBuffer.allocate(len);
    ud.put(my_sequencenum);
    ud.put(my_aepayload);

    return ud.array();
  }

  public int getMaxPayloadSize() {
    return my_maxencryptedbytes;
  }

  public byte[] getUserDataHeader() {
    return my_userdataheader.getUDH();
  }

  public void setUserDataHeader(final UserDataHeader myUserDataHeader) {
    this.my_userdataheader = myUserDataHeader;
  }

  /** @return the sequence number of the message. */
  public byte getSequenceNumber() {
    return my_sequencenum;
  }

  /** Set the sequence number.
   * @param the_sequencenum the sequence number.
   */
  public void setSequenceNumber(final byte the_sequencenum) {
    this.my_sequencenum = the_sequencenum;
  }

  public byte[] getEncryptedPayload() {
    return my_aepayload;
  }

  public void setEncryptedPayload(final byte[] theEncryptedPayload) {
    assert (theEncryptedPayload.length + UDH_SIZE + SEQ_SIZE) <= MAX_PDU_SIZE;
    this.my_aepayload = theEncryptedPayload;
  }
  
  /**
   * @return The size of the mac in bits.
   */
  public int getMacBits() {
    return my_macbytes * Byte.SIZE;
  }
  
  /** @return the size of the mac in bytes. */
  public int getMacBytes() {
    return my_macbytes;
  }
}
