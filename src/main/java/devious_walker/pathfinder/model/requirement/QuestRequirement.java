package devious_walker.pathfinder.model.requirement;

import devious_walker.quests.Quests;
import lombok.Value;
import net.runelite.api.Quest;
import net.runelite.api.QuestState;

import java.util.Set;

@Value
public class QuestRequirement implements Requirement
{
    Quest quest;
    Set<QuestState> states;

    @Override
    public Boolean get()
    {
        return states.contains(Quests.getState(quest));
    }
}
