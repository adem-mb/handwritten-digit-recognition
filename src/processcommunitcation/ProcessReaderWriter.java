package processcommunitcation;
import java.io.*;
import java.util.ArrayList;
import java.util.regex.Pattern;

public class ProcessReaderWriter {

    private BufferedReader reader;
    private BufferedWriter writer;
    private Subscriber sb;
    public ProcessReaderWriter() {

        Process p = null;
        try {

            ProcessBuilder pb = new ProcessBuilder("python", ProcessReaderWriter
                    .class.getResource("../pythonFiles/use.py").getPath().substring(1).replaceAll("%20"," "));
            p=pb.start();
            final BufferedReader error=new BufferedReader(new InputStreamReader(p.getErrorStream()));
            Thread errorThread = new Thread(new Runnable() {
                @Override
                public void run() {
                    String line;
                    try {
                        while ((line = error.readLine()) != null) {

                            System.err.println(line);
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            });
            errorThread.start();
            reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
            writer = new BufferedWriter(new OutputStreamWriter(p.getOutputStream()));
            Thread readerThread = new Thread(new Runnable() {
                @Override
                public void run() {
                    notifySubscriber();
                }
            });
            readerThread.start();
            System.out.println(p.isAlive());
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
    public void setSubscriber(Subscriber sb){
        this.sb=sb;

    }
    private void notifySubscriber() {

        String line;
        try {
            while ((line = reader.readLine()) != null) {

                sb.newMessage(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public void getPrediction(String img) {
        try {
            writer.write(img+"\n");
            writer.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


}
