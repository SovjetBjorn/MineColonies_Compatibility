package steve_gall.minecolonies_compatibility.module.common.flansmod;

import com.minecolonies.api.entity.citizen.AbstractEntityCitizen;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import steve_gall.minecolonies_compatibility.api.common.entity.ai.CustomizedAIContext;
import steve_gall.minecolonies_compatibility.api.common.entity.ai.guard.CustomizedAIGunner;
import steve_gall.minecolonies_compatibility.core.common.MineColoniesCompatibility;
import steve_gall.minecolonies_compatibility.core.common.config.MineColoniesCompatibilityConfigServer;
import steve_gall.minecolonies_tweaks.api.common.requestsystem.IDeliverableObject;
import com.flansmod.common.guns.ItemGun;
import com.flansmod.common.guns.AmmoType;

public class GunnerFlansModAI extends CustomizedAIGunner
{
    public static final String TAG_KEY = MineColoniesCompatibility.rl("flansmod_gunner").toString();

    public GunnerFlansModAI()
    {
    }

    @Override
    public boolean test(@NotNull CustomizedAIContext context)
    {
        return super.test(context) && context.getWeapon().getItem() instanceof ItemGun;
    }

    @Override
    protected boolean testAmmo(ItemStack stack)
    {
        return stack.getItem() instanceof AmmoType;
    }

    @Override
    protected IDeliverableObject createAmmoRequest(int minCount)
    {
        return new FlansModAmmo(minCount);
    }

    @Override
    protected boolean isAmmoRequest(IDeliverableObject object)
    {
        return object instanceof FlansModAmmo;
    }

    @Override
    public boolean canMeleeAttack(@NotNull CustomizedAIContext context, @NotNull LivingEntity target)
    {
        return true;
    }

    @Override
    public boolean canRangedAttack(@NotNull CustomizedAIContext context, @NotNull LivingEntity target)
    {
        var user = context.getUser();

        if (!super.canRangedAttack(context, target))
        {
            return false;
        }

        if (this.getWeaponConfig().needReload.get().booleanValue() && this.getBulletCount(user) <= 0)
        {
            if (!this.reload(user))
            {
                return false;
            }
        }

        return true;
    }

    @Override
    protected void onReloadStarted(@NotNull AbstractEntityCitizen user)
    {
        super.onReloadStarted(user);

        user.playSound(SoundEvents.GUN_RELOAD.get(), 1.0F, 1.0F);
    }

    @Override
    protected void onReloadStopped(@NotNull AbstractEntityCitizen user, boolean complete)
    {
        super.onReloadStopped(user, complete);

        if (complete)
        {
            this.setBulletCount(user, 30); // Assuming FlansMod guns have a default magazine size of 30
        }
    }

    @Override
    public float getMeleeAttackDamage(@NotNull CustomizedAIContext context, @NotNull LivingEntity target)
    {
        var weapon = context.getWeapon();
        var damage = super.getMeleeAttackDamage(context, target);

        return damage;
    }

    @Override
    public void doRangedAttack(@NotNull CustomizedAIContext context, @NotNull LivingEntity target)
    {
        var user = context.getUser();
        var weapon = context.getWeapon();
        var level = user.level();

        // Implement FlansMod gun firing logic here

        if (this.getWeaponConfig().needReload.get().booleanValue())
        {
            this.setBulletCount(user, this.getBulletCount(user) - 1);
        }
    }

    @Override
    protected AttackDelayConfig getAttackDealyConfig()
    {
        return this.getWeaponConfig().attackDelay;
    }

    @Override
    public double getAttackDistance(@NotNull CustomizedAIContext context, @NotNull LivingEntity target)
    {
        var weapon = context.getWeapon();
        var distance = super.getAttackDistance(context, target);

        return distance;
    }

    @Override
    public double getHorizontalSearchRange(@NotNull CustomizedAIContext context)
    {
        var weapon = context.getWeapon();
        var range = super.getHorizontalSearchRange(context);

        return range;
    }

    @Override
    @NotNull
    public String getTagKey()
    {
        return TAG_KEY;
    }

    public FlansModConfig getWeaponConfig()
    {
        return MineColoniesCompatibilityConfigServer.INSTANCE.modules.FlansMod.job.gunner;
    }

    public int getBulletCount(@NotNull AbstractEntityCitizen user)
    {
        return this.getOrEmptyTag(user).getInt("bulletCount");
    }

    public void setBulletCount(@NotNull AbstractEntityCitizen user, int count)
    {
        this.getOrCreateTag(user).putInt("bulletCount", Math.max(count, 0));
    }

    @Override
    protected int getReloadDuration()
    {
        return this.getWeaponConfig().reloadDuration.get().intValue();
    }
}