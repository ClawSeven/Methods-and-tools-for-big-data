import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;

import org.apache.avro.Schema;
import org.apache.avro.mapred.AvroKey;
import org.apache.avro.mapred.AvroValue;
import org.apache.avro.mapreduce.AvroJob;
import org.apache.avro.mapreduce.AvroKeyInputFormat;
import org.apache.avro.mapreduce.AvroKeyValueOutputFormat;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;

import file.avro.Avfile;

public class ex2 extends Configured implements Tool {
    public static class Map extends
            Mapper<AvroKey<Avfile>, NullWritable, Text, IntWritable> {
        @Override
        public void map(AvroKey<Avfile> key, NullWritable value, Context context)
                throws IOException, InterruptedException {

            String raw = myString(key.datum().getFilecontent());

            String[] content = raw.split("\n");
            Text sid = new Text();
            IntWritable grade = new IntWritable();

            for (String s: content) {
                String[] record = s.split(",");

                if (record.length != 3)
                    return;
                int num = Integer.parseInt(record[2]);
                grade.set(num);
                sid.set(record[1]);
                context.write(sid, grade);
            }
        }
    }

    public static class Reduce extends
            Reducer<Text, IntWritable, AvroKey<CharSequence>, AvroValue<Integer>> {
        @Override
        public void reduce(Text key, Iterable<IntWritable> values,
                           Context context) throws IOException, InterruptedException {

            int maxValue = Integer.MIN_VALUE;

            for (IntWritable value : values)
                maxValue = Math.max(maxValue, value.get());
            context.write(new AvroKey<>(key.toString()),
                    new AvroValue<>(maxValue));

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

    public int run(String[] args) throws Exception {
        Job job = new Job(getConf());
        job.setJarByClass(ex2.class);
        job.setJobName("ex2");

        FileInputFormat.setInputPaths(job, new Path(args[0]));
        FileOutputFormat.setOutputPath(job, new Path(args[1]));

        job.setMapperClass(ex2.Map.class);
        job.setReducerClass(ex2.Reduce.class);

        job.setInputFormatClass(AvroKeyInputFormat.class);
        AvroJob.setInputKeySchema(job, Avfile.getClassSchema());

        job.setMapOutputKeyClass(Text.class);
        job.setMapOutputValueClass(IntWritable.class);

        job.setOutputFormatClass(AvroKeyValueOutputFormat.class);
        AvroJob.setOutputKeySchema(job, Schema.create(Schema.Type.STRING));
        AvroJob.setOutputValueSchema(job, Schema.create(Schema.Type.INT));

        job.setMapOutputKeyClass(Text.class);
        job.setMapOutputValueClass(IntWritable.class);
        job.setSortComparatorClass(Text.Comparator.class);

        boolean result = job.waitForCompletion(true);
        System.out.println("Finished");

        return (result ? 0 : 1);
    }

    public static void main(String[] args) throws Exception {
        if (args.length != 2) {
            System.err.println("Usage: ex2 <input path> <output path>");
            System.exit(-1);
        }
        int res = ToolRunner.run(new ex2(), args);
        System.exit(res);
    }
}
