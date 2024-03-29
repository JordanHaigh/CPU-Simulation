import Algorithms.ClockPolicy;
import Algorithms.IPageReplacementAlgorithm;
import Algorithms.LRU;
import Machine.CPU;
import Machine.Memory;
import Model.Page;
import Model.SchedulingProcess;
import Model.ProcessFileReader;
import ObserverPattern.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


/**
 * Student Number: 3256730 Jordan Haigh
 * COMP2240 A3
 * Main entry of the program
 */
public class c3256730A3 implements ISubscriber
{
    private int currentTime = 0;
    private ProcessFileReader processFileReader = new ProcessFileReader();
    private List<SchedulingProcess> masterSchedulingProcessList = new ArrayList<>();
    private List<IPageReplacementAlgorithm> pageReplacementAlgorithms = new ArrayList<>();
    private CPU cpu;



    public static void main(String[]args)
    {
        c3256730A3 intFace = new c3256730A3();
        try { intFace.run(args); } catch (IOException e) { e.printStackTrace(); }
    }


    private void run(String[]args) throws IOException {
        List<SchedulingProcess> masterSchedulingProcessList = processFileReader.run(args);

        //Start processing
        Memory memory = new Memory(masterSchedulingProcessList.size());

        IPageReplacementAlgorithm lru = new LRU(memory);
        IPageReplacementAlgorithm clockPolicy = new ClockPolicy(memory);
        pageReplacementAlgorithms.add(lru);
        pageReplacementAlgorithms.add(clockPolicy);


        for(IPageReplacementAlgorithm pageReplacementAlgorithm: pageReplacementAlgorithms)
        {
            List<SchedulingProcess> copiedSchedulingProcessList = new ArrayList<>();
            memory.clear(); //Wipe frames and current size of frames
            memory.setPageReplacementAlgorithm(pageReplacementAlgorithm);

            copiedSchedulingProcessList.clear();

            for(SchedulingProcess schedulingProcess : masterSchedulingProcessList)
            {
                copiedSchedulingProcessList.add(new SchedulingProcess(schedulingProcess));
                for(Page page: schedulingProcess.getPageList())
                    page.resetData();
            }
            //Clean data to work with, no old data



            cpu = new CPU(copiedSchedulingProcessList, memory);
            cpu.addSubscriber(this);
            currentTime = 0; //Resets for each page replacement algorithm

            //While each process still has pages in its page list, keep going
            while(cpu.hasQueuedProcesses() || copiedSchedulingProcessList.size() > 0)
            {
                cpu.cycle();
            }

            //individual statistics
            runIndividualStatistics(pageReplacementAlgorithm, copiedSchedulingProcessList);
            System.out.println();
        }

    }

    /**
     * private void runIndividuaStatistics(IPageReplacementAlgorithm, List<SchedulingProcess>)
     * Runs data statistics for a page replacement algorithm
     * @param pageReplacementAlgorithm - Page replacement algorithm
     * @param copiedSchedulingProcessList - Scheduling Process List
     */
    private void runIndividualStatistics(IPageReplacementAlgorithm pageReplacementAlgorithm, List<SchedulingProcess> copiedSchedulingProcessList)
    {
        StringBuilder sb = new StringBuilder();

        sb.append(pageReplacementAlgorithm.toString())
                .append("\n")
                .append(String.format("%-10s", "PID"))
                .append(String.format("%-20s", "Turnaround Time"))
                .append(String.format("%-10s", "# Faults"))
                .append(String.format("%-10s", "Fault Times"));

        List<SchedulingProcess> completedProcessList = cpu.getCompletedProcessList();


        //Using Java Lambdas here
        completedProcessList.sort(SchedulingProcess::compareTo);


        for(SchedulingProcess process: completedProcessList)
        {
            sb.append("\n")
                    .append(String.format("%-10d", process.getId()))
                    .append(String.format("%-20d", process.getTurnaroundTime()))
                    .append(String.format("%-10d", process.getNumberFaultTimes()))
                    .append(String.format("%-10s", process.getFaultTimesToString()));
        }

        System.out.println(sb.toString());
    }

    /**
     * public void handleMessage (ObservableMessage message)
     * Handles message and updates the current time
     * @param message - Message containing the cpu time
     */
    @Override
    public void handleMessage(ObservableMessage message) {
        if(message instanceof ObservableCPUTimeMessage)
        {
            currentTime = ((ObservableCPUTimeMessage) message).getCpuTimeTick();

        }
    }
}
