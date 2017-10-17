import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class c3256730A3 implements ISubscriber
{
    private int currentTime = 0;

    public static void main(String[]args)
    {
        c3256730A3 intFace = new c3256730A3();
        try { intFace.run(args); } catch (IOException e) { e.printStackTrace(); }
    }


    private void run(String[]args) throws IOException {
        if(args.length == 0)
        {
            throw new IllegalArgumentException("Error. Missing program arguments");
        }

        List<Process> processList = new ArrayList<>();

        for(String filePath: args)
        {
            //Read each process file
            Process process = readProcess(filePath);

            processList.add(process);
        }


        //Start processing
        Memory memory = new Memory(processList.size());
        IPageReplacementAlgorithm lru = new LRU(memory);
        IPageReplacementAlgorithm clockPolicy = new ClockPolicy(memory);
        List<IPageReplacementAlgorithm> pageReplacementAlgorithms = new ArrayList<>();
        pageReplacementAlgorithms.add(lru);
        pageReplacementAlgorithms.add(clockPolicy);

        for(IPageReplacementAlgorithm pageReplacementAlgorithm: pageReplacementAlgorithms)
        {
            memory.setPageReplacementAlgorithm(pageReplacementAlgorithm);
            CPU cpu = new CPU(processList, memory);
            //While each process still has pages in its page list, keep going
            boolean keepGoing = true;
            while(cpu.hasQueuedProcesses())
            {
                cpu.cycle();
            }
        }

        //todo for each loop with two different cpus running two different page replacement algorithms
        CPU cpu = new CPU(processList);


        cpu.cycle();

    }
    private Process readProcess(String filePath) throws IOException {
        try(BufferedReader reader = new BufferedReader(new FileReader(filePath)))
        {
            String fileContents = readWholeFile(reader);

            // Once file has been fully read, convert the string builder into a standard java string
            return readProcessDataFromString(fileContents ,filePath);
        }
        catch(Exception e)
        {
            throw e;
        }

    }



    /**
     * public String readWholeFile(Buffered reader)
     * Reads the entire input data file and appends to a stringbuilder
     * @param reader - BufferedReader used to read the file
     * @return - String containing all data
     */
    private String readWholeFile(BufferedReader reader) throws IOException
    {
        //Modified from http://abhinandanmk.blogspot.com.au/2012/05/java-how-to-read-complete-text-file.html
        String line = null;
        StringBuilder sb = new StringBuilder();
        while((line = reader.readLine()) != null)
        {
            sb.append(line).append("\n");
        }

        return sb.toString();
    }

    private Process readProcessDataFromString(String fileContents, String filePath)
    {
        String processName = filePath.replaceAll(".txt","")
                                    .replaceAll(".dat","")
                                    .replaceAll("process","")
                                    .replaceAll("Process","");

        int processId = Integer.parseInt(processName);

        //Cleanse line to remove \n, \t characters and make the any glyphs toUpperCase
        String cleansedData = cleanseProcessData(fileContents);

        String[] individualPages = cleansedData.split(" ");

        List<Page> pageList = new ArrayList<>();

        for(String page: individualPages)
        {
            int pageId = Integer.parseInt(page);
            pageList.add(new Page(pageId));
        }

        Process process = new Process(processId, pageList);

        return process;
    }

    private String cleanseProcessData(String fileContents)
    {
        String cleansed = fileContents.replaceAll("begin\n", "")
                                    .replaceAll("end\n", "")
                                    .replaceAll("\r\n", " ") // \r\n is windows version of new line
                                    .replaceAll("\n", " ")
                                    .replaceAll("\t", " ");



        while (cleansed.contains("  "))
            cleansed = cleansed.replace("  ", " ");

        return cleansed;
    }


    @Override
    public void handleMessage(ObservableMessage message) {
        if(message instanceof ObservableCPUTimeMessage)
        {
            currentTime = ((ObservableCPUTimeMessage) message).getCpuTimeTick();

        }
    }
}
