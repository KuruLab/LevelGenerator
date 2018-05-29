package parallelEvaluation;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Process extends Thread {

    private String threadName;
    // a pilha de tasks funciona como um recurso (Resource) interno (local) a cada processo
    private Resource<Runnable> tasksQueue;
    private boolean finished;

    public Process(String name) {
        this.threadName = name;
        this.tasksQueue = new Resource(name);
        this.finished = false;
    }

    public Process(String name, Resource queue) {
        this.threadName = name;
        this.tasksQueue = queue;
        this.finished = false;
    }

    public Resource<Runnable> getTasksQueue() {
        return tasksQueue;
    }

    public void setTasksQueue(Resource<Runnable> tasksQueue) {
        this.tasksQueue = tasksQueue;
    }

    public String getThreadName() {
        return threadName;
    }

    public void setThreadName(String threadName) {
        this.threadName = threadName;
    }

    public void addTask(Runnable task) {
        //System.out.println("Processo \""+this.threadName+"\" adicionando Task na pilha");
        this.tasksQueue.putRegister(task);
    }

    public void setFinished() {
        this.finished = true;
        this.tasksQueue.setFinished();
        //System.out.println("Processo \""+this.threadName+"\" finalizando...");
    }

    public boolean isFinished() {
        return this.finished;
    }

    @Override
    public void run() {
        //System.out.println("Processo \""+this.threadName+"\" em execucao.");
        // somente sai do loop quando todas as tasks forem executadas
        // quando nao ha mais tasks, o processo eh pausado ate que mais tasks sejam adicionadas
        // a taskQueue eh finalizada pela MainTask, quando atribiu este processo como finalizado
        while (!tasksQueue.isFinished() || tasksQueue.getNumOfRegisters() > 0) {
            Runnable nextTask = null;
            try {
                nextTask = this.tasksQueue.getRegister();
            } catch (Exception ex) {
                Logger.getLogger(Process.class.getName()).log(Level.SEVERE, null, ex);
            }
            if (nextTask == null) {
                try {
                    tasksQueue.suspend();
                } catch (Exception ex) {
                    Logger.getLogger(Process.class.getName()).log(Level.SEVERE, null, ex);
                }
            } else {
                //printRunningTask(nextTask.getTaskName());
                nextTask.run();
            }
        }
        //System.err.println(this.threadName+": Finalizado.");
    }
}
