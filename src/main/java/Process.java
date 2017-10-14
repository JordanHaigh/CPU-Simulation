import java.util.ArrayList;
import java.util.List;

public class Process
{
    private List<Page> pageList = new ArrayList<>();
    private static final int MAX_PAGES = 50;
    private String id;

     private int startTime;
    private int finishTime;
    private ProcessState processState;



    public Process(String id, List<Page> pageList)
    {
        this.id = id;
        this.pageList = pageList;

        startTime = 0;
        processState = ProcessState.NEW;
    }

    public String getId() { return id; }
    public boolean isNew() { return processState.equals(ProcessState.NEW); }
    public boolean isReady() { return processState.equals(ProcessState.READY); }
    public boolean isRunning() { return processState.equals(ProcessState.RUNNING); }

    public void admit(int currentTime)
    {
        //Precondition check to determine if process is in the NEW state
        if(isNew())
        {
            //Update state
            processState = ProcessState.READY;
            //stateTransitionMessage(ProcessState.READY, currentTime);

        }
        else
            runTimeExceptionMessage(ProcessState.NEW);
    }

    public void dispatch(int currentTime)
    {
        //Precondition check to determine if the process is in the READY state
        if(isReady())
        {
            //Update state
            processState = ProcessState.RUNNING;
            //stateTransitionMessage(ProcessState.RUNNING, currentTime);

        }
        else
            runTimeExceptionMessage(ProcessState.READY);
    }

    public void interrupt(int currentTime)
    {
        //Precondition check to determine if the process is in the RUNNING state
        if(isRunning())
        {
            //Update state
            processState = ProcessState.READY;
            //stateTransitionMessage(ProcessState.READY, currentTime);

        }
        else
            runTimeExceptionMessage(ProcessState.RUNNING);
    }

    public void exit(int currentTime)
    {
        if(isRunning())
        {
            //Update State
            processState = ProcessState.TERMINATED;
            //stateTransitionMessage(ProcessState.TERMINATED, currentTime);

            //Update finishTime
            finishTime = currentTime;
        }
    }

    /**
     * private void runtimeExceptionMessage(ProcessState requiredState)
     * Throws Runtime Exception Message
     * @param requiredState - State the process is meant to be in
     */
    private void runTimeExceptionMessage(ProcessState requiredState)
    {
        throw new RuntimeException("Process is not in the " + requiredState + "state for correct transition. Actual State: " + processState);
    }

}

