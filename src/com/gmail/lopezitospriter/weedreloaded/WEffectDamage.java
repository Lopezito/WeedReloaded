package com.gmail.lopezitospriter.weedreloaded;

import org.bukkit.entity.LivingEntity;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;

public class WEffectDamage extends WEffect{
	private DamageCause cause;
	private Amplifier amp;
	private int amplitude;

	public WEffectDamage(DamageCause cause, int delay, int duration, int amplifyer, Amplifier amp){
		this.cause = cause;
		this.delay = delay;
		this.duration = duration;
		amplitude = amplifyer;
		this.amp = amp;
	}

	public DamageCause getCause(){
		return cause;
	}

	@Override
	public String getId(){
		return "damage_" + getCause().toString();
	}

	@Override
	public void apply(LivingEntity entity){
		Weed.addDamageEffect(entity, new DamageEffect(cause, duration, amplitude, amp));
	}
}