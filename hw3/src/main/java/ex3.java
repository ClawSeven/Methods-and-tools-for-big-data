import file.avro.Avfile;
import org.apache.avro.Schema;
import org.apache.avro.generic.GenericRecord;
import org.apache.avro.generic.GenericData;
import org.apache.avro.mapred.AvroKey;
import org.apache.avro.mapreduce.AvroJob;
import org.apache.avro.mapreduce.AvroKeyInputFormat;
import org.apache.avro.mapreduce.AvroKeyValueOutputFormat;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;
import org.apache.hadoop.util.bloom.BloomFilter;
import org.apache.hadoop.util.bloom.Key;
import org.apache.hadoop.util.hash.Hash;
import java.io.*;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;

public class ex3 extends Configured implements Tool {
    private static final String out_schema="{\"type\":\"record\",\"name\":\"blooms\",\"fields\":"
            + "[{\"name\":\"bloombytes\",\"type\":\"bytes\"}]}";

    public static class Map extends
            Mapper<AvroKey<Avfile>, NullWritable, NullWritable, BloomFilter> {

        @Override
        public void map(AvroKey<Avfile> key, NullWritable value, Context context) {
            BloomFilter filter =
                    new BloomFilter(1000, 5, Hash.MURMUR_HASH);
            String raw = myString(key.datum().getFilecontent());

            String[] content = raw.split("\n");
            Text sid = new Text();
//            IntWritable grade = new IntWritable();

            for (String s: content) {
                String[] record = s.split(",");
                if (record.length != 3)
                    return;
                sid.set(record[1]);
                filter.add(new Key(sid.getBytes()));
//                int num = Integer.parseInt(record[2]);
//                byte[] tmp = record[1].substring(record[1].length()-1).getBytes();
//                grade.set(num);
//                sid.set(record[1]);
//                context.write(sid, grade);
            }
        }
    }

    public static class Reduce extends
            Reducer<NullWritable,BloomFilter,AvroKey<GenericRecord>,NullWritable> {
        @Override
        public void reduce(NullWritable key, Iterable<BloomFilter> values, Context context) throws IOException, InterruptedException {
            BloomFilter total=new BloomFilter(1000, 5, Hash.MURMUR_HASH);
            for (BloomFilter filter: values) {
                total.or(filter);
            }
            ByteArrayOutputStream b_tmp=new ByteArrayOutputStream();
            DataOutputStream d_tmp=new DataOutputStream(b_tmp);
            total.write(d_tmp);
            d_tmp.close();

            byte[] bytes=b_tmp.toByteArray();
            GenericRecord record=new GenericData.Record(Schema.parse(out_schema));
            record.put("bloombytes",ByteBuffer.wrap(bytes));
            context.write(new AvroKey<>(record) , NullWritable.get());
        }
    }

    private static String myString(ByteBuffer buffer)
    {
        try {
            Charset charset = Charset.forName("UTF-8");
            CharsetDecoder decoder = charset.newDecoder();
            CharBuffer charBuffer = decoder.decode(buffer.asReadOnlyBuffer());
            return charBuffer.toString();
        } catch (Exception ex) {
            ex.printStackTrace();
            return "";
        }
    }

    public int run(final String[] args) throws Exception {
        Job job = new Job(getConf());
        job.setJarByClass(ex3.class);
        job.setJobName("ex3");

        FileInputFormat.setInputPaths(job, new Path(args[0]));
        FileOutputFormat.setOutputPath(job, new Path(args[1]));

        job.setMapperClass(ex3.Map.class);
        job.setReducerClass(ex3.Reduce.class);

        job.setInputFormatClass(AvroKeyInputFormat.class);
        AvroJob.setInputKeySchema(job, Avfile.getClassSchema());

        job.setMapOutputKeyClass(NullWritable.class);
        job.setMapOutputValueClass(BloomFilter.class);

        job.setOutputFormatClass(AvroKeyValueOutputFormat.class);

        job.setMapOutputKeyClass(NullWritable.class);
        job.setMapOutputValueClass(BloomFilter.class);

        boolean result = job.waitForCompletion(true);
        System.out.println("Finished");

        return (result ? 0 : 1);
    }

    public static void main(final String[] args) throws Exception {
        if (args.length != 2) {
            System.err.println("Usage: ex3 <input path> <output path>");
            System.exit(-1);
        }
        int res = ToolRunner.run(new Configuration(), new ex3(), args);
        System.exit(res);
    }
}
