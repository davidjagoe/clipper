public class HelloJavaLtkMain 
{
    public static void main(String[] args) throws InterruptedException 
    {
        HelloJavaLtk app = new HelloJavaLtk();
        
        System.out.println("Starting reader.");
        app.run("10.2.0.99");
        Thread.sleep(30000);
        System.out.println("Stopping reader.");
        app.stop();
        System.out.println("Exiting application.");
        System.exit(0);
    }
}