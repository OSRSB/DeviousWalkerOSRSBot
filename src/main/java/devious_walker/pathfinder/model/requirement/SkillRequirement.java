package devious_walker.pathfinder.model.requirement;

import lombok.Value;
import net.runelite.api.Skill;

import static net.runelite.rsb.methods.MethodProvider.methods;

@Value
public class SkillRequirement implements Requirement
{
    Skill skill;
    int level;

    @Override
    public Boolean get()
    {
        return methods.skills.getRealLevel(skill) >= level;
    }
}
