package p2;

public class SecondThread implements Runnable {
    @Override
    public void run() {
        System.out.println("SecondThread");
    }
}
