package parallelEvaluation;

import java.util.LinkedList;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Resource<S> {

    private LinkedList<S> registers;
    private boolean finished;
    private int counter;
    private String name;

    public Resource(String name) {
        this.registers = new LinkedList<S>();
        this.finished = false;
        this.counter = 0;
        this.name = name;
    }

    public synchronized void incrementBreed() {
        this.counter++;
    }

    public synchronized LinkedList<S> getRegisters() {
        return this.registers;
    }

    public synchronized void putRegister(S register) {
        this.registers.addLast(register);
        this.incrementBreed();
        //System.err.println(name + " new resource... " + this.registers.size());
        this.notifyAll();
    }

    public synchronized void putRegister(int index, S register) {
        this.registers.add(index, register);
        this.incrementBreed();
        //System.err.println(name + " new resource... " + this.registers.size());
        this.notifyAll();
    }

    public synchronized S getRegister() throws Exception {
        if (this.registers.size() == 0) {
            if (this.isFinished() == true) {
                return null;
            } else {
                //System.err.println(name + " waiting for resource...");
                this.wait();
                //System.err.println(name + " woke up...");
                return getRegister();
            }
        } else {
            return this.registers.removeFirst();
        }
    }

    public synchronized S getRegister(int index) {
        try {
            if (this.registers.size() == 0) {
                if (this.isFinished() == true) {
                    return null;
                } else {
                    //System.err.println(name + " waiting for resource...");
                    this.wait();
                    //System.err.println(name + " woke up...");

                    return getRegister(index);

                }
            } else {
                return this.registers.get(index);
            }
        } catch (Exception ex) {
            //Logger.getLogger(Resource.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
    }

    public synchronized S removeRegister(int index) {
        try {
            if (this.registers.size() == 0) {
                if (this.isFinished() == true) {
                    return null;
                } else {
                    //System.err.println(name + " waiting for resource...");
                    this.wait();
                    //System.err.println(name + " woke up...");

                    return removeRegister(index);

                }
            } else {
                return this.registers.remove(index);
            }
        } catch (Exception ex) {
            Logger.getLogger(Resource.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
    }

    public int getNumOfRegisters() {
        return this.registers.size();
    }

    public synchronized void setFinished() {
        //System.err.println("Finalizando recurso " + name + "... registros restantes: " + this.getNumOfRegisters());
        this.finished = true;
        this.notifyAll();
    }

    public boolean isFinished() {
        return this.finished;
    }

    public synchronized void suspend() throws Exception {
        this.wait();
    }

    public synchronized void wakeUp() {
        this.notifyAll();
    }

    public synchronized int getCounter() {
        return counter;
    }
}
