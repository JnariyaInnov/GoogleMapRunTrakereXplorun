package brice.explorun.models;

import java.util.ArrayList;

public abstract class Observable
{
	private ArrayList<Observer> observers;

	public Observable()
	{
		this.observers = new ArrayList<>();
	}

	public void registerObserver(Observer obj)
	{
		this.observers.add(obj);
	}

	public void unregisterObserver(Observer obj)
	{
		this.observers.remove(obj);
	}

	public void notifyAllObservers()
	{
		for (Observer o: this.observers)
		{
			o.update();
		}
	}
}
