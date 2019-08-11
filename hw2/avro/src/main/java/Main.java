public class Main {
    static public void main(String[] args) {
        String avro_path = "../large_file.avro";
        String avsc_path = "src/main/avro/file.avsc";
        String file_path = "../gen_csv";
        String file_dir = "../avro_extract";

        CompactSmallFiles ctest  = new CompactSmallFiles();
//        ctest.compact( avsc_path, file_path, avro_path);

        ExtractSmallFiles etest = new ExtractSmallFiles();
        etest.extract(avro_path, file_dir);
    }

}
