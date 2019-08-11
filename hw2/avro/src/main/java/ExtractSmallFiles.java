import file.avro.CmpFile;

import org.apache.avro.file.DataFileReader;
import org.apache.avro.io.DatumReader;
import org.apache.avro.specific.SpecificDatumReader;
import org.apache.commons.codec.digest.DigestUtils;

import java.io.*;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;

public class ExtractSmallFiles {
    public ExtractSmallFiles() {}
    public void extract(String path, String gen_dir) {
        try {
            File dir = new File(gen_dir);
            if (!dir.mkdir()) {
                System.err.println("dir already exists");
            }
            DatumReader<CmpFile> fileDatumReader = new SpecificDatumReader<>(CmpFile.class);
            DataFileReader<CmpFile> dataFileReader = new DataFileReader<>(new File(path), fileDatumReader);
            CmpFile cf = null;

            while (dataFileReader.hasNext()) {
                cf = dataFileReader.next();
//                System.out.println(cf);  // output data in JSON format

                // why cannot Bytebuffer.array() to construct a new String?
//                System.out.println(cf.getFilecontent());
                String content = myString(cf.getFilecontent());
                String sha = DigestUtils.shaHex(content);
                if (!cf.getChecksum().toString().equals(sha))
                    System.err.println(cf.getFilename().toString() + " content inconsistent!");

                File file = new File(gen_dir + "/" + cf.getFilename().toString());
                FileWriter writer = new FileWriter(file);
                writer.write(content);
                writer.flush();
                writer.close();
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
            System.exit(-1);
        }
    }

    public static String myString(ByteBuffer buffer)
    {
        try {
            Charset charset = Charset.forName("UTF-8");
            CharsetDecoder decoder = charset.newDecoder();
            // asReadOnlyBuffer() allows multiple use of the ByteBuffer
            CharBuffer charBuffer = decoder.decode(buffer.asReadOnlyBuffer());
            return charBuffer.toString();
        } catch (Exception ex) {
            ex.printStackTrace();
            return "";
        }
    }
}
