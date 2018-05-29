package parallelEvaluation;

import evoGraph.GeneticAlgorithm;

/**
 *
 * @author Kurumin
 */
public class Notifier implements Runnable {

    public GeneticAlgorithm ga;
    public boolean finished;

    public Notifier(GeneticAlgorithm ga) {
        this.ga = ga;
        this.finished = false;
    }

    @Override
    public void run() {
        int prev = 0;
        while (!finished) {
            try {
                Thread.sleep(10);
                int gen = ga.getCurrentGeneration();

                if (prev == gen) {
                    ga.wakeUp();
                    if (ga.getProcesses() != null) {
                        for (int j = 0; j < ga.getProcesses().length; j++) {
                            if (ga.getProcesses()[j] != null) {
                                ga.getProcesses()[j].getTasksQueue().wakeUp();
                            }
                        }
                    }
                }
                prev = gen;
            } catch (InterruptedException ex) {
                ex.printStackTrace();
            }
        }
    }

    public boolean isFinished() {
        return finished;
    }

    public void setFinished(boolean finished) {
        this.finished = finished;
    }
}
