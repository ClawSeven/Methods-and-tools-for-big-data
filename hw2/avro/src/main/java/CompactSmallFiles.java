import file.avro.CmpFile;

import java.io.File;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Scanner;

import org.apache.avro.file.CodecFactory;
import org.apache.commons.codec.digest.DigestUtils;

import org.apache.avro.Schema;
import org.apache.avro.file.DataFileWriter;
import org.apache.avro.io.DatumWriter;
import org.apache.avro.specific.SpecificDatumWriter;

public class CompactSmallFiles {
    public CompactSmallFiles() {
        myFiles = new ArrayList<>();
    }

    // read files from path and put file names in myFiles
    public void compact(String schema_path, String path, String des) {
        getFiles(path);
        // after finish operation, clean it?
        try {
            Schema schema = new Schema.Parser().parse(new File(schema_path));
            DatumWriter<CmpFile> fileDatumWriter = new SpecificDatumWriter<>(CmpFile.class);
            DataFileWriter<CmpFile> dataFileWriter = new DataFileWriter<>(fileDatumWriter);

            dataFileWriter.setCodec(CodecFactory.snappyCodec());
            dataFileWriter.create(schema, new File(des));

            for (File f: myFiles) {
                StringBuilder content = new StringBuilder();
                Scanner inputStream = new Scanner(f);
                inputStream.useDelimiter(System.getProperty("line.separator"));
                if (inputStream.hasNext()) {
                    while(true) {
                        content.append(inputStream.next());
                        if (!inputStream.hasNext())
                            break;
                        content.append("\n");
                    }
                }
                String sha = DigestUtils.shaHex(content.toString());
                ByteBuffer byBuffer = ByteBuffer.wrap(content.toString().getBytes());

                CmpFile cfile = CmpFile.newBuilder()
                        .setFilename(f.getName())
                        .setFilecontent(byBuffer)
                        .setChecksum(sha)
                        .build();
                dataFileWriter.append(cfile);
                inputStream.close();
                byBuffer.clear();
            }
            dataFileWriter.close();
        } catch (Exception e) {
            System.err.println(e.getMessage());
//            System.out.println("?!");
        }
        myFiles.clear();
    }

    private void getFiles(String path) {
        File file = new File(path);
        if(file.isDirectory()) {
            File[] files = file.listFiles();
            for(File fileIndex:files) {
                if(!fileIndex.isDirectory()) {
                    myFiles.add(fileIndex);
                } // else getFiles(fileIndex.getPath());
            }
        }
    }

    private ArrayList<File> myFiles;
}
