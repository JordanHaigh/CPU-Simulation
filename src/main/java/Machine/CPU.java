package Machine;

import Algorithms.ISchedulingAlgorithm;
import Algorithms.RoundRobin;
import Model.Instruction;
import Model.Page;
import Model.SchedulingProcess;
import ObserverPattern.*;

import java.util.ArrayList;
import java.util.List;
/**
 * Student Number: 3256730 Jordan Haigh
 * COMP2240 A3
 * CPU.java is used for executing a process for a period of time.
 * Incorporates a memory module and IO controller for page distribution
 */
public class CPU implements IObservable, ISubscriber {
    private ISchedulingAlgorithm schedulingAlgorithm = new RoundRobin();
    private int currentTime;

    private List<ISubscriber> subscriberList = new ArrayList<>();

    private List<SchedulingProcess> processList = new ArrayList<>();
    
    
    
    private List<SchedulingProcess> completedProcessList = new ArrayList<>();
    private SchedulingProcess currentProcess;

    private Memory memory;
    private IOController ioController;

    /**
     * public CPU(List<SchedulingProcess> processList, Memory memory)
     * Overloaded Constructor
     * @param processList - List of processes
     * @param memory - Memory module
     */
    public CPU(List<SchedulingProcess> processList, Memory memory)
    {
        this.processList = processList;
        this.memory = memory;

        currentTime = 0;

        ioController = new IOController();
        addSubscriber(ioController);


        ioController.addSubscriber(memory);
        memory.addSubscriber(this);

    }

    /**
     * public int get
     * @return
     */
    public int getCurrentTime() {
        return currentTime;
    }
    public boolean hasQueuedProcesses() {return processList.size() > 0; }
    public List<SchedulingProcess> getCompletedProcessList(){return completedProcessList; }

    /**
     * public void performProcessing(Model.SchedulingProcess process, int numberOfCycles)
     * Run the process on the cpu for a specified period of time
     * Once completed, it will be added to a 'Completed Model.SchedulingProcess List' for data statistics
     * If the process does not complete processing within the allocated number of cycles, it is sent to the back
     * of the process list.
     * @param process - Current process that will run on the Machine.CPU
     * @param numberOfCycles - Length of time the process runs for
     */
    public void performProcessing(SchedulingProcess process, int numberOfCycles)
    {
        this.currentProcess = process; //Used in determining if Machine.CPU is running if we need to preempt

        for(int i = 0; i < numberOfCycles; i++)
        {
            Page nextPageFromProcess = process.getNextPageFromList();

            if (!nextPageFromProcess.isLoadedInMemory())
            {
                issuePageFault(nextPageFromProcess);
                //System.out.println("Time  " + currentTime + ": " + nextPageFromProcess.getParentProcess().toString() + ": PAGE("+ nextPageFromProcess.getPageNumber()+") FAULT");
                break;
            }
            else {
                process.run(currentTime);
                //System.out.println("Time  " + currentTime + ": " + nextPageFromProcess.getParentProcess().toString() + ": PAGE("+ nextPageFromProcess.getPageNumber()+") RUNNING");
                updateTimeTick(Instruction.INSTRUCTION_TIME);

            }
        }

        if(process.hasReachedEndOfPageList())
        {
            processList.remove(process);
            completedProcessList.add(process);
        }

        else //In the event of round robin
        {
            processList.remove(process);
            processList.add(process); //Add to back of the process list
        }

    }

    /**
     * public void updateTimeTick(int timeIncrement)
     * Updates the current time by the specified increment
     * @param timeIncrement - Time increment to update current time
     */
    private void updateTimeTick(int timeIncrement)
    {
        currentTime += timeIncrement;

        ObservableMessage message = new ObservableCPUTimeMessage(currentTime);
        notifySubscribers(message);
    }

    /**
     * public void cycle()
     * Determines the next process to run on the CPU. Starts running process based on scheduling algorithm
     */
    public void cycle()
    {
        checkAllProcessesBlocked();

        SchedulingProcess nextProcessToRun = schedulingAlgorithm.nextProcessToRun(processList);

        if(nextProcessToRun != null && !nextProcessToRun.isBlocked())
            schedulingAlgorithm.runProcess(nextProcessToRun, this);
        else if (nextProcessToRun.isBlocked())
        {
            processList.remove(nextProcessToRun);
            processList.add(nextProcessToRun);
            //updateTimeTick(1);
            //process is waiting for pages to be loaded into memory.
            //Cycle for RR cycles and see if it ends up being loaded during this time quantum
        }
        else
        {
            //No processes to run. Currently idling
            //updateTimeTick(1);
             System.out.println("No processes to run: " + currentTime);

        }
    }

    /**
     * private void issuePageFault(Page page)
     * Blocks the process that the page belongs to and creates a new page fault. Picked up by IO controller
     * and added to IO Request List
     * @param page - Page to fault
     */
    private void issuePageFault(Page page)
    {
        SchedulingProcess parentProcess = page.getParentProcess();
        parentProcess.block(currentTime);

        parentProcess.addPageFaultTimeToList(currentTime);

        ObservablePageFaultMessage pageFaultMessage = new ObservablePageFaultMessage(page, currentTime);
        notifySubscribers(pageFaultMessage);
    }

    /**
     * private void checkAllProcessesBlocked
     * Checks if all processes are in the blocked state.
     * If all in the blocked state, it will find the next time to update to
     */
    private void checkAllProcessesBlocked()
    {
        boolean allProcessesBlocked = true;
        for(SchedulingProcess process: processList)
        {
            allProcessesBlocked = process.isBlocked();
            if(!allProcessesBlocked)
                break;
        }

        //check if all processes blocked
        if(allProcessesBlocked)
        {
            //if all blocked, update to minimum time that pages is ready
            //Find minimum time to update to
            int n = getNextTimeReadyFromIOController();

            int delta = n-currentTime;
            updateTimeTick(delta);
        }
    }

    /**
     * private int getNextTimeReadyFromIOController()
     * @return - Next time value when page is loaded in to memory
     */
    private int getNextTimeReadyFromIOController()
    {
        return ioController.getNextIORequest().getPageReadyTime();
    }


    /**
     * public void addSubscriber(ISubscriber subscriber)
     * Adds a new subscriber to the subscribers list.
     * Utilised for the observer pattern
     * @param subscriber - New subscriber
     */
    @Override
    public void addSubscriber(ISubscriber subscriber) {
        subscriberList.add(subscriber);
    }


    /**
     * public void removeSubScriber(ISubscriber subscriber)
     * Removes a subscriber from the subscribers list
     * @param subscriber - Existing subscriber in the the list
     */
    @Override
    public void removeSubscriber(ISubscriber subscriber) {
        subscriberList.remove(subscriber);
    }


    /**
     * public void notifySubscribers(ObservableMessage message)
     * Broadcast a message from the CPU class to all subscribers of this class.
     * Utilised for the observer pattern
     * @param message - Observable Message to be sent to all subscribers
     */
    @Override
    public void notifySubscribers(ObservableMessage message) {
        for(ISubscriber subscriber: subscriberList)
            subscriber.handleMessage(message);
    }


    @Override
    public void handleMessage(ObservableMessage message) {
        if(message instanceof ObservableRemoveProcessReaddMessage)
        {
            SchedulingProcess process = ((ObservableRemoveProcessReaddMessage) message).getProcess();

            processList.remove(process);
            processList.add(process);
        }
    }
}

