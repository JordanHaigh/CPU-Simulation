import Algorithms.ClockPolicy;
import Algorithms.IPageReplacementAlgorithm;
import Machine.IOController;
import Machine.Memory;
import Model.Page;
import Model.PageGeneratorFactory;
import Model.SchedulingProcess;
import org.junit.*;
import java.util.*;

public class ClockPolicyTests
{
    @Test
    public void getReplacementIndex() throws Exception
    {
        PageGeneratorFactory factory = new PageGeneratorFactory();



        List<Page> pageList = new ArrayList<Page>();
        int arr[] = {1,2,3,4,5,6,7,8,9,10,9,8,7,6,5,11,12,1,2,3,4,5}; //{2,3,2,1,5,2,4,5,3,2,5,2};
        for(int i = 0; i < arr.length;i++)
        {
            Page page = factory.getPage(1,arr[i]);
            pageList.add(page);
        }



        SchedulingProcess schedulingProcess = new SchedulingProcess(1, pageList);

        for(Page page: pageList)
            page.linkProcessToPage(schedulingProcess);

        Memory memory = new Memory(1,10);
        IPageReplacementAlgorithm clock = new ClockPolicy(memory);
        memory.setPageReplacementAlgorithm(clock);


        for(Page page: pageList)
        {
            if(!page.isLoadedInMemory())
                memory.addToMemory(page,1);
            else
            {
                page.setUseBit(true);
            }
        }

        Assert.assertEquals(11, memory.getFrames()[0].getPageNumber());
        Assert.assertEquals(12, memory.getFrames()[1].getPageNumber());
        Assert.assertEquals(1, memory.getFrames()[2].getPageNumber());



    }

    @Test
    public void checkThreeProcessesRunningInMemory()
    {
        PageGeneratorFactory factory = new PageGeneratorFactory();



        List<Page> pageList = new ArrayList<Page>();
        int arr[] = {2,3,2,1,5,2,4,5,3,2,5,2};
        for(int i = 0; i < arr.length;i++)
        {
            Page page = factory.getPage(1,arr[i]);
            pageList.add(page);
        }



        SchedulingProcess schedulingProcess = new SchedulingProcess(1, pageList);

        for(Page page: pageList)
            page.linkProcessToPage(schedulingProcess);

        Memory memory = new Memory(1,3);
        IPageReplacementAlgorithm clock = new ClockPolicy(memory);
        memory.setPageReplacementAlgorithm(clock);


        for(Page page: pageList)
        {
            if(!page.isLoadedInMemory())
                memory.addToMemory(page,1);

            break;
        }

        Assert.assertEquals(1, schedulingProcess.countDistinctProcessesRunning());


    }

}