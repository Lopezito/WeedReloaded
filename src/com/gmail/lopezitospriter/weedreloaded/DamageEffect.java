package com.gmail.lopezitospriter.weedreloaded;

import org.bukkit.event.entity.EntityDamageEvent.DamageCause;

public class DamageEffect{
	private DamageCause cause;
	private Amplifier amp;
	private int duration, amplitude;

	public DamageEffect(DamageCause cause, int duration, int amplitude, Amplifier amp){
		setCause(cause);
		setDuration(duration);
		setAmplitude(amplitude);
		setAmplifier(amp);
	}

	public DamageCause getCause(){
		return cause;
	}

	public void setCause(DamageCause cause){
		this.cause = cause;
	}

	public int getDuration(){
		return duration;
	}

	public void setDuration(int duration){
		this.duration = duration;
	}

	public int getAmplitude(){
		return amplitude;
	}

	public void setAmplitude(int amplitude){
		this.amplitude = amplitude;
	}

	public Amplifier getAmplifier(){
		return amp;
	}

	public void setAmplifier(Amplifier amp){
		this.amp = amp;
	}
}