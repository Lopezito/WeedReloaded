package com.gmail.lopezitospriter.weedreloaded;

import java.util.Collection;
import java.util.HashSet;

import org.bukkit.Bukkit;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.Event;

public class Drug{
	private Activation activation;
	private String[] args;
	private HashSet<WEffect> effects;

	public Drug(Activation activation, String[] args, HashSet<WEffect> effects){
		setActivation(activation);
		setArgs(args);
		setEffects(effects);
	}

	public Activation getActivation(){
		return activation;
	}

	public void setActivation(Activation activation){
		this.activation = activation;
	}

	public String[] getArgs(){
		return args;
	}

	public void setArgs(String[] args){
		this.args = args;
	}

	public HashSet<WEffect> getEffects(){
		return effects;
	}

	public void setEffects(HashSet<WEffect> effects){
		this.effects = effects;
	}

	public void activate(Event event){
		Collection<LivingEntity> c = activation.activate(event, args);
		
		if(c != null)
			for(final LivingEntity e : c)
				for(final WEffect effect : effects)
					Bukkit.getScheduler().scheduleSyncDelayedTask(
							Weed.plugin,
							new Runnable(){
								@Override
								public void run(){
									if(e != null && !e.isDead())
										effect.apply(e);
								}
							},
							effect.getDelay() * 20
							);
	}
}