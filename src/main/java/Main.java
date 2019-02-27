import org.telegram.telegrambots.ApiContextInitializer;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import sun.misc.Unsafe;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Timer;
import java.util.TimerTask;

public class Main {

    public static void main(String[] args) {

        //disable warning "An illegal reflective access operation has occurred"
        Main.disableWarning();

        Main.initTelegBotsAPI();
        //configure to repeat once a minute
        Proxy proxy  = new Proxy();
        try {
            proxy.getNewProxyArray();
        } catch (IOException e) {
            e.printStackTrace();
        }
        Timer timer = new Timer();
        TimerTask myTask = new TimerTask() {
            @Override
            public void run() {
                Parser ps = new Parser(proxy);
                ps.parseMainPage();
            }
        };
        timer.schedule(myTask, 0, 30000);
    }

    public static void initTelegBotsAPI(){
        ApiContextInitializer.init();

        TelegramBotsApi botsApi = new TelegramBotsApi();

        try {
            botsApi.registerBot(new TelegramBot());
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    public static void disableWarning() {
        try {
            Field theUnsafe = Unsafe.class.getDeclaredField("theUnsafe"); theUnsafe.setAccessible(true);
            Unsafe u = (Unsafe) theUnsafe.get(null);
            Class cls = Class.forName("jdk.internal.module.IllegalAccessLogger");
            Field logger = cls.getDeclaredField("logger");
            u.putObjectVolatile(cls, u.staticFieldOffset(logger), null);
        } catch (Exception e) {
            // ignore
        }
    }

}