package brice.explorun.models;

public interface RouteObserver
{
	void onFormValidate(int sport, int leftPinValue, int rightPinValue);
	void onRouteStart();
	void onRouteStop();
}
