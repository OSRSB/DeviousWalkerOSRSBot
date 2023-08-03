package devious_walker;


import lombok.extern.slf4j.Slf4j;
import net.runelite.client.RuneLite;
import net.runelite.client.callback.ClientThread;
import net.runelite.rsb.botLauncher.BotLite;

import java.util.concurrent.*;

import static net.runelite.rsb.methods.MethodProvider.methods;

@Slf4j
public class GameThread
{
    private static final long TIMEOUT = 1000;

    static ClientThread clientThread;

    public static void invoke(Runnable runnable)
    {
        if (clientThread == null) {
            clientThread = RuneLite.getInjector().getInstance(ClientThread.class);
        }
        if (methods.client.isClientThread())
        {
            runnable.run();
        }
        else
        {
            clientThread.invoke(runnable);
        }
    }

    public static <T> T invokeLater(Callable<T> callable)
    {
        if (clientThread == null) {
            clientThread = RuneLite.getInjector().getInstance(ClientThread.class);
        }

        if (methods.client.isClientThread())
        {
            try
            {
                return callable.call();
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }

        try
        {
            FutureTask<T> futureTask = new FutureTask<>(callable);
            clientThread.invokeLater(futureTask);
            return futureTask.get(TIMEOUT, TimeUnit.MILLISECONDS);
        }
        catch (ExecutionException | InterruptedException | TimeoutException e)
        {
            e.printStackTrace();
            throw new RuntimeException("Client thread invoke timed out after " + TIMEOUT + " ms");
        }
    }
}