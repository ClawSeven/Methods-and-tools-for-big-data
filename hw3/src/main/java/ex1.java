import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import java.util.StringTokenizer;
import java.io.IOException;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;

public class ex1 extends Configured implements Tool {
    public static class Map extends Mapper<LongWritable, Text, Text, IntWritable>{
        @Override
        public void map (LongWritable key, Text value, Context context)
                throws IOException, InterruptedException {
            // by default, KEYIN is usually the byte of the start of the line
            // VALUEIN is the content of this line

            IntWritable grade = new IntWritable(1);
            Text sid = new Text();
            StringTokenizer itr = new StringTokenizer(value.toString(), ",");
            itr.nextToken();
            sid.set(itr.nextToken());
            int num = Integer.parseInt(itr.nextToken());
            grade.set(num);
            context.write(sid, grade);
        }
    }

    public static class Reduce extends
            Reducer<Text, IntWritable, Text, IntWritable>{

        @Override
        public void reduce(Text key, Iterable<IntWritable> values, Context context)
                throws IOException, InterruptedException {
            // outputs pairs composed of a student ID and its highest grade.
            // use Iterable<Text> to iterate over all the values of a given key.

            int maxValue = Integer.MIN_VALUE;
            for (IntWritable value : values)
                maxValue = Math.max(maxValue, value.get());
            IntWritable grade = new IntWritable(maxValue);
            context.write(key, grade);
        }

    }

    public int run(String[] args) throws Exception {
        Job job = new Job(getConf());
        job.setJarByClass(ex1.class);
        job.setJobName("ex1");

        job.setMapperClass(ex1.Map.class);
        job.setReducerClass(ex1.Reduce.class);
        job.setOutputKeyClass(Text.class);
        job.setOutputValueClass(IntWritable.class);
        FileInputFormat.addInputPath(job, new Path(args[0]));
        FileOutputFormat.setOutputPath(job, new Path(args[1]));

        boolean result = job.waitForCompletion(true);
        System.out.println("Finished");

        return (result ? 0 : 1);
    }

    public static void main(String[] args) throws Exception {
        if (args.length != 2) {
            System.err.println("Usage: ex3 <input path> <output path>");
            System.exit(-1);
        }
        int res = ToolRunner.run(new ex1(), args);
        System.exit(res);
    }
}
