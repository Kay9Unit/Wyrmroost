package WolfShotz.Wyrmroost.entities.dragon;

import WolfShotz.Wyrmroost.entities.dragon.helpers.goals.DefendHomeGoal;
import WolfShotz.Wyrmroost.entities.dragon.helpers.goals.DragonBreedGoal;
import WolfShotz.Wyrmroost.entities.dragon.helpers.goals.MoveToHomeGoal;
import WolfShotz.Wyrmroost.entities.util.CommonGoalWrappers;
import WolfShotz.Wyrmroost.util.TickFloat;
import com.google.common.collect.ImmutableSet;
import net.minecraft.entity.*;
import net.minecraft.entity.ai.goal.*;
import net.minecraft.entity.passive.BeeEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Items;
import net.minecraft.util.IItemProvider;
import net.minecraft.world.World;

import java.util.Collection;

import static net.minecraft.entity.SharedMonsterAttributes.*;

public class AlpineEntity extends AbstractDragonEntity
{
    public final TickFloat sitTime = new TickFloat().setLimit(0, 1);

    public AlpineEntity(EntityType<? extends AbstractDragonEntity> dragon, World world)
    {
        super(dragon, world);

        registerVariantData(6, false);
    }

    @Override
    protected void registerAttributes()
    {
        super.registerAttributes();

        getAttribute(MAX_HEALTH).setBaseValue(40d); // 20 hearts
        getAttribute(MOVEMENT_SPEED).setBaseValue(0.22d);
        getAttribute(KNOCKBACK_RESISTANCE).setBaseValue(1); // no knockback
        getAttributes().registerAttribute(ATTACK_DAMAGE).setBaseValue(5d); // 2.5 hearts
        getAttributes().registerAttribute(FLYING_SPEED).setBaseValue(0.08);
        getAttributes().registerAttribute(PROJECTILE_DAMAGE).setBaseValue(1d); // 0.5 hearts
    }

    @Override
    protected void registerGoals()
    {
        super.registerGoals();

        goalSelector.addGoal(4, new MoveToHomeGoal(this));
        goalSelector.addGoal(5, new MeleeAttackGoal(this, 1.1d, true));
        goalSelector.addGoal(6, CommonGoalWrappers.followOwner(this, 1.1, 13, 5));
        goalSelector.addGoal(7, new DragonBreedGoal(this, false));
        goalSelector.addGoal(8, new WaterAvoidingRandomWalkingGoal(this, 1));
        goalSelector.addGoal(9, new LookAtGoal(this, LivingEntity.class, 10));
        goalSelector.addGoal(10, new LookRandomlyGoal(this));

        targetSelector.addGoal(1, new OwnerHurtByTargetGoal(this));
        targetSelector.addGoal(2, new OwnerHurtTargetGoal(this));
        targetSelector.addGoal(3, new DefendHomeGoal(this));
        targetSelector.addGoal(4, new HurtByTargetGoal(this));
        targetSelector.addGoal(5, CommonGoalWrappers.nonTamedTarget(this, BeeEntity.class, true, false, bee -> ((BeeEntity) bee).hasNectar()));
    }

    @Override
    public void livingTick()
    {
        super.livingTick();
        sitTime.add(isSitting() || isSleeping()? 0.1f : -0.1f);
        sleepTimer.add(isSleeping()? 0.1f : -0.1f);
    }

    @Override
    public boolean attackEntityAsMob(Entity enemy)
    {
        boolean flag = super.attackEntityAsMob(enemy);

        if (!isTamed() && flag && !enemy.isAlive() && enemy.getType() == EntityType.BEE)
        {
            BeeEntity bee = (BeeEntity) enemy;
            if (bee.hasNectar() && bee.getLeashed())
            {
                Entity holder = bee.getLeashHolder();
                if (holder instanceof PlayerEntity) tame(true, (PlayerEntity) holder);
            }
        }
        return flag;
    }

    @Override
    protected boolean canBeRidden(Entity entityIn) { return true; }

    @Override
    public Collection<? extends IItemProvider> getFoodItems() { return ImmutableSet.of(Items.HONEYCOMB, Items.HONEY_BOTTLE); }

    @Override
    protected float getStandingEyeHeight(Pose poseIn, EntitySize sizeIn) { return sizeIn.height * 1.25f; }
}
