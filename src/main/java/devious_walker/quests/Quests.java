package devious_walker.quests;


import net.runelite.api.Quest;
import net.runelite.api.QuestState;

import static net.runelite.rsb.methods.MethodProvider.methods;

public class Quests
{

    public static QuestState getState(Quest quest)
    {
        return quest.getState(methods.client);
    }

    public static boolean isFinished(Quest quest)
    {
        return getState(quest) == QuestState.FINISHED;
    }

}