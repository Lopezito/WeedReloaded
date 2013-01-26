package com.gmail.lopezitospriter.weedreloaded;

import java.util.ArrayList;

import org.bukkit.entity.LivingEntity;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.potion.PotionEffectType;

public abstract class WEffect{
	protected int delay;
	protected int duration;

	public abstract String getId();

	public int getDelay(){
		return delay;
	}

	public int getDuration(){
		return duration;
	}

	public abstract void apply(LivingEntity entity);

	public static boolean isPotion(String id){
		return id.startsWith("potion_");
	}

	public static ArrayList<String> getAvailableEffects(){
		ArrayList<String> e = new ArrayList<String>();

		for(PotionEffectType t : PotionEffectType.values())
			if(t != null)
				e.add("potion_" + t.getName());
		for(DamageCause d : DamageCause.values())
			if(d != null)
				e.add("damage_" + d.toString());
		return e;
	}
}