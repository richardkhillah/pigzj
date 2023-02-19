import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.zip.Deflater;

/**
 * RFC1952 spec for Zip Compression member
 * 
 * https://www.rfc-editor.org/rfc/rfc1952
 * 
 * Each Zip member has the following structure:
 * 
 * Units:
         +---+
         |   | <-- the vertical bars might be missing
         +---+

      represents one byte; a box like this:

         +==============+
         |              |
         +==============+

      represents a variable number of bytes.



         +---+---+---+---+---+---+---+---+---+---+
         |ID1|ID2|CM |FLG|     MTIME     |XFL|OS | (more-->)
         +---+---+---+---+---+---+---+---+---+---+

      (if FLG.FEXTRA set)

         +---+---+=================================+
         | XLEN  |...XLEN bytes of "extra field"...| (more-->)
         +---+---+=================================+

      (if FLG.FNAME set)

         +=========================================+
         |...original file name, zero-terminated...| (more-->)
         +=========================================+

      (if FLG.FCOMMENT set)

         +===================================+
         |...file comment, zero-terminated...| (more-->)
         +===================================+

      (if FLG.FHCRC set)

         +---+---+
         | CRC16 |
         +---+---+

         +=======================+
         |...compressed blocks...| (more-->)
         +=======================+

           0   1   2   3   4   5   6   7
         +---+---+---+---+---+---+---+---+
         |     CRC32     |     ISIZE     |
         +---+---+---+---+---+---+---+---+
 */
public class ZipMember {
    public final static int TRAILER_SIZE = 8; // 8 byte

    // Header flags
    private final static byte ID1 = (byte)0x1f;
    private final static byte ID2 = (byte)0x8b;
    private final static byte CM = Deflater.DEFLATED;
    private final static byte FLG = 0;
   //  private final static int MTIME = 0;
   //  private final static byte XFL = 0;
    private final static byte OS = (byte)0xff;

    private ZipMember(){}

    /**
     * Called from WriteTask. Create a gzip header byte[].
     * 
     * Since this program reads stdin, no Filename or Comments will
     * be accomodated.
     * 
     * @param compressionLevel The compression level set in ZipConfiguration
     * @param modificationTime The modification time
     * @return
     * @throws IOException
     */
    public static byte[] makeHeader(int compressionLevel, 
                                    long modificationTime) throws IOException {
        int mod_time_sec = (int)(modificationTime / 1000); // 4-byte MTime
        byte comp_level = compressionLevel == Deflater.BEST_COMPRESSION ? (byte)2 :
                (compressionLevel == Deflater.BEST_SPEED
                    ||compressionLevel==Deflater.NO_COMPRESSION ? (byte)4 : 0);
        
        ByteBuffer header = ByteBuffer.allocate(10);
                            // ID's identify the file as being in gzip format
        header.put(ID1);    // fixed value 31 (0x1f, \037)
        header.put(ID2);    // fixed value 139 (0x8b, \213)
        header.put(CM);     // Compression Method
        header.put(FLG);    // Flags
        header.putInt(mod_time_sec);    // 4-byte Modification Time
        header.put(comp_level);         // Extra Flags
        header.put(OS);     // Operating System
        // This Header will not have filename or comments
        return header.array();
    }

    /**
     * Called from WriteTask. Create a gzip trailer.
     * 
     * @param crc32
     * @param uncompressedBytes
     * @return
     */
    public static byte[] makeTrailer(int crc32, int uncompressedBytes) {
        ByteBuffer trailer = ByteBuffer.allocate(TRAILER_SIZE);

        byte[] c = new byte[4];
        writeInt(crc32, c, 0);
        trailer.put(c);

        byte[] uc = new byte[4];
        writeInt(uncompressedBytes, uc, 0);
        trailer.put(uc);

        return trailer.array();
    }

    /**
     * Helper to convert a signed int into unsigned byte representation
     * @param i      // into to convert
     * @param buf    // byte buffer to write converted byte into
     * @param offset
     */
    private static void writeInt(int i, byte[] buf, int offset) {
      writeShort(i & 0xffff, buf, offset);            // write lower
      writeShort((i >> 16) & 0xffff, buf, offset + 2);// write upper
    }

    /**
     * 
     * @param s
     * @param buf
     * @param offset
     */
    private static void writeShort(int s, byte[] buf, int offset) {
      buf[offset] = (byte)(s & 0xff);       // write lower byte
      buf[offset+1] = (byte)(s >> 8 & 0xff);  // write upper byte
    }
}
