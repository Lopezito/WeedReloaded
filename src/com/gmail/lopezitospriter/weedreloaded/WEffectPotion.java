package com.gmail.lopezitospriter.weedreloaded;

import org.bukkit.entity.LivingEntity;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class WEffectPotion extends WEffect{
	private PotionEffectType effect;
	private Amplifier amp;
	private int amplitude, max_amplitude;

	public WEffectPotion(PotionEffectType effect, int delay, int duration, int amplifyer, int max_amplifyer, Amplifier amp){
		this.effect = effect;
		this.delay = delay;
		this.duration = duration;
		amplitude = amplifyer;
		max_amplitude = max_amplifyer;
		this.amp = amp;
	}

	public PotionEffectType getEffect(){
		return effect;
	}

	@Override
	public String getId(){
		return "potion_" + effect.getName();
	}

	@Override
	public void apply(LivingEntity entity){
		Weed.addPotionEffect(entity, new PotionEffect(effect, duration * 20, amp.getResult(Weed.getPotionEffectAmplitude(entity, effect), amplitude, max_amplitude)));
	}
}